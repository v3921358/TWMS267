let event = portal.getEvent("party_dragon_rider");
if (event != null) {
        if (event.getVariable("boss2") == null) {
                event.setVariable("boss2", true);
                map.spawnMob(8300007, 250, -10);
        }
}
portal.abortWarp();

