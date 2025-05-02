import { pic } from "./pic";

var 箱子代碼 = 2633452;

var itemList = Array(
1212120,
		1213018,
		1214018,
		1222113,
		1232113,
		1242122,
		1252098,
		1262039,
		1272017,
		1282017,
		1292018,
		1302343,
		1312203,
		1322255,
		1332279,
		1362140,
		1372228,
		1382265,
		1402259,
		1403018,
		1412181,
		1422189,
		1432218,
		1442274,
		1452257,
		1462243,
		1472265,
		1482221,
		1492235,
		1522143,
		1532150,
		1542117,
		1552119,
		1582023,
		1592020

);
        var text = "";
		for(var i=0; i<itemList.length; i++) {
			text+="#L"+i+"##v"+itemList[i]+"##z"+itemList[i]+"##l\r\n";
		}
		var selection = npc.askMenu(""+pic.QA圖+"\r\n#b尊貴玩家你好,請選擇你要換取的武器：\r\n此為等級武器箱：\r\n#r"+text);
		var itemid = itemList[selection];
		var toDrop = player.makeItemWithId(itemid); // 武器
        toDrop.setStr(150); //裝備力量
		toDrop.setDex(150); //裝備敏捷
		toDrop.setInt(150); //裝備智力
		toDrop.setLuk(150); //裝備幸運
		toDrop.setPad(400);
		toDrop.setMad(400);
		toDrop.setDamR(10); //總傷害
		toDrop.setBossDamageR(30); //B傷
		toDrop.setIgnorePDR(20); //無視
		toDrop.setCHUC(10);  //星力值
		player.gainItem(toDrop);
		player.loseItem(箱子代碼, 1);
		
		npc.say("#fs15##b您已經成功兌換裝備：\r\n#r道具：#i"+itemid+"#");

