import { pic } from "./pic";

var 箱子代碼 = 2631496;

var itemList = Array(
1162081,
1162080,
1162083,
1162082

);
        var text = "";
		for(var i=0; i<itemList.length; i++) {
			text+="#L"+i+"##v"+itemList[i]+"##z"+itemList[i]+"##l\r\n";
		}
		var selection = npc.askMenu(""+pic.QA圖+"\r\n#b尊貴玩家你好,請選擇你要換取的防具：\r\n此為等級防具箱：\r\n#r"+text);
		var itemid = itemList[selection];
		var toDrop = player.makeItemWithId(itemid); // 武器
        toDrop.setStr(50); //裝備力量
		toDrop.setDex(50); //裝備敏捷
		toDrop.setInt(50); //裝備智力
		toDrop.setLuk(50); //裝備幸運
		toDrop.setPad(50);
		toDrop.setMad(50);
		toDrop.setDamR(5); //總傷害
		toDrop.setBossDamageR(5); //B傷
		toDrop.setIgnorePDR(5); //無視
		toDrop.setCHUC(10);  //星力值
		player.gainItem(toDrop);
		player.loseItem(箱子代碼, 1);
		
		npc.say("#fs15##b您已經成功兌換裝備：\r\n#r道具：#i"+itemid+"#");

