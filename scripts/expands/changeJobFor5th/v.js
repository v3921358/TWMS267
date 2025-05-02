/* global npc, player */

/**
 * V核心大師
 * NpcId 1540945
 *
 */

if (player.isQuestCompleted(1465)) {
    let response = npc.askMenu("#d你想變得更強嗎?這裡是#r五轉中心#b\r\n#L0#我想強化#r V核心#l\r\n#L10087##b我想購買#r V碎片#b#l\r\n#L2##b增加#r神祕徽章#b後3格裝備欄<#v4000295##v4000236# x1500、5億楓幣>#l\r\n" + (player.isGm() ? "\r\n\r\nGM才能看到\r\n#L4#完成#r五轉#b#l#L3##r免費領取核心碎片9999#b#l" : ""), 3003307);

    switch (response) {
        case 0:
            player.openUI(1131);
            break;
        case 10087:
            // 購買碎片
            var needNX = 1000; // 每片碎片所需的點券
            var number = npc.askNumber("#b請輸入購買碎片的數量\r\n持有楓點:" + player.getCashShopCurrency(2) + "\r\n 1個#rV碎片#b=#r" + needNX + "#b楓點 #r", "1", 1, 99999);
            if (needNX * number > player.getCashShopCurrency(2)) {
                npc.say("楓點不足", 3003307);
            } else {
                player.updateQuestRecordEx(1477, "count", player.getIntQuestRecordEx(1477, "count") + number);
                player.modifyCashShopCurrency(2, -(needNX * number));
                npc.say("購買成功", 3003307);
            }
            break;
        case 1:
            if (player.getPQLog("FREE_VCORE") <= 0) {
                if (player.canGainItem(2435902, 5)) {
                    player.addPQLog("FREE_VCORE");
                    player.gainItem(2435902, 5);
                    npc.say("恭喜您成功領取#r5#k個核心寶石，我們明天見。", 3003307);
                } else {
                    npc.say("揹包消耗欄空間不足！請清理！", 3003307);
                }
            } else {
                npc.say("你今天已經領取過核心寶石了。", 3003307);
            }
            break;
        case 2:
            if (player.isQuestCompleted(34478)) {
                npc.say("你已經增加過#r神祕徽章#b裝備欄了！", 3003307);
            } else {
                let answer = npc.askYesNo("增加#r神祕徽章#b裝備欄需要以下材料\r\n#r\r\n1.#v4000295##z4000295# x1500\r\n2.#v4000236##z4000236# x1500\r\n3.5E楓幣\r\n#d確認是否要開啟？");
                if (answer == 1) {
                    if (player.hasItem(4000295, 1500) && player.hasItem(4000236, 1500) && player.hasMesos(500000000)) {
                        player.loseItem(4000295, 1500);
                        player.loseItem(4000236, 1500);
                        player.loseMesos(500000000);
                        player.completeQuest(34478, 0);
                        npc.say("#r神祕徽章#b裝備欄已增加！", 3003307);
                    } else {
                        npc.say("材料不足，請檢查以下材料是否足夠！#r\r\n1.#v4000295##z4000295# x1500#r\r\n2.#v4000236##z4000236# x1500\r\n3.冒險幣5E", 3003307);
                    }
                }
            }
            break;
        case 3:
            player.updateQuestRecordEx(1477, "count", "9999");
            npc.say("核心碎片領取成功，現在去強化技能吧！#r\r\n如果不想一個個點，[實用功能]有一鍵滿5轉技能哦");
            player.runScript("五轉");
            break;
        case 4:
            player.completeQuest(1465, 0);
            break;
    }
} else {
    if (player.getLevel() < 200) {
        npc.say("#d當你達到 #r200 #d級時再來找我吧！！", 3003307);
    } else {
        player.completeQuest(1465, 0);
        npc.say("#d恭喜你突破了自己的極限！\r\n現在#r我已讓你完成第五次轉職#,d加油吧，修煉者！", 3003307);
        player.runScript("五轉");
    }
}
