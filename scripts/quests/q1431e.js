/**
 *
 *
 */

if (player.hasItem(4031059, 1) && player.canGainItem(1142109, 1)) {
    player.loseItem(4031059);
    player.gainItem(1142109, 1);
    player.setJob(111);
    player.gainSp(3);
    npc.completeQuest();
    npc.next().sayX("恭喜!你現在是一名勇士了!");
} else {
    npc.next().sayX("請確認你裝備欄有一格空間。");
}