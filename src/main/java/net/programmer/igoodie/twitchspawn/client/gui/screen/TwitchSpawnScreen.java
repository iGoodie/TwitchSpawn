package net.programmer.igoodie.twitchspawn.client.gui.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.network.SocketManager;
import net.programmer.igoodie.twitchspawn.network.socket.base.SocketTracer;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

public class TwitchSpawnScreen extends Screen {

    private static final ResourceLocation GOODIE_POSE_TEXTURE =
            new ResourceLocation(TwitchSpawn.MOD_ID, "textures/goodie_pose.png");

    protected Button startButton;
    protected Button stopButton;
    protected Button refreshButton;
    protected Button openConfigFolderButton;

    protected float blockChangeTick = 0;
    protected ItemStack itemStack;

    public TwitchSpawnScreen() {
        super(new TextComponent("TwitchSpawn"));
        pickRandomItem();
        refreshButtons();
    }

    public void pickRandomItem() {
        Item[] items = {
                Items.BEACON,
                Items.MELON,
                Items.CACTUS,
                Items.DIAMOND,
                Items.SLIME_BALL,
                Items.CHORUS_FRUIT,
                Items.CHORUS_FLOWER,
                Items.AXOLOTL_BUCKET,
        };

        Item item = Math.random() >= 0.4
                ? Items.AXOLOTL_BUCKET
                : items[(int) Math.floor(Math.random() * items.length)];

        itemStack = new ItemStack(item);
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
                        if (SocketManager.isRunning())
                            SocketManager.stop();
                        SocketManager.initialize();
                    } catch (TwitchSpawnLoadingErrors twitchSpawnLoadingErrors) {
                        twitchSpawnLoadingErrors.printStackTrace();
                    }
                }
        );

        this.refreshButton.active = !SocketManager.isRunning();

        this.openConfigFolderButton = new Button(
                10, buttonYOffset + 100,
                120, 20,
                new TextComponent("Open Config Folder"),
                (button) -> {
                    try {
                        File configDirectory = new File(ConfigManager.CONFIG_DIR_PATH);
                        openDir(configDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private static void openDir(File file) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows
            new ProcessBuilder("explorer", file.getAbsolutePath()).start();
        } else if (os.contains("mac")) {
            // macOS
            new ProcessBuilder("open", file.getAbsolutePath()).start();
        } else if (os.contains("nix") || os.contains("nux")) {
            // Unix or Linux
            new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
        } else {
            throw new UnsupportedOperationException("OS not supported");
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        startButton.mouseMoved(mouseX, mouseY);
        stopButton.mouseMoved(mouseX, mouseY);
        refreshButton.mouseMoved(mouseX, mouseY);
        openConfigFolderButton.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        startButton.mouseClicked(mouseX, mouseY, button);
        stopButton.mouseClicked(mouseX, mouseY, button);
        refreshButton.mouseClicked(mouseX, mouseY, button);
        openConfigFolderButton.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        startButton.mouseReleased(mouseX, mouseY, button);
        stopButton.mouseReleased(mouseX, mouseY, button);
        refreshButton.mouseReleased(mouseX, mouseY, button);
        openConfigFolderButton.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        startButton.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        stopButton.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        refreshButton.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        openConfigFolderButton.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        return super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        startButton.mouseScrolled(p_94686_, p_94687_, p_94688_);
        stopButton.mouseScrolled(p_94686_, p_94687_, p_94688_);
        refreshButton.mouseScrolled(p_94686_, p_94687_, p_94688_);
        openConfigFolderButton.mouseScrolled(p_94686_, p_94687_, p_94688_);
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
        openConfigFolderButton.render(matrixStack, mouseX, mouseY, partialTicks);

        blockChangeTick += partialTicks;

        if (blockChangeTick >= 40f) {
            pickRandomItem();
            blockChangeTick = 0;
        }

        Font fontRenderer = getMinecraft().font;

        if (!ConfigManager.CLIENT_CREDS.twitchNickname.isEmpty()) {
            String platformUrl = "twitch.tv/";
            String minecraftText = "minecraft:";
            String streamerNick = ConfigManager.CLIENT_CREDS.twitchNickname.toLowerCase();
            String ign = Minecraft.getInstance().player.getDisplayName().getString();

            fontRenderer.draw(matrixStack, minecraftText,
                    width - fontRenderer.width((ign)) - fontRenderer.width(minecraftText) - 14,
                    10,
                    0xFF_52A535);

            fontRenderer.draw(matrixStack, ign,
                    width - fontRenderer.width((ign)) - 10,
                    10,
                    0xFF_FFFFFF);

            fontRenderer.draw(matrixStack, platformUrl,
                    width - fontRenderer.width((streamerNick)) - fontRenderer.width(platformUrl) - 12,
                    20,
                    0xFF_A970FF);

            fontRenderer.draw(matrixStack, streamerNick,
                    width - fontRenderer.width((streamerNick)) - 10,
                    20,
                    0xFF_FFFFFF);
        }

        {
            float scale = 50;

            int x = width - 140;
            int y = height - 20;

            int ux = 0;
            int uy = 0;
            int tw = 362;
            int th = 362;
            int vx = 223;
            int vh = 362;

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GOODIE_POSE_TEXTURE);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            matrixStack.pushPose();
            matrixStack.translate(x - 21, y - 1.9 * scale, 500);
            float scl = 0.27f;
            matrixStack.scale(scl, scl, scl);
            GuiComponent.blit(matrixStack,
                    0, 0, ux, uy, vx, vh, tw, th);
            matrixStack.popPose();

            String text = "iGoodie";

            fontRenderer.draw(matrixStack, text,
                    x - fontRenderer.width(text) / 2f + 0.05f * scale,
                    y - 2.2f * scale,
                    0xFF_AAAAAA);
        }

        {
            float p_98854_ = 100;
            float p_98855_ = -20;
            float scale = 50;

            int posX = width - 50;
            int posY = height - 20;

            LocalPlayer entity = Minecraft.getInstance().player;

            float f = (float) Math.atan((double) (p_98854_ / 40.0F));
            float f1 = (float) Math.atan((double) (p_98855_ / 40.0F));
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate((double) posX, (double) posY, 1050.0D);
            posestack.scale(1.0F, 1.0F, -1.0F);
            RenderSystem.applyModelViewMatrix();
            PoseStack posestack1 = new PoseStack();
            posestack1.translate(0.0D, 0.0D, 1000.0D);
            posestack1.scale((float) scale, (float) scale, (float) scale);
            Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
            Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
            quaternion.mul(quaternion1);
            posestack1.mulPose(quaternion);
            float f2 = entity.yBodyRot;
            float f3 = entity.getYRot();
            float f4 = entity.getXRot();
            float f5 = entity.yHeadRotO;
            float f6 = entity.yHeadRot;
            entity.yBodyRot = 180.0F + f * 20.0F;
            entity.setYRot(180.0F + f * 40.0F);
            entity.setXRot(-f1 * 20.0F);
            entity.yHeadRot = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            quaternion1.conj();
            entityrenderdispatcher.overrideCameraOrientation(quaternion1);
            entityrenderdispatcher.setRenderShadow(false);
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
//        RenderSystem.runAsFancy(() -> {
//            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
//        });
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);


            {
                Minecraft minecraft = Minecraft.getInstance();
                TextureManager mgr = minecraft.getTextureManager();
                ItemRenderer renderer = minecraft.getItemRenderer();

                posestack.pushPose();
                posestack1.pushPose();
                posestack1.translate(1.1, 0.088f * scale, -18);
                float scl = 0.5f;
                posestack1.scale(scl, scl, scl);
                double rotation = -100 * ((System.currentTimeMillis() / 1000D) + (1 / 25f)) % 360 * (Math.PI / 180d);
                posestack1.mulPose(Quaternion.fromXYZ((float) 0, ((float) rotation), (float) 0));
                double yOffset = Math.sin(rotation) / 15;
                posestack1.translate(0, yOffset, 0);
                renderer.renderStatic(
                        itemStack,
                        ItemTransforms.TransformType.FIXED,
                        15728880, OverlayTexture.NO_OVERLAY,
                        posestack1,
                        multibuffersource$buffersource,
                        0
                );
                posestack1.popPose();
                posestack.popPose();
            }

            multibuffersource$buffersource.endBatch();
            entityrenderdispatcher.setRenderShadow(true);
            entity.yBodyRot = f2;
            entity.setYRot(f3);
            entity.setXRot(f4);
            entity.yHeadRotO = f5;
            entity.yHeadRot = f6;

            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();

            String text = entity.getName().getString();

            fontRenderer.draw(matrixStack, text,
                    posX - fontRenderer.width(text) / 2f - 0.05f * scale,
                    posY - 2.2f * scale,
                    0xFF_AAAAAA);
        }
    }

    private void renderConnectionStatus(PoseStack matrixStack, SocketTracer tracer, int x, int y, boolean optional) {
        Font fontRenderer = getMinecraft().font;

        String text = String.format("> %s  -  %s %s", tracer.getPlatform().name,
                tracer.isConnected() ? "Connected" : "Not Connected",
                optional ? "(Optional)" : "");

        fontRenderer.draw(matrixStack, text, x, y,
                tracer.isConnected() ? 0xFF_FFFFFF : 0xFF_FF0000);
    }

}
