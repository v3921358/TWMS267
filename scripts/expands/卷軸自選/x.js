import { pic } from "scripts/expands/pic.js";
import { color } from "scripts/expands/color.js";
import { pic } from "./pic";

let itemList = Array(
2613048,
2613049,
2612055,
2612056,
2616059,
2616060,
2615029,
2615030,
2048815,
2048816


		);

let 箱子代碼 = 2439447;//箱子ID


let text = "#fc0xFF00FFFF# #i"+箱子代碼+"#〔 ｘ卷軸自選系統 〕：\r\n#n#fc0xFFFFE5B4#"
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