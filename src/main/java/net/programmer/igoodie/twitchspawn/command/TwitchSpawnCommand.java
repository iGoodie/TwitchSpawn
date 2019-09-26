package net.programmer.igoodie.twitchspawn.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.command.module.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TwitchSpawnCommand extends CommandBase {

    Map<String, CommandModule> modules;

    public TwitchSpawnCommand() {
        this.modules = new HashMap<>();

        registerModule(new ModuleStart());
        registerModule(new ModuleStop());
        registerModule(new ModuleReloadcfg());
        registerModule(new ModuleStatus());
        registerModule(new ModuleRules());
        registerModule(new ModuleSimulate());
        registerModule(new ModuleTest());
    }

    private void registerModule(CommandModule module) {
        this.modules.put(module.getName(), module);
    }

    /* -------------------------------------------- */

    @Nonnull
    @Override
    public String getName() {
        return "twitchspawn";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/twitcspawn " + String.join("|", modules.keySet());
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, @Nonnull ICommandSender sender) {
        return true;
    }

    /* -------------------------------------------- */

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] commandArgs, @Nullable BlockPos targetPos) {
        TwitchSpawn.LOGGER.debug("Completing for {} (len={}) in {}\n",
                Arrays.toString(commandArgs), commandArgs.length, modules.keySet());

        // First word, complete with module names!
        if (commandArgs.length == 1)
            return getListOfStringsMatchingLastWord(commandArgs, modules.keySet());

        // Fetch associated module
        String moduleName = commandArgs[0];
        CommandModule commandModule = modules.get(moduleName);

        // Return module's tab completions, if present
        if (commandModule != null)
            return commandModule.getTabCompletions(splitModuleArgs(commandArgs));

        // No completions
        return Collections.emptyList();
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] commandArgs) throws CommandException {
        TwitchSpawn.LOGGER.debug("Executing for {} (len={})\n",
                Arrays.toString(commandArgs), commandArgs.length);

        // Expected at least the module name
        if (commandArgs.length == 0)
            throw new WrongUsageException(getUsage(sender));

        // Fetch associated module
        String moduleName = commandArgs[0];
        CommandModule commandModule = modules.get(moduleName);

        // Unknown module within the args
        if (commandModule == null)
            throw new CommandException("Unknown module -> " + moduleName);

        // Execute the module
        commandModule.execute(sender, splitModuleArgs(commandArgs));
    }

    private String[] splitModuleArgs(String[] commandArgs) {
        return Arrays.copyOfRange(commandArgs, 1, commandArgs.length);
    }

}
