import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";


var 黑點 = "#fEffect/CharacterEff.img/1102355/0/0#";
var 右藍星 = "#fEffect/CharacterEff.img/1051296/1/2#";
var 左藍星 = "#fEffect/CharacterEff.img/1051296/1/3#";

var text = "\t  " + pic.立體金星星 + pic.粗楷體 + "#b來自楓之谷世界的某人" + pic.粗楷體 + pic.立體金星星 + "\r\n";
text += "\t     " + pic.立體金星星 + pic.粗楷體 + "#b歡迎你加入" + pic.粗楷體 + pic.立體金星星 + "\r\n";
text += "#L0#" + pic.金箭頭右 + "  [ #b領取#k ] - 萬能主選單。#l\r\n\r\n";
text += "#L1#" + pic.金箭頭右 + "  [ #r未滿#k ] - 升級至一轉和離開。#l\r\n\r\n";
var selection = npc.askMenu(text)

switch (selection) {
    case 0:
        if (!player.hasItem(2436058)) {
            player.gainItem(2437007, 1); /* 領取主選單 */
        } else {
            npc.say(pic.楷體 + "您已經具有主選單。");
        }
        break;
    case 1:
        if (player.getLevel() < 10) {
            player.setLevel(10);
            player.gainInventorySlots(1, 48);
            player.gainInventorySlots(2, 48);
            player.gainInventorySlots(3, 48);
            player.gainInventorySlots(4, 48);
            player.changeMap(104020000);
        } else {
            player.changeMap(104020000);
            npc.say(pic.楷體 + "您等級已經超出10等了,無法進行此操作。");
        }
        break;
}
