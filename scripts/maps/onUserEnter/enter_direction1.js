/*
 * Boss: Demian
 * 剧情1
 */

player.setInGameCurNodeEventEnd(true)
player.setStandAloneMode(true)
player.setInGameDirectionMode(true, false, false, false)
player.setLayerBlind(true, -1, 0)
player.showSpineScreen(0, "Map/Effect2.img/DemianIllust/1pahseSp/demian", "animation", "intro")
player.soundEffect("Sound/SoundEff.img/BossDemian/phase1")
npc.setDelay(8000)
player.setLayerOn(1000, "BlackOut", 0, 0, 13, "Map/Effect2.img/BlackOut", 4)
npc.setDelay(1000)
player.offSpineScreen("intro", 100)
player.setLayerOff(1000, "BlackOut")
player.setStandAloneMode(false)
player.setInGameDirectionMode(false, false, false, false)
player.changeMap(map.getId() - 60, 0)


