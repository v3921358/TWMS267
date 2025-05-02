if (!"o".equals(player.getQuestRecordEx(21002, "mo1"))) {
        player.updateQuestRecordEx(21002, "mo1", "o");
        player.showAvatarOriented("Effect/OnUserEff.img/guideEffect/aranTutorial/legendBalloon1");
}
portal.abortWarp();