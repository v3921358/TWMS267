//
// Quest ID:17618
// [凱梅爾茲共和國] 初次見面


if (npc.askYesNoS("#b(交易品的量很多,自己一個人運的話,應該還沒走遠。現在追上去,應該還能抓住他。)#k", true)) {
    // Response is Yes
    npc.ui(1).uiMax().meFlip().next().noEsc().sayX("他說是貝里村的反方向。那我只要往我來時的相反方向走就行了。");
    npc.startQuest();
} else {
    // Response is No
    npc.ui(1).meFlip().noEsc().sayX("還需要準備下。");
}