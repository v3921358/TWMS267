let event = player.getEvent("boss_suu_extreme");
if (event != null && event.getVariable("boss3") == null) {
        event.setVariable("boss3", false)
        
        let boss = map.makeMob(8881202);
        boss.changeBaseHp(720000000000000);
        // boss.setFieldActionType(1);
        // boss.setFieldActionValue(4755);
        map.spawnMob(boss, 515, 90);//main boss
        
        map.startFieldEvent();
}