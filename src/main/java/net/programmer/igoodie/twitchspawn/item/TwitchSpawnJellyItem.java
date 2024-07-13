package net.programmer.igoodie.twitchspawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TwitchSpawnJellyItem extends Item {

    public TwitchSpawnJellyItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent(""));
        tooltip.add(new TextComponent("\u2602 If you received this sticky item,")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(new TextComponent("This means the ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(new TextComponent("Derpy Goodie Dude")
                        .withStyle(ChatFormatting.AQUA))
                .append(" paid"));
        tooltip.add(new TextComponent("your stream a visit!")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(new TextComponent(""));
        tooltip.add(new TextComponent("\u2661 Collect 4 of them to craft a fun item")
                .withStyle(ChatFormatting.GREEN));
    }

}
