// 
// Quest ID:17667
// [凱梅爾茲共和國] 什麼關係


npc.ui(1).uiMax().next().sayX("該死……！");
npc.ui(1).uiMax().meFlip().next().sayX("你為什麼要裝死？你的目的是什麼！");
npc.ui(1).uiMax().next().sayX("我應該沒有義務回答你的問題吧？而且，你們應該沒有權利抓住我不放吧？");
if (npc.askYesNoE("(這傢伙，嘴巴還挺緊！#h0#，你能讓這傢伙開口嗎？)", 9390202)) {
    npc.startQuest();
    npc.ui(1).uiMax().meFlip().next().sayX("喂，萊文，人質沒必要有頭髮吧？還有這個難看的鬍子。");
    npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("是啊，完全沒必要啊。其實衣服也沒必要吧？");
    npc.ui(1).uiMax().meFlip().next().sayX("是啊，把頭髮和鬍子全部剃掉，衣服也脫下來，那他估計就不能逃跑了，這個主意不錯啊。讓我看看，拔鬍子應該用鑷子吧……");
    npc.id(9390207).ui(1).uiMax().npcFlip().next().sayX("其實那些像要暗殺我的人是我的手下。");
    npc.ui(1).uiMax().meFlip().next().sayX("看來你現在才想說啊。");
} else {
    npc.id(9390202).ui(1).npcFlip().sayX("再想想。只有讓那傢伙開口，才能證明我們的清白。");
}