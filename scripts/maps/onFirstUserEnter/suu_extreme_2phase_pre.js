let event = player.getEvent("boss_suu_extreme");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)
        
        let boss = map.makeMob(8881201);
        boss.changeBaseHp(545000000000000);
        // boss.setFieldActionType(1);
        // boss.setFieldActionValue(4755);
        map.spawnMob(boss, 515, 97);//main boss

        map.startFieldEvent();
}