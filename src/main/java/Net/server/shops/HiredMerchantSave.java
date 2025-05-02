/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.shops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Emy
 */
public class HiredMerchantSave {

    private static final Logger log = LoggerFactory.getLogger(HiredMerchantSave.class);
    public static final int NumSavingThreads = 5;
    private static final TimingThread[] Threads = new TimingThread[NumSavingThreads];
    private static final AtomicInteger Distribute = new AtomicInteger(0);

    static {
        for (int i = 0; i < Threads.length; i++) {
            Threads[i] = new TimingThread(new HiredMerchantSaveRunnable());
        }
    }

    public static void QueueShopForSave(HiredMerchant hm) {
        int Current = Distribute.getAndIncrement() % NumSavingThreads;
        Threads[Current].getRunnable().Queue(hm);
    }

    public static void Execute(Object ToNotify) {
        for (TimingThread Thread1 : Threads) {
            Thread1.getRunnable().SetToNotify(ToNotify);
        }
        for (TimingThread Thread : Threads) {
            Thread.start();
        }
    }

    private static class HiredMerchantSaveRunnable implements Runnable {

        private static final AtomicInteger RunningThreadID = new AtomicInteger(0);
        private final int ThreadID = RunningThreadID.incrementAndGet();
        private final ArrayBlockingQueue<HiredMerchant> Queue = new ArrayBlockingQueue<>(500); //500 Start Capacity (Should be plenty)
        private long TimeTaken = 0;
        private int ShopsSaved = 0;
        private volatile Object ToNotify;

        @Override
        public void run() {
            try {
                while (!Queue.isEmpty()) {
                    HiredMerchant next = Queue.take();
                    long Start = System.currentTimeMillis();
                    if (next.getMCOwner() != null && next.getMCOwner().getPlayerShop() == next) {
                        next.getMCOwner().setPlayerShop(null);
                    }
                    next.closeShop(true, false);
                    TimeTaken += (System.currentTimeMillis() - Start);
                    ShopsSaved++;
                }
                System.out.println("[保存僱傭商店數據 線程 " + ThreadID + "] 共保存: " + ShopsSaved + " | 耗時: " + TimeTaken + " 毫秒.");
                synchronized (ToNotify) {
                    ToNotify.notify();
                }
            } catch (InterruptedException ex) {
                log.error("保存僱傭商店數據出錯", ex);
            }
        }

        private void Queue(HiredMerchant hm) {
            Queue.add(hm);
        }

        private void SetToNotify(Object o) {
            if (ToNotify == null) {
                ToNotify = o;
            }
        }
    }

    private static class TimingThread extends Thread {

        private final HiredMerchantSaveRunnable ext;

        public TimingThread(HiredMerchantSaveRunnable r) {
            super(r);
            ext = r;
        }

        public HiredMerchantSaveRunnable getRunnable() {
            return ext;
        }
    }
}
