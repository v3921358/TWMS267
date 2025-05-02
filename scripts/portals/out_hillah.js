if (npc.id(2184001).askYesNo("要在這放棄嗎？")) {
        npc.id(2184001).next().say("沒辦法啊。謝謝你幫我到這裡。");
        player.changeMap(262030000, 0);
} else {
        /* Response is No */
        npc.id(2184001).say("請擊退希拉，救出阿斯旺吧。");
}