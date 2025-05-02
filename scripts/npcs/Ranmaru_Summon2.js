
// [森蘭丸]  |  [9421581]
// MapName:秘密祭壇

if (party == null || player.getCharacterId() != party.getLeader().getCharacterId()) {
        npc.noEsc().next().sayX("請讓隊長與我交談。");
} else if (npc.noEsc().askAcceptX("別再多說了，你能阻止我嗎？#k")) {
        let event = player.getEvent("boss_ranmaru_hard");
        if (event != null && event.getVariable("boss") == null) {
                event.setVariable("boss", false);
                map.spawnMob(9421583, -424, -15);
                event.startTimer("Spawn_R", 60 * 1000);
                event.startTimer("Spawn_B", 2 * 60 * 1000);
                event.startTimer("Spawn_P", 3 * 60 * 1000);
        }
        map.destroyTempNpc(9130091);
}
