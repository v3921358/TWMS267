let event = player.getEvent("boss_suu_normal");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)
        
        map.spawnMob(8881103, 27, 498);
        map.startFieldEvent();
}