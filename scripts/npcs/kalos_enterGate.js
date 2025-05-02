import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.JinHillah

let startMap = 410005005;
if (map.getId() == startMap) {
    npc.startBossUI(boss.index,boss.difficulty)
}