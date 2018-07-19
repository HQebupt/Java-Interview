package com.crossoverjie.actual;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Function: 两个线程交替执行打印 1~100
 *
 * lock 版
 *
 * @author crossoverJie
 *         Date: 11/02/2018 10:04
 * @since JDK 1.8
 */
public class TwoThread {

    private int start = 1;

    /**
     * 保证内存可见性
     * 其实用锁了之后也可以保证可见性 这里用不用 volatile 都一样
     */
    private boolean flag = false;

    /**
     * 重入锁
     */
    private final static Lock LOCK = new ReentrantLock();

    public static void main(String[] args) {
        TwoThread twoThread = new TwoThread();

        Thread t0 = new Thread(new OuNum(twoThread));
        t0.setName("t0");


        Thread t1 = new Thread(new JiNum(twoThread));
        t1.setName("t1");

        t0.start();
        t1.start();
    }

    /**
     * 偶数线程
     */
    public static class OuNum implements Runnable {

        private TwoThread number;

        public OuNum(TwoThread number) {
            this.number = number;
        }

        @Override
        public void run() {
            while (number.start <= 10000) {
                try {
                        LOCK.lock();
                        if (number.flag) {
                            System.out.println(Thread.currentThread().getName() + "+-+" + number.start);
                            number.start++;
                            number.flag = false;
                        }
                    } finally {
                        LOCK.unlock();
                }
            }
        }
    }

    /**
     * 奇数线程
     */
    public static class JiNum implements Runnable {

        private TwoThread number;

        public JiNum(TwoThread number) {
            this.number = number;
        }

        @Override
        public void run() {
            while (number.start <= 10000) {
                try {
                    LOCK.lock();
                    if (!number.flag) {
                        System.out.println(Thread.currentThread().getName() + "+-+" + number.start);
                        number.start++;
                        number.flag = true;
                    }
                } finally {
                    LOCK.unlock();
                } 
            }
        }
    }
}
