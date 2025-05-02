let selection = npc.askYesNo("你好。我是負責家族支援工作的蕾雅。為了工作方便，我來到了英雄殿堂，為大家提供幫助。你想到英雄殿堂去處理家族相關事務嗎？");
if (selection == 1) {
    npc.sayNext("好的，我馬上把你送過去。");
    npc.rememberMap("GUILD_FIELD");
    player.changeMap(200000301, 0);
} else {
    npc.sayNext("想去英雄殿堂的話，請再來找我。");
}
