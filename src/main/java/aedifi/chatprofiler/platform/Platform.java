package aedifi.chatprofiler.platform;

import aedifi.chatprofiler.config.APConfig;

import java.util.UUID;

public abstract class Platform {

    private final APConfig config;

    protected Platform(APConfig config) {
        this.config = config;
    }

    public APConfig getConfig() {
        return config;
    }

    public abstract String getPlayerName(UUID playerUUID);
}
