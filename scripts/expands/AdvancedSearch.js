var isAngel = player.getBeginner() == 6001
var isZero = player.getBeginner() == 10000

var str = ""
str += "================#e高級檢索工具#n================"
str += "\r\n#L1#道具#l"
str += "\r\n#L2#NPC#l"
str += "\r\n#L3#地圖#l"
str += "\r\n#L4#怪物#l"
str += "\r\n#L5#任務#l"
str += "\r\n#L6#技能#l"
str += "\r\n#L13#V核心#l"
str += "\r\n#L7#職業#l"
// str += "\r\n#L8#裝備套裝#l"
// str += "\r\n#L9#伺服器包頭#l"
// str += "\r\n#L10#用戶端包頭#l"
// str += "\r\n#L14#膚色#l"
// str += "\r\n#L11#髮型#l"
// str += "\r\n#L12#臉型#l"

var select = npc.askMenu(str)
var searchVal = npc.defText("").minLen(0).maxLen(1000).askTextX("請輸入需要檢索的訊息:")
var list = npc.getSearchData(select, searchVal)
if (list === null) {
    npc.say("搜尋不到訊息")
} else {
    var choice
    switch (select) {
        case 11:
        case 12:
        case 14:
            break
        default:
            var text = npc.searchData(select, searchVal)
            if (!text.includes("搜尋不到此")) {
                choice = npc.askMenu(text)
            } else {
                npc.say(text)
            }
            break
    }
    if (choice != null) {
        switch (select) {
            case 1:
                if (choice < 1000000) {
                    var secChoice
                    if (isAngel) {
                        secChoice = npc.askAngelicBuster()
                        player.getPlayer().changeBeauty(choice, secChoice)
                    } else if (isZero) {
                        secChoice = npc.askMenu("請選擇要接受變更的角色.#b\r\n\r\n#b#L0#神之子阿爾法#l\r\n#L1#神之子貝塔#l\r\n#L2#神之子阿爾法 + 神之子貝塔#l")
                        player.getPlayer().changeBeauty(choice, secChoice == 1)
                        if (secChoice == 2) {
                            player.getPlayer().changeBeauty(choice, true)
                        }
                    } else {
                        player.getPlayer().changeBeauty(choice, false)
                    }
                } else if (choice < 2000000) {
                    var toDrop = sh.itemEquip(choice).copy(); // 生成一個Eq
                   //toDrop.setPad(30000); // 素質["物攻"]
                   //toDrop.setMad(30000); // 素質["魔攻"]
                   //toDrop.setPdd(30000); // 素質["防"]
                   //toDrop.setAcc(30000); // 素質["命中"]
                   //toDrop.setBossDamage(100); // 素質["王傷"]
                   //toDrop.setIgnorePDR(100); // 素質["無視"]
                   //toDrop.setTotalDamage(100); // 素質["總傷"]
                   //toDrop.setEnhance(30); // 素質["星力"]
                    player.gainItem(toDrop);
                } else if (choice >= 5000000 && choice < 5010000) {
                    if (player.canGainItem(select, 1)) {
                        var day = -1
                        if (npc.askYesNo("選中的寵物為#i" + choice + ":# #z" + choice + "#\r\n是否需要自定義生命時間(不設定則根據原道具時間給)?"))
                            day = npc.askNumber("選中的寵物為#i" + select + ":# #z" + select + "#\r\n請輸入生命時間(天 0為永久):", 1 , 1 ,365);
                        player.gainItem(choice, 1, day)
                    }
                } else {
                    if (player.canGainItem(choice, 1)) {
                        var nCount = npc.askNumber("選中的道具為#i" + choice + ":# #z" + choice + "#\r\n請輸入製作數量:", 1 , 1 ,32766);
                        player.gainItem(choice, nCount);
                    }
                }
                break
            case 2:
                player.dropMessage(5, "打開NPC,ID:" + choice)
                player.getPlayer().openNpc(choice)
                break
            case 3:
                player.dropMessage(5, "傳送到地圖,ID:" + choice)
                player.changeMap(choice, 0)
                break
            case 4:
                var x = player.getPosition().x;
                var y = player.getPosition().y;
                var nCount = npc.askNumber("選中的怪物為#o" + choice + "#\r\n請輸入召喚數量:", 1 , 1 ,32766);
                for (var i = 0; i < nCount; i++) {
                    map.spawnMob(choice, x, y);
                }
                break
            case 5:
                var qs = npc.askMenu("選中的任務ID為" + choice + "\r\n請選擇需要執行的操作:\r\n#L0#開始任務#l\r\n#L1#完成任務#l")
                switch (qs) {
                    case 0:
                        player.startQuest(choice)
                        break
                    case 1:
                        player.completeQuest(choice, 933027)
                        break
                }
                break
            case 6:
                var lv = npc.askNumber("選中的技能ID為" + choice + "\r\n請輸入使用等級:", 1 , 1 ,32766);
                player.useSkillEffect(choice, lv)
                break
            case 13:
                var count = npc.askNumber("選中的V核心ID為" + choice + "\r\n請輸入製作數量:", 1 , 1 ,32766);
                player.gainVCoreSkill(choice, count)
                break
            case 7:
                player.dropMessage(5, "進行職業,本次改變職業代碼:" + choice);
                player.setJob(choice);
                // 最大化技能
                switch (choice) {
                    case 100: // subJob 劍士
                        player.maxSkills();
                        player.setJob(100);
                        player.maxSkills();
                        break;
                    case 110: // subJob 狂戰士
                        player.maxSkills();
                        player.setJob(110);
                        player.maxSkills();
                        break;
                    case 111: // subJob 十字軍
                        player.maxSkills();
                        player.setJob(110);
                        player.maxSkills();
                        player.setJob(111);
                        player.maxSkills();
                        break;
                    case 112: // subJob 英雄
                        player.maxSkills();
                        player.setJob(110);
                        player.maxSkills();
                        player.setJob(111);
                        player.maxSkills();
                        player.setJob(112);
                        player.maxSkills();
                        break;
                    case 120: // subJob 見習騎士
                        player.maxSkills();
                        player.setJob(120);
                        player.maxSkills();
                        break;
                    case 121: // subJob 騎士
                        player.maxSkills();
                        player.setJob(120);
                        player.maxSkills();
                        player.setJob(121);
                        player.maxSkills();
                        break;
                    case 122: // subJob 聖騎士
                        player.maxSkills();
                        player.setJob(120);
                        player.maxSkills();
                        player.setJob(121);
                        player.maxSkills();
                        player.setJob(122);
                        player.maxSkills();
                        break;
                    case 130: // subJob 槍騎兵
                        player.maxSkills();
                        player.setJob(130);
                        player.maxSkills();
                        break;
                    case 131: // subJob 嗜血狂騎
                        player.maxSkills();
                        player.setJob(130);
                        player.maxSkills();
                        player.setJob(131);
                        player.maxSkills();
                        break;
                    case 132: // subJob 黑騎士
                        player.maxSkills();
                        player.setJob(130);
                        player.maxSkills();
                        player.setJob(131);
                        player.maxSkills();
                        player.setJob(132);
                        player.maxSkills();
                        break;
                    case 200: // subJob 法師
                        player.maxSkills();
                        break;
                    case 210: // subJob 巫師（火，毒）
                        player.maxSkills();
                        player.setJob(210);
                        player.maxSkills();
                        break;
                    case 211: // subJob 魔導士（火，毒）
                        player.maxSkills();
                        player.setJob(210);
                        player.maxSkills();
                        player.setJob(211);
                        player.maxSkills();
                        break;
                    case 212: // subJob 大魔導士（火，毒）
                        player.maxSkills();
                        player.setJob(210);
                        player.maxSkills();
                        player.setJob(211);
                        player.maxSkills();
                        player.setJob(212);
                        player.maxSkills();
                        break;
                    case 220: // subJob 巫師（冰，雷）
                        player.maxSkills();
                        player.setJob(220);
                        player.maxSkills();
                        break;
                    case 221: // subJob 魔導士（冰，雷）
                        player.maxSkills();
                        player.setJob(220);
                        player.maxSkills();
                        player.setJob(221);
                        player.maxSkills();
                        break;
                    case 222: // subJob 大魔導士（冰，雷）
                        player.maxSkills();
                        player.setJob(220);
                        player.maxSkills();
                        player.setJob(221);
                        player.maxSkills();
                        player.setJob(222);
                        player.maxSkills();
                        break;
                    case 230: // subJob 僧侶
                        player.maxSkills();
                        player.setJob(210);
                        player.maxSkills();
                        player.setJob(230);
                        player.maxSkills();
                        break;
                    case 231: // subJob 祭司
                        player.maxSkills();
                        player.setJob(210);
                        player.maxSkills();
                        player.setJob(230);
                        player.maxSkills();
                        player.setJob(231);
                        player.maxSkills();
                        break;
                    case 232: // subJob 主教
                        player.maxSkills();
                        player.setJob(210);
                        player.maxSkills();
                        player.setJob(230);
                        player.maxSkills();
                        player.setJob(231);
                        player.maxSkills();
                        player.setJob(232);
                        player.maxSkills();
                        break;
                    case 300: // subJob 弓箭手
                        player.maxSkills();
                        break;
                    case 301: // subJob 開拓者
                        player.maxSkills();
                        player.setJob(301);
                        player.maxSkills();
                        break;
                    case 310: // subJob 獵人
                        player.maxSkills();
                        player.setJob(310);
                        player.maxSkills();
                        break;
                    case 311: // subJob 遊俠
                        player.maxSkills();
                        player.setJob(310);
                        player.maxSkills();
                        player.setJob(311);
                        player.maxSkills();
                        break;
                    case 312: // subJob 箭神
                        player.maxSkills();
                        player.setJob(310);
                        player.maxSkills();
                        player.setJob(311);
                        player.maxSkills();
                        player.setJob(312);
                        player.maxSkills();
                        break;
                    case 320: // subJob 弩弓手
                        player.maxSkills();
                        player.setJob(320);
                        player.maxSkills();
                        break;
                    case 321: // subJob 狙擊手
                        player.maxSkills();
                        player.setJob(320);
                        player.maxSkills();
                        player.setJob(321);
                        player.maxSkills();
                        break;
                    case 322: // subJob 神射手
                        player.maxSkills();
                        player.setJob(320);
                        player.maxSkills();
                        player.setJob(321);
                        player.maxSkills();
                        player.setJob(322);
                        player.maxSkills();
                        break;
                    case 330: // subJob 開拓者
                        player.maxSkills();
                        player.setJob(301);
                        player.maxSkills();
                        player.setJob(330);
                        player.maxSkills();
                        break;
                    case 331: // subJob 開拓者
                        player.maxSkills();
                        player.setJob(301);
                        player.maxSkills();
                        player.setJob(330);
                        player.maxSkills();
                        player.setJob(331);
                        player.maxSkills();
                        break;
                    case 332: // subJob 開拓者
                        player.maxSkills();
                        player.setJob(301);
                        player.maxSkills();
                        player.setJob(330);
                        player.maxSkills();
                        player.setJob(331);
                        player.maxSkills();
                        player.setJob(332);
                        player.maxSkills();
                        break;
                    case 400: // subJob 盜賊
                        player.maxSkills();
                        break;
                    case 401: // subJob 盜賊影武
                        player.maxSkills();
                        break;
                    case 410: // subJob 刺客
                        player.maxSkills();
                        player.setJob(410);
                        player.maxSkills();
                        break;
                    case 411: // subJob 暗殺者
                        player.maxSkills();
                        player.setJob(410);
                        player.maxSkills();
                        player.setJob(411);
                        player.maxSkills();
                        break;
                    case 412: // subJob 夜使者
                        player.maxSkills();
                        player.setJob(410);
                        player.maxSkills();
                        player.setJob(411);
                        player.maxSkills();
                        player.setJob(412);
                        player.maxSkills();
                        break;
                    case 420: // subJob 俠盜
                        player.maxSkills();
                        player.setJob(420);
                        player.maxSkills();
                        break;
                    case 421: // subJob 神偷
                        player.maxSkills();
                        player.setJob(420);
                        player.maxSkills();
                        player.setJob(421);
                        player.maxSkills();
                        break;
                    case 422: // subJob 暗影神偷
                        player.maxSkills();
                        player.setJob(420);
                        player.maxSkills();
                        player.setJob(421);
                        player.maxSkills();
                        player.setJob(422);
                        player.maxSkills();
                        break;
                    case 430: // subJob 下忍
                        player.maxSkills();
                        player.setJob(430);
                        player.maxSkills();
                        break;
                    case 431: // subJob 中忍
                        player.maxSkills();
                        player.setJob(430);
                        player.maxSkills();
                        player.setJob(431);
                        player.maxSkills();
                        break;
                    case 432: // subJob 上忍
                        player.maxSkills();
                        player.setJob(430);
                        player.maxSkills();
                        player.setJob(431);
                        player.maxSkills();
                        player.setJob(432);
                        player.maxSkills();
                        break;
                    case 433: // subJob 隱忍
                        player.maxSkills();
                        player.setJob(430);
                        player.maxSkills();
                        player.setJob(431);
                        player.maxSkills();
                        player.setJob(432);
                        player.maxSkills();
                        player.setJob(433);
                        player.maxSkills();
                        break;
                    case 434: // subJob 影武者
                        player.maxSkills();
                        player.setJob(430);
                        player.maxSkills();
                        player.setJob(431);
                        player.maxSkills();
                        player.setJob(432);
                        player.maxSkills();
                        player.setJob(433);
                        player.maxSkills();
                        player.setJob(434);
                        player.maxSkills();
                        break;
                    case 500: // subJob 海盜
                        player.maxSkills();
                        break;
                    case 501: // subJob 砲手
                        player.maxSkills();
                        player.setJob(501);
                        player.maxSkills();
                        break;
                    case 510: // subJob 打手
                        player.maxSkills();
                        player.setJob(501);
                        player.maxSkills();
                        player.setJob(510);
                        player.maxSkills();
                        break;
                    case 511: // subJob 格鬥家
                        player.maxSkills();
                        player.setJob(501);
                        player.maxSkills();
                        player.setJob(510);
                        player.maxSkills();
                        player.setJob(511);
                        player.maxSkills();
                        break;
                    case 512: // subJob 拳霸
                        player.maxSkills();
                        player.setJob(501);
                        player.maxSkills();
                        player.setJob(510);
                        player.maxSkills();
                        player.setJob(511);
                        player.maxSkills();
                        player.setJob(512);
                        player.maxSkills();
                        break;
                    case 520: // subJob 槍手
                        player.maxSkills();
                        player.setJob(520);
                        player.maxSkills();
                        break;
                    case 521: // subJob 神槍手
                        player.maxSkills();
                        player.setJob(520);
                        player.maxSkills();
                        player.setJob(521);
                        player.maxSkills();
                        break;
                    case 522: // subJob 槍神
                        player.maxSkills();
                        player.setJob(520);
                        player.maxSkills();
                        player.setJob(521);
                        player.maxSkills();
                        player.setJob(522);
                        player.maxSkills();
                        break;
                    case 530: // subJob 重砲兵
                        player.maxSkills();
                        player.setJob(530);
                        player.maxSkills();
                        break;
                    case 531: // subJob 重砲兵隊長
                        player.maxSkills();
                        player.setJob(530);
                        player.maxSkills();
                        player.setJob(531);
                        player.maxSkills();
                        break;
                    case 532: // subJob 重砲指揮官
                        player.maxSkills();
                        player.setJob(530);
                        player.maxSkills();
                        player.setJob(531);
                        player.maxSkills();
                        player.setJob(532);
                        player.maxSkills();
                        break;
                    case 1000: // subJob 貴族
                        player.maxSkills();
                        break;
                    case 1100: // subJob 聖魂劍士
                        player.maxSkills();
                        player.setJob(1100);
                        player.maxSkills();
                        break;
                    case 1110: // subJob 聖魂劍士
                        player.maxSkills();
                        player.setJob(1100);
                        player.maxSkills();
                        player.setJob(1110);
                        player.maxSkills();
                        break;
                    case 1111: // subJob 聖魂劍士
                        player.maxSkills();
                        player.setJob(1100);
                        player.maxSkills();
                        player.setJob(1110);
                        player.maxSkills();
                        player.setJob(1111);
                        player.maxSkills();
                        break;
                    case 1112: // subJob 聖魂劍士
                        player.maxSkills();
                        player.setJob(1100);
                        player.maxSkills();
                        player.setJob(1110);
                        player.maxSkills();
                        player.setJob(1111);
                        player.maxSkills();
                        player.setJob(1112);
                        player.maxSkills();
                        break;
                    case 1200: // subJob 烈焰巫師
                        player.maxSkills();
                        player.setJob(1200);
                        player.maxSkills();
                        break;
                    case 1210: // subJob 烈焰巫師
                        player.maxSkills();
                        player.setJob(1200);
                        player.maxSkills();
                        player.setJob(1210);
                        player.maxSkills();
                        break;
                    case 1211: // subJob 烈焰巫師
                        player.maxSkills();
                        player.setJob(1200);
                        player.maxSkills();
                        player.setJob(1210);
                        player.maxSkills();
                        player.setJob(1211);
                        player.maxSkills();
                        break;
                    case 1212: // subJob 烈焰巫師
                        player.maxSkills();
                        player.setJob(1200);
                        player.maxSkills();
                        player.setJob(1210);
                        player.maxSkills();
                        player.setJob(1211);
                        player.maxSkills();
                        player.setJob(1212);
                        player.maxSkills();
                        break;
                    case 1300: // subJob 破風使者
                        player.maxSkills();
                        player.setJob(1300);
                        player.maxSkills();
                        break;
                    case 1310: // subJob 破風使者
                        player.maxSkills();
                        player.setJob(1300);
                        player.maxSkills();
                        player.setJob(1310);
                        player.maxSkills();
                        break;
                    case 1311: // subJob 破風使者
                        player.maxSkills();
                        player.setJob(1300);
                        player.maxSkills();
                        player.setJob(1310);
                        player.maxSkills();
                        player.setJob(1311);
                        player.maxSkills();
                        break;
                    case 1312: // subJob 破風使者
                        player.maxSkills();
                        player.setJob(1300);
                        player.maxSkills();
                        player.setJob(1310);
                        player.maxSkills();
                        player.setJob(1311);
                        player.maxSkills();
                        player.setJob(1312);
                        player.maxSkills();
                        break;
                    case 1400: // subJob 暗夜行者
                        player.maxSkills();
                        player.setJob(1400);
                        player.maxSkills();
                        break;
                    case 1410: // subJob 暗夜行者
                        player.maxSkills();
                        player.setJob(1400);
                        player.maxSkills();
                        player.setJob(1410);
                        player.maxSkills();
                        break;
                    case 1411: // subJob 暗夜行者
                        player.maxSkills();
                        player.setJob(1400);
                        player.maxSkills();
                        player.setJob(1410);
                        player.maxSkills();
                        player.setJob(1411);
                        player.maxSkills();
                        break;
                    case 1412: // subJob 暗夜行者
                        player.maxSkills();
                        player.setJob(1400);
                        player.maxSkills();
                        player.setJob(1410);
                        player.maxSkills();
                        player.setJob(1411);
                        player.maxSkills();
                        player.setJob(1412);
                        player.maxSkills();
                        break;
                    case 1500: // subJob 閃雷悍將
                        player.maxSkills();
                        player.setJob(1500);
                        player.maxSkills();
                        break;
                    case 1510: // subJob 閃雷悍將
                        player.maxSkills();
                        player.setJob(1500);
                        player.maxSkills();
                        player.setJob(1510);
                        player.maxSkills();
                        break;
                    case 1511: // subJob 閃雷悍將
                        player.maxSkills();
                        player.setJob(1500);
                        player.maxSkills();
                        player.setJob(1510);
                        player.maxSkills();
                        player.setJob(1511);
                        player.maxSkills();
                        break;
                    case 1512: // subJob 閃雷悍將
                        player.maxSkills();
                        player.setJob(1500);
                        player.maxSkills();
                        player.setJob(1510);
                        player.maxSkills();
                        player.setJob(1511);
                        player.maxSkills();
                        player.setJob(1512);
                        player.maxSkills();
                        break;
                    case 2000: // subJob 傳說
                        player.maxSkills();
                        break;
                    case 2001: // subJob 龍魔導士
                        player.maxSkills();
                        player.setJob(2001);
                        player.maxSkills();
                        break;
                    case 2002: // subJob 精靈遊俠
                        player.maxSkills();
                        player.setJob(2002);
                        player.maxSkills();
                        break;
                    case 2003: // subJob 幻影俠盜
                        player.maxSkills();
                        player.setJob(2003);
                        player.maxSkills();
                        break;
                    case 2004: // subJob 夜光
                        player.maxSkills();
                        player.setJob(2004);
                        player.maxSkills();
                        break;
                    case 2005: // subJob 隱月
                        player.maxSkills();
                        player.setJob(2005);
                        player.maxSkills();
                        break;
                    case 2100: // subJob 狂狼勇士
                        player.maxSkills();
                        player.setJob(2100);
                        player.maxSkills();
                        break;
                    case 2110: // subJob 狂狼勇士
                        player.maxSkills();
                        player.setJob(2100);
                        player.maxSkills();
                        player.setJob(2110);
                        player.maxSkills();
                        break;
                    case 2111: // subJob 狂狼勇士
                        player.maxSkills();
                        player.setJob(2100);
                        player.maxSkills();
                        player.setJob(2110);
                        player.maxSkills();
                        player.setJob(2111);
                        player.maxSkills();
                        break;
                    case 2112: // subJob 狂狼勇士
                        player.maxSkills();
                        player.setJob(2100);
                        player.maxSkills();
                        player.setJob(2110);
                        player.maxSkills();
                        player.setJob(2111);
                        player.maxSkills();
                        player.setJob(2112);
                        player.maxSkills();
                        break;
                    case 2200: // subJob 龍魔導士
                        player.maxSkills();
                        player.setJob(2200);
                        player.maxSkills();
                        break;
                    case 2211: // subJob 龍魔導士
                        player.maxSkills();
                        player.setJob(2200);
                        player.maxSkills();
                        player.setJob(2211);
                        player.maxSkills();
                        break;
                    case 2214: // subJob 龍魔導士
                        player.maxSkills();
                        player.setJob(2200);
                        player.maxSkills();
                        player.setJob(2211);
                        player.maxSkills();
                        player.setJob(2214);
                        player.maxSkills();
                        break;
                    case 2217: // subJob 龍魔導士
                        player.maxSkills();
                        player.setJob(2200);
                        player.maxSkills();
                        player.setJob(2211);
                        player.maxSkills();
                        player.setJob(2214);
                        player.maxSkills();
                        player.setJob(2217);
                        player.maxSkills();
                        break;
                    case 2300: // subJob 精靈遊俠
                        player.maxSkills();
                        player.setJob(2300);
                        player.maxSkills();
                        break;
                    case 2310: // subJob 精靈遊俠
                        player.maxSkills();
                        player.setJob(2300);
                        player.maxSkills();
                        player.setJob(2310);
                        player.maxSkills();
                        break;
                    case 2311: // subJob 精靈遊俠
                        player.maxSkills();
                        player.setJob(2300);
                        player.maxSkills();
                        player.setJob(2310);
                        player.maxSkills();
                        player.setJob(2311);
                        player.maxSkills();
                        break;
                    case 2312: // subJob 精靈遊俠
                        player.maxSkills();
                        player.setJob(2300);
                        player.maxSkills();
                        player.setJob(2310);
                        player.maxSkills();
                        player.setJob(2311);
                        player.maxSkills();
                        player.setJob(2312);
                        player.maxSkills();
                        break;
                    case 2400: // subJob 幻影俠盜
                        player.maxSkills();
                        player.setJob(2400);
                        player.maxSkills();
                        break;
                    case 2410: // subJob 幻影俠盜
                        player.maxSkills();
                        player.setJob(2400);
                        player.maxSkills();
                        player.setJob(2410);
                        player.maxSkills();
                        break;
                    case 2411: // subJob 幻影俠盜
                        player.maxSkills();
                        player.setJob(2400);
                        player.maxSkills();
                        player.setJob(2410);
                        player.maxSkills();
                        player.setJob(2411);
                        player.maxSkills();
                        break;
                    case 2412: // subJob 幻影俠盜
                        player.maxSkills();
                        player.setJob(2400);
                        player.maxSkills();
                        player.setJob(2410);
                        player.maxSkills();
                        player.setJob(2411);
                        player.maxSkills();
                        player.setJob(2412);
                        player.maxSkills();
                        break;
                    case 2500: // subJob 隱月
                        player.maxSkills();
                        player.setJob(2500);
                        player.maxSkills();
                        break;
                    case 2510: // subJob 隱月
                        player.maxSkills();
                        player.setJob(2500);
                        player.maxSkills();
                        player.setJob(2510);
                        player.maxSkills();
                        break;
                    case 2511: // subJob 隱月
                        player.maxSkills();
                        player.setJob(2500);
                        player.maxSkills();
                        player.setJob(2510);
                        player.maxSkills();
                        player.setJob(2511);
                        player.maxSkills();
                        break;
                    case 2512: // subJob 隱月
                        player.maxSkills();
                        player.setJob(2500);
                        player.maxSkills();
                        player.setJob(2510);
                        player.maxSkills();
                        player.setJob(2511);
                        player.maxSkills();
                        player.setJob(2512);
                        player.maxSkills();
                        break;
                    case 2700: // subJob 夜光
                        player.maxSkills();
                        player.setJob(2700);
                        player.maxSkills();
                        break;
                    case 2710: // subJob 夜光
                        player.maxSkills();
                        player.setJob(2700);
                        player.maxSkills();
                        player.setJob(2710);
                        player.maxSkills();
                        break;
                    case 2711: // subJob 夜光
                        player.maxSkills();
                        player.setJob(2700);
                        player.maxSkills();
                        player.setJob(2710);
                        player.maxSkills();
                        player.setJob(2711);
                        player.maxSkills();
                        break;
                    case 2712: // subJob 夜光
                        player.maxSkills();
                        player.setJob(2700);
                        player.maxSkills();
                        player.setJob(2710);
                        player.maxSkills();
                        player.setJob(2711);
                        player.maxSkills();
                        player.setJob(2712);
                        player.maxSkills();
                        break;
                    case 3000: // subJob 市民
                        player.maxSkills();
                        break;
                    case 3001: // subJob 惡魔
                        player.maxSkills();
                        player.setJob(3001);
                        player.maxSkills();
                        break;
                    case 3002: // subJob 傑諾
                        player.maxSkills();
                        player.setJob(3002);
                        player.maxSkills();
                        break;
                    case 3100: // subJob 惡魔殺手
                        player.maxSkills();
                        player.setJob(3100);
                        player.maxSkills();
                        break;
                    case 3101: // subJob 惡魔復仇者
                        player.maxSkills();
                        player.setJob(3101);
                        player.maxSkills();
                        break;
                    case 3110: // subJob 惡魔殺手
                        player.maxSkills();
                        player.setJob(3110);
                        player.maxSkills();
                        break;
                    case 3111: // subJob 惡魔殺手
                        player.maxSkills();
                        player.setJob(3110);
                        player.maxSkills();
                        player.setJob(3111);
                        player.maxSkills();
                        break;
                    case 3112: // subJob 惡魔殺手
                        player.maxSkills();
                        player.setJob(3110);
                        player.maxSkills();
                        player.setJob(3111);
                        player.maxSkills();
                        player.setJob(3112);
                        player.maxSkills();
                        break;
                    case 3120: // subJob 惡魔復仇者
                        player.maxSkills();
                        player.setJob(3120);
                        player.maxSkills();
                        break;
                    case 3121: // subJob 惡魔復仇者
                        player.maxSkills();
                        player.setJob(3120);
                        player.maxSkills();
                        player.setJob(3121);
                        player.maxSkills();
                        break;
                    case 3122: // subJob 惡魔復仇者
                        player.maxSkills();
                        player.setJob(3120);
                        player.maxSkills();
                        player.setJob(3121);
                        player.maxSkills();
                        player.setJob(3122);
                        player.maxSkills();
                        break;
                    case 3200: // subJob 煉獄巫師
                        player.maxSkills();
                        player.setJob(3200);
                        player.maxSkills();
                        break;
                    case 3210: // subJob 煉獄巫師
                        player.maxSkills();
                        player.setJob(3200);
                        player.maxSkills();
                        player.setJob(3210);
                        player.maxSkills();
                        break;
                    case 3211: // subJob 煉獄巫師
                        player.maxSkills();
                        player.setJob(3200);
                        player.maxSkills();
                        player.setJob(3210);
                        player.maxSkills();
                        player.setJob(3211);
                        player.maxSkills();
                        break;
                    case 3212: // subJob 煉獄巫師
                        player.maxSkills();
                        player.setJob(3200);
                        player.maxSkills();
                        player.setJob(3210);
                        player.maxSkills();
                        player.setJob(3211);
                        player.maxSkills();
                        player.setJob(3212);
                        player.maxSkills();
                        break;
                    case 3300: // subJob 狂豹獵人
                        player.maxSkills();
                        player.setJob(3300);
                        player.maxSkills();
                        break;
                    case 3310: // subJob 狂豹獵人
                        player.maxSkills();
                        player.setJob(3300);
                        player.maxSkills();
                        player.setJob(3310);
                        player.maxSkills();
                        break;
                    case 3311: // subJob 狂豹獵人
                        player.maxSkills();
                        player.setJob(3300);
                        player.maxSkills();
                        player.setJob(3310);
                        player.maxSkills();
                        player.setJob(3311);
                        player.maxSkills();
                        break;
                    case 3312: // subJob 狂豹獵人
                        player.maxSkills();
                        player.setJob(3300);
                        player.maxSkills();
                        player.setJob(3310);
                        player.maxSkills();
                        player.setJob(3311);
                        player.maxSkills();
                        player.setJob(3312);
                        player.maxSkills();
                        break;
                    case 3500: // subJob 機甲戰神
                        player.maxSkills();
                        player.setJob(3500);
                        player.maxSkills();
                        break;
                    case 3510: // subJob 機甲戰神
                        player.maxSkills();
                        player.setJob(3500);
                        player.maxSkills();
                        player.setJob(3510);
                        player.maxSkills();
                        break;
                    case 3511: // subJob 機甲戰神
                        player.maxSkills();
                        player.setJob(3500);
                        player.maxSkills();
                        player.setJob(3510);
                        player.maxSkills();
                        player.setJob(3511);
                        player.maxSkills();
                        break;
                    case 3512: // subJob 機甲戰神
                        player.maxSkills();
                        player.setJob(3500);
                        player.maxSkills();
                        player.setJob(3510);
                        player.maxSkills();
                        player.setJob(3511);
                        player.maxSkills();
                        player.setJob(3512);
                        player.maxSkills();
                        break;
                    case 3600: // subJob 傑諾
                        player.maxSkills();
                        player.setJob(3600);
                        player.maxSkills();
                        break;
                    case 3610: // subJob 傑諾
                        player.maxSkills();
                        player.setJob(3600);
                        player.maxSkills();
                        player.setJob(3610);
                        player.maxSkills();
                        break;
                    case 3611: // subJob 傑諾
                        player.maxSkills();
                        player.setJob(3600);
                        player.maxSkills();
                        player.setJob(3610);
                        player.maxSkills();
                        player.setJob(3611);
                        player.maxSkills();
                        break;
                    case 3612: // subJob 傑諾
                        player.maxSkills();
                        player.setJob(3600);
                        player.maxSkills();
                        player.setJob(3610);
                        player.maxSkills();
                        player.setJob(3611);
                        player.maxSkills();
                        player.setJob(3612);
                        player.maxSkills();
                        break;
                    case 3700: // subJob 爆拳槍神
                        player.maxSkills();
                        player.setJob(3700);
                        player.maxSkills();
                        break;
                    case 3710: // subJob 爆拳槍神
                        player.maxSkills();
                        player.setJob(3700);
                        player.maxSkills();
                        player.setJob(3710);
                        player.maxSkills();
                        break;
                    case 3711: // subJob 爆拳槍神
                        player.maxSkills();
                        player.setJob(3700);
                        player.maxSkills();
                        player.setJob(3710);
                        player.maxSkills();
                        player.setJob(3711);
                        player.maxSkills();
                        break;
                    case 3712: // subJob 爆拳槍神
                        player.maxSkills();
                        player.setJob(3700);
                        player.maxSkills();
                        player.setJob(3710);
                        player.maxSkills();
                        player.setJob(3711);
                        player.maxSkills();
                        player.setJob(3712);
                        player.maxSkills();
                        break;
                    case 4001: // subJob 劍豪
                        player.maxSkills();
                        player.setJob(4001);
                        player.maxSkills();
                        break;
                    case 4002: // subJob 陰陽師
                        player.maxSkills();
                        player.setJob(4002);
                        player.maxSkills();
                        break;
                    case 4100: // subJob 劍豪
                        player.maxSkills();
                        player.setJob(4001);
                        player.maxSkills();
                        player.setJob(4100);
                        player.maxSkills();
                        break;
                    case 4110: // subJob 劍豪
                        player.maxSkills();
                        player.setJob(4001);
                        player.maxSkills();
                        player.setJob(4100);
                        player.maxSkills();
                        player.setJob(4110);
                        player.maxSkills();
                        break;
                    case 4111: // subJob 劍豪
                        player.maxSkills();
                        player.setJob(4001);
                        player.maxSkills();
                        player.setJob(4100);
                        player.maxSkills();
                        player.setJob(4110);
                        player.maxSkills();
                        player.setJob(4111);
                        player.maxSkills();
                        break;
                    case 4112: // subJob 劍豪
                        player.maxSkills();
                        player.setJob(4001);
                        player.maxSkills();
                        player.setJob(4100);
                        player.maxSkills();
                        player.setJob(4110);
                        player.maxSkills();
                        player.setJob(4111);
                        player.maxSkills();
                        player.setJob(4112);
                        player.maxSkills();
                        break;
                    case 4200: // subJob 陰陽師
                        player.maxSkills();
                        player.setJob(4002);
                        player.maxSkills();
                        player.setJob(4200);
                        player.maxSkills();
                        break;
                    case 4210: // subJob 陰陽師
                        player.maxSkills();
                        player.setJob(4002);
                        player.maxSkills();
                        player.setJob(4200);
                        player.maxSkills();
                        player.setJob(4210);
                        player.maxSkills();
                        break;
                    case 4211: // subJob 陰陽師
                        player.maxSkills();
                        player.setJob(4002);
                        player.maxSkills();
                        player.setJob(4200);
                        player.maxSkills();
                        player.setJob(4210);
                        player.maxSkills();
                        player.setJob(4211);
                        player.maxSkills();
                        break;
                    case 4212: // subJob 陰陽師
                        player.maxSkills();
                        player.setJob(4002);
                        player.maxSkills();
                        player.setJob(4200);
                        player.maxSkills();
                        player.setJob(4210);
                        player.maxSkills();
                        player.setJob(4211);
                        player.maxSkills();
                        player.setJob(4212);
                        player.maxSkills();
                        break;
                    case 5000: // subJob 米哈逸
                        player.maxSkills();
                        player.setJob(5000);
                        player.maxSkills();
                        break;
                    case 5100: // subJob 米哈逸
                        player.maxSkills();
                        player.setJob(5000);
                        player.maxSkills();
                        player.setJob(5100);
                        player.maxSkills();
                        break;
                    case 5110: // subJob 米哈逸
                        player.maxSkills();
                        player.setJob(5000);
                        player.maxSkills();
                        player.setJob(5100);
                        player.maxSkills();
                        player.setJob(5110);
                        player.maxSkills();
                        break;
                    case 5111: // subJob 米哈逸
                        player.maxSkills();
                        player.setJob(5000);
                        player.maxSkills();
                        player.setJob(5100);
                        player.maxSkills();
                        player.setJob(5110);
                        player.maxSkills();
                        player.setJob(5111);
                        player.maxSkills();
                        break;
                    case 5112: // subJob 米哈逸
                        player.maxSkills();
                        player.setJob(5000);
                        player.maxSkills();
                        player.setJob(5100);
                        player.maxSkills();
                        player.setJob(5110);
                        player.maxSkills();
                        player.setJob(5111);
                        player.maxSkills();
                        player.setJob(5112);
                        player.maxSkills();
                        break;
                    case 6000: // subJob 凱撒
                        player.maxSkills();
                        player.setJob(6000);
                        player.maxSkills();
                        break;
                    case 6001: // subJob 天使破壞者
                        player.maxSkills();
                        player.setJob(6001);
                        player.maxSkills();
                        break;
                    case 6002: // subJob 卡蒂娜
                        player.maxSkills();
                        player.setJob(6002);
                        player.maxSkills();
                        break;
                    case 6003: // subJob 凱殷
                        player.maxSkills();
                        player.setJob(6003);
                        player.maxSkills();
                        break;
                    case 6100: // subJob 凱撒
                        player.maxSkills();
                        player.setJob(6000);
                        player.maxSkills();
                        player.setJob(6100);
                        player.maxSkills();
                        break;
                    case 6110: // subJob 凱撒
                        player.maxSkills();
                        player.setJob(6000);
                        player.maxSkills();
                        player.setJob(6100);
                        player.maxSkills();
                        player.setJob(6110);
                        player.maxSkills();
                        break;
                    case 6111: // subJob 凱撒
                        player.maxSkills();
                        player.setJob(6000);
                        player.maxSkills();
                        player.setJob(6100);
                        player.maxSkills();
                        player.setJob(6110);
                        player.maxSkills();
                        player.setJob(6111);
                        player.maxSkills();
                        break;
                    case 6112: // subJob 凱撒
                        player.maxSkills();
                        player.setJob(6000);
                        player.maxSkills();
                        player.setJob(6100);
                        player.maxSkills();
                        player.setJob(6110);
                        player.maxSkills();
                        player.setJob(6111);
                        player.maxSkills();
                        player.setJob(6112);
                        player.maxSkills();
                        break;
                    case 6300: // subJob 凱殷
                        player.maxSkills();
                        player.setJob(6300);
                        player.maxSkills();
                        break;
                    case 6310: // subJob 凱殷
                        player.maxSkills();
                        player.setJob(6300);
                        player.maxSkills();
                        player.setJob(6310);
                        player.maxSkills();
                        break;
                    case 6311: // subJob 凱殷
                        player.maxSkills();
                        player.setJob(6300);
                        player.maxSkills();
                        player.setJob(6310);
                        player.maxSkills();
                        player.setJob(6311);
                        player.maxSkills();
                        break;
                    case 6312: // subJob 凱殷
                        player.maxSkills();
                        player.setJob(6300);
                        player.maxSkills();
                        player.setJob(6310);
                        player.maxSkills();
                        player.setJob(6311);
                        player.maxSkills();
                        player.setJob(6312);
                        player.maxSkills();
                        break;
                    case 6400: // subJob 卡蒂娜
                        player.maxSkills();
                        player.setJob(6002);
                        player.maxSkills();
                        player.setJob(6400);
                        player.maxSkills();
                        break;
                    case 6410: // subJob 卡蒂娜
                        player.maxSkills();
                        player.setJob(6002);
                        player.maxSkills();
                        player.setJob(6400);
                        player.maxSkills();
                        player.setJob(6410);
                        player.maxSkills();
                        break;
                    case 6411: // subJob 卡蒂娜
                        player.maxSkills();
                        player.setJob(6002);
                        player.maxSkills();
                        player.setJob(6400);
                        player.maxSkills();
                        player.setJob(6410);
                        player.maxSkills();
                        player.setJob(6411);
                        player.maxSkills();
                        break;
                    case 6412: // subJob 卡蒂娜
                        player.maxSkills();
                        player.setJob(6002);
                        player.maxSkills();
                        player.setJob(6400);
                        player.maxSkills();
                        player.setJob(6410);
                        player.maxSkills();
                        player.setJob(6411);
                        player.maxSkills();
                        player.setJob(6412);
                        player.maxSkills();
                        break;
                    case 6500: // subJob 天使破壞者
                        player.maxSkills();
                        player.setJob(6001);
                        player.maxSkills();
                        player.setJob(6500);
                        player.maxSkills();
                        break;
                    case 6510: // subJob 天使破壞者
                        player.maxSkills();
                        player.setJob(6001);
                        player.maxSkills();
                        player.setJob(6500);
                        player.maxSkills();
                        player.setJob(6510);
                        player.maxSkills();
                        break;
                    case 6511: // subJob 天使破壞者
                        player.maxSkills();
                        player.setJob(6001);
                        player.maxSkills();
                        player.setJob(6500);
                        player.maxSkills();
                        player.setJob(6510);
                        player.maxSkills();
                        player.setJob(6511);
                        player.maxSkills();
                        break;
                    case 6512: // subJob 天使破壞者
                        player.maxSkills();
                        player.setJob(6001);
                        player.maxSkills();
                        player.setJob(6500);
                        player.maxSkills();
                        player.setJob(6510);
                        player.maxSkills();
                        player.setJob(6511);
                        player.maxSkills();
                        player.setJob(6512);
                        player.maxSkills();
                        break;
                    case 11000: // subJob 幻獸師
                        player.maxSkills();
                        player.setJob(11000);
                        player.maxSkills();
                        break;
                    case 11200: // subJob 幻獸師
                        player.maxSkills();
                        player.setJob(11000);
                        player.maxSkills();
                        player.setJob(11200);
                        player.maxSkills();
                        break;
                    case 11210: // subJob 幻獸師
                        player.maxSkills();
                        player.setJob(11000);
                        player.maxSkills();
                        player.setJob(11200);
                        player.maxSkills();
                        player.setJob(11210);
                        player.maxSkills();
                        break;
                    case 11211: // subJob 幻獸師
                        player.maxSkills();
                        player.setJob(11000);
                        player.maxSkills();
                        player.setJob(11200);
                        player.maxSkills();
                        player.setJob(11210);
                        player.maxSkills();
                        player.setJob(11211);
                        player.maxSkills();
                        break;
                    case 11212: // subJob 幻獸師
                        player.maxSkills();
                        player.setJob(11000);
                        player.maxSkills();
                        player.setJob(11200);
                        player.maxSkills();
                        player.setJob(11210);
                        player.maxSkills();
                        player.setJob(11211);
                        player.maxSkills();
                        player.setJob(11212);
                        player.maxSkills();
                        break;
                    case 14000: // subJob 凱內西斯
                        player.maxSkills();
                        player.setJob(14000);
                        player.maxSkills();
                        break;
                    case 14200: // subJob 凱內西斯
                        player.maxSkills();
                        player.setJob(14000);
                        player.maxSkills();
                        player.setJob(14200);
                        player.maxSkills();
                        break;
                    case 14210: // subJob 凱內西斯
                        player.maxSkills();
                        player.setJob(14000);
                        player.maxSkills();
                        player.setJob(14200);
                        player.maxSkills();
                        player.setJob(14210);
                        player.maxSkills();
                        break;
                    case 14211: // subJob 凱內西斯
                        player.maxSkills();
                        player.setJob(14000);
                        player.maxSkills();
                        player.setJob(14200);
                        player.maxSkills();
                        player.setJob(14210);
                        player.maxSkills();
                        player.setJob(14211);
                        player.maxSkills();
                        break;
                    case 14212: // subJob 凱內西斯
                        player.maxSkills();
                        player.setJob(14000);
                        player.maxSkills();
                        player.setJob(14200);
                        player.maxSkills();
                        player.setJob(14210);
                        player.maxSkills();
                        player.setJob(14211);
                        player.maxSkills();
                        player.setJob(14212);
                        player.maxSkills();
                        break;
                    case 15000: // subJob 伊利恩
                        player.maxSkills();
                        player.setJob(15000);
                        player.maxSkills();
                        break;
                    case 15001: // subJob 亞克
                        player.maxSkills();
                        player.setJob(15001);
                        player.maxSkills();
                        break;
                    case 15002: // subJob 阿戴爾
                        player.maxSkills();
                        player.setJob(15002);
                        player.maxSkills();
                        break;
                    case 15003: // subJob 卡莉
                        player.maxSkills();
                        player.setJob(15003);
                        player.maxSkills();
                        break;
                    case 15100: // subJob 阿戴爾
                        player.maxSkills();
                        player.setJob(15002);
                        player.maxSkills();
                        player.setJob(15100);
                        player.maxSkills();
                        break;
                    case 15110: // subJob 阿戴爾
                        player.maxSkills();
                        player.setJob(15002);
                        player.maxSkills();
                        player.setJob(15100);
                        player.maxSkills();
                        player.setJob(15110);
                        player.maxSkills();
                        break;
                    case 15111: // subJob 阿戴爾
                        player.maxSkills();
                        player.setJob(15002);
                        player.maxSkills();
                        player.setJob(15100);
                        player.maxSkills();
                        player.setJob(15110);
                        player.maxSkills();
                        player.setJob(15111);
                        player.maxSkills();
                        break;
                    case 15112: // subJob 阿戴爾
                        player.maxSkills();
                        player.setJob(15002);
                        player.maxSkills();
                        player.setJob(15100);
                        player.maxSkills();
                        player.setJob(15110);
                        player.maxSkills();
                        player.setJob(15111);
                        player.maxSkills();
                        player.setJob(15112);
                        player.maxSkills();
                        break;
                    case 15200: // subJob 伊利恩
                        player.maxSkills();
                        player.setJob(15200);
                        player.maxSkills();
                        break;
                    case 15210: // subJob 伊利恩
                        player.maxSkills();
                        player.setJob(15200);
                        player.maxSkills();
                        player.setJob(15210);
                        player.maxSkills();
                        break;
                    case 15211: // subJob 伊利恩
                        player.maxSkills();
                        player.setJob(15200);
                        player.maxSkills();
                        player.setJob(15210);
                        player.maxSkills();
                        player.setJob(15211);
                        player.maxSkills();
                        break;
                    case 15212: // subJob 伊利恩
                        player.maxSkills();
                        player.setJob(15200);
                        player.maxSkills();
                        player.setJob(15210);
                        player.maxSkills();
                        player.setJob(15211);
                        player.maxSkills();
                        player.setJob(15212);
                        player.maxSkills();
                        break;
                    case 15400: // subJob 卡莉
                        player.maxSkills();
                        player.setJob(15400);
                        player.maxSkills();
                        break;
                    case 15410: // subJob 卡莉
                        player.maxSkills();
                        player.setJob(15400);
                        player.maxSkills();
                        player.setJob(15410);
                        player.maxSkills();
                        break;
                    case 15411: // subJob 卡莉
                        player.maxSkills();
                        player.setJob(15400);
                        player.maxSkills();
                        player.setJob(15410);
                        player.maxSkills();
                        player.setJob(15411);
                        player.maxSkills();
                        break;
                    case 15412: // subJob 卡莉
                        player.maxSkills();
                        player.setJob(15400);
                        player.maxSkills();
                        player.setJob(15410);
                        player.maxSkills();
                        player.setJob(15411);
                        player.maxSkills();
                        player.setJob(15412);
                        player.maxSkills();
                        break;
                    case 15500: // subJob 亞克
                        player.maxSkills();
                        player.setJob(15500);
                        player.maxSkills();
                        break;
                    case 15510: // subJob 亞克
                        player.maxSkills();
                        player.setJob(15500);
                        player.maxSkills();
                        player.setJob(15510);
                        player.maxSkills();
                        break;
                    case 15511: // subJob 亞克
                        player.maxSkills();
                        player.setJob(15500);
                        player.maxSkills();
                        player.setJob(15510);
                        player.maxSkills();
                        player.setJob(15511);
                        player.maxSkills();
                        break;
                    case 15512: // subJob 亞克
                        player.maxSkills();
                        player.setJob(15500);
                        player.maxSkills();
                        player.setJob(15510);
                        player.maxSkills();
                        player.setJob(15511);
                        player.maxSkills();
                        player.setJob(15512);
                        player.maxSkills();
                        break;
                    case 16000: // subJob 虎影
                        player.maxSkills();
                        player.setJob(16000);
                        player.maxSkills();
                        break;
                    case 16001: // subJob 菈菈
                        player.maxSkills();
                        player.setJob(16000);
                        player.maxSkills();
                        player.setJob(16001);
                        player.maxSkills();
                        break;
                    case 16200: // subJob 菈菈
                        player.maxSkills();
                        player.setJob(16000);
                        player.maxSkills();
                        player.setJob(16001);
                        player.maxSkills();
                        player.setJob(16200);
                        player.maxSkills();
                        break;
                    case 16210: // subJob 菈菈
                        player.maxSkills();
                        player.setJob(16000);
                        player.maxSkills();
                        player.setJob(16001);
                        player.maxSkills();
                        player.setJob(16200);
                        player.maxSkills();
                        player.setJob(16210);
                        player.maxSkills();
                        break;
                    case 16211: // subJob 菈菈
                        player.maxSkills();
                        player.setJob(16000);
                        player.maxSkills();
                        player.setJob(16001);
                        player.maxSkills();
                        player.setJob(16200);
                        player.maxSkills();
                        player.setJob(16210);
                        player.maxSkills();
                        player.setJob(16211);
                        player.maxSkills();
                        break;
                    case 16212: // subJob 菈菈
                        player.maxSkills();
                        player.setJob(16000);
                        player.maxSkills();
                        player.setJob(16001);
                        player.maxSkills();
                        player.setJob(16200);
                        player.maxSkills();
                        player.setJob(16210);
                        player.maxSkills();
                        player.setJob(16211);
                        player.maxSkills();
                        player.setJob(16212);
                        player.maxSkills();
                        break;
                    case 16400: // subJob 虎影
                        player.maxSkills();
                        player.setJob(16400);
                        player.maxSkills();
                        break;
                    case 16410: // subJob 虎影
                        player.maxSkills();
                        player.setJob(16400);
                        player.maxSkills();
                        player.setJob(16410);
                        player.maxSkills();
                        break;
                    case 16411: // subJob 虎影
                        player.maxSkills();
                        player.setJob(16400);
                        player.maxSkills();
                        player.setJob(16410);
                        player.maxSkills();
                        player.setJob(16411);
                        player.maxSkills();
                        break;
                    case 16412: // subJob 虎影
                        player.maxSkills();
                        player.setJob(16400);
                        player.maxSkills();
                        player.setJob(16410);
                        player.maxSkills();
                        player.setJob(16411);
                        player.maxSkills();
                        player.setJob(16412);
                        player.maxSkills();
                        break;
                    case 17000: // subJob 墨玄
                        player.maxSkills();
                        player.setJob(17000);
                        player.maxSkills();
                        break;
                    case 17001: // subJob 琳恩
                        player.maxSkills();
                        player.setJob(17001);
                        player.maxSkills();
                        break;
                    case 17200: // subJob 琳恩
                        player.maxSkills();
                        player.setJob(17001);
                        player.maxSkills();
                        player.setJob(17200);
                        player.maxSkills();
                        break;
                    case 17210: // subJob 琳恩
                        player.maxSkills();
                        player.setJob(17001);
                        player.maxSkills();
                        player.setJob(17200);
                        player.maxSkills();
                        player.setJob(17210);
                        player.maxSkills();
                        break;
                    case 17211: // subJob 琳恩
                        player.maxSkills();
                        player.setJob(17001);
                        player.maxSkills();
                        player.setJob(17200);
                        player.maxSkills();
                        player.setJob(17210);
                        player.maxSkills();
                        player.setJob(17211);
                        player.maxSkills();
                        break;
                    case 17212: // subJob 琳恩
                        player.maxSkills();
                        player.setJob(17001);
                        player.maxSkills();
                        player.setJob(17200);
                        player.maxSkills();
                        player.setJob(17210);
                        player.maxSkills();
                        player.setJob(17211);
                        player.maxSkills();
                        player.setJob(17212);
                        player.maxSkills();
                        break;
                    case 17500: // subJob 墨玄
                        player.maxSkills();
                        player.setJob(17000);
                        player.maxSkills();
                        player.setJob(17500);
                        player.maxSkills();
                        break;
                    case 17510: // subJob 墨玄
                        player.maxSkills();
                        player.setJob(17000);
                        player.maxSkills();
                        player.setJob(17500);
                        player.maxSkills();
                        player.setJob(17510);
                        player.maxSkills();
                        break;
                    case 17511: // subJob 墨玄
                        player.maxSkills();
                        player.setJob(17000);
                        player.maxSkills();
                        player.setJob(17500);
                        player.maxSkills();
                        player.setJob(17510);
                        player.maxSkills();
                        player.setJob(17511);
                        player.maxSkills();
                        break;
                    case 17512: // subJob 墨玄
                        player.maxSkills();
                        player.setJob(17000);
                        player.maxSkills();
                        player.setJob(17500);
                        player.maxSkills();
                        player.setJob(17510);
                        player.maxSkills();
                        player.setJob(17511);
                        player.maxSkills();
                        player.setJob(17512);
                        player.maxSkills();
                        break;
                }
                break
            // case 8:
            //     var setItemArrays = npc.getSetItems(choice)
            //     var sel = ""
            //     for (var num = 0 ; num < setItemArrays.length ; num++) {
            //         var i = setItemArrays[num]
            //         sel += "\r\n#L" + i + "##i" + i + ":# #z" + i + "#(" + i + ")#l"
            //     }
            //     var itemId = npc.askMenu("選中的套裝ID為" + choice + sel)
            //     if (player.canHold(choice)) {
            //         player.gainItem("高級檢索", choice, 1)
            //     }
            //     break
            // case 11:
            // case 12:
            // case 14:
            //     var secondLookValue = 0
            //     if (isAngel) {
            //         if (npc.askAngelicBuster())
            //             secondLookValue = 1
            //     } else if (isZero) {
            //         var sChoice = npc.askMenu("請選擇要接受變更的角色.#b\r\n\r\n#b#L0#神之子阿爾法#l\r\n#L1#神之子貝塔#l\r\n#L2#神之子阿爾法 + 神之子貝塔#l")
            //         secondLookValue = sChoice == 1 ? 2 : sChoice == 2 ? 101 : 0
            //     }
            //     var cardID = select == 11 ? 5150043 : select == 12 ? 5152050 : 5153013;
            //     choice = npc.askAvatar("", cardID, secondLookValue, 0, list)
            //     player.changeBeauty(list[choice], secondLookValue == 1 || secondLookValue == 2)
            //     if (secondLookValue == 101)
            //         player.changeBeauty(list[choice], true)
            //     if (select == 14)
            //         player.dropMessage(5, "更變膚色, 膚色代碼:" + list[choice])
            //     else {
            //         var type = select == 11 ? "髮型" : select == 12 ? "臉型" : "膚色"
            //         player.dropMessage(5, "更變" + type + "為 " + sh.getItemName(list[choice]) + "(" + list[choice] + ")")
            //     }
            //     break
            default:
                break
        }
    }
}
