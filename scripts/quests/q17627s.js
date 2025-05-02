//
// Quest ID:17627
// [凱梅爾茲共和國] 擊退阿庫旁多（2）


npc.next().sayX("它們比我們想象中的弱多了。看來傳聞有點誇大了。");
if (npc.askAcceptX("雖然擊退了#o9390808#，但事情好像並沒有就此結束。剛剛我接到報告，好像又發現了其他群體。你這次也會幫我的吧？")) {
    // Response is Yes
    npc.next().sayX("#b#e#o9390811##k#n好像在#b#k#m865020100##k#n。我們這次也來打賭，看誰先擊退#b#e30只#k#n吧！我先到#m865020100#等著你！");
    npc.startQuest();
} else {
    // Response is No
    npc.ui(1).sayX("你還沒準備好?準備好之後,告訴我。");
}