package net.programmer.igoodie.twitchspawn.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ItemParser {

    String itemId;
    String itemNbt;

    public ItemParser(String itemText) {
        String[] itemTokens = itemText.split("\\{", 2);

        this.itemId = itemTokens[0];
        this.itemNbt = itemTokens.length != 2 ? null : "{" + itemTokens[1];
    }

    public Item getItem() {
        ResourceLocation itemResource = new ResourceLocation(itemId);
        return Item.REGISTRY.getObject(itemResource);
    }

    public NBTTagCompound getNBT() {
        try {
            return JsonToNBT.getTagFromJson(itemNbt);

        } catch (NBTException e) {
            return null;
        }
    }

    public boolean isValid() {
        if (getItem() == null)
            return false;

        if (itemNbt != null && getNBT() == null)
            return false;

        return true;
    }

    public ItemStack generateItemStack(int amount) {
        if (!isValid())
            throw new IllegalArgumentException("Cannot generate item stack from invalid item text");

        ItemStack itemStack = new ItemStack(getItem(), amount);

        if (itemNbt != null) {
            NBTTagCompound nbt = getNBT();
            if (nbt != null) {
                itemStack.setTagCompound(nbt);
            }
        }

        return itemStack;
    }

    @Override
    public String toString() {
        return String.format("{id:%s, nbt:%s}", itemId, itemNbt);
    }

}
