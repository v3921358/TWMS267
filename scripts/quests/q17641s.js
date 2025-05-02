// 
// Quest ID:17641
// [凱梅爾茲共和國] 遠征隊起航


npc.next().sayX("搞什麼。怎麼這麼遲才來。");
npc.ui(1).uiMax().next().sayX("啊,對不起,出了點小事。");
if (npc.askYesNo("小事兒?嗯,算了。趕緊上船吧。都準備好了吧?這次離開很長時間回不來的,需要的東西可都帶齊了。")) {
    // Response is Yes
    npc.ui(1).uiMax().next().sayX("好了,我作為船長有事要處理就先進去了。");
    npc.startQuest();
    npc.ui(1).uiMax().meFlip().next().sayX("沒問題。去吧。");
    npc.id(9390204).ui(1).uiMax().npcFlip().next().sayX("喔啦啦,這真巧啊。");
    npc.ui(1).uiMax().meFlip().next().sayX("............");
} else {
    npc.ui(1).sayX("還沒有準備好嗎?那麼再去準備準備再來吧。");
}