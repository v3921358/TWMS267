import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Suu

let startMap = 350060300;
if (map.getId() == startMap) {
    portal.playPortalSE();
    portal.startBossUI(boss.index,boss.difficulty)
}