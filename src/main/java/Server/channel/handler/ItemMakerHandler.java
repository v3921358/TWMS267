package Server.channel.handler;

import Client.*;
import Client.inventory.*;
import Client.skills.SkillFactory;
import Client.skills.SkillFactory.CraftingEntry;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.SkillConstants;
import Config.constants.enums.UserChatMessageType;
import Net.server.ItemMakerFactory;
import Net.server.ItemMakerFactory.GemCreateEntry;
import Net.server.ItemMakerFactory.ItemMakerCreateEntry;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.buffs.MapleStatEffect;
import Net.server.maps.MapleExtractor;
import Net.server.maps.MapleReactor;
import Net.server.quest.MapleQuest;
import Packet.EffectPacket;
import Packet.MaplePacketCreator;
import Server.world.WorldBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketReader;

import java.util.*;
import java.util.Map.Entry;

public class ItemMakerHandler {

    private static final Logger log = LoggerFactory.getLogger(ItemMakerHandler.class);
    private static final Map<String, Integer> craftingEffects = new HashMap<>();

    static {
        craftingEffects.put("Effect/BasicEff.img/professions/herbalism", 92000000);
        craftingEffects.put("Effect/BasicEff.img/professions/mining", 92010000);
        craftingEffects.put("Effect/BasicEff.img/professions/herbalismExtract", 92000000);
        craftingEffects.put("Effect/BasicEff.img/professions/miningExtract", 92010000);

        craftingEffects.put("Effect/BasicEff.img/professions/equip_product", 92020000);
        craftingEffects.put("Effect/BasicEff.img/professions/acc_product", 92030000);
        craftingEffects.put("Effect/BasicEff.img/professions/alchemy", 92040000);
    }

    /*
     * 使用鍛造技能
     */
    public static void ItemMaker(MaplePacketReader slea, MapleClient c) {
        int makerType = slea.readInt();
        switch (makerType) {
            case 1: { // Gem
                int toCreate = slea.readInt();
                if (ItemConstants.類型.強化寶石(toCreate)) {
                    GemCreateEntry gem = ItemMakerFactory.getInstance().getGemInfo(toCreate);
                    if (gem == null) {
                        return;
                    }
                    if (!hasSkill(c, gem.getReqSkillLevel())) {
                        return; // H4x
                    }
                    if (c.getPlayer().getMeso() < gem.getCost()) {
                        return; // H4x
                    }
                    int randGemGiven = getRandomGem(gem.getRandomReward());
                    if (c.getPlayer().getInventory(ItemConstants.getInventoryType(randGemGiven)).isFull()) {
                        return; // We'll do handling for this later
                    }
                    int taken = checkRequiredNRemove(c, gem.getReqRecipes());
                    if (taken == 0) {
                        return; // We'll do handling for this later
                    }
                    c.getPlayer().gainMeso(-gem.getCost(), false);
                    MapleInventoryManipulator.addById(c, randGemGiven, taken == randGemGiven ? 9 : 1, "Made by Gem " + toCreate + " on " + DateUtil.getCurrentDate()); // Gem is always 1
                    c.announce(EffectPacket.ItemMaker_Success());
                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), EffectPacket.ItemMaker_Success_3rdParty(c.getPlayer().getId()), false);
                } else if (ItemConstants.isOtherGem(toCreate)) {
                    //non-gems that are gems
                    //stim and numEnchanter always 0
                    GemCreateEntry gem = ItemMakerFactory.getInstance().getGemInfo(toCreate);
                    if (gem == null) {
                        return;
                    }
                    if (!hasSkill(c, gem.getReqSkillLevel())) {
                        return; // H4x
                    }
                    if (c.getPlayer().getMeso() < gem.getCost()) {
                        return; // H4x
                    }
                    if (c.getPlayer().getInventory(ItemConstants.getInventoryType(toCreate)).isFull()) {
                        return; // We'll do handling for this later
                    }
                    if (checkRequiredNRemove(c, gem.getReqRecipes()) == 0) {
                        return; // We'll do handling for this later
                    }
                    c.getPlayer().gainMeso(-gem.getCost(), false);
                    if (ItemConstants.getInventoryType(toCreate, false) == MapleInventoryType.EQUIP) {
                        MapleInventoryManipulator.addbyItem(c, MapleItemInformationProvider.getInstance().getEquipById(toCreate));
                    } else {
                        MapleInventoryManipulator.addById(c, toCreate, 1, "Made by Gem " + toCreate + " on " + DateUtil.getCurrentDate()); // Gem is always 1
                    }
                    c.announce(EffectPacket.ItemMaker_Success());
                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), EffectPacket.ItemMaker_Success_3rdParty(c.getPlayer().getId()), false);
                } else {
                    boolean stimulator = slea.readByte() > 0;
                    int numEnchanter = slea.readInt();
                    ItemMakerCreateEntry create = ItemMakerFactory.getInstance().getCreateInfo(toCreate);
                    if (create == null) {
                        return;
                    }
                    if (numEnchanter > create.getTUC()) {
                        return; // h4x
                    }
                    if (!hasSkill(c, create.getReqSkillLevel())) {
                        return; // H4x
                    }
                    if (c.getPlayer().getMeso() < create.getCost()) {
                        return; // H4x
                    }
                    if (c.getPlayer().getInventory(ItemConstants.getInventoryType(toCreate)).isFull()) {
                        return; // We'll do handling for this later
                    }
                    if (checkRequiredNRemove(c, create.getReqItems()) == 0) {
                        return; // We'll do handling for this later
                    }
                    c.getPlayer().gainMeso(-create.getCost(), false);
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    Equip toGive = ii.getEquipById(toCreate);
                    if (stimulator || numEnchanter > 0) {
                        if (c.getPlayer().haveItem(create.getStimulator(), 1, false, true)) {
                            ii.randomizeStats_Above(toGive);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, create.getStimulator(), 1, false, false);
                        }
                        for (int i = 0; i < numEnchanter; i++) {
                            int enchant = slea.readInt();
                            if (c.getPlayer().haveItem(enchant, 1, false, true)) {
                                Map<String, Integer> stats = ii.getItemBaseInfo(enchant);
                                if (stats != null) {
                                    addEnchantStats(stats, toGive);
                                    MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, enchant, 1, false, false);
                                }
                            }
                        }
                    }
                    if (!stimulator || Randomizer.nextInt(10) != 0) {
                        MapleInventoryManipulator.addbyItem(c, toGive);
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), EffectPacket.ItemMaker_Success_3rdParty(c.getPlayer().getId()), false);
                    } else {
                        c.getPlayer().dropMessage(5, "The item was overwhelmed by the stimulator.");
                    }
                    c.announce(EffectPacket.ItemMaker_Success());
                }
                break;
            }
            case 3: { // Making Crystals
                int etc = slea.readInt();
                if (c.getPlayer().haveItem(etc, 100, false, true)) {
                    MapleInventoryManipulator.addById(c, getCreateCrystal(etc), 1, "Made by Maker " + etc + " on " + DateUtil.getCurrentDate());
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, etc, 100, false, false);
                    c.announce(EffectPacket.ItemMaker_Success());
                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), EffectPacket.ItemMaker_Success_3rdParty(c.getPlayer().getId()), false);
                }
                break;
            }
            case 4: { // Disassembling EQ.
                int itemId = slea.readInt();
                c.getPlayer().updateTick(slea.readInt());
                short slot = (short) slea.readInt();
                Item toUse = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
                if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
                    return;
                }
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (!ii.isDropRestricted(itemId) && !ii.isAccountShared(itemId)) {
                    int[] toGive = getCrystal(itemId, ii.getReqLevel(itemId));
                    MapleInventoryManipulator.addById(c, toGive[0], toGive[1], "Made by disassemble " + itemId + " on " + DateUtil.getCurrentDate());
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, slot, (byte) 1, false);
                }
                c.announce(EffectPacket.ItemMaker_Success());
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), EffectPacket.ItemMaker_Success_3rdParty(c.getPlayer().getId()), false);
                break;
            }
        }
    }

    private static int getCreateCrystal(int etc) {
        int itemid;
        short level = MapleItemInformationProvider.getInstance().getItemMakeLevel(etc);
        if (level >= 31 && level <= 50) {
            itemid = 4260000; //下等怪物結晶C - 31級到50級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 51 && level <= 60) {
            itemid = 4260001; //下等怪物結晶B - 51級到60級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 61 && level <= 70) {
            itemid = 4260002; //下等怪物結晶A - 61級到70級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 71 && level <= 80) {
            itemid = 4260003; //中等怪物結晶C - 71級到80級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 81 && level <= 90) {
            itemid = 4260004; //中等怪物結晶B - 81級到90級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 91 && level <= 100) {
            itemid = 4260005; //中等怪物結晶A - 91級到100級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 101 && level <= 110) {
            itemid = 4260006; //高等怪物結晶C - 101級到110級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 111 && level <= 120) {
            itemid = 4260007; //高等怪物結晶B - 111級到120級之間怪物的戰利品聚集而成的結晶。
        } else if (level >= 121) {
            itemid = 4260008; //高等怪物結晶A - 121級以上怪物的戰利品聚集而成的結晶。
        } else {
            throw new RuntimeException("Invalid Item Maker id");
        }
        return itemid;
    }

    private static int[] getCrystal(int itemid, int level) {
        int[] all = new int[2];
        all[0] = -1;
        if (level >= 31 && level <= 50) {
            all[0] = 4260000;
        } else if (level >= 51 && level <= 60) {
            all[0] = 4260001;
        } else if (level >= 61 && level <= 70) {
            all[0] = 4260002;
        } else if (level >= 71 && level <= 80) {
            all[0] = 4260003;
        } else if (level >= 81 && level <= 90) {
            all[0] = 4260004;
        } else if (level >= 91 && level <= 100) {
            all[0] = 4260005;
        } else if (level >= 101 && level <= 110) {
            all[0] = 4260006;
        } else if (level >= 111 && level <= 120) {
            all[0] = 4260007;
        } else if (level >= 121 && level <= 200) {
            all[0] = 4260008;
        } else {
            throw new RuntimeException("Invalid Item Maker type" + level);
        }
        if (ItemConstants.類型.武器(itemid) || ItemConstants.類型.套服(itemid)) {
            all[1] = Randomizer.rand(5, 11);
        } else {
            all[1] = Randomizer.rand(3, 7);
        }
        return all;
    }

    private static void addEnchantStats(Map<String, Integer> stats, Equip item) {
        Integer s = stats.get("incPAD");
        if (s != null && s != 0) {
            item.setPad((short) (item.getPad() + s));
        }
        s = stats.get("incMAD");
        if (s != null && s != 0) {
            item.setMad((short) (item.getMad() + s));
        }
        s = stats.get("incACC");
        if (s != null && s != 0) {
            item.setAcc((short) (item.getAcc() + s));
        }
        s = stats.get("incEVA");
        if (s != null && s != 0) {
            item.setAvoid((short) (item.getAvoid() + s));
        }
        s = stats.get("incSpeed");
        if (s != null && s != 0) {
            item.setSpeed((short) (item.getSpeed() + s));
        }
        s = stats.get("incJump");
        if (s != null && s != 0) {
            item.setJump((short) (item.getJump() + s));
        }
        s = stats.get("MaxHP");
        if (s != null && s != 0) {
            item.setHp((short) (item.getHp() + s));
        }
        s = stats.get("MaxMP");
        if (s != null && s != 0) {
            item.setMp((short) (item.getMp() + s));
        }
        s = stats.get("incSTR");
        if (s != null && s != 0) {
            item.setStr((short) (item.getStr() + s));
        }
        s = stats.get("incDEX");
        if (s != null && s != 0) {
            item.setDex((short) (item.getDex() + s));
        }
        s = stats.get("incINT");
        if (s != null && s != 0) {
            item.setInt((short) (item.getInt() + s));
        }
        s = stats.get("incLUK");
        if (s != null && s != 0) {
            item.setLuk((short) (item.getLuk() + s));
        }
        s = stats.get("randOption");
        if (s != null && s != 0) {
            final int ma = item.getPad(), wa = item.getMad();
            if (wa > 0) {
                item.setPad((short) (Randomizer.nextBoolean() ? (wa + s) : (wa - s)));
            }
            if (ma > 0) {
                item.setMad((short) (Randomizer.nextBoolean() ? (ma + s) : (ma - s)));
            }
        }
        s = stats.get("randStat");
        if (s != null && s != 0) {
            final int str = item.getStr(), dex = item.getDex(), luk = item.getLuk(), int_ = item.getInt();
            if (str > 0) {
                item.setStr((short) (Randomizer.nextBoolean() ? (str + s) : (str - s)));
            }
            if (dex > 0) {
                item.setDex((short) (Randomizer.nextBoolean() ? (dex + s) : (dex - s)));
            }
            if (int_ > 0) {
                item.setInt((short) (Randomizer.nextBoolean() ? (int_ + s) : (int_ - s)));
            }
            if (luk > 0) {
                item.setLuk((short) (Randomizer.nextBoolean() ? (luk + s) : (luk - s)));
            }
        }
    }

    private static int getRandomGem(List<Pair<Integer, Integer>> rewards) {
        int itemid;
        List<Integer> items = new ArrayList<>();
        for (Pair<Integer, Integer> p : rewards) {
            itemid = p.getLeft();
            for (int i = 0; i < p.getRight(); i++) {
                items.add(itemid);
            }
        }
        return items.get(Randomizer.nextInt(items.size()));
    }

    private static int checkRequiredNRemove(MapleClient c, List<Pair<Integer, Integer>> recipe) {
        int itemid = 0;
        for (Pair<Integer, Integer> p : recipe) {
            if (!c.getPlayer().haveItem(p.getLeft(), p.getRight(), false, true)) {
                return 0;
            }
        }
        for (Pair<Integer, Integer> p : recipe) {
            itemid = p.getLeft();
            MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(itemid), itemid, p.getRight(), false, false);
        }
        return itemid;
    }

    private static boolean hasSkill(MapleClient c, int reqlvl) {
        return c.getPlayer().getSkillLevel(SkillFactory.getSkill(SkillConstants.getSkillByJob(1007, c.getPlayer().getJob()))) >= reqlvl;
    }

    /*
     * 使用配方
     */
    public static void UseRecipe(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 251) {
            c.sendEnableActions();
            return;
        }
        if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
    }

    /*
     * 分解機
     */
    public static void MakeExtractor(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        int itemId = slea.readInt();
        if (itemId > 0) {
            int fee = slea.readInt();
            Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
            if (toUse == null || toUse.getQuantity() < 1 || itemId / 1000 != 3049 || fee <= 0 || chr.getExtractor() != null || !chr.getMap().isTown()) {
                c.sendEnableActions();
                return;
            }
            chr.setExtractor(new MapleExtractor(chr, itemId, fee, chr.getFH())); //no clue about time left
            chr.getMap().spawnExtractor(chr.getExtractor());
        } else {
            chr.removeExtractor();
        }
    }

    /*
     * 使用礦物(草藥)背包
     */
    public static void UseBag(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        byte type = slea.readByte();
        Item toUse = chr.getInventory(type).getItem(slot);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (toUse == null || chr.getExtendedSlots(type) == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 265 && itemId / 10000 != 308 && itemId / 10000 != 433 || !ii.itemExists(toUse.getItemId())) {
            c.sendEnableActions();
            return;
        }
        boolean firstTime = false;
        if (toUse.getExtendSlot() < 1 || !ItemAttribute.TradeBlock.check(toUse.getAttribute()) || !ItemAttribute.ColdProof.check(toUse.getAttribute())) {
            int bagMax = 1;
            MapleStatEffect itemEffect = ii.getItemEffect(itemId);
            switch (type) {
                case 3:
                    bagMax = 5;
                    break;
                case 4:
                    if (itemEffect.getType() == 6) {
                    } else {
                        bagMax = 5;
                    }
                    break;
            }
            List<Item> extendedSlots = chr.getExtendedSlots(type);
            List<Short> slots = new LinkedList<>();
            int bagCount = 0;
            for (Item it : extendedSlots) {
                if (it.getExtendSlot() > 0) {
                    slots.add(it.getExtendSlot());
                }
                MapleStatEffect bagEffect = ii.getItemEffect(it.getItemId());
                if (type == 4) {
                    if (itemEffect.getType() > 4 && itemEffect.getType() != 7) {
                        if (bagEffect.getType() == itemEffect.getType()) {
                            bagCount += 1;
                        }
                    } else {
                        bagCount += 1;
                    }
                } else {
                    if (bagEffect.getType() == itemEffect.getType()) {
                        bagCount += 1;
                    }
                }
            }
            if (bagCount >= bagMax) {
                c.getPlayer().dropMessage(1, ii.getName(itemId) + " 道具只能使用" + bagMax + "個.");
                c.sendEnableActions();
                return;
            }
            firstTime = true;
            for (short i = 1; i <= extendedSlots.size() + 1; i++) {
                if (!slots.contains(i)) {
                    toUse.setExtendSlot(i);
                    break;
                }
            }
            if (toUse.getExtendSlot() <= 0) {
                c.getPlayer().dropMessage(1, ii.getName(itemId) + " 開啟時發生未知錯誤");
                c.sendEnableActions();
                return;
            }
            extendedSlots.add(toUse);
            toUse.addAttribute(ItemAttribute.TradeBlock.getValue());
            toUse.addAttribute(ItemAttribute.ColdProof.getValue());
            chr.forceUpdateItem(toUse);
        }
        c.announce(MaplePacketCreator.openBag(toUse.getExtendSlot(), itemId, firstTime));
        c.sendEnableActions();
    }

    /*
     * 進行採礦或者採藥
     */
    public static void StartHarvest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        //its ok if a hacker bypasses this as we do everything in the reactor anyway
        MapleReactor reactor = chr.getMap().getReactorByOid(slea.readInt());
        if (reactor == null || !reactor.isAlive() || reactor.getReactorId() > 200011 || reactor.getPosition().distance(chr.getPosition()) > 100 || chr.getFatigue() >= 200) {
            c.sendEnableActions();
            return;
        }
        MapleQuestStatus marr = chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.HARVEST_TIME));
        if (marr.getCustomData() == null) {
            marr.setCustomData("0");
        }
        long lastTime = Long.parseLong(marr.getCustomData());
        if (lastTime + (5000) > System.currentTimeMillis()) {
            c.announce(MaplePacketCreator.harvestMessage(reactor.getObjectId(), MapleEnumClass.HarvestMsg.HARVEST_UNABLE_COLLECT)); //還無法採集。
        } else {
            marr.setCustomData(String.valueOf(System.currentTimeMillis()));
            c.announce(MaplePacketCreator.harvestMessage(reactor.getObjectId(), MapleEnumClass.HarvestMsg.HARVEST_ACTION_START)); //開始採集
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showHarvesting(chr.getId(), 0), false);
        }
    }

    public static void StopHarvest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {

        final MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(slea.readInt());
        if (reactor != null) {
            reactor.forceHitReactor((byte) 0);
            reactor.setGatherTime(0L);
        }

    }

    /*
     * 專業技術界面
     */
    public static void ProfessionInfo(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        String stat = slea.readMapleAsciiString();
        int array = slea.readInt();
        int mode = slea.readInt();
        int rate;
        switch (stat) {
            // 超技點數
            case "hyper":
            case "hyper_shaman": {
                int chance = 0;
                if (mode == 0 && (array == 28 || array == 30 || array == 33 || array == 36 || array == 38)) {
                    chance = 1;
                } else if (mode == 1 && (array == 28 || array == 32 || array == 38)) {
                    chance = 1;
                }
                c.announce(MaplePacketCreator.updateSpecialStat(stat, array, mode, array <= 40, chance));
                return;
            }
            // 極限屬性點數
            case "incHyperStat": {
                rate = SkillConstants.getHyperAPByLevel(array);
                break;
            }
            // 極限屬性需求點數
            case "needHyperStatLv": {
                rate = SkillConstants.getHyperStatAPNeedByLevel(array);
                break;
            }
            // 名譽等級
            case "honorLeveling": {
                rate = -1;
                break;
            }
            default: {
                if (stat.startsWith("9200") || stat.startsWith("9201")) {
                    rate = 100;
                } else {
                    rate = Math.max(0, 100 - ((array + 1) - chr.getProfessionLevel(Integer.parseInt(stat))) * 20);
                }
                break;
            }
        }
        if (rate != -1) {
            c.announce(MaplePacketCreator.updateSpecialStat(stat, array, mode, true, rate));
        }
    }

    /*
     * 專業技術道具製作動畫效果
     */
    public static void CraftEffect(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr.getMapId() != 910001000 && chr.getMap().getExtractorSize() <= 0) {
            return; //ardent mill
        }
        String effect = slea.readMapleAsciiString();
        Integer profession = craftingEffects.get(effect);
        if (profession != null && (chr.getProfessionLevel(profession) > 0 || (profession == 92040000 && chr.getMap().getExtractorSize() > 0))) {
            int time = slea.readInt();
            if (time > 6000 || time < 3000) {
                time = 4000;
            }
            c.announce(EffectPacket.showOwnEffectUOL(effect, time, effect.endsWith("Extract") ? 1 : 0));
            chr.getMap().broadcastMessage(chr, EffectPacket.showEffectUOL(chr.getId(), effect, time, effect.endsWith("Extract") ? 1 : 0), false);
        }
    }

    /*
     * 專業技術開始製作道具
     */
    public static void CraftMake(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr.getMapId() != 910001000 && chr.getMap().getExtractorSize() <= 0) {
            return; //ardent mill
        }
        int something = slea.readInt(); //no clue what it is, but its between 288 and 305..
        int time = slea.readInt();
        if (time > 6000 || time < 3000) {
            time = 4000;
        }
        chr.getMap().broadcastMessage(MaplePacketCreator.craftMake(chr.getId(), something, time));
    }

    /*
     * 專業技術製作道具完成
     */
    public static void CraftComplete(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (!chr.getCheatTracker().canCraftMake()) {
            chr.sendPolice("系統檢測到您的專業技術製作速度異常，系統對您進行掉線處理。");
            c.sendEnableActions();
            log.info("[作弊] " + chr.getName() + " (等級 " + chr.getLevel() + ") 專業技術製作速度異常。地圖ID: " + chr.getMapId());
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + chr.getName() + " ID: " + chr.getId() + " (等級 " + chr.getLevel() + ") 專業技術製作速度異常。地圖ID: " + chr.getMapId()));
            return;
        }
        slea.readInt();
        int craftID = slea.readInt();
        if (craftID == 0) {
            if (slea.readInt() == 1) {
                craftID = slea.readInt();
            }
        }
        CraftingEntry ce = SkillFactory.getCraft(craftID);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((chr.getMapId() != 910001000 && (craftID != 92049000 || chr.getMap().getExtractorSize() <= 0)) || ce == null || chr.getFatigue() >= 200) {
            return;
        }
        int theLevl = chr.getProfessionLevel((craftID / 10000) * 10000);
        if (theLevl <= 0 && craftID != 92049000) {
            return;
        }
        List<Pair<Integer, Integer>> showItems = new ArrayList<>();
        int toGet = 0, expGain = 0, fatigue = 0, craftType = 2;
        int quantity = 1;
        CraftRanking cr = CraftRanking.GOOD;
        if (craftID == 92028000 || craftID == 92038000) { //分解裝備、飾品
            int itemId = slea.readInt();
            long sn = slea.readLong();
            int reqLevel = ii.getReqLevel(itemId);
            Item item = chr.getInventory(MapleInventoryType.EQUIP).findBySN(sn, itemId);
            craftType = 1;
            if (item == null || chr.getInventory(MapleInventoryType.ETC).isFull()) {
                return;
            }
            if ((theLevl == 0 || theLevl < (reqLevel > 130 ? 6 : ((reqLevel - 30) / 20)))) {
                return;
            }
            toGet = 4031016; //神秘書 - 誰都不知道內容的神秘的書
            quantity = Randomizer.rand(3, ItemConstants.類型.武器(itemId) || ItemConstants.類型.套服(itemId) ? 11 : 7);
            if (reqLevel <= 60) {
                toGet = 4021013; //低級物品結晶 - 分解裝備後獲得的結晶。含有物品原始的力量。用於製作40~60級物品。
            } else if (reqLevel <= 90) {
                toGet = 4021014; //中級物品結晶 - 分解裝備後獲得的結晶。含有物品原始的力量。用於製作70~90級物品。
            } else if (reqLevel <= 120) {
                toGet = 4021015; //高級物品結晶 - 分解裝備後獲得的結晶。含有物品原始的力量。用於製作100~120級物品。
            }
            if (quantity <= 5) {
                cr = CraftRanking.SOSO;
            }
            if (Randomizer.nextInt(5) == 0 && toGet != 4031016) {
                toGet++;
                quantity = 1;
                cr = CraftRanking.COOL;
            } else if (Randomizer.nextInt(100) == 0 && reqLevel > 105) {
                toGet = 4021021; //賢者之石 - 含有煉金術的精髓的礦物。乍一看像是液體。分解105級以上裝備時偶爾可以發現。
                quantity = 1;
                cr = CraftRanking.COOL;
            }
            fatigue = 3;
            MapleInventoryManipulator.addById(c, toGet, quantity, "分解獲得 " + itemId + " 時間 " + DateUtil.getCurrentDate());
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item.getPosition(), (byte) 1, false);
            showItems.add(new Pair<>(itemId, -1));
            showItems.add(new Pair<>(toGet, quantity));
        } else if (craftID == 92049000) { //分解機分解裝備
            int extractorId = slea.readInt();
            int itemId = slea.readInt();
            long sn = slea.readLong();
            int reqLevel = ii.getReqLevel(itemId);
            Item item = chr.getInventory(MapleInventoryType.EQUIP).findBySN(sn, itemId);
            if (item == null || chr.getInventory(MapleInventoryType.ETC).isFull()) {
                return;
            }
            if (extractorId <= 0 && (theLevl == 0 || theLevl < (reqLevel > 130 ? 6 : ((reqLevel - 30) / 20)))) {
                return;
            } else if (extractorId > 0) {
                MapleCharacter extract = chr.getMap().getPlayerObject(extractorId);
                if (extract == null || extract.getExtractor() == null) {
                    return;
                }
                MapleExtractor extractor = extract.getExtractor();
                if (extractor.owner != chr.getId()) { //fee
                    if (chr.getMeso() < extractor.fee) {
                        return;
                    }
                    MapleStatEffect eff = ii.getItemEffect(extractor.itemId);
                    if (eff != null && eff.getUseLevel() < reqLevel) {
                        return;
                    }
                    chr.gainMeso(-extractor.fee, true);
                    MapleCharacter owner = chr.getMap().getPlayerObject(extractor.owner);
                    if (owner != null && owner.getMeso() < (Integer.MAX_VALUE - extractor.fee)) {
                        owner.gainMeso(extractor.fee, false);
                    }
                }
            }
            toGet = 4031016; //神秘書 - 誰都不知道內容的神秘的書
            quantity = Randomizer.rand(3, ItemConstants.類型.武器(itemId) || ItemConstants.類型.套服(itemId) ? 11 : 7);
            if (reqLevel <= 60) {
                toGet = 4021013; //低級物品結晶 - 分解裝備後獲得的結晶。含有物品原始的力量。用於製作40~60級物品。
            } else if (reqLevel <= 90) {
                toGet = 4021014; //中級物品結晶 - 分解裝備後獲得的結晶。含有物品原始的力量。用於製作70~90級物品。
            } else if (reqLevel <= 120) {
                toGet = 4021015; //高級物品結晶 - 分解裝備後獲得的結晶。含有物品原始的力量。用於製作100~120級物品。
            }
            if (quantity <= 5) {
                cr = CraftRanking.SOSO;
            }
            if (Randomizer.nextInt(5) == 0 && toGet != 4031016) {
                toGet++;
                quantity = 1;
                cr = CraftRanking.COOL;
            } else if (Randomizer.nextInt(100) == 0 && reqLevel > 105) {
                toGet = 4021021; //賢者之石 - 含有煉金術的精髓的礦物。乍一看像是液體。分解105級以上裝備時偶爾可以發現。
                quantity = 1;
                cr = CraftRanking.COOL;
            }
            fatigue = 3;
            MapleInventoryManipulator.addById(c, toGet, quantity, "分解獲得 " + itemId + " 時間 " + DateUtil.getCurrentDate());
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item.getPosition(), (byte) 1, false);
            showItems.add(new Pair<>(itemId, -1));
            showItems.add(new Pair<>(toGet, quantity));
        } else if (craftID == 92049001) { //合成裝備. /mindfuck
            int itemId = slea.readInt();
            long invId1 = slea.readLong();
            long invId2 = slea.readLong();
            int reqLevel = ii.getReqLevel(itemId);
            Equip item1 = (Equip) chr.getInventory(MapleInventoryType.EQUIP).findBySN(invId1, itemId);
            Equip item2 = (Equip) chr.getInventory(MapleInventoryType.EQUIP).findBySN(invId2, itemId);
            for (short i = 0; i < chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit(); i++) {
                Item item = chr.getInventory(MapleInventoryType.EQUIP).getItem(i);
                if (item != null && item.getItemId() == itemId && item != item1 && item != item2) {
                    if (item1 == null) {
                        item1 = (Equip) item;
                    } else if (item2 == null) {
                        item2 = (Equip) item;
                        break;
                    }
                }
            }
            if (item1 == null || item2 == null) {
                return;
            }
            if (theLevl < (reqLevel > 130 ? 6 : ((reqLevel - 30) / 20))) {
                return;
            }
            int potentialState = 5, potentialChance = (theLevl * 2), toRemove = 1;
            if (reqLevel <= 30) {
                toRemove = 1;
            } else if (reqLevel <= 70) {
                toRemove = 2;
            } else if (reqLevel <= 120) {
                toRemove = 3;
            } else {
                toRemove = 4;
            }
            if (!chr.haveItem(4021017, toRemove)) {
                chr.dropMessage(5, "合成裝備所需要的煉金術師之石不足，至少需要" + toRemove + "個。");
                c.sendEnableActions();
                return;
            }
            if (item1.getState(false) > 0 && item2.getState(false) > 0) {
                potentialChance = 100;
            } else if (item1.getState(false) > 0 || item2.getState(false) > 0) {
                potentialChance *= 2;
            }
            if (item1.getState(false) == item2.getState(false) && item1.getState(false) > 5) {
                potentialState = item1.getState(false);
            }
            //use average stats if scrolled.
            Equip newEquip = ii.fuse(item1.getCurrentUpgradeCount() > 0 ? ii.getEquipById(itemId) : item1, item2.getCurrentUpgradeCount() > 0 ? ii.getEquipById(itemId) : item2);
            int newStat = ii.getTotalStat(newEquip);
            if (newStat > ii.getTotalStat(item1) || newStat > ii.getTotalStat(item2)) {
                cr = CraftRanking.COOL;
            } else if (newStat < ii.getTotalStat(item1) || newStat < ii.getTotalStat(item2)) {
                cr = CraftRanking.SOSO;
            }
            if (Randomizer.nextInt(100) < (newEquip.getRestUpgradeCount() > 0 || potentialChance >= 100 ? potentialChance : (potentialChance / 2))) {
                newEquip.resetPotential_Fuse(theLevl > 5, potentialState);
            }
            newEquip.setAttribute(ItemAttribute.Crafted.getValue());
            newEquip.setOwner(chr.getName());
            toGet = newEquip.getItemId();
            expGain = (60 - ((theLevl - 1) * 4));
            fatigue = 3;
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item1.getPosition(), (byte) 1, false);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item2.getPosition(), (byte) 1, false);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4021017, toRemove, false, false);
            MapleInventoryManipulator.addbyItem(c, newEquip);
            showItems.add(new Pair<>(itemId, -1));
            showItems.add(new Pair<>(itemId, -1));
            showItems.add(new Pair<>(4021017, -toRemove));
            showItems.add(new Pair<>(toGet, 1));
        } else {
            if (ce.needOpenItem && chr.getSkillLevel(craftID) <= 0) {
                return;
            }
            for (Entry<Integer, Integer> e : ce.reqItems.entrySet()) {
                if (!chr.haveItem(e.getKey(), e.getValue())) {
                    return;
                }
            }
            for (Triple<Integer, Integer, Integer> i : ce.targetItems) {
                if (!MapleInventoryManipulator.checkSpace(c, i.left, i.mid, "")) {
                    return;
                }
            }
            for (Entry<Integer, Integer> e : ce.reqItems.entrySet()) {
                MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(e.getKey()), e.getKey(), e.getValue(), false, false);
                showItems.add(new Pair<>(e.getKey(), -e.getValue()));
            }
            if (Randomizer.nextInt(100) < (100 - (ce.reqSkillLevel - theLevl) * 20) || (craftID / 10000 <= 9201)) {
                while (true) {
                    boolean passed = false;
                    for (Triple<Integer, Integer, Integer> i : ce.targetItems) {
                        if (Randomizer.nextInt(100) < i.right) {
                            toGet = i.left;
                            quantity = i.mid.shortValue();
                            Item receive = null;
                            if (ItemConstants.getInventoryType(toGet, false) == MapleInventoryType.EQUIP) {
                                Equip first = ii.getEquipById(toGet);
                                if (Randomizer.nextInt(100) < (theLevl * 2)) {
                                    first = ii.randomizeStats(first);
                                    cr = CraftRanking.COOL;
                                }
                                if (Randomizer.nextInt(100) < (theLevl * (first.getRestUpgradeCount() > 0 ? 2 : 1))) {
                                    first.renewPotential(false);
                                    cr = CraftRanking.COOL;
                                }
                                receive = first;
                                receive.setAttribute(ItemAttribute.Crafted.getValue());
                            } else {
                                receive = new Item(toGet, (short) 0, (short) quantity, ItemAttribute.TradeOnce.getValue());
                            }
                            if (ce.period > 0) {
                                long period = ce.period;
                                receive.setExpiration(System.currentTimeMillis() + (period * 60 * 1000)); //period 在WZ裡面顯示是按分計算
                            }
                            receive.setOwner(chr.getName());
                            receive.setGMLog("製作裝備 " + craftID + " 在 " + DateUtil.getCurrentDate());
                            MapleInventoryManipulator.addFromDrop(c, receive, false, false);
                            showItems.add(new Pair<>(receive.getItemId(), (int) receive.getQuantity()));
                            if (ce.needOpenItem) {
                                int mLevel = chr.getMasterLevel(craftID);
                                if (mLevel == 1) {
                                    chr.changeSingleSkillLevel(ce, 0, (byte) 0);
                                } else if (mLevel > 1) {
                                    chr.changeSingleSkillLevel(ce, Integer.MAX_VALUE, (byte) (chr.getMasterLevel(craftID) - 1));
                                }
                            }
                            fatigue = ce.incFatigability;
                            expGain = ce.incSkillProficiency == 0 ? (((fatigue * 20) - (ce.reqSkillLevel - theLevl) * 4)) : ce.incSkillProficiency;
                            chr.getTrait(MapleTraitType.craft).addExp(cr.craft, chr);
                            passed = true;
                            break;
                        }
                    }
                    if (passed) {
                        break;
                    }
                }
            } else {
                quantity = 0;
                cr = CraftRanking.FAIL;
            }
        }
        if (expGain > 0 && theLevl <= 11) {
            expGain *= chr.isAdmin() ? 20 : ServerConfig.CHANNEL_RATE_TRAIT;
            if (Randomizer.nextInt(100) < chr.getTrait(MapleTraitType.craft).getLevel() / 5) {
                expGain *= 2;
            }
            String s = "煉金術";
            switch (craftID / 10000) {
                case 9200:
                    s = "採藥";
                    break;
                case 9201:
                    s = "採礦";
                    break;
                case 9202:
                    s = "裝備製作";
                    break;
                case 9203:
                    s = "飾品製作";
                    break;
            }
            chr.dropMessage(-5, s + "的熟練度提高了。(+" + expGain + ")");
            if (chr.addProfessionExp((craftID / 10000) * 10000, expGain)) {
                chr.dropMessage(-5, s + "的等級提升了。");
            }
        } else {
            expGain = 0;
        }
        MapleQuest.getInstance(2550).forceStart(c.getPlayer(), 9031000, "1"); //removes tutorial stuff
        chr.setFatigue((byte) (chr.getFatigue() + fatigue));
        chr.getMap().broadcastMessage(MaplePacketCreator.craftFinished(chr.getId(), craftID, craftType, cr.ranking, toGet, quantity, expGain));
        if (!showItems.isEmpty()) {
            c.announce(EffectPacket.getShowItemGain(showItems));
        }
    }

    /*
     * 使用道具寶寶
     */
    public static void UsePot(MaplePacketReader slea, MapleClient c) {
        int itemid = slea.readInt();
        Item slot = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slea.readShort());
        if (slot == null || slot.getQuantity() <= 0 || slot.getItemId() != itemid || itemid / 10000 != 244 || MapleItemInformationProvider.getInstance().getPot(itemid) == null) {
            c.sendEnableActions();
            return;
        }
        c.sendEnableActions();
        for (int i = 0; i < c.getPlayer().getImps().length; i++) {
            if (c.getPlayer().getImps()[i] == null) {
                c.getPlayer().getImps()[i] = new MapleImp(itemid);
                c.announce(MaplePacketCreator.updateImp(c.getPlayer().getImps()[i], ImpFlag.SUMMONED.getValue(), i, false));
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot.getPosition(), (short) 1, false, false);
                return;
            }
        }
    }

    /*
     * 清除道具寶寶
     */
    public static void ClearPot(MaplePacketReader slea, MapleClient c) {
        int index = slea.readInt() - 1;
        if (index < 0 || index >= c.getPlayer().getImps().length || c.getPlayer().getImps()[index] == null) {
            c.sendEnableActions();
            return;
        }
        c.announce(MaplePacketCreator.updateImp(c.getPlayer().getImps()[index], ImpFlag.REMOVED.getValue(), index, false));
        c.getPlayer().getImps()[index] = null;
    }

    /*
     * 餵養道具寶寶
     */
    public static void FeedPot(MaplePacketReader slea, MapleClient c) {
        int itemid = slea.readInt();
        Item slot = c.getPlayer().getInventory(ItemConstants.getInventoryType(itemid)).getItem((short) slea.readInt());
        if (slot == null || slot.getQuantity() <= 0 || slot.getItemId() != itemid) {
            c.sendEnableActions();
            return;
        }
        int level = ItemConstants.getInventoryType(itemid) == MapleInventoryType.ETC ? MapleItemInformationProvider.getInstance().getItemMakeLevel(itemid) : MapleItemInformationProvider.getInstance().getReqLevel(itemid);
        if (level <= 0 || level < (Math.min(120, c.getPlayer().getLevel()) - 50) || (ItemConstants.getInventoryType(itemid) != MapleInventoryType.ETC && ItemConstants.getInventoryType(itemid, false) != MapleInventoryType.EQUIP)) {
            c.getPlayer().dropMessage(1, "餵養道具寶寶出錯。");
            c.sendEnableActions();
            return;
        }
        int index = slea.readInt() - 1;
        if (c.getPlayer().isAdmin()) {
            c.getPlayer().dropMessage(5, "餵養道具寶寶 index: " + index);
        }
        if (index < 0 || index >= c.getPlayer().getImps().length || c.getPlayer().getImps()[index] == null || c.getPlayer().getImps()[index].getLevel() >= (MapleItemInformationProvider.getInstance().getPot(c.getPlayer().getImps()[index].getItemId()).right - 1) || c.getPlayer().getImps()[index].getState() != 1) {
            c.sendEnableActions();
            return;
        }
        int mask = ImpFlag.FULLNESS.getValue();
        mask |= ImpFlag.FULLNESS_2.getValue();
        mask |= ImpFlag.UPDATE_TIME.getValue();
        mask |= ImpFlag.AWAKE_TIME.getValue();
        //this is where the magic happens
        c.getPlayer().getImps()[index].setFullness(c.getPlayer().getImps()[index].getFullness() + (100 * (ItemConstants.getInventoryType(itemid, false) == MapleInventoryType.EQUIP ? 2 : 1)));
        if (Randomizer.nextBoolean()) {
            mask |= ImpFlag.CLOSENESS.getValue();
            c.getPlayer().getImps()[index].setCloseness(c.getPlayer().getImps()[index].getCloseness() + 1 + (Randomizer.nextInt(5 * (ItemConstants.getInventoryType(itemid, false) == MapleInventoryType.EQUIP ? 2 : 1))));
        } else if (Randomizer.nextInt(5) == 0) { //1/10 chance of sickness
            c.getPlayer().getImps()[index].setState(4); //sick
            mask |= ImpFlag.STATE.getValue();
        }
        if (c.getPlayer().getImps()[index].getFullness() >= 1000) {
            c.getPlayer().getImps()[index].setState(1);
            c.getPlayer().getImps()[index].setFullness(0);
            c.getPlayer().getImps()[index].setLevel(c.getPlayer().getImps()[index].getLevel() + 1);
            mask |= ImpFlag.SUMMONED.getValue();
            if (c.getPlayer().getImps()[index].getLevel() >= (MapleItemInformationProvider.getInstance().getPot(c.getPlayer().getImps()[index].getItemId()).right - 1)) {
                c.getPlayer().getImps()[index].setState(5);
            }
        }
        MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemid), slot.getPosition(), (short) 1, false, false);
        c.announce(MaplePacketCreator.updateImp(c.getPlayer().getImps()[index], mask, index, false));
    }

    /*
     * 治癒道具寶寶
     * 道具寶寶生病了使用藥水
     */
    public static void CurePot(MaplePacketReader slea, MapleClient c) {
        int itemid = slea.readInt();
        Item slot = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((short) slea.readInt());
        if (slot == null || slot.getQuantity() <= 0 || slot.getItemId() != itemid || itemid / 10000 != 434) {
            c.sendEnableActions();
            return;
        }
        int index = slea.readInt() - 1;
        if (c.getPlayer().isAdmin()) {
            c.getPlayer().dropMessage(5, "治癒道具寶寶 index: " + index);
        }
        if (index < 0 || index >= c.getPlayer().getImps().length || c.getPlayer().getImps()[index] == null || c.getPlayer().getImps()[index].getState() != 4) {
            c.sendEnableActions();
            return;
        }
        c.getPlayer().getImps()[index].setState(1);
        c.announce(MaplePacketCreator.updateImp(c.getPlayer().getImps()[index], ImpFlag.STATE.getValue(), index, false));
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot.getPosition(), (short) 1, false, false);
    }

    /*
     * 道具寶寶等級滿後的獎勵
     */
    public static void RewardPot(MaplePacketReader slea, MapleClient c) {
        int index = slea.readInt() - 1;
        if (c.getPlayer().isAdmin()) {
            c.getPlayer().dropMessage(5, "道具寶寶獎勵 index: " + index);
        }
        if (index < 0 || index >= c.getPlayer().getImps().length || c.getPlayer().getImps()[index] == null || c.getPlayer().getImps()[index].getLevel() < (MapleItemInformationProvider.getInstance().getPot(c.getPlayer().getImps()[index].getItemId()).right - 1)) {
            c.sendEnableActions();
            return;
        }
        int itemid = ItemConstants.getRewardPot(c.getPlayer().getImps()[index].getItemId(), c.getPlayer().getImps()[index].getCloseness());
        if (itemid <= 0 || !MapleInventoryManipulator.checkSpace(c, itemid, (short) 1, "")) {
            c.getPlayer().dropMessage(1, "您的背包空間不足。");
            c.sendEnableActions();
            return;
        }
        MapleInventoryManipulator.addById(c, itemid, 1, "道具寶寶 " + c.getPlayer().getImps()[index].getItemId() + " 在 " + DateUtil.getCurrentDate());
        c.announce(MaplePacketCreator.updateImp(c.getPlayer().getImps()[index], ImpFlag.REMOVED.getValue(), index, false));
        c.getPlayer().getImps()[index] = null;
    }

    public enum CraftRanking {

        SOSO(25, 30),
        GOOD(26, 40),
        COOL(27, 50),
        //0x18	FAIL	由於未知原因 製作道具失敗
        //0x19	FAIL	物品製作失敗.
        //0x1A	FAIL	分解機已撤除，分解取消。
        //0x1B	FAIL	分解機的主任無法繼續獲得手續費。
        FAIL(29, 20);
        public final int ranking;
        public final int craft;

        CraftRanking(int ranking, int craft) {
            this.ranking = ranking;
            this.craft = craft;
        }
    }
}
