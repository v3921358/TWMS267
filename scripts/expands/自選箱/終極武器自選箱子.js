import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";

var 箱子代碼 = 2632949;

var itemList = Array(
1212066,
1222061,
1232060,
1242065,
1272019,
1282036,
1302277,
1312155,
1322205,
1332227,
1362092,
1372179,
1382211,
1402199,
1412137,
1422142,
1432169,
1442225,
1452207,
1462195,
1472216,
1482170,
1492181,
1522096,
1532100,
1582041,
1252064,
1542070,
1552070,
1403020,
1592030,
1214020,
1213020

);
        var text = "";
		for(var i=0; i<itemList.length; i++) {
			text+="#L"+i+"##v"+itemList[i]+"##z"+itemList[i]+"##l\r\n";
		}
		var selection = npc.askMenu(""+pic.QA圖+"\r\n#b尊貴玩家你好,請選擇你要換取的武器：\r\n此為１００等級武器箱：\r\n#r"+text);
		var itemid = itemList[selection];
        var toDrop = sh.itemEquip(itemid).copy(); // 生成一個Eq
        toDrop.setStr(50); //裝備力量
		toDrop.setDex(50); //裝備敏捷
		toDrop.setInt(50); //裝備智力
		toDrop.setLuk(50); //裝備幸運
		toDrop.setTotalDamage(5); //總傷害
		toDrop.setBossDamage(5); //B傷
		toDrop.setIgnorePDR(5); //無視
		toDrop.setEnhance(10);  //星力值
		player.gainItem(toDrop);
		player.loseItem(箱子代碼, 1);
		
		npc.say("#fs15##b您已經成功兌換裝備：\r\n#r道具：#i"+itemid+"#");

