
if (!"o".equals(player.getQuestRecordEx(22013, "egg"))) {
        player.updateQuestRecordEx(22013, "egg", "o");
        player.runScript("evan_dragon_egg");
}
portal.abortWarp();
