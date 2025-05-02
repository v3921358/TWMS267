// runScript

if (!"o".equals(player.getQuestRecordEx(89741, "arr2"))) {
        player.updateQuestRecordEx(89741, "arr2", "o");
        player.runScript("Resi_tutor61");
}
portal.abortWarp();