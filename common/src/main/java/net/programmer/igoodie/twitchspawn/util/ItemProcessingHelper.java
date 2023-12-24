package net.programmer.igoodie.twitchspawn.util;


import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;

import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;


/**
 * This class provides helper methods for item processing.
 */
public class ItemProcessingHelper
{
    /**
     * Creates an item stack from given item input and item amount.
     * @param itemInput Item input.
     * @param itemAmount Item amount.
     * @return Created item stack.
     * @throws CommandSyntaxException If something goes wrong during parsing.
     */
    public static ItemStack createItemStack(String itemInput, int itemAmount) throws CommandSyntaxException
    {
        Either<ItemParser.ItemResult, ItemParser.TagResult> itemResult =
            ItemParser.parseForTesting(BuiltInRegistries.ITEM.asLookup(), new StringReader(itemInput));

        if (itemResult.left().isPresent())
        {
            ItemStack itemStack = new ItemStack(itemResult.left().get().item(), itemAmount);
            itemResult.ifRight(tagResult -> itemStack.setTag(tagResult.nbt()));

            return itemStack;
        }

        throw new InternalError("Invalid item format occurred after validation... Something fishy here..");
    }
}
