//
// Quest ID:17629
// [凱梅爾茲共和國] 擊退阿庫旁多（4）


npc.next().sayX("咳咳.. 哎呀.. 這下要完了。它們的數量不斷變多。喂,#h0#,你還好嗎?");
npc.ui(1).uiMax().next().sayX("嗯。唉,還可以。這果然是陷阱。");
npc.next().sayX("雖然是陷阱.. 但我們還是戰勝了啊。呼..");
npc.ui(1).uiMax().next().sayX("沒錯。但是剛剛太輕率了。");
npc.next().sayX("呼~ 沒關係。好像都結束了。它們應該不會再出現了。如果在這裡再碰到它們的話... 我們就難辦了。");
npc.completeQuest();
player.gainExp(793937);