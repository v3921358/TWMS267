
let event = portal.getEvent("Pirate");
if (event != null) {

        if (player.getAmountOfItem(4001120) >= 50 && player.getAmountOfItem(4001122) >= 5) {
                event.setVariable("stage2", "3");
                player.loseItem(4001120, 50);
                player.loseItem(4001122, 5);
        }
        if (event.getVariable("stage2").equals("3")) {
                portal.playPortalSE();
                party.changeMap(925100400, 1);
        } else {
                player.showSystemMessage("請收集初級X50個、高級X5個、海盜憑證，否則無法通過！");
                portal.abortWarp();
        }
} else {
        player.dropAlertNotice("发生错误！");
        player.changeMap(990001100, 0);
} 