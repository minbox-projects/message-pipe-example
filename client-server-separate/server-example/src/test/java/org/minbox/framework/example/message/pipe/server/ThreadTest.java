package org.minbox.framework.example.message.pipe.server;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 恒宇少年
 */
public class ThreadTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static Boolean run = true;//控制是否生产和消费
    private static final Integer MAX_CAPACITY = 5;//缓冲区最大数量
    private static final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();//缓冲队列

    public static void main(String[] args) throws Exception {
        executorService.submit(() -> new Provider().start());
        executorService.submit(() -> new Consumer().start());
    }

    /**
     * 生产者
     */
    static class Provider {
        public void start() {
            while (run) {
                synchronized (queue) {
                    while (queue.size() >= MAX_CAPACITY * 2) {
                        try {
                            System.out.println("缓冲队列已满，等待消费");
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        String string = UUID.randomUUID().toString();
                        queue.put(string);
                        System.out.println("生产:" + string);
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    queue.notifyAll();//通知生产者和消费者
                }
            }
        }
    }

    /**
     * 消费者
     */
    static class Consumer {
        public void start() {
            while (run) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            System.out.println("队列为空，等待生产");
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        System.out.println("消费：" + queue.take());
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    queue.notifyAll();//通知生产者和消费者
                }
            }
        }
    }
}
