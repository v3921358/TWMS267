import { spawnBoss } from "scripts/modals/dojang_spawn.js";

let event = player.getEvent("dojang_event");
if (event != null) {
    if (map.getEventMobCount() > 0) {
        map.clearMobs()
    }
    spawnBoss(map);
    let ss = map.getId() - 925070000;
    player.soundEffect("Dojang/start")
    player.showScreenAutoLetterBox("dojang/start/stage", -1)
    player.showScreenAutoLetterBox("dojang/start/number/" + parseInt(ss / 100), -1)

}