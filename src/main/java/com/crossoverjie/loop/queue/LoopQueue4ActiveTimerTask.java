package com.crossoverjie.loop.queue;

import java.util.*;
import java.util.concurrent.*;

/**
 * input: 如果30s没有消息来，就把用户的状态设置维离线。 模拟消息使用`long uid` 来表示消息到了。
 * 1. 拿什么来存储  --> 30大小的环形队列
 * 2. 如何更新数据
 * 3. 到时间的任务，如何处理。
 * 4. 需要几个数据结构，是否需要线程安全
 */
public class LoopQueue4ActiveTimerTask {
    volatile List<Set<Long>> loopQueue = new ArrayList(30);
    volatile int currentIndex = 0;
    Map<Long, Integer> indexMap = new ConcurrentHashMap<>();

    LoopQueue4ActiveTimerTask() {
        for (int i = 0; i < 30; i++) {
            loopQueue.add(ConcurrentHashMap.<Long>newKeySet());
        }
    }

    /**
     * 每隔1s转动环形队列
     */
    class TimerTask implements Runnable {
        @Override
        public void run() {
            int index = currentIndex;
            currentIndex = (currentIndex + 1) % 30;
            Set<Long> messages = loopQueue.get(index);

            for (Iterator<Long> it = messages.iterator(); it.hasNext(); ) {
                Long message = it.next();
                System.out.println("do something, message:" + message);
                it.remove();
                indexMap.remove(message); // TODO
            }
        }
    }

    public void addMessage(Long uid) {
        if (indexMap.containsKey(uid)) {
            Set<Long> currLocation = loopQueue.get(indexMap.get(uid));
            currLocation.remove(uid);
            indexMap.remove(uid); // TODO
        }
        int targetIndex = (currentIndex - 1 + 30) % 30;
        Set<Long> targetLocation = loopQueue.get(targetIndex);
        targetLocation.add(uid);
        indexMap.put(uid, targetIndex); // TODO: indexMap 的remove 和 put 无法做到数据的最终一直性
    }

    public static void main(String[] args) throws InterruptedException {
        LoopQueue4ActiveTimerTask mainThread = new LoopQueue4ActiveTimerTask();

        // 1s schedule thread
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(mainThread.new TimerTask(), 1, 1, TimeUnit.SECONDS);

        // add msg to loop queue
        for (int i = 0; i < 60; i++) {
            mainThread.addMessage(new Long(i));
            TimeUnit.MILLISECONDS.sleep(500);
        }

        for (int i = 15; i < 60; i++) {
            mainThread.addMessage(new Long(i));
            TimeUnit.SECONDS.sleep(2);
        }
    }


}
