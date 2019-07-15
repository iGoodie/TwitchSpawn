package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;

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

}
