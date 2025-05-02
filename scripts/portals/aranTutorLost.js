if (!"o".equals(player.getQuestRecordEx(21002, "fin"))) {
        player.updateQuestRecordEx(21002, "fin", "o");
        player.showReservedEffect("Effect/Direction1.img/aranTutorial/Child");
        player.showReservedEffect("Effect/Direction1.img/aranTutorial/ClickChild");
}
portal.abortWarp();