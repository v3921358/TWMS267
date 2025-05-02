import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Seren

let startMap = 410000670;
if (map.getId() == startMap) {
    npc.startBossUI(boss.index,boss.difficulty)
}