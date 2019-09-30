package net.programmer.igoodie.twitchspawn.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class ItemDataCommand extends CommandBase {

    @Override
    public String getName() {
        return "itemdata";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/itemdata";
    }

    /* ------------------------------------------------- */

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            System.out.printf("Sender's type is a(n) %s\n", sender.getClass().getSimpleName());
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        ItemStack heldItemstack = player.inventory.mainInventory.get(player.inventory.currentItem);

        System.out.printf("Holding: %s, %s\n",
                heldItemstack.getDisplayName(),
                heldItemstack.serializeNBT());
    }

}
