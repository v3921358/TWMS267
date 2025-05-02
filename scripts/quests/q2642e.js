/**
 *
 *
 */
npc.next().sayX("不錯不錯，現在可以走雙刀俠之路了。");
let ret = npc.askYesNo("你做好準備成為一名雙刀俠了嗎？");
if (ret == 1) {
    player.setJob(432);
    player.gainSp(3);
    npc.completeQuest();
} else {
    npc.next().sayX("好的，那等你準備好了在來吧。");
}