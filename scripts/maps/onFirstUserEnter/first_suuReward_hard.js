let event = player.getEvent("boss_suu_hard");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)
        map.spawnMob(8881153, 27, 498);
        map.startFieldEvent();
}