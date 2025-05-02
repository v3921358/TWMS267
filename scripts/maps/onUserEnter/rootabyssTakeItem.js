const EVENT_NAME = "boss_banban_normal";
const MONSTER_ID = 7120110;
const Y_POSITION = 221;

let event = player.getEvent(EVENT_NAME);

if (event && event.getVariable("monster") === null) {
    event.setVariable("monster", true);

    for (let x = 1; x <= 10; x++) {
        let monster = map.makeMob(MONSTER_ID);
        map.spawnMob(monster, 350 * x, Y_POSITION);
    }
}