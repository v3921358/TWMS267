//
// Quest ID:17662
// [凱梅爾茲共和國] 尋找克萊爾


if (npc.askYesNo("你做好去找克萊爾的準備了嗎? ")) {
    npc.next().sayX("估測了一下那些傢伙逃跑的方向,往#b#m865030200##k方向逃跑的可能性較大。我們往那邊去看看吧。");
    npc.startQuest();
} else {
    npc.ui(1).sayX("準備好了就告訴我。");
}