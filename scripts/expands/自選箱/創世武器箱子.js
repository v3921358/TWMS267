import { pic } from "./pic";

var 箱子代碼 = 2630115;

var itemList = Array(
1212129,
		1213022,
		1214022,
		1222122,
		1232122,
		1242141,
		1252106,
		1262051,
		1272040,
		1282040,
		1292022,
		1302355,
		1312213,
		1322264,
		1332289,
		1362149,
		1372237,
		1382274,
		1402268,
		1403022,
		1412189,
		1422197,
		1432227,
		1442285,
		1452266,
		1462252,
		1472275,
		1482232,
		1492245,
		1522152,
		1532157,
		1542128,
		1552130,
		1562010,
		1572010,
		1582044,
		1592022

);
        var text = "";
		for(var i=0; i<itemList.length; i++) {
			text+="#L"+i+"##v"+itemList[i]+"##z"+itemList[i]+"##l\r\n";
		}
		var selection = npc.askMenu(""+pic.QA圖+"\r\n#b尊貴玩家你好,請選擇你要換取的武器：\r\n此為等級武器箱：\r\n#r"+text);
		var itemid = itemList[selection];
		var toDrop = player.makeItemWithId(itemid); // 武器
        toDrop.setStr(200); //裝備力量
		toDrop.setDex(200); //裝備敏捷
		toDrop.setInt(200); //裝備智力
		toDrop.setLuk(200); //裝備幸運
		toDrop.setPad(550);
		toDrop.setMad(550);
		toDrop.setDamR(10); //總傷害
		toDrop.setBossDamageR(30); //B傷
		toDrop.setIgnorePDR(20); //無視
		toDrop.setCHUC(10);  //星力值
		player.gainItem(toDrop);
		player.loseItem(箱子代碼, 1);
		
		npc.say("#fs15##b您已經成功兌換裝備：\r\n#r道具：#i"+itemid+"#");

