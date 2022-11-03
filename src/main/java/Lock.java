import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;

public class Lock {

    private static int NUMBER = 20;

    private static void getNumber() {
        System.out.println("thread: " + Thread.currentThread().getId() + ", number:" + NUMBER);
        NUMBER -= 1;
    }

    public static void main(String[] args) throws InterruptedException {

        ExponentialBackoffRetry exponentialBackoffRetry = new ExponentialBackoffRetry(1000, 10);
        CuratorFramework cf = CuratorFrameworkFactory.builder()
                .connectString("bigdata01:2181")
                .retryPolicy(exponentialBackoffRetry)
                .build();
        cf.start();

        InterProcessMutex lock = new InterProcessMutex(cf, "/mylock");

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        lock.acquire();
                        getNumber();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threads.add(thread);
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }
    }
}
