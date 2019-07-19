package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.Arrays;
import java.util.List;

public class DropAction extends TSLAction {

    private ItemStack itemStack;

    /*
     * Exemplar valid params:
     * minecraft:diamond_block 123
     * stone_block 321
     * diamond_sword{Enchantments:[{id:smite,lvl:2},{id:sweeping,lvl:2},{id:unbreaking,lvl:3}]}
     */
    public DropAction(List<String> params) throws TSLSyntaxError {
        if (params.size() != 1 && params.size() != 2)
            throw new TSLSyntaxError("Invalid length of parameters: " + params);

        try {
            ItemParser itemParser = new ItemParser(new StringReader(params.get(0)), true).parse();
            int amount = params.size() != 2 ? 1 : Integer.parseInt(params.get(1));

            System.out.println(itemParser);
            System.out.println(amount);

            this.itemStack = new ItemStack(itemParser.getItem(), amount);
            this.itemStack.setTag(itemParser.getNbt());

        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError(e.getRawMessage().getString());

        } catch (Exception e) {
            throw new TSLSyntaxError("Invalid action parameter: " + params);
        }
    }

    @Override
    protected void performAction(ServerPlayerEntity player) {
        player.dropItem(this.itemStack, false, false);
    }

}
