import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Will
let startMap = 450007240;
if (map.getId() == startMap) {
    npc.startBossUI(boss.index, boss.difficulty)
}