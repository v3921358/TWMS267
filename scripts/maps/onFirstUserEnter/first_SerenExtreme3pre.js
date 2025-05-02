let event = player.getEvent("boss_seren_extreme");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)
        
        map.spawnMob(8880664, 0, 257);
        
        map.startFieldEvent();
}