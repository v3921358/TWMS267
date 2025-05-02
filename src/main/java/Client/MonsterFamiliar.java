package Client;

import Client.inventory.FamiliarCard;
import Config.configs.ServerConfig;
import Net.server.MapleItemInformationProvider;
import Net.server.StructItemOption;
import Net.server.maps.AnimatedMapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.movement.LifeMovement;
import Net.server.movement.LifeMovementFragment;
import connection.OutPacket;
import connection.packet.OverseasPacket;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import tools.Randomizer;

import java.awt.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class MonsterFamiliar extends AnimatedMapleMapObject implements Serializable {

    private static final long serialVersionUID = 795419937713738569L;
    private final int id;
    private final int familiar;
    private final int accountid;
    private final int characterid;
    private int exp;
    private String name;
    private short fh = 0;
    private byte grade, level;
    private int skill, option1, option2, option3;
    private double pad;
    private byte flag = 0x8;
    private boolean summoned = false;
    private boolean lock = false;

    public MonsterFamiliar(int id, int familiar, int accountid, int characterid, String name, byte grade, byte level, int exp, int skill, int option1, int option2, int option3, boolean summoned, boolean lock) {
        this.id = id;
        this.familiar = familiar;
        this.accountid = accountid;
        this.characterid = characterid;
        this.name = name;
        this.grade = (byte) Math.min(Math.max(0, grade), 4);
        this.level = (byte) Math.min(Math.max(1, level), 5);
        this.exp = exp;
        this.skill = skill;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        setStance(0);
        setPosition(new Point(0, 0));
        this.summoned = summoned;
        setLock(lock);
    }

    public MonsterFamiliar(int accountid, int characterid, int familiar, FamiliarCard mf) {
        this.id = Randomizer.nextInt();
        this.accountid = accountid;
        this.characterid = characterid;
        this.familiar = familiar;
        this.name = "";
        this.grade = mf.getGrade();
        this.level = mf.getLevel();
        this.skill = mf.getSkill() > 0 ? mf.getSkill() : Randomizer.rand(800, 904) + 1;
        this.pad = MapleItemInformationProvider.getInstance().getFamiliarTable_pad().get(getGrade()).get(level - 1);
        if (mf.getOption1() > 0 || mf.getOption2() > 0 || mf.getOption3() > 0) {
            this.option1 = mf.getOption1();
            this.option2 = mf.getOption2();
            this.option3 = mf.getOption3();
        } else {
            initOptions();
        }
    }

    public void initPad() {
        this.pad = MapleItemInformationProvider.getInstance().getFamiliarTable_pad().get(getGrade()).get(level - 1);
    }

    public int getOption(int i) {
        switch (i) {
            case 0: {
                return option1;
            }
            case 1: {
                return option2;
            }
            case 2: {
                return option3;
            }
        }
        return 0;
    }


    public int setOption(final int i, final int option) {
        switch (i) {
            case 0: {
                option1 = option;
            }
            case 1: {
                option2 = option;
            }
            case 2: {
                option3 = option;
            }
        }
        return 0;
    }


    public void initOptions() {
        LinkedList<List<StructItemOption>> options = new LinkedList<>(MapleItemInformationProvider.getInstance().getFamiliar_option().values());
        byte incDAMrCount = 0;
        for (int i = 0; i < 3; ++i) {
            Collections.shuffle(options);
            for (List<StructItemOption> optionList : options) {
                if (optionList.size() < level) {
                    continue;
                }
                final StructItemOption option = optionList.get(level - 1);
                if (option.opID / 10000 != grade) {
                    continue;
                }
                if (ServerConfig.familiarIncDAMrHard && option.opString.contains("最終傷害")) {
                    if (!Randomizer.isSuccess(40 - 19 * incDAMrCount)) {
                        continue;
                    }
                    incDAMrCount++;
                }
                setOption(i, option.opID);
            }
        }
    }

    public void gainExp(int exp) {
        this.exp += exp;
        while (this.exp >= 100) {
            ++this.level;
            this.exp -= 100;
        }
        if (this.level >= 5) {
            this.level = 5;
            this.exp = 0;
        }
    }

    public void updateGrade() {
        ++this.grade;
        this.level = 1;
        if (this.grade >= 4) {
            this.grade = 4;
        }
    }

    public double getPad() {
        return pad;
    }

    public int getCharacterid() {
        return characterid;
    }

    public void setFh(short fh) {
        this.fh = fh;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(byte grade) {
        this.grade = grade;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public void setOption1(int option1) {
        this.option1 = option1;
    }

    public void setOption2(int option2) {
        this.option2 = option2;
    }

    public void setOption3(int option3) {
        this.option3 = option3;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(short skill) {
        this.skill = skill;
    }

    public int getOption1() {
        return option1;
    }

    public void setOption1(short option1) {
        this.option1 = option1;
    }

    public int getOption2() {
        return option2;
    }

    public void setOption2(short option2) {
        this.option2 = option2;
    }

    public int getOption3() {
        return option3;
    }

    public void setOption3(short option3) {
        this.option3 = option3;
    }

    public int getFamiliar() {
        return familiar;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public short getFh() {
        return fh;
    }

    public void setFh(int f) {
        fh = ((short) f);
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (client.getPlayer() == null || client.getPlayer().getId() != characterid) {
            client.write(OverseasPacket.extraEquipResult(ExtraEquipResult.spawnFamiliar(accountid, characterid, false, this, getPosition(), false)));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        if (client.getPlayer() == null || client.getPlayer().getId() != characterid) {
            client.write(OverseasPacket.extraEquipResult(ExtraEquipResult.removeFamiliar(characterid, false)));
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.FAMILIAR;
    }

    public void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if ((move instanceof LifeMovement)) { // && ((move instanceof StaticLifeMovement))) {
                setStance(((LifeMovement) move).getMoveAction()); // setFh(((StaticLifeMovement) move).getUnk());
            }
        }
    }

    public FamiliarCard createFamiliarCard() {
        return new FamiliarCard((short) skill, level, grade, option1, option2, option3);
    }

    public boolean isSummoned() {
        return summoned;
    }

    public void setSummoned(boolean summoned) {
        this.summoned = summoned;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = (byte) flag;
    }

    public boolean hasFlag(int flag) {
        return (this.flag | (byte) flag) != 0;
    }

    public void addFlag(int flag) {
        this.flag |= (byte) flag;
    }

    public void removeFlag(int flag) {
        this.flag &= (byte) ~flag;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
        if (lock) flag |= 0x10;
        else flag &= ~0x10;
    }

    public void encode(OutPacket outPacket) {
        outPacket.encodeLong(id);
        outPacket.encodeInt(2);
        outPacket.encodeInt(familiar);
        outPacket.encodeString(this.name, 15);
        outPacket.encodeByte(0);
        outPacket.encodeShort(level);
        outPacket.encodeShort(skill);
        outPacket.encodeShort(exp);
        outPacket.encodeShort(0);
        outPacket.encodeShort(level);
        for (int i = 0; i < 3; ++i) {
            outPacket.encodeShort(getOption(i));
        }
        outPacket.encodeByte(flag);
        outPacket.encodeByte(grade);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeInt(0);
    }
}
