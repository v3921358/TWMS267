/*
 * Boss: Lucid
 * 过场动画2
 */

player.setInGameCurNodeEventEnd(true)
player.setInGameDirectionMode(true, false, false, false)
player.setLayerBlind(true, 255, 0)
npc.setDelay(500)
player.setInGameCurNodeEventEnd(true)
player.showNpcEffectPlay(0, "Map/Effect3.img/BossLucid/Lucid5", 0, 89, 36, 1, 1, 1, 0, null)
player.setLayerBlind(false, 0, 0, 0, 0, 1500, 0)
player.soundEffect("Sound/SoundEff.img/ArcaneRiver/phase2", 200, 0, 0)
player.showNpcEffectPlay(0, "Map/Effect3.img/BossLucid/Lucid2", 0, 89, 36, 1, 10, 1, 0, null)
player.showNpcEffectPlay(0, "Map/Effect3.img/BossLucid/Lucid3", 0, -140, 100, 1, 11, 1, 0, null)
player.showNpcEffectPlay(0, "Map/Effect3.img/BossLucid/Lucid4", 0, 89, 36, 1, 1, 1, 0, null)
npc.setDelay(2000)
player.setLayerBlind(true, 255, 240, 240, 240, 1300, 0)
npc.setDelay(1600)
player.inGameDirection22(700)
player.setInGameDirectionMode(false, true, false, false)
let diff = 50;
if (map.getId() == 450003880) {
        diff = 40;
}
player.changeMap(map.getId() + diff, 0);