package aedifi.chatprofiler.listener;

import aedifi.chatprofiler.platform.Platform;
import aedifi.chatprofiler.rewrite.ProtocolMappingRegistry;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisguisedChat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatRewriteListener implements PacketListener {

    private final Platform platform;
    private final ProtocolMappingRegistry registry;
    private final Logger logger;
    private final Set<ClientVersion> warnedVersions = ConcurrentHashMap.newKeySet();

    public ChatRewriteListener(Platform platform, ProtocolMappingRegistry registry, Logger logger) {
        this.platform = platform;
        this.registry = registry;
        this.logger = logger;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!platform.getConfig().isBlockChatReports()) {
            return;
        }
        if (event.getPacketType() != PacketType.Play.Server.CHAT_MESSAGE) {
            return;
        }

        ClientVersion clientVersion = event.getUser().getClientVersion();
        if (clientVersion.isOlderThan(ClientVersion.V_1_19_1)) {
            return;
        }

        try {
            WrapperPlayServerChatMessage wrapper = new WrapperPlayServerChatMessage(event);
            ChatMessage message = wrapper.getMessage();
            Component content = message.getChatContent();
            ChatType.Bound chatFormatting = null;

            if (message instanceof ChatMessage_v1_19_3 modernMessage) {
                content = modernMessage.getUnsignedChatContent().orElse(content);
                chatFormatting = modernMessage.getChatFormatting();
            } else if (message instanceof ChatMessage_v1_19_1 signedMessage) {
                if (signedMessage.getUnsignedChatContent() != null) {
                    content = signedMessage.getUnsignedChatContent();
                }
                chatFormatting = signedMessage.getChatFormatting();
            }

            event.setCancelled(true);
            switch (registry.resolve(clientVersion)) {
                case DISGUISED_CHAT -> {
                    if (chatFormatting == null) {
                        event.getUser().sendPacketSilently(new WrapperPlayServerSystemChatMessage(false, content));
                    } else {
                        event.getUser().sendPacketSilently(new WrapperPlayServerDisguisedChat(content, chatFormatting));
                    }
                }
                case SYSTEM_CHAT -> event.getUser().sendPacketSilently(new WrapperPlayServerSystemChatMessage(false, content));
            }
        } catch (Throwable throwable) {
            if (platform.getConfig().isLogCompatibilityWarnings() && warnedVersions.add(clientVersion)) {
                logger.warn("Chat profiler failed to rewrite CHAT_MESSAGE for client {} using route {}. Leaving packet untouched.",
                        clientVersion, registry.describeRoute(clientVersion), throwable);
            }
        }
    }
}
