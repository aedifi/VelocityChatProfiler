### VelocityChatProfiler
VelocityChatProfiler (or simply ChatProfiler, as known to the server itself) is a plugin for [Velocity](https://github.com/PaperMC/velocity), a Minecraft server proxy used by Aedificium in order to wrap the [PaperMC](https://github.com/PaperMC/Paper) server software (i.e. the "backend").  Simply put, it uses the [PacketEvents](https://github.com/retrooper/packetevents) protocol library in order to accomplish three things:

1. Covertly intercepts Minecraft's new ["signed" messages](https://gist.github.com/kennytv/ed783dd244ca0321bbd882c347892874) (`CHAT_MESSAGE`) with an unsigned form of message (`SYSTEM_CHAT` or `DISGUISED_CHAT`, depending on the client version).
2. Spoofs an enabled secure chat state for the server, which in turn avoids the warning popup upon player join for servers without `enforce_secure_profile=true`.
3. Cancels any incoming `CHAT_SESSION_UPDATE` packets from clients (requires `block-chat-reports` in the plugin's config) and outgoing `PLAYER_CHAT_HEADER` packets, in turn making any chat messages "unreportable" (unrecognized by the game's reporting interface).

This Velocity plugin is designed to replace existing PaperMC plugins such as [AntiPopup](https://github.com/aedifi/AntiPopup) in order to better streamline the intra-network experience and provide a greater degree of privacy assurance for those who do not trust Microsoft with the contents of their chat messages.

Because of the way this plugin is written, it should only target client versions **1.19.1 and upwards** while also maintaining any existing backwards compatibility (which, in turn, requires something to the likes of [ViaBackwards](https://github.com/ViaVersion/ViaBackwards)).

As of writing, the plugin has been last tested on Velocity 3.5.0-SNAPSHOT (git-ab99bde9-b585).
