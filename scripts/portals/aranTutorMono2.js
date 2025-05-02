if (!"o".equals(player.getQuestRecordEx(21002, "mo3"))) {
        player.updateQuestRecordEx(21002, "mo3", "o");
        player.showAvatarOriented("Effect/OnUserEff.img/guideEffect/aranTutorial/legendBalloon3");
}
portal.abortWarp();