//
// Quest ID:17657
// [凱梅爾茲共和國] 她的身份


npc.next().sayX("我是誰真的有那麼重要嗎?");
npc.id(9390202).ui(1).npcFlip().next().sayX("我們可是互相救過命又互相幫助的關係嘛。以後也那樣就好了。所以我希望我們能明確一下關係。");
if (npc.askYesNo("好吧。既然你都這麼說了,我就告訴你吧。你做好吃驚的準備了嗎?")) {
    npc.next().sayX("好吧,哎咦。");
    npc.startQuest();
} else {
    npc.ui(1).sayX("怎麼了?真的要揭曉了又害怕了嗎?");
}