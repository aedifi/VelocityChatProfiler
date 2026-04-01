package aedifi.chatprofiler.listener;

import aedifi.chatprofiler.platform.Platform;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;

public final class PopupPacketListener implements PacketListener {

    private final Platform platform;

    public PopupPacketListener(Platform platform) {
        this.platform = platform;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        ClientVersion clientVersion = event.getUser().getClientVersion();

        if (packetType == PacketType.Status.Server.RESPONSE
                && clientVersion.isNewerThan(ClientVersion.V_1_18_2)
                && platform.getConfig().isBlockChatReports()) {
            WrapperStatusServerResponse wrapper = new WrapperStatusServerResponse(event);
            JsonObject response = wrapper.getComponent();
            response.addProperty("preventsChatReports", true);
            wrapper.setComponent(response);
            return;
        }

        if (packetType == PacketType.Play.Server.SERVER_DATA
                && clientVersion.isOlderThan(ClientVersion.V_1_20_5)
                && !platform.getConfig().isShowPopup()) {
            WrapperPlayServerServerData wrapper = new WrapperPlayServerServerData(event);
            wrapper.setEnforceSecureChat(true);
            return;
        }

        if (packetType == PacketType.Play.Server.JOIN_GAME
                && clientVersion.isNewerThan(ClientVersion.V_1_20_3)
                && !platform.getConfig().isShowPopup()) {
            WrapperPlayServerJoinGame wrapper = new WrapperPlayServerJoinGame(event);
            wrapper.setEnforcesSecureChat(true);
            return;
        }

        if (packetType == PacketType.Play.Server.PLAYER_CHAT_HEADER
                && !platform.getConfig().isSendHeader()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (platform.getConfig().isBlockChatReports()
                && event.getPacketType() == PacketType.Play.Client.CHAT_SESSION_UPDATE) {
            event.setCancelled(true);
        }
    }
}
