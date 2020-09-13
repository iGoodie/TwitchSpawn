package net.programmer.igoodie.twitchspawn.eventqueue;

import java.util.Timer;
import java.util.TimerTask;

public class EventQueueTask {

    final String name;
    Runnable routine;
    long cooldown;

    public EventQueueTask(Runnable routine) {
        this("Runnable task", routine);
    }

    public EventQueueTask(String name, Runnable routine) {
        this.name = name;
        this.routine = routine;
    }

    public EventQueueTask(long cooldown) {
        this("Cooldown task", cooldown);
    }

    public EventQueueTask(String name, long cooldown) {
        this.name = name;
        this.cooldown = cooldown;
    }

    private void sleep(long millis) throws InterruptedException {
        final Thread currentThread = Thread.currentThread();
        synchronized (currentThread) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (currentThread) {
                        currentThread.notifyAll();
                    }
                }
            }, cooldown);

            currentThread.wait();
        }
    }

    public void run() throws InterruptedException {
        switch (getType()) {
            case SLEEP:
                sleep(cooldown);
                return;

            case ROUTINE:
                routine.run();
                return;

            default:
                System.out.println("Wut?");
        }
    }

    public Type getType() {
        if (routine != null) return Type.ROUTINE;
        return Type.SLEEP;
    }

    @Override
    public String toString() {
        return name + (cooldown == 0 ? "" : String.format("(%d)", cooldown));
    }

    /* --------------------------------- */

    public enum Type {
        SLEEP, ROUTINE
    }

}
