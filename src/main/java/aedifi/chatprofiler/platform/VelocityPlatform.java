package aedifi.chatprofiler.platform;

import aedifi.chatprofiler.config.APConfig;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.UUID;

public final class VelocityPlatform extends Platform {

    private final ProxyServer server;

    public VelocityPlatform(ProxyServer server, APConfig config) {
        super(config);
        this.server = server;
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return server.getPlayer(playerUUID).map(player -> player.getUsername()).orElse("unknown");
    }
}
