/*     
 *
 *  功能：[消亡旅途]失去記憶的神官
 *

 */
if (player.isQuestCompleted(1466)) {
    npc.ui(1).uiMax().next().sayX("#b（詢問到底發生了什麼事。）#k");
    npc.next().sayX("看來得先提提這件事了，你見過村莊外圍的那條寬闊的湖嗎？聽村民說，那條湖叫做忘卻之湖，一旦墜入便會失去所有的記憶。");
    npc.next().sayX("可能是受到了湖的影響，據說這周圍的人每天都會丟失一點點的記憶，所以才有了這記憶之樹的存在。大家會將珍貴的記憶掛在樹上，每日細細端詳，趁著它們風乾消失之前……");
    npc.next().sayX("聽村民們提到了樹的事情之後，我的心咯噔了一下，那上面是否也會有和我相關的記憶呢，我便立刻下定決心去調查那棵樹，可是……");
    npc.next().sayX("可是不知為何，我剛一碰到就……");
    player.setInGameCurNodeEventEnd(true);
    player.setStandAloneMode(true);
    player.setInGameDirectionMode(true, false, false, false);
    player.setForcedInput(0);
    npc.setDelay(30);
    player.setInGameCurNodeEventEnd(true);
    player.changeBGM("Bgm00/Silence");
    player.setLayerOn(0, "11", 0, 0, -1, "Map/Effect2.img/BlackOut", 4);
    player.setLayerOn(2000, "0", 0, -80, -1, "Map/Effect2.img/ArcaneRiver1/tree1", 4);
    player.playExclSoundWithDownBGM("SoundEff.img/ArcaneRiver/tree_revive");
    npc.setDelay(4000);
    player.setLayerOff(300, "0");
    player.setLayerOn(2000, "1", 0, -80, -1, "Map/Effect2.img/ArcaneRiver1/tree2", 4);
    player.playExclSoundWithDownBGM("SoundEff.img/ArcaneRiver/tree2");
    npc.setDelay(3000);
    player.setLayerOff(300, "1");
    player.setLayerOn(2000, "2", 0, -80, -1, "Map/Effect2.img/ArcaneRiver1/tree3", 4);
    player.playExclSoundWithDownBGM("SoundEff.img/ArcaneRiver/tree");
    npc.setDelay(5000);
    player.setLayerOff(300, "2");
    player.setLayerOff(300, "11");
    player.setStandAloneMode(false);
    player.setInGameDirectionMode(false, false, false, false);
    player.changeBGM("Bgm46/Lake Of Oblivion");
    npc.next().sayX("那些記憶……村民的珍貴的記憶……全都四散而去，人們大失所望，就連每日重複的那些工作也都停工了，負責在忘卻之湖上渡河的船也停運了。");
    npc.completeQuest();
} else {
    npc.ui(1).sayX("... 要怎麼做才能撫慰他們的心靈呢？\r\n\r\n#b（必須完成<另一種力量，神祕力量>任務，獲得神祕徽章。）");
}