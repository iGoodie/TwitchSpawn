package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.google.gson.JsonArray;
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
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLRuleTokenizer;
import net.programmer.igoodie.twitchspawn.util.MessageEvaluator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class TSLAction implements TSLFlowNode {

    /**
     * Determines whether action should be notified to the player or not
     */
    protected boolean silent = false;

    /**
     * If message field is not null,
     * then the action is overriding the default subtitle message.
     */
    protected JsonArray message;

    /**
     * Splits action part from message part on the display keyword
     * @param words Words to be searched
     * @return Action part
     */
    protected List<String> actionPart(List<String> words) {
        return actionPart(words, TSLRuleTokenizer.DISPLAY_KEYWORD);
    }

    /**
     * Splits action part from message part
     * @param words Words to be searched
     * @param until The word to stop collecting
     * @return Action part
     */
    protected List<String> actionPart(List<String> words, String until) {
        LinkedList<String> actionPart = new LinkedList<>();

        for(String word : words) {
            if(word.equalsIgnoreCase(until))
                break;
            actionPart.add(word);
        }

        return actionPart;
    }

    /**
     * Performs the Action on targeted player
     *
     * @param player Target player of the action
     * @param args   Arguments of the event
     */
    protected abstract void performAction(ServerPlayerEntity player, EventArguments args);

    /**
     * Evaluates a value for given expression
     * that is used in the subtitle.
     *
     * @param expression Expression to be evaluated
     * @param args       Arguments of the event
     * @return Evaluated value of given expression
     */
    protected String subtitleEvaluator(String expression, EventArguments args) {
        return null;
    }

    /**
     * Some TSL actions might need other
     * action's subtitle json. In those cases,
     * subclass modifies this method.
     * (E.g {@link EitherAction})
     *
     * @return Associated subtitle action's name
     */
    protected String associatedSubtitleAction() {
        return TSLActionKeyword.ofClass(getClass());
    }

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

        // If not silent, notify player
        if (!silent) {
            // Fetch title and format it
            String title = ConfigManager.TITLES.getTextComponentRaw(args.eventName);
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
                if (expression.equals("months") && args.subscriptionMonths != 0)
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
            String actionName = associatedSubtitleAction();
            String subtitle = message == null
                    ? ConfigManager.SUBTITLES.getTextComponentRaw(actionName)
                    : message.toString();
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
                if (expression.equals("months") && args.subscriptionMonths != 0)
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

            // Finally notify player
            notifyPlayer(player, title, subtitle);
        }

        // Perform action for found player
        performAction(player, args);

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
