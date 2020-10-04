package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class ThrowAction extends ItemSelectiveAction {

    public ThrowAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        long countFrom = actionWords.stream()
                .filter(word -> word.equalsIgnoreCase("FROM")).count();

        if (countFrom == 0) parseSingleWord(actionWords);
        else if (countFrom == 1) parseFrom(actionWords);
        else throw new TSLSyntaxError("At most 1 FROM statement expected, found %d instead.", countFrom);
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        if (selectionType == SelectionType.WITH_INDEX) {
            ItemStack extracted = getInventory(player, inventoryType).set(inventoryIndex, ItemStack.EMPTY);
            if (!extracted.isEmpty())
                player.dropItem(extracted, false, true);

        } else if (selectionType == SelectionType.EVERYTHING) {
            if (inventoryType == null) {
                player.inventory.dropAllItems();
            } else {
                List<ItemStack> inventory = getInventory(player, inventoryType);
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack itemStack = inventory.get(i);
                    if (!itemStack.isEmpty()) {
                        player.dropItem(itemStack, false, true);
                        inventory.set(i, ItemStack.EMPTY);
                    }
                }
            }

        } else if (selectionType == SelectionType.RANDOM) {
            InventorySlot randomSlot = inventoryType == null
                    ? randomInventorySlot(player, false)
                    : randomInventorySlot(getInventory(player, inventoryType), false);
            if (randomSlot != null) {
                ItemStack extracted = randomSlot.pullOut();
                player.dropItem(extracted, false, true);
            }

        } else if (selectionType == SelectionType.ONLY_HELD_ITEM) {
            int selectedHotbarIndex = player.inventory.currentItem;
            ItemStack extracted = player.inventory.mainInventory.set(selectedHotbarIndex, ItemStack.EMPTY);
            if (!extracted.isEmpty())
                player.dropItem(extracted, false, true);

        } else if (selectionType == SelectionType.HOTBAR) {
            for (int i = 0; i <= 8; i++) {
                ItemStack extracted = player.inventory.mainInventory.set(i, ItemStack.EMPTY);
                if (!extracted.isEmpty())
                    player.dropItem(extracted, false, true);
            }
        }

        CommandSource commandSource = player.getCommandSource()
                .withPermissionLevel(9999).withFeedbackDisabled();
        player.getServer().getCommandManager().handleCommand(commandSource,
                "/playsound minecraft:entity.ender_pearl.throw master @s");
        player.getServer().getCommandManager().handleCommand(commandSource,
                "/particle minecraft:witch ~ ~ ~ 2 2 2 0.1 400");
    }

}
