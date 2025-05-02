/**
 *
 *
 */
npc.next().sayX("最近成長了不少，但是要我承認你的能力，你就必須從影子雙刀那邊帶回#r30個#k黑珠，並且我就幫助你喚醒慧眼，使你獲得更強大的力量，蛻變成雙刀客！");
let ret = npc.askYesNo("是否現在就開始進行試煉？");
if (ret == 1) {
    npc.startQuest();
    player.changeMap(103050370, 0);
} else {
    npc.next().sayX("那你好好準備下。");
}
