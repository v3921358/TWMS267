//
// Quest ID:17681
// [凱梅爾茲共和國] 最終報告


npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("你來了啊，從你的臉上就能看到你很辛苦啊。");
npc.ui(1).uiMax().meFlip().next().sayX("真是多虧了某人啊。");
npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("什麼某人啊，你這說的像是有哪個人特意讓你受苦了啊。你的表情似乎是在說我就是那個人啊？");
npc.ui(1).uiMax().meFlip().next().sayX("……究竟帕爾巴特是什麼人，你給了他什麼才讓他帶我到凱梅爾茲的？還有，一開始去凱梅爾茲的船本身……");
npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("請你不要再說這些沒營養的話了。最終你不是沒什麼損失嗎？而且還交到了好朋友。當然，我並沒有預料到這一點。");
npc.ui(1).uiMax().meFlip().next().sayX("#b(……我感覺這次也被這個人玩弄於股掌之中啊。)#k");
npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("總之，辛苦你了。委託你做事的時候，總是有好結果呢。看來這就是意外之喜吧。");
npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("哎呀，我說錯了，這都是你有實力的結果啊。");
npc.ui(1).uiMax().meFlip().next().sayX("#b(……我居然忘了讓女皇教訓南哈特……)#k");

npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("對了，這個是女皇大人為了你準備的小小獎勵。\r\n\r\n#b#i2049702:##t2049702##k\r\n#b#i2431935:##t2431935##k\r\n#b#i2431936:##t2431936##k");
if (player.getFreeSlots(2) >= 3) {
    //let selection = npc.askMenuX("請挑選一件您中意的武器。\r\n#L1##b#i1432200:##t1432200##k#l\r\n#L2##b#i1442254:##t1442254##k#l", 1101002);
    npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("還有，你從凱梅爾茲帶來的書信裡，還附有這樣的東西。你可得好好保管它。\r\n\r\n#b#i1142981:##t1142981##k");
    if (player.gainItem(1142981, 1)) {
        player.gainItem(2049702, 1);
        player.gainItem(2431935, 1);
        player.gainItem(2431936, 1);
        npc.completeQuest();
        player.gainExp(11058907);
        npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("#face0#而我的謝禮嘛……\r\n以後我們一起去凱梅爾茲旅行吧。你不是說那裡風景很秀麗嗎。");
    } else {
        npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("揹包空間不足，請清理！");
    }

} else {
    npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("揹包空間不足，請清理！");
}