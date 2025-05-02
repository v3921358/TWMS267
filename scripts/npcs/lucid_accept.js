import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Lucid

let startMap = 450003600;
if (map.getId() == startMap) {
    npc.startBossUI(boss.index,boss.difficulty)
}
