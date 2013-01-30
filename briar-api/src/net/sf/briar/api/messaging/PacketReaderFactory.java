package net.sf.briar.api.messaging;

import java.io.InputStream;

public interface PacketReaderFactory {

	PacketReader createPacketReader(InputStream in);
}
