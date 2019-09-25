package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ModuleReloadcfg extends CommandModule {

    @Override
    public String getName() {
        return "reloadcfg";
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        System.out.println("Executed " + getName());
    }

}
