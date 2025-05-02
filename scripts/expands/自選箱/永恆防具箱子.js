import { pic } from "./pic";

var 箱子代碼 = 2630114;

var itemList = Array(
1005980,
1042433,
1062285,
1005981,
1042434,
1062286,
1005982,
1042435,
1062287,
1005983,
1042436,
1062288,
1005984,
1042437,
1062289

);
        var text = "";
		for(var i=0; i<itemList.length; i++) {
			text+="#L"+i+"##v"+itemList[i]+"##z"+itemList[i]+"##l\r\n";
		}
		var selection = npc.askMenu(""+pic.QA圖+"\r\n#b尊貴玩家你好,請選擇你要換取的防具：\r\n此為等級防具箱：\r\n#r"+text);
		var itemid = itemList[selection];
		var toDrop = player.makeItemWithId(itemid); // 武器
        toDrop.setStr(100); //裝備力量
		toDrop.setDex(100); //裝備敏捷
		toDrop.setInt(100); //裝備智力
		toDrop.setLuk(100); //裝備幸運
		toDrop.setPad(50);
		toDrop.setMad(50);
		toDrop.setDamR(5); //總傷害
		toDrop.setBossDamageR(5); //B傷
		toDrop.setIgnorePDR(5); //無視
		toDrop.setCHUC(10);  //星力值
		player.gainItem(toDrop);
		player.loseItem(箱子代碼, 1);
		
		npc.say("#fs15##b您已經成功兌換裝備：\r\n#r道具：#i"+itemid+"#");

