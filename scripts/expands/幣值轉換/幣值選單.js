import { pic } from "scripts/expands/pic.js";
import func from 'scripts/expands/func.js';

let Nx2 = player.getPoint(); ///取得楓葉點數
let Nx1 = player.getCash(); ///取得樂豆點數

var str = "";
str += ""+pic.問題和解答圖檔+"#fs13##b ： 楓之谷幣值轉換\r\n#fc0xFF800080#";
str += "#L1#"+pic.金方框+"."+pic.金色右指示箭頭+"〔樂豆點數購買樂豆鑽〕#l\r\n";
str += "#L2#"+pic.金方框+"."+pic.金色右指示箭頭+"〔 樂豆鑽轉換為樂豆 〕#l\r\n\r\n";

str += "#L3#"+pic.藍方框+"."+pic.金色右指示箭頭+"〔楓葉點數購買楓點鑽〕#l\r\n";
str += "#L4#"+pic.藍方框+"."+pic.金色右指示箭頭+"〔 楓點鑽轉換為楓點 〕#l\r\n\r\n";

let selection_menu = npc.askMenu(str);

switch (selection_menu) {
    case 1: ///樂豆點數購買樂豆鑽
		let 購買樂豆鑽數量 = npc.askNumber("#fc0xFF800080#〔消耗#b樂豆點數#fc0xFF800080#,轉換為 #i4034848# #fc0xFF800080#〕：\r\n#r〔請輸入購買數量〕：\r\n",1,1,99999);
		if(100 * 購買樂豆鑽數量 > player.getCash()){
		player.dropAlertNotice("樂豆點數不足");
		}else{
		player.modifyCashShopCurrency(1,-(100*購買樂豆鑽數量));
		player.gainItem(4034848,購買樂豆鑽數量);
		player.dropAlertNotice("兌換成功\r\n共獲得：〔"+購買樂豆鑽數量+"〕個樂豆鑽");
		player.dropMessage(8, "本次購買消耗樂豆點數："+購買樂豆鑽數量*100+" 點");
		}
        break;
	case 2: ///樂豆鑽轉換為樂豆
	    let 樂豆鑽ID = 4034848;
		let 樂豆數量 = npc.askNumber("#fc0xFF800080#〔消耗:#i"+樂豆鑽ID+"# 轉換為#b樂豆點數#fc0xFF800080#〕：\r\n#r〔請輸入兌換數量〕：\r\n",1,1,99999);
		if(player.hasItem(樂豆鑽ID,樂豆數量)) {
		player.modifyCashShopCurrency(1,樂豆數量*100);
		player.loseItem(樂豆鑽ID,樂豆數量);
		player.dropAlertNotice("兌換成功,獲得：〔"+樂豆數量*100+"〕點樂豆點數");
		player.dropMessage(8, "本次兌換消耗樂豆鑽："+樂豆數量+" 顆");
		}else{
		player.dropAlertNotice("兌換失敗,請注意兌換數量");
		}
        break;
	case 3: ///楓葉點數購買楓點鑽
        let 購買楓點鑽數量 = npc.askNumber("#fc0xFF800080#〔消耗#b楓葉點數#fc0xFF800080#,轉換為 #i4033079# #fc0xFF800080#〕：\r\n#r〔請輸入購買數量〕：\r\n",1,1,99999);
		if(1000 * 購買楓點鑽數量 > player.getPoint()){
		player.dropAlertNotice("楓葉點數不足");
		}else{
		player.modifyCashShopCurrency(2,-(1000*購買楓點鑽數量));
		player.gainItem(4033079,購買楓點鑽數量);
		player.dropAlertNotice("兌換成功\r\n共獲得：〔"+購買楓點鑽數量+"〕個楓點鑽");
		player.dropMessage(8, "本次購買消耗楓葉點數："+購買楓點鑽數量*1000+" 點");
		}
        break;
	case 4: ///楓點鑽轉換為楓點
        let 楓點鑽ID = 4033079;		
		let 楓點數量 = npc.askNumber("#fc0xFF800080#〔消耗:#i"+楓點鑽ID+"# 轉換為#b楓葉點數#fc0xFF800080#〕：\r\n#r〔請輸入兌換數量〕：\r\n",1,1,99999);
		if(player.hasItem(楓點鑽ID,楓點數量)) {
		player.modifyCashShopCurrency(2,楓點數量*1000);
		player.loseItem(楓點鑽ID,楓點數量);
		player.dropAlertNotice("兌換成功,獲得：〔"+楓點數量*1000+"〕點楓葉點數");
		player.dropMessage(8, "本次兌換消耗楓點鑽："+楓點數量+" 顆");
		}else{
		player.dropAlertNotice("兌換失敗,請注意兌換數量");
		}
        break;
}