//
// Quest ID:17622
// [凱梅爾茲共和國] 擔心的眼神


npc.ui(1).uiMax().next().sayX("你看起來心情好像不太好,是我做錯什麼了嗎?");
npc.next().sayX("嗯?怎麼可能~ 很高興能重新見到你。你打算在桑凱梅爾茲待多久?");
npc.ui(1).uiMax().next().sayX("這個麼…一時半會好像走不了。我感覺事情不會那麼順利地解決。\r\n#b(拜你所賜....)#k");
npc.next().sayX("那你在桑凱梅爾茲期間,就住在我們商團吧!明天我帶你參觀下商團!那你先休息吧~!");
npc.completeQuest();
player.gainExp(530255);
