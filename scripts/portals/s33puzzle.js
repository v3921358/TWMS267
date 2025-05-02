let event = portal.getEvent();
if (event != null) {
        var pName = portal.getName();
        var sub = pName.substring(0, 2);
        if (player.isGm()) {
                player.dropMessage(6, "[Debug] pName:" + pName + " sub:" + sub);
        }
        switch (sub) {
                case "pt":
                        var rName = pName.substring(2, 4);//获得传送口对应反应堆的名字
                        var x = parseInt(rName.substring(0, 1));
                        var y = parseInt(rName.substring(1, 2));
                        player.dropMessage(1,"x: "+x+ "y "+y);
                        if (x == 5 && y == 5) {
                                if (!"clear".equals(event.getVariable("stage33"))) {
                                        event.setVariable("stage33", "clear");
                                        player.screenEffect("quest/party/clear");
                                        map.blowWeatherEffectNotice("你现在可以前往下一层了。", 147, 15000);
                                        player.teleportToPortalId(17);
                                }
                                break;
                        }
                        var rStat = map.getReactorStateId(rName);//获得相应反应堆的 状态
                        if (player.isGm()) {
                                player.dropMessage(6, "[Debug] rName:" + rName + " x:" + x + " y:" + y + " rStat:" + rStat);
                        }
                        var portalId = 0;
                        var newName ="";
                        switch (rStat) {
                                case 0://向左
                                        newName = ""+x +""+ (y - 1);
                                        
                                        portalId = map.getPortalIdByName("go" + newName);
                                        break;
                                case 1://向上
                                        newName = String(x - 1) + String(y);
                                        portalId = map.getPortalIdByName("go" + newName);
                                        break;
                                case 2://向右
                                        newName = String(x) + String(y + 1);
                                        portalId = map.getPortalIdByName("go" + newName);
                                        break;
                                case 3://向下
                                        if (x == 4 && y == 4) {
                                                portalId = map.getPortalIdByName("go55");
                                        } else {
                                                newName = String(x + 1) + String(y);
                                                portalId = map.getPortalIdByName("go" + newName);
                                        }
                                        break;
                        }
                        if (player.isGm()) {
                                player.dropMessage(6, "[Debug] portalId:" + portalId +"newName : "+newName);
                        }
                        if (portalId != -1) {
                                player.teleportToPortalId(portalId);
                        }
                        break;
        }
}
portal.abortWarp();
