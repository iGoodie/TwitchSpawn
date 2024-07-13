package net.programmer.igoodie.twitchspawn.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TwitchSpawnJellyBlock extends SlimeBlock {

    public TwitchSpawnJellyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void updateEntityAfterFallOn(@NotNull BlockGetter block, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(block, entity);
        } else {
            this.bounceUp(entity);
        }
    }

    private void bounceUp(Entity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < 0.0) {
            double d0 = entity instanceof LivingEntity ? 1.5 : 1.2;
            entity.setDeltaMovement(vec3.x, -vec3.y * d0, vec3.z);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent(""));
        tooltip.add(new TextComponent("\u2602 This block feels very bouncy...")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(new TextComponent(""));
        tooltip.add(new TextComponent("\u2661 Place down and give it a jump!")
                .withStyle(ChatFormatting.GREEN));
    }
}
