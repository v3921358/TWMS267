/**
 * Roger's Apple (NPC 2000 Quest 1021) End
 * Maple Road: Lower level of the Training Camp (Map 2)
 *
 * Gives advice to new players on how to use items.
 *
 *
 */

npc.next().sayX("How easy is it to consume the item? Simple, right? You can set a #bhotkey#k on the right bottom slot. Haha you didn't know that! right? Oh, and if you are a beginner, HP will automatically recover itself as time goes by. Well it takes time but this is one of the strategies for the beginners.");
npc.next().sayX("Okay, this is all I can teach you. I know it's sad but it is time to say good bye. Well take care if yourself and Good luck my friend!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2010000# 3 #t2010000#\r\n#v2010009# 3 #t2010009#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10 exp");
player.gainExp(10);
player.gainItem(2010000, 3);
player.gainItem(2010009, 3);
npc.completeQuest();