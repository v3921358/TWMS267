/*     
 *
 *  功能：[拉克蘭]墜落
 *

 */
if (player.isQuestCompleted(34325)) {
    player.startQuest(34326, 0, "3");
}
player.setInGameCurNodeEventEnd(true);
player.setStandAloneMode(true);
player.setInGameDirectionMode(true, false, false, false);
player.setForcedFlip(-1);
player.spawnTempNpc(3003252, -463, -21);
player.setNpcSpecialAction(3003252, "summon", 0, false);
//player.setNPCForceFlip(3003252, 1);
player.spawnTempNpc(3003256, 22, -27);
player.setNpcSpecialAction(3003256, "summon", 0, false);
player.spawnTempNpc(3003254, -67, -174);
player.setNpcSpecialAction(3003254, "summon", 0, false);
player.spawnTempNpc(3003255, 57, -169);
player.setNpcSpecialAction(3003255, "summon", 0, false);
player.spawnTempNpc(3003251, -374, -24);
player.setNpcSpecialAction(3003251, "sit", 999999, false);
//player.setNPCForceFlip(3003251, 1);
player.setLayerBlind(true, 255, 0);
npc.setCameraZoom(0, 1400, 0, -221, -73);
player.setForcedInput(0);
npc.setDelay(30);
//player.setOverlapDetail(0, 200, 200, true);

//player.removeOverlapDetail(1500);
player.setForcedInput(0);
//player.teleport(0); 
player.setLayerBlind(false, 255, 1000);
npc.id(3003252).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#防毒面具，你還好吧？");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#隨著夢境的減弱，我也在逐漸變弱，這意味著什麼…！！");
player.showNpcEffectPlay(3003251, "Effect/OnUserEff.img/emotion/oh", 0, 0, 0, true, 0, false, 0);
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#你感受到了嗎？");
player.changeBGM("SoundEff.img/ArcaneRiver/thrill");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#拉克蘭正在擴張！以極快的速度！");
npc.id(3003252).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#難道，她的目的是…！！");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#不僅是神祕河畔，她準備把全世界都吞噬在自己的夢境之中！");
npc.setDelay(1000);
player.playExclSoundWithDownBGM("SoundEff.img/ArcaneRiver/wind");
player.showNpcEffectPlay(3003252, "Map/Effect3.img/Lacheln/pung3", 0, 0, -30, true, 0, false, 0);
player.setNpcSpecialAction(3003252, "special", 99999999, false);

npc.setDelay(500);
player.playExclSoundWithDownBGM("SoundEff.img/ArcaneRiver/wind");
player.showNpcEffectPlay(3003256, "Map/Effect3.img/Lacheln/pung3", 0, 0, -30, true, 0, false, 0);
player.setNpcSpecialAction(3003256, "special", 99999999, false);

npc.setDelay(500);
player.playExclSoundWithDownBGM("SoundEff.img/ArcaneRiver/wind");
player.showNpcEffectPlay(3003254, "Map/Effect3.img/Lacheln/pung3", 0, 0, -30, true, 0, false, 0);
player.setNpcSpecialAction(3003254, "special", 99999999, false);

npc.setDelay(500);
player.playExclSoundWithDownBGM("SoundEff.img/ArcaneRiver/wind");
player.showNpcEffectPlay(3003255, "Map/Effect3.img/Lacheln/pung3", 0, 0, -30, true, 0, false, 0);
player.setNpcSpecialAction(3003255, "special", 99999999, false);

npc.setDelay(500);
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("啊！！");
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("都變成了清道夫？！！");
npc.setDelay(500);
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("希望與絕望………好朋友…");
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("如果一個朋友變強………其他朋友也會隨之變強。");
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("就算再怎麼掙扎………你們也無法擺脫這裡。");
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("這裡是夢境與幻想的城市。");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#快走！！#ho#!");
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("你們要去的地方…已經定好了。");
//player.setLayerBlind(true, 255, 100);
player.setLayerOn(1000, "11", 0, 0, -1, "Map/Effect2.img/BlackOut", 4);
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("再這樣要掉下去了！");
npc.setDelay(1000);
player.setLayerOn(1000, "0", 0, -80, -1, "Map/Effect3.img/Lacheln/4", 4);
npc.setDelay(3000);
player.setLayerOff(300, "0");
npc.setDelay(1000);
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("沒有浮上來。");
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("神祕河畔會處理他們的。");
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("他們會變成艾爾達…成為…偉大的他的能…");
npc.id(3003249).ui(1).uiMax().npcFlip().next().noEsc().sayX("毫無意義的結局…");
npc.setDelay(1000);
npc.id(3003250).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face5#呵呵，再見。");
npc.setDelay(1000);
player.changeBGM("Bgm00/Silence");
npc.id(3003250).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face2#等等。");
npc.id(3003250).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face2#剛才那個人是…");
npc.id(3003250).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face2#難道…");
//player.setOverlapDetail(0, 200, 200, true);
player.setLayerOff(300, "11");
player.changeMap(450003760, 1);
player.setInGameCurNodeEventEnd(true);
player.setStandAloneMode(false);
player.setInGameDirectionMode(false, false, false, false);
//player.removeOverlapDetail(1500);