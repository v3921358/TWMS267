package Server.world.guild;

import Client.MapleCharacter;
import Packet.PacketHelper;
import lombok.Getter;
import lombok.Setter;
import tools.DateUtil;
import tools.data.MaplePacketLittleEndianWriter;

import java.io.Serializable;

@Getter
public class MapleGuildCharacter implements Serializable {

    public static final long serialVersionUID = 2058609046116597760L;
    @Setter
    private byte channel = -1;
    @Setter
    private byte guildRank;
    @Setter
    private byte allianceRank;
    @Setter
    private int level;
    private int id;
    @Setter
    private int jobId;
    @Setter
    private int guildId;
    @Setter
    private int guildContribution;
    @Setter
    @Getter
    private boolean online;
    private String name;

    public MapleGuildCharacter() {
    }

    /*
     * 從在線角色讀取信息
     */
    public MapleGuildCharacter(MapleCharacter chr) {
        name = chr.getName();
        level = chr.getLevel();
        id = chr.getId();
        if (chr.getClient() != null) {
            channel = (byte) chr.getClient().getChannel();
        }
        jobId = chr.getJob();
        guildRank = chr.getGuildRank();
        guildId = chr.getGuildId();
        guildContribution = chr.getGuildContribution();
        allianceRank = chr.getAllianceRank();
        online = true;
    }

    /*
     * 從數據庫中讀取公會成員信息時需要這個
     */
    public MapleGuildCharacter(int id, short lv, String name, byte channel, int job, byte rank, int guildContribution, byte allianceRank, int guildid, boolean on) {
        this.level = lv;
        this.id = id;
        this.name = name;
        if (on) {
            this.channel = channel;
        }
        this.jobId = job;
        this.online = on;
        this.guildRank = rank;
        this.allianceRank = allianceRank;
        this.guildContribution = guildContribution;
        this.guildId = guildid;
    }

    public void encodeData(MaplePacketLittleEndianWriter mplew) {
        mplew.writeInt(getId());
        mplew.writeMapleAsciiString(getName());
        mplew.writeInt(getJobId());
        mplew.writeInt(getLevel());
        mplew.writeInt(getGuildRank()); //should be always 5 but whatevs
        mplew.writeInt(getAllianceRank()); //? could be guild signature, but doesn't seem to matter 申請列表:3
        mplew.writeLong(PacketHelper.getTime(-2));
        mplew.write(isOnline()); //should always be 1 too 申請列表:0
        mplew.writeLong(PacketHelper.getTime(-2));
        mplew.writeInt(getGuildContribution()); //should always 3 申請列表:0
        mplew.writeInt(getGuildContribution()); //IGP
        mplew.writeLong(PacketHelper.getTime(-2));
        mplew.writeInt(Integer.parseInt(DateUtil.getCurrentDate().replaceAll("-", "")));
        mplew.writeLong(PacketHelper.getTime(-2));
    }
}
