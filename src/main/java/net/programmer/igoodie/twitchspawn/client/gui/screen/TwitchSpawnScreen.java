package net.programmer.igoodie.twitchspawn.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
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
        super(new StringTextComponent("TwitchSpawn"));
        refreshButtons();
    }

    public void refreshButtons() {
        this.startButton = new Button(
                10, 75,
                100, 20,
                new StringTextComponent("Connect"),
                (button) -> SocketManager.start()
        );
        this.startButton.active = !SocketManager.isRunning();

        this.stopButton = new Button(
                10, 100,
                100, 20,
                new StringTextComponent("Disconnect"),
                (button) -> SocketManager.stop()
        );
        this.stopButton.active = SocketManager.isRunning();

        this.refreshButton = new Button(
                10, 125,
                100, 20,
                new StringTextComponent("Reload Configs"),
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
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        StatusIndicatorOverlay.render(matrixStack, PreferencesConfig.IndicatorDisplay.ENABLED);

        FontRenderer fontRenderer = getMinecraft().fontRenderer;

        renderConnectionStatus(matrixStack, SocketManager.STREAMLABS_SOCKET, 10, 35);
        renderConnectionStatus(matrixStack, SocketManager.TWITCH_PUB_SUB_SOCKET, 10, 45);

        refreshButtons();
        startButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        stopButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        refreshButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void renderConnectionStatus(MatrixStack matrixStack, SocketTracer tracer, int x, int y) {
        FontRenderer fontRenderer = getMinecraft().fontRenderer;

        String text = String.format("> %s %s", tracer.getPlatform().name,
                tracer.isConnected() ? "Connected" : "Not Connected");

        fontRenderer.drawString(matrixStack, text, x, y,
                tracer.isConnected() ? 0xFF_FFFFFF : 0xFF_FF0000);
    }

}
