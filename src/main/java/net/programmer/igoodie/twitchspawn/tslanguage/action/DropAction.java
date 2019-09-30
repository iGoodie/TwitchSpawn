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
    private int itemDamage;

    /*
     * Params:
     * 0  - item: minecraft:diamond_block{someNBT:"Data"}
     * 1  - amount: 1
     * 2? - damage: 0
     *
     * Possible param count: [2,3]
     */
    public DropAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() != 2 && actionWords.size() != 3)
            throw new TSLSyntaxError("Invalid length of words: " + actionWords);

        this.itemRaw = actionWords.get(0);
        this.itemAmount = parseInt(actionWords.get(1));
        this.itemDamage = actionWords.size() >= 3 ? parseInt(actionWords.get(2)) : 0;

        // Check if given item word is parse-able
        if (!new ItemParser(this.itemRaw).isValid())
            throw new TSLSyntaxError("Invalid item text");
    }

    private int parseInt(String string) throws TSLSyntaxError {
        try { return Integer.parseInt(string); } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected an integer, found instead -> %s", string);
        }
    }

    @Override
    protected void performAction(EntityPlayerMP player, EventArguments args) {
        ItemStack itemStack = createItemStack(args);
        player.dropItem(itemStack, false, false);
    }

    private ItemStack createItemStack(EventArguments args) {
        String input = replaceExpressions(itemRaw, args);
        ItemStack itemStack = new ItemParser(input).generateItemStack(itemAmount);
        itemStack.setItemDamage(itemDamage);
        return itemStack;
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
