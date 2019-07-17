package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;

public abstract class TSLAction extends TSLFlowNode {

    /**
     * Performs the Action on targeted player
     * @param player Target player of the action
     */
    public abstract void performAction(ServerPlayerEntity player);

    /**
     * Executes the action node.
     * It fetches player entity with given nickname from the server
     * before trying to perform the action.
     * @param nickname Nickname of the streamer
     */
    public void execute(String nickname) {
        ServerPlayerEntity player = getPlayer(nickname);

        if (player == null) {
            TwitchSpawn.LOGGER.info("");
            return;
        }

        performAction(player);

        TwitchSpawn.LOGGER.info("{} action performed for {}",
                this.getClass().getSimpleName(), nickname);
    }

    /**
     * Fetches player entity from the server.
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
