/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.world;

import Client.MapleCharacter;
import Packet.GuildPacket;
import Server.auction.AuctionServer;
import Server.cashshop.CashShopServer;
import Server.channel.ChannelServer;
import Server.channel.PlayerStorage;
import Server.world.guild.MapleBBSThread;
import Server.world.guild.MapleGuild;
import Server.world.guild.MapleGuildCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author PlayDK
 */
public class WorldGuildService {

    private static final Logger log = LoggerFactory.getLogger(WorldGuildService.class);
    private final Map<Integer, MapleGuild> guildList;
    private final ReentrantReadWriteLock lock;
    private final Map<String, Integer> tempCreatingName;

    private WorldGuildService() {
        //log.info("正在啟動[WorldGuildService]");
        lock = new ReentrantReadWriteLock();
        guildList = new LinkedHashMap<>();
        tempCreatingName = new LinkedHashMap<>();
    }

    public static WorldGuildService getInstance() {
        return SingletonHolder.instance;
    }

    public void addLoadedGuild(MapleGuild guild) {
        if (guild.isProper()) {
            guildList.put(guild.getId(), guild);
        }
    }

    /*
     * 創建1個新的公會
     */
    public int createGuild(int leaderId, String name) {
        return MapleGuild.createGuild(leaderId, name);
    }

    public MapleGuild getGuild(int guildId) {
        MapleGuild ret = null;
        lock.readLock().lock();
        try {
            ret = guildList.get(guildId);
        } finally {
            lock.readLock().unlock();
        }
        if (ret == null) {
            lock.writeLock().lock();
            try {
                ret = new MapleGuild(guildId);
                if (ret == null || ret.getId() <= 0 || !ret.isProper()) { //failed to load
                    return null;
                }
                guildList.put(guildId, ret);
            } finally {
                lock.writeLock().unlock();
            }
        }
        return ret; //Guild doesn't exist?
    }

    /**
     * 檢查是否有正在建立的公會名稱
     *
     * @param name 名稱
     * @return 是或否
     */
    public boolean existCreatingGuildName(String name) {
        return tempCreatingName.containsKey(name);
    }

    public String getCreatingGuildName(int cid) {
        for (Entry<String, Integer> tcn : tempCreatingName.entrySet()) {
            if (tcn.getValue() == cid) {
                return tcn.getKey();
            }
        }
        return "";
    }

    public void addCreatingGuildName(String name, int cid) {
        tempCreatingName.put(name, cid);
    }

    public void removeCreatingGuildName(String name) {
        tempCreatingName.remove(name);
    }

    public MapleGuild getGuildByName(String guildName) {
        lock.readLock().lock();
        try {
            for (MapleGuild guild : guildList.values()) {
                if (guild.getName().equalsIgnoreCase(guildName)) {
                    return guild;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public MapleGuild getGuild(MapleCharacter chr) {
        return getGuild(chr.getGuildId());
    }

    /*
     * 更新公會成員在線信息
     */
    public void setGuildMemberOnline(MapleGuildCharacter guildMember, boolean isOnline, int channel) {
        if (guildMember != null) {
            MapleGuild guild = getGuild(guildMember.getGuildId());
            if (guild != null) {
                guild.setOnline(guildMember.getId(), isOnline, channel);
            }
        }
    }

    /*
     * 給公會所有成員發送封包信息
     */
    public void guildPacket(int guildId, byte[] message) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null && message != null) {
            guild.broadcast(message);
        }
    }

    /*
     * 添加新的公會成員
     */
    public int addGuildMember(MapleGuildCharacter guildMember) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            return guild.addGuildMember(guildMember);
        }
        return 0;
    }

    public int addGuildMember(int guildId, int chrId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.addGuildMember(chrId);
        }
        return 0;
    }

    /*
     * 添加新的公會申請列表
     */
    public int addGuildApplyMember(MapleGuildCharacter guildMember, String info) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            return guild.addGuildApplyMember(guildMember, info);
        }
        return 0;
    }

    /*
     * 拒絕角色的公會申請
     */
    public int denyGuildApplyMember(int guildId, int chrId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            //刪除公會申請信息
            guild.denyGuildApplyMember(chrId);
            int ch = WorldFindService.getInstance().findChannel(chrId);
            if (ch < 0) {
                return 0;
            }
            MapleCharacter player = getStorage(ch).getCharacterById(chrId);
            if (player == null) {
                return 0;
            }
            player.getClient().announce(GuildPacket.DenyGuildApply(chrId, guildId));
            return 1;
        }
        return 0;
    }

    /*
     * 玩家自己離開公會
     */
    public void leaveGuild(MapleGuildCharacter guildMember) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            guild.leaveGuild(guildMember);
        }
    }

    /*
     * 公會職位稱號變更
     */
    public void changeRank(int guildId, int chrId, int newRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.changeRank(chrId, newRank);
        }
    }

    /*
     * 驅逐成員
     */
    public void expelMember(MapleGuildCharacter initiator, String name, int chrId) {
        MapleGuild guild = getGuild(initiator.getGuildId());
        if (guild != null) {
            guild.expelMember(initiator, name, chrId);
        }
    }

    /*
     * 修改公會公告
     */
    public void setGuildNotice(int guildId, int cid, String notice) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.setGuildNotice(cid, notice);
        }
    }

    /*
     * 修改公會會長
     */
    public void setGuildLeader(int guildId, int chrId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.changeGuildLeader(chrId);
        }
    }

    /*
     * 獲取公會指定技能的等級
     */
    public int getSkillLevel(int guildId, int skillId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.getSkillLevel(skillId);
        }
        return 0;
    }

    /*
     * 購買公會技能
     */
    public boolean purchaseSkill(int guildId, int skillId, String name, int chrId) {
        MapleGuild guild = getGuild(guildId);
        return guild != null && guild.purchaseSkill(skillId, name, chrId);
    }

    /*
     * 激活公會技能
     */
    public boolean activateSkill(int guildId, int skillId, int changerID, String name) {
        MapleGuild guild = getGuild(guildId);
        return guild != null && guild.activateSkill(skillId, changerID, name);
    }

    /*
     * 更新公會成員 升級 或者 改變職業
     */
    public void memberLevelJobUpdate(MapleGuildCharacter guildMember) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            guild.memberLevelJobUpdate(guildMember);
        }
    }

    /*
     * 公會成員職位變更
     */
    public void changeGradeNameAndAuthority(int guildId, int changerID, byte gradeIndex, String name, int authority) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.changeGradeNameAndAuthority(changerID, gradeIndex, name, authority);
        }
    }

    /*
     * 公會頭像變更
     */
    public void setGuildEmblem(int guildId, int changerID, short bg, byte bgcolor, short logo, byte logocolor) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.setGuildEmblem(changerID, bg, bgcolor, logo, logocolor);
        }
    }

    public void setGuildEmblem(int guildId, int changerID, byte[] imageMark) {
        if (imageMark == null || imageMark.length <= 0 || imageMark.length > 60000) {
            return;
        }
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.setGuildEmblem(changerID, imageMark);
        }
    }

    /*
     * 解散公會
     */
    public void disbandGuild(int guildId) {
        MapleGuild guild = getGuild(guildId);
        lock.writeLock().lock();
        try {
            if (guild != null) {
                guild.disbandGuild();
                guildList.remove(guildId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /*
     * 刪除公會成員
     */
    public void deleteGuildCharacter(int guildId, int charId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            MapleGuildCharacter mc = guild.getMGC(charId);
            if (mc != null) {
                if (mc.getGuildRank() > 1) { //not leader
                    guild.leaveGuild(mc);
                } else {
                    guild.disbandGuild();
                }
            }
        }
    }

    /*
     * 增加公會成員上限數量
     */
    public boolean increaseGuildCapacity(int guildId, boolean b) {
        MapleGuild guild = getGuild(guildId);
        return guild != null && guild.increaseCapacity(b);
    }

    /*
     * 增加公會的貢獻度
     */
    public void gainGP(int guildId, int amount) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.gainGP(amount);
        }
    }

    public void gainGP(int guildId, int amount, int chrId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.gainGP(amount, false, chrId);
        }
    }

    public int getGP(int guildId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.getGP();
        }
        return 0;
    }

    public int getInvitedId(int guildId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.getInvitedId();
        }
        return 0;
    }

    public void setInvitedId(int guildId, int inviteId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.setInvitedId(inviteId);
        }
    }

    public int getGuildLeader(int guildName) {
        MapleGuild guild = getGuild(guildName);
        if (guild != null) {
            return guild.getLeaderId();
        }
        return 0;
    }

    public int getGuildLeader(String guildName) {
        MapleGuild guild = getGuildByName(guildName);
        if (guild != null) {
            return guild.getLeaderId();
        }
        return 0;
    }

    public void save() {
        System.out.println("正在保存公會數據...");
        lock.writeLock().lock();
        try {
            for (MapleGuild guild : guildList.values()) {
                guild.writeToDB(false);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /*
     * 獲取公會BSS的信息
     */
    public List<MapleBBSThread> getBBS(int guildId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.getBBS();
        }
        return null;
    }

    /*
     * 添加1個公會BBS信息
     */
    public int addBBSThread(int guildId, String title, String text, int icon, boolean bNotice, int posterId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.addBBSThread(title, text, icon, bNotice, posterId);
        }
        return -1;
    }

    /*
     * 編輯公會BSS信息
     */
    public void editBBSThread(int guildId, int localthreadId, String title, String text, int icon, int posterId, int guildRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.editBBSThread(localthreadId, title, text, icon, posterId, guildRank);
        }
    }

    /*
     * 刪除公會BBS信息
     */
    public void deleteBBSThread(int guildId, int localthreadId, int posterId, int guildRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.deleteBBSThread(localthreadId, posterId, guildRank);
        }
    }

    /*
     * 添加公會BBS信息的回復 也就是留言
     */
    public void addBBSReply(int guildId, int localthreadId, String text, int posterId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.addBBSReply(localthreadId, text, posterId);
        }
    }

    /*
     * 刪除公會BBS信息的回復
     */
    public void deleteBBSReply(int guildId, int localthreadId, int replyId, int posterId, int guildRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.deleteBBSReply(localthreadId, replyId, posterId, guildRank);
        }
    }

    /*
     * 修改公會圖標
     */
    public void changeEmblem(int guildId, int changerID, int affectedPlayers, MapleGuild guild) {
        byte[] packet;
        if (guild.getImageLogo() != null && guild.getImageLogo().length > 0 && guild.getImageLogo().length <= 60000) {
            packet = GuildPacket.guildEmblemChange(guildId, changerID, (short) guild.getLogoBG(), (byte) guild.getLogoBGColor(), (short) guild.getLogo(), (byte) guild.getLogoColor());
        } else {
            packet = GuildPacket.guildEmblemChange(guildId, changerID, (short) guild.getLogoBG(), (byte) guild.getLogoBGColor(), (short) guild.getLogo(), (byte) guild.getLogoColor(), guild.getImageLogo());
        }
        WorldBroadcastService.getInstance().sendGuildPacket(affectedPlayers, packet, -1, guildId, false);
        setGuildAndRank(affectedPlayers, -1, -1, -1, -1);
    }

    public void setGuildAndRank(int chrId, int guildId, int rank, int contribution, int alliancerank) {
        int ch = WorldFindService.getInstance().findChannel(chrId);
        if (ch == -1) {
            return;
        }
        MapleCharacter player = getStorage(ch).getCharacterById(chrId);
        if (player == null) {
            return;
        }
        boolean isDifferentGuild;
        if (guildId == -1 && rank == -1) { //just need a respawn
            isDifferentGuild = true;
        } else {
            isDifferentGuild = guildId != player.getGuildId();
            player.setGuildId(guildId);
            player.setGuildRank((byte) rank);
            player.setGuildContribution(contribution);
            player.setAllianceRank((byte) alliancerank);
            player.saveGuildStatus();
        }
        if (isDifferentGuild && ch > 0) {
            player.getMap().broadcastMessage(player, GuildPacket.loadGuildName(player), false);
            player.getMap().broadcastMessage(player, GuildPacket.loadGuildIcon(player), false);
        }
    }

    public PlayerStorage getStorage(int channel) {
        if (channel == -20) {
            return AuctionServer.getInstance().getPlayerStorage();
        } else if (channel == -10) {
            return CashShopServer.getPlayerStorage();
        }
        return ChannelServer.getInstance(channel).getPlayerStorage();
    }

    public List<Pair<Integer, MapleGuild>> getGuildList() {
        List<Pair<Integer, MapleGuild>> gui = new ArrayList<>();
        for (Entry<Integer, MapleGuild> g : guildList.entrySet()) {
            gui.add(new Pair<>(g.getKey(), g.getValue()));
        }
        return gui;
    }

    private static class SingletonHolder {

        protected static final WorldGuildService instance = new WorldGuildService();
    }
}
