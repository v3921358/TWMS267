package Plugin.script.binding;

import Client.*;
import Client.hexa.MapleHexaSkill;
import Client.inventory.*;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.stat.MapleHyperStats;
import Config.configs.Config;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.enums.InGameDirectionEventType;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.管理員;
import Database.DatabaseConnectionEx;
import Database.DatabaseLoader;
import Database.tools.SqlTool;
import Handler.warpToGameHandler;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MaplePortal;
import Net.server.buffs.MapleStatEffect;
import Net.server.commands.CommandProcessorUtil;
import Net.server.commands.GMCommand;
import Net.server.factory.MobCollectionFactory;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.life.MapleMonsterInformationProvider;
import Net.server.life.MapleNPC;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.maps.field.ActionBarField;
import Net.server.quest.MapleQuest;
import Net.server.quest.MapleQuestRequirement;
import Net.server.quest.MapleQuestRequirementType;
import Net.server.shop.MapleShopFactory;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Packet.*;
import Plugin.provider.loaders.StringData;
import Server.channel.ChannelServer;
import Server.channel.handler.InterServerHandler;
import Server.world.WorldAllianceService;
import Server.world.WorldBroadcastService;
import Server.world.WorldGuildService;
import Server.world.guild.MapleGuild;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import SwordieX.client.party.PartyResult;
import connection.packet.*;
import SwordieX.field.ClockPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import SwordieX.util.Position;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ScriptPlayer extends PlayerScriptInteraction {

    private int level;
    private boolean changed_wishlist, changed_trocklocations, changed_skillmacros, changed_savedlocations, changed_questinfo, changed_worldshareinfo, changed_skills, changed_reports, changed_vcores,
            changed_innerSkills, changed_keyValue, changed_buylimit, changed_accountbuylimit, changed_soulcollection, changed_mobcollection, changed_familiars;
    private Map<Integer, MonsterFamiliar> familiars;
    private MonsterFamiliar summonedFamiliar;
    private transient MapleMap map;

    public ScriptPlayer(MapleCharacter player) {
        super(player);
    }
    /**
     * 給予六轉能量的訊息
     * 指向回MapleCharacter
     */
    public void gainErda(int itemid) {
        if (getPlayer() != null) {
            getPlayer().gainErda(itemid);
        }
    }

    public void teachskill(int skillid, int level) {
        Skill skill = SkillFactory.getSkill(skillid);
        getPlayer().changeSingleSkillLevel(skill, level, skill.getMasterLevel(), SkillFactory.getDefaultSExpiry(skill));
        getPlayer().dropMessage(5, "技能ID_:"+skillid+"_技能添加成功。");
    }


    /**
     * 六轉能量值指向回MapleCharacter做增加動作
     */
    public void addEdraSoul(int amount) {
        if (getPlayer() != null) {
            getPlayer().addEdraSoul(amount);
        }
    }

    /**
     * 給予裝備
     */
    public void addByItem(final Item item) {
        MapleInventoryManipulator.addbyItem(getClient(), item);
    }

    /**
     * 臉型
     */
    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.臉型, face);
    }

    /**
     * 髮型
     */
    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.髮型, hair);
    }

    /**
     * 膚色
     */
    public void setSkin(int skin) {
        getPlayer().setSkinColor((byte) skin);
        getPlayer().updateSingleStat(MapleStat.皮膚, skin);
    }


    /**
     * 機器人臉型
     */
    public void setAndroidFace(int face) {
        getPlayer().getAndroid().setFace(face);
        getPlayer().updateAndroidLook();
    }

    /**
     * 機器人髮型
     */
    public void setAndroidHair(int hair) {
        getPlayer().getAndroid().setHair(hair);
        getPlayer().updateAndroidLook();
    }

    /**
     * 機器人膚色
     */
    public void setAndroidSkin(int skin) {
        getPlayer().getAndroid().setSkin(skin);
        getPlayer().updateAndroidLook();
    }

    /**
     * custombuff專用
     */
    private static void inc(Map<SecondaryStat, Integer> map, SecondaryStat stat, int val) {
        map.merge(stat, val, (a, b) -> a + b);
    }

    public int getId() {
        return getPlayer().getId();
    }


    /**
     * 断开客户端连接
     */
    public void dissociateClient() {
        getClient().disconnect(true, true);
    }

    /**
     * 屏幕右下方 显示弹出Npc对话
     *
     * @param npcId
     * @param duration
     * @param msg
     * @param sound
     */
    public void addPopupSay(int npcId, int duration, String msg, String sound) {
        getClient().announce(UIPacket.addPopupSay(npcId, duration, msg, sound));
    }

    public void addPopupSay(int npcId, int showTimer, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AddPopupSay.getValue());
        mplew.writeInt(npcId); // test:蕭公_2091011
        mplew.writeInt(showTimer);
        mplew.writeMapleAsciiString(msg);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(300);
        mplew.writeInt(1);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        getClient().announce(mplew.getPacket());
    }

    public int getItemQuantity(int item) {
        return getPlayer().getItemQuantity(item);
    }


    /*
     * 改變hp值
     */
    public void setMaxHp(int hp) {
        getPlayer().getStat().setInfo(hp, getPlayer().getStat().getMaxMp(false), hp, getPlayer().getStat().getMp());
        getPlayer().updateSingleStat(MapleStat.HP, hp);
        getPlayer().updateSingleStat(MapleStat.MAXHP, hp);
        getPlayer().getStat().recalcLocalStats(getPlayer());
    }

    /*
     * 改變mp值
     */
    public void setMaxMp(int hp) {
        getPlayer().getStat().setInfo(getPlayer().getStat().getMaxHp(false), hp, getPlayer().getStat().getHp(), hp);
        getPlayer().updateSingleStat(MapleStat.MP, hp);
        getPlayer().updateSingleStat(MapleStat.MAXMP, hp);
        getPlayer().getStat().recalcLocalStats(getPlayer());
    }


    /**
     * 增加事件记录值
     *
     * @param key
     */
    public void addPQLog(String key) {
        getPlayer().setPQLog(key);
    }

    /**
     * 增加事件记录值
     *
     * @param key
     * @param value
     * @param resetDays
     */
    public void addPQLog(String key, int value, int resetDays) {
        getPlayer().setPQLog(key, 0, value, resetDays);
    }

    public int getPQLog(String key) {
        return getPlayer().getPQLog(key);
    }

    public void resetPQLog(String key) {
        getPlayer().resetPQLog(key);
    }

    public String getQuestRecordEx(int quest) {
        if (!getPlayer().getInfoQuest_Map().containsKey(quest) || MapleQuest.getInstance(quest) == null) {
            return null;
        }
        return getPlayer().getInfoQuest_Map().get(quest);
    }

    public String getQuestRecord(int quest, String key) {
        return getPlayer().getQuestInfo(quest, key);
    }

    public String getQuestEntryData(int quest) {
        return getPlayer().getQuest(quest).getCustomData();
    }

    public void updateQuestRecordEx(int questid, String data) {
        getPlayer().updateInfoQuest(questid, data);
    }

    public void updateQuestRecordEx(int questid, int data) {
        getPlayer().updateInfoQuest(questid, String.valueOf(data));
    }

    public void updateQuestRecord(int questid, String key, String value) {
        getPlayer().updateOneQuestInfo(questid, key, value);
    }

    public void updateQuestRecord(int questid, String key, int value) {
        getPlayer().updateOneQuestInfo(questid, key, String.valueOf(value));
    }

    public void updateQuestRecord(int questid, int key, String value) {
        getPlayer().updateOneQuestInfo(questid, String.valueOf(key), value);
    }

    public void updateQuestRecord(int questid, int key, int value) {
        getPlayer().updateOneQuestInfo(questid, String.valueOf(key), String.valueOf(value));
    }

    public void setQuestData(int quest, String data) {
        getPlayer().getQuest(quest).setCustomData(data);
    }

    /**
     * 取消道具Buff
     *
     * @param itemId
     */
    public void cancelItemEffect(int itemId) {
        getClient().getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(itemId), false, -1);
    }

    /**
     * 取消技能Buff
     *
     * @param skillId
     */
    public void cancelSkillEffect(int skillId) {
        if (getPlayer().getSkillEffect(skillId) != null) {
            getClient().getPlayer().cancelEffect(getPlayer().getSkillEffect(skillId), false, -1);
        }
    }

    /**
     * 判断能否获取指定数量的道具
     *
     * @param itemId
     * @param quantity
     * @return
     */
    public boolean canGainItem(int itemId, int quantity) {
        return MapleInventoryManipulator.checkSpace(getClient(), itemId, quantity, "");
    }

    /**
     * 传送到指定地图
     */
    public void changeMap(int mapId) {

        if (getPlayer().getEventInstance() != null) {
            if (getPlayer().getEventInstance().getFields().get(mapId) != null) {
                getPlayer().changeMap(getPlayer().getEventInstance().getFields().get(mapId).getMap());
                getPlayer().write(warpToGameHandler.EquipRuneSetting());
                return;
            }
        }
        changeMap(mapId, 0);
        getPlayer().write(warpToGameHandler.EquipRuneSetting());
    }

    /**
     * 传送到指定地图
     *
     * @param mapId
     * @param portal
     */
    public void changeMap(int mapId, int portal) {
        if (getPlayer().getEventInstance() != null) {
            if (getPlayer().getEventInstance().getFields().get(mapId) != null) {
                getPlayer().changeMap(getPlayer().getEventInstance().getFields().get(mapId).getMap());
                getPlayer().write(warpToGameHandler.EquipRuneSetting());
                return;
            }
        }
        getPlayer().changeMap(mapId, portal);
        getPlayer().write(warpToGameHandler.EquipRuneSetting());
    }

    /**
     * 传送到指定地图
     *
     * @param mapId
     * @param portal
     */
    public void changeMap(int mapId, String portal) {
        MapleMap map = getClient().getChannelServer().getMapFactory().getMap(mapId);
        if (map == null) {
            return;
        }
        MaplePortal portalPos = map.getPortal(portal);
        if (portalPos == null) {
            return;
        }
        if (getPlayer().getEventInstance() != null) {
            if (getPlayer().getEventInstance().getFields().get(mapId) != null) {
                getPlayer().changeMap(getPlayer().getEventInstance().getFields().get(mapId).getMap(), portalPos);
                getPlayer().write(warpToGameHandler.EquipRuneSetting());
                return;
            }
        }
        getPlayer().changeMap(map, portalPos);
        getPlayer().write(warpToGameHandler.EquipRuneSetting());
    }

    /**
     * 传送到指定地图
     *
     * @param map
     */
    public void changeMap(MapleMap map) {
        if (getPlayer().getEventInstance() != null) {
            if (getPlayer().getEventInstance().getFields().get(map.getId()) != null) {
                getPlayer().changeMap(getPlayer().getEventInstance().getFields().get(map.getId()).getMap());
                getPlayer().write(warpToGameHandler.EquipRuneSetting());
                return;
            }
        }
        getPlayer().changeMap(map);
        getPlayer().write(warpToGameHandler.EquipRuneSetting());
    }

    /**
     * 传送到指定地图
     *
     * @param map
     */
    public void changeMap(ScriptField map) {
        if (getPlayer().getEventInstance() != null) {
            if (getPlayer().getEventInstance().getFields().get(map.getId()) != null) {
                getPlayer().changeMap(getPlayer().getEventInstance().getFields().get(map.getId()).getMap());
                getPlayer().write(warpToGameHandler.EquipRuneSetting());
                return;
            }
        }
        getPlayer().changeMap(map.getMap());
        getPlayer().write(warpToGameHandler.EquipRuneSetting());
    }


    /**
     * 传送到指定地图
     *
     * @param map
     * @param x
     * @param y
     */
    public void changeMap(MapleMap map, int x, int y) {
        Point pos = new Point(x, y);
        getPlayer().changeMapToPosition(map, pos);
        getPlayer().write(warpToGameHandler.EquipRuneSetting());
    }

    /**
     * 传送到指定频道和地图
     *
     * @param channel
     * @param mapId
     */
    public void changeChannelAndMap(int channel, int mapId) {
        getPlayer().changeChannel(channel);
        getPlayer().changeMap(mapId, 0);
        getPlayer().write(warpToGameHandler.EquipRuneSetting());
    }

    public void startQuest(int questId) {
        MapleQuest quest = MapleQuest.getInstance(questId);
        int npc_id = 9330072;
        for (MapleQuestRequirement qr : quest.getStartReqs()) {
            if (qr.getType() == MapleQuestRequirementType.npc) {
                npc_id = qr.getIntStore();
                break;
            }
        }
        quest.forceStart(getPlayer(), npc_id, "");
    }

    public void startQuest(int questId, int npcId) {
        MapleQuest quest = MapleQuest.getInstance(questId);
        int npc_id = npcId;
        for (MapleQuestRequirement qr : quest.getStartReqs()) {
            if (qr.getType() == MapleQuestRequirementType.npc) {
                npc_id = qr.getIntStore();
                break;
            }
        }
        quest.forceStart(getPlayer(), npc_id, "");
    }

    public void startQuest(int questId, int npcId, String data) {
        MapleQuest quest = MapleQuest.getInstance(questId);
        int npc_id = npcId;
        for (MapleQuestRequirement qr : quest.getStartReqs()) {
            if (qr.getType() == MapleQuestRequirementType.npc) {
                npc_id = qr.getIntStore();
                break;
            }
        }
        quest.forceStart(getPlayer(), npc_id, data);
    }

    public void completeQuest(int questId, int npcId) {
        MapleQuest quest = MapleQuest.getInstance(questId);
        int npc_id = npcId;
        for (MapleQuestRequirement qr : quest.getCompleteReqs()) {
            if (qr.getType() == MapleQuestRequirementType.npc) {
                npc_id = qr.getIntStore();
                break;
            }
        }
        quest.forceComplete(getPlayer(), npc_id, false);
    }

    /**
     * @param questId
     */
    public void forfeitQuest(int questId) {
        MapleQuest.getInstance(questId).forfeit(getPlayer());
    }

    /**
     * 創建聯盟
     *
     * @param allianceName
     * @return
     */
    public boolean createAlliance(String allianceName) {
        Party pt = getPlayer().getParty();
        MapleCharacter otherChar = getClient().getChannelServer().getPlayerStorage().getCharacterById(pt.getMembers().get(1).getCharID());
        if (otherChar == null || otherChar.getId() == getPlayer().getId()) {
            return false;
        }
        try {
            return WorldAllianceService.getInstance().createAlliance(allianceName, getPlayer().getId(), otherChar.getId(), getPlayer().getGuildId(), otherChar.getGuildId());
        } catch (Exception re) {
            log.error("createAlliance 錯誤", re);
            return false;
        }
    }

    /**
     * 獲取聯盟ID
     *
     * @return
     */
    public int getAllianceId() {
        MapleGuild guild = WorldGuildService.getInstance().getGuild(getPlayer().getGuildId());
        return guild.getAllianceId();
    }

    /**
     * 獲取聯盟容量
     *
     * @return
     */
    public int getAllianceCapacity() {
        MapleGuild guild = WorldGuildService.getInstance().getGuild(getPlayer().getGuildId());
        return WorldAllianceService.getInstance().getAlliance(guild.getAllianceId()).getCapacity();
    }

    /**
     * 獲取聯盟排名
     *
     * @return
     */
    public String[] getAllianceRank() {
        MapleGuild guild = WorldGuildService.getInstance().getGuild(getPlayer().getGuildId());
        return WorldAllianceService.getInstance().getAlliance(guild.getAllianceId()).getRanks();
    }

    /**
     * 獲取公會ID
     *
     * @return
     */
    public int getGuildId() {
        return getPlayer().getGuildId();
    }

    /**
     * 獲取公會人數
     *
     * @return
     */
    public int getGuildCapacity() {
        return getPlayer().getGuild().getCapacity();
    }

    /**
     * 獲取公會貢獻
     *
     * @return
     */
    public int getGuildContribution() {
        return getPlayer().getGuildContribution();
    }

    /**
     * 是否有公會
     *
     * @return
     */
    public boolean hasGuild() {
        return getPlayer().getGuild() != null;
    }

    /**
     * 公會等級
     *
     * @return
     */
    public int getGuildRank() {
        return getPlayer().getGuildRank();
    }


    /**
     * 創建公會
     *
     * @param name
     * @return
     */
    public int createGuild(String name) {
        Party pt = getPlayer().getParty();

        if (pt.getPartyLeaderID() != getPlayer().getId()) {
            return -1;
        }

        try {
            return WorldGuildService.getInstance().createGuild(getPlayer().getId(), name);
        } catch (Exception re) {
            log.error("createGuild 錯誤", re);
            return -1;
        }
    }

    /**
     * 刪除公會
     */
    public void disbandGuild() {
        getPlayer().getGuild().disbandGuild();
    }

    /**
     * 弹出窗口消息
     *
     * @param message
     */
    public void dropAlertNotice(String message) {
        getPlayer().dropAlertNotice(message);
    }

    /**
     * 显示消息
     *
     * @param type
     * @param message
     */
    public void dropMessage(int type, String message) {
        getPlayer().dropMessage(type, message);
    }


    public int getLevel() {
        return getPlayer().getLevel();
    }


    /**
     * 設置等級
     *
     */
    public void setLevel(int newLevel) {
        getPlayer().setLevel(newLevel-1);
        getPlayer().levelUp(true);
    }

    /**
     * 给与AP点
     *
     * @param gain
     */
    public void gainAp(short gain) {
        getPlayer().gainAp(gain);
    }

    /**
     * 增加好友数量上限
     *
     * @param gain
     */
    public void gainBuddySlots(short gain) {
        getPlayer().setBuddyCapacity((byte) gain);
    }

    /**
     * 给与宠物亲密度
     *
     * @param gain
     */
    public void gainCloseness(short gain) {
        MaplePet pet = getPlayer().getSpawnPet(0);
        if (pet != null) {
            pet.setCloseness(pet.getCloseness() + (gain * ServerConfig.CHANNEL_RATE_TRAIT));
            getPlayer().petUpdateStats(pet, true);
        }
    }

    /**
     * 拓展栏位
     *
     * @param type
     * @param addSlot
     */
    public void gainInventorySlots(byte type, int addSlot) {
        MapleInventory inv = getPlayer().getInventory(MapleInventoryType.getByType(type));
        inv.addSlot((byte) addSlot);
        getPlayer().send(InventoryPacket.updateInventorySlotLimit(type, (byte) inv.getSlotLimit()));
    }

    /**
     * 给与经验值
     *
     * @param gain
     */
    public void gainExp(long gain) {
        getPlayer().gainExp(gain, true, true, true);
    }
    /**
     * 给与指定数量的道具
     *
     * @param itemId
     * @param quantity
     * @return
     */
    public boolean gainItem(int itemId, int quantity) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(itemId)) {
            getPlayer().dropMessage(5, itemId + " 這個道具不存在.");
            return false;
        } else {
            if (ItemConstants.getInventoryType(itemId).getType() == 1 || ItemConstants.類型.寵物(itemId) || ItemConstants.類型.寵物裝備(itemId)) {
                MapleInventory equipedIv = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
                for (int i = 0; i < quantity; i++) {
                    Equip equip = ii.getEquipById(itemId);
                    MapleInventoryManipulator.addbyItem(getClient(), equip);
                    equipedIv.addFromDB(equip);
                }
                getPlayer().send(MaplePacketCreator.getShowItemGain(itemId, quantity));
            } else {
                MapleInventory equipedIv = getPlayer().getInventory(ItemConstants.getInventoryType(itemId, false));
                Item item = new Item(itemId, (byte) 0, (short) quantity, 0);
                MapleInventoryManipulator.addbyItem(getClient(), item);
                getPlayer().send(MaplePacketCreator.getShowItemGain(itemId, quantity));
                equipedIv.addFromDB(item);
            }
            return true;
        }
    }

    /**
     * 给与指定数量的道具
     *
     * @param itemId
     * @param quantity
     * @param duration
     * @return
     */
    public boolean gainItem(int itemId, short quantity, long duration) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(itemId)) {
            getPlayer().dropMessage(5, itemId + " 這個道具不存在.");
            return false;
        } else {

            if (ItemConstants.getInventoryType(itemId).getType() == 1 || ItemConstants.類型.寵物(itemId) || ItemConstants.類型.寵物裝備(itemId)) {
                MapleInventory equipedIv = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
                for (int i = 0; i < quantity; i++) {
                    Equip equip = ii.getEquipById(itemId);
                    if (duration > 0) {
                        if (duration < 1000) {
                            equip.setExpiration(System.currentTimeMillis() + (duration * 24 * 60 * 60 * 1000));
                        } else {
                            equip.setExpiration(System.currentTimeMillis() + duration);
                        }
                    }
                    MapleInventoryManipulator.addbyItem(getClient(), equip);
                    equipedIv.addFromDB(equip);
                }
                getPlayer().send(MaplePacketCreator.getShowItemGain(itemId, quantity));
            } else {
                MapleInventory equipedIv = getPlayer().getInventory(ItemConstants.getInventoryType(itemId).getType());
                Item item = new Item(itemId, (byte) 0, quantity, 0);
                if (duration > 0) {
                    if (duration < 1000) {
                        item.setExpiration(System.currentTimeMillis() + (duration * 24 * 60 * 60 * 1000));
                    } else {
                        item.setExpiration(System.currentTimeMillis() + duration);
                    }
                }
                MapleInventoryManipulator.addbyItem(getClient(), item);
                equipedIv.addFromDB(item);
                getPlayer().send(MaplePacketCreator.getShowItemGain(itemId, quantity));
            }
            return true;
        }
    }

    public void updateItem(Item item) {
        getPlayer().forceUpdateItem(item);
    }

    public void updateItem(short slot, Item item) {
        getPlayer().forceUpdateItem(getPlayer().getInventory(ItemConstants.getInventoryType(item.getItemId())).getItem(slot));
    }

    /**
     * @param itemId
     * @param day
     * @return
     */
    public boolean gainPetItem(int itemId, int day) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(itemId)) {
            getPlayer().dropMessage(5, itemId + " 這個道具不存在.");
            return false;
        } else {
            Item item;
            int flag = (short) ItemAttribute.Seal.getValue();
            final MaplePet pet;
            if (ItemConstants.類型.寵物(itemId)) {
                pet = MaplePet.createPet(itemId);
                if (pet != null && day == -1) {
                    day = ii.getLife(itemId);
                    if (day < 0) {
                        day = 0;
                    }
                }
            } else {
                pet = null;
            }
            item = new Item(itemId, (byte) 0, (short) 1, 0);
            item.setPet(pet);
            if (day > 0) {
                if (day < 1000) {
                    item.setExpiration(System.currentTimeMillis() + ((long) day * 24 * 60 * 60 * 1000));
                } else {
                    item.setExpiration(System.currentTimeMillis() + day);
                }
            }
            return MapleInventoryManipulator.addbyItem(getClient(), item);
        }
    }

    /**
     * 給与道具
     *
     * @param item
     * @return
     */
    public boolean gainItem(Item item) {
        MapleInventory equipedIv = getPlayer().getInventory(ItemConstants.getInventoryType(item.getItemId()).getType());
        boolean result = MapleInventoryManipulator.addbyItem(getClient(), item);
        equipedIv.addFromDB(item);
        return result;
    }

    /**
     * 給予金幣
     *
     * @param gain
     */
    public void gainMeso(long gain) {
        getPlayer().gainMeso(gain, true, true);
    }

    /**
     * 给与指定职业的技能点
     *
     * @param skillbook
     * @param gain
     */
    public void gainSp(int skillbook, short gain) {
        getPlayer().gainSP(gain, skillbook);
    }

    /**
     * 给与当前职业技能点
     *
     * @param gain
     */
    public void gainSp(short gain) {
        getPlayer().gainSP(gain);
    }

    /**
     * 获取账号Id
     *
     * @return
     */
    public int getAccountId() {
        return getPlayer().getAccountID();
    }

    /**
     * 获取角色Id
     *
     * @return
     */
    public int getCharacterId() {
        return getPlayer().getId();
    }

    /**
     * 获取物品数量，不包含穿戴中的道具
     *
     * @param itemId
     * @return
     */
    public int getAmountOfItem(int itemId) {
        return getPlayer().getItemQuantity(itemId, false);
    }

    /**
     * 获取物品数量，可以包含穿戴中的道具
     *
     * @param itemId
     * @param checkEquipped
     * @return
     */
    public int getAmountOfItem(int itemId, boolean checkEquipped) {
        return getPlayer().getItemQuantity(itemId, checkEquipped);
    }

    /**
     * 获取好友数量上限
     *
     * @return
     */
    public byte getBuddyCapacity() {
        return getPlayer().getBuddyCapacity();
    }

    /**
     * 获取当前频道
     *
     * @return
     */
    public int getChannel() {
        return getClient().getChannel();
    }

    /**
     * @return
     */
    public short getDex() {
        return getPlayer().getStat().getDex();
    }

    /**
     * 获取脸型
     *
     * @return
     */
    public int getFace() {
        return getPlayer().getFace();
    }

    /**
     * 获取指定背包类型剩余空格
     *
     * @param type
     * @return
     */
    public short getFreeSlots(byte type) {
        return getPlayer().getInventory(type).getNumFreeSlot();
    }

    /**
     * 獲取性別
     *
     * @return
     */
    public int getGender() {
        return getPlayer().getGender();
    }

    public ScriptEvent getEvent() {
        return getPlayer().getEventInstance();
    }

    public ScriptEvent getEvent(String name) {
        if (name.equals(getPlayer().getEventInstance().getName())) {
            return getPlayer().getEventInstance();
        }
        return null;
    }

    public void setEvent(ScriptEvent event) {
        getPlayer().setEventInstance(event);
        if (event != null) {

            getPlayer().send(MaplePacketCreator.practiceMode(event.isPracticeMode()));
        }
    }

    public void showTimer(double seconds) {
        long sec = (long) Math.ceil(seconds * 1000);
        getClient().announce(FieldPacket.clock(ClockPacket.secondsClock(sec)));

        // 武陵
        //getClient().announce(FieldPacket.clock(ClockPacket.pauseTimer(true, 900 /*預設*/, 120/*等待秒數*/)));
    }

    public void showTimer(int time, int elapseTime) {
        getPlayer().send(FieldPacket.clock(ClockPacket.TimerInfoEx(time, elapseTime)));
    }

    public void pauseShowTimer(boolean type, int time, int elapseTime) {
        getPlayer().send(FieldPacket.clock(ClockPacket.pauseTimer(type, time, elapseTime)));
    }

    public void showTimer(boolean left, int seconds) {
        getClient().announce(FieldPacket.clock(ClockPacket.shiftTimer(left, seconds)));
    }

    public void closeTimer() {
        getClient().announce(FieldPacket.clock(ClockPacket.secondsClock(-1)));
    }

    public void setDeathCount(int nDeathCount) {
        getPlayer().setDeathCount(nDeathCount);
        getPlayer().send(CField.setDeathCount(getPlayer(), nDeathCount));
        getPlayer().getMap().broadcastMessage(CField.setDeathCount(getPlayer(), nDeathCount));
        if (nDeathCount <= 0) {
            closeUI(94);
        }
    }

    public void showDeathCount() {
        getPlayer().send(CField.setDeathCount(getPlayer(), getPlayer().getDeathCount()));
        getPlayer().getMap().broadcastMessage(CField.setDeathCount(getPlayer(), getPlayer().getDeathCount()));
    }

    public boolean hasItem(int itemId) {
        return getPlayer().haveItem(itemId);
    }

    public boolean hasItem(int itemId, int quantity) {
        return getPlayer().haveItem(itemId, quantity);
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getNowOnlineTime() {
        return getPlayer().getNowOnlineTime();
    }

    public int getOnlineTime() {
        return getPlayer().getOnlineTime();
    }

    public int getSkillLevel(int skillId) {
        return getPlayer().getSkillLevel(skillId);
    }

    public void changeSkillLevel(int skillId, byte newLevel) {
        Skill sk = SkillFactory.getSkill(skillId);
        if (sk != null) {
            getPlayer().changeSkillLevel(sk, newLevel, sk.getMasterLevel());
        }
    }

    public int getSp(int skillId) {
        return getPlayer().getRemainingSp();
    }

    public short getSubJob() {
        return getPlayer().getSubcategory();
//        return getPlayer().getJobWithSub();
    }

    public void setSubJob(short var1) {
        getPlayer().setSubcategory(var1);
    }

    public short getJob() {
        return getPlayer().getJob();
    }

    public void setJob(int var1) {
        getPlayer().changeJob(var1);
    }

    public int getWorld() {
        return getPlayer().getWorld();
    }

    public String getWorldShareRecord(int quest) {
        return getPlayer().getWorldShareInfo(quest);
    }

    public String getWorldShareRecord(int quest, String key) {
        return getPlayer().getWorldShareInfo(quest, key);
    }

    public void updateWorldShareRecord(int questid, String data) {
        getPlayer().updateWorldShareInfo(questid, data);
    }

    public void updateWorldShareRecord(int questid, String key, String value) {
        getPlayer().updateWorldShareInfo(questid, key, value);
    }

    public boolean hasEquipped(int itemId) {
        return getPlayer().hasEquipped(itemId);
    }

    public boolean hasMeso(long min) {
        return getPlayer().getMeso() >= min;
    }

    public void loseMesos(long quantity) {
        getPlayer().gainMeso(-1 * quantity, true, true);
    }

    public long getMeso() {
        return getPlayer().getMeso();
    }

    public long getCash() {
        return getPlayer().getCSPoints(1);
    }

    public long getPoint() {
        return getPlayer().getCSPoints(2);
    }

    /***
     * 取得儲值金額
     * @param type 儲值函數 1 = 當前儲值金額 2 = 已經消費金額 3 = 總計消費金額 4 = 儲值獎勵
     * @return
     */
    public int getHyPay(int type) {
        return getPlayer().getHyPay(type);
    }

    public void modifyCashShopCurrency(int type, int value) {
        getPlayer().modifyCSPoints(type, value);
    }

    public void increaseAllianceCapacity() {
        WorldAllianceService.getInstance().changeAllianceCapacity(getPlayer().getGuild().getAllianceId());
    }

    public void setHp(int hp) {
        getPlayer().addHP(hp);
    }

    public void increaseMaxHp(int delta) {
        getPlayer().getStat().setInfo(getPlayer().getStat().getMaxHp(false) + delta, getPlayer().getStat().getMaxMp(false), getPlayer().getStat().getMaxHp(false) + delta, getPlayer().getStat().getMp());
        getPlayer().updateSingleStat(MapleStat.HP, getPlayer().getStat().getMaxHp(false) + delta);
        getPlayer().updateSingleStat(MapleStat.MAXHP, getPlayer().getStat().getMaxHp(false) + delta);
        getPlayer().getStat().recalcLocalStats(getPlayer());
    }

    public void increaseMaxMp(int delta) {
        getPlayer().getStat().setInfo(getPlayer().getStat().getMaxHp(false), getPlayer().getStat().getMaxMp(false) + delta, getPlayer().getStat().getHp(), getPlayer().getStat().getMaxMp(false) + delta);
        getPlayer().updateSingleStat(MapleStat.MP, getPlayer().getStat().getMaxMp(false) + delta);
        getPlayer().updateSingleStat(MapleStat.MAXMP, getPlayer().getStat().getMaxMp(false) + delta);
        getPlayer().getStat().recalcLocalStats(getPlayer());
    }

    public boolean isGm() {
        return getPlayer().isGm();
    }

    public boolean isQuestCompleted(int questId) {
        return getPlayer().getQuestStatus(questId) == 2;
    }

    public boolean isQuestStarted(int questId) {
        return getPlayer().getQuestStatus(questId) == 1;
    }

    public void loseInvSlot(byte type, short slot) {
        getPlayer().getInventory(type).removeSlot(slot);
    }

    public void loseItem(int itemId) {
        getPlayer().removeItem(itemId);
    }

    public void loseItem(int itemId, int quantity) {
        getPlayer().removeItem(itemId, quantity);
    }

    public void maxSkills() {
        getPlayer().maxSkillsByJob(getPlayer().getJob());
    }

    public void openUI(int uiId) {
        getPlayer().send(UIPacket.sendOpenWindow(uiId));
    }

    public void closeUI(int uiId) {
        getPlayer().send(UIPacket.sendCloseWindow(uiId));
    }

    public void openUIWithOption(int uiId, int option) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserOpenUIWithOption.getValue());
        /*
         * 0x03 傳授技能後顯示的窗口
         * 0x15 組隊搜索窗口
         * 0x21 道具修理窗口
         * 0x2A 專業技術窗口
         */
        mplew.writeInt(uiId);
        mplew.writeInt(option);
        mplew.writeInt(0); //V.114新增 未知
        getClient().announce(mplew.getPacket());
    }

    public void openURL(String sURL) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserOpenURL.getValue());
        mplew.write((byte) 0);
        mplew.write((byte) 1);
        mplew.writeMapleAsciiString(sURL);

        getClient().announce(mplew.getPacket());
    }

    public void openWebUI(String sURL) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserOpenURL.getValue());
        mplew.write((byte) 0);
        mplew.write((byte) 1);
        mplew.writeMapleAsciiString(sURL);

        getClient().announce(mplew.getPacket());
    }

    public void openWebUI(int id, String uiPath, String url) {
        getClient().announce(MaplePacketCreator.openWebUI(id, uiPath, url));
    }

    public void playExclSoundWithDownBGM(String data) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_UserEffectLocal);
        hh.write(EffectOpcode.UserEffect_PlayExclSoundWithDownBGM.getValue());
        hh.writeMapleAsciiString(data);
        hh.writeInt(100);
        getClient().announce(hh.getPacket());
    }

    public void playSoundWithMuteBGM(String wzPath) {
        getClient().announce(EffectPacket.playSoundWithMuteBGM(wzPath));
    }

    public void removeAdditionalEffect() {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.RemoveAdditionalEffect, null, null, null));
    }

    public void resetHyperSkill() {
        Map<Integer, SkillEntry> oldList = new HashMap<>(getPlayer().getSkills());
        Map<Integer, SkillEntry> newList = new HashMap<>();
        for (Map.Entry<Integer, SkillEntry> toRemove : oldList.entrySet()) {
            Skill skill = SkillFactory.getSkill(toRemove.getKey());
            if (skill != null && skill.isHyperSkill() && getPlayer().getSkillLevel(toRemove.getKey()) == 1) {
                if (skill.canBeLearnedBy(getPlayer().getJobWithSub())) {
                    newList.put(toRemove.getKey(), new SkillEntry((byte) 0, toRemove.getValue().masterlevel, toRemove.getValue().expiration));
                } else {
                    newList.put(toRemove.getKey(), new SkillEntry((byte) 0, (byte) 0, -1));
                }
            }
        }
        oldList.clear();
        newList.clear();

        getClient().sendEnableActions();
    }

    public void resetHyperStatSkill(int pos) {
        Map<Integer, SkillEntry> oldList = new HashMap<>(getPlayer().getSkills());
        Map<Integer, SkillEntry> newList = new HashMap<>();
        for (Map.Entry<Integer, SkillEntry> toRemove : oldList.entrySet()) {
            Skill skill = SkillFactory.getSkill(toRemove.getKey());
            if (skill != null && skill.isHyperStat() && getPlayer().getSkillLevel(toRemove.getKey()) > 0) {
                newList.put(toRemove.getKey(), new SkillEntry((byte) 0, (byte) 0, -1));
            }
        }
        getPlayer().gainMeso(-10000000, true, true);
        getPlayer().changeSkillsLevel(newList);
        for (MapleHyperStats right : getPlayer().loadHyperStats(pos)) {
            getPlayer().resetHyperStats(right.getPosition(), right.getSkillid());
        }
        getClient().announce(MaplePacketCreator.updateHyperPresets(getPlayer(), pos, (byte) 1));
        oldList.clear();
        newList.clear();
        getClient().sendEnableActions();
    }

    public void resetSkills() {
        getPlayer().clearSkills();
    }

    public void resetVSkills() {
        getPlayer().resetVSkills();
    }

    public void resetStats(short str, short dex, short _int, short luk) {
        getPlayer().resetStats(str, dex, _int, luk);
    }

    public boolean revivePet(long uniqueId, int itemId) {
        Item item = getPlayer().getInventory(MapleInventoryType.CASH).findByLiSN(uniqueId);
        if (item == null) {
            return false;
        }
        if (!ItemConstants.類型.寵物(item.getItemId())) {
            return false;
        }
        if (item.getPet() == null) {
            return false;
        }
        if (MapleItemInformationProvider.getInstance().getLimitedLife(item.getItemId()) != 0) {
            return false;
        }
        if (item.getExpiration() < 0 || item.getExpiration() > System.currentTimeMillis()) {
            return false;
        }
        switch (itemId) {
            case 4070000: // 生命水
            case 5180000: // 生命水
            case 5689000: // 優質生命水
                item.setExpiration(System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L));
                break;
            case 5689005: // 高濃縮優質生命水
                item.setExpiration(System.currentTimeMillis() + (270 * 24 * 60 * 60 * 1000L));
                break;
            case 5180003: // 永恆的生命水
                item.setExpiration(-1);
                break;
            default:
                return false;
        }
        getPlayer().forceReAddItem(item);
        return true;
    }

    public void screenEffect(String name) {
        getPlayer().showScreenEffect(name);
    }

    public void scriptProgressItemMessage(int itemId, String msg) {
        getPlayer().send(UIPacket.ScriptProgressItemMessage(itemId, msg));
    }

    public void scriptProgressMessage(String msg) {
        getClient().announce(UIPacket.getTopMsg(msg));
    }

    public void setAvatarLook(int[] items) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.AvatarLookSet, null, items, null));
    }

    public void setDirectionMod(boolean bSet) {
        getClient().announce(UIPacket.setDirectionMod(bSet));
    }

    public void setDirection(boolean bSet) {
        getPlayer().setDirection(-1);
    }

    public void setFaceOff(int nFaceItemID) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.FaceOff, null, new int[]{nFaceItemID}, null));
    }

    public void setForcedAction(int n2, int n3) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.ForcedAction, null, new int[]{n2, n3}, null));
    }

    public void setForcedFlip(int n2) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.ForcedFlip, null, new int[]{n2}, null));
    }

    public void setForcedInput(int n2) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.ForcedInput, null, new int[]{n2}, null));
    }

    public void setForcedMove(int n2, int n3) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.ForcedMove, null, new int[]{n2, n3}, null));
    }

    public void setInGameCurNodeEventEnd(final boolean inGameCurNode) {
        getPlayer().setInGameCurNode(inGameCurNode);
        getClient().announce(UIPacket.inGameCurNodeEventEnd(inGameCurNode));
    }

    public void setInGameDirectionMode(final boolean b, final boolean b2, final boolean b3, final boolean b4) {
        getClient().announce(UIPacket.SetInGameDirectionMode(b, b2, b3, b4));
    }

    public void inGameDirection22(int var) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.UNK_226_5, var));
    }

    public void sendDirectionEvent(String type, int var) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.valueOf(type), var));
    }

    public void showNpcEffectPlay(int n1, String string, int n2, int n3, int n4, int n7, int n8, int n9, int n10, String str) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.EffectPlay, string, new int[]{n2, n3, n4, 0, 0, n7, n8, n9, n10}, str));
    }

    public void setLayerBlind(final boolean b, final int n, final int n2) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.blind(b ? 1 : 0, n, 0, 0, 0, n2, 0)));
    }

    public void setLayerBlind(final boolean enable, final int n, final int r, final int g, final int b, final int n2, final int unk3) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.blind(enable ? 1 : 0, n, r, g, b, n2, unk3)));
    }

    public void setLayerBlindWhite(final boolean b, final int n, final int n2) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.blind(b ? 1 : 0, n, 255, 255, 255, n2, 0)));
    }

    public void setLayerMove(int n, String s, int n2, int n3) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.onOffLayer(1, n, s, n2, n3, 0, "", 0, false, 0, false, 0, 0)));
    }

    public void setLayerOff(int n, String s) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.onOffLayer(2, n, s, 0, 0, 0, "", 0, false, 0, false, 0, 0)));
    }

    public void setLayerOn(int n, String s, int n2, int n3, int n4, String s2, int n5) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.onOffLayer(0, n, s, n2, n3, n4, s2, n5, true, -1, false, 0, 0)));
    }

    public void setStandAloneMode(boolean enable) {
        getPlayer().send(UIPacket.SetStandAloneMode(enable));
    }

    public void setStaticScreenMessage(final int n, final String s, final boolean b) {
        getClient().announce(UIPacket.setStaticScreenMessage(n, s, b));
    }

    public void setUserEmotionLocal(final int n, final int n2) {
        getClient().announce(UIPacket.UserEmotionLocal(n, n2));
    }

    public void setVansheeMode(int n2) {
        getClient().announce(UIPacket.getDirectionEvent(InGameDirectionEventType.VansheeMode, null, new int[]{n2}, null));
    }

    public void changeBGM(String name) {
        FieldPacket.fieldEffect(FieldEffect.changeBGM(name, 0, 0, 0));
    }

    public void showAvatarOriented(final String s, final boolean toOther) {
        getClient().announce(EffectPacket.showAvatarOriented(s));
        if (toOther) {
            getPlayer().getMap().broadcastMessage(getPlayer(), EffectPacket.showAvatarOriented(getPlayer().getId(), s), false);
        }
    }

    public void showAvatarOrientedRepeat(final boolean b, String s) {
        getClient().announce(EffectPacket.showAvatarOrientedRepeat(b, s));
    }

    public void showBlindEffect(final boolean b) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_UserEffectLocal);
        hh.write(EffectOpcode.UserEffect_BlindEffect.getValue());
        hh.writeBool(b);
        getClient().announce(hh.getPacket());
    }

    public void showDoJangRank() {
        getClient().announce(MaplePacketCreator.getDojangRanking());
    }

    public void showProgressMessageFont(String msg, int fontNameType, int fontSize, int fontColorType, int fadeOutDelay) {
        getClient().announce(UIPacket.getSpecialTopMsg(msg, fontNameType, fontSize, fontColorType, fadeOutDelay));
    }

    public void showReservedEffect(final boolean screenCoord, final int rx, final int ry, final String data) {
        getClient().announce(EffectPacket.showReservedEffect(screenCoord, rx, ry, data));
    }

    public void showScreenAutoLetterBox(final String s, final int n) {
        this.getPlayer().showScreenAutoLetterBox(s, n);
    }

    public void showScreenDelayedEffect(final String s, final int n) {
        this.getPlayer().showScreenDelayedEffect(s, n);
    }

    public void showSpineScreen(int endDelay, String path, String aniamtionName, String str) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.showSpineScreen(endDelay, path, aniamtionName, str)));
    }

    public void offSpineScreen(String str, int val) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.offSpineScreen(str, val, "", 0)));
    }

    public void showSystemMessage(String msg) {
        getClient().announce(MaplePacketCreator.showRedNotice(msg));
    }

    public void showSpouseMessage(int type, String msg) {
        getPlayer().dropSpouseMessage(UserChatMessageType.getByType(type), msg);
    }

    public final void showTopScreenEffect(final String s, final int n) {
        getPlayer().send(FieldPacket.fieldEffect(FieldEffect.getFieldBackgroundEffectFromWz(s, n)));
    }

    public void showWeatherEffectNotice(final String s, final int n, final int n2) {
        getPlayer().getMap().broadcastMessage(UIPacket.showWeatherEffectNotice(s, n, n2, true));
    }

    public void soundEffect(final String s, int vol, int n1, int n2) {
        this.getClient().announce(FieldPacket.fieldEffect(FieldEffect.playSound(s, vol, n1, n2)));
    }

    public void soundEffect(final String s) {
        this.getClient().announce(FieldPacket.fieldEffect(FieldEffect.playSound(s, 100, 0, 0)));
    }

    public void destroyTempNpc(int npcId) {
        getPlayer().getMap().removeNpc(npcId);
    }

    public void spawnTempNpc(int npcId, int x, int y) {
        getPlayer().getMap().spawnNpc(npcId, new Point(x, y));
    }

    public void teleport(final int n, final int n2, int x, int y) {
        getClient().announce(MaplePacketCreator.userTeleport(false, n, n2, new Point(x, y)));
    }

    public void teleportPortal(final int n, final int portal) {
        MapleMap map = getPlayer().getMap();
        if (map == null) {
            return;
        }
        MaplePortal portalPos = map.getPortal(portal);
        if (portalPos == null) {
            return;
        }
        getClient().announce(MaplePacketCreator.userTeleport(false, n, getPlayer().getId(), portalPos.getPosition()));
    }

    public void teleportPortal(final int n, final String portal) {
        MapleMap map = getPlayer().getMap();
        if (map == null) {
            return;
        }
        MaplePortal portalPos = map.getPortal(portal);
        if (portalPos == null) {
            return;
        }
        getClient().announce(MaplePacketCreator.userTeleport(false, n, getPlayer().getId(), portalPos.getPosition()));
    }

    public void teleportToPortalId(int portalID) {
        MapleMap map = getPlayer().getMap();
        if (map == null) {
            return;
        }
        MaplePortal portalPos = map.getPortal(portalID);
        if (portalPos == null) {
            return;
        }
        getClient().announce(MaplePacketCreator.userTeleport(false, 0, getPlayer().getId(), portalPos.getPosition()));
    }

    public void teleportToPortalId(int n, int portalID) {
        MapleMap map = getPlayer().getMap();
        if (map == null) {
            return;
        }
        MaplePortal portalPos = map.getPortal(portalID);
        if (portalPos == null) {
            return;
        }
        getClient().announce(MaplePacketCreator.userTeleport(false, n, getPlayer().getId(), portalPos.getPosition()));
    }

    public void updateDamageSkin(int id) {
        getPlayer().changeDamageSkin(id);
    }

    public void UseItemEffect(int itemId) {
        if (itemId == 0) {
            getPlayer().setItemEffect(0);
            getPlayer().setItemEffectType(0);
        } else {
            Item toUse = getPlayer().getInventory(MapleInventoryType.CASH).findById(itemId); //現金欄道具
            if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
                getClient().sendEnableActions();
                return;
            }
            if (itemId != 5510000) { //原地復活術
                getPlayer().setItemEffect(itemId);
                getPlayer().setItemEffectType(ItemConstants.getInventoryType(itemId).getType());
            }
        }
        getPlayer().getMap().broadcastMessage(getPlayer(), MaplePacketCreator.itemEffect(getPlayer().getId(), itemId, ItemConstants.getInventoryType(itemId).getType()), false);
    }

    public void useSkillEffect(int skillId, int level) {
        SkillFactory.getSkillEffect(skillId, level).applyTo(getPlayer());
    }

    public void useSkillEffect(int skillId, int level, int duration) {
        SkillFactory.getSkillEffect(skillId, level).applyTo(getPlayer(), duration);
    }

    public void useMobSkillEffect(int skillId, int level, int duration) {
        SkillFactory.getSkillEffect(skillId, level).applyToMonster(getPlayer(), duration);
    }

    public boolean hasEffect(int sourceId) {
        return getPlayer().getEffects().get(sourceId).isEmpty();
    }

    public Point getPosition() {
        return getPlayer().getPosition();
    }

//    /**
//     * 账号共享记录值增加1
//     * 一天后重置
//     *
//     * @param event 记录名
//     */
//    addEventValue(event: String): void
//
//    /**
//     * 账号共享记录值增加指定数量
//     * 一天后重置
//     *
//     * @param event 记录名
//     * @param value 增加数量
//     */
//    addEventValue(event: String, value: int): void
//
//    /**
//     * 账号共享记录值增加指定数量
//     * 永不重置
//     *
//     * @param event 记录名
//     * @param value 增加数量
//     */
//    addPermanentEventValue(event: String, value: int): void
//
//    /**
//     * 账号共享记录值增加指定数量
//     * 在设定的天数后重置
//     *
//     * @param event 记录名
//     * @param value 增加数量
//     * @param resetDays 重置天数，-1为永久
//     */
//    addEventValue(event: String, value: int, resetDays: int): void
//
//    /**
//     * 删除指定的账号共享记录
//     *
//     * @param event 记录名
//     */
//    resetEventValue(event: String): void
//
//    /**
//     * 获取指定的账号共享记录
//     *
//     * @param event 记录名
//     * @return
//     */
//    getEventValue(event: String): int

    public void setActionBar(int id) {
        ActionBarField.MapleFieldActionBar bm;
        if ((bm = ActionBarField.MapleFieldActionBar.createActionBar(id)) != null) {
            getPlayer().setActionBar(bm);
        } else {
            getPlayer().setActionBar(null);
        }
    }

    /* 給予名聲值 */
    public void modifyHonor(int val) {
        getPlayer().gainHonor(val);
    }

    public String getAccountName() {
        return getPlayer().getName();
    }

    public int getMapId() {
        return getPlayer().getMapId();
    }

    public boolean gainRandVSkill() {
        return gainRandVSkill(Randomizer.isSuccess(20) ? 0 : 1, false, false);
    }

    public boolean gainRandVSkill(final int nCoreType, final boolean indieJob, final boolean onlyJob) {
        return getPlayer().gainRandVSkill(nCoreType, indieJob, onlyJob);
    }

    public boolean gainVCoreSkill(final int vcoreoid, final int nCount) {
        return getPlayer().gainVCoreSkill(vcoreoid, nCount, false);
    }

    public void runNpc(int npcId, int shopId) {
        MapleShopFactory.getInstance().getShop(shopId).sendShop(getClient(), npcId, true);
    }

    public void openShop(int npcId, int shopId) {
        MapleShopFactory.getInstance().getShop(shopId).sendShop(getClient(), npcId, true);
    }

    public void showSpecialUI(final boolean b, final String s) {
        getClient().announce(UIPacket.ShowSpecialUI(b, s));
    }

    public void showAchieveRate() {
        getClient().announce(MaplePacketCreator.achievementRatio(0));
    }

    public void setAchieveRate(int var1) {
        getClient().announce(MaplePacketCreator.achievementRatio(var1));
    }

    public void zeroTag(boolean beta) {
        getPlayer().zeroTag();
    }

    public void setKeyValue(String key, String value) {
        getPlayer().setKeyValue(key, value);
    }

    public String getKeyValue(String key) {
        return getPlayer().getKeyValue(key);
    }

    public int getIntKeyValue(String key) {
        return Integer.parseInt(getPlayer().getKeyValue(key));
    }

    public void addTrait(String t, int e) {
        getPlayer().getTrait(MapleTraitType.valueOf(t)).addExp(e, getPlayer());
    }

    public void completeMobCollection() {
        MobCollectionFactory.doneCollection(getPlayer());
    }

    public void registerMobCollection(int mobId) {
        MobCollectionFactory.registerMobCollection(getPlayer(), mobId);
    }

    public boolean checkMobCollection(int mobId) {
        return MobCollectionFactory.checkMobCollection(getPlayer(), mobId);
    }

    public boolean checkMobCollection(String s) {
        return MobCollectionFactory.checkMobCollection(getPlayer(), s);
    }

    public void handleRandCollection(int s) {
        MobCollectionFactory.handleRandCollection(getPlayer(), s);
    }

//    initTradeKingShop(): void

    public void registerMobCollectionQuest(int s) {
        MobCollectionFactory.registerMobCollection(getPlayer(), s);
    }

    public boolean hasAndroid() {
        return getPlayer().getAndroid() != null;
    }

    public int getAndroidFace() {
        return getPlayer().getAndroid().getFace();
    }

    public int getAndroidHair() {
        return getPlayer().getAndroid().getHair();
    }

    public int getAndroidSkin() {
        return getPlayer().getAndroid().getSkin();
    }

//    機器人髮型混染
//    setAndroidHair(hair: int, baseColor: int, mixedColor: int, mixedPercent: int): void

//    機器人臉型混染
//    setAndroidFace(face: int, baseColor: int, mixedColor: int, mixedPercent: int): void


    public void updateTowerRank(int stage, int time) {
        int world = getPlayer().getWorld();
        int chrId = getPlayer().getId();
        String chrName = getPlayer().getName();
        DatabaseLoader.DatabaseConnection.domain(con -> {
            ResultSet rs = SqlTool.query(con, "SELECT * FROM `zrank_lobby` WHERE `world` = ? AND `characters_id` = ?", world, chrId);
            if (rs.next()) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.DAY_OF_WEEK, 2);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                SqlTool.update(con, "UPDATE `zrank_lobby` SET `stage` = ?, `time` = ? WHERE `world` = ? AND `characters_id` = ? AND ((`stage` < ? OR (`stage` = ? AND `time` > ?)) OR (`logtime` < ?))", stage, time, world, chrId, stage, stage, time, c.getTime().getTime());
                return null;
            }
            SqlTool.update("INSERT INTO `zrank_lobby` (`world`, `characters_id`, `characters_name`, `stage`, `time`)VALUES (?, ?, ?, ?, ?)", world, chrId, chrName, stage, time);
            return null;
        });
    }

    public void showTextEffect(String message, int second, int posY) {
        getClient().announce(EffectPacket.showCombustionMessage(message, second * 1000, posY));
    }

    public int getJobCategory() {
        return getPlayer().getCarteByJob();
    }

    public void removeWeaponSoul() {
        getPlayer().setSoulMP(0);
    }

    public void removeBuffs() {
        if (getPlayer().getAllBuffs().size() > 0) {
            getPlayer().removeBuffs(true);
        }
//        getPlayer().removeBuffs(true);
    }

    public int getMaxHp() {
        return getPlayer().getStat().getMaxHp();
    }

    public int getMaxMp() {
        return getPlayer().getStat().getMaxMp();
    }

    public long getExpNeededForLevel() {
        if (getPlayer().getLevel() < 300) {
            return GameConstants.getExpNeededForLevel(getPlayer().getLevel() + 1);
        } else {
            return 0;

        }
    }

    /**
     * 自定義buff
     * 下線就會消失，請配合qrex和playerLogin.js或者server進行登入給予buff等操作
     *
     * @param buffName
     * @param levels
     * @throws IOException
     */
    public void customBuff(String buffName, String levels) throws IOException {

        EnumMap<SecondaryStat, Integer> map = new EnumMap<>(SecondaryStat.class);
        ObjectMapper objectMapper = new ObjectMapper();
        // 读取 JSON 文件并转换为 Map<String, Object>
        Map<String, Object> jsonMap = objectMapper.readValue(new File("config/CustomBuff.json"), Map.class);
        Map<String, Object> buffMap = (Map<String, Object>) jsonMap.get(buffName);
        if (buffMap != null) {

            int buffId = ((Number) buffMap.get("buff_id")).intValue();
            if (!ItemConstants.類型.消耗(buffId)) {
                return;
            }
            if (Integer.parseInt(levels) < 1) {
                MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect(buffId);
                effect.applyTo(getPlayer(), 1);
            } else {
                Map<String, Map<String, Integer>> levelsMap = (Map<String, Map<String, Integer>>) buffMap.get("levels");
                if (levelsMap.isEmpty()) {
                    return;
                }
                levelsMap.forEach((level, stats) -> {
                    if (levels.equals(level)) {
                        stats.forEach((stat, value) -> {
                            SecondaryStat secondaryStat = SecondaryStat.valueOf(stat);
                            inc(map, secondaryStat, value);
                        });
                    }
                });
                if (map.isEmpty()) {
                    return;
                }
                MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect(buffId);
                effect.setStatups(map);
                effect.applyTo(getPlayer(), 999999999);
            }
        }
    }


    public void showScreenShaking(int mapID, boolean stop) {
        getClient().announce(UIPacket.screenShake(mapID, stop));
    }

    public void showBalloonMsg(String path, int duration) {
        getPlayer().write(UserLocal.inGameDirectionEvent(InGameDirectionEvent.effectPlay(path, duration,
                new Position(0, -100), 0, 0, true, 0, "")));
    }

    public void showStageClear(int n) {
        getClient().announce(FieldPacket.fieldEffect(FieldEffect.showClearStageExpWindow(n)));
    }

    public void startBurn(int type, long time) {
        getPlayer().setBurningChrType(type);
        getPlayer().setBurningChrTime(time);
    }

    public int getMarriageId() {
        return getPlayer().getMarriageId();
    }

    public void setMarriageId(int playerid) {
        getPlayer().setMarriageId(playerid);
    }


    public int getDressingRoomSlot(int style) {
        int defaultSlot = style == 1 ? 0 : 3;
        MapleCharacter chr = getPlayer();
        if (chr == null) {
            return defaultSlot;
        }
        List<Integer> slots = chr.getSalon().getOrDefault(style, null);
        if (slots == null) {
            slots = new LinkedList<>();
            for (int i = 0; i < defaultSlot; i++) {
                slots.add(0);
            }
            chr.getSalon().put(style, slots);
        }

        return slots.size();
    }

    /**
     * 1-skin, 2-face, 3-hair
     *
     * @param style
     * @param slot
     * @return
     */
    public boolean setDressingRoomSlot(int style, int slot) {
        int maxSlot = style == 1 ? 6 : 102;
        if (slot > maxSlot || getPlayer() == null) {
            return false;
        }

        int oldSlot = getDressingRoomSlot(style);
        List<Integer> slots = getPlayer().getSalon().get(style);
        if (slot < oldSlot) {
            for (int i = 0; i < oldSlot - slot; i++) {
                slots.remove(slots.size() - 1);
            }
        } else if (slot > oldSlot) {
            for (int i = 0; i < slot - oldSlot; i++) {
                slots.add(0);
            }
        }
        getPlayer().send(MaplePacketCreator.encodeUpdateCombingRoomSlotCount(3 - style, 0, slot, slot));
        getPlayer().send(MaplePacketCreator.encodeCombingRoomOldSlotCount(3 - style, 0, oldSlot));
        return true;
    }

    public boolean increaseTrunkCapacity(int gain) {
        if (getPlayer().getTrunk().getSlots() + gain > 128) {
            getPlayer().getTrunk().increaseSlots((byte) 128);
            return false;
        } else {
            getPlayer().getTrunk().increaseSlots((byte) gain);
            return true;
        }

    }

    public void enterCS() {
        InterServerHandler.enterCS(getClient(), getClient().getPlayer());
    }

    public void increaseDamageSkinCapacity() {
        String count = getPlayer().getOneInfo(56829, "count");
        final int damskinslot = count == null ? 1 : Integer.valueOf(count);
        if (damskinslot < GameConstants.DamageSkinSlotMax) {
            getPlayer().updateOneInfo(56829, "count", String.valueOf(damskinslot + 1));
            getPlayer().send(InventoryPacket.UserDamageSkinSaveResult(2, 4, getPlayer()));
            getPlayer().dropMessage(1, "傷害皮膚擴充成功，當前有：" + (damskinslot + 1) + " 格");
        } else {
            getPlayer().dropMessage(1, "傷害皮膚擴充失敗，欄位已超過上限。");
        }
    }

    public void enableEquipSlotExt(int days) {

        getClient().announce(MTSCSPacket.擴充項鏈(days));
    }

//    public void showGiantBossMiniMap(String infos) {
//        chr.send(MaplePacketCreator.SendGiantBossMap(infomap));
//    }

    public boolean runAntiMacro() {
        return MapleAntiMacro.startAnti(getPlayer(), getPlayer(), MapleAntiMacro.ITEM_ANTI, true);
    }

    public int getFame() {
        return getPlayer().getFame();
    }

    public void setFame(int fame) {
        getPlayer().setFame(fame);
    }

    public void updateFamiliars() {
        getPlayer().updateFamiliars();
    }

    public List<MonsterFamiliar> getFamiliars() {
        return getPlayer().getFamiliars();
    }

    public void setHexaCoreLevel(int coreId, int level) {
        MapleHexaSkill mhs = new MapleHexaSkill(coreId, level);
        getPlayer().updateHexaSkill(mhs);
    }

    public void clearHexaSkills() {
        for (Integer mhs : getPlayer().getHexaSkills().keySet()) {
            getPlayer().getHexaSkills().get(mhs).setSkilllv(0);
        }
    }

    public void createParty() {
        if (getPlayer().getParty() == null) {
            Party party = Party.createNewParty(false, false, getPlayer().getName() + "的隊伍", getPlayer().getClient().getWorld());
            PartyMember pm = new PartyMember(getPlayer());
            party.setPartyLeaderID(pm.getCharID());
            party.getPartyMembers()[0] = pm;
            getPlayer().setParty(party);
            getPlayer().write(WvsContext.partyResult(PartyResult.createNewParty(party)));
        }

    }

    public void disbandParty() {
        getPlayer().getParty().disband();
    }

    /***
     * 是否完成某個任務
     * @param questId 任務ID
     * @return 有無
     */
    public boolean isQuestFinished(int questId) {
        return getPlayer().getQuestStatus(questId) == 2;
    }

    /***
     * 檢測玩家背包空間
     * @param type 背包類型
     * @return 剩餘數量
     */
    public short getSpace(int type) {
        return getPlayer().getInventory(MapleInventoryType.getByType((byte) type)).getNumFreeSlot();
    }

    /***
     * 給玩家道具並且裝備上該道具
     * @param itemId
     * @param slot
     */
    public void gainItemAndEquip(int itemId, short slot) {
        MapleInventoryManipulator.addItemAndEquip(getClient(), itemId, slot);
    }

    /***
     * 取得當前職業資料
     */
    public MapleJob getMapleJob() {
        return MapleJob.getById(getPlayer().getJobWithSub());
    }

    /***
     * 取得職業資料
     * @param id
     * @return
     */
    public MapleJob getMapleJobById(int id) {
        return MapleJob.getById(id);
    }

    /***
     * 取得全部職業資料
     * @return
     */
    public MapleJob[] getAllMapleJobs() {
        return MapleJob.values();
    }


    /**
     * 獲取全部背包類型還有剩餘空格嗎
     *
     * @param slot
     * @return
     */
    public boolean getFreeAllSlots(int slot) {
        return getPlayer().canHoldSlots(slot);
    }

    /***
     *  移除某個背包格子道具
     * @param invType
     * @param slot
     * @param quantity
     */
    public void removeSlot(int invType, short slot, short quantity) {
        MapleInventoryManipulator.removeFromSlot(getClient(), MapleInventoryType.getByType((byte) invType), slot, quantity, true);
    }

    /**
     * GM指令 - 根據角色名給玩家道具
     *
     * @param name
     * @param itemId
     * @param quantity
     */
    public void giveItemForPlayerName(String name, int itemId, int quantity) {
        List<Map<String, Object>> result = SqlTool.customSqlResult("SELECT * FROM characters WHERE name =?", name);
        if (!result.isEmpty()) {
            MapleCharacter character = MapleCharacter.getCharacterById((Integer) result.getFirst().get("id"));
            if (character.isOnline()) {
                new ScriptPlayer(character).gainItem(itemId, quantity);
                getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "成功給予。");
            } else {
                getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不在線。");
            }
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不存在。");
        }
    }

    /**
     * GM指令 - 根據地圖id給玩家道具
     *
     * @param mapId
     * @param itemId
     * @param quantity
     */
    public void giveItemForMap(int mapId, int itemId, int quantity) {
        List<MapleCharacter> characters = getClient().getChannelServer().getMapFactory().getMap(mapId).getCharacters();

        for (MapleCharacter character : characters) {
            new ScriptPlayer(character).gainItem(itemId, quantity);
        }

        getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "一共給予了" + characters.size() + "個角色。");
    }

    /**
     * GM指令 - 根據角色名給玩家道具
     *
     * @param name
     * @param type
     * @param quantity
     */
    public void givePointForPlayerName(String name, String type, int quantity) {
        List<Map<String, Object>> result = SqlTool.customSqlResult("SELECT * FROM characters WHERE name =?", name);
        if (!result.isEmpty()) {
            MapleCharacter character = MapleCharacter.getCharacterById((Integer) result.getFirst().get("id"));
            if (character.isOnline()) {
                switch (type) {
                    case "cash":
                        new ScriptPlayer(character).modifyCashShopCurrency(1, quantity);
                        getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "成功給予。");
                        break;
                    case "point":
                        new ScriptPlayer(character).modifyCashShopCurrency(2, quantity);
                        getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "成功給予。");
                        break;
                    case "meso":
                        new ScriptPlayer(character).gainMeso(quantity);
                        getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "成功給予。");
                        break;
                    default:
                        getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "givePoint指令格式錯誤。");
                        break;
                }
            } else {
                getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不在線。");
            }
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不存在。");
        }
    }

    /**
     * GM指令 - 無敵
     */
    public void gmInvincible() {
        if (getPlayer().isGm()) {
            getPlayer().setInvincible(!getPlayer().isInvincible());
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "GM無敵: " + getPlayer().isInvincible());
        }
    }

    /**
     * GM指令 - 隱身
     */
    public void gmHide() {
        if (getPlayer().isGm()) {
            Skill skill = SkillFactory.getSkill(管理員.終極隱藏);
            if (getPlayer().isHidden()) {
                getPlayer().cancelEffect(skill.getEffect(1), false, -1);
            } else {
                skill.getEffect(1).applyTo(getPlayer());
            }
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "GM隱身: " + getPlayer().isHidden());
        }
    }

    /**
     * GM指令 - 殺掉全圖怪物
     */
    public void gmKillMap() {
        if (getPlayer().isGm()) {
            getPlayer().getMap().killAllMonsters(getPlayer(), true);
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "操作成功。");
        }
    }

    /**
     * GM指令 - 調試信息
     *
     * @param debug
     */
    public void gmDebug(String debug) {
        if (getPlayer().isGm()) {
            String[] lis = {"quests", "npcs", "items", "maps", "items", "portals", "events", "reactors", "commands"};
            switch (debug) {
                case "quests":
                case "npcs":
                case "items":
                case "portals":
                case "events":
                case "reactors":
                case "commands":
                    if (!getPlayer().getScriptManagerDebug().contains(debug)) {
                        getPlayer().getScriptManagerDebug().add(debug);
                    } else {
                        getPlayer().getScriptManagerDebug().remove(debug);
                    }
                    break;
                case "maps":
                    if (getPlayer().getScriptManagerDebug().contains("maps")) {
                        getPlayer().getScriptManagerDebug().add("maps");
                        getPlayer().getScriptManagerDebug().add("maps/onUserEnter");
                        getPlayer().getScriptManagerDebug().add("maps/onFirstUserEnter");
                    } else {
                        getPlayer().getScriptManagerDebug().remove("maps");
                        getPlayer().getScriptManagerDebug().remove("maps/onUserEnter");
                        getPlayer().getScriptManagerDebug().remove("maps/onFirstUserEnter");
                    }
                    break;
                case "clear":
                    getPlayer().getScriptManagerDebug().clear();
                    break;
                case "show":
                    break;
                case "all":
                    getPlayer().getScriptManagerDebug().clear();
                    for (String s : lis) {
                        getPlayer().getScriptManagerDebug().add(s);
                    }
                    getPlayer().getScriptManagerDebug().add("maps/onUserEnter");
                    getPlayer().getScriptManagerDebug().add("maps/onFirstUserEnter");
                    break;
                default:
                    getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "指令格式錯誤。");

            }
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "當前過濾調試信息: " + String.join(",", getPlayer().getScriptManagerDebug()));
        }
    }

    /**
     * GM指令 - 獲取在線角色數量
     *
     * @return
     */
    public int getOnlinePlayersNum() {
        int ch = Math.min(ServerConfig.CHANNELS_PER_WORLD, 40);
        int count = 0;
        for (int i = 1; i <= ch; i++) {
            Collection<MapleMap> maps = ChannelServer.getInstance(i).getMapFactory().getAllMaps();
            for (MapleMap map : maps) {
                count += map.getCharacters().size();
            }
        }
        return count;
    }

    /**
     * GM指令 - 獲取怪物資訊
     */
    public void gmGetMob() {
        if (getPlayer().isGm()) {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "──────────────────────────");
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "地圖名： " + getPlayer().getMap().getMapName());
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "地圖id： " + getPlayer().getMap().getId());
            List<MapleMonster> monsters = getPlayer().getMap().getMonsters();
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "怪物數量： " + monsters.size());

            for (MapleMonster monster : monsters) {
                getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), StringData.getMobStringById(monster.getId()) + "(id: " + monster.getId() + ") HP: " + monster.getHp() + "/" + monster.getMaxHP());
            }
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "──────────────────────────");
        }
    }

    /**
     * GM指令 - 通過角色名踢人
     *
     * @param all
     * @param name
     */
    public void KickForPlayerName(boolean all, String name) {
        if (all) {
            int ch = Math.min(ServerConfig.CHANNELS_PER_WORLD, 40);
            for (int i = 1; i <= ch; i++) {
                Collection<MapleMap> maps = ChannelServer.getInstance(i).getMapFactory().getAllMaps();
                for (MapleMap map : maps) {
                    for (MapleCharacter chr : map.getCharacters()) {
                        new ScriptPlayer(chr).dissociateClient();
                    }
                }
            }
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "操作成功。");
        } else {
            List<Map<String, Object>> result = SqlTool.customSqlResult("SELECT * FROM characters WHERE name =?", name);
            if (!result.isEmpty()) {
                MapleCharacter character = MapleCharacter.getCharacterById((Integer) result.getFirst().get("id"));
                if (character.isOnline()) {
                    new ScriptPlayer(character).dissociateClient();
                    getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "操作成功。");
                } else {
                    getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不在線。");
                }
            } else {
                getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不存在。");
            }
        }
    }


    public short getBeginner() {
        return JobConstants.getBeginner(getPlayer().getJob());
    }

    /**
     * GM指令 - 技能無CD
     */
    public void gmCooldown() {
        boolean cooldown = getPlayer().isGmcooldown();
        getPlayer().setGmcooldown(!cooldown);
        getPlayer().resetAllCooldowns(!cooldown);
        if (!cooldown) {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "技能無CD: 開啟");
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "技能無CD: 關閉");
        }

    }

    /**
     * GM指令 - 傷害倍增（分身）
     *
     * @param times
     */
    public void gmSeparation(int times) {
        if (times < 1) times = 1;
        getPlayer().setSeparation(times);
        getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "當前傷害倍數: " + times);
    }

    /**
     * GM指令 - 自動攻擊
     *
     * @param trigger
     */
    public void autoAttack(boolean trigger) {
        if (trigger) {
            getPlayer().getTimerInstance().scheduleAtFixedRate(() -> {
                MapleMap map = getPlayer().getMap();
                double range = Double.POSITIVE_INFINITY;
                List<MapleMapObject> targets = map.getMapObjectsInRange(getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER));
                if (!targets.isEmpty()) {
                    long damage = getPlayer().getCalcDamage().getRandomDamage(getPlayer(), true);
                    MapleMonster mob;
                    for (MapleMapObject monstermo : targets) {
                        mob = (MapleMonster) monstermo;
                        map.broadcastMessage(MobPacket.damageMonster(mob.getObjectId(), damage));
                        mob.damage(getPlayer(), 0, damage, false);
                    }
                }
            }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "自動攻擊: 開啟");
        } else {
            getPlayer().getTimerInstance().shutdownNow();
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "自動攻擊: 關閉");
        }
    }


    /**
     * GM指令 - 封號
     *
     * @param name
     * @param reason
     * @param mac
     */
    public void ban(String name, String reason, boolean mac) {
        List<Map<String, Object>> result = SqlTool.customSqlResult("SELECT * FROM characters WHERE name =?", name);
        if (!result.isEmpty()) {
            MapleCharacter character = MapleCharacter.getCharacterById((Integer) result.getFirst().get("id"));
            if (character.isOnline()) {
                character.ban(reason, mac, false, false);
                getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "操作成功。");
            } else {
                getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不在線。");
            }
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色不存在。");
        }
    }

    /**
     * GM指令 - 設置頻道EXP倍率
     *
     * @param channel
     * @param rate
     */
    public void gmExpRate(int channel, int rate) {
        ChannelServer cs = ChannelServer.getInstance(channel);
        if (cs != null) {
            cs.setExpRate(rate);
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "頻道: " + channel + " 當前EXP倍率為 " + cs.getExpRate());
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "頻道: " + channel + " 不存在。");
        }
    }

    /**
     * GM指令 - 設置頻道掉寶倍率
     *
     * @param channel
     * @param rate
     */
    public void gmDropRate(int channel, int rate) {
        ChannelServer cs = ChannelServer.getInstance(channel);
        if (cs != null) {
            cs.setDropRate(rate);
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "頻道: " + channel + " 當前掉寶倍率為 " + cs.getDropRate());
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "頻道: " + channel + " 不存在。");
        }
    }

    /**
     * GM指令 - 召喚怪物
     *
     * @param mobId
     * @param count
     * @param hp
     */
    public void gmSpawn(int mobId, int count, int hp) {
        if (MapleLifeFactory.getMonster(mobId) != null) {
            for (int i = 0; i < count; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mobId);
                if (hp > 0) {
                    mob.changeBaseHp(hp);
                }
                getPlayer().getMap().spawnMonsterOnGroundBelow(mob, getPlayer().getPosition());
            }
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "怪物不存在。");
        }
    }

    /**
     * 獲取指定背包欄位獲取道具實例
     *
     * @param type
     * @param slot
     * @return
     */
    public ScriptItem getInventorySlot(byte type, short slot) {
        return new ScriptItem(getPlayer().getInventory(type).getItem(slot));
    }

    /**
     * 發送廣播 - notice中必須帶有 { itemid }
     *
     * @param notice
     * @param item
     * @param text
     */
    public void broadcastGachaponMessage(String notice, Item item, String text) {
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.getGachaponMega(getPlayer().getName(), notice, item, 3, getClient().getChannel()));
    }

    /**
     * GM指令 - 解封賬號
     *
     * @param name
     */
    public void unban(String name) {
        boolean trigger = MapleClient.unban(name) >= 0;
        if (MapleClient.unbanIPMacs(name) < 0) {
            trigger = false;
        }
        if (MapleClient.unHellban(name) < 0) {
            trigger = false;
        }
        if (trigger) {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色名: " + name + " 解除封禁。");
        } else {
            getPlayer().dropSpouseMessage(UserChatMessageType.getByType(9), "角色名: " + name + " 不在封禁狀態");
        }
    }

    /* hertz creat */
    public void openPacketUIByAdmin() {
        new GMCommand.PacketUI().execute(getPlayer().getClient(), null);
    }

    public void reloadSkill() {
        Config.load();
        SkillFactory.loadSkillData();
        SkillFactory.loadDelays();
        SkillFactory.loadMemorySkills();
        getPlayer().dropMessage(5, "重載完畢.");
    }

    // v核心碎片
    public void gainVCraftCore(int quantity) {
        getPlayer().gainVCraftCore(quantity);
    }


    // v核心碎片
    public void reload(int type) {
        switch (type){
            case 1:
                Config.load();
                break;
            case 2:
                MapleMonsterInformationProvider.getInstance().clearDrops();
            case 4:
                MapleShopFactory.getInstance().clear();
                getPlayer().dropMessage(5, "重讀商店內容完成。");
                break;

        }
        Config.load();
    }


    public boolean hasMesos(int meso) {
        return getPlayer().getMeso() >= meso;
    }


    public int addHyPay(int hypay) {
        int pay = getHyPay(1);
        int payUsed = getHyPay(2);
        int payReward = getHyPay(4);
        if (hypay > pay) {
            return -1;
        }
        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET pay = ? WHERE accname = ?")) {
                ps.setInt(1, pay - hypay); //當前儲值金額
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException e) {
            log.error("加減儲值信息發生錯誤", e);
            return -1;
        }
    }

    /*
     *  取得里程紀錄點數
     * */
    public int getModifyMileage(){
        return getPlayer().getMileage();
    }


    public int getMaplePoints() {
        return getMaplePoints(false);
    }

    public int getMaplePoints(boolean onlyMPoint) {
        return getClient().getMaplePoints(onlyMPoint);
    }

    public int getMileage() {
        return getClient().getMileage();
    }

    /*
     *  里程點數
     * */
    public int modifyMileage(final int quantity) {
        return modifyMileage(quantity, 2, false, true, null);
    }

    public int modifyMileage(final int quantity, final int type) {
        return modifyMileage(quantity, type, false, true, null);
    }

    public int modifyMileage(final int quantity, final String log) {
        return modifyMileage(quantity, 2, false, true, log);
    }

    public int modifyMileage(final int quantity, final boolean show) {
        return modifyMileage(quantity, 2, show, true, null);
    }


    public int modifyMileage(final int quantity, final int type, final boolean show, final boolean limitMax, final String log) {
        if (quantity == 0) {
            return 0;
        }
        int itemID = 2431872;
        if (quantity > 0 && getMileage() + quantity < 0) {
            if (show) {
                getPlayer().send(UIPacket.ScriptProgressItemMessage(itemID, "里程已達到上限."));
            }
            return 3;
        }
        int result = quantity > 0 ? getClient().rechargeMileage(quantity, type, limitMax, log) : getClient().modifyMileage(quantity);
        if (show && result > 0 && result < 3) {
            switch (result) {
                case 1:
                    getPlayer().send(UIPacket.ScriptProgressItemMessage(itemID, "里程已達到每日上限！"));
                    break;
                case 2:
                    getPlayer().send(UIPacket.ScriptProgressItemMessage(itemID, "里程已達到每月上限！"));
                    break;
            }
            return result;
        }
        if (result == 0 && show && quantity != 0) {
            getPlayer().send(UIPacket.ScriptProgressItemMessage(itemID, (quantity > 0 ? "獲得 " : "消耗 ") + Math.abs(quantity) + " 里程！"));
            getPlayer().send(EffectPacket.showSpecialEffect(EffectOpcode.UserEffect_ExpItemConsumed));
        }
        return result;
    }

    public int updateHypay() {
        int pay = getHyPay(1);
        int payUsed = getHyPay(2);
        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET pay = 0, payUsed = ? WHERE accname = ?")) {
                ps.setInt(1, payUsed + pay);
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException e) {
            log.error("加減儲值信息發生錯誤", e);
            return -1;
        }
    }

    public int addPayReward(int hypay) {
        int pay = getHyPay(1);
        int payUsed = getHyPay(2);
        int payReward = getHyPay(4);
        if (hypay > pay) {
            return -1;
        }
        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET payReward = ? WHERE accname = ?")) {
                ps.setInt(1, payReward + hypay); //消費獎勵
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException e) {
            log.error("加減儲值信息發生錯誤", e);
            return -1;
        }
    }

    public int delPayReward(int pay) {
        int payReward = getHyPay(4);
        if (pay <= 0) {
            return -1;
        }
        if (pay > payReward) {
            return -1;
        }
        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET payReward = ? WHERE accname = ?")) {
                ps.setInt(1, payReward - pay); //消費獎勵
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException ex) {
            log.error("加減消費獎勵信息發生錯誤", ex);
            return -1;
        }
    }


    public void updateReward() {
        List<MapleReward> rewards = new LinkedList<>();
        List<Integer> toRemove = new LinkedList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM rewards WHERE `accid` = ? OR (`accid` IS NULL AND `cid` = ?)")) {
                ps.setInt(1, getPlayer().getAccountID());
                ps.setInt(2, getPlayer().getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (rewards.size() >= 200) {// 避免禮物箱過多道具，導致封包過長38
                            break;
                        }
                        if (rs.getLong("end") > 0 && rs.getLong("end") <= System.currentTimeMillis()) {
                            toRemove.add(rs.getInt("id"));
                            continue;
                        }
                        rewards.add(new MapleReward(
                                rs.getInt("id"),
                                rs.getLong("start"),
                                rs.getLong("end"),
                                rs.getInt("type"),
                                rs.getInt("amount"),
                                rs.getInt("itemId"),
                                rs.getString("desc")));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Unable to update rewards: ", e);
        }
        for (int i : toRemove) {
            deleteReward(i, false);
        }
        getPlayer().send(MaplePacketCreator.updateReward(0, (byte) 0x09, rewards, 0x09));
    }

    public MapleReward getReward(int id) {
        MapleReward reward = null;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM rewards WHERE `id` = ? AND (`accid` = ? OR (`accid` IS NULL AND `cid` = ?))")) {
                ps.setInt(1, id);
                ps.setInt(2, getPlayer().getAccountID());
                ps.setInt(3, getPlayer().getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        reward = new MapleReward(rs.getInt("id"), rs.getLong("start"), rs.getLong("end"), rs.getInt("type"), rs.getInt("amount"), rs.getInt("itemId"), rs.getString("desc"));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Unable to obtain reward information: ", e);
        }
        return reward;
    }

    public void addReward(boolean acc, int type, long amount, int item, String desc) {
        addReward(acc, 0L, 0L, type, amount, item, desc);
    }

    public void addReward(boolean acc, long start, long end, int type, long amount, int itemId, String desc) {
        addReward(acc ? getPlayer().getAccountID() : 0, getPlayer().getId(), start, end, type, amount, itemId, desc);
    }

    public static void addReward(int accid, int cid, long start, long end, int type, long amount, int itemId, String desc) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO rewards (`accid`, `cid`, `start`, `end`, `type`, `amount`, `itemId`, `desc`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                if (accid > 0) {
                    ps.setInt(1, accid);
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                if (cid > 0) {
                    ps.setInt(2, cid);
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setLong(3, start);
                ps.setLong(4, end);
                ps.setInt(5, type);
                ps.setLong(6, amount);
                ps.setInt(7, itemId);
                ps.setString(8, desc);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Unable to obtain reward: ", e);
        }
    }

    public void deleteReward(int id) {
        deleteReward(id, true);
    }

    public void deleteReward(int id, boolean update) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM rewards WHERE `id` = ? AND (`accid` = ? OR (`accid` IS NULL AND `cid` = ?))")) {
                ps.setInt(1, id);
                ps.setInt(2, getPlayer().getAccountID());
                ps.setInt(3, getPlayer().getId());
                ps.execute();
            }
        } catch (SQLException e) {
            log.error("Unable to delete reward: ", e);
        }
        if (update) {
            updateReward();
        }
    }

    public void makeNpc(int npcid){
        int npcId = npcid;
        MapleNPC npc = MapleLifeFactory.getNPC(npcId, getPlayer().getMapId());
        if (npc != null && !npc.getName().equals("MISSINGNO")) {
            int xpos = getPlayer().getPosition().x;
            int ypos = getPlayer().getPosition().y;
            int fh = getPlayer().getMap().getFootholds().findBelow(getPlayer().getPosition()).getId();
            npc.setPosition(getPlayer().getPosition());
            npc.setCy(ypos);
            npc.setRx0(xpos + 50);
            npc.setRx1(xpos - 50);
            npc.setCurrentFh(fh);
            npc.setCustom(true);
            try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, npcId);
                ps.setInt(2, 0);
                ps.setInt(3, fh);
                ps.setInt(4, ypos);
                ps.setInt(4, ypos);
                ps.setInt(5, xpos + 50);
                ps.setInt(6, xpos - 50);
                ps.setString(7, "n");
                ps.setInt(8, xpos);
                ps.setInt(9, ypos);
                ps.setInt(10, getPlayer().getMapId());
                ps.executeUpdate();
            } catch (SQLException e) {
                getPlayer().dropMessage(6, "儲存Npc訊息到資料庫中出現錯誤.");
            }
            getPlayer().getMap().addMapObject(npc);
            getPlayer().getMap().broadcastMessage(NPCPacket.spawnNPC(npc));
        } else {
            getPlayer().dropMessage(6, "你應該輸入一個正確的 Npc-Id.");
        }
    }

    private final Map<String, Object> tempValues = new HashMap<>();

    public Map<String, Object> getTempValues() {
        return tempValues;
    }

    public final void setFamiliarsChanged(final boolean change) {
        this.changed_familiars = change;
    }


    public MonsterFamiliar getSummonedFamiliar() {
        return summonedFamiliar;
    }

    public void removeFamiliarsInfo(final int n) {
        if (familiars.containsKey(n)) {
            changed_familiars = true;
            this.familiars.remove(n);
        }
    }

    public void addFamiliarsInfo(final MonsterFamiliar monsterFamiliar) {
        changed_familiars = true;
        this.familiars.put(monsterFamiliar.getId(), monsterFamiliar);
    }


    public void initFamiliar(final MonsterFamiliar cbr) {
        if (this.summonedFamiliar != null) {
            this.summonedFamiliar.setSummoned(false);
        }
        this.summonedFamiliar = cbr;
        this.summonedFamiliar.setSummoned(true);
    }

    public void removeFamiliar() {
        if (this.summonedFamiliar != null) {
            this.summonedFamiliar.setSummoned(false);
            if (this.map != null) {
                this.map.disappearMapObject(this.summonedFamiliar);
            }
        }
        this.summonedFamiliar = null;
        this.getPlayer().write(OverseasPacket.extraEquipResult(ExtraEquipResult.removeFamiliar(getId(), true)));
    }

    /*
     * 新增函數
     * 帳號下的角色統計計算每日活動次數
     */
    public int getEventCount(String eventId) {
        return getEventCount(eventId, 0);
    }

    public int getEventCount(String eventId, int type) {
        return getEventCount(eventId, type, 1);
    }

    public int getEventCount(String eventId, int type, int resetDay) {
        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            int count = 0;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_event WHERE accId = ? AND eventId = ?");
            ps.setInt(1, this.getAccountId());
            ps.setString(2, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                /*
                 * 年：calendar.get(Calendar.YEAR)
                 * 月：calendar.get(Calendar.MONTH)+1
                 * 日：calendar.get(Calendar.DAY_OF_MONTH)
                 * 星期：calendar.get(Calendar.DAY_OF_WEEK)-1
                 */
                count = rs.getInt("count");
                Timestamp updateTime = rs.getTimestamp("updateTime");
                if (type == 0) {
                    Calendar sqlcal = Calendar.getInstance();
                    if (updateTime != null) {
                        sqlcal.setTimeInMillis(updateTime.getTime());
                    }
                    if (sqlcal.get(Calendar.DAY_OF_MONTH) + resetDay <= Calendar.getInstance().get(Calendar.DAY_OF_MONTH) || sqlcal.get(Calendar.MONTH) + 1 <= Calendar.getInstance().get(Calendar.MONTH) || sqlcal.get(Calendar.YEAR) + 1 <= Calendar.getInstance().get(Calendar.YEAR)) {
                        count = 0;
                        PreparedStatement psu = con.prepareStatement("UPDATE accounts_event SET count = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
                        psu.setInt(1, getPlayer().getAccountID());
                        psu.setString(2, eventId);
                        psu.executeUpdate();
                        psu.close();
                    }
                }
            } else {
                PreparedStatement psu = con.prepareStatement("INSERT INTO accounts_event (accId, eventId, count, type) VALUES (?, ?, ?, ?)");
                psu.setInt(1, getPlayer().getAccountID());
                psu.setString(2, eventId);
                psu.setInt(3, 0);
                psu.setInt(4, type);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
            log.error("獲取 EventCount 次數.", Ex);
            return -1;
        }
    }

    /*
     * 增加帳號下的角色統計計算每日活動次數
     */
    public void setEventCount(String eventId) {
        setEventCount(eventId, 0);
    }

    public void setEventCount(String eventId, int type) {
        setEventCount(eventId, type, 1);
    }

    public void setEventCount(String eventId, int type, int count) {
        setEventCount(eventId, type, count, 1, true);
    }

    public void setEventCount(String eventId, int type, int count, int date, boolean updateTime) {
        int eventCount = getEventCount(eventId, type, date);
        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps;
            if (updateTime) {
                //如果 updateTime 為 ture 則更新 updateTime
                ps = con.prepareStatement("UPDATE accounts_event SET count = ?, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
            } else {
                ps = con.prepareStatement("UPDATE accounts_event SET count = ?, type = ? WHERE accId = ? AND eventId = ?");
            }
            ps.setInt(1, eventCount + count);
            ps.setInt(2, type);
            ps.setInt(3, getPlayer().getAccountID());
            ps.setString(4, eventId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
            log.error("增加 EventCount 次數失敗.", Ex);
        }
    }

    /*
     * 重置帳號下的角色統計計算每日活動次數
     */
    public void resetEventCount(String eventId) {
        resetEventCount(eventId, 0);
    }

    public void resetEventCount(String eventId, int type) {
        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts_event SET count = 0, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
            ps.setInt(1, type);
            ps.setInt(2, this.getAccountId());
            ps.setString(3, eventId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
            log.error("重置 EventCount 次數失敗.", Ex);
        }
    }

    public void chatbyEffect(int showMS, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Enter_Field_UserChat.getValue());
        mplew.writeInt(0);
        mplew.writeInt(showMS);
        mplew.writeMapleAsciiString(message);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(300);
        mplew.writeInt(1);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        getPlayer().send(mplew.getPacket());
    }

    public void isBurning() {
        if(getPlayer().getBurningChrTime() > 0) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldEffect.getValue());
        mplew.write(22);
        mplew.writeMapleAsciiString("Effect/EventEffect2.img/HyperBurning/startEff");
        mplew.writeInt(0);
        mplew.writeInt(-1);
        getPlayer().send(mplew.getPacket());
        }
    }

}
