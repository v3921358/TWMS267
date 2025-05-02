import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Kalos

let startMap = map.getId();
if (map.getId() == startMap) {
    portal.playPortalSE();
    portal.startBossUI(boss.index,boss.difficulty)
}
