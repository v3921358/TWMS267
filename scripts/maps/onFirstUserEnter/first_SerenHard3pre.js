let event = player.getEvent("boss_seren_hard");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)
        
        map.spawnMob(8880614, 0, 257);
        
        map.startFieldEvent();
}