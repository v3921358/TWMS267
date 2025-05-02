package Client.inventory;

import Client.MapleCharacter;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Net.auth.Auth;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.StructItemOption;
import Packet.EffectPacket;
import Packet.InventoryPacket;
import Packet.MaplePacketCreator;
import Server.world.WorldBroadcastService;
import connection.OutPacket;
import connection.packet.OverseasPacket;
import SwordieX.util.FileTime;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;

import java.io.Serializable;
import java.util.*;

public class Equip extends Item implements Serializable {

    public static final long ARMOR_RATIO = 350000;
    public static final long WEAPON_RATIO = 700000;
    private static final long serialVersionUID = -4385634094556865314L;
    //charm: -1 = has not been initialized yet, 0 = already been worn, >0 = has teh charm exp
    private byte restUpgradeCount = 0, currentUpgradeCount = 0, vicioushammer = 0, platinumhammer = 0, state = 0, addState;
    private short enchantBuff = 0, reqLevel = -1, yggdrasilWisdom = 0, bossDamage = 0, ignorePDR = 0, totalDamage = 0, allStat = 0, karmaCount = -1; //新增的裝備屬性
    private boolean finalStrike = false;  //新增的裝備屬性
    private short str = 0, dex = 0, _int = 0, luk = 0, hp = 0, mp = 0, pad = 0, mad = 0, pdd = 0, mdd = 0, acc = 0, avoid = 0, hands = 0, speed = 0, jump = 0, charmExp = 0, pvpDamage = 0;
    private int durability = -1, incSkill = -1;
    private int potential1 = 0, potential2 = 0, potential3 = 0, potential4 = 0, potential5 = 0, potential6 = 0;
    private int socket1 = -1, socket2 = -1, socket3 = -1; //V.102新增 裝備插槽
    private int itemSkin = 0; //裝備皮膚 也是裝備外觀改變 以後會用到暫時寫在這
    private MapleRing ring = null;
    private MapleAndroid android = null;
    // 潛能鎖
    private int lockSlot = 0;
    private short lockId = 0;
    private byte sealedLevel = 0;
    private long sealedExp = 0, itemEXP = 0;
    private short soulOptionID, soulSocketID, soulOption;
    private int soulSkill = 0;
    private Map<EquipStats, Long> statsTest = new LinkedHashMap<>();
    private int iIncReq;
    private NirvanaFlame nirvanaFlame = new NirvanaFlame();
    private StarForce starForce = new StarForce();
    private int failCount = 0;
    private int ARCExp = 1;
    private short ARC, ARCLevel = 1;
    private int autExp = 1;
    private short aut, autLevel = 1;

    private boolean mvpEquip = false;

    public Equip(int id, short position, int sn, int flag, short espos) {
        super(id, position, (short) 1, flag, sn, espos);
    }

    @Override
    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getSN(), getAttribute(), getESPos());
        ret.mvpEquip = mvpEquip;

        ret.str = str; //力量
        ret.dex = dex; //敏捷
        ret._int = _int; //智力
        ret.luk = luk; //幸運
        ret.hp = hp; //Hp
        ret.mp = mp; //Mp
        ret.mad = mad; //魔法攻擊
        ret.mdd = mdd; //魔法防禦
        ret.pad = pad; //物理攻擊
        ret.pdd = pdd; //物理防禦
        ret.acc = acc; //命中率
        ret.avoid = avoid; //迴避率
        ret.hands = hands; //手技
        ret.speed = speed; //移動速度
        ret.jump = jump; //跳躍力
        ret.restUpgradeCount = restUpgradeCount;  //可升級次數
        ret.currentUpgradeCount = currentUpgradeCount; //已升級次數
        ret.itemEXP = itemEXP;
        ret.durability = durability; //耐久度
        ret.vicioushammer = vicioushammer; //金錘子
        ret.platinumhammer = platinumhammer; //白金鎚子
        ret.state = state; //潛能等級
        ret.addState = addState;
        ret.potential1 = potential1; //潛能1
        ret.potential2 = potential2; //潛能2
        ret.potential3 = potential3; //潛能3
        ret.potential4 = potential4; //潛能4
        ret.potential5 = potential5; //潛能5
        ret.potential6 = potential6; //潛能6
        ret.charmExp = charmExp; //魅力經驗
        ret.pvpDamage = pvpDamage; //大亂鬥攻擊力
        ret.incSkill = incSkill; //是否擁有技能
        ret.socket1 = socket1; //鑲嵌寶石1
        ret.socket2 = socket2; //鑲嵌寶石1
        ret.socket3 = socket3; //鑲嵌寶石1
        ret.itemSkin = itemSkin; //道具合成後的外觀
        //---------------------------------------------------------
        //下面的為新增的裝備屬性
        ret.enchantBuff = enchantBuff;
        ret.reqLevel = reqLevel;
        ret.yggdrasilWisdom = yggdrasilWisdom;
        ret.finalStrike = finalStrike;
        ret.bossDamage = bossDamage;
        ret.ignorePDR = ignorePDR;
        ret.totalDamage = totalDamage;
        ret.allStat = allStat;
        ret.karmaCount = karmaCount;
        ret.statsTest = statsTest;
        //---------------------------------------------------------
        ret.setGMLog(getGMLog()); //裝備是從什麼地方獲得的信息
        ret.setGiftFrom(getGiftFrom()); //是誰送的禮物
        ret.setOwner(getOwner()); //擁有者名字
        ret.setQuantity(getQuantity()); //數量
        ret.setExpiration(getTrueExpiration()); //道具經驗
        ret.setInventoryId(getInventoryId()); //道具的SQLid分解裝備和合成裝備需要
        //--------------------------------------------------------
        ret.lockSlot = lockSlot;
        ret.lockId = lockId;
        ret.sealedLevel = sealedLevel;
        ret.sealedExp = sealedExp;
        //靈魂武器
        ret.soulOptionID = soulOptionID;
        ret.soulSocketID = soulSocketID;
        ret.soulOption = soulOption;
        ret.soulSkill = soulSkill;
        ret.nirvanaFlame = new NirvanaFlame(nirvanaFlame);
        ret.nirvanaFlame.resetEquipExStats(ret);
        ret.starForce = new StarForce(starForce);
        ret.starForce.resetEquipStats(ret);
        ret.ARC = ARC;
        ret.ARCExp = ARCExp;
        ret.ARCLevel = ARCLevel;
        ret.aut = aut;
        ret.autExp = autExp;
        ret.autLevel = autLevel;

        ret.mvpEquip = mvpEquip;
        return ret;
    }

    public Item inherit(Equip srcEquip, Equip decEquip) {
        this.str = (short) (this.str + (short) (srcEquip.str - decEquip.str));
        this.dex = (short) (this.dex + (short) (srcEquip.dex - decEquip.dex));
        this._int = (short) (this._int + (short) (srcEquip._int - decEquip._int));
        this.luk = (short) (this.luk + (short) (srcEquip.luk - decEquip.luk));
        this.hp = (short) (this.hp + (short) (srcEquip.hp - decEquip.hp));
        this.mp = (short) (this.mp + (short) (srcEquip.mp - decEquip.mp));
        this.mad = (short) (this.mad + (short) (srcEquip.mad - decEquip.mad));
        this.mdd = (short) (this.mdd + (short) (srcEquip.mdd - decEquip.mdd));
        this.pad = (short) (this.pad + (short) (srcEquip.pad - decEquip.pad));
        this.pdd = (short) (this.pdd + (short) (srcEquip.pdd - decEquip.pdd));
        this.acc = (short) (this.acc + (short) (srcEquip.acc - decEquip.acc));
        this.avoid = (short) (this.avoid + (short) (srcEquip.avoid - decEquip.avoid));
        this.hands = (short) (this.hands + (short) (srcEquip.hands - decEquip.hands));
        this.speed = (short) (this.speed + (short) (srcEquip.speed - decEquip.speed));
        this.jump = (short) (this.jump + (short) (srcEquip.jump - decEquip.jump));
        this.restUpgradeCount = srcEquip.restUpgradeCount;
        this.currentUpgradeCount = srcEquip.currentUpgradeCount;
        this.itemEXP = srcEquip.itemEXP;
        this.durability = srcEquip.durability;
        this.vicioushammer = srcEquip.vicioushammer;
        this.platinumhammer = srcEquip.platinumhammer;
        this.charmExp = srcEquip.charmExp;
        this.pvpDamage = srcEquip.pvpDamage;
        this.incSkill = srcEquip.incSkill;
        this.enchantBuff = srcEquip.enchantBuff;
        this.reqLevel = srcEquip.reqLevel;
        this.yggdrasilWisdom = srcEquip.yggdrasilWisdom;
        this.finalStrike = srcEquip.finalStrike;
        this.bossDamage = srcEquip.bossDamage;
        this.ignorePDR = srcEquip.ignorePDR;
        this.totalDamage = srcEquip.totalDamage;
        this.allStat = srcEquip.allStat;
        this.karmaCount = srcEquip.karmaCount;
        this.nirvanaFlame = new NirvanaFlame(srcEquip.nirvanaFlame);
        this.nirvanaFlame.resetEquipExStats(this);
        this.starForce = new StarForce(srcEquip.starForce);
        this.starForce.resetEquipStats(this);
        this.soulOptionID = srcEquip.soulOptionID;
        this.soulSocketID = srcEquip.soulSocketID;
        this.soulOption = srcEquip.soulOption;
        this.sealedLevel = srcEquip.sealedLevel;
        this.sealedExp = srcEquip.sealedExp;
        this.setGiftFrom(this.getGiftFrom());
        this.copyPotential(srcEquip);
        return this;
    }

    @Override
    public byte getType() {
        return 1;
    }

    public Equip copyPotential(final Equip equip) {
        this.potential1 = equip.potential1;
        this.potential2 = equip.potential2;
        this.potential3 = equip.potential3;
        this.potential4 = equip.potential4;
        this.potential5 = equip.potential5;
        this.potential6 = equip.potential6;
        this.state = equip.state;
        this.addState = equip.addState;
        return this;
    }

    public byte getRestUpgradeCount() {
        return restUpgradeCount;
    }

    public void setRestUpgradeCount(byte restUpgradeCount) {
        this.restUpgradeCount = restUpgradeCount;
    }

    public short getTotalStr() {
        return (short) (str + starForce.getStr() + nirvanaFlame.getStr());
    }

    public short getSF_Str() {
        return (short) (starForce.getStr() + nirvanaFlame.getStr());
    }

    public short getStr() {
        return str;
    }

    public void setStr(short str) {
        if (str < 0) {
            str = 0;
        }
        this.str = str;
    }

    public short getTotalDex() {
        return (short) (dex + starForce.getDex() + nirvanaFlame.getDex());
    }

    public short getSF_Dex() {
        return (short) (starForce.getDex() + nirvanaFlame.getDex());
    }

    public short getDex() {
        return dex;
    }

    public void setDex(short dex) {
        if (dex < 0) {
            dex = 0;
        }
        this.dex = dex;
    }

    public short getTotalInt() {
        return (short) (_int + starForce.getInt() + nirvanaFlame.getInt());
    }

    public short getSF_Int() {
        return (short) (starForce.getInt() + nirvanaFlame.getInt());
    }

    public short getInt() {
        return _int;
    }

    public void setInt(short _int) {
        if (_int < 0) {
            _int = 0;
        }
        this._int = _int;
    }

    public short getTotalLuk() {
        return (short) (luk + starForce.getLuk() + nirvanaFlame.getLuk());
    }

    public short getSF_Luk() {
        return (short) (starForce.getLuk() + nirvanaFlame.getLuk());
    }

    public short getLuk() {
        return luk;
    }

    public void setLuk(short luk) {
        if (luk < 0) {
            luk = 0;
        }
        this.luk = luk;
    }

    public short getTotalHp() {
        return (short) (hp + starForce.getHp() + nirvanaFlame.getHp());
    }

    public short getSF_Hp() {
        return (short) (starForce.getHp() + nirvanaFlame.getHp());
    }

    public short getHp() {
        return hp;
    }

    public void setHp(short hp) {
        if (hp < 0) {
            hp = 0;
        }
        this.hp = hp;
    }

    public short getTotalMp() {
        return (short) (mp + starForce.getMp() + nirvanaFlame.getMp());
    }

    public short getSF_Mp() {
        return (short) (starForce.getMp() + nirvanaFlame.getMp());
    }

    public short getMp() {
        return mp;
    }

    public void setMp(short mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public short getTotalPad() {
        return (short) (pad + starForce.getPad() + nirvanaFlame.getPad());
    }

    public short getSF_Pad() {
        return (short) (starForce.getPad() + nirvanaFlame.getPad());
    }

    public short getPad() {
        return pad;
    }

    public void setPad(short pad) {
        if (pad < 0) {
            pad = 0;
        }
        this.pad = pad;
    }

    public short getTotalMad() {
        return (short) (mad + starForce.getMad() + nirvanaFlame.getMad());
    }

    public short getMad() {
        return mad;
    }

    public short getSF_Mad() {
        return (short) (starForce.getMad() + nirvanaFlame.getMad());
    }

    public void setMad(short mad) {
        if (mad < 0) {
            mad = 0;
        }
        this.mad = mad;
    }

    public short getTotalPdd() {
        return (short) (pdd + starForce.getPdd() + nirvanaFlame.getPdd());
    }

    public short getSF_Pdd() {
        return (short) (starForce.getPdd() + nirvanaFlame.getPdd());
    }

    public short getPdd() {
        return pdd;
    }

    public void setPdd(short pdd) {
        if (pdd < 0) {
            pdd = 0;
        }
        this.pdd = pdd;
    }

    public short getTotalMdd() {
        return (short) (mdd + starForce.getMdd() + nirvanaFlame.getMdd());
    }

    public short getSF_Mdd() {
        return (short) (starForce.getMdd() + nirvanaFlame.getMdd());
    }

    public short getMdd() {
        return mdd;
    }

    public void setMdd(short mdd) {
        if (mdd < 0) {
            mdd = 0;
        }
        this.mdd = mdd;
    }

    public short getTotalAcc() {
        return (short) (acc + starForce.getAcc() + nirvanaFlame.getAcc());
    }

    public short getAcc() {
        return acc;
    }

    public void setAcc(short acc) {
        if (acc < 0) {
            acc = 0;
        }
        this.acc = acc;
    }

    public short getTotalAvoid() {
        return (short) (avoid + starForce.getAvoid() + nirvanaFlame.getAvoid());
    }

    public short getAvoid() {
        return avoid;
    }

    public void setAvoid(short avoid) {
        if (avoid < 0) {
            avoid = 0;
        }
        this.avoid = avoid;
    }

    public short getTotalHands() {
        return (short) (hands + starForce.getHands() + nirvanaFlame.getHands());
    }

    public short getSF_Hands() {
        return (short) (starForce.getHands() + nirvanaFlame.getHands());
    }

    public short getHands() {
        return hands;
    }

    public void setHands(short hands) {
        if (hands < 0) {
            hands = 0;
        }
        this.hands = hands;
    }

    public short getTotalSpeed() {
        return (short) (speed + starForce.getSpeed() + nirvanaFlame.getSpeed());
    }

    public short getSpeed() {
        return speed;
    }

    public short getSF_Speed() {
        return (short) (starForce.getSpeed() + nirvanaFlame.getSpeed());
    }

    public void setSpeed(short speed) {
        if (speed < 0) {
            speed = 0;
        }
        this.speed = speed;
    }

    public short getTotalJump() {
        return (short) (jump + starForce.getJump() + nirvanaFlame.getJump());
    }

    public short getSF_Jump() {
        return (short) (starForce.getJump() + nirvanaFlame.getJump());
    }

    public short getJump() {
        return jump;
    }

    public void setJump(short jump) {
        if (jump < 0) {
            jump = 0;
        }
        this.jump = jump;
    }

    public byte getCurrentUpgradeCount() {
        return currentUpgradeCount;
    }

    public void setCurrentUpgradeCount(byte currentUpgradeCount) {
        this.currentUpgradeCount = currentUpgradeCount;
    }

    public byte getViciousHammer() {
        return vicioushammer;
    }

    public void setViciousHammer(byte ham) {
        vicioushammer = ham;
    }

    public byte getPlatinumHammer() {
        return platinumhammer;
    }

    public void setPlatinumHammer(byte ham) {
        platinumhammer = ham;
    }

    public byte getTotalHammer() {
        return (byte) (vicioushammer + platinumhammer);
    }

    public long getItemEXP() {
        return itemEXP;
    }

    public void setItemEXP(long itemEXP) {
        if (itemEXP < 0) {
            itemEXP = 0;
        }
        this.itemEXP = itemEXP;
    }

    public long getEquipExp() {
        if (itemEXP <= 0) {
            return 0;
        }
        //aproximate value
        if (ItemConstants.類型.武器(getItemId())) {
            return itemEXP / WEAPON_RATIO;
        } else {
            return itemEXP / ARMOR_RATIO;
        }
    }

    public long getEquipExpForLevel() {
        if (getEquipExp() <= 0) {
            return 0;
        }
        long expz = getEquipExp();
        for (int i = getBaseLevel(); i <= ItemConstants.getMaxLevel(getItemId()); i++) {
            if (expz >= ItemConstants.getExpForLevel(i, getItemId())) {
                expz -= ItemConstants.getExpForLevel(i, getItemId());
            } else {
                break;
            }
        }
        return expz;
    }

    public long getExpPercentage() {
        if (getEquipLevel() < getBaseLevel() || getEquipLevel() > ItemConstants.getMaxLevel(getItemId()) || ItemConstants.getExpForLevel(getEquipLevel(), getItemId()) <= 0) {
            return 0;
        }
        return getEquipExpForLevel() * 100 / ItemConstants.getExpForLevel(getEquipLevel(), getItemId());
    }

    public int getEquipLevel() {
        int fixLevel = MapleItemInformationProvider.getInstance().getEquipmentSkillsFixLevel(getItemId());
        if (fixLevel > 0) {
            return fixLevel;
        }

        int maxLevel = ItemConstants.getMaxLevel(getItemId());
        int levelz = getBaseLevel();
        if (getEquipExp() <= 0) {
            return Math.min(levelz, maxLevel);
        }
        long expz = getEquipExp();
        for (int i = levelz; i < maxLevel; i++) {
            if (expz >= ItemConstants.getExpForLevel(i, getItemId())) {
                levelz++;
                expz -= ItemConstants.getExpForLevel(i, getItemId());
            } else {
                break;
            }
        }
        return levelz;
    }

    public int getBaseLevel() {
        return (GameConstants.getStatFromWeapon(getItemId()) == null ? 1 : 0);
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("設置裝備的數量錯誤 欲設置的數量： " + quantity + " (道具ID: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    /*
     * 耐久度也就是持久
     */
    public int getDurability() {
        return durability;
    }

    public void setDurability(int dur) {
        this.durability = dur;
    }

    public int getPotential(int pos, boolean add) {
        switch (pos) {
            case 1: {
                if (add) {
                    return this.potential4;
                }
                return this.potential1;
            }
            case 2: {
                if (add) {
                    return this.potential5;
                }
                return this.potential2;
            }
            case 3: {
                if (add) {
                    return this.potential6;
                }
                return this.potential3;
            }
        }
        return 0;
    }

    public void setPotential(int potential, int pos, boolean add) {
        switch (pos) {
            case 1: {
                if (add) {
                    this.potential4 = potential;
                    break;
                }
                this.potential1 = potential;
                break;
            }
            case 2: {
                if (add) {
                    this.potential5 = potential;
                    break;
                }
                this.potential2 = potential;
                break;
            }
            case 3: {
                if (add) {
                    this.potential6 = potential;
                    break;
                }
                this.potential3 = potential;
            }
        }
    }

    /*
     * 潛能屬性1
     */
    public int getPotential1() {
        return potential1;
    }

    public void setPotential1(int en) {
        this.potential1 = en;
    }

    /*
     * 潛能屬性2
     */
    public int getPotential2() {
        return potential2;
    }

    public void setPotential2(int en) {
        this.potential2 = en;
    }

    /*
     * 潛能屬性3
     */
    public int getPotential3() {
        return potential3;
    }

    public void setPotential3(int en) {
        this.potential3 = en;
    }

    /*
     * 潛能屬性4
     */
    public int getPotential4() {
        return potential4;
    }

    public void setPotential4(int en) {
        this.potential4 = en;
    }

    /*
     * 潛能屬性5
     */
    public int getPotential5() {
        return potential5;
    }

    public void setPotential5(int en) {
        this.potential5 = en;
    }

    /*
     * 潛能屬性6
     */
    public int getPotential6() {
        return potential6;
    }

    public void setPotential6(int en) {
        this.potential6 = en;
    }

    /*
     * 裝備的等級
     * 15 = 未鑒定 16以下 20以上都是未鑒定
     * 16 = C級
     * 17 = B級
     * 18 = A級
     * 19 = S級
     * 20 = SS級
     */
    public byte getState(boolean add) {
        if (ServerConfig.DISABLE_POTENTIAL) return 0;
        if (add) {
            return addState;
        }
        return state;
    }

    public void setState(byte en, boolean add) {
        if (ServerConfig.DISABLE_POTENTIAL) en = 0;
        if (add) {
            addState = en;
        } else {
            state = en;
        }
    }

    public void initAllState() {
        initState(false);
        initState(true);
    }

    public void initState(boolean useAddPot) {
        int ret = 0;
        int v1;
        int v2;
        int v3;
        if (!useAddPot) {
            v1 = this.potential1;
            v2 = this.potential2;
            v3 = this.potential3;
        } else {
            v1 = this.potential4;
            v2 = this.potential5;
            v3 = this.potential6;
        }
        if (v1 >= 40000 || v2 >= 40000 || v3 >= 40000) {
            ret = 20;// 傳說
        } else if (v1 >= 30000 || v2 >= 30000 || v3 >= 30000) {
            ret = 19;// 罕見
        } else if (v1 >= 20000 || v2 >= 20000 || v3 >= 20000) {
            ret = 18;// 稀有
        } else if (v1 >= 1 || v2 >= 1 || v3 >= 1) {
            ret = 17;// 特殊
        } else if (v1 == -20 || v2 == -20 || v1 == -4 || v2 == -4) {
            ret = 4;// 未鑒定傳說
        } else if (v1 == -19 || v2 == -19 || v1 == -3 || v2 == -3) {
            ret = 3;// 未鑒定罕見
        } else if (v1 == -18 || v2 == -18 || v1 == -2 || v2 == -2) {
            ret = 2;// 未鑒定稀有
        } else if (v1 == -17 || v2 == -17 || v1 == -1 || v2 == -1) {
            ret = 1;// 未鑒定特殊
        } else if (v1 < 0 || v2 < 0 || v3 < 0) {
            return;
        }
        setState((byte) ret, useAddPot);
    }

    public void resetPotential_Fuse(boolean half, int potentialState) { //makeskill - equip first receive
        //0.16% chance unique, 4% chance epic, else rare
        potentialState = -potentialState;
        if (Randomizer.nextInt(100) < 4) {
            potentialState -= Randomizer.nextInt(100) < 4 ? 2 : 1;
        }
        setPotential1(potentialState);
        setPotential2((Randomizer.nextInt(half ? 5 : 10) == 0 ? potentialState : 0)); //1/10 chance of 3 line
        setPotential3(0); //just set it theoretically
        initState(false);
    }

    public void renewPotential(final boolean add) {
        this.renewPotential(0, add);
    }

    public void renewPotential(final int rank, final boolean add) {
        this.renewPotential(rank, false, add);
    }

    public void renewPotential(final boolean third, final boolean add) {
        this.renewPotential(0, third, add);
    }

    public void renewPotential(final int rank, final boolean third, final boolean add) {
        int state;
        switch (rank) {
            case 1: {
                state = -17;
                break;
            }
            case 2: {
                state = -18;
                break;
            }
            case 3: {
                state = -19;
                break;
            }
            case 4: {
                state = -20;
                break;
            }
            default: {
                state = ((Randomizer.nextInt(100) < 4) ? ((Randomizer.nextInt(100) < 4) ? -19 : -18) : -17);
                break;
            }
        }
        final boolean b3 = (this.getState(add) != 0 && this.getPotential(3, add) != 0) || third;
        this.setPotential(state, 1, add);
        this.setPotential(Randomizer.nextInt(10) <= 1 || b3 ? state : 0, 2, add);
        this.setPotential(0, 3, add);
        this.initState(add);
    }

    public boolean useCube(int cubeId, MapleCharacter player) {
        return useCube(cubeId, player, 0);
    }

    public boolean useCube(int cubeId, MapleCharacter player, int lockslot) {
        return useCube((short) 0, 0, cubeId, player, lockslot);
    }

    public boolean useCube(short opcode, int action, int cubeId, MapleCharacter player) {
        return useCube(opcode, action, cubeId, player, 0);
    }

    public boolean useCube(short opcode, int action, int cubeId, MapleCharacter player, int lockslot) {
        if (player.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1) {
            int cubeTpye = ItemConstants.方塊.getDefaultPotentialFlag(cubeId);
            boolean isBonus = ItemConstants.方塊.CubeType.附加潛能.check(cubeTpye);
            if (!ItemConstants.方塊.canUseCube(this, cubeId)) {
                player.dropMessage(5, "你無法對這個物品使用這個方塊。");
                return false;
            }
            switch (cubeId) {
                case 3994895: // 楓方塊
                case 3996222: // v259 神秘方塊
                case 5062017: // 閃耀方塊
                case 5062019: // 閃耀鏡射方塊
                case 5062020: // 閃炫方塊
                case 5062021: // 新對等方塊
                case 5062024: // 六角魔方
                case 5062026: // 組合方塊
                case 5062030: // 恢復方塊
                case 5062032: // 卡勒瑪恢復方塊
                    break;
                default:
                    long meso = ItemConstants.方塊.getCubeNeedMeso(this);
                    if (player.getMeso() < meso) {
                        player.dropMessage(5, "您沒有足夠的楓幣。");
                        player.sendEnableActions();
                        return false;
                    }
                    player.gainMeso(-meso, false);
                    break;
            }
            if (this.getState(isBonus) >= 17 && this.getState(isBonus) <= 20) {
                int oldState = getState(isBonus);
                int rateIndex = oldState - (oldState < 17 ? 1 : 17);
                int stateRate;
                if (rateIndex >= 0 && rateIndex <= 2) {
                    stateRate = ServerConfig.CHANNEL_RATE_POTENTIALLEVEL * ItemConstants.方塊.getCubeRankUpRate(cubeId)[rateIndex];
                } else {
                    stateRate = 0;
                }
                if (EnhanceResultType.UPGRADE_TIER.check(getEnchantBuff())) {
                    stateRate = 1000000;
                    if (player.isAdmin()) {
                        player.dropMessage(-6, "裝備自帶100%潛能等級提升成功率");
                    }
                } else if (player.isAdmin() && player.isInvincible() && getState(isBonus) < 20 && stateRate < 1000000) {
                    //player.dropMessage(-6, "伺服器管理員無敵狀態潛能等級提升成功率100%");
                    stateRate = 1000000;
                }
                boolean isMemorial = false;
                switch (cubeId) {
                    case 5062010: // 黑色方塊
                    case 5062017: // 閃耀方塊
                    case 5062090: // 記憶方塊
                    case 3994895: // 楓方塊
                    case 5062019: // 閃耀鏡射方塊
                    case 5062020: // 閃炫方塊
                    case 5062030: // 恢復方塊
                    case 5062021: // 新對等方塊
                    case 5062024: // 六角魔方
                    case 5062026: // 組合方塊
                    case 5062500:
                    case 5062032: // 卡勒瑪恢復方塊
                    case 5062503: { // 白色附加方塊
                        isMemorial = true;
                        break;
                    }
                }
                int debris = ItemConstants.方塊.getCubeDebris(cubeId);
                if (debris > 0 && !MapleInventoryManipulator.addById(player.getClient(), debris, 1, "Cube on " + DateUtil.getCurrentDate())) {
                    return false;
                }
                if (isMemorial) {
                    int lines;
                    String pots = "";
                    if (cubeId == 5062020 || cubeId == 5062100) { // 閃炫方塊 || 六角魔方
                        int newState = getState(isBonus);
                        List<Integer> newPots = new ArrayList<>();
                        for (int i = 0; i < 2; i++) {
                            renewPotential(stateRate, cubeTpye, lockslot);
                            magnify();
                            newPots.add(getPotential(1, isBonus));
                            newPots.add(getPotential(2, isBonus));
                            if (getPotential(3, isBonus) > 0) {
                                newPots.add(getPotential(3, isBonus));
                            }
                            if (i == 0) {
                                int state = getState(isBonus);
                                setState((byte) newState, isBonus);
                                newState = state;
                            }
                        }
                        lines = newPots.size() / 2;
                        setState((byte) newState, isBonus);
                        for (int i = 0; i < newPots.size(); i++) {
                            pots += newPots.get(i);
                            if (i < newPots.size() - 1) {
                                pots += ",";
                            }
                        }
                        if (cubeId == 5062024) { // 六角魔方
                            player.send(InventoryPacket.showHyunPotentialResult(newPots));
                        } else {
                            player.write(OverseasPacket.getHexaCubeRes(opcode, action, newPots));
                        }
                    } else {
                        lines = getPotential(2, isBonus) != 0 ? 3 : 2;
//                        player.updateInfoQuest(GameConstants.楓方塊, "dst=-1;Pot0=-1;Pot1=-1;Pot2=-1;add=0");
                        for (int i = 0; i < lines; ++i) {
//                            player.updateOneInfo(GameConstants.楓方塊, "Pot" + i, String.valueOf(getPotential(i + 1, isBonus)));
                            pots += getPotential(i + 1, isBonus);
                            if (i < lines - 1) {
                                pots += ",";
                            }
                        }
                        renewPotential(stateRate, cubeTpye, lockslot);
                        magnify();
//                        player.updateOneInfo(GameConstants.楓方塊, "dst", String.valueOf(getPosition()));
//                        player.updateOneInfo(GameConstants.楓方塊, "add", isBonus ? "1" : "0");
                        if (cubeId == 5062017 || cubeId == 5062030) { // 閃耀方塊
                            player.write(OverseasPacket.getAnimusCubeRes(opcode, action, cubeId, this));
                        } else {
                            player.send(InventoryPacket.showCubeResetResult(getPosition(), this, cubeId, player.getInventory(MapleInventoryType.CASH).findById(cubeId).getPosition()));
                        }
                    }
                    player.updateOneInfo(GameConstants.台方塊, "c", String.valueOf(lines), false);
                    player.updateOneInfo(GameConstants.台方塊, "i", String.valueOf(getItemId()), false);
                    player.updateOneInfo(GameConstants.台方塊, "o", pots, false);
                    player.updateOneInfo(GameConstants.台方塊, "p", String.valueOf(getPosition()), false);
                    player.updateOneInfo(GameConstants.台方塊, "u", String.valueOf(cubeId), false);
                    player.updateOneInfo(GameConstants.台方塊, "s", String.valueOf(oldState), false);
                } else if (cubeId == 5062026) { // 結合方塊
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    int reqLevel = ii.getReqLevel(getItemId()) / 10;
                    final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
                    while (true) {
                        if (reqLevel >= 20) {
                            reqLevel = 19;
                        }
                        StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                        if (pot != null && pot.reqLevel / 10 <= reqLevel && ItemConstants.方塊.optionTypeFits(pot.optionType, getItemId()) && ItemConstants.方塊.potentialIDFits(pot.opID, getState(isBonus), lockslot) && ItemConstants.方塊.isAllowedPotentialStat(this, pot.opID, isBonus, ItemConstants.方塊.CubeType.點商光環.check(cubeTpye)) && (!ItemConstants.方塊.CubeType.去掉無用潛能.check(cubeTpye) || (ItemConstants.方塊.CubeType.去掉無用潛能.check(cubeTpye) && !ItemConstants.方塊.isUselessPotential(pot)))) { // optionType
                            setPotential(pot.opID, lockslot, isBonus);
                            break;
                        }
                    }
                } else {
                    renewPotential(stateRate, cubeTpye, lockslot);
                    magnify();
                    if (cubeId == 5062019 || cubeId == 5062021) { // 閃耀鏡射方塊 || 新對等方塊
                        player.write(OverseasPacket.getTmsCubeRes(opcode, action, 0));
                    } else if (cubeId != 3994895) { // 不是楓方塊
                        player.send(InventoryPacket.showCubeResult(player.getId(), oldState < getState(isBonus), cubeId, this.getPosition(), Math.max(0, player.getItemQuantity(cubeId) - 1), this.copy()));
                        player.forceUpdateItem(this);
                        player.getMap().broadcastMessage(InventoryPacket.showPotentialReset(player.getId(), oldState < getState(isBonus), cubeId, debris, getItemId()));
                    }
                }

                if (oldState < getState(isBonus) && getState(isBonus) == 20) {
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    StringBuilder msg = new StringBuilder();
                    msg.append(player.getName()).append("使用").append(ii.getName(cubeId));
                    String eqName = "{" + ii.getName(getItemId()) + "}";
                    msg.append("將").append(eqName).append("的");
                    if (isBonus) {
                        msg.append("附加潛能");
                    }
                    msg.append("等級提升為傳說.");
                    if (player.isIntern()) {
                        msg.append("（管理員此訊息僅自己可見）");
                        player.send(MaplePacketCreator.gachaponMsg(msg.toString(), this));
                    } else {
                        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg(msg.toString(), this));
                    }
                    player.send(EffectPacket.showIncubatorEffect(-1, 0, "Effect/BasicEff/Event1/Best"));
                    player.getMap().broadcastMessage(player, EffectPacket.showIncubatorEffect(player.getId(), 0, "Effect/BasicEff/Event1/Best"), false);
                }
                return true;
            }
            player.dropMessage(5, "請確認您要重置的道具具有潛能屬性。");
        }
        return false;
    }

    public boolean magnify() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int reqLevel = ii.getReqLevel(getItemId()) / 10;
        final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
        final boolean isBonus = !(getState(false) < 17 && getState(false) > 0);

        int lockedLine = 0;
        int locked = Math.abs(getPotential(1, isBonus)) % 1000000;
        if (locked >= 100000) {
            lockedLine = locked / 100000;
            locked %= 100000;
        } else {
            locked = 0;
        }
        final int lines = (getPotential(2, isBonus) != 0) ? 3 : 2;

        // 鑒定潛能
        int new_state = getState(isBonus) + 16;
        if (new_state > 20) {
            new_state = 20;
        } else if (new_state < 17) {
            new_state = 17;
        }
        final int cubeType = Math.abs(getPotential(3, isBonus));
        setPotential(0, 3, isBonus);
        final boolean twins = ItemConstants.方塊.CubeType.前兩條相同.check(cubeType);
        for (int i = 1; i <= lines; ++i) {
            if (i == lockedLine) {
                setPotential(locked, lockedLine, isBonus);
                continue;
            }
            while (true) {
                if (reqLevel >= 20) {
                    reqLevel = 19;
                }
                StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                if (pot != null && pot.reqLevel / 10 <= reqLevel && ItemConstants.方塊.optionTypeFits(pot.optionType, getItemId()) && ItemConstants.方塊.potentialIDFits(pot.opID, new_state, ItemConstants.方塊.CubeType.對等.check(cubeType) ? 1 : i) && ItemConstants.方塊.isAllowedPotentialStat(this, pot.opID, isBonus, ItemConstants.方塊.CubeType.點商光環.check(cubeType)) && (!ItemConstants.方塊.CubeType.去掉無用潛能.check(cubeType) || (ItemConstants.方塊.CubeType.去掉無用潛能.check(cubeType) && !ItemConstants.方塊.isUselessPotential(pot)))) { // optionType
                    if (i == 1 && twins) {
                        setPotential(pot.opID, 2, isBonus);
                    }
                    if (i != 2 || !twins) {
                        setPotential(pot.opID, i, isBonus);
                    }
                    break;
                }
            }
        }
        initState(isBonus);
        return true;
    }

    public List<StructItemOption> getFitOptionList(int size) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int level = ii.getReqLevel(this.getItemId()) / 10;
        level = level >= 20 ? 19 : level;
        List<List<StructItemOption>> linkedList = new LinkedList<>(ii.getAllPotentialInfo().values());
        int state = this.getState(false) + 16;
        if (state > 20) {
            state = 20;
        } else if (state < 17) {
            state = 17;
        }
        int n5 = Math.abs(this.getPotential(3, false));
        List<StructItemOption> arrayList = new ArrayList<>(6);
        for (int i2 = 1; i2 <= size; ++i2) {
            boolean bl2 = false;
            while (!bl2) {
                StructItemOption itemOption = linkedList.get(Randomizer.nextInt(linkedList.size())).get(level);
                if (itemOption == null
                        || GameConstants.isAboveA(itemOption.opID)
                        || !GameConstants.optionTypeFits(itemOption.optionType, this.getItemId())
                        || !GameConstants.isBlockedPotential(this, itemOption.opID, false, ItemConstants.方塊.CubeType.點商光環.check(n5))
                        || !GameConstants.potentialIDFits(itemOption.opID, state, ItemConstants.方塊.CubeType.對等.check(n5) ? 1 : i2)
                        || ItemConstants.方塊.CubeType.去掉無用潛能.check(n5)
                        && (!ItemConstants.方塊.CubeType.去掉無用潛能.check(n5) || ItemConstants.方塊.isUselessPotential(itemOption))) {
                    continue;
                }
                arrayList.add(itemOption);
                bl2 = true;
            }
        }
        return arrayList;
    }

    public void renewPotential(int defaultRate, int flag, int lockSlot) {
        int miracleRate = 1;//方塊雙倍時間
        final boolean bonus = ItemConstants.方塊.CubeType.附加潛能.check(flag);
        final boolean threeLine = this.getPotential(3, bonus) > 0;

        int rank = (Randomizer.nextInt(1000000) < defaultRate * miracleRate) ? 1 : 0;
        if (ItemConstants.方塊.CubeType.等級下降.check(flag) && rank == 0) {
            rank = ((Randomizer.nextInt(1000000) < (defaultRate + 200000) * miracleRate) ? -1 : 0);
        }

        if (ItemConstants.方塊.CubeType.前兩條相同.check(flag)) {
            flag -= ((Randomizer.nextInt(10) <= 5) ? ItemConstants.方塊.CubeType.前兩條相同.getValue() : 0);
        }

        if (this.getState(bonus) + rank < 17 || this.getState(bonus) + rank > (!ItemConstants.方塊.CubeType.傳說.check(flag) ? !ItemConstants.方塊.CubeType.罕見.check(flag) ? !ItemConstants.方塊.CubeType.稀有.check(flag) ? 17 : 18 : 19 : 20)) {
            rank = 0;
        }

        setState((byte) (this.getState(bonus) + rank - 16), bonus);

        if (lockSlot != 0 && lockSlot <= 3) {
            this.setPotential(-(lockSlot * 100000 + this.getPotential(lockSlot, bonus)), 1, bonus);
        } else {
            this.setPotential(-getState(bonus), 1, bonus);
        }

        if (ItemConstants.方塊.CubeType.調整潛能條數.check(flag)) {
            this.setPotential((Randomizer.nextInt(10) <= 2) ? -getState(bonus) : 0, 2, bonus);
        } else if (threeLine) {
            this.setPotential(-getState(bonus), 2, bonus);
        } else {
            this.setPotential(0, 2, bonus);
        }
        setPotential(-flag, 3, bonus);

        if (ItemConstants.方塊.CubeType.洗後無法交易.check(flag)) {
            addAttribute(ItemAttribute.TradeBlock.getValue());
        }

        initState(bonus);
    }

    public void setNewArcInfo(int job) {
        ARCLevel = 1;
        ARCExp = 1;
        recalcArcStat(job);
    }

    public void recalcArcStat(int job) {
        int n = ARCLevel + 2;
        ARC = (short) (10 * n);
        if (JobConstants.is惡魔復仇者(job)) {
            hp = (short) (2100 * n);
        } else if (JobConstants.is傑諾(job)) {
            str = (short) (48 * n);
            dex = (short) (48 * n);
            luk = (short) (48 * n);
        } else {
            switch (JobConstants.getJobBranch(job)) {
                case 1:
                    str = (short) (100 * n);
                    break;
                case 2:
                    _int = (short) (100 * n);
                    break;
                case 3:
                    dex = (short) (100 * n);
                    break;
                case 4:
                    luk = (short) (100 * n);
                    break;
                case 5:
                    if (JobConstants.is拳霸(job) || JobConstants.is隱月(job) || JobConstants.is重砲指揮官(job) || JobConstants.is閃雷悍將(job) || JobConstants.is亞克(job)) {
                        str = (short) (100 * n);
                    } else {
                        dex = (short) (100 * n);
                    }
            }
        }
    }

    public void setNewAutInfo(int job) {
        autLevel = 1;
        autExp = 1;
        recalcAutStat(job);
    }

    public void recalcAutStat(int job) {
        int n = autLevel;
        aut = (short) (10 * n);
        if (JobConstants.is惡魔復仇者(job)) {
            hp = (short) (6300 + 4200 * n);
        } else if (JobConstants.is傑諾(job)) {
            str = (short) (144 + 96 * n);
            dex = (short) (144 + 96 * n);
            luk = (short) (144 + 96 * n);
        } else {
            switch (JobConstants.getJobBranch(job)) {
                case 1:
                    str = (short) (300 + 200 * n);
                    break;
                case 2:
                    _int = (short) (300 + 200 * n);
                    break;
                case 3:
                    dex = (short) (300 + 200 * n);
                    break;
                case 4:
                    luk = (short) (300 + 200 * n);
                    break;
                case 5:
                    if (JobConstants.is拳霸(job) || JobConstants.is隱月(job) || JobConstants.is重砲指揮官(job) || JobConstants.is閃雷悍將(job) || JobConstants.is亞克(job)) {
                        str = (short) (300 + 200 * n);
                    } else {
                        dex = (short) (300 + 200 * n);
                    }
            }
        }
    }

    /*
     * 裝備技能
     */
    public int getIncSkill() {
        return incSkill;
    }

    public void setIncSkill(int inc) {
        this.incSkill = inc;
    }

    /*
     * 裝備魅力經驗
     */
    public short getCharmEXP() {
        return charmExp;
    }

    public void setCharmEXP(short s) {
        this.charmExp = s;
    }

    /*
     * 裝備大亂鬥攻擊力
     */
    public short getPVPDamage() {
        return pvpDamage;
    }

    public void setPVPDamage(short p) {
        this.pvpDamage = p;
    }

    /*
     * 戒指
     */
    public MapleRing getRing() {
        if (!ItemConstants.類型.特效裝備(getItemId()) || getSN() <= 0) {
            return null;
        }
        if (ring == null) {
            ring = MapleRing.loadFromDb(getSN(), getPosition() < 0);
        }
        return ring;
    }

    public void setRing(MapleRing ring) {
        this.ring = ring;
    }

    /*
     * 機器人
     */
    public MapleAndroid getAndroid() {
        if (getItemId() / 10000 != 166 || getSN() <= 0) {
            return null;
        }
        if (android == null) {
            android = MapleAndroid.loadFromDb(getItemId(), getSN());
        }
        return android;
    }

    public void setAndroid(MapleAndroid android) {
        this.android = android;
        if (android != null && getSN() != android.getUniqueId() && android.getUniqueId() > 0) {
            setSN(android.getUniqueId());
        }
    }

    /*
     * 裝備插槽 可以鑲嵌寶石
     * V.102新增功能
     * 0x01 = 你可以在這件物品上鑲入星岩。
     * 0x03 = 你可以在這件物品上鑲入星岩。 有個鑲嵌的孔 未鑲嵌
     * 0x13 = 有1個插孔 已經鑲嵌東西
     */
    public short getSocketState() {
        short flag = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean isSocketItem = ServerConfig.ALL_SOCKET ? !ii.isCash(getItemId()) : (ii.isActivatedSocketItem(getItemId()) || socket1 >= 0);
        if (isSocketItem) {
            flag |= SocketFlag.可以鑲嵌.getValue();
            if (socket1 == -1) {
                setSocket1(0);
            }
            if (socket1 != -1) {
                flag |= SocketFlag.已打孔01.getValue();
            }
            if (socket2 != -1) {
                flag |= SocketFlag.已打孔02.getValue();
            }
            if (socket3 != -1) {
                flag |= SocketFlag.已打孔03.getValue();
            }
            if (socket1 > 0) {
                flag |= SocketFlag.已鑲嵌01.getValue();
            }
            if (socket2 > 0) {
                flag |= SocketFlag.已鑲嵌02.getValue();
            }
            if (socket3 > 0) {
                flag |= SocketFlag.已鑲嵌03.getValue();
            }
        }
        return flag;
    }

    public int getSocket1() {
        return socket1;
    }

    public void setSocket1(int socket) {
        this.socket1 = socket;
    }

    public int getSocket2() {
        return socket2;
    }

    public void setSocket2(int socket) {
        this.socket2 = socket;
    }

    public int getSocket3() {
        return socket3;
    }

    public void setSocket3(int socket) {
        this.socket3 = socket;
    }

    /*
     * 裝備合成後的外觀
     */
    public int getItemSkin() {
        return itemSkin;
    }

    public void setItemSkin(int id) {
        this.itemSkin = id;
    }

    /*
     * 新增的裝備屬性
     */
    public short getEnchantBuff() {
        return enchantBuff;
    }

    public void setEnchantBuff(short enchantBuff) {
        if (enchantBuff < 0) {
            enchantBuff = 0;
        }
        this.enchantBuff = enchantBuff;
    }

    public short getReqLevel() {
        if (reqLevel == -1) {
            reqLevel = (short) MapleItemInformationProvider.getInstance().getReqLevel(getItemId());
        }
        return reqLevel;
    }

    public short getTotalReqLevel() {
        return (short) Math.max(1, getReqLevel() - nirvanaFlame.getReqLevel());
    }

    public void setReqLevel(short reqLevel) {
        if (reqLevel < 0) {
            reqLevel = 0;
        }
        this.reqLevel = reqLevel;
    }

    public byte getDownLevel() {
        return (byte) (MapleItemInformationProvider.getInstance().getReqLevel(getItemId()) - getReqLevel());
    }

    public byte getTotalDownLevel() {
        return (byte) (MapleItemInformationProvider.getInstance().getReqLevel(getItemId()) - getTotalReqLevel());
    }

    public short getYggdrasilWisdom() {
        return yggdrasilWisdom;
    }

    public void setYggdrasilWisdom(short yggdrasilWisdom) {
        if (yggdrasilWisdom < 0) {
            yggdrasilWisdom = 0;
        }
        this.yggdrasilWisdom = yggdrasilWisdom;
    }

    public boolean getFinalStrike() {
        return finalStrike;
    }

    public void setFinalStrike(boolean finalStrike) {
        this.finalStrike = finalStrike;
    }

    public short getTotalBossDamage() {
        return (short) (bossDamage + starForce.getBossDamage() + nirvanaFlame.getBossDamage());
    }

    public short getBossDamage() {
        return bossDamage;
    }

    public void setBossDamage(short bossDamage) {
        if (bossDamage < 0) {
            bossDamage = 0;
        }
        this.bossDamage = bossDamage;
    }

    public short getTotalIgnorePDR() {
        return (short) (ignorePDR + starForce.getIgnorePDR() + nirvanaFlame.getIgnorePDR());
    }

    public short getIgnorePDR() {
        return ignorePDR;
    }

    public void setIgnorePDR(short ignorePDR) {
        if (ignorePDR < 0) {
            ignorePDR = 0;
        }
        this.ignorePDR = ignorePDR;
    }

    /*
     * 新增的裝備特殊屬性
     */
    public short getTotalTotalDamage() {
        return (short) (totalDamage + starForce.getTotalDamage() + nirvanaFlame.getTotalDamage());
    }

    public short getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(short totalDamage) {
        if (totalDamage < 0) {
            totalDamage = 0;
        }
        this.totalDamage = totalDamage;
    }

    public short getTotalAllStat() {
        return (short) (allStat + starForce.getAllStat() + nirvanaFlame.getAllStat());
    }

    public short getAllStat() {
        return allStat;
    }

    public void setAllStat(short allStat) {
        if (allStat < 0) {
            allStat = 0;
        }
        this.allStat = allStat;
    }

    public short getKarmaCount() {
        return karmaCount;
    }

    public void setKarmaCount(short karmaCount) {
        this.karmaCount = karmaCount;
    }

    public Map<EquipStats, Long> getStatsTest() {
        return statsTest;
    }

    /*
     * 裝備的總體狀態
     */
    public int getEquipFlag() {
        int flag = 0;
        if (getRestUpgradeCount() > 0) {
            flag |= EquipStats.可使用捲軸次數.getValue();
        }
        if (getCurrentUpgradeCount() > 0) {
            flag |= EquipStats.捲軸強化次數.getValue();
        }
        if (getTotalStr() > 0) {
            flag |= EquipStats.力量.getValue();
        }
        if (getTotalDex() > 0) {
            flag |= EquipStats.敏捷.getValue();
        }
        if (getTotalInt() > 0) {
            flag |= EquipStats.智力.getValue();
        }
        if (getTotalLuk() > 0) {
            flag |= EquipStats.幸運.getValue();
        }
        if (getTotalHp() > 0) {
            flag |= EquipStats.MaxHP.getValue();
        }
        if (getTotalMp() > 0) {
            flag |= EquipStats.MaxMP.getValue();
        }
        if (getTotalPad() > 0) {
            flag |= EquipStats.攻擊力.getValue();
        }
        if (getTotalMad() > 0) {
            flag |= EquipStats.魔力.getValue();
        }
        if (getTotalPdd() > 0) {
            flag |= EquipStats.防禦力.getValue();
        }
        if (getTotalHands() > 0) {
            flag |= EquipStats.靈敏度.getValue();
        }
        if (getTotalSpeed() > 0) {
            flag |= EquipStats.移動速度.getValue();
        }
        if (getTotalJump() > 0) {
            flag |= EquipStats.跳躍力.getValue();
        }
        if (getCAttribute() > 0) {
            flag |= EquipStats.狀態.getValue();
        }
        if (getIncSkill() > 0) {
            flag |= EquipStats.裝備技能.getValue();
        }
        if (isSealedEquip()) {
            if (getSealedLevel() > 0) {
                flag |= EquipStats.裝備等級.getValue();
            }
            if (getSealedExp() > 0) {
                flag |= EquipStats.裝備經驗.getValue();
            }
        } else {
            if (getEquipLevel() > 0) {
                flag |= EquipStats.裝備等級.getValue();
            }
            if (getExpPercentage() > 0) {
                flag |= EquipStats.裝備經驗.getValue();
            }
        }
        if (getDurability() > 0) {
            flag |= EquipStats.耐久度.getValue();
        }
        if (getTotalHammer() > 0) {
            flag |= EquipStats.鎚子.getValue();
        }
        if (getPVPDamage() > 0) {
            flag |= EquipStats.大亂鬥傷害.getValue();
        }
        if (getDownLevel() > 0) {
            flag |= EquipStats.套用等級減少.getValue();
        }
        if (getEnchantBuff() > 0) {
            flag |= EquipStats.ENHANCT_BUFF.getValue();
        }
        if (getiIncReq() > 0) {
            flag |= EquipStats.REQUIRED_LEVEL.getValue(); //穿戴裝備的等級要求提高
        }
        if (getYggdrasilWisdom() > 0) {
            flag |= EquipStats.YGGDRASIL_WISDOM.getValue();
        }
        if (getFinalStrike()) {
            flag |= EquipStats.FINAL_STRIKE.getValue(); //最終一擊卷軸成功
        }
        if (getTotalBossDamage() > 0) {
            flag |= EquipStats.BOSS傷.getValue(); //BOSS傷害增加百分比
        }
        if (getTotalIgnorePDR() > 0) {
            flag |= EquipStats.無視防禦.getValue(); //無視怪物增加百分比
        }
        return flag;
    }

    /*
     * 裝備的基礎屬性
     */
    public int getEquipBaseFlag() {
        int flag = 0;
        if (getStr() > 0) {
            flag |= EquipBaseStat.力量.getFlag();
        }
        if (getDex() > 0) {
            flag |= EquipBaseStat.敏捷.getFlag();
        }
        if (getInt() > 0) {
            flag |= EquipBaseStat.智力.getFlag();
        }
        if (getLuk() > 0) {
            flag |= EquipBaseStat.幸運.getFlag();
        }
        if (getHp() > 0) {
            flag |= EquipBaseStat.MaxHP.getFlag();
        }
        if (getMp() > 0) {
            flag |= EquipBaseStat.MaxMP.getFlag();
        }
        if (getPad() > 0) {
            flag |= EquipBaseStat.攻擊力.getFlag();
        }
        if (getMad() > 0) {
            flag |= EquipBaseStat.魔力.getFlag();
        }
        if (getPdd() > 0) {
            flag |= EquipBaseStat.防禦力.getFlag();
        }
        if (getHands() > 0) {
            flag |= EquipBaseStat.靈敏度.getFlag();
        }
        if (getSpeed() > 0) {
            flag |= EquipBaseStat.移動速度.getFlag();
        }
        if (getJump() > 0) {
            flag |= EquipBaseStat.跳躍力.getFlag();
        }
        return flag;
    }

    /*
     * 裝備星力強化的基礎屬性
     */
    public int getEquipSFBaseFlag() {
        int flag = 0;
        if (getSF_Str() > 0) {
            flag |= EquipBaseStat.力量.getFlag();
        }
        if (getSF_Dex() > 0) {
            flag |= EquipBaseStat.敏捷.getFlag();
        }
        if (getSF_Int() > 0) {
            flag |= EquipBaseStat.智力.getFlag();
        }
        if (getSF_Luk() > 0) {
            flag |= EquipBaseStat.幸運.getFlag();
        }
        if (getSF_Hp() > 0) {
            flag |= EquipBaseStat.MaxHP.getFlag();
        }
        if (getSF_Mp() > 0) {
            flag |= EquipBaseStat.MaxMP.getFlag();
        }
        if (getSF_Pad() > 0) {
            flag |= EquipBaseStat.攻擊力.getFlag();
        }
        if (getSF_Mad() > 0) {
            flag |= EquipBaseStat.魔力.getFlag();
        }
        if (getSF_Pdd() > 0) {
            flag |= EquipBaseStat.防禦力.getFlag();
        }
        if (getSF_Hands() > 0) {
            flag |= EquipBaseStat.靈敏度.getFlag();
        }
        if (getSF_Speed() > 0) {
            flag |= EquipBaseStat.移動速度.getFlag();
        }
        if (getSF_Jump() > 0) {
            flag |= EquipBaseStat.跳躍力.getFlag();
        }
        return flag;
    }

    /*
     * 裝備的特殊狀態
     */
    public int getEquipSpecialFlag() {
        int flag = 0;
        if (getRestUpgradeCount() > 0) {
            flag |= EquipSpecialStat.可使用捲軸次數.getFlag();
        }
        if (getCurrentUpgradeCount() > 0) {
            flag |= EquipSpecialStat.捲軸強化次數.getFlag();
        }
        if (getCAttribute() > 0) {
            flag |= EquipSpecialStat.狀態.getFlag();
        }
        if (getIncSkill() > 0) {
            flag |= EquipSpecialStat.裝備技能.getFlag();
        }
        if (isSealedEquip()) {
            if (getSealedLevel() > 0) {
                flag |= EquipSpecialStat.裝備等級.getFlag();
            }
            if (getSealedExp() > 0) {
                flag |= EquipSpecialStat.裝備經驗.getFlag();
            }
        } else {
            if (getEquipLevel() > 0) {
                flag |= EquipSpecialStat.裝備等級.getFlag();
            }
            if (getExpPercentage() > 0) {
                flag |= EquipSpecialStat.裝備經驗.getFlag();
            }
        }
        if (getDurability() > 0) {
            flag |= EquipSpecialStat.耐久度.getFlag();
        }
        if (getTotalHammer() > 0) {
            flag |= EquipSpecialStat.鎚子.getFlag();
        }
        if (getDownLevel() > 0) {
            flag |= EquipSpecialStat.套用等級減少.getFlag();
        }
        if (getEnchantBuff() > 0) {
            flag |= EquipSpecialStat.ENHANCT_BUFF.getFlag();
        }
        if (getiIncReq() > 0) {
            flag |= EquipSpecialStat.REQUIRED_LEVEL.getFlag(); //穿戴裝備的等級要求提高
        }
        if (getYggdrasilWisdom() > 0) {
            flag |= EquipSpecialStat.YGGDRASIL_WISDOM.getFlag();
        }
        if (getFinalStrike()) {
            flag |= EquipSpecialStat.FINAL_STRIKE.getFlag(); //最終一擊卷軸成功
        }
        if (getTotalBossDamage() > 0) {
            flag |= EquipSpecialStat.BOSS傷.getFlag(); //BOSS傷害增加百分比
        }
        if (getTotalIgnorePDR() > 0) {
            flag |= EquipSpecialStat.無視防禦.getFlag(); //無視怪物增加百分比
        }
        if (getTotalTotalDamage() > 0) {
            flag |= EquipSpecialStat.總傷害.getFlag(); //裝備總傷害百分比增加
        }
        if (getTotalAllStat() > 0) {
            flag |= EquipSpecialStat.全屬性.getFlag(); //裝備所有屬性百分比增加
        }
        flag |= EquipSpecialStat.剪刀次數.getFlag(); //可以使用剪刀多少次 默認必須
        if (getFlameFlag() != 0) {
            flag |= EquipSpecialStat.輪迴星火.getFlag();
        }
        flag |= EquipSpecialStat.星力強化.getFlag();
        return flag;
    }

    public void setLockPotential(int slot, short id) {
        lockSlot = slot;
        lockId = id;
    }

    public int getLockSlot() {
        return lockSlot;
    }

    public int getLockId() {
        return lockId;
    }

    public boolean isSealedEquip() {
        return GameConstants.isSealedEquip(getItemId());
    }

    public byte getSealedLevel() {
        return sealedLevel;
    }

    public void setSealedLevel(byte level) {
        sealedLevel = level;
    }

    public void gainSealedExp(long gain) {
        sealedExp += gain;
    }

    public long getSealedExp() {
        return sealedExp;
    }

    public void setSealedExp(long exp) {
        sealedExp = exp;
    }

    public short getSoulOptionID() {
        return soulOptionID;
    }

    public void setSoulOptionID(short soulname) {
        this.soulOptionID = soulname;
    }

    public short getSoulSocketID() {
        return soulSocketID;
    }

    public void setSoulSocketID(short soulenchanter) {
        this.soulSocketID = soulenchanter;
    }

    public short getSoulOption() {
        return soulOption;
    }

    public void setSoulOption(short soulpotential) {
        this.soulOption = soulpotential;
    }

    public int getSoulSkill() {
        return soulSkill;
    }

    public void setSoulSkill(int skillid) {
        this.soulSkill = skillid;
    }

    public int getiIncReq() {
        return iIncReq;
    }

    public NirvanaFlame getNirvanaFlame() {
        return nirvanaFlame;
    }

    public void setNirvanaFlame(NirvanaFlame nirvanaFlame) {
        this.nirvanaFlame = nirvanaFlame;
        nirvanaFlame.resetEquipExStats(this);
    }

    public long getFlameFlag() {
        return nirvanaFlame.getFlag();
    }

    public void setFlameFlag(long flag) {
        nirvanaFlame.setFlag(flag);
        nirvanaFlame.resetEquipExStats(this);
    }

    public StarForce getStarForce() {
        return starForce;
    }

    public void setStarForce(StarForce starForce) {
        this.starForce = starForce;
        starForce.resetEquipStats(this);
    }

    /*
     * 星級
     */
    public byte getStarForceLevel() {
        return starForce.getLevel();
    }

    public void setStarForceLevel(byte level) {
        if (starForce.getLevel() <= level) {
            setFailCount(0);
        }
        starForce.setLevel(level);
        starForce.resetEquipStats(this);
    }

    public byte getEnhance() {
        return getStarForceLevel();
    }

    public void setEnhance(byte level) {
        setStarForceLevel(level);
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public short getARC() {
        return ARC;
    }

    public void setARC(short ARC) {
        this.ARC = ARC;
    }

    public int getArcExp() {
        return ARCExp;
    }

    public void setARCExp(int ARCExp) {
        this.ARCExp = ARCExp;
    }

    public short getARCLevel() {
        return ARCLevel;
    }

    public void setARCLevel(short ARCLevel) {
        this.ARCLevel = ARCLevel;
    }

    public int getReqJob() {
        return MapleItemInformationProvider.getInstance().getReqJob(getItemId());
    }

    public int getReqSpecJob() {
        return MapleItemInformationProvider.getInstance().getReqSpecJob(getItemId());
    }

    public void transmit(int itemID) {
        transmit(itemID, 0);
    }

    public void transmit(int itemID, int jobId) {
        transmit(itemID, jobId, true);
    }

    public void transmit(int itemID, int jobId, boolean resetStat) {
        if (!ItemConstants.類型.裝備(itemID)) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Equip normalEquip = ii.getEquipById(getItemId());
        if (normalEquip == null) {
            return;
        }
        short addStr = (short) (str - normalEquip.str);
        short addDex = (short) (dex - normalEquip.dex);
        short addInt = (short) (_int - normalEquip._int);
        short addLuk = (short) (luk - normalEquip.luk);
        short addHp = (short) (hp - normalEquip.hp);
        short addMp = (short) (mp - normalEquip.mp);
        short addMad = (short) (mad - normalEquip.mad);
        short addMdd = (short) (mdd - normalEquip.mdd);
        short addPad = (short) (pad - normalEquip.pad);
        short addPdd = (short) (pdd - normalEquip.pdd);
        short addAcc = (short) (acc - normalEquip.acc);
        short addAvoid = (short) (avoid - normalEquip.avoid);
        short addHands = (short) (hands - normalEquip.hands);
        short addSpeed = (short) (speed - normalEquip.speed);
        short addJump = (short) (jump - normalEquip.jump);
        normalEquip = ii.getEquipById(itemID);
        if (normalEquip == null) {
            return;
        }
        int srcReqJob = getReqJob();
        setItemId(itemID);
        int dstReqJob = normalEquip.getReqJob();
        if (srcReqJob != dstReqJob && srcReqJob > 0 && dstReqJob > 0) {
            final int 劍士 = 0x1;
            final int 法師 = 0x2;
            final int 弓箭手 = 0x4;
            final int 盜賊 = 0x8;
            final int 海盜 = 0x10;
            short tempValue;
            if ((srcReqJob & 法師) != (dstReqJob & 法師)) {
                tempValue = addPad;
                addPad = addMad;
                addMad = tempValue;
                nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iPAD, NirvanaFlame.EquipExFlag.FLAGEx_iMAD);
                int pot;
                Map<String, Integer> data;
                for (int i = 1; i <= 6; i++) {
                    pot = getPotential(i > 3 ? i - 3 : i, i > 3);
                    if (pot >= 60000 || pot <= 0) {
                        continue;
                    } else if (pot == 32051 || pot == 32052 || pot == 42051 || pot == 42052) {
                        setPotential(pot + 2, i > 3 ? i - 3 : i, i > 3);
                    } else if (pot == 32053 || pot == 32054 || pot == 42053 || pot == 42054) {
                        setPotential(pot - 2, i > 3 ? i - 3 : i, i > 3);
                    } else {
                        data = ii.getPotentialInfo(pot).get(0).data;
                        if (data.containsKey("incPAD") || data.containsKey("incPADr") || data.containsKey("incPADlv")) {
                            setPotential(pot + 1, i > 3 ? i - 3 : i, i > 3);
                        } else if (data.containsKey("incMAD") || data.containsKey("incMADr") || data.containsKey("incMADlv")) {
                            setPotential(pot - 1, i > 3 ? i - 3 : i, i > 3);
                        }
                    }
                }
            }
            Pair<String[], String[]> statName = new Pair<>(null, null);
            if ((srcReqJob & 劍士) != 0) {
                statName.left = new String[]{"STR", "DEX"};
            } else if ((srcReqJob & 法師) != 0) {
                statName.left = new String[]{"INT", "LUK"};
            } else if ((srcReqJob & 弓箭手) != 0) {
                statName.left = new String[]{"DEX", "STR"};
            } else if ((srcReqJob & 盜賊) != 0) {
                statName.left = new String[]{"LUK", "DEX"};
            } else if ((srcReqJob & 海盜) != 0) {
                boolean isDexPirate = MapleWeapon.靈魂射手.check(getItemId()) || MapleWeapon.火槍.check(getItemId()) || JobConstants.isDexPirate(getReqSpecJob() * 100);
                if (!isDexPirate && !ItemConstants.類型.武器(getItemId()) && !ItemConstants.類型.副手(getItemId())) {
                    isDexPirate = JobConstants.isDexPirate(jobId);
                }
                if (isDexPirate) {
                    statName.left = new String[]{"DEX", "STR"};
                } else {
                    statName.left = new String[]{"STR", "DEX"};
                }
            }
            if ((dstReqJob & 劍士) != 0) {
                statName.right = new String[]{"STR", "DEX"};
            } else if ((dstReqJob & 法師) != 0) {
                statName.right = new String[]{"INT", "LUK"};
            } else if ((dstReqJob & 弓箭手) != 0) {
                statName.right = new String[]{"DEX", "STR"};
            } else if ((dstReqJob & 盜賊) != 0) {
                statName.right = new String[]{"LUK", "DEX"};
            } else if ((dstReqJob & 海盜) != 0) {
                boolean isDexPirate = MapleWeapon.靈魂射手.check(itemID) || MapleWeapon.火槍.check(itemID) || JobConstants.isDexPirate(normalEquip.getReqSpecJob() * 100);
                if (!isDexPirate && !ItemConstants.類型.武器(itemID) && !ItemConstants.類型.副手(itemID)) {
                    isDexPirate = JobConstants.isDexPirate(jobId);
                }
                if (isDexPirate) {
                    statName.right = new String[]{"DEX", "STR"};
                } else {
                    statName.right = new String[]{"STR", "DEX"};
                }
            }

            if (statName.left != null && statName.right != null && !(statName.left[0].equals(statName.right[0]) && statName.left[1].equals(statName.right[1]))) {
                if (("STR".equals(statName.left[0]) || "DEX".equals(statName.left[0])) && ("STR".equals(statName.right[0]) || "DEX".equals(statName.right[0]))) {
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iSTR, NirvanaFlame.EquipExFlag.FLAGEx_iDEX);
                    tempValue = addStr;
                    addStr = addDex;
                    addDex = tempValue;
                    statName.left[1] = null;
                } else if (("STR".equals(statName.left[0]) || "INT".equals(statName.left[0])) && ("STR".equals(statName.right[0]) || "INT".equals(statName.right[0]))) {
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iSTR, NirvanaFlame.EquipExFlag.FLAGEx_iINT);
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iDEX, NirvanaFlame.EquipExFlag.FLAGEx_iLUK);
                    tempValue = addStr;
                    addStr = addInt;
                    addInt = tempValue;
                    tempValue = addDex;
                    addDex = addLuk;
                    addLuk = tempValue;
                } else if (("STR".equals(statName.left[0]) || "LUK".equals(statName.left[0])) && ("STR".equals(statName.right[0]) || "LUK".equals(statName.right[0]))) {
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iSTR, NirvanaFlame.EquipExFlag.FLAGEx_iLUK);
                    tempValue = addStr;
                    addStr = addLuk;
                    addLuk = tempValue;
                    statName.left[1] = null;
                } else if (("DEX".equals(statName.left[0]) || "INT".equals(statName.left[0])) && ("DEX".equals(statName.right[0]) || "INT".equals(statName.right[0]))) {
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iDEX, NirvanaFlame.EquipExFlag.FLAGEx_iINT);
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iSTR, NirvanaFlame.EquipExFlag.FLAGEx_iLUK);
                    tempValue = addDex;
                    addDex = addInt;
                    addInt = tempValue;
                    tempValue = addStr;
                    addStr = addLuk;
                    addLuk = tempValue;
                } else if (("DEX".equals(statName.left[0]) || "LUK".equals(statName.left[0])) && ("DEX".equals(statName.right[0]) || "LUK".equals(statName.right[0]))) {
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iDEX, NirvanaFlame.EquipExFlag.FLAGEx_iLUK);
                    tempValue = addDex;
                    addDex = addLuk;
                    addLuk = tempValue;
                    if ("DEX".equals(statName.left[0])) {
                        nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iSTR, NirvanaFlame.EquipExFlag.FLAGEx_iDEX);
                        tempValue = addStr;
                        addStr = addDex;
                        addDex = tempValue;
                        statName.left[1] = "DEX";
                    } else {
                        nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iSTR, NirvanaFlame.EquipExFlag.FLAGEx_iLUK);
                        tempValue = addStr;
                        addStr = addLuk;
                        addLuk = tempValue;
                        statName.left[1] = "LUK";
                    }
                    statName.right[1] = "STR";
                } else if (("INT".equals(statName.left[0]) || "LUK".equals(statName.left[0])) && ("INT".equals(statName.right[0]) || "LUK".equals(statName.right[0]))) {
                    nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iINT, NirvanaFlame.EquipExFlag.FLAGEx_iLUK);
                    tempValue = addInt;
                    addInt = addLuk;
                    addLuk = tempValue;
                    if ("INT".equals(statName.left[0])) {
                        nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iINT, NirvanaFlame.EquipExFlag.FLAGEx_iDEX);
                        tempValue = addInt;
                        addInt = addDex;
                        addDex = tempValue;
                    } else {
                        nirvanaFlame.transmitStat(NirvanaFlame.EquipExFlag.FLAGEx_iLUK, NirvanaFlame.EquipExFlag.FLAGEx_iDEX);
                        tempValue = addLuk;
                        addLuk = addDex;
                        addDex = tempValue;
                    }
                    statName.left[1] = statName.left[0];
                    statName.right[1] = "DEX";
                }
                for (int i = 0; i < 2; i++) {
                    if (statName.left[i] == null) {
                        continue;
                    }
                    int moveValue = 0;
                    switch (statName.left[i]) {
                        case "STR":
                            moveValue = 4;
                            break;
                        case "DEX":
                            moveValue = 3;
                            break;
                        case "INT":
                            moveValue = 2;
                            break;
                        case "LUK":
                            moveValue = 1;
                            break;
                    }
                    switch (statName.right[i]) {
                        case "STR":
                            moveValue -= 4;
                            break;
                        case "DEX":
                            moveValue -= 3;
                            break;
                        case "INT":
                            moveValue -= 2;
                            break;
                        case "LUK":
                            moveValue -= 1;
                            break;
                    }
                    if (moveValue != 0) {
                        int pot;
                        Map<String, Integer> data;
                        for (int j = 1; j <= 6; j++) {
                            pot = getPotential(j > 3 ? j - 3 : j, j > 3);
                            switch (pot) {
                                case 30047: // STR +9%
                                    pot = 30041; // STR +10%
                                    break;
                                case 30048: // INT +9%
                                    pot = 30043; // INT +10%
                                    break;
                                case 40047: // DEX +12%
                                    pot = 40042; // DEX +13%
                                    break;
                                case 40048: // LUK +12%
                                    pot = 40044; // LUK +13%
                                    break;
                            }
                            if (pot >= 70000 || pot <= 0) {
                                continue;
                            } else {
                                data = ii.getPotentialInfo(pot).get(0).data;
                                if (data.containsKey("inc" + statName.left[i]) || data.containsKey("inc" + statName.left[i] + "r") || data.containsKey("inc" + statName.left[i] + "lv")) {
                                    setPotential(pot + moveValue, j > 3 ? j - 3 : j, j > 3);
                                } else if (data.containsKey("inc" + statName.right[i]) || data.containsKey("inc" + statName.right[i] + "r") || data.containsKey("inc" + statName.right[i] + "lv")) {
                                    setPotential(pot - moveValue, j > 3 ? j - 3 : j, j > 3);
                                }
                            }
                        }
                    }
                }
            }
        }

        str = (short) (normalEquip.str + addStr);
        dex = (short) (normalEquip.dex + addDex);
        _int = (short) (normalEquip._int + addInt);
        luk = (short) (normalEquip.luk + addLuk);
        hp = (short) (normalEquip.hp + addHp);
        mp = (short) (normalEquip.mp + addMp);
        mad = (short) (normalEquip.mad + addMad);
        mdd = (short) (normalEquip.mdd + addMdd);
        pad = (short) (normalEquip.pad + addPad);
        pdd = (short) (normalEquip.pdd + addPdd);
        acc = (short) (normalEquip.acc + addAcc);
        avoid = (short) (normalEquip.avoid + addAvoid);
        hands = (short) (normalEquip.hands + addHands);
        speed = (short) (normalEquip.speed + addSpeed);
        jump = (short) (normalEquip.jump + addJump);

        if (resetStat) {
            nirvanaFlame.resetEquipExStats(this);
            starForce.resetEquipStats(this);
        }
    }

    public void setMvpEquip(boolean isMvpEquip) {
        this.mvpEquip = isMvpEquip;
    }

    public boolean isMvpEquip() {
        return isMvpEquip(true);
    }

    public boolean isMvpEquip(boolean checkPermission) {
        return (!checkPermission || Auth.checkPermission("MVPEquip")) && this.mvpEquip;
    }

    public short getAut() {
        return aut;
    }

    public void setAut(short aut) {
        this.aut = aut;
    }

    public int getAutExp() {
        return autExp;
    }

    public void setAutExp(int autExp) {
        this.autExp = autExp;
    }

    public short getAutLevel() {
        return autLevel;
    }

    public void setAutLevel(short autLevel) {
        this.autLevel = autLevel;
    }

    public enum ScrollResult {

        失敗, 成功, 消失
    }

    @Override
    public void encode(OutPacket outPacket) {
        super.encode(outPacket);

        // GW_ItemSlotEquip_RawEncode
        boolean hasUniqueId = encodeBaseRaw(outPacket);
        boolean isCashItem = MapleItemInformationProvider.getInstance().isCash(getItemId());

        outPacket.encodeByte(0); // 218 sub_2BBE1E0 , v255 sub_144A9F3D0

        encodeEquipBase(outPacket);

        // 擁有者名字
        outPacket.encodeString(isMvpEquip() ? "ＭＶＰ" : getOwner(), 15);
        // 潛能等級 17 = 特殊rare, 18 = 稀有epic, 19 = 罕見unique, 20 = 傳說legendary, potential flags. special grade is 14 but it crashes
        outPacket.encodeByte((getState(true) > 0 && getState(true) < 17) ? (getState(false) | 0x20) : getState(false));
        // 裝備星級
        outPacket.encodeByte(getStarForceLevel());
        // 潛在能力
        for (int i = 1; i <= 3; ++i) {
            outPacket.encodeShort((getPotential(i, false) <= 0) ? 0 : getPotential(i, false));
        }
        // 附加潛能
        for (int j = 1; j <= 3; ++j) {
            outPacket.encodeShort((getState(true) > 0 && getState(true) < 17) ? ((j == 1) ? getState(true) : 0) : getPotential(j, true));
        }
        // 鐵砧
        outPacket.encodeShort(isCashItem ? 0 : getItemSkin() % 10000);
        /*
         * Alien Stone FLAG
         * 0x01 = 你可以在這件物品上鑲入星岩。
         * 0x03 = 你可以在這件物品上鑲入星岩。 有個鑲嵌的孔 未鑲嵌
         * 0x13 = 有1個插孔 已經鑲嵌東西
         */
        outPacket.encodeShort(getSocketState()); //V.101新增
        //outPacket.encodeShort(getSocket1() % 10000); //V.102新增 鑲嵌寶石1 ID: 3281 = 全屬性+4%
        //outPacket.encodeShort(getSocket2() % 10000); //V.102新增 鑲嵌寶石2
        //outPacket.encodeShort(getSocket3() % 10000); //V.102新增 鑲嵌寶石3

        //outPacket.encodeInt(0);//equip.getMixColor() v260 remove

        if (!hasUniqueId) {
            outPacket.encodeLong(getSN());
        }

        // GW_CashItemOption_Encode
        outPacket.encodeLong(0); // liCashItemSN
        outPacket.encodeFT(FileTime.fromType(FileTime.Type.ZERO_TIME)); //getTime(equip.getFtExpireDate())
        outPacket.encodeInt(0);//equip.getCSGrade()
        for (int i = 0; i < 3; ++i) {
            outPacket.encodeInt(0);//equip.getAnOption(i)
        }

        // v260 add
        outPacket.encodeLong(0);
        outPacket.encodeInt(0);
        // v262 add
        outPacket.encodeInt(0);

        // 靈魂寶珠
        outPacket.encodeShort(getSoulOptionID());
        // 靈魂捲軸
        outPacket.encodeShort(getSoulSocketID());
        // 靈魂潛能
        outPacket.encodeShort(getSoulOption());
        // 秘法符文
        if (ItemConstants.類型.秘法符文(getItemId())) {
            outPacket.encodeShort(getARC());
            outPacket.encodeInt(getArcExp());
            outPacket.encodeShort(getARCLevel());
        }
        if (ItemConstants.類型.真實符文(getItemId())) {
            outPacket.encodeShort(getAut());
            outPacket.encodeInt(getAutExp());
            outPacket.encodeShort(getAutLevel());
        }

        // 220 sub_587F80 // v255 sub_1403B1B10 // v267.2 sub_140549660
        outPacket.encodeShort(-1);
        outPacket.encodeFT(FileTime.fromType(FileTime.Type.MAX_TIME));
        outPacket.encodeFT(FileTime.fromType(FileTime.Type.ZERO_TIME));
        outPacket.encodeFT(FileTime.fromType(FileTime.Type.MAX_TIME));
        // v267.2 sub_140549700
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        // 機器人
        if (ItemConstants.類型.機器人(getItemId())) {
            if (getAndroid() != null) {
                getAndroid().encodeAndroidLook(outPacket);
            } else {
                outPacket.encodeArr(new byte[28]);
            }
        }
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);

        int sfFlag;
        int unkFlag;
        if (getPosition() < 0 && EnhanceResultType.EQUIP_MARK.check(getEnchantBuff())) {
            sfFlag = 0;
            unkFlag = 0;
        } else {
            sfFlag = getEquipSFBaseFlag();
            unkFlag = 0;
        }

        //TODO:不知道甚麼強化加的基礎屬性(sub_140409230)
        encodeEquipCalcStat(outPacket, unkFlag, 2);

        outPacket.encodeByte(sfFlag > 0);
        if (sfFlag > 0) {
            //TODO:應該是星力強化加的基礎屬性(sub_140409230)
            encodeEquipCalcStat(outPacket, sfFlag, 1);
        }
        // 點商鐵砧
        outPacket.encodeInt(isCashItem ? getItemSkin() : 0);
        outPacket.encodeFT(FileTime.fromType(FileTime.Type.ZERO_TIME));
    }

    public void encodeEquipBase(OutPacket outPacket) {
        // GW_ItemSlotEquipBase__Encode
        int baseFlag;
        int exFlag;
        if (getPosition() < 0 && EnhanceResultType.EQUIP_MARK.check(getEnchantBuff())) {
            baseFlag = 0;
            exFlag = 0;
        } else {
            baseFlag = getEquipBaseFlag();
            exFlag = getEquipSpecialFlag();
        }

        // 基礎屬性
        encodeEquipCalcStat(outPacket, baseFlag, 0);

        // 特殊裝備屬性
        outPacket.encodeInt(exFlag);
        if (EquipSpecialStat.可使用捲軸次數.check(exFlag)) {
            outPacket.encodeByte(getRestUpgradeCount());
        }
        if (EquipSpecialStat.捲軸強化次數.check(exFlag)) {
            outPacket.encodeByte(getCurrentUpgradeCount());
        }
        if (EquipSpecialStat.狀態.check(exFlag)) {
            outPacket.encodeInt(getCAttribute());
        }
        if (EquipSpecialStat.裝備技能.check(exFlag)) {
            outPacket.encodeByte(getIncSkill() > 0);
        }
        if (EquipSpecialStat.裝備等級.check(exFlag)) {
            if (isSealedEquip()) {
                outPacket.encodeByte(getSealedLevel());
            } else {
                outPacket.encodeByte(Math.max(getBaseLevel(), getEquipLevel()));
            }
        }
        if (EquipSpecialStat.裝備經驗.check(exFlag)) {
            if (isSealedEquip()) {
                outPacket.encodeLong(getSealedExp());
            } else {
                outPacket.encodeLong(getExpPercentage() * 100000); // 10000000 = 100% 好像現在是20000是滿經驗 V.110修改 以前是Int
            }
        }
        if (EquipSpecialStat.耐久度.check(exFlag)) {
            outPacket.encodeInt(getDurability());
        }
        if (EquipSpecialStat.鎚子.check(exFlag)) {
            outPacket.encodeShort(getViciousHammer()); // 黃金鐵鎚
            outPacket.encodeShort(getPlatinumHammer()); // 白金鎚子
        }
        /*if (EquipStats.大亂鬥傷害.check(exFlag)) {
            outPacket.encodeShort(getPVPDamage());
        }*/
        if (EquipSpecialStat.套用等級減少.check(exFlag)) {
            outPacket.encodeByte(getDownLevel());
        }
        if (EquipSpecialStat.ENHANCT_BUFF.check(exFlag)) {
            outPacket.encodeShort(getEnchantBuff()); //強化效果
        }
        if (EquipSpecialStat.DURABILITY_SPECIAL.check(exFlag)) {
            outPacket.encodeInt(0);
        }
        if (EquipSpecialStat.REQUIRED_LEVEL.check(exFlag)) {
            outPacket.encodeByte(getiIncReq()); //穿戴裝備的等級要求提高多少級
        }
        if (EquipSpecialStat.YGGDRASIL_WISDOM.check(exFlag)) {
            outPacket.encodeByte(getYggdrasilWisdom());
        }
        if (EquipSpecialStat.FINAL_STRIKE.check(exFlag)) {
            outPacket.encodeByte(getFinalStrike()); //最終一擊卷軸成功
        }
        if (EquipSpecialStat.BOSS傷.check(exFlag)) {
            outPacket.encodeByte(getTotalBossDamage());
        }
        if (EquipSpecialStat.無視防禦.check(exFlag)) {
            outPacket.encodeByte(getTotalIgnorePDR());
        }
        if (EquipSpecialStat.總傷害.check(exFlag)) {
            outPacket.encodeByte(getTotalTotalDamage()); // 裝備總傷害百分比增加
        }
        if (EquipSpecialStat.全屬性.check(exFlag)) {
            outPacket.encodeByte(getTotalAllStat()); // 裝備所有屬性百分比增加
        }
        if (EquipSpecialStat.剪刀次數.check(exFlag)) {
            outPacket.encodeByte(-1); //可以使用剪刀多少次 默認-1 必須發送這個封包 0x0A = 宿命剪刀1次
        }
        if (EquipSpecialStat.輪迴星火.check(exFlag)) {
            outPacket.encodeLong(getFlameFlag());
        }
        if (EquipSpecialStat.星力強化.check(exFlag)) {
            outPacket.encodeInt(256);
        }
    }

    public void encodeEquipCalcStat(OutPacket outPacket, int baseFlag, int type) {
        // GW_ItemSlotEquipCalcStat_Encode
        outPacket.encodeInt(baseFlag);
        if (EquipBaseStat.力量.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getStr() : getSF_Str());
        }
        if (EquipBaseStat.敏捷.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getDex() : getSF_Dex());
        }
        if (EquipBaseStat.智力.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getInt() : getSF_Int());
        }
        if (EquipBaseStat.幸運.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getLuk() : getSF_Luk());
        }
        if (EquipBaseStat.MaxHP.check(baseFlag)) {
            outPacket.encodeShort(ItemConstants.類型.秘法符文(getItemId()) || ItemConstants.類型.真實符文(getItemId()) ? (getTotalHp() / 10) : (getTotalHp()));
        }
        if (EquipBaseStat.MaxMP.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getMp() : getSF_Mp());
        }
        if (EquipBaseStat.攻擊力.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getPad() : getSF_Pad());
        }
        if (EquipBaseStat.魔力.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getMad() : getSF_Mad());
        }
        if (EquipBaseStat.防禦力.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getPdd() : getSF_Pdd());
        }
        if (EquipBaseStat.魔法防禦力.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getMdd() : getSF_Mdd());
        }
        if (EquipBaseStat.靈敏度.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getHands() : getSF_Hands());
        }
        if (EquipBaseStat.移動速度.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getSpeed() : getSF_Speed());
        }
        if (EquipBaseStat.跳躍力.check(baseFlag)) {
            outPacket.encodeShort(type == 0 ? getJump() : getSF_Jump());
        }
    }

}
