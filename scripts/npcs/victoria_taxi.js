/*creat */
let taxiMaps = [100000000, 104000000, 102000000, 101000000, 103000000, 120000000, 105000000];


npc.sayNext("你好~!我是#p1012000#。你想快速又安全地移動到其他村莊嗎? 那麼就請使用令客戶百分百滿意的#b#p1012000##k吧。這次我給你免費優待!我將會送你去想去的地方。");
let prompt = "請選擇目的地。#b";
for (let i = 0; i < taxiMaps.length; i++) {
    if (taxiMaps[i] != map.getId()) {
        prompt += "\r\n#L" + i + "##m" + taxiMaps[i] + "##l";
    }
}
let mapIndex = npc.askMenu(prompt);
let selection = npc.askYesNo("看樣子, 你好像已經沒有什麼事情需要在這裡做了。確定要移動到#b#m" + taxiMaps[mapIndex] + "##k村莊嗎?");
if (selection == 1) {
    player.changeMap(taxiMaps[mapIndex]);
} else if (selection == 0) {
    npc.say("如果你想移動到其他村莊, 請隨時使用我們的計程車~");
}

