/*
 * Boss: Lucid
 * 过场动画1
 */
player.setInGameCurNodeEventEnd(true)
player.setInGameDirectionMode(true, false, false, false)
player.setLayerBlind(true, 255, 0)
player.showSpineScreen(0, "Map/Effect3.img/BossLucid/Lucid/lusi", "animation", "")
player.soundEffect("Sound/SoundEff.img/ArcaneRiver/lucid_spine")
npc.setDelay(9000)
player.setInGameCurNodeEventEnd(true)
player.inGameDirection22(700)
player.setInGameDirectionMode(false, true, false, false)
player.changeMap(450003840, 0)


