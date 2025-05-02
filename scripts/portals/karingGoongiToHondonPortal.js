if (map.getReactorStateId("hondonPt") > 0) {
    player.changeMap(map.getId() + 80, 0)
} else {
    portal.abortWarp()
}