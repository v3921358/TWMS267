import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Slime

let startMap = 160000000;
if (map.getId() == startMap) {
    npc.startBossUI(boss.index,boss.difficulty)
}