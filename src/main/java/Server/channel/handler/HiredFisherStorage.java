package Server.channel.handler;

import Net.server.shops.HiredFisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 免責聲明：本模擬器源代碼下載自ragezone.com，僅用於技術研究學習，無任何商業行為。
 */
public final class HiredFisherStorage {

    private final Logger log = LoggerFactory.getLogger(HiredFisherStorage.class);
    private int running_FisherID = 0;
    private final ReentrantReadWriteLock fisherLock = new ReentrantReadWriteLock(true); //僱傭商店多線程操作
    private final ReentrantReadWriteLock.ReadLock fReadLock = fisherLock.readLock();  // 讀鎖
    private final ReentrantReadWriteLock.WriteLock fWriteLock = fisherLock.writeLock(); // 寫鎖
    private final HashMap<Integer, HiredFisher> hiredFishers = new HashMap<>();
    private final int channel;

    public HiredFisherStorage(final int channel) {
        this.channel = channel;
    }

    public void saveAllFisher() {
        fWriteLock.lock();
        try {
            for (Map.Entry<Integer, HiredFisher> it : hiredFishers.entrySet()) {
                it.getValue().saveItems();
            }
        } finally {
            fWriteLock.unlock();
        }
    }

    public void closeAllFisher() {
        int ret = 0;
        final long start = System.currentTimeMillis();
        fWriteLock.lock();
        try {
            for (Map.Entry<Integer, HiredFisher> it : hiredFishers.entrySet()) {
                it.getValue().closeShop(true, false);
                ret++;
            }
        } catch (Exception e) {
            log.error("關閉僱傭釣手出現錯誤...", e);
        } finally {
            fWriteLock.unlock();
        }
        System.out.println(("頻道 " + this.channel + " 共保存僱傭釣手: " + ret + " | 耗時: " + (System.currentTimeMillis() - start) + " 毫秒."));
    }


    public boolean containsFisher(final int accId, final int chrId) {
        boolean contains = false;
        fReadLock.lock();
        try {
            for (HiredFisher hf : hiredFishers.values()) {
                if (hf.getOwnerAccId() == accId && hf.getOwnerId() == chrId) {
                    contains = true;
                    break;
                }
            }
        } finally {
            fReadLock.unlock();
        }
        return contains;
    }

    public HiredFisher getHiredFisher(final int accId, final int chrId) {
        fReadLock.lock();
        try {
            for (HiredFisher it : hiredFishers.values()) {
                if (it.getOwnerAccId() == accId && it.getOwnerId() == chrId) {
                    return it;
                }
            }
        } finally {
            fReadLock.unlock();
        }
        return null;
    }

    public int addFisher(HiredFisher hiredFisher) {
        fWriteLock.lock();
        try {
            running_FisherID++;
            hiredFishers.put(running_FisherID, hiredFisher);
            return this.running_FisherID;
        } finally {
            fWriteLock.unlock();
        }
    }

    public void removeFisher(HiredFisher hFisher) {
        fWriteLock.lock();
        try {
            hiredFishers.remove(hFisher.getId());
        } finally {
            fWriteLock.unlock();
        }
    }

    public int getChannel() {
        return channel;
    }
}