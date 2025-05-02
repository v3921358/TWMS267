import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Dusk

let startMap = 450009301;
if (map.getId() == startMap) {
    portal.playPortalSE();
    portal.startBossUI(boss.index,boss.difficulty)
}