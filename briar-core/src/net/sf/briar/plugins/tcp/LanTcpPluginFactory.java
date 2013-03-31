package net.sf.briar.plugins.tcp;

import java.util.concurrent.Executor;

import net.sf.briar.api.TransportId;
import net.sf.briar.api.clock.Clock;
import net.sf.briar.api.clock.SystemClock;
import net.sf.briar.api.plugins.duplex.DuplexPlugin;
import net.sf.briar.api.plugins.duplex.DuplexPluginCallback;
import net.sf.briar.api.plugins.duplex.DuplexPluginFactory;

public class LanTcpPluginFactory implements DuplexPluginFactory {

	private static final long MAX_LATENCY = 60 * 1000; // 1 minute
	private static final long POLLING_INTERVAL = 60 * 1000; // 1 minute

	private final Executor pluginExecutor;
	private final Clock clock;

	public LanTcpPluginFactory(Executor pluginExecutor) {
		this.pluginExecutor = pluginExecutor;
		clock = new SystemClock();
	}

	public TransportId getId() {
		return LanTcpPlugin.ID;
	}

	public DuplexPlugin createPlugin(DuplexPluginCallback callback) {
		return new LanTcpPlugin(pluginExecutor, clock, callback, MAX_LATENCY,
				POLLING_INTERVAL);
	}
}
