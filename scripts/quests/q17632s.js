// 
// Quest ID:17632
// [凱梅爾茲共和國] 運河的結界


npc.ui(1).uiMax().next().sayX("萊文,我們回去吧。");
npc.next().sayX("等下。 ");
npc.ui(1).uiMax().next().sayX("嗯?為什麼?什麼事?");
npc.next().sayX("不是什麼事啦.. 我想再四處轉轉。");
npc.ui(1).uiMax().next().sayX("四處轉轉。剛剛的事情還沒讓你清醒嗎?");
if (npc.askYesNo("不是,不是追阿庫旁多。我只是.. 想再四處轉轉。如果你累的話,你就先回去吧。")) {
    // Response is Yes
    npc.startQuest();
    npc.ui(1).uiMax().next().sayX("哎呀。我知道了。現在還很危險,我陪你一起吧。");
    player.changeMap(865020300, 3);
} else {
    // Response is No
    npc.ui(1).sayX("好的,我回去看看,你先走。");
}
