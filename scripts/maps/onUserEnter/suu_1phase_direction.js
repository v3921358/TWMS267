player.setInGameCurNodeEventEnd(true)
player.setInGameDirectionMode(true, true, false, false)
player.setVansheeMode(1)
npc.delay(2000);
player.showSpineScreenSuu("intro", "Mob/BossPattern/BossEnterAni.img/suu/1phase")
npc.delay(1320);
map.playFieldSound("Sound/Etc.img/Suu1PhaseEnter0");
npc.delay(300);
map.playFieldSound("Sound/Etc.img/Suu1PhaseEnterEff");
npc.delay(5000);
player.setInGameDirectionMode(false, false, false, false)
player.changeMap(map.getId() + 100);