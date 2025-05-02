switch (map.getId()) {
        case 211060010:
                ortal.playPortalSE();
                player.changeMap(211060100, 2);
                break;
        case 211060000:
                if (player.isQuestStarted(38074) || player.isQuestStarted(38075)) {
                        player.updateQuestRecordEx(38074, "clear", "1");
                        player.setCustomData(38074, "clear");
                        player.completeQuest(38074, 0);
                        player.updateQuestRecordEx(38074, "clear");
                        player.startQuest(38075, 0);
                        player.changeMap(921110300, 1);
                        portal.playPortalSE();
                } else if (player.isQuestStarted(3143) || player.isQuestCompleted(3143)) {
                        portal.playPortalSE();
                        player.changeMap(211060010, 1);
                } else {
                        portal.abortWarp();
                        player.scriptProgressMessage("在未經允許前不允許進入。");
                }
                break;
        default:
                portal.abortWarp();
}