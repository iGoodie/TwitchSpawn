package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.LinkedList;
import java.util.List;

public abstract class ItemSelectiveAction extends TSLAction {

    public enum InventoryType {
        MAIN_INVENTORY(36), ARMOR_INVENTORY(4), HOTBAR_INVENTORY(9), OFFHAND_INVENTORY(1);

        public int capacity;

        InventoryType(int capacity) { this.capacity = capacity; }
    }

    public enum SelectionType {WITH_INDEX, ONLY_HELD_ITEM, HOTBAR, EVERYTHING, RANDOM}

    protected static class InventorySlot {
        public List<ItemStack> inventory;
        public int index;

        public InventorySlot(List<ItemStack> inventory, int index) {
            this.inventory = inventory;
            this.index = index;
        }

        public ItemStack pullOut() {
            return inventory.set(index, ItemStack.EMPTY);
        }
    }

    protected InventoryType inventoryType = null;
    protected SelectionType selectionType = SelectionType.WITH_INDEX;
    protected int inventoryIndex = -1;

    protected List<ItemStack> getInventory(ServerPlayerEntity player, InventoryType inventoryType) {
        switch (inventoryType) {
            case MAIN_INVENTORY:
                return player.inventory.mainInventory;
            case ARMOR_INVENTORY:
                return player.inventory.armorInventory;
            case OFFHAND_INVENTORY:
                return player.inventory.offHandInventory;
            case HOTBAR_INVENTORY:
                return player.inventory.mainInventory.subList(0, 8 + 1);
            default:
                return null; // Not possible
        }
    }

    protected void parseFrom(List<String> words) throws TSLSyntaxError {
        List<String> leftHand = new LinkedList<>();
        List<String> rightHand = new LinkedList<>();

        List<String> filling = leftHand;

        // Separate left hand and right hand
        for (String word : words) {
            if (word.equalsIgnoreCase("FROM")) {
                filling = rightHand;
                continue;
            }

            filling.add(word);
        }

        String inventoryName = String.join(" ", rightHand);

        // Parse inventory name
        if (inventoryName.equalsIgnoreCase("inventory"))
            inventoryType = InventoryType.MAIN_INVENTORY;
        else if (inventoryName.equalsIgnoreCase("armors"))
            inventoryType = InventoryType.ARMOR_INVENTORY;
        else if (inventoryName.equalsIgnoreCase("hotbar"))
            inventoryType = InventoryType.HOTBAR_INVENTORY;
        else
            throw new TSLSyntaxError("Unknown inventory name -> %s", inventoryName);

        // Parse index - with two words (e.g "slot 2")
        if (leftHand.size() == 2) {
            if (leftHand.get(0).equalsIgnoreCase("slot")) {
                parseInventoryIndex(leftHand);

            } else {
                throw new TSLSyntaxError("Unknown inventory selector -> %s", leftHand);
            }

            // with one word (e.g "everything")
        } else if (leftHand.size() == 1) {
            if (leftHand.get(0).equalsIgnoreCase("everything")) {
                selectionType = SelectionType.EVERYTHING;

            } else if (leftHand.get(0).equalsIgnoreCase("randomly")) {
                selectionType = SelectionType.RANDOM;

            } else {
                throw new TSLSyntaxError("Unknown inventory selector -> %s", leftHand);
            }

        } else {
            throw new TSLSyntaxError("Expected 1 or 2 words before FROM selector, found -> %s", leftHand);
        }
    }

    protected void parseInventoryIndex(List<String> leftHand) throws TSLSyntaxError {
        try {
            inventoryIndex = Integer.parseInt(leftHand.get(1));
        } catch (Exception e) {
            throw new TSLSyntaxError("Malformed slot value, expected integer -> %s", leftHand.get(1));
        }

        if (inventoryIndex < 0 || inventoryType.capacity < inventoryIndex)
            throw new TSLSyntaxError("Slot %d out of bound. It must be between [0,%d]",
                    inventoryIndex, inventoryType.capacity - 1);
    }

    protected void parseSingleWord(List<String> words) throws TSLSyntaxError {
        if (words.size() != 1)
            throw new TSLSyntaxError("Invalid length of words -> " + words);

        String word = words.get(0);

        if (word.equalsIgnoreCase("helmet")) {
            inventoryType = InventoryType.ARMOR_INVENTORY;
            inventoryIndex = 3;

        } else if (word.equalsIgnoreCase("chestplate")) {
            inventoryType = InventoryType.ARMOR_INVENTORY;
            inventoryIndex = 2;

        } else if (word.equalsIgnoreCase("leggings")) {
            inventoryType = InventoryType.ARMOR_INVENTORY;
            inventoryIndex = 1;

        } else if (word.equalsIgnoreCase("boots")) {
            inventoryType = InventoryType.ARMOR_INVENTORY;
            inventoryIndex = 0;

        } else if (word.equalsIgnoreCase("off-hand")) {
            inventoryType = InventoryType.OFFHAND_INVENTORY;
            inventoryIndex = 0;

        } else if (word.equalsIgnoreCase("main-hand")) {
            selectionType = SelectionType.ONLY_HELD_ITEM;

        } else if (word.equalsIgnoreCase("hotbar")) {
            selectionType = SelectionType.HOTBAR;

        } else if (word.equalsIgnoreCase("everything")) {
            selectionType = SelectionType.EVERYTHING;

        } else if (word.equalsIgnoreCase("randomly")) {
            selectionType = SelectionType.RANDOM;

        } else {
            throw new TSLSyntaxError("Unknown slot expression -> %s", word);
        }
    }

    protected InventorySlot randomInventorySlot(ServerPlayerEntity player, boolean includeEmptySlots) {
        List<InventorySlot> possibleSlots = new LinkedList<>();

        InventorySlot fromMainInventory = randomInventorySlot(player.inventory.mainInventory, includeEmptySlots);
        InventorySlot fromArmors = randomInventorySlot(player.inventory.armorInventory, includeEmptySlots);
        InventorySlot fromOffHand = randomInventorySlot(player.inventory.offHandInventory, includeEmptySlots);

        if (fromMainInventory != null)
            possibleSlots.add(fromMainInventory);
        if (fromArmors != null)
            possibleSlots.add(fromArmors);
        if (fromOffHand != null)
            possibleSlots.add(fromOffHand);

        if (possibleSlots.size() == 0)
            return null;

        int randomIndex = (int) (Math.random() * possibleSlots.size());
        return possibleSlots.get(randomIndex);
    }

    protected InventorySlot randomInventorySlot(List<ItemStack> inventory, boolean includeEmptySlots) {
        List<InventorySlot> possibleSlots = new LinkedList<>();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.get(i);
            if (includeEmptySlots || !itemStack.equals(ItemStack.EMPTY)) {
                possibleSlots.add(new InventorySlot(inventory, i));
            }
        }

        if (possibleSlots.size() == 0)
            return null;

        int randomIndex = (int) (Math.random() * possibleSlots.size());
        return possibleSlots.get(randomIndex);
    }

}
