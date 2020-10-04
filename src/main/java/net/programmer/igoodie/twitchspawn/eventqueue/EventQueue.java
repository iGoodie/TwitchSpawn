package net.programmer.igoodie.twitchspawn.eventqueue;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;

import java.util.*;

public class EventQueue {

    private final Thread innerThread;
    private volatile EventQueueState state;
    private volatile Deque<EventQueueTask> tasks;
    private volatile boolean waitingForServer;
    private long cooldown; // milliseconds
    private int succeededEvents;
    private int discardedEvents;

    public EventQueue(long cooldownDuration) {
        this.innerThread = new Thread(() -> {
            while (true) stepThread();
        }, "TwitchSpawn Event Queue");
        this.state = EventQueueState.PAUSED;
        this.tasks = new LinkedList<>();
        this.cooldown = cooldownDuration;

        this.innerThread.start();
    }

    private void stepThread() {
        try {
            if (hasUnhandledEvent()) {
                if (waitingForServer) {
                    // TODO: Sleep?
                    return;
                }

                EventQueueTask task = tasks.remove();

                if (task.getType() == EventQueueTask.Type.SLEEP)
                    state = EventQueueState.COOLDOWN;

                task.run();

                if (task.getType() == EventQueueTask.Type.SLEEP)
                    state = EventQueueState.WORKING;

            } else {
                pause();
            }

        } catch (Throwable e) {
            discardedEvents++;
            e.printStackTrace(); // TODO:
        }
    }

    private void unpause() {
        synchronized (innerThread) {
            state = EventQueueState.WORKING;
            innerThread.notifyAll();
        }
    }

    private void pause() {
        synchronized (innerThread) {
            try {
                state = EventQueueState.PAUSED;
                innerThread.wait();

            } catch (InterruptedException e) {
                e.printStackTrace(); // TODO
            }
        }
    }

    public void updateThread() {
        if (state == EventQueueState.PAUSED) unpause();
    }

    public void cancelUpcomingSleep() {
        assert this.tasks instanceof LinkedList;
        List<EventQueueTask> tasks = (LinkedList<EventQueueTask>) this.tasks;
        for (int i = 0; i < tasks.size(); i++) {
            EventQueueTask task = tasks.get(i);
            if (task.getType() == EventQueueTask.Type.SLEEP) {
                tasks.remove(i);
                return;
            }
        }
    }

    public void queueSleepFirst() {
        queueSleepFirst(cooldown);
    }

    public void queueSleepFirst(long millis) {
        tasks.addFirst(new EventQueueTask("Sleep", millis));
    }

    public void queueSleep() {
        queueSleep(cooldown);
    }

    public void queueSleep(long millis) {
        tasks.add(new EventQueueTask("Sleep", millis));
    }

    public void queueFirst(String name, Runnable task) {
        tasks.addFirst(new EventQueueTask(name, task));

    }

    public void queue(Runnable task) {
        queue("Runnable task", task);
    }

    public void queue(String name, Runnable task) {
        tasks.add(new EventQueueTask(name, task));
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

        tasks.add(new EventQueueTask("TSL Event task", () -> {
            waitingForServer = true;
            TwitchSpawn.SERVER.execute(() -> {
                try {
                    boolean performed = eventNode.process(args);

                    if (performed) succeededEvents++;
                    else discardedEvents++;

                } catch (Throwable e) {
                    discardedEvents++;
                    // TODO: "Event failed HUD" maybe?

                } finally {
                    waitingForServer = false;
                }
            });
        }));
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

    public void reset() {
        tasks.clear();
        succeededEvents = 0;
        discardedEvents = 0;
    }

}
