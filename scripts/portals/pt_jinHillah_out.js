// runScript
portal.abortWarp()
if (map.getEventMobCount() > 0) {
    player.showSystemMessage("要退出，請點擊傳送點。")
} else {
    player.runScript("jinhillah_accept")
}