let event = player.getEvent("boss_kalos_easy");
if (event != null && event.getVariable("boss3") == null) {
        event.setVariable("boss3", false)

        map.spawnMob(8881018, 0, 398);

        map.startFieldEvent();
}