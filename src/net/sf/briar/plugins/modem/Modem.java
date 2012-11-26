package net.sf.briar.plugins.modem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A modem that can be used for multiple sequential incoming and outgoing
 * calls. If an exception is thrown, a new modem instance must be created.
 */
interface Modem {

	/**
	 * Call this method after creating the modem and before making any calls.
	 */
	void init() throws IOException;

	/**
	 * Initiates an outgoing call and returns true if the call connects. If the
	 * call does not connect the modem is hung up.
	 */
	boolean dial(String number) throws IOException;

	/** Returns a stream for reading from the currently connected call. */
	InputStream getInputStream();

	/** Returns a stream for writing to the currently connected call. */
	OutputStream getOutputStream();

	/** Hangs up the modem, ending the currently connected call. */
	void hangUp() throws IOException;

	interface Callback {

		/** Called when an incoming call connects. */
		void incomingCallConnected();
	}
}
