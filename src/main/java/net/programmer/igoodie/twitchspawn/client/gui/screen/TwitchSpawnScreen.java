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
import net.programmer.igoodie.twitchspawn.network.TwitchPubSubSocket;

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
                (button) -> {
                    StreamlabsSocket.INSTANCE.start();
                    TwitchPubSubSocket.INSTANCE.start();
                }
        );
        this.startButton.active = !StreamlabsSocket.INSTANCE.running;

        this.stopButton = new Button(
                10, 100,
                100, 20,
                new StringTextComponent("Disconnect"),
                (button) -> {
                    StreamlabsSocket.INSTANCE.stop();
                    TwitchPubSubSocket.INSTANCE.stop();
                }
        );
        this.stopButton.active = StreamlabsSocket.INSTANCE.running;

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

        this.refreshButton.active = !StreamlabsSocket.INSTANCE.running;
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

        fontRenderer.drawString(matrixStack,
                StreamlabsSocket.INSTANCE.running
                        ? "> Streamlabs Connected"
                        : "> Streamlabs Not connected",
                10, 35,
                StreamlabsSocket.INSTANCE.running ? 0xFF_FFFFFF : 0xFF_FF0000);

        fontRenderer.drawString(matrixStack,
                TwitchPubSubSocket.INSTANCE.pingScheduler != null
                        ? "> Twitch PubSub Connected"
                        : "> Twitch PubSub Not connected",
                10, 45,
                TwitchPubSubSocket.INSTANCE.pingScheduler != null ? 0xFF_FFFFFF : 0xFF_FF0000);

        startButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        stopButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        refreshButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        refreshButtons();
    }

}
