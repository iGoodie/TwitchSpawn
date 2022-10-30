package net.programmer.igoodie.twitchspawn.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.network.SocketManager;
import net.programmer.igoodie.twitchspawn.network.socket.base.SocketTracer;

import javax.annotation.Nonnull;

public class TwitchSpawnScreen extends Screen {

    protected Button startButton;
    protected Button stopButton;
    protected Button refreshButton;

    public TwitchSpawnScreen() {
        super(new TextComponent("TwitchSpawn"));
        refreshButtons();
    }

    public void refreshButtons() {
        int buttonYOffset = 80;

        this.startButton = new Button(
                10, buttonYOffset,
                100, 20,
                new TextComponent("Connect"),
                (button) -> SocketManager.start()
        );
        this.startButton.active = !SocketManager.isRunning();

        this.stopButton = new Button(
                10, buttonYOffset + 25,
                100, 20,
                new TextComponent("Disconnect"),
                (button) -> SocketManager.stop()
        );
        this.stopButton.active = SocketManager.isRunning();

        this.refreshButton = new Button(
                10, buttonYOffset + 50,
                100, 20,
                new TextComponent("Reload Configs"),
                (button) -> {
                    try {
                        ConfigManager.loadConfigs();
                    } catch (TwitchSpawnLoadingErrors twitchSpawnLoadingErrors) {
                        twitchSpawnLoadingErrors.printStackTrace();
                    }
                }
        );

        this.refreshButton.active = !SocketManager.isRunning();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        startButton.mouseMoved(mouseX, mouseY);
        stopButton.mouseMoved(mouseX, mouseY);
        refreshButton.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        startButton.mouseClicked(mouseX, mouseY, button);
        stopButton.mouseClicked(mouseX, mouseY, button);
        refreshButton.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        startButton.mouseReleased(mouseX, mouseY, button);
        stopButton.mouseReleased(mouseX, mouseY, button);
        refreshButton.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        startButton.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        startButton.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        startButton.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        return super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        startButton.mouseScrolled(p_94686_, p_94687_, p_94688_);
        stopButton.mouseScrolled(p_94686_, p_94687_, p_94688_);
        refreshButton.mouseScrolled(p_94686_, p_94687_, p_94688_);
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        StatusIndicatorOverlay.render(matrixStack, PreferencesConfig.IndicatorDisplay.ENABLED);

        renderConnectionStatus(matrixStack, SocketManager.PLATFORM_SOCKET, 10, 35, false);
        renderConnectionStatus(matrixStack, SocketManager.TWITCH_PUB_SUB_SOCKET, 10, 45, false);
        renderConnectionStatus(matrixStack, SocketManager.TWITCH_CHAT_SOCKET, 10, 55, true);

        refreshButtons();
        startButton.render(matrixStack, mouseX, mouseY, partialTicks);
        stopButton.render(matrixStack, mouseX, mouseY, partialTicks);
        refreshButton.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void renderConnectionStatus(PoseStack matrixStack, SocketTracer tracer, int x, int y, boolean optional) {
        Font fontRenderer = getMinecraft().font;

        String text = String.format("> %s %s %s", tracer.getPlatform().name,
                tracer.isConnected() ? "Connected" : "Not Connected",
                optional ? "(Optional)" : "");

        fontRenderer.draw(matrixStack, text, x, y,
                tracer.isConnected() ? 0xFF_FFFFFF : 0xFF_FF0000);
    }

}
