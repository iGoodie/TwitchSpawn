package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;
import net.programmer.igoodie.twitchspawn.util.RegistryHelpers;

import java.util.List;

public class DropAction extends TSLAction {

    private String itemRaw;
    private int itemAmount;

    /*
     * Exemplar valid params:
     * minecraft:diamond_block 123
     * stone_block 321
     * diamond_sword[enchantments={smite:2,sweeping_edge:2,unbreaking:3}]
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
            new ItemParser(RegistryHelpers.lookup()).parse(new StringReader(randomItem));

        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError(e.getRawMessage().getString());
        }
    }

    @Override
    protected void performAction(ServerPlayer player, EventArguments args) {
        ItemInput itemInput = parseItem(args);
        int maxStackSize = new ItemStack(itemInput.item(), 1, itemInput.components()).getMaxStackSize();

        // Oversized stacks are no longer valid, so drop in max-sized chunks
        for (int remaining = itemAmount; remaining > 0; remaining -= maxStackSize) {
            int count = Math.min(remaining, maxStackSize);
            player.drop(new ItemStack(itemInput.item(), count, itemInput.components()), false, Prediction.SERVER_ONLY);
        }
    }

    private ItemInput parseItem(EventArguments args) {
        try {
            String input = replaceExpressions(itemRaw, args);
            return new ItemParser(RegistryHelpers.lookup()).parse(new StringReader(input));

        } catch (CommandSyntaxException e) {
            throw new InternalError("Invalid item format occurred after validation... Something fishy here..");
        }
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        ItemInput itemInput = parseItem(args);

        if (expression.equals("itemName"))
            return new ItemStack(itemInput.item(), 1, itemInput.components()).getHoverName().getString();

        if (expression.equals("itemCount"))
            return String.valueOf(itemAmount);

        return null;
    }

}
