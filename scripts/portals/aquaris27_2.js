var event = portal.getEvent();
if (event != null) {
        var name = portal.getName();
        var sub2 = name.substring(0, 2);
        switch (sub2) {
                case "pt":
                        var ptID = "0" + name.substring(5, 6);
                        var check = event.getVariable("stage27_trap2");
                        if (!ptID.equals(check)) {
                                player.teleportToPortalId(2);
                                map.blowWeatherEffectNotice("該死！ 有陷阱。 讓我們繞過這裡從別處走吧。", 147, 5000);
                        }
                        break;
                case "lv":
                        var lvID = name.substring(2, 3);
                        if (!"lv1_2_3".equals(name)) {
                                player.teleportToPortalId(2);
                                map.blowWeatherEffectNotice("該死！ 有陷阱。 讓我們繞過這裡從別處走吧。", 147, 5000);
                        }
                        break;
        }
}
portal.abortWarp();