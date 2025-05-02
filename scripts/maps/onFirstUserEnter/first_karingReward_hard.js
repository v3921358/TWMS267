let event = player.getEvent("boss_karing_hard");
if (event != null && event.getVariable("reward") == null) {
        map.spawnMob(8880945, 486, 105);
}
