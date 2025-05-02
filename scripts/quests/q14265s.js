if (getEventValue("9點福利", false) < 1) {
//npc.ui(1).sayX("確認領取？？");
    setEventValue("9點30福利", 1)
    npc.completeQuest();
    // player.gainParcel(4033356, 20, "21點30福利")//正義火種
    // player.gainParcel(4002001, 2, "21點30福利")//藍蝸牛郵票
    // player.gainParcel(5062500, 50, "21點30福利")//大師附加神奇魔方
    // player.gainParcel(5062009, 50, "21點福利")//超級神奇魔方
    // player.gainParcel(2434630, 10, "21點福利")//3W點券
    // player.gainParcel(2434629, 10, "21點福利")//3W抵用
    // player.gainParcel(4000016, 20, "21點福利")//最高階物品結晶
    npc.broadcastPlayerNotice(15, "『全服福利』 : 玩家 【 " + player.getName() + " 】 成功領取每日9點30福利。 點選頭上信封，接受郵件領取。");
    npc.ui(1).sayX("成功領取每日9點30福利");
} else {
    npc.completeQuest();
    npc.sayX("你的賬號已經領取過了，無法再次領取。")
}


//flag = true 查總的不重置
//flag = false 查當天的
function getEventValue(event1, flag) {
    var charInfo = getCharInfo();
    if (flag) {
        var sql = "SELECT SUM(value) AS numbe FROM zz_xr_event WHERE accounts_id = " + parseInt(charInfo.accounts_id) + " and event = '" + event1 + "' and world =" + parseInt(charInfo.world) + " ";
    } else {
        var sql = "SELECT SUM(value) AS numbe FROM zz_xr_event WHERE accounts_id = " + parseInt(charInfo.accounts_id) + " and event = '" + event1 + "' and world =" + parseInt(charInfo.world) + " AND DATE_FORMAT(time, '%Y-%m-%d') = DATE_FORMAT(NOW(), '%Y-%m-%d') ";
    }


    var resultList = player.customSqlResult(sql);
    var count = 0;
    for (var i = 0; i < resultList.size(); i++) {
        var result = resultList.get(i);
        if (result == null) {
            break;
        }
        count = result.get("numbe");
        if (count == null) {
            count = 0;
        }
    }

    return parseInt(count);
}

function setEventValue(event1, value1) {
    var charInfo = getCharInfo();
    var sql = "INSERT INTO zz_xr_event(accounts_id, world, `event`,`value`,time) VALUES(" + charInfo.accounts_id + "," + charInfo.world + ",'" + event1 + "'," + value1 + ",now())";

    player.customSqlInsert(sql);
}

function getCharInfo() {
    var sql = "SELECT accountid accounts_id,world FROM characters WHERE id = " + player.getId() + "";
    var resultList = player.customSqlResult(sql);
    var charInfo = {};
    for (var i = 0; i < resultList.size(); i++) {
        var result = resultList.get(i);
        if (result == null) {
            break;
        }
        charInfo.accounts_id = result.get("accounts_id");
        charInfo.world = result.get("world");
    }
    return charInfo;
}