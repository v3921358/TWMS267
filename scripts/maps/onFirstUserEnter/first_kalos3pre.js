let event = player.getEvent("boss_kalos_normal");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)

        map.spawnMob(8880808, 0, 398);

        map.startFieldEvent();
}