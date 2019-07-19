package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;

import java.lang.reflect.Field;

public abstract class TSLAction implements TSLFlowNode {

    /**
     * Performs the Action on targeted player
     *
     * @param player Target player of the action
     */
    protected abstract void performAction(ServerPlayerEntity player);

    /**
     * Processes the action node with given .
     * It fetches player entity with given nickname from the server
     * before trying to perform the action.
     *
     * @param args Arguments of the event (derived from Streamlabs Socket API)
     * @return True if processed successfully
     */
    @Override
    public boolean process(EventArguments args) {
        System.out.println("Processing " + getClass().getSimpleName());
        if(args == null) return true;

        ServerPlayerEntity player = getPlayer(args.streamerNickname);

        if (player == null) {
            TwitchSpawn.LOGGER.info("Player {} is not found. Skipping {} event.",
                    args.streamerNickname, this.getClass().getSimpleName());
            return false;
        }

        performAction(player);

        TwitchSpawn.LOGGER.info("{} action performed for {}",
                this.getClass().getSimpleName(), args.streamerNickname);
        return true;
    }

    /**
     * Fetches player entity from the server.
     *
     * @param username Nickname of the player
     * @return Player entity with given nickname
     * @throws IllegalStateException if executed on a non-running server
     */
    private ServerPlayerEntity getPlayer(String username) {
        MinecraftServer server = TwitchSpawn.SERVER;

        if (server == null)
            throw new IllegalStateException("TSLAction tried to fetch player from a not-running server.");

        return server.getPlayerList().getPlayerByUsername(username);
    }

}
