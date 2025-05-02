// ID:[807300210]
// MapName:秘密祭壇
// 秘密祭壇


if (map.getId() == 807300110) {
        let event = player.getEvent("boss_ranmaru_normal");
        if (event != null && event.getVariable("npc") == null) {
                event.setVariable("npc", true);
                map.spawnTempNpc(9130090, -424, -15);
        }
} else if (map.getId() == 807300210) {
        let event = player.getEvent("boss_ranmaru_chaos");
        if (event != null && event.getVariable("npc") == null) {
                event.setVariable("npc", true);
                map.spawnTempNpc(9130090, -424, -15);
        }
}
