package net.programmer.igoodie.twitchspawn;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.programmer.igoodie.twitchspawn.client.gui.screen.TwitchSpawnScreen;
import net.programmer.igoodie.twitchspawn.log.TSLogger;
import net.programmer.igoodie.twitchspawn.network.SocketManager;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TwitchSpawnClient {

    public static final TSLogger LOGGER = TwitchSpawn.getStreamerLogger("LOCAL_CLIENT");
    public static KeyMapping openTwitchSpawnScreen;

    public static void registerKeybinds(final FMLClientSetupEvent event) {
        openTwitchSpawnScreen = new KeyMapping("key.twitchspawn.open_ts_screen", 293, "key.category.twitchspawn");

        ClientRegistry.registerKeyBinding(openTwitchSpawnScreen);
    }

    @SubscribeEvent
    public static void onLeavingServer(ClientPlayerNetworkEvent.LoggedOutEvent event) { // <-- Replace with leave
        SocketManager.stop();
    }

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.screen == null && openTwitchSpawnScreen.isDown()) {
            minecraft.setScreen(new TwitchSpawnScreen());
        }
    }

}
