package Packet;


import Client.MapleCharacter;
import Client.MapleTraitType;
import SwordieX.client.character.Burning;
import SwordieX.client.character.ExtendSP;
import SwordieX.client.character.NonCombatStatDayLimit;
import connection.OutPacket;
import SwordieX.util.FileTime;
import lombok.Getter;
import lombok.Setter;
import tools.data.MaplePacketLittleEndianWriter;

@Setter
@Getter
public class SelectCharInfoData {
    private int characterId;
    private int characterIdForLog;
    private int worldIdForLog;
    private String name;
    private int gender;
    private int skin;
    private int face;
    private int hair;
    private int level;
    private int job;
    private int str;
    private int dex;
    private int int_;
    private int luk;
    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int ap;
    private ExtendSP extendSP;
    private int sp;
    private long exp;
    private int pop;
    private int wp;
    private int gachExp;
    private long posMap;
    private int portal;
    private int subJob;
    private int defFaceAcc;
    private int fatigue;
    private int lastFatigueUpdateTime;
    private int charismaExp;
    private int insightExp;
    private int willExp;
    private int craftExp;
    private int senseExp;
    private int charmExp;
    private NonCombatStatDayLimit nonCombatStatDayLimit;
    private int pvpExp;
    private int pvpGrade;
    private int pvpPoint;
    private int pvpModeLevel;
    private int pvpModeType;
    private int eventPoint;
    private Burning burning;

    public SelectCharInfoData(MapleCharacter chr) {
        characterId = chr.getId();
        characterIdForLog = chr.getId();
        worldIdForLog = chr.getWorld();
        name = chr.getName();
        gender = chr.getGender();
        skin = chr.getSkinColor();
        face = chr.getFace();
        hair = chr.getHair();
        level = chr.getLevel();
        job = chr.getJob();
        str = chr.getStat().str;
        dex = chr.getStat().dex;
        int_ = chr.getStat().int_;
        luk = chr.getStat().luk;
        hp = chr.getStat().hp;
        maxHp = chr.getStat().getMaxHp();
        mp = chr.getStat().mp;
        maxMp = chr.getStat().getMaxMp();
        ap = chr.getRemainingAp();
        extendSP = new ExtendSP(chr);
        sp = chr.getRemainingSp();
        exp = chr.getExp();
        pop = chr.getFame();
        wp = chr.getWeaponPoint();
        gachExp = chr.getGachExp();
        posMap = chr.getMapId();
        portal = chr.getInitialSpawnpoint();
        subJob = chr.getSubcategory();
        defFaceAcc = chr.getDecorate();
        fatigue = chr.getFatigue();
        lastFatigueUpdateTime = FileTime.currentTime().toYYMMDDHHintValue();
        charismaExp = chr.getTrait(MapleTraitType.charisma).getTotalExp();
        insightExp = chr.getTrait(MapleTraitType.insight).getTotalExp();
        willExp = chr.getTrait(MapleTraitType.will).getTotalExp();
        craftExp = chr.getTrait(MapleTraitType.craft).getTotalExp();
        senseExp = chr.getTrait(MapleTraitType.sense).getTotalExp();
        charmExp = chr.getTrait(MapleTraitType.charm).getTotalExp();
        nonCombatStatDayLimit = new NonCombatStatDayLimit();
        pvpExp = chr.getStat().pvpExp;
        pvpGrade = chr.getStat().pvpRank;
        pvpPoint = chr.getBattlePoints();
        pvpModeLevel = 6;
        pvpModeType = 7;
        eventPoint = 0;
        burning = new Burning(chr.getBurningChrType(),
                chr.getBurningChrType() > Burning.無 ? 10 : 0,
                chr.getBurningChrType() == Burning.燃燒加速器 ? 130 : chr.getBurningChrType() == Burning.超級燃燒 ? 150 : chr.getBurningChrType() == Burning.極限燃燒 ? 200 : 0,
                chr.getBurningChrTime() > 0 ? FileTime.currentTime() : FileTime.fromType(FileTime.Type.PLAIN_ZERO),
                chr.getBurningChrTime() > 0 ? FileTime.fromLong(chr.getBurningChrTime()) : FileTime.fromType(FileTime.Type.ZERO_TIME)
        );
    }

    public int getInt() {
        return int_;
    }

    public void setInt(int inte) {
        this.int_ = inte;
    }

    public long getPosMap() {
        return posMap == 0 ? 931000000 : posMap;
    }

    public void encode(MaplePacketLittleEndianWriter mplew) {
        OutPacket outPacket = new OutPacket();
        mplew.writeInt(characterId);
        mplew.writeInt(characterId);
        mplew.writeInt(worldIdForLog);
        mplew.writeAsciiString(name, 15);
        mplew.write(gender); // 性別
        mplew.write(skin); // chr.getSkinColor
        mplew.write(0); //UNK
        mplew.writeInt(face);
        mplew.writeInt(hair);
        mplew.writeInt(level);
        mplew.writeShort(job); // chr.getJob();
        mplew.writeShort(str); // 力量
        mplew.writeShort(dex); // 敏捷
        mplew.writeShort(int_); // 智力
        mplew.writeShort(luk); // 幸運
        mplew.writeInt(maxHp);
        mplew.writeInt(hp);
        mplew.writeInt(maxMp);
        mplew.writeInt(mp);
        mplew.writeShort(ap);
        mplew.write(0);
        mplew.writeLong(exp);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeInt(posMap);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeLong(PacketHelper.getTime(-2L)); // tms封包參照 94354848000000000L
        mplew.writeZeroBytes(8);
        mplew.write(8);
        mplew.writeZeroBytes(31);
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeLong(PacketHelper.getTime(-2L)); // tms封包參照 94354848000000000L
        mplew.writeInt(20240630); // 本日日期 - yyyy-XX-XX
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeZeroBytes(27);
        mplew.writeLong(10483544864L);
        mplew.writeInt(10000);
        mplew.writeInt(0);
        mplew.writeLong(PacketHelper.getTime(-2L)); // tms封包參照 94354848000000000L
        mplew.writeZeroBytes(49);
        mplew.writeLong(PacketHelper.getTime(-2L)); // tms封包參照 94354848000000000L
        mplew.writeInt(20240629); // 本日日期
        mplew.writeZeroBytes(18);
        mplew.writeLong(2393504880238534400L);
        mplew.write(50);
        mplew.writeLong(150842304000000000L);
        mplew.writeLong(PacketHelper.getTime(-2L)); // tms封包參照 94354848000000000L
        mplew.writeZeroBytes(67);
        mplew.write(outPacket.getData());
    }
}

