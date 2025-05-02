player.setInGameCurNodeEventEnd(true)
player.setInGameDirectionMode(true, true, false, false)
player.setVansheeMode(1)
npc.delay(2000);
player.showSpineScreenSuuField("Mob/BossPattern/BossEnterAni.img/suu/2phase/skeleton.skel", "2phase")
map.playFieldSound("Sound/Etc.img/Suu2PhaseEnterEff");
npc.delay(2700);
map.playFieldSound("Sound/Etc.img/Suu2PhaseEnter0");
npc.delay(9300);
map.playFieldSound("Sound/Etc.img/Suu2PhaseEnter1");
npc.delay(6000);
player.setInGameDirectionMode(false, false, false, false)
player.changeMap(map.getId() + 100);