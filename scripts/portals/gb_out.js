if (player.getMapId() == 941000200 || player.getMapId() == 941000100) {
    player.changeMap(941000000)
} else if (player.getMapId() == 941000000) {
    const rMap = player.getIntQuestRecordEx(100811, "rMap")
    if (rMap > 0) {
        player.changeMap(rMap)
    } else {
        player.changeMap(100000000)
    }
}