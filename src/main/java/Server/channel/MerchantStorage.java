package Server.channel;

import Net.server.shops.HiredMerchant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MerchantStorage {

    private static final Logger log = LoggerFactory.getLogger(ChannelServer.class);
    private final int channel;
    private final Map<Integer, HiredMerchant> merchants = new HashMap<>();
    private int running_MerchantID = 0;
    private ReentrantReadWriteLock merchLock = null; //僱傭商店多線程操作
    private ReentrantReadWriteLock.ReadLock mcReadLock = null;  // 讀鎖
    private ReentrantReadWriteLock.WriteLock mcWriteLock = null; // 寫鎖

    MerchantStorage(int channel) {
        this.channel = channel;
        // 創建公平的可重入讀寫鎖
        merchLock = new ReentrantReadWriteLock(true);
        mcReadLock = merchLock.readLock();
        mcWriteLock = merchLock.writeLock();
    }

    public void closeAllMerchants() {
        int ret = 0;
        long Start = System.currentTimeMillis();
        mcWriteLock.lock();
        try {
            Iterator<Map.Entry<Integer, HiredMerchant>> hmit = merchants.entrySet().iterator();
            while (hmit.hasNext()) {
                hmit.next().getValue().closeShop(true, false);
                hmit.remove();
                ret++;
            }
        } catch (Exception e) {
            log.error("關閉僱傭商店出現錯誤..." + e);
        } finally {
            mcWriteLock.unlock();
        }
        log.info("頻道 " + channel + " 共保存僱傭商店: " + ret + " | 耗時: " + (System.currentTimeMillis() - Start) + " 毫秒.");
    }

    public int addMerchant(HiredMerchant hMerchant) {
        mcWriteLock.lock();
        try {
            running_MerchantID++;
            merchants.put(running_MerchantID, hMerchant);
            return running_MerchantID;
        } finally {
            mcWriteLock.unlock();
        }
    }

    public void removeMerchant(HiredMerchant hMerchant) {
        mcWriteLock.lock();
        try {
            merchants.remove(hMerchant.getStoreId());
        } finally {
            mcWriteLock.unlock();
        }
    }

    public boolean containsMerchant(int accId) {
        boolean contains = false;
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if (hm.getOwnerAccId() == accId) {
                    contains = true;
                    break;
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return contains;
    }

    public boolean containsMerchant(int accId, int chrId) {
        boolean contains = false;
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if (hm.getOwnerAccId() == accId && hm.getOwnerId() == chrId) {
                    contains = true;
                    break;
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return contains;
    }

    public List<HiredMerchant> searchMerchant(int itemSearch) {
        List<HiredMerchant> list = new LinkedList<>();
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return list;
    }

    public HiredMerchant getHiredMerchants(int accId, int chrId) {
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if (hm.getOwnerAccId() == accId && hm.getOwnerId() == chrId) {
                    return hm;
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return null;
    }
}
