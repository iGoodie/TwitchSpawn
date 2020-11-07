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
import net.programmer.igoodie.twitchspawn.network.StreamlabsSocket;

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
                10, 55,
                100, 20,
                new StringTextComponent("Connect"),
                (button) -> {
                    StreamlabsSocket.INSTANCE.start();
                }
        );
        this.startButton.active = !StreamlabsSocket.INSTANCE.running;

        this.stopButton = new Button(
                10, 80,
                100, 20,
                new StringTextComponent("Disconnect"),
                (button) -> {
                    StreamlabsSocket.INSTANCE.stop();
                }
        );
        this.stopButton.active = StreamlabsSocket.INSTANCE.running;

        this.refreshButton = new Button(
                10, 105,
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
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        startButton.mouseMoved(mouseX, mouseY);
        stopButton.mouseMoved(mouseX, mouseY);
        refreshButton.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        startButton.mouseClicked(mouseX, mouseY, button);
        stopButton.mouseClicked(mouseX, mouseY, button);
        refreshButton.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        StatusIndicatorOverlay.render(matrixStack, PreferencesConfig.IndicatorDisplay.ENABLED);

        FontRenderer fontRenderer = getMinecraft().fontRenderer;

        fontRenderer.drawString(matrixStack, StreamlabsSocket.INSTANCE.running ? "> Connected" : "> Not connected",
                10, 35, 0xFF_FFFFFF);

        refreshButtons();
        startButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        stopButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        refreshButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

}
