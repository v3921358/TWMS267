package SwordieX.client.character;

import Client.MapleCharacter;
import Client.MapleTraitType;
import Config.constants.JobConstants;
import SwordieX.client.character.avatar.AvatarLook;
import connection.OutPacket;
import SwordieX.util.FileTime;
import lombok.Getter;
import lombok.Setter;
import tools.data.MaplePacketLittleEndianWriter;

@Setter
@Getter
public class CharacterStat {
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
    private CharacterStat characterStat;
    private AvatarLook avatarLook;
    private AvatarLook secondAvatarLook;


    public CharacterStat(MapleCharacter chr) {
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
        encode(outPacket);
        mplew.write(outPacket.getData());
    }

    public void encode(OutPacket outPacket) {
        outPacket.encodeInt(getCharacterId());
        outPacket.encodeInt(getCharacterIdForLog());
        outPacket.encodeInt(getWorldIdForLog()); // change world id  v264->
        outPacket.encodeString(getName(), 15);

        outPacket.encodeByte(getGender());
        outPacket.encodeByte(0); //
        outPacket.encodeByte(getSkin());
        outPacket.encodeInt(0); // v267 + 填充
        outPacket.encodeInt(getFace());
        outPacket.encodeInt(getHair());
        outPacket.encodeInt(getLevel());
        outPacket.encodeShort(getJob());
        outPacket.encodeShort(getStr());
        outPacket.encodeShort(getDex());
        outPacket.encodeShort(getInt());
        outPacket.encodeShort(getLuk());
        outPacket.encodeInt(getHp());
        outPacket.encodeInt(getMaxHp());
        outPacket.encodeInt(getMp());
        outPacket.encodeInt(getMaxMp());

        outPacket.encodeShort(getAp());
        if (JobConstants.isSeparatedSpJob(getJob())) {
            getExtendSP().encode(outPacket);
        } else {
            outPacket.encodeShort(getSp());
        }
        outPacket.encodeLong(getExp());
        outPacket.encodeInt(getPop());
        outPacket.encodeInt(getWp());
        outPacket.encodeLong(getGachExp());
        outPacket.encodeArr("3B 37 4F 01 00 40 E0 FD");
        outPacket.encodeInt((int) getPosMap());
        outPacket.encodeByte(getPortal());
        //---------------------------------------
        outPacket.encodeShort(getSubJob());
        if (JobConstants.hasDecorate(getJob())) {
            outPacket.encodeInt(getDefFaceAcc());
        }
        outPacket.encodeByte(0);
        outPacket.encodeArr("00 40 E0 FD 3B 37 4F 01");

        outPacket.encodeInt(getCharismaExp());
        outPacket.encodeInt(getInsightExp());
        outPacket.encodeInt(getWillExp());
        outPacket.encodeInt(getCraftExp());
        outPacket.encodeInt(getSenseExp());
        outPacket.encodeInt(getCharmExp());
        getNonCombatStatDayLimit().encode(outPacket); // 2024/09/17 (int)
        outPacket.encodeInt(0);
        outPacket.encodeByte(10);
        outPacket.encodeInt(0);
        outPacket.encodeByte(6);
        outPacket.encodeByte(7);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeArr("45 4B DB 01 90 1E A2 C3"); // Time
        // function
        outPacket.encodeArr("00 80 05 BB 46 E6 17 02");
        outPacket.encodeArr("00 40 E0 FD 3B 37 4F 01");
        outPacket.encodeInt(getBurning().getStartLv());// 4
        outPacket.encodeInt(getBurning().getEndLv()); // 8
        outPacket.encodeInt(0); // 12
        outPacket.encodeByte(getBurning().getBurningType()); //13
        outPacket.encodeInt(0); // 17
        outPacket.encodeArr(new byte[25]);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
    }
}
