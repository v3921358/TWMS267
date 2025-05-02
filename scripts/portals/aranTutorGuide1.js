if (!"o".equals(player.getQuestRecordEx(21002, "chain"))) {
        player.updateQuestRecordEx(21002, "chain", "o");
        player.scriptProgressMessage("按Ctrl鍵進行普通攻擊吧！");
        player.showAvatarOriented("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialGuide2");
}
portal.abortWarp();