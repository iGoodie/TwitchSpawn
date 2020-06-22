package net.programmer.igoodie.twitchspawn.command;

import net.minecraft.command.ICommandSender;

import javax.annotation.Nonnull;

public class TSCommand extends TwitchSpawnCommand {

    @Nonnull
    @Override
    public String getName() {
        return "ts";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/ts " + String.join("|", modules.keySet());
    }

}
