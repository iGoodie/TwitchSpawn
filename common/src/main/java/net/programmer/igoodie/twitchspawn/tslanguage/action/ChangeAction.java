package net.programmer.igoodie.twitchspawn.tslanguage.action;


import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;
import net.programmer.igoodie.twitchspawn.util.ItemProcessingHelper;

public class ChangeAction extends ItemSelectiveAction {

    private String itemRaw;
    private int itemAmount;

    /*
     * Exemplar valid TSL:
     * CHANGE helmet INTO apple
     * CHANGE slot 21 FROM inventory INTO %stick{display:{Name:"\"Stick of Truth!\""}}%
     * CHANGE inventory INTO slime_ball{display:{Name:"\"Ewwww\""}}
     */
    public ChangeAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        List<String> slotSelectionPart = new LinkedList<>();
        List<String> itemPart = new LinkedList<>();
        splitParts(actionWords, slotSelectionPart, itemPart);

        // Parse slot selection part
        parseSlot(slotSelectionPart);

        // Parse item part
        parseItem(itemPart);
    }

    private void splitParts(List<String> actionWords, List<String> slotSelectionPart, List<String> itemPart) throws TSLSyntaxError {
        long delimiterCount = actionWords.stream()
                .filter(word -> word.equalsIgnoreCase("INTO")).count();

        if (delimiterCount != 1)
            throw new TSLSyntaxError("Expected exactly one INTO word.");

        List<String> filling = slotSelectionPart;

        for (String actionWord : actionWords) {
            if (actionWord.equalsIgnoreCase("INTO")) {
                filling = itemPart;
                continue;
            }
            filling.add(actionWord);
        }
    }

    private void parseSlot(List<String> slotSelectionPart) throws TSLSyntaxError {
        long countFrom = slotSelectionPart.stream()
                .filter(word -> word.equalsIgnoreCase("FROM")).count();

        if (countFrom == 0) parseSingleWord(slotSelectionPart);
        else if (countFrom == 1) parseFrom(slotSelectionPart);
        else throw new TSLSyntaxError("At most 1 FROM statement expected, found %d instead.", countFrom);
    }

    private void parseItem(List<String> itemPart) throws TSLSyntaxError {
        if (itemPart.size() != 1 && itemPart.size() != 2)
            throw new TSLSyntaxError("Invalid length of item words: " + itemPart);

        this.itemRaw = itemPart.get(0);

        try {
            this.itemAmount = itemPart.size() != 2 ? 1 : Integer.parseInt(itemPart.get(1));

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected an integer, found instead -> %s", itemPart.get(1));
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
            // Why whole in try-catch? Well, I think it fits better here. Can change if not needed.
            ItemStack itemStack = ItemProcessingHelper.createItemStack(
                this.replaceExpressions(this.itemRaw, args),
                this.itemAmount);

            if (this.selectionType == SelectionType.WITH_INDEX) {
                this.getInventory(player, this.inventoryType).set(this.inventoryIndex, itemStack.copy());
            } else if (this.selectionType == SelectionType.EVERYTHING) {
                if (this.inventoryType == null) {
                    this.setAll(player.getInventory().items, itemStack);
                    this.setAll(player.getInventory().armor, itemStack);
                    this.setAll(player.getInventory().offhand, itemStack);
                } else {
                    this.setAll(getInventory(player, this.inventoryType), itemStack);
                }
            } else if (this.selectionType == SelectionType.RANDOM)  {
                InventorySlot randomSlot = this.inventoryType == null
                    ? randomInventorySlot(player, true)
                    : randomInventorySlot(this.getInventory(player, this.inventoryType), true);

                if (randomSlot != null) {
                    randomSlot.inventory.set(randomSlot.index, itemStack.copy());
                }
            } else if (this.selectionType == SelectionType.ONLY_HELD_ITEM) {
                int selectedHotbarIndex = player.getInventory().selected;
                player.getInventory().items.set(selectedHotbarIndex, itemStack.copy());
            } else if (this.selectionType == SelectionType.HOTBAR) {
                for (int i = 0; i <= 8; i++)
                {
                    player.getInventory().items.set(i, itemStack.copy());
                }
            }
        } catch (CommandSyntaxException e) {
            TwitchSpawn.LOGGER.error("Failed to parse item: " + this.itemRaw + " with error: " + e.getRawMessage());
        }

        MinecraftServer server = player.getServer();

        if (server != null) {
            CommandSourceStack commandSource = player.createCommandSourceStack()
                    .withPermission(9999).withSuppressedOutput();
            server.getCommands().performPrefixedCommand(commandSource,
                    "/playsound minecraft:item.armor.equip_leather master @s");
            server.getCommands().performPrefixedCommand(commandSource,
                    "/particle minecraft:entity_effect ~ ~ ~ 2 2 2 0.1 400");
        }
    }

    private void setAll(List<ItemStack> inventory, ItemStack itemStack) {
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, itemStack.copy());
        }
    }
}
