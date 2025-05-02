player.setInGameCurNodeEventEnd(true)
player.setInGameDirectionMode(true, true, false, false)
player.setVansheeMode(1)
npc.delay(2000);
player.showSpineScreenSuuField("Mob/BossPattern/BossEnterAni.img/suu/3phase/skeleton.skel", "3phase")
map.playFieldSound("Sound/Etc.img/Suu3PhaseEnterEff");
npc.delay(1500);
map.playFieldSound("Sound/Etc.img/Suu3PhaseEnter0");
npc.delay(6690);
map.playFieldSound("Sound/Etc.img/Suu3PhaseEnter1_1");
npc.delay(1200);
map.playFieldSound("Sound/Etc.img/Suu3PhaseEnter1_2");
npc.delay(1200);
map.playFieldSound("Sound/Etc.img/Suu3PhaseEnter1_3");
npc.delay(1200);
map.playFieldSound("Sound/Etc.img/Suu3PhaseEnter1_4");
npc.delay(1200);
map.playFieldSound("Sound/Etc.img/Suu3PhaseEnter2_0");
npc.delay(3600);
player.setInGameDirectionMode(false, false, false, false)
player.changeMap(map.getId() + 100);
