package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.*;
import net.minecraft.nbt.CompoundNBT;

public class CommandArguments {

    public static RequiredArgumentBuilder<CommandSource, String> string(String name) {
        return Commands.argument(name, StringArgumentType.string());
    }

    public static RequiredArgumentBuilder<CommandSource, Integer> integer(String name) {
        return Commands.argument(name, IntegerArgumentType.integer());
    }

    public static RequiredArgumentBuilder<CommandSource, Integer> integer(String name, int min, int max) {
        return Commands.argument(name, IntegerArgumentType.integer(min, max));
    }

    public static RequiredArgumentBuilder<CommandSource, ItemInput> item(String name) {
        return Commands.argument(name, ItemArgument.item());
    }

    public static RequiredArgumentBuilder<CommandSource, EntitySelector> player(String name) {
        return Commands.argument(name, EntityArgument.player());
    }

    public static RequiredArgumentBuilder<CommandSource, String> streamer(String name) {
        return Commands.argument(name, StreamerArgumentType.streamerNick());
    }

    public static RequiredArgumentBuilder<CommandSource, String> rulesetName(String name) {
        return Commands.argument(name, RulesetNameArgumentType.rulesetName());
    }

    public static RequiredArgumentBuilder<CommandSource, CompoundNBT> nbtCompound(String name) {
        return Commands.argument(name, NBTCompoundTagArgument.nbt());
    }

    public static RequiredArgumentBuilder<CommandSource, String> tslWords(String name) {
        return Commands.argument(name, TSLWordsArgumentType.tslWords());
    }

}
