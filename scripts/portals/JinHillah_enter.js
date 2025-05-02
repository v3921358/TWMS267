import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.JinHillah

let startMap = 450011990;
if (map.getId() == startMap) {
    portal.playPortalSE();
    portal.startBossUI(boss.index,boss.difficulty)
}