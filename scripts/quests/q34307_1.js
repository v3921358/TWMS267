var questId = 34307;
player.startQuest(questId, 0)
if (player.isQuestStarted(questId)) {
    player.completeQuest(questId, 0);
}
if (player.isQuestStarted(34307)) {
    npc.next().sayX("吃是幸福的事，不停地吃代表著無盡的幸福，我在幸福中不停的吃。那是我的幸福，無盡的幸福。");
    player.updateQuestRecordEx(34307, "NpcSpeech", "30032281");
    player.startQuest(34340, 3003228, "1");
} else {
    npc.ui(1).sayX("吃是幸福的事，不停地吃代表著無盡的幸福，我在幸福中不停的吃。那是我的幸福，無盡的幸福。");
}