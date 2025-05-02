// 
// Quest ID:17673
// [凱梅爾茲共和國] 證據不足


npc.next().sayX("雖然有嫌疑,但是沒有證據證言,無法公開抗議。");
npc.ui(1).uiMax().next().sayX("是的，關於那一點真的是很可惜。如果有西溫就好了。");
npc.next().sayX("已經是過去的事了有什麼辦法呢。別再想了。比起那個我有話對你說。");
npc.ui(1).uiMax().next().sayX("您有話對我說,是什麼事呢?");
npc.next().sayX("嗯..你能過一會兒再來找我說話嗎。我整理一下思緒");
npc.completeQuest();
player.gainExp(1058907);