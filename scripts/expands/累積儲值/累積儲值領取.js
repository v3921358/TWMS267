/* 52hz */
import { pic } from "scripts/expands/pic.js";
import { color } from "scripts/expands/color.js";
import func from 'scripts/expands/func.js';

var 暗夜 = "#fEffect/ItemEff/1004718/effect/default/3#"; // 暗夜
var 星閃 = "#fEffect/ItemEff/1005393/effect/backDefault/3#"; // 星閃
var 小臉 = "#fEffect/ItemEff/1012634/effect/default/1#"; // 小臉
var EventLog = "累積儲值";
var totalDonate = [1000, 3000, 6000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000, 55000, 60000, 65000, 70000];

// Array(itemId, quantity, level, attributes)
var giftContent = [
    /*1000*/
    Array(1012438, 1, 0, {力量: 50, 智力: 50, 敏捷: 50, 幸運: 50, 物攻: 50, 魔攻: 50, 王傷: 5, 無視: 5, 總傷: 5, 星力: 15}), // 波賽頓紋身(臉飾)
    Array(1022211, 1, 0, {力量: 50, 智力: 50, 敏捷: 50, 幸運: 50, 物攻: 50, 魔攻: 50, 王傷: 5, 無視: 5, 總傷: 5, 星力: 15}), // 波賽頓眼鏡(眼飾)
    Array(2436382, 20, 0), // V卷箱 x20
    Array(2450163, 10, 0), // 三倍經驗券 x10
    Array(5062020, 30, 0), // 閃炫方塊
    Array(5062026, 30, 0), // 結合方塊
    Array(5062500, 30, 0), // 附加方塊
    Array(5743003, 30, 0), // 萌獸方塊
    Array(5060048, 30, 0), // 黃金蘋果
    Array(5060029, 30, 0), // 艾比箱
    Array(5060025, 5, 0), // 魔法豎琴

    /*3000*/
    Array(1662072, 1, 1, {力量: 50, 智力: 50, 敏捷: 50, 幸運: 50, 物攻: 50, 魔攻: 50, 王傷: 5, 無視: 5, 總傷: 5}), // 戰鬥機器人(女)
    Array(1672069, 1, 1, {力量: 50, 智力: 50, 敏捷: 50, 幸運: 50, 物攻: 50, 魔攻: 50, 王傷: 5, 無視: 5, 總傷: 5, 卷軸: 15, 星力: 15}), // 女武神之心
    Array(2436382, 20, 1), // V卷箱 x20
    Array(2450163, 10, 1), // 三倍經驗券 x10
    Array(5062020, 30, 1), // 閃炫方塊
    Array(5062026, 30, 1), // 結合方塊
    Array(5062500, 30, 1), // 附加方塊
    Array(5743003, 30, 1), // 萌獸方塊
    Array(5060048, 30, 1), // 黃金蘋果
    Array(5060029, 30, 1), // 艾比箱
    Array(5060025, 15, 1), // 魔法豎琴

    /*6000*/
    Array(1113195, 1, 2, {力量: 80, 智力: 80, 敏捷: 80, 幸運: 80, 物攻: 80, 魔攻: 80, 王傷: 5, 無視: 5, 總傷: 5, 星力: 15, 卷軸: 5}), // 魔性的戒指
    Array(1122334, 1, 2, {力量: 80, 智力: 80, 敏捷: 80, 幸運: 80, 物攻: 80, 魔攻: 80, 王傷: 5, 無視: 5, 總傷: 5, 星力: 15}), // 準備好的精靈墜飾
    Array(2633915, 3, 2), // 神秘防具箱*3
    Array(2436380, 20, 2), // B卷箱 x20
    Array(2450163, 10, 2), // 三倍經驗券 x10
    Array(5062020, 50, 2), // 閃炫方塊
    Array(5062026, 50, 2), // 結合方塊
    Array(5062500, 50, 2), // 附加方塊
    Array(5743003, 50, 2), // 萌獸方塊
    Array(5060048, 50, 2), // 黃金蘋果
    Array(5060029, 40, 2), // 艾比箱
    Array(5060025, 30, 2), // 魔法豎琴

    /*10000*/
    Array(1143334, 1, 3, {力量: 150, 智力: 150, 敏捷: 150, 幸運: 150, 物攻: 150, 魔攻: 150, 王傷: 5, 無視: 5, 總傷: 5}), // 天選之人勳章
    Array(1114400, 1, 3, {力量: 150, 智力: 150, 敏捷: 150, 幸運: 150, 物攻: 150, 魔攻: 150, 王傷: 5, 無視: 5, 總傷: 5}), // 燃燒戒指
    Array(2633914, 1, 3), // 神秘武器自選箱 *1
    Array(2436380, 20, 3), // B卷箱 x20
    Array(2450163, 10, 3), // 三倍經驗券 x10
    Array(5062020, 50, 3), // 閃炫方塊
    Array(5062026, 50, 3), // 結合方塊
    Array(5062500, 50, 3), // 附加方塊
    Array(5743003, 50, 3), // 萌獸方塊
    Array(5060048, 50, 3), // 黃金蘋果
    Array(5060029, 40, 3), // 艾比箱
    Array(5060025, 30, 3), // 魔法豎琴

    /*15000*/
    Array(1182136, 1, 4, {力量: 120, 智力: 120, 敏捷: 120, 幸運: 120, 物攻: 120, 魔攻: 120, 王傷: 8, 無視: 8, 總傷: 8}), // MX-131
    Array(2633915, 3, 4), // 神秘防具箱*3
    Array(2436380, 30, 4), // B卷箱 x30
    Array(2450163, 10, 4), // 三倍經驗券 x10
    Array(5062020, 50, 4), // 閃炫方塊
    Array(5062026, 50, 4), // 結合方塊
    Array(5062500, 50, 4), // 附加方塊
    Array(5743003, 50, 4), // 萌獸方塊
    Array(5060048, 50, 4), // 黃金蘋果
    Array(5060029, 40, 4), // 艾比箱
    Array(5060025, 30, 4), // 魔法豎琴

    /*20000*/
    Array(1122267, 1, 5, {力量: 150, 智力: 150, 敏捷: 150, 幸運: 150, 物攻: 150, 魔攻: 150, 王傷: 10, 無視: 10, 總傷: 10, 卷軸: 10}), // 頂級培羅德項鍊
    Array(1132315, 1, 5, {力量: 150, 智力: 150, 敏捷: 150, 幸運: 150, 物攻: 150, 魔攻: 150, 王傷: 10, 無視: 10, 總傷: 10, 卷軸: 10}), // 頂級培羅德腰帶
    Array(2436380, 30, 5), // B卷箱 x30
    Array(2450163, 20, 5), // 三倍經驗券 x20
    Array(5062020, 80, 5), // 閃炫方塊
    Array(5062026, 80, 5), // 結合方塊
    Array(5062500, 80, 5), // 附加方塊
    Array(5743003, 80, 5), // 萌獸方塊
    Array(5060048, 80, 5), // 黃金蘋果
    Array(5060029, 50, 5), // 艾比箱
    Array(5060025, 40, 5), // 魔法豎琴

    /*25000*/
    Array(1113075, 1, 6, {力量: 150, 智力: 150, 敏捷: 150, 幸運: 150, 物攻: 150, 魔攻: 150, 王傷: 10, 無視: 10, 總傷: 10, 卷軸: 10}), // 頂級培羅德戒指
    Array(1032223, 1, 6, {力量: 150, 智力: 150, 敏捷: 150, 幸運: 150, 物攻: 150, 魔攻: 150, 王傷: 10, 無視: 10, 總傷: 10, 卷軸: 10}), // 頂級培羅德耳環
    Array(2436380, 30, 6), // B卷箱 x30
    Array(2450163, 20, 6), // 三倍經驗券 x20
    Array(5062020, 80, 6), // 閃炫方塊
    Array(5062026, 80, 6), // 結合方塊
    Array(5062500, 80, 6), // 附加方塊
    Array(5743003, 80, 6), // 萌獸方塊
    Array(5060048, 80, 6), // 黃金蘋果
    Array(5060029, 50, 6), // 艾比箱
    Array(5060025, 40, 6), // 魔法豎琴

    /*30000*/
    Array(1113211, 1, 7, {力量: 180, 智力: 180, 敏捷: 180, 幸運: 180, 物攻: 180, 魔攻: 180, 王傷: 15, 無視: 15, 總傷: 15, 卷軸: 8}), // 天上的氣息
    Array(1113220, 1, 7, {力量: 180, 智力: 180, 敏捷: 180, 幸運: 180, 物攻: 180, 魔攻: 180, 王傷: 15, 無視: 15, 總傷: 15}), // 幽暗戒指
    Array(5064400, 30, 7), // 恢復卡 x30
    Array(2436384, 30, 7), // 榮耀卷箱 x30
    Array(4310307, 1, 7), // 勳章升級石 X1
    Array(2450163, 20, 7), // 三倍經驗券 x20
    Array(5062020, 100, 7), // 閃炫方塊
    Array(5062026, 100, 7), // 結合方塊
    Array(5062500, 100, 7), // 附加方塊
    Array(5743003, 100, 7), // 萌獸方塊
    Array(5060048, 100, 7), // 黃金蘋果
    Array(5060029, 50, 7), // 艾比箱
    Array(5060025, 50, 7), // 魔法豎琴

    /*35000*/
    Array(1022278, 1, 8, {力量: 220, 智力: 220, 敏捷: 220, 幸運: 220, 物攻: 220, 魔攻: 220, 王傷: 20, 無視: 20, 星力: 15, 總傷: 10, 卷軸: 10}), // 附有魔力的眼罩
    Array(1012632, 1, 8, {力量: 220, 智力: 220, 敏捷: 220, 幸運: 220, 物攻: 220, 魔攻: 220, 王傷: 20, 無視: 20, 星力: 15, 總傷: 10, 卷軸: 10}), // 口紅控制器
    Array(5064400, 30, 8), // 恢復卡 x30
    Array(2436384, 30, 8), // 榮耀卷箱 x30
    Array(2450163, 20, 8), // 三倍經驗券 x20
    Array(5062020, 100, 8), // 閃炫方塊
    Array(5062026, 100, 8), // 結合方塊
    Array(5062500, 100, 8), // 附加方塊
    Array(5743003, 100, 8), // 萌獸方塊
    Array(5060048, 100, 8), // 黃金蘋果
    Array(5060029, 50, 8), // 艾比箱
    Array(5060025, 50, 8), // 魔法豎琴

    /*40000*/
    Array(1122430, 1, 9, {力量: 220, 智力: 220, 敏捷: 220, 幸運: 220, 物攻: 220, 魔攻: 220, 王傷: 20, 無視: 20, 星力: 15, 總傷: 10, 卷軸: 10}), // 苦痛的根源
    Array(1132308, 1, 9, {力量: 220, 智力: 220, 敏捷: 220, 幸運: 220, 物攻: 220, 魔攻: 220, 王傷: 20, 無視: 20, 星力: 15, 總傷: 10, 卷軸: 10}), // 夢幻腰帶
    Array(5064400, 50, 9), // 恢復卡 x50
    Array(2436384, 50, 9), // 榮耀卷箱 x50
    Array(4310307, 1, 9), // 勳章升級石 X1
    Array(2450163, 20, 9), // 三倍經驗券 x20
    Array(5062020, 100, 9), // 閃炫方塊
    Array(5062026, 100, 9), // 結合方塊
    Array(5062500, 100, 9), // 附加方塊
    Array(5743003, 100, 9), // 萌獸方塊
    Array(5060048, 100, 9), // 黃金蘋果
    Array(5060029, 50, 9), // 艾比箱
    Array(5060025, 50, 9), // 魔法豎琴

    /*45000*/
    Array(1113306, 1, 10, {力量: 220, 智力: 220, 敏捷: 220, 幸運: 220, 物攻: 220, 魔攻: 220, 王傷: 20, 無視: 20, 星力: 15, 總傷: 10, 卷軸: 10}), // 巨大的恐怖
    Array(2633926, 1, 10), // 改屬魔導書
    Array(5064400, 50, 10), // 恢復卡 x50
    Array(2436384, 50, 10), // 榮耀卷箱 x50
    Array(2450163, 20, 10), // 三倍經驗券 x20
    Array(5062020, 100, 10), // 閃炫方塊
    Array(5062026, 100, 10), // 結合方塊
    Array(5062500, 100, 10), // 附加方塊
    Array(5743003, 100, 10), // 萌獸方塊
    Array(5060048, 100, 10), // 黃金蘋果
    Array(5060029, 50, 10), // 艾比箱
    Array(5060025, 50, 10), // 魔法豎琴

    /*50000*/
    Array(1182158, 1, 11, {力量: 220, 智力: 220, 敏捷: 220, 幸運: 220, 物攻: 220, 魔攻: 220, 王傷: 20, 無視: 20, 星力: 15, 總傷: 10}), // 黑翼胸章
    Array(2633547, 3, 11), // 改屬神秘裝自選 *3
    Array(5064400, 50, 11), // 恢復卡 x50
    Array(2436384, 50, 11), // 榮耀卷箱 x50
    Array(4310307, 1, 11), // 勳章升級石 X1
    Array(2450163, 20, 11), // 三倍經驗券 x20
    Array(5062020, 150, 11), // 閃炫方塊
    Array(5062026, 150, 11), // 結合方塊
    Array(5062500, 150, 11), // 附加方塊
    Array(5743003, 150, 11), // 萌獸方塊
    Array(5060048, 150, 11), // 黃金蘋果
    Array(5060029, 60, 11), // 艾比箱
    Array(5060025, 60, 11), // 魔法豎琴

    /*55000*/
    Array(1672075, 1, 12, {力量: 220, 智力: 220, 敏捷: 220, 幸運: 220, 物攻: 220, 魔攻: 220, 王傷: 20, 無視: 20, 總傷: 15, 卷軸: 20}), // m-d心臟
    Array(2633547, 3, 12), // 改屬神秘裝自選 *3
    Array(5064400, 50, 12), // 恢復卡 x50
    Array(2436384, 50, 12), // 榮耀卷箱 x50
    Array(2450163, 20, 12), // 三倍經驗券 x20
    Array(5062020, 150, 12), // 閃炫方塊
    Array(5062026, 150, 12), // 結合方塊
    Array(5062500, 150, 12), // 附加方塊
    Array(5743003, 150, 12), // 萌獸方塊
    Array(5060048, 150, 12), // 黃金蘋果
    Array(5060029, 60, 12), // 艾比箱
    Array(5060025, 60, 12), // 魔法豎琴

    /*60000*/
    Array(2636098, 2, 13), // 改屬點裝自選 *2
    Array(2633549, 2, 13), // 改屬永恆裝自選 *2 
    Array(5064400, 50, 13), // 恢復卡 x50
    Array(2436384, 50, 13), // 榮耀卷箱 x50
    Array(4310307, 1, 13), // 勳章升級石 X1
    Array(2450163, 20, 13), // 三倍經驗券 x20
    Array(5062020, 150, 13), // 閃炫方塊
    Array(5062026, 150, 13), // 結合方塊
    Array(5062500, 150, 13), // 附加方塊
    Array(5743003, 150, 13), // 萌獸方塊
    Array(5060048, 150, 13), // 黃金蘋果
    Array(5060029, 60, 13), // 艾比箱
    Array(5060025, 60, 13), // 魔法豎琴

    /*65000*/
    Array(2636098, 2, 14), // 改屬點裝自選 *2
    Array(2633549, 2, 14), // 改屬永恆裝自選 *2 
    Array(5064400, 50, 14), // 恢復卡 x50
    Array(2436384, 50, 14), // 榮耀卷箱 x50
    Array(2450163, 20, 14), // 三倍經驗券 x20
    Array(5062020, 200, 14), // 閃炫方塊
    Array(5062026, 200, 14), // 結合方塊
    Array(5062500, 200, 14), // 附加方塊
    Array(5743003, 200, 14), // 萌獸方塊
    Array(5060048, 200, 14), // 黃金蘋果
    Array(5060029, 70, 14), // 艾比箱
    Array(5060025, 70, 14), // 魔法豎琴

    /*70000*/
    Array(1662072, 1, 15, {力量: 400, 智力: 400, 敏捷: 400, 幸運: 400, 物攻: 400, 魔攻: 400, 王傷: 20, 無視: 20, 總傷: 20}), // 戰鬥機器人(女)
    Array(2633550, 1, 15), // 改屬創世自選
    Array(5064400, 50, 15), // 恢復卡 x50
    Array(2436384, 50, 15), // 榮耀卷箱 x50
    Array(4310307, 1, 15), // 勳章升級石 X1
    Array(2450163, 20, 15), // 三倍經驗券 x20
    Array(5062020, 200, 15), // 閃炫方塊
    Array(5062026, 200, 15), // 結合方塊
    Array(5062500, 200, 15), // 附加方塊
    Array(5743003, 200, 15), // 萌獸方塊
    Array(5060048, 200, 15), // 黃金蘋果
    Array(5060029, 70, 15), // 艾比箱
    Array(5060025, 70, 15) // 魔法豎琴
];

var giftId = -1;
var giftToken = [];
var gifts = null;

let sel = showLevelMenu();

let isAccept = showGiftAcceptView(sel);

if(isAccept){
    completeView();
}else{
    npc.say(pic.楷體+"#r下次等您有空再來領取。");
}

function showLevelMenu(){
    var text = "\r\n";
    for (var key in totalDonate) {
        var tips = "";
        giftToken[key] = false;
        if (player.getHyPay(2) >= totalDonate[key]) {
            if (player.getEventCount(EventLog + key, 1) === 0) {
                tips = "#d【可領取】";
                giftToken[key] = true;
            } else {
                tips = "#g【已領取】#b";
            }
        } else {
            tips = "#r【未領取】#b";
        }
        text += "#k#L" + key + "#" + pic.寶箱 + " 滿額 #r#e" + totalDonate[key] + "#n#k 累積贊助 " + tips + "#l#k\r\n";
    }

    return npc.askMenu(text);
}

function showGiftAcceptView(selectionLevel){
    giftId = parseInt(selectionLevel);
    var text = "#r#e" + totalDonate[giftId] + "#n#b累積儲值獎勵內容：\r\n";
    gifts = getGift(giftId);
    for (var key in gifts) {
        var itemId = gifts[key][0];
        var itemQuantity = gifts[key][1];
        text += "#v" + itemId + "##b#t" + itemId + "##k #rx " + itemQuantity + "#k\r\n";
    }
    text += "\r\n#d是否領取？#k";

    return npc.askAccept(text);
}

function completeView(){
    if (giftId !== -1 && gifts !== null) {
        let mP = player;
        // 檢查背包空間
        let requiredSlots = 0;
        for (var key in gifts) {
            requiredSlots += 1; // 每個物品至少需要一格
        }
        let hasEnoughSpace = true;
        for (let i = 1; i <= 6; i++) {
            if (mP.getFreeSlots(i) < requiredSlots) {
                hasEnoughSpace = false;
                break;
            }
        }
        if (!hasEnoughSpace) {
            npc.say(pic.楷體+"#r您的背包空間不足。");
            return;
        }

        // 判斷是否已領取過
        if (giftToken[giftId] && mP.getEventCount(EventLog + giftId, 1) === 0) {
            for (var key in gifts) {
                var itemId = gifts[key][0];
                var itemQuantity = gifts[key][1];
                var itemAttr = gifts[key][3]; // 可能為 undefined

                if (itemAttr) {
                    buyCustom(itemId, itemQuantity, itemAttr);
                } else {
                    mP.gainItem(itemId, itemQuantity);
                }
            }
            mP.setEventCount(EventLog + giftId, 1, -1); // 標記為已領取
            mP.dropMessage(6,"領取了累積儲值獎勵:"+totalDonate[giftId]);
        } else {
            npc.say(pic.楷體+"#r已領取過。");
        }
    } else {
        npc.say(pic.楷體+"#r領取錯誤！請聯繫管理員！。");
    }
}

function getGift(id) {
    var lastGiftContent = [];
    for (var key in giftContent) {
        if (giftContent[key][2] === id)
            lastGiftContent.push(giftContent[key]);
    }
    return lastGiftContent;
}

function buyCustom(itemId, quantity, attributes) {
    if (itemId != 0 && quantity != 0) {
        var toDrop = sh.itemEquip(itemId).copy(); // 生成一個Eq
        // 設置裝備屬性
        if (attributes != null) {
            for (var prop in attributes) {
                if (attributes.hasOwnProperty(prop)) {
                    switch (prop) {
                        case "全屬":
                            toDrop.setStr(attributes[prop]);
                            toDrop.setInt(attributes[prop]);
                            toDrop.setDex(attributes[prop]);
                            toDrop.setLuk(attributes[prop]);
                            break;
                        case "力量":
                            toDrop.setStr(attributes[prop]);
                            break;
                        case "智力":
                            toDrop.setInt(attributes[prop]);
                            break;
                        case "敏捷":
                            toDrop.setDex(attributes[prop]);
                            break;
                        case "幸運":
                            toDrop.setLuk(attributes[prop]);
                            break;
                        case "血量":
                            toDrop.setHp(attributes[prop]);
                            break;
                        case "魔量":
                            toDrop.setMp(attributes[prop]);
                            break;
                        case "物攻":
                            toDrop.setPad(attributes[prop]);
                            break;
                        case "魔攻":
                            toDrop.setMad(attributes[prop]);
                            break;
                        case "防禦":
                            toDrop.setPdd(attributes[prop]);
                            break;
                        case "王傷":
                            toDrop.setBossDamage(attributes[prop]);
                            break;
                        case "無視":
                            toDrop.setIgnorePDR(attributes[prop]);
                            break;
                        case "星力":
                            toDrop.setEnhance(attributes[prop]);
                            break;
                        case "卷軸":
                            toDrop.setRestUpgradeCount(attributes[prop]);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        player.gainItem(toDrop);
    }
}

// Number.prototype.formatMoney 函式
Number.prototype.formatMoney = function (places, symbol, thousand, decimal) {
    places = !isNaN(places = Math.abs(places)) ? places : 2;
    symbol = symbol !== undefined ? symbol : " ";
    thousand = thousand || ",";
    decimal = decimal || ".";
    var number = this,
        negative = number < 0 ? "-" : "",
        i = parseInt(number = Math.abs(+number || 0).toFixed(places), 10) + "",
        j = (j = i.length) > 3 ? j % 3 : 0;
    return symbol + negative + (j ? i.substr(0, j) + thousand : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + thousand) + (places ? decimal + Math.abs(number - i).toFixed(places).slice(2) : "");
};
