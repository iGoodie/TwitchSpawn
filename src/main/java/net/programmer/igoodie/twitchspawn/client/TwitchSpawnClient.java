package net.programmer.igoodie.twitchspawn.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.udl.NotepadUDLUpdater;

public class TwitchSpawnClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NotepadUDLUpdater.attemptUpdate();

        // Payload handlers run on the client thread
        ClientPlayNetworking.registerGlobalReceiver(StatusChangedPacket.TYPE, (packet, context) -> StatusChangedPacket.handle(packet));
        ClientPlayNetworking.registerGlobalReceiver(OsRunPacket.TYPE, (packet, context) -> OsRunPacket.handle(packet));
        ClientPlayNetworking.registerGlobalReceiver(GlobalChatCooldownPacket.TYPE, (packet, context) -> GlobalChatCooldownPacket.handle(packet));

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(TwitchSpawn.MOD_ID, "status_indicator"),
                (graphics, deltaTracker) -> StatusIndicatorOverlay.render(graphics));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(TwitchSpawn.MOD_ID, "global_chat_cooldown"),
                (graphics, deltaTracker) -> GlobalChatCooldownOverlay.render(graphics));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> {
            TwitchSpawnLoadingErrors errors = TwitchSpawn.LOADING_ERRORS;
            if (errors == null) return;
            errors.toWarningMessages().forEach(message -> client.gui.chatListener().handleSystemMessage(message, false));
            TwitchSpawn.LOADING_ERRORS = null;
        }));
    }

}
