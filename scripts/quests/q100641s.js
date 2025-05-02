//
// Quest ID:100642
// [AWAKE] 剪刀石頭布

if (npc.ui(1).noEsc().askAcceptX("#e[ 剪刀石頭布！]#n\r\n\r\n百人剪刀石頭布遊戲即將開啟，要來體驗下樂趣嗎？\r\n\r\n#b（同意後將切換#r頻道#k前往等待地圖）。")) {
    player.updateQuestRecordEx(100641, "rMap", String(map.getId()));
    if (player.getChannel() == 1) {
        player.changeMap(993030000);
    } else {
        player.changeMapAndChannel(993030000, 1);
    }
} else {
    /* Response is No */
    npc.ui(1).sayX("嗯……好的, 你去看看其他遊戲吧。真是遺憾……");
}