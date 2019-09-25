package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ItemParser;

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
            throw new TSLSyntaxError("Invalid length of words: " + actionWords);

        this.itemRaw = actionWords.get(0);

        try {
            this.itemAmount = actionWords.size() != 2 ? 1 : Integer.parseInt(actionWords.get(1));

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected an integer, found instead -> %s", actionWords.get(1));
        }

        // Check if given item word is parse-able
        if (!new ItemParser(this.itemRaw).isValid())
            throw new TSLSyntaxError("Invalid item text"); // TODO
    }

    @Override
    protected void performAction(EntityPlayerMP player, EventArguments args) {
        ItemStack itemStack = createItemStack(args);
        player.dropItem(itemStack, false, false);
    }

    private ItemStack createItemStack(EventArguments args) {
        String input = replaceExpressions(itemRaw, args);
        return new ItemParser(input).generateItemStack(itemAmount);
//        try {
//            String input = replaceExpressions(itemRaw, args);
//
//            ItemParser itemParser = new ItemParser(new StringReader(input), true).parse();
//            ItemStack itemStack = new ItemStack(itemParser.getItem(), itemAmount);
//            itemStack.setTag(itemParser.getNbt());
//
//            return itemStack;
//
//        } catch (CommandSyntaxException e) {
//            throw new InternalError("Invalid item format occurred after validation... Something fishy here..");
//        }
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        ItemStack itemStack = createItemStack(args);

        if (expression.equals("itemName"))
            return itemStack.getItem().getItemStackDisplayName(itemStack);
//            return itemStack.getItem().getName().getString(); // getName() is client only...

        if (expression.equals("itemCount"))
            return String.valueOf(itemStack.getCount());

        return null;
    }

}
