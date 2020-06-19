package net.programmer.igoodie.twitchspawn.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class EventQueue {

    private Timer timer;
    private volatile boolean timerTicking;
    private volatile Queue<TimerTask> tasks;
    private long cooldown; // milliseconds
    private long frozenUntil; // ms timestamp
    private int succeededEvents;
    private int discardedEvents;

    public EventQueue(long cooldownDuration) {
        this.timer = new Timer();
        this.tasks = new LinkedList<>();
        this.cooldown = cooldownDuration;
        this.frozenUntil = -1;
    }

    public void queue(Runnable task) {
        tasks.add(new TimerTask() {
            @Override
            public void run() {
                try {
                    long now = System.currentTimeMillis();

                    task.run();

                    frozenUntil = now + cooldown;

                } catch (Throwable e) {
                    discardedEvents++;
                    // TODO: "Event failed HUD"
                }
            }
        });
        updateTimer();
    }

    public void queue(TSLEvent eventNode, EventArguments args, CooldownBucket cooldownBucket) {
        if (eventNode.willPerform(args)) {
            if (cooldownBucket != null) {
                cooldownBucket.consume(args.actorNickname);

                ServerPlayerEntity playerEntity = TwitchSpawn.SERVER
                        .getPlayerList()
                        .getPlayerByUsername(args.streamerNickname);

                if (playerEntity != null) {
                    NetworkManager.CHANNEL.sendTo(
                            new GlobalChatCooldownPacket(cooldownBucket.getGlobalCooldownTimestamp()),
                            playerEntity.connection.netManager,
                            NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            }
        }
        tasks.add(new TimerTask() {
            @Override
            public void run() {
                TwitchSpawn.SERVER.execute(() -> {
                    try {
                        long now = System.currentTimeMillis();
                        boolean performed = eventNode.process(args);

                        if (!performed) {
                            discardedEvents++;
                            return;
                        }

                        frozenUntil = now + cooldown;
                        succeededEvents++;

                    } catch (Throwable e) {
                        discardedEvents++;
                        // TODO: "Event failed HUD"
                    }
                });
            }
        });
        updateTimer();
    }

    private synchronized void updateTimer() {
        if (!timerTicking) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (hasUnhandledEvent()) {
                        proceed();

                    } else {
                        this.cancel(); // XXX: Is reliable?
                        timerTicking = false;
                    }
                }
            }, 10, 10); // per 10ms
            timerTicking = true;
        }
    }

    public synchronized int succeededEventCount() {
        return succeededEvents;
    }

    public synchronized int discardedEventCount() {
        return discardedEvents;
    }

    public synchronized int unhandledEventCount() {
        return tasks.size();
    }

    public synchronized boolean hasUnhandledEvent() {
        return !tasks.isEmpty();
    }

    public boolean proceed() {
        long now = System.currentTimeMillis();
        if (now < frozenUntil) return false;
        if (tasks.isEmpty()) return false;

        TimerTask task = tasks.remove();
        task.run();
        return true;
    }

    public void reset() {
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timerTicking = false;
        tasks.clear();
        this.frozenUntil = -1;
        succeededEvents = 0;
        discardedEvents = 0;
    }

}
