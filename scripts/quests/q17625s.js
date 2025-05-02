// 
// Quest ID:17625
// [凱梅爾茲共和國] 我來抵抗阿庫旁多


if (npc.askYesNo("你想瞭解更多關於阿庫旁多的事情？")) {
    // Response is Yes
    npc.ui(1).uiMax().next().sayX("阿庫旁多在很久之前就和我們共同存在了。實際上，雖然以前我們說不上是往來關係，但至少是中立關係。");
    npc.ui(1).uiMax().next().sayX("雖然現在凱梅爾茲成為貿易發達的國家，但以前這裡主要以漁業為主。不知從何時開始，這裡透過貿易變得富有，開發了首都桑凱梅爾茲，還建了運河。");
    npc.ui(1).uiMax().next().sayX("從那時開始，阿庫旁多就開始敵對我們。大概他們認為是我們破壞了他們生活的地方。總之，之後他們就少數人聚集在一起開始襲擊旅行者和商人。");
    npc.ui(1).uiMax().next().sayX("所以，我們這次想要教訓下阿庫旁多那些傢伙。");
    npc.ui(1).uiMax().meFlip().next().sayX("#b(就是這個!!)#k\r\n統帥,請把我也編入阿庫旁多討伐團吧。");
    npc.ui(1).uiMax().next().sayX("嗯?這不是你國家的事情,你為什麼打算參與呢?");
    npc.ui(1).uiMax().meFlip().next().sayX("#b友.邦.國.家#k.的事情就是我的事情,這是冒險島世界的傳統。");
    npc.ui(1).uiMax().next().sayX("嗯.... 感謝你的好意,但...");
    npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("爸爸，不...統帥！#h0#...擁有出眾的實力。我能保證。他肯定會給我們帶來幫助的。讓他和我們在一起怎麼樣？");
    npc.ui(1).uiMax().next().sayX("你認為我們弱到需要藉助別人的力量了嗎?");
    npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("我不是那個意思...");
    npc.ui(1).uiMax().next().sayX("你快去討伐阿庫旁多那些傢伙吧。");
    npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("是…我知道了。");
    npc.startQuest();
    npc.ui(1).sayX("你是叫#h0#吧…很感謝你的好意，但我不想讓外人插手我們共和國的事情。希望你能諒解。");
} else {
    // Response is No
    npc.ui(1).sayX("哼…如果你不願意也沒辦法。我看你眼睛發光，以為你有興趣呢。");
}