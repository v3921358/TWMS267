let event = player.getEvent("boss_kalos_extreme");
if (event != null && event.getVariable("reward") == null) {
        event.setVariable("reward", false)

        map.spawnMob(8881058, 0, 398);

        map.startFieldEvent();
}