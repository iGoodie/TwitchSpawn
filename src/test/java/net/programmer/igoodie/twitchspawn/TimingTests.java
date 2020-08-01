package net.programmer.igoodie.twitchspawn;

import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TimingTests {

    @Test
    @DisplayName("should preserve queue and approximate timing")
    public void timerQueueTest() throws InterruptedException {
        long duration = 1 * 1000;
        EventQueue queue = new EventQueue(duration);
        List<Integer> list = new LinkedList<>();

        // Fill queue
        queue.queue(() -> list.add(1));
        queue.queue(() -> list.add(2));
        queue.queue(() -> list.add(3));
        queue.queue(() -> list.add(4));
        queue.queue(() -> list.add(5));

        // Wait for timer to consume task
        for (int i = 5; i >= 0; i--) {
            System.out.println("Waiting for #" + (5 - i + 1));
            Assertions.assertEquals(i, queue.unhandledEventCount());
            if (i != 0) Thread.sleep(duration - 10);
        }

        System.out.println(list);
        Assertions.assertIterableEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

}
