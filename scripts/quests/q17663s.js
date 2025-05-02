// 
// Quest ID:17663
// [凱梅爾茲共和國] 暗殺團據點


npc.next().sayX("那些傢伙們看上去是這地方的守衛。");
npc.ui(1).uiMax().next().sayX(" ...如果說是守衛的話這人數也太多了吧?");
if (npc.askYesNo("...不管怎樣不幹掉他們的話就無法透過這裡了。先把能看到的都幹掉。")) {
    npc.startQuest();
    npc.next().sayX("我們比比看誰幹掉的更多吧?走吧!");
} else {
    npc.ui(1).sayX("你不會到了這裡害怕了吧?");
}
