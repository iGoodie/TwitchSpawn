package net.programmer.igoodie.twitchspawn.tslanguage.action;


import com.google.gson.JsonArray;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLRuleTokenizer;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;
import net.programmer.igoodie.twitchspawn.util.MCPHelpers;

public abstract class TSLAction implements TSLFlowNode {

    private static final int DEFAULT_FADE_IN_TICKS = 10;
    private static final int DEFAULT_STAY_TICKS = 70;
    private static final int DEFAULT_FADE_OUT_TICKS = 20;

    /**
     * Determines whether action is a reflection or not
     */
    protected ServerPlayer reflectedUser;

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
     * @return
     */
    public boolean isReflection() {
        return reflectedUser != null;
    }

    /**
     * Splits action part from message part on the display keyword
     *
     * @param words Words to be searched
     * @return Action part
     */
    protected List<String> actionPart(List<String> words) {
        return actionPart(words, TSLRuleTokenizer.DISPLAY_KEYWORD);
    }

    /**
     * Splits action part from message part
     *
     * @param words Words to be searched
     * @param until The word to stop collecting
     * @return Action part
     */
    protected List<String> actionPart(List<String> words, String until) {
        LinkedList<String> actionPart = new LinkedList<>();

        for (String word : words) {
            if (word.equalsIgnoreCase(until))
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
    protected abstract void performAction(ServerPlayer player, EventArguments args);

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
        ServerPlayer player = this.isReflection()
                ? reflectedUser
                : getPlayer(args.streamerNickname);

        if (player == null) {
            TwitchSpawn.LOGGER.info("Player {} is not found. Skipping {} event.",
                    args.streamerNickname, this.getClass().getSimpleName());
            return false;
        }

        // If not silent, notify player
        if (!silent) {
            notifyPlayer(player, args);
        }

        // Perform action for found player
        performAction(player, args);

        TwitchSpawn.LOGGER.info("{} action performed for {}",
                TSLActionKeyword.ofClass(this.getClass()), args.streamerNickname);
        return true;
    }

    @Override
    public boolean willPerform(EventArguments args) {
        return true;
    }

    protected String titleMessage(EventArguments args) {
        String title = this.isReflection()
                ? ConfigManager.TITLES.getTextComponentRaw("reflection")
                : ConfigManager.TITLES.getTextComponentRaw(args.eventName);

        title = replaceExpressions(title, args);

        return title;
    }

    protected String subtitleMessage(EventArguments args) {
        String actionName = associatedSubtitleAction();

        String subtitle = message == null
                ? ConfigManager.SUBTITLES.getTextComponentRaw(actionName)
                : message.toString();

        subtitle = ExpressionEvaluator.replaceExpressions(subtitle, expression -> {
            String messageEvaluation = ExpressionEvaluator.fromArgs(expression, args);

            if (messageEvaluation != null)
                return messageEvaluation;

            // Not a common one, go for action specific routine
            String actionEvaluation = subtitleEvaluator(expression, args);
            return actionEvaluation != null ? actionEvaluation : "${" + expression + "}";
        });

        return subtitle;
    }

    protected String replaceExpressions(String input, EventArguments args) {
        return ExpressionEvaluator.replaceExpressions(input, expression -> {
            String evaluation = ExpressionEvaluator.fromArgs(expression, args);

            if (evaluation != null)
                return evaluation;

            return "${" + expression + "}";
        });
    }

    protected void notifyPlayer(ServerPlayer player, EventArguments args) {
        notifyPlayer(player, titleMessage(args), subtitleMessage(args));
    }

    protected void notifyPlayer(ServerPlayer player, String title, String subtitle) {
        // Form and send sound packet
        float volume = (float) ConfigManager.PREFERENCES.notificationVolume;
        float pitch = ConfigManager.PREFERENCES.notificationPitch == -1
                ? (float) Math.random() : (float) ConfigManager.PREFERENCES.notificationPitch;
        SoundEvent soundLocation = SoundEvents.PLAYER_LEVELUP;
        SoundSource category = SoundSource.MASTER;
        Vec3 position = player.position();
        Holder<SoundEvent> lookup = player.getServer().registryAccess().
            registryOrThrow(Registries.SOUND_EVENT).
            wrapAsHolder(soundLocation);

        ClientboundSoundPacket packetSound = new ClientboundSoundPacket(lookup,
            category,
            position.x,
            position.y,
            position.z,
            volume,
            pitch,
            0);
        player.connection.send(packetSound);

        if (ConfigManager.PREFERENCES.messageDisplay == PreferencesConfig.MessageDisplay.DISABLED)
            return; // Stop here since message displaying is disabled

        // Form text and subtext components
        Component text = MCPHelpers.fromJsonLenient(title);
        Component subtext = MCPHelpers.fromJsonLenient(subtitle);

        if (subtext != null && subtext.getString().equals("NOTHING_0xDEADC0DE_0xDEADBEEF")) {
            return; // Stop here since it was a  DISPLAYING NOTHING statement
        }

        if (ConfigManager.PREFERENCES.messageDisplay == PreferencesConfig.MessageDisplay.TITLES) {
            // Form title and subtitle packets
            ClientboundSetTitleTextPacket packet = new ClientboundSetTitleTextPacket(text);
            ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(subtext); // 20
            ClientboundSetTitlesAnimationPacket timePacket = new ClientboundSetTitlesAnimationPacket(
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.1f / 50), // 10
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.7f / 50), // 70
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.2f / 50));// 20

            // Send them over
            player.connection.send(packet);
            player.connection.send(subtitlePacket);
            player.connection.send(timePacket);
        }

        if (ConfigManager.PREFERENCES.messageDisplay == PreferencesConfig.MessageDisplay.CHAT) {
            if (text != null) player.sendSystemMessage(MCPHelpers.merge(Component.translatable(">> "), text));
            if (subtext != null) player.sendSystemMessage(MCPHelpers.merge(Component.translatable(">> "), subtext));
        }
    }

    /**
     * Fetches player entity from the server.
     *
     * @param username Nickname of the player
     * @return Player entity with given nickname
     * @throws IllegalStateException if executed on a non-running server
     */
    protected ServerPlayer getPlayer(String username) {
        if (TwitchSpawn.SERVER == null)
            throw new IllegalStateException("TSLAction tried to fetch player from a not-running server.");

        return TwitchSpawn.SERVER.getPlayerList().getPlayerByName(username);
    }

    /* ------------------------------------------------ */

    protected int parseInt(String string) throws TSLSyntaxError {
        try { return Integer.parseInt(string); } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected an integer, found instead -> %s", string);
        }
    }
}
