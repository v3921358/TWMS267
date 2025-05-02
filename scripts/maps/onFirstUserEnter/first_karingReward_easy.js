let event = player.getEvent("boss_karing_easy");
if (event != null && event.getVariable("reward") == null) {
        map.spawnMob(8880915, 486, 105);
}
