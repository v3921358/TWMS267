import { pic } from "scripts/expands/pic.js";
import { color } from "scripts/expands/color.js";
let itemList = Array(
2612080,
2612081,
2613068,
2613069,
2615051,
2615052,
2616216,
2616217,
2048825,
2048826,
);

let 箱子代碼 = 2439449;//箱子ID


let text = "#fc0xFF00FFFF# #i"+箱子代碼+"#〔 究極卷軸自選系統 〕：\r\n#n#fc0xFFFFE5B4#"
for (var i = 0; i < itemList.length; i++) {
			text += "#L" + i + "##v" + itemList[i] + "##z" + itemList[i] + "##l\r\n";
		}
let selected = npc.askMenuA(text);

text = ""+pic.寶箱+"#fc0xFF800000#欲兌換道具為：\r\n\r\n#i"+itemList[selected]+"#,#fc0xFF800080##z"+itemList[selected]+"#\r\n\r\n#r〔請幫我輸入想要兌換的數量：〕";

let YN = npc.askNumber(text,1,1,100);

	if(player.hasItem(箱子代碼,YN)) {
		var itemid = itemList[selected];
			player.gainItem(itemid,YN);
			player.loseItem(箱子代碼,YN);
			npc.say("#fs14##b本次兌換成功！");
		}else{
			npc.say("#fs14##r"+pic.wn2+" 身上沒有足夠的自選箱子！");
}