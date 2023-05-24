package net.programmer.igoodie.twitchspawn.network.server;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class DiscordConnection extends ListenerAdapter implements CommandSource {

    public static DiscordConnection INSTANCE;

    public static void start() {
        try {
            if (INSTANCE != null) INSTANCE.jda.shutdownNow();
            INSTANCE = new DiscordConnection(ConfigManager.DISCORD_CONN.getToken());
        } catch (Exception e) {
            TwitchSpawn.LOGGER.error("Failed to connect to Discord");
        }
    }

    protected JDA jda;

    private DiscordConnection(String token) throws Exception {
        this.jda = JDABuilder.createDefault(token)
                .addEventListeners(this)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        this.jda.awaitReady();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        TwitchSpawn.LOGGER.info("Connected to Discord. {} guilds available.", event.getGuildAvailableCount());
    }

    protected Queue<String> commands = new LinkedList<>();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!ConfigManager.DISCORD_CONN.getChannelId().equals(event.getChannel().getId()))
            return;

        if (!ConfigManager.DISCORD_CONN.getEditorIds().contains(event.getAuthor().getId()))
            return;

        String command = event.getMessage().getContentRaw();

        if (!command.startsWith("/"))
            return;

        commands.add(command);

        if (TwitchSpawn.SERVER != null) {
            while (!commands.isEmpty()) {
                String awaitingCommand = commands.poll();
                TwitchSpawn.SERVER.execute(() -> {
                    CommandSourceStack sourceStack = createCommandSourceStack();
                    int result = TwitchSpawn.SERVER.getCommands().performCommand(sourceStack, awaitingCommand);
                    EmojiUnion emoji = result >= 0
                            ? Emoji.fromFormatted("\uD83D\uDC4D")
                            : Emoji.fromFormatted("\uD83D\uDC4E");
                    event.getMessage().addReaction(emoji).queue();
                });
            }
        }
    }

    /* -------------------------------- */

    private static final Component DISCORD_COMPONENT = new TextComponent("DiscordConn");

    public CommandSourceStack createCommandSourceStack() {
        ServerLevel serverLevel = TwitchSpawn.SERVER.overworld();
        return new CommandSourceStack(this,
                Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()),
                Vec2.ZERO,
                serverLevel,
                4,
                "DiscordConn",
                DISCORD_COMPONENT,
                TwitchSpawn.SERVER,
                null);
    }

    @Override
    public void sendMessage(@NotNull Component p_80166_, @NotNull UUID p_80167_) {
        // Do nothing when a message is sent
    }

    @Override
    public boolean acceptsSuccess() {
        return false;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

}
