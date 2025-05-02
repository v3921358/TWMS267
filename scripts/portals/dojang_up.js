let diff = map.getId() - 925070000;
let ss = parseInt(diff / 100);
let event = player.getEvent("dojang_event");
if (event != null) {
    let portalId = parseInt(Math.random() * 5);
    if (map.getId() == 925070000) {
        event.stopTimer("StartUp");//停止倒計時傳送到第一層
        portal.playPortalSE();
        player.changeMap(925070100, portalId);
    } else if ("1".equals(event.getVariable("Stage_" + ss))) {
        portal.playPortalSE();
        player.changeMap(map.getId() + 100, portalId);
    } else {
        player.showSystemMessage("大門尚未開啟。");
    }
}