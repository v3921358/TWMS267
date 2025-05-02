if (!"o".equals(player.getQuestRecordEx(21002, "normal"))) {
        player.updateQuestRecordEx(21002, "normal", "o");
        player.scriptProgressMessage("按Ctrl鍵進行普通攻擊吧！");
        player.showAvatarOriented("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialGuide1");
}
portal.abortWarp();