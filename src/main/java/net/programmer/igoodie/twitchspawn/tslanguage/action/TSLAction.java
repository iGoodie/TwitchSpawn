package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.google.gson.JsonArray;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import net.minecraft.command.CommandPlaySound;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLRuleTokenizer;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;

import java.util.LinkedList;
import java.util.List;

public abstract class TSLAction implements TSLFlowNode {

    /**
     * Determines whether action is a reflection or not
     */
    protected EntityPlayerMP reflectedUser;

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
    protected abstract void performAction(EntityPlayerMP player, EventArguments args);

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
        EntityPlayerMP player = this.isReflection()
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

    protected void notifyPlayer(EntityPlayerMP player, EventArguments args) {
        notifyPlayer(player, titleMessage(args), subtitleMessage(args));
    }

    protected void notifyPlayer(EntityPlayerMP player, String title, String subtitle) {
        // Form and send sound packet
        Vec3d playerPosition = player.getPositionVector();
        float volume = (float) ConfigManager.PREFERENCES.notificationVolume;
        float pitch = ConfigManager.PREFERENCES.notificationPitch == -1
                ? (float) Math.random() : (float) ConfigManager.PREFERENCES.notificationPitch;
        ResourceLocation soundLocation = new ResourceLocation("minecraft:entity.player.levelup");
        SoundCategory soundCategory = SoundCategory.MASTER;
        SoundEvent soundEvent = new SoundEvent(soundLocation);
        SPacketSoundEffect soundPacket = new SPacketSoundEffect(soundEvent, soundCategory,
                playerPosition.x, playerPosition.y, playerPosition.z, volume, pitch);
        player.connection.sendPacket(soundPacket);

        if (ConfigManager.PREFERENCES.messageDisplay == PreferencesConfig.MessageDisplay.DISABLED)
            return; // Stop here since message displaying is disabled

        // Form text and subtext components
        ITextComponent text = ITextComponent.Serializer.fromJsonLenient(title);
        ITextComponent subtext = ITextComponent.Serializer.fromJsonLenient(subtitle);

        if (ConfigManager.PREFERENCES.messageDisplay == PreferencesConfig.MessageDisplay.TITLES) {
            // Form title and subtitle packets
            SPacketTitle packet = new SPacketTitle(SPacketTitle.Type.TITLE, text,
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.1f / 50), // 10
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.7f / 50), // 70
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.2f / 50)); // 20
            SPacketTitle subtitlePacket = new SPacketTitle(SPacketTitle.Type.SUBTITLE, subtext,
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.1f / 50), // 10
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.7f / 50), // 70
                    (int) (ConfigManager.PREFERENCES.notificationDelay * 0.2f / 50)); // 20

            // Send them over
            player.connection.sendPacket(packet);
            player.connection.sendPacket(subtitlePacket);
        }

        if (ConfigManager.PREFERENCES.messageDisplay == PreferencesConfig.MessageDisplay.CHAT) {
            if (text != null) player.sendMessage(new TextComponentString(">> ").appendSibling(text));
            if (subtext != null) player.sendMessage(new TextComponentString(">> ").appendSibling(subtext));
        }
    }

    /**
     * Fetches player entity from the server.
     *
     * @param username Nickname of the player
     * @return Player entity with given nickname
     * @throws IllegalStateException if executed on a non-running server
     */
    protected EntityPlayerMP getPlayer(String username) {
        if (TwitchSpawn.SERVER == null)
            throw new IllegalStateException("TSLAction tried to fetch player from a not-running server.");

        return TwitchSpawn.SERVER.getPlayerList().getPlayerByUsername(username);
    }

    /**
     * Fetches a command sender from a player by slightly changing it on demand.
     *
     * @param player           Multiplayer player entity
     * @param permissionBypass Either bypasses command permissions or not
     * @param feedbackDisabled Either feedback is disabled or not
     * @return
     */
    protected ICommandSender getCommandSender(EntityPlayerMP player, boolean permissionBypass, boolean feedbackDisabled) {
        ICommandSender sourceCommandSender = player.getCommandSenderEntity();

        return new ICommandSender() {
            @Override
            public String getName() {
                return sourceCommandSender.getName();
            }

            @Override
            public ITextComponent getDisplayName() {
                return sourceCommandSender.getDisplayName();
            }

            @Override
            public void sendMessage(ITextComponent component) {
                if (!feedbackDisabled)
                    sourceCommandSender.sendMessage(component);
            }

            @Override
            public boolean canUseCommand(int permLevel, String commandName) {
                return permissionBypass || sourceCommandSender.canUseCommand(permLevel, commandName);
            }

            @Override
            public BlockPos getPosition() {
                return sourceCommandSender.getPosition();
            }

            @Override
            public Vec3d getPositionVector() {
                return sourceCommandSender.getPositionVector();
            }

            @Override
            public World getEntityWorld() {
                return sourceCommandSender.getEntityWorld();
            }

            @Override
            public Entity getCommandSenderEntity() {
                return sourceCommandSender.getCommandSenderEntity();
            }

            @Override
            public boolean sendCommandFeedback() {
                return !feedbackDisabled;
            }

            @Override
            public void setCommandStat(CommandResultStats.Type type, int amount) {
                sourceCommandSender.setCommandStat(type, amount);
            }

            @Override
            public MinecraftServer getServer() {
                return sourceCommandSender.getServer();
            }

            @Override
            public boolean equals(Object obj) {
                return sourceCommandSender.equals(obj);
            }

            @Override
            public int hashCode() {
                return sourceCommandSender.hashCode();
            }

            @Override
            public String toString() {
                return sourceCommandSender.toString();
            }
        };
    }

    /* ------------------------------------------------ */

    protected int parseInt(String string) throws TSLSyntaxError {
        try { return Integer.parseInt(string); } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected an integer, found instead -> %s", string);
        }
    }

}
