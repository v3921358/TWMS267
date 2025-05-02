if (!"o".equals(player.getQuestRecordEx(21002, "arr1"))) {
        player.updateQuestRecordEx(21002, "arr1", "o");
        player.showAvatarOriented("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
}
portal.abortWarp();