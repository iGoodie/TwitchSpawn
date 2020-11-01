package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class ShuffleAction extends ItemSelectiveAction {

    private int firstIndex;
    private int lastIndex;

    public ShuffleAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() == 1)
            parseInventoryName(actionWords);

        else if (actionWords.size() == 3)
            parseSlot(actionWords);

        else throw new TSLSyntaxError("Invalid length of words: %s", actionWords);
    }

    private void parseInventoryName(List<String> actionWords) throws TSLSyntaxError {
        String inventoryName = actionWords.get(0);

        if (inventoryName.equalsIgnoreCase("inventory")) {
            this.inventoryType = InventoryType.MAIN_INVENTORY;
            this.firstIndex = 0;
            this.lastIndex = inventoryType.capacity - 1;

        } else if (inventoryName.equalsIgnoreCase("armors")) {
            this.inventoryType = InventoryType.ARMOR_INVENTORY;
            this.firstIndex = 0;
            this.lastIndex = inventoryType.capacity - 1;

        } else if (inventoryName.equalsIgnoreCase("hotbar")) {
            this.inventoryType = InventoryType.MAIN_INVENTORY;
            this.firstIndex = 0;
            this.lastIndex = 8;

        } else {
            throw new TSLSyntaxError("Unknown inventory name -> %s", inventoryName);
        }
    }

    private void parseSlot(List<String> actionWords) throws TSLSyntaxError {
        if (!actionWords.get(0).equalsIgnoreCase("slot"))
            throw new TSLSyntaxError("Unknown inventory name -> %s", actionWords.get(0));

        this.inventoryType = InventoryType.MAIN_INVENTORY;

        try { // Parse first index
            this.firstIndex = Integer.parseInt(actionWords.get(1));

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Malformed integer -> %s", actionWords.get(1));
        }

        try { // Parse last index
            this.lastIndex = Integer.parseInt(actionWords.get(2));

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Malformed integer -> %s", actionWords.get(2));
        }

        // Range check
        if (this.firstIndex < 0)
            throw new TSLSyntaxError("First index cannot be negative");

        if (this.lastIndex >= inventoryType.capacity)
            throw new TSLSyntaxError("Last index cannot be bigger than %d", inventoryType.capacity - 1);

        if (this.firstIndex > this.lastIndex) {
            throw new TSLSyntaxError("First index cannot be greater than the last index.");
        }

    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        shuffle(getInventory(player, inventoryType), this.firstIndex, this.lastIndex);

        CommandSource commandSource = player.getCommandSource()
                .withPermissionLevel(9999).withFeedbackDisabled();
        player.getServer().getCommandManager().handleCommand(commandSource,
                "/playsound minecraft:block.conduit.activate master @s");
        player.getServer().getCommandManager().handleCommand(commandSource,
                "/particle minecraft:end_rod ~ ~ ~ 2 2 2 0.0001 400");
    }

    private void shuffle(List<ItemStack> inventory, int firstIndex, int lastIndex) {
        for (int i = lastIndex; i > firstIndex; i--) {
            int randomIndex = (int) (Math.random() * (i - firstIndex + 1)) + firstIndex;

            ItemStack temp = inventory.get(i);
            inventory.set(i, inventory.get(randomIndex));
            inventory.set(randomIndex, temp);
        }
    }

}
