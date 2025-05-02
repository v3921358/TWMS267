var status = -1;
var text;
var diff;
var sel;
var time;
var aaa = "#fEffect/CharacterEff/1112949/4/0#";

// 每個禮包所需的線上時長
var condition = new Array(120, 240, 360, 720);

var reward = new Array( // 禮包編號、道具id、數量
	// 120
	Array(1, 2632485, 1),   //一般防具素質附魔 x1

	// 240
	Array(2, 2632485, 1),   //一般武器素質附魔 x1

    // 360
	Array(3, 2632486, 1),   //一般武器素質附魔 x1

	// 240
	Array(4, 2632496, 1),  //贊助額度兌換券 [+30TWD] X1
);

var time = player.getOnlineTime();
var curlevel = -1;

text = "#e#d您今天線上時長為： #r" + time + "#k #d分鐘#n#k\r\n#e#d提示#n#k：#e#r23 ： 50#n #b至#r #e00 ： 10#n #b時無法領取線上獎勵。#k\r\n#b請在 #e#r23：50#n#b 分前領取當天未領取的獎勵。以免造成損失。服務中心內還有免費點券可以領哦~#k\r\n\r\n";
for(var i = 1; i <= condition.length; i++) {
	text += "#b#L" + i + "#" + aaa + " 領取線上" + condition[i - 1] + "分鐘獎勵";
	if(player.getPQLog("線上禮包" + i) > 0) {
		text += "(已領取)";
		curlevel = curlevel == -1 ? i : curlevel;
	}
	text += "#l\r\n";
}
text += "#k";
let selection = npc.askMenuS(text);
// 23:50 ~ 23: 59 前一天不領取的時間  00:00 ~ 00:10 第二天不領取的時間  
var date = new Date();
var hour=date.getHours();
var minute=date.getMinutes();
if((hour == 23 && (minute >= 50 && minute <= 59)) || (hour == 0 && (minute >= 0 && minute <= 10))) {
	npc.say("#d伺服器當前時間： #r" + hour + " 時 " + minute + " 分 #k\r\n\r\n#e#d提示#n#k：#r23 ： 50 #b至#r 00 ： 10 #b時無法領取線上獎勵。#k");
} else if(player.getPQLog("線上禮包" + selection) >0) {
	npc.say("這個禮包您已經領取過了");
} else {

	text = "\t\t\t\t#e#r- 線上 " + condition[selection - 1] + " 分鐘獎勵 -#k#n\r\n\r\n";
	for(var i = 0; i < reward.length; i++) {
		if(reward[i][0] == selection) {
			text += "\t\t\t#i" + reward[i][1] + "# #z" + reward[i][1] + "#[" + reward[i][2] + "個]\r\n";
		}
	}
	let sel = npc.askYesNo(text);
	if(sel == 1) {
		var rewardlist = Array();
		var nums = 0;
		for(var i = 0; i < reward.length; i++) {
			if(reward[i][0] == selection) {
				rewardlist.push(new Array(reward[i][1], reward[i][2]));
				nums ++;
			}
		}
		if(time < condition[selection - 1]) {
			npc.say("線上時間不足，無法領取。");
			player.runScript("zxjl");

		} else if(!player.getFreeSlots(1) > nums) {
			npc.say("包裹空間不足，請確保包裹每個欄位有至少 " + nums + " 格空間");

		} else {
			
			player.addPQLog("線上禮包" + selection, 1, 1);
			for(var i = 0; i < rewardlist.length; i++) {
				if(rewardlist[i][0] == -1 || rewardlist[i][0] == -2) {
					//點券或抵用
					player.modifyCashShopCurrency(-rewardlist[i][0], rewardlist[i][1]);
				} else if (rewardlist[i][0] == 0) {
					player.gainMesos(rewardlist[i][1]);
				} else {
					//無期限道具
					player.gainItem(rewardlist[i][0], rewardlist[i][1]);
				}
			}
			npc.broadcastNotice("『線上時間獎勵』" + " : " + "玩家 " + player.getName() + " 領取了線上獎勵。");
			npc.say("領取成功！");
			player.runScript("福利中心/線上獎勵");

		}

	}

}

function getEventCount(eventName) {

	var sql = "select value,time from accounts_event where accounts_id = ? and event =? ";

	var result = player.customSqlResult(sql, player.getAccountId(), eventName);

	if(result.size() > 0) {
		if(result.get(0) != null) {
			return result.get(0).get("value");
		}
	} else {
		var sql = "insert into  accounts_event (accounts_id,world,event,type,value) values(?,?,?,?,?)";

		var result = player.customSqlInsert(sql, player.getAccountId(), player.getWorld(), eventName, 0, 0);
		return 0;
	}

}

function setEventCount(eventName, type, value) {

	var sql = "update accounts_event set type=?,value=value+? where accounts_id=? and event=?";

	var result = player.customSqlUpdate(sql, type, value, player.getAccountId(), eventName);

}