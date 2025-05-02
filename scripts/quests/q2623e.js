/**
 *
 *
 */
npc.next().sayX("不錯不錯，你確實有資格走雙刀之路。");
let ret = npc.askYesNo("你做好準備成為一名見習刀客了嗎？");
if (ret == 1) {
    player.loseItem(4032616);
    player.setJob(430);
    player.gainSp(3);
    npc.completeQuest();
} else {
    npc.next().sayX("那你好好準備下。");
}
