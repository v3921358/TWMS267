import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.BlackMage

let startMap = 450012500;
if (map.getId() == startMap) {
    portal.playPortalSE();
    portal.startBossUI(boss.index,boss.difficulty)
}