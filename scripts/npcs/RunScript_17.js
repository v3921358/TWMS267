if (npc.askYesNo("#r#e巨龍決戰#n#k正等著勇士您挑戰！\r\n#b#e是否進入聯盟團戰#n#k？")) {
    player.changeMap(921172000);
} else {
    npc.say("因不明原因無法送您進場。請稍後再試。");
}