// [敢死隊的暗號石版]  |  [2083000]
// MapName:洞穴入口

if (player.hasItem(4001086, 1)) {
        if (npc.askYesNo("寫在石板的字在發光打開在石板後的門.要移動到秘密通道嗎??")) {
                /* Response is Yes */
                player.changeMap(240050400, 0);
        } else {
                /* Response is No */
                npc.next().sayX("要移動的話就跟我對話.");
        }
} else {
        npc.say("無法讀出石板上的內容. 不知是做什麼用的.");
}