//
// Quest ID:17628
// [凱梅爾茲共和國] 擊退阿庫旁多（3）


if (npc.askAcceptX("這裡到處都是#e#b#o9390809##k#n！好，我們這次也努力各自擊退#b#e30只#k#n吧！")) {
    npc.startQuest();
    npc.ui(1).sayX("#b(不知為何，我有種不祥的預感...)");
} else {
    // Response is No
    npc.ui(1).sayX("你還沒準備好?準備好之後,告訴我。");
}