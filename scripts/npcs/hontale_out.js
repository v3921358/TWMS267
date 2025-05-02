// [樹根水晶]  |  [2083002]
// MapName:闇黑龍王洞穴入口

if (npc.askYesNo("要回到#m240050000#嗎?")) {
        /* Response is Yes */
        player.changeMap(240050000, 0);
} else {
        /* Response is No */
        npc.say("想好後在與我說話.");
}