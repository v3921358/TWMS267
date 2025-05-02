let event = portal.getEvent();

if (event != null) {
        var stage = parseInt(event.getVariable("stage"));

        var rStat = map.getReactorStateId("g") + 1;
        var rValue = parseInt(player.getQuestRecordEx(42006, "g"));

        if (rValue > rStat) {//获得的数字 大于当前的数字 则进行处理
                player.updateQuestRecordEx(42006, "gc", String(rValue));
                player.showAvatarOrientedRepeat(false, "");
                player.showAvatarOriented("Effect/OnUserEff.img/aquarisTower/success");
                player.updateQuestRecordEx(42006, "g", "0");
                map.setReactorState("g", rValue - 1, 0);
                var rc = player.getQuestRecordEx(42006, "rc");
                var gc = player.getQuestRecordEx(42006, "gc");
                var bc = player.getQuestRecordEx(42006, "bc");
                var yc = player.getQuestRecordEx(42006, "yc");
                if (rc != "0" && gc != "0" && bc != "0" && yc != "0") {
                        //完成当前阶段
                        if (event.getVariable("stage" + stage) != "clear") {
                                event.setVariable("stage" + stage, "clear");
                                player.screenEffect("quest/party/clear");
                                event.getVariable("map" + stage).blowWeatherEffectNotice("你现在可以前往下一层了。", 147, 15000);
                        }
                }
        }
} else {
        player.showSystemMessage("发生未知错误！");
}
portal.abortWarp();