if (map.getReactorStateId("goongiPt") > 0) {
    player.changeMap(map.getId() - 80, 0)
} else {
    portal.abortWarp()
}