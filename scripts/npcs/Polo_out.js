var text = `你同樣也是一名偉大的狩獵者...辛苦了！\r\n#b`
text += `#L1#領取烈焰戰狼狩獵獎勵#l\r\n`
text += `#L2#讓我回到原本的地方#l\r\n`
var sel = npc.askMenu(text)
switch (sel) {
    case 1:
        var damage = Number(player.getVariable("fire_wolf_damage"))
        /**
         * TODO:領取獎勵
         */
        npc.say(`你造成了${damage}點傷害！`)
        player.setVariable("fire_wolf_damage", 0);
        break
    case 2:
        player.setVariable("fire_wolf_damage", 0);
        let [map, spawnPoint] = npc.resetRememberedMap("RETURN_MAP");
        if (map == 999999999) { //warped to FM without having previous position saved
            map = 100000000;
            spawnPoint = 0;
        }
        player.changeMap(map, spawnPoint);
        break
    // go back to original place
}