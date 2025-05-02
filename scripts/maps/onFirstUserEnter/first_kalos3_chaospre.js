let event = player.getEvent("boss_kalos_chaos");
if (event != null && event.getVariable("boss3") == null) {
        event.setVariable("boss3", false)

        map.spawnMob(8881038, 0, 398);

        map.startFieldEvent();
}