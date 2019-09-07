package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.types.templates.Tag;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.command.impl.PlaySoundCommand;
import net.minecraft.command.impl.TitleCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.*;
import net.minecraftforge.common.util.Constants;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.NBTUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DropAction extends TSLAction {

    private ItemStack itemStack;

    /*
     * Exemplar valid params:
     * minecraft:diamond_block 123
     * stone_block 321
     * diamond_sword{Enchantments:[{id:smite,lvl:2},{id:sweeping,lvl:2},{id:unbreaking,lvl:3}]}
     */
    public DropAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() != 1 && actionWords.size() != 2)
            throw new TSLSyntaxError("Invalid length of words: " + actionWords);

        try {
            ItemParser itemParser = new ItemParser(new StringReader(actionWords.get(0)), true).parse();
            int amount = actionWords.size() != 2 ? 1 : Integer.parseInt(actionWords.get(1));

            this.itemStack = new ItemStack(itemParser.getItem(), amount);
            this.itemStack.setTag(itemParser.getNbt());

        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError(e.getRawMessage().getString());

        } catch (Exception e) {
            throw new TSLSyntaxError("Invalid action words: " + actionWords);
        }
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        ItemStack itemStack = this.itemStack.copy();
        // TODO: Replace item lore
        if (itemStack.hasDisplayName()) {
            itemStack.setDisplayName(NBTUtils.ReplaceExpressions(itemStack.getDisplayName(), args));
        }
        player.dropItem(itemStack, false, false);
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        if (expression.equals("itemName"))
            return itemStack.getItem().getDisplayName(itemStack).getString();
//            return itemStack.getItem().getName().getString(); // getName() is client only...
        if (expression.equals("itemCount"))
            return String.valueOf(itemStack.getCount());
        return null;
    }

}
