package aedifi.chatprofiler;

import aedifi.chatprofiler.config.APConfig;
import aedifi.chatprofiler.listener.ChatRewriteListener;
import aedifi.chatprofiler.listener.PopupPacketListener;
import aedifi.chatprofiler.platform.VelocityPlatform;
import aedifi.chatprofiler.rewrite.ProtocolMappingRegistry;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "chatprofiler",
        name = "ChatProfiler",
        authors = {"Aedificium"},
        version = BuildConstants.VERSION
)
public final class ChatProfilerPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private APConfig config;

    @Inject
    public ChatProfilerPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            config = new APConfig(dataDirectory.toFile(), this.getClass().getClassLoader());
        } catch (IOException exception) {
            throw new RuntimeException("Could not initialize config.yml", exception);
        }

        VelocityPlatform platform = new VelocityPlatform(server, config);
        ProtocolMappingRegistry mappingRegistry = new ProtocolMappingRegistry();

        PluginContainer pluginContainer = server.getPluginManager().ensurePluginContainer(this);
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, pluginContainer, logger, dataDirectory));
        PacketEvents.getAPI().getSettings()
                .debug(config.isDebug())
                .checkForUpdates(false);
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().getEventManager()
                .registerListener(new PopupPacketListener(platform), PacketListenerPriority.LOW);
        PacketEvents.getAPI().getEventManager()
                .registerListener(new ChatRewriteListener(platform, mappingRegistry, logger), PacketListenerPriority.LOWEST);

        PacketEvents.getAPI().init();
        logStartupDiagnostics(mappingRegistry);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
        }
    }

    private void logStartupDiagnostics(ProtocolMappingRegistry mappingRegistry) {
        logger.info("Chat profiler initialized on Velocity {}.", server.getVersion().getVersion());
        logger.info("Config: block-chat-reports={}, show-popup={}, send-header={}, debug={}.",
                config.isBlockChatReports(), config.isShowPopup(), config.isSendHeader(), config.isDebug());
        logger.info("Rewrite route mapping: 1.19.1-1.19.2={}, 1.19.3+={}.",
                mappingRegistry.describeRoute(com.github.retrooper.packetevents.protocol.player.ClientVersion.V_1_19_1),
                mappingRegistry.describeRoute(com.github.retrooper.packetevents.protocol.player.ClientVersion.V_1_19_3));
    }
}
