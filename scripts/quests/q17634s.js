//
// Quest ID:17634
// [凱梅爾茲共和國] 向吉爾伯特報告


npc.next().sayX("嗯。事情怎麼樣了。");
npc.ui(1).uiMax().next().sayX("嗯,順利地趕走了阿庫旁多。我想一段時間內,它們應該不會來襲擊了。");
if (npc.askYesNo("那麼簡單就處理好了?嗯,中間沒有別的問題了?")) {
    // Response is Yes
    npc.startQuest();
    npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("是的,統帥.. 實際上...");
    npc.ui(1).uiMax().meFlip().next().sayX("嗯,多虧了萊文出眾的領導力,沒有遇到什麼大問題。");
    npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("(喂,你幹嘛說謊啊?)");
    npc.ui(1).uiMax().next().sayX("那真是個好訊息。沒有遇到什麼困難?");
    npc.ui(1).uiMax().meFlip().next().sayX("(那些不愉快的事情,沒有必要說出來。)");
    npc.ui(1).uiMax().next().sayX("嗯.. 也許是我低估了萊文。");
    npc.ui(1).uiMax().meFlip().next().sayX("啊,還有,我們在回來的路上發現了奇怪的結界。");
    npc.ui(1).uiMax().next().sayX("結界?是什麼結界?");
    npc.ui(1).uiMax().meFlip().next().sayX("我們那時太疲憊,而且不知道會不會遇到什麼危險,我們就沒進入結界內部。");
    npc.ui(1).uiMax().next().sayX("嗯.. 我知道了。我會另外派人去調查結界的。辛苦你了。你和萊文一起去休息吧。");
} else {
    npc.ui(1).sayX("你還沒做好報告的準備啊?");
}