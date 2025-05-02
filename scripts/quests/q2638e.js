/**
 *
 *
 */
npc.next().sayX("不錯不錯，你帶回來了黑珠，我承認你的能力，我要幫助喚醒慧眼。");
let ret = npc.askYesNo("你做好準備好喚醒慧眼，獲得更強大的力量，蛻變成雙刀客嗎？");
if (ret == 1) {
    player.loseItem(4032616);
    player.setJob(431);
    player.gainSp(3);
    npc.completeQuest();
} else {
    npc.next().sayX("好的，那等你準備好了在來吧。");
}