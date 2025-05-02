// 
// Quest ID:17005
// [凱梅爾茲交易所]首次航行
npc.sayNextIllusNoEsc("好了,那我現在告訴你一些有關正式貿易的情況吧。開始貿易之後,可以先設定目的地。 ", 9390220, 9390220, 1, true);
if (!"1".equals(player.getQuestRecordEx(17009, "step"))) {
    player.updateQuestRecordEx(17009, "step", "1");
}
player.openUI(554);
npc.sayNextIllusNoEsc("有藍色標記的地區是可以進行貿易的地區。 ", 9390220, 9390220, 1, true);
npc.sayNextIllusNoEsc("雖然現在還只能去 [多爾切]地區,但是隨著貿易次數的增多,能去的地區也會變多的。", 9390220, 9390220, 1, true);
npc.sayIllusNoEsc("只要將滑鼠放在貿易區上方, 就能看到貿易區資訊了。先試著移動到[多爾切]地區吧？", 9390220, 9390220, 1, true);
