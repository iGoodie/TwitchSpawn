package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
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
        TwitchSpawn.LOGGER.debug("Reached TSLAction node -> {} with {}",
                this.getClass().getSimpleName(), args);

        ServerPlayerEntity player = getPlayer(args.streamerNickname);

        if (player == null) {
            TwitchSpawn.LOGGER.info("Player {} is not found. Skipping {} event.",
                    args.streamerNickname, this.getClass().getSimpleName());
            return false;
        }

        notifyPlayer(player, args, "\"${streamer} got an action\"", "\"Test thingies\"");
        performAction(player);

        TwitchSpawn.LOGGER.info("{} action performed for {}",
                this.getClass().getSimpleName(), args.streamerNickname);
        return true;
    }

    private void notifyPlayer(ServerPlayerEntity player, EventArguments args, String title, String subtitle) {
        title = title.replace("${actor}", args.actorNickname)
                .replace("${streamer}", args.streamerNickname);
        subtitle = subtitle.replace("${actor}", args.actorNickname)
                .replace("${streamer}", args.streamerNickname);

        ResourceLocation soundLocation = new ResourceLocation("minecraft:entity.player.levelup");
        SoundCategory category = SoundCategory.MASTER;
        SPlaySoundPacket packetSound = new SPlaySoundPacket(soundLocation, category, player.getPositionVec(), 1.0f, 0.0f);
        player.connection.sendPacket(packetSound);

        ITextComponent text = ITextComponent.Serializer.fromJsonLenient(title);
        STitlePacket packet = new STitlePacket(STitlePacket.Type.TITLE, text);
        player.connection.sendPacket(packet);

        ITextComponent subtext = ITextComponent.Serializer.fromJsonLenient(subtitle);
        STitlePacket subtitlePacket = new STitlePacket(STitlePacket.Type.SUBTITLE, subtext);
        player.connection.sendPacket(subtitlePacket);
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
