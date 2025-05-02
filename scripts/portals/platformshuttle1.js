// runScript

let event = portal.getEvent();
if (event != null) {
        let nextName = event.getVariable("next");
        let c = event.getVariable("count");
        if (c == null) {
                c = 0;
        }
        if ("1".equals(nextName)) {
                c += 1;
                event.setVariable("count", String(c));
        }
        event.setVariable("next", "0");
        if (c / 2 >= 30) {
                player.teleportToPortalId(3, 0);
                player.runScript("platformar_done");
        }
}
portal.abortWarp();
