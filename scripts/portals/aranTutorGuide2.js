if (!"o".equals(player.getQuestRecordEx(21002, "cmd"))) {
        player.updateQuestRecordEx(21002, "cmd", "o");
        player.showAvatarOriented("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialGuide3");
}
portal.abortWarp();