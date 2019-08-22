package net.programmer.igoodie.twitchspawn.time;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class TimeTaskQueue {

    private long duration; // milliseconds
    private Deque<TimerTask> queue;
    private Timer timer;

    public TimeTaskQueue(long duration) {
        this.duration = duration;
        this.queue = new LinkedList<>();
        this.timer = new Timer();
    }

    public void queue(Runnable task) {
        TimerTask actualTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    task.run();

                } catch (Exception e) {
                    TwitchSpawn.LOGGER.info("Failed to run task");
                    e.printStackTrace();
                }
            }
        };

        TimerTask enderTask = new TimerTask() {
            @Override
            public void run() {
                // Unlocks the time period
                queue.poll(); // Remove self
            }
        };

        long delay = 0;

        if (!queue.isEmpty()) {
            long now = System.currentTimeMillis();
            long lastStart = queue.getLast().scheduledExecutionTime();
            delay += (lastStart - now); // dt
        }

        queue.add(enderTask);
        timer.schedule(actualTask, delay);
        timer.schedule(enderTask, delay + duration);
    }

    public int size() {
        return queue.size();
    }

    public void cleanAll() {
        timer.cancel();
        queue.clear();
        timer = new Timer();
    }

}
