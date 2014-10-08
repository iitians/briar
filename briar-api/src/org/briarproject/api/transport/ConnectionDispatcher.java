package org.briarproject.api.transport;

import org.briarproject.api.ContactId;
import org.briarproject.api.TransportId;
import org.briarproject.api.plugins.duplex.DuplexTransportConnection;
import org.briarproject.api.plugins.simplex.SimplexTransportReader;
import org.briarproject.api.plugins.simplex.SimplexTransportWriter;

public interface ConnectionDispatcher {

	void dispatchIncomingConnection(TransportId t, SimplexTransportReader r);

	void dispatchIncomingConnection(TransportId t, DuplexTransportConnection d);

	void dispatchOutgoingConnection(ContactId c, TransportId t,
			SimplexTransportWriter w);

	void dispatchOutgoingConnection(ContactId c, TransportId t,
			DuplexTransportConnection d);
}
