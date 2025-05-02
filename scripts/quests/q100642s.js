// 
// Quest ID:100642
// [AWAKE] 賓果

if (npc.ui(1).noEsc().askAcceptX("#e[ 百人賓果！]#n\r\n\r\n百人賓果遊戲即將開啟，要來體驗下樂趣嗎？\r\n\r\n#b（同意後將切換#r頻道#k前往等待地圖）。")) {
    player.updateQuestRecordEx(100642, "rMap", String(map.getId()));
    if (player.getChannel() == 1) {
        player.changeMap(922290000);
    } else {
        player.changeMapAndChannel(922290000, 1);
    }
} else {
    /* Response is No */
    npc.ui(1).sayX("嗯……好的, 你去看看其他遊戲吧。真是遺憾……");
}