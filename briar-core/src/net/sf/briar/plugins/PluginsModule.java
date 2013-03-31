package net.sf.briar.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.briar.api.android.AndroidExecutor;
import net.sf.briar.api.crypto.CryptoComponent;
import net.sf.briar.api.lifecycle.ShutdownManager;
import net.sf.briar.api.plugins.PluginExecutor;
import net.sf.briar.api.plugins.PluginManager;
import net.sf.briar.api.plugins.duplex.DuplexPluginConfig;
import net.sf.briar.api.plugins.duplex.DuplexPluginFactory;
import net.sf.briar.api.plugins.simplex.SimplexPluginConfig;
import net.sf.briar.api.plugins.simplex.SimplexPluginFactory;
import net.sf.briar.api.reliability.ReliabilityLayerFactory;
import net.sf.briar.plugins.bluetooth.BluetoothPluginFactory;
import net.sf.briar.plugins.droidtooth.DroidtoothPluginFactory;
import net.sf.briar.plugins.file.RemovableDrivePluginFactory;
import net.sf.briar.plugins.modem.ModemPluginFactory;
import net.sf.briar.plugins.tcp.LanTcpPluginFactory;
import net.sf.briar.plugins.tcp.WanTcpPluginFactory;
import net.sf.briar.util.OsUtils;
import android.content.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class PluginsModule extends AbstractModule {

	@Override
	protected void configure() {
		// The executor is unbounded, so tasks can be dependent or long-lived
		ExecutorService e = Executors.newCachedThreadPool();
		bind(ExecutorService.class).annotatedWith(
				PluginExecutor.class).toInstance(e);
		bind(PluginManager.class).to(
				PluginManagerImpl.class).in(Singleton.class);
		bind(Poller.class).to(PollerImpl.class);
	}

	@Provides
	SimplexPluginConfig getSimplexPluginConfig(
			@PluginExecutor ExecutorService pluginExecutor) {
		final Collection<SimplexPluginFactory> factories =
				new ArrayList<SimplexPluginFactory>();
		if(!OsUtils.isAndroid()) {
			// No simplex plugins for Android
		} else {
			factories.add(new RemovableDrivePluginFactory(pluginExecutor));
		}
		return new SimplexPluginConfig() {
			public Collection<SimplexPluginFactory> getFactories() {
				return factories;
			}
		};
	}

	@Provides
	DuplexPluginConfig getDuplexPluginConfig(
			@PluginExecutor ExecutorService pluginExecutor,
			AndroidExecutor androidExecutor, Context appContext,
			ReliabilityLayerFactory reliabilityFactory,
			ShutdownManager shutdownManager, CryptoComponent crypto) {
		final Collection<DuplexPluginFactory> factories =
				new ArrayList<DuplexPluginFactory>();
		if(OsUtils.isAndroid()) {
			factories.add(new DroidtoothPluginFactory(pluginExecutor,
					androidExecutor, appContext, crypto.getSecureRandom()));
		} else {
			factories.add(new BluetoothPluginFactory(pluginExecutor,
					crypto.getSecureRandom()));
			factories.add(new ModemPluginFactory(pluginExecutor,
					reliabilityFactory));
		}
		factories.add(new LanTcpPluginFactory(pluginExecutor));
		factories.add(new WanTcpPluginFactory(pluginExecutor, shutdownManager));
		return new DuplexPluginConfig() {
			public Collection<DuplexPluginFactory> getFactories() {
				return factories;
			}
		};
	}
}
