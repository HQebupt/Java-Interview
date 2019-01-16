package com.crossoverjie.loop.queue;


import java.util.*;
import java.util.concurrent.*;

/**
 * input: a message (string)
 * output: after 48 hours, consume the message
 */
public class LoopQueue4TimerTask {
    ExecutorService executor = Executors.newFixedThreadPool(16);
    List<Set<Task>> loopQueue = new ArrayList(3600);
    volatile int currentIndex = 0;

    LoopQueue4TimerTask() {
        for (int i = 0; i < 3600; i++) {
            loopQueue.add(ConcurrentHashMap.<Task>newKeySet());
        }
    }

    class TimerTask implements Runnable {
        @Override
        public void run() {
            int index = currentIndex;
            currentIndex = (currentIndex + 1) % 3600;
            Set<Task> tasks = loopQueue.get(index);
            for (Iterator<Task> it = tasks.iterator(); it.hasNext(); ) {
                Task task = it.next();
                if (task.cycleNum == 0) {
                    executor.submit(task);
                    it.remove();
                } else {
                    task.cycleNum--;
                }
            }
        }
    }

    static class Task implements Runnable {
        int cycleNum;
        String message;
        int delaySeconds;

        Task(int delaySeconds, String message) {
            this.delaySeconds = delaySeconds;
            this.message = message;
            this.cycleNum = delaySeconds / 3600;
        }

        @Override
        public void run() {
            System.out.println("tid:" + Thread.currentThread().getId() + "; message:" + message);
        }
    }

    public void addTask(Task task) {
        int seconds = task.delaySeconds + currentIndex;
        int index = seconds % 3600;
        loopQueue.get(index).add(task);
    }

    public static void main(String[] args) {
        LoopQueue4TimerTask mainThread = new LoopQueue4TimerTask();

        // 1s schedule thread
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(mainThread.new TimerTask(), 1, 1, TimeUnit.SECONDS);

        // add Task to loop queue
        mainThread.addTask(new Task(3610, "task 3610 is coming."));
        mainThread.addTask(new Task(1, "task 1 is coming."));
        mainThread.addTask(new Task(2, "task 2 is coming."));
        mainThread.addTask(new Task(3, "task 3 is coming."));

    }

}
