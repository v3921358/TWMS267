player.setInGameCurNodeEventEnd(true);
player.setStandAloneMode(true);
player.setInGameDirectionMode(true, false, false, false);
player.setLayerBlind(true, -1, 0);
player.showSpineScreen(0, "Effect/Direction20.img/bossKalos/1phase_spine/skeleton", "animation", "intro");
player.soundEffect("Sound/SoundEff.img/kalos/1phase");
npc.setDelay(7000);
player.offSpineScreen("intro", 100);
player.setLayerOff(1000, "BlackOut");
player.setStandAloneMode(false);
player.setInGameDirectionMode(false, false, false, false);
player.changeMap(410006220);




