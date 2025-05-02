/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleCharacter;
import Client.MapleCharacterUtil;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.MapleInventory;
import Client.inventory.MapleInventoryType;
import Config.configs.Config;
import Config.constants.ItemConstants;
import Config.constants.ServerConstants;
import Net.server.MapleItemInformationProvider;
import Net.server.quest.MapleQuest;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import Server.login.JobInfoFlag;
import Server.login.JobType;
import Server.login.LoginInformationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;
import tools.data.MaplePacketReader;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author PlayDK
 */
public class CreateCharHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateCharHandler.class);

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        String name;
        byte gender, skin, hairColor;
        short subcategory;
        Map<JobInfoFlag, Integer> infos = new LinkedHashMap<>();

        JobType job;
        name = slea.readMapleAsciiString();
        int keymode = slea.readInt(); //V.117.1新增 鍵盤模式 1 = 老鍵盤模式  2 = 新鍵盤模式
        if (keymode != 1 && keymode != 2) {
            System.out.println("創建角色錯誤 鍵盤模式錯誤 當前模式: " + keymode);
            c.announce(LoginPacket.charNameResponse(name, (byte) 3));
            return;
        }
        slea.readInt(); // [FF FF FF FF] V.106新增 未知
        int job_type = slea.readInt(); // [01 00 00 00]
        job = JobType.getByType(job_type);
        if (job == null) {
            System.out.println("創建角色錯誤 沒有找到該職業類型的數據 當前職業類型: " + job_type);
            return;
        }
        // 職業開放狀態
        if (!ServerConstants.isOpenJob(job.name())) {
            c.announce(LoginPacket.charNameResponse(name, (byte) 3));
            c.announce(MaplePacketCreator.serverNotice(1, "該職業已關閉創建。"));
            return;
        }
        slea.readShort(); // [00 00] subcategory 暗影雙刀 = 1
        gender = slea.readByte();
        skin = slea.readByte();
        boolean skinOk = skin == 0;
        switch (job) {
            case 凱殷:
            case 菈菈:
            case 琳恩:
                skin = 3;
                skinOk = true;
                break;
            case 皇家騎士團:
            case 米哈逸:
                skin = 10;
                skinOk = true;
                break;
            case 伊利恩:
            case 狂狼勇士:
                skin = 11;
                skinOk = true;
                break;
            case 阿戴爾:
            case 精靈遊俠:
                skin = 12;
                skinOk = true;
                break;
            case 惡魔:
                skinOk = skinOk || skin == 13;
                break;
            case 卡莉:
                skin = 30;
                skinOk = true;
                break;
            case 墨玄: // v263 墨玄重做
                skin = 41;
                skinOk = true;
                break;
        }
        if (!skinOk) {
            System.err.println("創建職業皮膚顏色錯誤, 職業:" + job.name() + " 皮膚:" + skin);
            return;
        }
        hairColor = slea.readByte();
        // 驗證創建角色的可選項是否正確
        int index = 0;
        for (JobInfoFlag jf : JobInfoFlag.values()) {
            if (jf.check(job.flag)) {
                int value = slea.readInt();
                if (!LoginInformationProvider.getInstance().isEligibleItem(gender, index, job == JobType.開拓者 ? job.type : job == JobType.影武者 ? 1 : job.job.getId(), value)) {
                    System.err.println("創建角色確認道具出錯 - 性別:" + gender + " 職業:" + job.name() + " 類型:" + jf.name() + " 值:" + value);
                    c.announce(LoginPacket.charNameResponse(name, (byte) 3));
                    return;
                }
                if (jf == JobInfoFlag.尾巴 || jf == JobInfoFlag.耳朵) {
                    value = ItemConstants.getEffectItemID(value);
                }
                infos.put(jf, value);
                index++;
            } else {
                infos.put(jf, 0);
            }
        }
        slea.readMapleAsciiString();
        slea.readMapleAsciiString();
        slea.readMapleAsciiString();
        slea.readMapleAsciiString();
        slea.readMapleAsciiString();
        slea.readMapleAsciiString();
        slea.readMapleAsciiString();
        int packetSize = slea.readInt();
        slea.read(packetSize);
        slea.readMapleAsciiString();
        slea.readMapleAsciiString();

        if (slea.available() != 0) {
            System.err.println("創建角色讀取訊息出錯, 有未讀取訊息: " + HexTool.toString(slea.read((int) slea.available())));
            return;
        }

        if (Config.isDevelop()) {
            String info
                    = "\r\n名字: " + name
                    + "\r\n職業: " + job.name() + "(序號" + job_type + ")"
                    + "\r\n性別: " + gender
                    + "\r\n皮膚: " + skin
                    + "\r\n髮色: " + hairColor;
            for (Map.Entry<JobInfoFlag, Integer> i : infos.entrySet()) {
                info += "\r\n" + i.getKey().name() + ": " + i.getValue();
            }
            log.info(info);
        }
        //生成1個新角色的模版
        MapleCharacter newchar = MapleCharacter.getDefault(c, job);
        newchar.setWorld((byte) c.getWorldId());
        newchar.setFace(infos.get(JobInfoFlag.臉型));
        int hair = infos.get(JobInfoFlag.髮型);
        if (job == JobType.傑諾) {
            if (hair == 30000) { // 黑色基本冒險造型
                hair = 36137; // 棕色史烏造型
            } else if (hair == 31002) { // 橘色基本冒險造型
                hair = 37467; // 棕色殺人鯨造型
            }
        }
        newchar.setHair(hair); // + 頭髮顏色
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor(skin);
        newchar.setDecorate(infos.get(JobInfoFlag.臉飾));
        if (job.level > 1) {
            newchar.setLevel(job.level);
            newchar.setRemainingAp((short) ((job.level - 1) * 5));
            newchar.getStat().maxhp = 570;
            newchar.getStat().hp = 570;
            newchar.getStat().maxmp = 270;
            newchar.getStat().mp = 270;
        }
        if (job == JobType.神之子) {
            newchar.setLevel((short) 100);
            newchar.getStat().str = 518;
            newchar.getStat().dex = 4;
            newchar.getStat().luk = 4;
            newchar.getStat().int_ = 4;
            newchar.getStat().maxhp = 6485;
            newchar.getStat().hp = 6485;
            newchar.getStat().maxmp = 100;
            newchar.getStat().mp = 100;
            newchar.setRemainingSp(3, 0);
            newchar.setRemainingSp(3, 1);
        } else if (job == JobType.凱內西斯) {
            newchar.getStat().maxhp = 570;
            newchar.getStat().hp = 570;
            newchar.getStat().maxmp = 0;
            newchar.getStat().mp = 0;
            newchar.setRemainingAp((short) 45);
        } else if (job == JobType.惡魔) {
            newchar.getStat().mp = 10;
            newchar.getStat().maxmp = 10;
        }

        //部分職業給初始副手
        switch (job) {
            case 凱內西斯:
                infos.put(JobInfoFlag.副手, 1353200); // 西洋棋─士兵
                break;
            case 伊利恩:
                infos.put(JobInfoFlag.副手, 1353500); // 基礎魔法之翼
                break;
            case 卡蒂娜:
                infos.put(JobInfoFlag.副手, 1353300); // 發射器Type:D
                break;
            case 亞克:
                infos.put(JobInfoFlag.副手, 1353600); // 發射器Type:D
                break;
            case 開拓者:
                infos.put(JobInfoFlag.副手, 1353700); // 潛力遺物
                break;
        }
        //給新角色裝備
        MapleInventory equipedIv = newchar.getInventory(MapleInventoryType.EQUIPPED);
        Item item;
        int[] equips = new int[]{
                infos.get(JobInfoFlag.帽子),
                infos.get(JobInfoFlag.衣服),
                infos.get(JobInfoFlag.褲裙),
                infos.get(JobInfoFlag.披風),
                infos.get(JobInfoFlag.鞋子),
                infos.get(JobInfoFlag.手套),
                infos.get(JobInfoFlag.武器),
                infos.get(JobInfoFlag.副手),};
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (int i : equips) {
            if (i > 0) {
                short[] equipSlot = ItemConstants.getEquipedSlot(i);
                if (equipSlot == null || equipSlot.length < 1) {
                    System.err.println("創建角色新增裝備出錯, 裝備欄位未知, 道具ID" + i);
                    continue;
                }
                item = ii.getEquipById(i);
                item.setPosition(equipSlot[0]);
                item.setGMLog("角色創建");
                equipedIv.addFromDB(item);
            }
        }
        //給新角色藥水
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000013, (byte) 0, (short) 100, 0));
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000014, (byte) 0, (short) 100, 0));
        //給新角色指南書
        int[][] guidebooks = new int[][]{{4161001, 0}, {4161047, 1}, {4161048, 2000}, {4161052, 2001}, {4161054, 3}, {4161079, 2002}};
        int guidebook = 0;
        for (int[] i : guidebooks) {
            if (newchar.getJob() == i[1]) {
                guidebook = i[0];
            } else if (newchar.getJob() / 1000 == i[1]) {
                guidebook = i[0];
            }
        }
        if (guidebook > 0) {
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(guidebook, (byte) 0, (short) 1, 0));
        }
        //騎士團職業給角色任務
        if (job.equals(JobType.皇家騎士團)) {
            newchar.setQuestAdd(MapleQuest.getInstance(20022), (byte) 1, "1");
            newchar.setQuestAdd(MapleQuest.getInstance(20010), (byte) 1, null);
        }
        //開始檢測發送創建新的角色
        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (c.isGm() || c.canMakeCharacter(c.getWorldId()))) {
            MapleCharacter.saveNewCharToDB(newchar, job, keymode == 1);
            c.announce(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getId());
        } else {
            c.announce(LoginPacket.addNewCharEntry(newchar, false));
        }
    }
}
