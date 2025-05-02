// 
// Quest ID:17672
// [凱梅爾茲共和國] 洗脫嫌疑


npc.id(9390205).ui(1).uiMax().npcFlip().next().sayX("這人說的話都是事實。");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("啊,這不是..克萊爾你怎麼到這兒來了..!!");
npc.ui(1).uiMax().next().sayX("原來是班·特來敏的女兒克萊爾啊。");
npc.id(9390205).ui(1).uiMax().npcFlip().next().sayX("是的,您好嗎。首領大人。");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("你怎麼會來這裡?你現在不是在海本王國留學...");
npc.id(9390205).ui(1).uiMax().npcFlip().next().sayX("我的事情日後在另作稟報,父親。首領大人,這人說的話全部都是事實。我也在一旁聽得真真切切的,西溫想陷害這幾位的事實。");
npc.ui(1).uiMax().next().sayX("那是事實嗎?");
npc.id(9390205).ui(1).uiMax().npcFlip().next().sayX("是的,是事實。我還被他們綁架,多虧這幾位的幫助才逃脫出來的。");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("綁,綁架??豈,豈有此理...");
npc.ui(1).uiMax().next().sayX("請鎮靜。特來敏。謝謝你重要的證言,克萊爾小姐。多虧了你救了兩位差點兒枉受冤屈的年輕人。班·特來敏。你認為呢?你不會連你女兒的話都不相信了吧?");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("呃呃..好吧。先就那樣吧。克萊爾!你跟我來!");
npc.id(9390205).ui(1).uiMax().npcFlip().next().sayX("那麼我就告辭了。下次再見。");
npc.completeQuest();
player.gainExp(1058907);