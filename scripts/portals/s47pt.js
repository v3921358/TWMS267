let event = portal.getEvent();

if (player.isGm()) {
        player.showSystemMessage("Portal Name:" + portal.getName());
}
if (event != null) {
        if (portal.getName().equals(event.getVariable("stage47_Key"))) {
                if (!"1".equals(event.getVariable("stage47_Summon"))) {
                        event.setVariable("stage47_Summon", "1");
                        let mob = map.makeMob(9309128);
                        mob.changeBaseHp(50000000000);
                        map.spawnMob(mob, 500, 185);
                }
        }
} else {
        player.showSystemMessage("发生未知错误，请退出后重试!");
}
portal.abortWarp();