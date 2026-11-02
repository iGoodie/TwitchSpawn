package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.CompoundTag;

public class CommandArguments {

    public static RequiredArgumentBuilder<CommandSourceStack, String> string(String name) {
        return Commands.argument(name, StringArgumentType.string());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Integer> integer(String name) {
        return Commands.argument(name, IntegerArgumentType.integer());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Integer> integer(String name, int min, int max) {
        return Commands.argument(name, IntegerArgumentType.integer(min, max));
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> streamer(String name) {
        return Commands.argument(name, StreamerArgumentType.streamerNick());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> rulesetName(String name) {
        return Commands.argument(name, RulesetNameArgumentType.rulesetName());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, CompoundTag> nbtCompound(String name) {
        return Commands.argument(name, CompoundTagArgument.compoundTag());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> tslWords(String name) {
        return Commands.argument(name, TSLWordsArgumentType.tslWords());
    }

}
