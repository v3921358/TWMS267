if (!"2".equals(player.getQuestRecordEx(34340, "enter"))) {
    npc.ui(1).uiMax().next().sayX("（好像無法經過這裡。）");
    npc.ui(1).uiMax().next().sayX("（還是先回去吧。）");
    player.changeMap(450003100, 0);
    player.updateQuestRecordEx(34340, "enter", "2");
}