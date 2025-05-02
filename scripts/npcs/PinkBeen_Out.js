// [被遺忘的神殿管理人]  |  [2141002]
// MapName:神祇的黃昏

if (npc.askYesNo("你要停止戰鬥後離開嗎？")) {
        player.changeMap(parseInt(map.getId() / 1000) * 1000 + 300, 0);
} else {
        npc.next().sayX("請繼續挑戰。");
}