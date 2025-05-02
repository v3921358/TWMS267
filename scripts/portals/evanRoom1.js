// runScript
if (!"o".equals(player.getQuestRecordEx(22013, "hand"))) {
        player.updateQuestRecordEx(22013, "hand", "o");
        player.runScript("evan_hand");
}
portal.abortWarp();

