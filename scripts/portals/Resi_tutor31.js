// runScript
if (!"o".equals(player.getQuestRecordEx(89741, "arr1"))) {
        player.updateQuestRecordEx(89741, "arr1", "o");
        player.runScript("Resi_tutor31");
}
portal.abortWarp();