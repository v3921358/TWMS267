import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";



//var status = -1;
var eff = "#fEffect/BasicEff/MainNotice/BlockBuster/Default/3#"; //紅星
var yun9 = "#fEffect/ItemEff/1112811/0/0#";
var tz12 = "#fMap/MapHelper.img/weather/rose/0#"; ////美化2

var kkkbak1 = "#fMap/MapHelper.img/weather/rose/0#"; ////美化2
var bk1 = "#fEffect/ItemEff.img/1004532/effect/default/4#"
var bk2 = "#fEffect/ItemEff.img/1004475/effect/jump/0#"
var bk3 = "#fEffect/ItemEff.img/1004435/effect/alert/0#"
var bk4 = "#fEffect/ItemEff.img/1004477/effect/prone/1#"
var bk5 = "#fEffect/ItemEff.img/1004436/effect/default/3#"
var bk6 = "#fEffect/ItemEff.img/1004437/effect/walk1/0#"
var sg1 = "#fEffect/ItemEff.img/2022109/1#"
var sg2 = "#fEffect/ItemEff.img/2022109/2#"
var zx = "#fEffect/ItemEff.img/1003492/effect/default/1#"
var lj = "#fEffect/ItemEff.img/1003493/effect/default/1#"
var dq = "#fEffect/ItemEff.img/1003494/effect/default/1#"
var ye = "#fEffect/ItemEff.img/1003495/effect/default/0#"
var fg = "#fEffect/ItemEff.img/1004124/effect/ladder/1#"
var dtb = "#fEffect/CharacterEff.img/1112949/3/0#"
var xtb1 = "#fEffect/CharacterEff.img/1003249/0/0#" //第一排
var xtb2 = "#fEffect/CharacterEff.img/1003249/1/0#" //第二排
var xtb3 = "#fEffect/CharacterEff.img/1003252/0/0#" //第三排
var xtb4 = "#fEffect/CharacterEff.img/1112900/3/1#" //第四排
var zzz = "#fUI/UIWindowGL.img/FeedbackSystem/backgrnd3#"
var 黃星 = "#fEffect/CharacterEff/1112924/0/0#"
var icon = "#fUI/UIWindow2.img/QuestAlarm/BtQ/normal/0#"
var hii = "#fEffect/BasicEff/MainNotice/BlockBuster/Default/3#"
var a1 = "#fEffect/ItemEff/1070069/effect/walk1/0#"
var a2 = "#fEffect/ItemEff/1070069/effect/walk1/1#"
var a3 = "#fEffect/ItemEff/1070069/effect/walk1/2#"
var a4 = "#fEffect/ItemEff/1070069/effect/walk1/3#"
var a5 = "#fEffect/ItemEff/1070069/effect/walk1/4#"
var a6 = "#fEffect/ItemEff/1070069/effect/walk1/5#"
var a7 = "#fEffect/ItemEff/1070069/effect/walk1/6#"
var a8 = "#fEffect/ItemEff/1070069/effect/walk1/7#"
var 咖波 = "#fEffect/EventEffect/EffectAttach/3110008/0/walk/3#"
var 粉星 = "#fEffect/CharacterEff/1082229/0/0#";//粉星

//城鎮傳送
var maps = Array(
	Array(103000000, "墮落城市"),
	Array(100000000, "弓箭手村"),
	Array(101000000, "魔法森林"),

	Array(102000000, "勇士之村")

);
//練級地圖
var monstermaps = Array(
    Array(104010200,0,""+粉星+"砲台路-森林小徑          適合等級範圍:1-10"),
    Array(100040300,0,""+粉星+"弓箭手村-石巨人寺院3     適合等級範圍:10-40"),
    Array(103020420,0,""+粉星+"墮落城市-2號線3區段      適合等級範圍:40-80"),
    Array(260020610,0,""+粉星+"日落之路-沉睡沙漠        適合等級範圍:80-110"),
    Array(261010001,0,""+粉星+"蒙特鳩研究所-101號房     適合等級範圍:80-110"),
	Array(220030400,0,""+粉星+"玩具城-玩具工廠<B工程4>  適合等級範圍:110-130"),
	Array(240030101,0,""+粉星+"神木村-火焰森林          適合等級範圍:130-160"),
	Array(240040600,0,""+粉星+"神木村-主巢穴山峰        適合等級範圍:150-180"),
	Array(221030650,0,""+粉星+"UFO內部-走廊H02          適合等級範圍:170-210"),
	Array(273040100,0,""+粉星+"被遺棄的挖掘地區2        適合等級範圍:190-210"),
	Array(450001111,0,""+粉星+"無名村-岩石和火焰領土    適合等級範圍:210-230"),
	Array(450001216,0,""+粉星+"無名村-洞穴下側          適合等級範圍:210-230"),
	Array(450014300,0,""+粉星+"反轉城市-隱藏列車        適合等級範圍:210-230"),
	Array(450002010,0,""+粉星+"啾啾村-森林深處          適合等級範圍:220-240"),
	Array(450003300,0,""+粉星+"拉契爾恩-雞群遊樂場      適合等級範圍:230-250"),
	Array(450003420,0,""+粉星+"拉契爾恩-本色之處        適合等級範圍:230-250"),
	Array(450005430,0,""+粉星+"阿爾卡娜-下路            適合等級範圍:240-260"),
    Array(450006230,0,""+粉星+"魔菈斯-影子跳舞區        適合等級範圍:240-260"),
	Array(450007110,0,""+粉星+"艾斯佩拉-鏡光大海        適合等級範圍:250-260"),
	Array(450009330,0,""+粉星+"月之橋-虛空海浪          適合等級範圍:260-275"),
	Array(450011540,0,""+粉星+"苦痛迷宮-中心地帶4       適合等級範圍:260-275"),
	Array(450012340,0,""+粉星+"利曼-世界終結之處1-5     適合等級範圍:260-275"),
    Array(450012440,0,""+粉星+"利曼-世界終結之處2-5     適合等級範圍:260-275"),
    Array(450016010,0,""+粉星+"賽拉斯-光芒所及之處1     適合等級範圍:260-275"),
	Array(450016110,0,""+粉星+"賽拉斯-無限沉淪深海1     適合等級範圍:260-275"),
	Array(410003040,0,""+粉星+"AUT-支配的荒野           適合等級範圍:265-275"),
	Array(410003090,0,""+粉星+"AUT-汽車的戲院           適合等級範圍:265-275"),
	Array(410003200,0,""+粉星+"AUT-橫貫列車             適合等級範圍:265-275"),
    Array(410000520,0,""+粉星+"AUT-海邊岩石地帶         適合等級範圍:265-275"),
	Array(410000590,0,""+粉星+"AUT-西方城牆             適合等級範圍:265-275"),
	Array(410000700,0,""+粉星+"AUT-王立圖書館           適合等級範圍:265-275"),
	Array(410000840,0,""+粉星+"AUT-燃燒王立圖書         適合等級範圍:265-275"),
	Array(410007002,0,""+粉星+"AUT-通往城門的路         適合等級範圍:275-300"),
	Array(410007006,0,""+粉星+"AUT-被佔領的巷道         適合等級範圍:275-300"),
	Array(410007012,0,""+粉星+"AUT-陽光灑落的實驗室     適合等級範圍:275-300"),
	Array(410007015,0,""+粉星+"AUT-大門深鎖的實驗室     適合等級範圍:275-300\r\n")
);

var txt = pic.粗楷體+"\r\n";
txt += "#L0#" + 咖波 + " #b武陵道場 #l\r\n";
txt += "#L1#" + 咖波 + " #b自由市場 #l\r\n";
txt += "#L2#" + 咖波 + " #b城鎮傳送 #l\r\n";
txt += "#L3#" + 咖波 + " #b魔王傳送 #l\r\n";
txt += "#L4#" + 咖波 + " #b練功傳送 #l\r\n";
txt += "#L5#" + 咖波 + " #b創建公會 #l#k\t\r\n";

txt += "  ";
let selection = npc.askMenu(txt);
switch (selection) {
	case 0:
		player.changeMap(925020001);
		break;
	case 1:
		player.changeMap(910000000);
		break;
	case 2:
		player.runScript("citywarp")
		break;
	case 3:
		player.runScript("bosswarp")
		break;
	case 4:
		player.runScript("monsterwarp")
		break;
	case 5:
		player.changeMap(200000301);
		break;

}
