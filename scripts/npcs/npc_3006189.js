import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Karing
let startMap = 410007025;
if (map.getId() == startMap) {
    npc.startBossUI(boss.index, boss.difficulty)
}
