import { pic } from "./pic";

var 箱子代碼 = 2633914;

var itemList = Array(
1004808,
1053063,
1073158,
1082695,
1102940,
1152196,
1004809,
1053064,
1073159,
1082696,
1152197,
1102941,
1004810,
1053065,
1073160,
1082697,
1102942,
1152198,
1004811,
1053066,
1073161,
1082698,
1102943,
1152199,
1004812,
1053067,
1073162,
1082699,
1102944,
1152200

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

