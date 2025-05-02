/**
 *
 *
 */
npc.next().sayX("給#p1013102#餵過飯了嗎？小不點你去吃早飯吧。今天的早飯是#t2022620#。我拿出來了，嘻嘻。事實上，如果你不去給#p1013102#餵食，我就不打算給你早飯吃。");
let ret = npc.askYesNo("來，給你#b三明治，吃完之後到媽媽那裡去一趟。#k她好象有話要跟你說。");
if (ret == 1) {
    if (player.canGainItem(2022620, 1)) {
        player.gainItem(2022620, 1);
        npc.startQuest();
        npc.ui(1).uiMax().next().sayX("#b(有話要跟我說？先把#t2022620#吃了，然後到屋裡去看看吧。)#k");
        let string = ["UI/tutorial/evan/3/0"];
        npc.sayImage(string);
    } else {
        npc.next().sayX("先整理下你的揹包消耗欄吧。");
    }
} else {
    npc.next().sayX("嗯？幹嘛？你不想吃早飯了嗎？不吃東西可不好。如果你想吃飯的話，就再來找我。不快點說的話，就要被我吃掉了啊？");
}
