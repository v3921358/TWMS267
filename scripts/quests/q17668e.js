//
// Quest ID:17668
// [凱梅爾茲共和國] 西溫的消失


npc.id(9390202).ui(1).npcFlip().next().sayX("特來敏小姐你原來在這裡啊。你沒事嗎?沒有受傷嗎?");
npc.next().sayX("是的,我沒事。你們好像也沒事呢。");
npc.id(9390202).ui(1).npcFlip().next().sayX("是的,託你的福我們也沒事。");
npc.next().sayX("西溫的部下們怎麼樣了?");
npc.ui(1).uiMax().next().sayX("他們你就不用擔心了。只是.. 可惜最後西溫還是被殺了。");
npc.next().sayX("怎麼會這樣...");
npc.id(9390202).ui(1).npcFlip().next().sayX("不管怎樣大家都沒事真是萬幸。");
npc.completeQuest();
player.gainExp(1058907);