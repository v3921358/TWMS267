let event = player.getEvent("boss_seren_normal");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)
        
        map.spawnMob(8880644, 0, 257);
        
        map.startFieldEvent();
}