if (!"o".equals(player.getQuestRecordEx(22013, "dt01"))) {
        player.updateQuestRecordEx(22013, "dt01", "o");
        player.showScreenDelayedEffect("evan/dragonTalk01", 0);
}
portal.abortWarp();
