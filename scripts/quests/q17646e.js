//
// Quest ID:17646
// [凱梅爾茲共和國] 海里的暴徒


npc.next().sayX("大家都活著嗎。");
npc.ui(1).uiMax().next().sayX("嗯,船隻好像受到很嚴重的破壞,但人員好像並沒有傷亡。");
npc.next().sayX("呼,深海巨妖並沒有全力撲來真是萬幸。它連頭都沒有探出來。看來它不是要吃我們,只是跟我們開個玩笑而已。");
npc.ui(1).uiMax().next().sayX("原來是我們運氣好。");
npc.next().sayX("不管怎樣,#h0#多虧有你在啊。儘管是開玩笑,深海巨妖開的玩笑對人類來說可就是關乎生死的玩笑了。");
npc.completeQuest();
player.gainExp(5953667);