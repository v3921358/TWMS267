let event = player.getEvent("boss_suu_extreme");
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8881203, 27, 498);
    map.startFieldEvent();
}
