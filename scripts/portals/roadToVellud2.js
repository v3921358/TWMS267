// runScript

if (map.getId() == 863000100) {
    portal.abortWarp();
    player.runScript("beidler_npc");
} else {
    player.changeMap(863000100, 1);
}
