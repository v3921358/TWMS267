if (npc.askYesNo("#b是否接收楓葉黃金徽章 #i1190301#？")) {
    player.gainItem(1190301, 1);
    npc.sayX("#b請務必好好保存。")
    npc.completeQuest();
} else {
    npc.next().sayX("#b如果想接收時，請再來找我，這是您為楓之谷世界貢獻的禮物。");
}
