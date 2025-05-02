package Server.channel;

import Client.MapleCharacter;
import Client.MapleCharacterUtil;
import Config.configs.ServerConfig;
import Net.server.ShutdownServer;
import Net.server.Timer.PingTimer;
import Plugin.gui.GuiManager;
import Server.world.CharacterTransfer;
import Server.world.CheaterData;
import Server.world.WorldFindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerStorage {

    private static final Logger log = LoggerFactory.getLogger(PlayerStorage.class);
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock readLock = mutex.readLock(), writeLock = mutex.writeLock();
    private final ReentrantReadWriteLock mutex2 = new ReentrantReadWriteLock();
    private final Lock connectcheckReadLock = mutex2.readLock(), pendingWriteLock = mutex2.writeLock();
    private final Map<String, MapleCharacter> nameToChar = new LinkedHashMap<>();
    private final Map<Integer, MapleCharacter> idToChar = new LinkedHashMap<>();
    private final Map<Integer, CharacterTransfer> PendingCharacter = new HashMap<>();
    private final int channel;

    public PlayerStorage(int channel) {
        this.channel = channel;
        //PlayerTimer.getInstance().register(new UpdateCacheTask(), 10 * 1000);
        PingTimer.getInstance().register(new PersistingTask(), 60 * 1000);
        PingTimer.getInstance().register(new ConnectChecker(), 60 * 1000); //60秒檢測1次
    }

    public ArrayList<MapleCharacter> getAllCharacters() {
        readLock.lock();
        try {
            return new ArrayList<>(idToChar.values());
        } finally {
            readLock.unlock();
        }
    }

    /*
     * 註冊角色到伺服器上
     */
    public void registerPlayer(MapleCharacter chr) {
        writeLock.lock();
        try {
            nameToChar.put(chr.getName().toLowerCase(), chr);
            idToChar.put(chr.getId(), chr);
            if (ServerConfig.updatePlayerInGUI) {
                GuiManager.onlineStatusChanged(channel, getConnectedClients());
            }
        } finally {
            writeLock.unlock();
        }
        WorldFindService.getInstance().register(chr.getId(), chr.getName(), channel);
    }

    /*
     * 註冊臨時角色信息到伺服器上
     */
    public void registerPendingPlayer(CharacterTransfer chr, int playerId) {
        writeLock.lock();
        try {
            PendingCharacter.put(playerId, chr);
        } finally {
            writeLock.unlock();
        }
    }

    public final void deregisterPendingPlayerByAccountId(final int accountId) {
        writeLock.lock();
        try {
            deregisterPendingPlayerByAccountId_noLock(accountId);
        } finally {
            writeLock.unlock();
        }
    }

    public final void deregisterPendingPlayerByAccountId_noLock(final int accountId) {
        List<Integer> toRemoveIds = new LinkedList<>();
        final Collection<CharacterTransfer> chars = PendingCharacter.values();
        for (CharacterTransfer transfer : chars) {
            if (transfer.accountid == accountId) {
                toRemoveIds.add(transfer.characterid);
            }
        }
        for (int charid : toRemoveIds) {
            PendingCharacter.remove(charid);
        }
    }

    /*
     * 通過 chr
     * 註銷角色登記信息
     */
    public void deregisterPlayer(MapleCharacter chr) {
        WorldFindService.getInstance().forceDeregister(chr.getId(), removePlayer(chr.getId()));
    }

    /*
     * 通過 角色ID 和 角色名字
     * 註銷角色登記信息
     */
    public void deregisterPlayer(int idz) {
        WorldFindService.getInstance().forceDeregister(idz, removePlayer(idz));
    }

    /*
     * 通過 chr
     * 斷開角色登記信息
     */
    public void disconnectPlayer(MapleCharacter chr) {
        WorldFindService.getInstance().forceDeregisterEx(chr.getId(), removePlayer(chr.getId()));
    }

    private String removePlayer(int idz) {
        String namez = null;
        writeLock.lock();
        try {
            List<String> toRemoveNTC = new LinkedList<>();
            for (Map.Entry<String, MapleCharacter> entry : nameToChar.entrySet()) {
                if (entry.getValue() == null || entry.getValue().getId() == idz) {
                    toRemoveNTC.add(entry.getKey());
                    if (entry.getValue().getId() == idz) {
                        namez = entry.getKey();
                    }
                }
            }
            for (String name : toRemoveNTC) {
                nameToChar.remove(name);
            }
            MapleCharacter chr = idToChar.remove(idz);
            if (chr != null) {
                chr.saveOnlineTime();
            }
            GuiManager.onlineStatusChanged(channel, getConnectedClients());
        } finally {
            writeLock.unlock();
        }
        return namez;
    }

    public CharacterTransfer getPendingCharacter(int playerId) throws IOException {
        writeLock.lock();
        try {
            return PendingCharacter.remove(playerId);
        } finally {
            writeLock.unlock();
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        readLock.lock();
        try {
            return nameToChar.get(name.toLowerCase());
        } finally {
            readLock.unlock();
        }
    }

    public MapleCharacter getCharacterById(int id) {
        readLock.lock();
        try {
            return idToChar.get(id);
        } finally {
            readLock.unlock();
        }
    }

    public int getConnectedClients() {
        return idToChar.size();
    }

    public List<CheaterData> getCheaters() {
        List<CheaterData> cheaters = new ArrayList<>();
        readLock.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();
                if (chr.getCheatTracker().getPoints() > 0) {
                    cheaters.add(new CheaterData(chr.getCheatTracker().getPoints(), MapleCharacterUtil.makeMapleReadable(chr.getName()) + " ID: " + chr.getId() + " (" + chr.getCheatTracker().getPoints() + ") " + chr.getCheatTracker().getSummary()));
                }
            }
        } finally {
            readLock.unlock();
        }
        return cheaters;
    }

    public List<CheaterData> getReports() {
        List<CheaterData> cheaters = new ArrayList<>();
        readLock.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();
                if (chr.getReportPoints() > 0) {
                    cheaters.add(new CheaterData(chr.getReportPoints(), MapleCharacterUtil.makeMapleReadable(chr.getName()) + " ID: " + chr.getId() + " (" + chr.getReportPoints() + ") " + chr.getReportSummary()));
                }
            }
        } finally {
            readLock.unlock();
        }
        return cheaters;
    }

    /*
     * 斷開所有非GM角色連接
     */
    public void disconnectAll() {
        disconnectAll(false);
    }

    /*
     * 斷開所有角色連接
     */
    public void disconnectAll(boolean checkGM) {
        writeLock.lock();
        try {
            List<MapleCharacter> characters = new ArrayList<>(nameToChar.values());
            for (MapleCharacter chr : characters) {
                if (!chr.isGm() || !checkGM) {
                    chr.getClient().disconnect(false, false, true);
                    if (chr.getClient().getSession() != null && chr.getClient().getSession().isActive()) {
                        chr.getClient().getSession().close();
                    }
                    deregisterPlayer(chr);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /*
     * 獲取在線角色的名字
     */
    public String getOnlinePlayers(boolean byGM) {
        StringBuilder sb = new StringBuilder();
        if (byGM) {
            readLock.lock();
            try {
                for (MapleCharacter mapleCharacter : nameToChar.values()) {
                    sb.append(MapleCharacterUtil.makeMapleReadable(mapleCharacter.getName()));
                    sb.append(", ");
                }
            } finally {
                readLock.unlock();
            }
        } else {
            readLock.lock();
            try {
                Iterator<MapleCharacter> itr = nameToChar.values().iterator();
                MapleCharacter chr;
                while (itr.hasNext()) {
                    chr = itr.next();
                    if (!chr.isGm()) {
                        sb.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                        sb.append(", ");
                    }
                }
            } finally {
                readLock.unlock();
            }
        }
        return sb.toString();
    }

    /*
     * 發送給當前頻道在線玩家封包
     */
    public void broadcastPacket(byte[] data) {
        readLock.lock();
        try {
            for (MapleCharacter mapleCharacter : nameToChar.values()) {
                mapleCharacter.getClient().announce(data);
            }
        } finally {
            readLock.unlock();
        }
    }

    /*
     * 發送給當前頻道在線玩家喇叭的封包
     */
    public void broadcastSmegaPacket(byte[] data) {
        readLock.lock();
        try {
            Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();
                if (chr.getClient().isLoggedIn() && chr.getSmega()) {
                    chr.send(data);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /*
     * 發送給當前頻道在線GM的封包
     */
    public void broadcastGMPacket(byte[] data) {
        readLock.lock();
        try {
            Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();
                if (chr.getClient().isLoggedIn() && chr.isIntern()) {
                    chr.send(data);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public class PersistingTask implements Runnable {

        @Override
        public void run() {
            pendingWriteLock.lock();
            try {
                long currenttime = System.currentTimeMillis();
                // min
                PendingCharacter.entrySet().removeIf(next -> currenttime - next.getValue().TranferTime > 1000 * 60 * 30);
            } finally {
                pendingWriteLock.unlock();
            }
        }
    }

    private class ConnectChecker implements Runnable {

        @Override
        public void run() {
            connectcheckReadLock.lock();
            writeLock.lock();
            try {
                if (ShutdownServer.getInstance().isShutdown()) {
                    return;
                }
                Iterator<MapleCharacter> chrit = nameToChar.values().iterator();
                Map<Integer, MapleCharacter> disconnectList = new LinkedHashMap<>();
                MapleCharacter player;
                while (chrit.hasNext()) {
                    player = chrit.next();
                    if (player != null && (player.getClient() == null || player.getClient().getSession() == null || !player.getClient().getSession().isActive())) {
                        disconnectList.put(player.getId(), player);
                    }
                }
                Iterator<MapleCharacter> dcitr = disconnectList.values().iterator();
                while (dcitr.hasNext()) {
                    player = dcitr.next();
                    if (player != null) {
                        if (player.getClient() != null && player.getClient().getSession() != null) {
                            player.getClient().getSession().close();
//                            player.getClient().disconnect(false, false);
//                            player.getClient().updateLoginState(0);
                        }
                        disconnectPlayer(player);
                        dcitr.remove();
                    }
                }
            } finally {
                writeLock.unlock();
                connectcheckReadLock.unlock();
            }
        }
    }
}
