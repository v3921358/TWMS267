let event = portal.getEvent();
portal.abortWarp();
if (event != null) {
        var ll = event.getVariable("stage4_portal");
        //inRight:1  inLeft:2
        if (ll != null) {
                player.teleportToPortalId(parseInt(ll));
        } else {
                player.teleportToPortalId(2);
        }
} else {
        player.teleportToPortalId(3, 2);
}

