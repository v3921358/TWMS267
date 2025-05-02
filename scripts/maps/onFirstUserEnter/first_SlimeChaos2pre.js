let event = player.getEvent("boss_slime_chaos");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)
        
        map.spawnMob(8880725, 0, 316);
        
        map.startFieldEvent();
}