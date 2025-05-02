import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Demian

let startMap = 105300303;
if (map.getId() == startMap) {
    portal.playPortalSE();
    portal.startBossUI(boss.index,boss.difficulty)
}