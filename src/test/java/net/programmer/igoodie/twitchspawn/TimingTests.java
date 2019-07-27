package net.programmer.igoodie.twitchspawn;

import net.programmer.igoodie.twitchspawn.time.TimeTaskQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TimingTests {

    @Test
    @DisplayName("should preserve queue and approximate timing")
    public void timerQueueTest() throws InterruptedException {
        long duration = 1 * 1000;
        TimeTaskQueue queue = new TimeTaskQueue(duration);
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
            Assertions.assertEquals(i, queue.size());
            if (i != 0) Thread.sleep(duration);
        }

        System.out.println(list);
        Assertions.assertIterableEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

}
