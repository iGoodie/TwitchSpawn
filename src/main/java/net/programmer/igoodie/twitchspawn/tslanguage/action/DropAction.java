package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;

import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;
import net.programmer.igoodie.twitchspawn.util.ItemProcessingHelper;

public class DropAction extends TSLAction {

    private String itemRaw;
    private int itemAmount;

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
            throw new TSLSyntaxError("Invalid length of words: %s", actionWords);

        this.itemRaw = actionWords.get(0);

        try {
            this.itemAmount = actionWords.size() != 2 ? 1 : Integer.parseInt(actionWords.get(1));

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected an integer, found instead -> %s", actionWords.get(1));
        }

        try { // Check if given item word is parse-able
            EventArguments randomEvent = EventArguments.createRandom("RandomStreamer");
            String randomItem = ExpressionEvaluator.replaceExpressions(this.itemRaw,
                    expression -> ExpressionEvaluator.fromArgs(expression, randomEvent));

            // This is an ugly way, but I do not know if there is other way how to get lookup class.
            ItemParser.parseForTesting(BuiltInRegistries.ITEM.asLookup(), new StringReader(randomItem));
        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError(e.getRawMessage().getString());
        }
    }

    @Override
    protected void performAction(ServerPlayer player, EventArguments args) {
        try {
            ItemStack itemStack = ItemProcessingHelper.createItemStack(
                this.replaceExpressions(this.itemRaw, args),
                this.itemAmount);
            player.drop(itemStack, false, false);
        } catch (CommandSyntaxException e) {
            TwitchSpawn.LOGGER.error("Failed to parse item: " + this.itemRaw + " with error: " + e.getRawMessage());
        }
    }


    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        try {
            ItemStack itemStack = ItemProcessingHelper.createItemStack(
                this.replaceExpressions(this.itemRaw, args),
                this.itemAmount);

            if (expression.equals("itemName")) {
                return itemStack.getItem().getName(itemStack).getString();
            }

            if (expression.equals("itemCount")) {
                return String.valueOf(itemStack.getCount());
            }
        } catch (CommandSyntaxException e) {
            TwitchSpawn.LOGGER.error("Failed to parse item: " + this.itemRaw + " with error: " + e.getRawMessage());
        }

        return null;
    }

}
