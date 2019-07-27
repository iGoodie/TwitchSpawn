package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLPredicate;
import net.programmer.igoodie.twitchspawn.util.MessageEvaluator;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class TSLAction implements TSLFlowNode {

    /**
     * Performs the Action on targeted player
     *
     * @param player Target player of the action
     */
    protected abstract void performAction(ServerPlayerEntity player);

    /**
     * Evaluates a value for given expression
     * that is used in the subtitle.
     *
     * @param expression Expression to be evaluated
     * @param args       Arguments of the event
     * @return Evaluated value of given expression
     */
    protected abstract String subtitleEvaluator(String expression, EventArguments args);

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

        // Fetch title and format it
        String title = ConfigManager.TITLES.getTitleJsonRaw(args.eventAlias);
        title = MessageEvaluator.replaceExpressions(title, expression -> {
            // TODO: Implement better expression evaluator
            if (expression.equals("actor"))
                return args.actorNickname;
            if (expression.equals("streamer"))
                return args.streamerNickname;
            if (expression.equals("amount") && args.donationAmount != 0.0)
                return String.valueOf(args.donationAmount);
            if (expression.equals("amount_i") && args.donationAmount != 0.0)
                return String.valueOf((int) args.donationAmount);
            if (expression.equals("amount_f") && args.donationAmount != 0.0)
                return String.format("%.2f", args.donationAmount);
            if (expression.equals("currency") && args.donationCurrency != null)
                return args.donationCurrency;
            if (expression.equals("month") && args.subscriptionMonths != 0)
                return String.valueOf(args.subscriptionMonths);
            if (expression.equals("viewers") && args.viewerCount != 0)
                return String.valueOf(args.viewerCount);
            if (expression.equals("raiders") && args.raiderCount != 0)
                return String.valueOf(args.raiderCount);
            if (expression.equals("time"))
                return new SimpleDateFormat("HH:mm:ss").format(new Date());
            return "${" + expression + "}";
        });

        // Fetch subtitle and format it
        String actionName = TSLParser.ACTION_CLASSES.inverse().get(getClass());
        String subtitle = ConfigManager.SUBTITLES.getSubtitleJsonRaw(actionName);
        subtitle = MessageEvaluator.replaceExpressions(subtitle, expression -> {
            // TODO Implement better way to evaluate
            if (expression.equals("actor"))
                return args.actorNickname;
            if (expression.equals("streamer"))
                return args.streamerNickname;
            if (expression.equals("amount") && args.donationAmount != 0.0)
                return String.valueOf(args.donationAmount);
            if (expression.equals("amount_i") && args.donationAmount != 0.0)
                return String.valueOf((int) args.donationAmount);
            if (expression.equals("amount_f") && args.donationAmount != 0.0)
                return String.format("%.2f", args.donationAmount);
            if (expression.equals("currency") && args.donationCurrency != null)
                return args.donationCurrency;
            if (expression.equals("month") && args.subscriptionMonths != 0)
                return String.valueOf(args.subscriptionMonths);
            if (expression.equals("viewers") && args.viewerCount != 0)
                return String.valueOf(args.viewerCount);
            if (expression.equals("raiders") && args.raiderCount != 0)
                return String.valueOf(args.raiderCount);
            if (expression.equals("time"))
                return new SimpleDateFormat("HH:mm:ss").format(new Date());

            // Not a common one, go for action specific routine
            String actionEvaluation = subtitleEvaluator(expression, args);
            return actionEvaluation != null ? actionEvaluation : "${" + expression + "}";
        });

        // Notify player and perform the action
        notifyPlayer(player, title, subtitle);
        performAction(player);

        TwitchSpawn.LOGGER.info("{} action performed for {}",
                this.getClass().getSimpleName(), args.streamerNickname);
        return true;
    }

    private static final int DEFAULT_FADE_IN_TICKS = 10;
    private static final int DEFAULT_STAY_TICKS = 70;
    private static final int DEFAULT_FADE_OUT_TICKS = 20;

    private void notifyPlayer(ServerPlayerEntity player, String title, String subtitle) {
        ResourceLocation soundLocation = new ResourceLocation("minecraft:entity.player.levelup");
        SoundCategory category = SoundCategory.MASTER;
        SPlaySoundPacket packetSound = new SPlaySoundPacket(soundLocation, category, player.getPositionVec(), 1.0f, 0.0f);
        player.connection.sendPacket(packetSound);

        ITextComponent text = ITextComponent.Serializer.fromJsonLenient(title);
        STitlePacket packet = new STitlePacket(STitlePacket.Type.TITLE, text,
                DEFAULT_FADE_IN_TICKS, DEFAULT_STAY_TICKS, DEFAULT_FADE_OUT_TICKS);
        player.connection.sendPacket(packet);

        ITextComponent subtext = ITextComponent.Serializer.fromJsonLenient(subtitle);
        STitlePacket subtitlePacket = new STitlePacket(STitlePacket.Type.SUBTITLE, subtext,
                DEFAULT_FADE_IN_TICKS, DEFAULT_STAY_TICKS, DEFAULT_FADE_OUT_TICKS);
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
