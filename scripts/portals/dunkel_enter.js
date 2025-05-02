import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Dunkel

let startMap = 450012200;
if (map.getId() == startMap) {
    portal.playPortalSE();
    portal.startBossUI(boss.index,boss.difficulty)
}