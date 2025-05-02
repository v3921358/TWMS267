let event = player.getEvent("boss_suu_hard");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        
        let boss = map.makeMob(8881150);
        boss.changeBaseHp(1600000000000);
        // boss.setFieldActionType(1);
        // boss.setFieldActionValue(4755);
        map.spawnMob(boss, 0, -16);//main dummy boss
        map.startFieldEvent();
}