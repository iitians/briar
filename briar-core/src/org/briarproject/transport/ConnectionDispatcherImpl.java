package org.briarproject.transport;

import static java.util.logging.Level.WARNING;
import static org.briarproject.api.transport.TransportConstants.TAG_LENGTH;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.briarproject.api.ContactId;
import org.briarproject.api.TransportId;
import org.briarproject.api.db.DbException;
import org.briarproject.api.lifecycle.IoExecutor;
import org.briarproject.api.messaging.duplex.DuplexConnectionFactory;
import org.briarproject.api.messaging.simplex.SimplexConnectionFactory;
import org.briarproject.api.plugins.duplex.DuplexTransportConnection;
import org.briarproject.api.plugins.simplex.SimplexTransportReader;
import org.briarproject.api.plugins.simplex.SimplexTransportWriter;
import org.briarproject.api.transport.ConnectionDispatcher;
import org.briarproject.api.transport.StreamContext;
import org.briarproject.api.transport.TagRecogniser;

class ConnectionDispatcherImpl implements ConnectionDispatcher {

	private static final Logger LOG =
			Logger.getLogger(ConnectionDispatcherImpl.class.getName());

	private final Executor ioExecutor;
	private final TagRecogniser tagRecogniser;
	private final SimplexConnectionFactory simplexConnFactory;
	private final DuplexConnectionFactory duplexConnFactory;

	@Inject
	ConnectionDispatcherImpl(@IoExecutor Executor ioExecutor,
			TagRecogniser tagRecogniser,
			SimplexConnectionFactory simplexConnFactory,
			DuplexConnectionFactory duplexConnFactory) {
		this.ioExecutor = ioExecutor;
		this.tagRecogniser = tagRecogniser;
		this.simplexConnFactory = simplexConnFactory;
		this.duplexConnFactory = duplexConnFactory;
	}

	public void dispatchIncomingConnection(TransportId t,
			SimplexTransportReader r) {
		ioExecutor.execute(new DispatchSimplexConnection(t, r));
	}

	public void dispatchIncomingConnection(TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new DispatchDuplexConnection(t, d));
	}

	public void dispatchOutgoingConnection(ContactId c, TransportId t,
			SimplexTransportWriter w) {
		simplexConnFactory.createOutgoingConnection(c, t, w);
	}

	public void dispatchOutgoingConnection(ContactId c, TransportId t,
			DuplexTransportConnection d) {
		duplexConnFactory.createOutgoingConnection(c, t, d);
	}

	private byte[] readTag(InputStream in) throws IOException {
		byte[] b = new byte[TAG_LENGTH];
		int offset = 0;
		while(offset < b.length) {
			int read = in.read(b, offset, b.length - offset);
			if(read == -1) throw new EOFException();
			offset += read;
		}
		return b;
	}

	private class DispatchSimplexConnection implements Runnable {

		private final TransportId transportId;
		private final SimplexTransportReader transport;

		private DispatchSimplexConnection(TransportId transportId,
				SimplexTransportReader transport) {
			this.transportId = transportId;
			this.transport = transport;
		}

		public void run() {
			try {
				byte[] tag = readTag(transport.getInputStream());
				StreamContext ctx = tagRecogniser.recogniseTag(transportId,
						tag);
				if(ctx == null) {
					transport.dispose(false, false);
				} else {
					simplexConnFactory.createIncomingConnection(ctx,
							transport);
				}
			} catch(DbException e) {
				if(LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
				try {
					transport.dispose(true, false);
				} catch(IOException e1) {
					if(LOG.isLoggable(WARNING))
						LOG.log(WARNING, e1.toString(), e1);
				}
			} catch(IOException e) {
				if(LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
				try {
					transport.dispose(true, false);
				} catch(IOException e1) {
					if(LOG.isLoggable(WARNING))
						LOG.log(WARNING, e1.toString(), e1);
				}
			}
		}
	}

	private class DispatchDuplexConnection implements Runnable {

		private final TransportId transportId;
		private final DuplexTransportConnection transport;

		private DispatchDuplexConnection(TransportId transportId,
				DuplexTransportConnection transport) {
			this.transportId = transportId;
			this.transport = transport;
		}

		public void run() {
			byte[] tag;
			try {
				tag = readTag(transport.getInputStream());
			} catch(IOException e) {
				if(LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
				dispose(true, false);
				return;
			}
			StreamContext ctx = null;
			try {
				ctx = tagRecogniser.recogniseTag(transportId, tag);
			} catch(DbException e) {
				if(LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
				dispose(true, false);
				return;
			}
			if(ctx == null) dispose(false, false);
			else duplexConnFactory.createIncomingConnection(ctx, transport);
		}

		private void dispose(boolean exception, boolean recognised) {
			try {
				transport.dispose(exception, recognised);
			} catch(IOException e) {
				if(LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
			}
		}
	}
}