// 
// Quest ID:17650
// [凱梅爾茲共和國] 報告&憂慮


npc.ui(1).uiMax().next().sayX("首領大人,海盜討伐隊現在歸航了。");
npc.next().sayX("快來。看到你沒事我可真高興。看你的表情,任務很好地完成了啊。我說的對吧?");
npc.ui(1).uiMax().next().sayX("是的,你說對了。有了萊文的卓越指揮我們比想象中更輕鬆地擊退了敵人。");
npc.next().sayX("嗯,那真是萬幸。多虧了你們,現在海上交易也會進一步好轉了。");
npc.ui(1).uiMax().next().sayX("首領大人你的臉色不太好,是因為海本王國的原因嗎?");
npc.next().sayX("你聽說了嗎?你不提我還正在為那個頭疼呢。我真是琢磨不出這又會出什麼事兒。");
npc.completeQuest();
// Unhandled Stat Changed [CS_EXP] Packet: 00 00 00 00 01 00 00 00 00 00 61 EA CA 03 00 00 00 00 FF 00 00 00 00 
player.gainExp(953667);