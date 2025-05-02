/**
 *
 *
 */

let ret = npc.askYesNoNoEsc("很高興能在這裡見到你……幹嘛這麼吃驚？我看上去太年輕了嗎？其實我的年紀比看上去要大得多，你可別小看我。好了，我馬上讓你轉職成海盜。");
if (ret == 1) {
    npc.completeQuest();
    player.gainEquipInventorySlots(8);
    player.gainUseInventorySlots(8);
    player.gainSetupInventorySlots(8);
    player.gainEtcInventorySlots(8);
    player.gainCashInventorySlots(8);
    player.setJob(500);
    player.resetStats(4, 4, 4, 4);
    player.gainSp(3);
    if (player.canGainItem(1142107, 1)) {
        player.gainItem(1142107, 1);
    }
    if (player.canGainItem(1482065, 1)) {
        player.gainItem(1482065, 1);
    }
    if (player.canGainItem(1492065, 1)) {
        player.gainItem(1492065, 1);
    }
    if (player.canGainItem(2330000, 800)) {
        player.gainItem(2330000, 800);
    }
    if (player.canGainItem(2330000, 800)) {
        player.gainItem(2330000, 800);
    }
    npc.ui(1).uiMax().next().noEsc().sayX("好了，現在你已經是海盜的一員了。你已經有了很多海盜技能，你可以開啟技能窗看一看。我給了你一些SP，你可以用來提升技能。隨著等級的升高，你可以使用更多的技能。努力修煉吧。");
    npc.ui(1).uiMax().next().noEsc().sayX("光是技能還不行。請你開啟屬性窗，按照海盜的需要對自己的屬性進行分配。想成為拳手的話，就以力量為中心，想成為火槍手的話，就以敏捷為中心。不知道的話，使用#b自動分配#k會比較方便。");
    npc.ui(1).uiMax().next().noEsc().sayX("還有一個禮物就是, 我給你增加了裝備、消費、設定、其他道具保管箱的數量。若有價值的物品就放到揹包裡好了。");
    npc.ui(1).uiMax().next().noEsc().sayX("啊，還有一件事必須記住。你已經從新手成為了海盜，戰鬥時一定要注意體力。死了的話，之前積累的經驗值會受到損失。要是辛苦積累到的經驗值受到損失，豈不是很冤枉？");
    npc.ui(1).uiMax().next().noEsc().sayX("好了！我能教你的就只有這些。我給了你幾件適合你使用的武器，希望你一邊旅行，一邊鍛鍊自己。如果遇到了黑魔法師的部下，一定要消滅掉他們！明白了嗎？");
} else {
    npc.ui(1).uiMax().next().noEsc().sayX("你還沒做好心理準備嗎？");
}
