package aedifi.chatprofiler.rewrite;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public final class ProtocolMappingRegistry {

    public enum RewriteRoute {
        SYSTEM_CHAT,
        DISGUISED_CHAT
    }

    public RewriteRoute resolve(ClientVersion version) {
        if (version.isNewerThanOrEquals(ClientVersion.V_1_19_3)) {
            return RewriteRoute.DISGUISED_CHAT;
        }
        return RewriteRoute.SYSTEM_CHAT;
    }

    public String describeRoute(ClientVersion version) {
        RewriteRoute route = resolve(version);
        return switch (route) {
            case DISGUISED_CHAT -> "disguised_chat";
            case SYSTEM_CHAT -> "system_chat";
        };
    }
}
