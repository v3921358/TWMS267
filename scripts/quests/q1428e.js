/**
 *
 *
 */

if (player.hasItem(4031013, 30) && player.canGainItem(1142108, 1)) {
    player.loseItem(4031013);
    player.gainItem(1142108, 1);
    player.setJob(530);
    player.gainSp(3);
    npc.completeQuest();
    npc.next().sayX("恭喜!你現在是一名火炮手了!");
} else {
    npc.next().sayX("請確認你裝備欄有一格空間。");
}