let event = player.getEvent("boss_slime_normal");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)
        
        map.spawnMob(8880726, 0, 316);
        
        map.startFieldEvent();
}