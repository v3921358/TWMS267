/**
 *
 *
 */
npc.next().sayX("我現在肚子餓了，有什麼吃的嗎？");
let ret = npc.askYesNoS("#b龍應該吃什麼呢？我也不知道啊！去問問見多識廣的爸爸吧！", false);
if (ret == 1) {
    npc.startQuest();
} else {
    npc.next().sayX("你不給我吃的？你這真讓我發狂！");
}