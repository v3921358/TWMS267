/* global npc */
import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";

var  武器箱子 = 2630494;

var  itemId = 0;
var  itemList = Array(
1212066,
1222061,
1232060,
1242065,
1252064,
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
1542070,
1582041,
1222061, //黑色靈魂射手
1292020,
1262047, //黑色ESP
1403020, //黑色武拳
1213020, //黑色調節器
1592030, //黑色古代之弓
1214020, //黑色龍息射手
1552070 //黑色鐵扇(陰陽)


);

var  text = "";
for(var  i=0; i<itemList.length; i++) {
    text+="#L"+i+"##v"+itemList[i]+"##z"+itemList[i]+"##l\r\n";
}
let selection = npc.askMenu(""+pic.GM+"#d [ 100等級獎勵-黑色武器自由選擇 ]：\r\n"+color.櫻花紅紫+""+text);

itemId = itemList[selection];

let count = npc.minNum(1).maxNum(1).askNumber("#r請輸入道具數量為 : 1");

if( !player.getPlayer().haveItem(武器箱子,count) ){
    npc.say("#r所需道具不足！");
}else {
    player.gainItem(itemId, 1);
    player.loseItem(武器箱子, 1);
    npc.say("恭喜您，如願獲得了1個#b#z" + itemId + "#");
}
