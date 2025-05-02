/*
 * Boss: BlackMage - Extreme
 * Direction
 */

player.setInGameCurNodeEventEnd(true)
player.setStandAloneMode(true)
player.setInGameDirectionMode(true, false, false, false)
player.setLayerBlind(true, -1, 0)

player.changeBGM("Bgm00.img/Silence")
npc.setDelay(500)
switch (map.getId()) {
    case 450013900:
        player.showSpineScreen(0, "Effect/Direction20.img/bossBlackMage/start_spine/blasck_space", "animation", "intro")
        player.soundEffect("Sound/SoundEff.img/BM3/boss_start")
        break
    case 450013920:
        player.showSpineScreen(0, "Effect/Direction20.img/bossBlackMage/start2_spine/skeleton", "animation", "intro")
        player.soundEffect("Sound/SoundEff.img/BM3/boss_start2")
        break
    case 450013940:
        player.showSpineScreen(0, "Effect/Direction20.img/bossBlackMage/space/blasck_space", "animation", "intro")
        player.soundEffect("Sound/SoundEff.img/BM3/boss_start3")
        break
    case 450013960:
        player.showSpineScreen(0, "Effect/Direction20.img/bossBlackMage/start4_spine/black_Phase_3_4", "animation", "intro")
        player.soundEffect("Sound/SoundEff.img/BM3/boss_start4")
        break
}
npc.setDelay(5500)
player.setLayerOn(1000, "BlackOut", 0, 0, 13, "Map/Effect2.img/BlackOut", 4)
npc.setDelay(1000)
player.offSpineScreen("intro", 100)
player.setLayerOff(1000, "BlackOut")
player.setStandAloneMode(false)
player.setInGameDirectionMode(false, false, false, false)
player.changeMap(map.getId() + 10)


