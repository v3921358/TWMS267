let event = player.getEvent("boss_suu_hard");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)
        
        let boss = map.makeMob(8881151);
        boss.changeBaseHp(6800000000000);
        // boss.setFieldActionType(1);
        // boss.setFieldActionValue(4755);
        map.spawnMob(boss, 515, 97);//main boss

        map.startFieldEvent();
}