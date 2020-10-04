package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;

import java.util.List;

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
            new ItemParser(new StringReader(randomItem), true).parse();

        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError(e.getRawMessage().getString());
        }
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        ItemStack itemStack = createItemStack(args);
        player.dropItem(itemStack, false, false);
    }

    private ItemStack createItemStack(EventArguments args) {
        try {
            String input = replaceExpressions(itemRaw, args);

            ItemParser itemParser = new ItemParser(new StringReader(input), true).parse();
            ItemStack itemStack = new ItemStack(itemParser.getItem(), itemAmount);
            itemStack.setTag(itemParser.getNbt());

            return itemStack;

        } catch (CommandSyntaxException e) {
            throw new InternalError("Invalid item format occurred after validation... Something fishy here..");
        }
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        ItemStack itemStack = createItemStack(args);

        if (expression.equals("itemName"))
            return itemStack.getItem().getDisplayName(itemStack).getString();
//            return itemStack.getItem().getName().getString(); // getName() is client only...

        if (expression.equals("itemCount"))
            return String.valueOf(itemStack.getCount());

        return null;
    }

}
