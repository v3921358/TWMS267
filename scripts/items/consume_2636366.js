import { pic } from "scripts/expands/pic.js";

var itemListA = [1005980, 1005981, 1005982, 1005983, 1005984];
var itemListB = [1042433, 1042434, 1042435, 1042436, 1042437];
var itemListC = [1062285, 1062286, 1062287, 1062288, 1062289];

function handleItemSelection(itemList, boxId) {
    var text = "";
    for (var i = 0; i < itemList.length; i++) {
        text += "#L" + i + "##v" + itemList[i] + "##z" + itemList[i] + "##l\r\n";
    }
    var selection = npc.askMenu(pic.QA圖 + "\r\n#b尊貴玩家你好,請選擇換取的防具：\r\n#r" + text);
    var itemid = itemList[selection];
    var toDrop = sh.itemEquip(itemid).copy(); // 生成一個Eq
    toDrop.setHp(100); // 素質["血"]
    toDrop.setMp(100); // 素質["魔"]
    toDrop.setPad(100); // 素質["物攻"]
    toDrop.setMad(100); // 素質["魔攻"]
    toDrop.setPdd(100); // 素質["防"]
    toDrop.setAcc(100); // 素質["命中"]
    toDrop.setARC(100); // 素質["ARC等級"]
    toDrop.setBossDamage(100); // 素質["王傷"]
    toDrop.setIgnorePDR(100); // 素質["無視"]
    toDrop.setJump(100); // 素質["跳"]
    toDrop.setSpeed(100); // 素質["移速"]
    toDrop.setAvoid(100); // 素質["閃避"]
    toDrop.setTotalDamage(100); // 素質["總傷"]
    toDrop.setEnhance(100); // 素質["星力"]
    toDrop.setOwner(player.getName()); // 素質["鯨魚親手簽名"]
    //toDrop.setPotential4(100); // 素質["潛能第四條"]
    //toDrop.setPotential5(100); // 素質["潛能第五條"]
    //toDrop.setPotential6(100); // 素質["潛能第六條"]
    //toDrop.setCurrentUpgradeCount(100); // 素質["已充次數"]
    //toDrop.setRestUpgradeCount(100); // 素質["卷軸"]
    //toDrop.setViciousHammer(100); // 素質["黃槌"]
    //toDrop.setPlatinumHammer(100); // 素質["白槌"]
    //toDrop.setItemEXP(100); // 素質["經驗"]
    //toDrop.setFinalStrike(100); // 素質["最終一擊卷"]
    //toDrop.setYggdrasilWisdom(100); // 素質["樹的祝福"]
    //toDrop.setKarmaCount(100); // 素質["KarmaCount"]
    //toDrop.setFailCount(100); // 素質["FailCount"]
    //toDrop.setMvpEquip(100); // 素質["MVP"]
    player.gainItem(toDrop);
    player.loseItem(boxId, 1);

    npc.say("#fs15##b您已經成功兌換裝備：\r\n#r道具：#i" + itemid + "#");
}

if (npc.getItemId() == 2637923) {
    handleItemSelection(itemListA, 2637923);
}

if (npc.getItemId() == 2636365) {
    handleItemSelection(itemListB, 2636365);
}

if (npc.getItemId() == 2636366) {
    handleItemSelection(itemListC, 2636366);
}
