
/* Joyce
	Event NPC
*/

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
//跳跳地圖
var tiaotiaomaps = Array(
	Array(100000202, 0, "#r寵物公園                    「射手跳跳」#b"),
	Array(220000006, 0, "#r寵物訓練                    「玩具跳跳」#b"),
	Array(910360002, 0, "第一區域                    「 地鐵b1」"),
	Array(910360102, 0, "第一區域                    「 地鐵b2」"),
	Array(910360203, 0, "第一區域                    「 地鐵b3」"),
	Array(109040001, 0, " 向高地                      <第1階段> "),
	Array(910130000, 0, "忍苦樹林                     <第1階段> "),
	Array(109030001, 0, "上樓上樓                     <第1階段> "),
	Array(280020000, 0, "火山心臟                     <火山的心臟>")
);
var selectionectedMap = -1;
var selectionectedArea = -1;

var txt = "";
txt += "#L8#" + 咖波 + " #r[ 自由市場 ]#l\r\n";
txt += "#L0#" + 咖波 + " #b城鎮傳送 [ #r各大村莊#b ]#l\r\n";
txt += "#L7#" + 咖波 + " #b魔王傳送#l\r\n";
txt += "#L1#" + 咖波 + " #b練功傳送 [ #r有分級地圖#b ]#l\r\n";
//txt += "#L7#" + 咖波 + " #b結婚花園New [ #r舉辦婚禮之處#b ]#l#k\r\n";
txt += "#L2#" + 咖波 + " #b創建公會 [ #r公會本部 < 英雄之殿 >#b ] #l#k\t\r\n";
//txt += "#L5#" + 咖波 + " #b武陵道場 [ #r武公爺爺的地盤#b ]#l#k\t\r\n";
//txt += "#L10#" + 咖波 + " #b組隊副本 [ #r冰凍星球/副本匯集地#b ]#l#k\t\r\n";
//txt += "#L9#" + 咖波 + " #b組隊副本 [ #r龍騎士/天空的渡口#b ]#l#k\t\r\n";

txt += "  ";
let selection = npc.askMenu(txt);

var selectionStr = "#b請選擇你想去的地方: #b\r\n";


if(selection == 0 || selection == 1 || selection == 6) {

	if(selection == 0) {
		var j = 2;
		for(var i = 0; i < maps.length; i++) {

			selectionStr += "#L" + i + "#" + tz12 + maps[i][1] + " #l";

			if(i == j || i == j + 3) {
				selectionStr += "\r\n";
				j += 3;
			} else {
				selectionStr += "";
			}

		}
	} else if(selection == 1) {
		//練級地圖
		for(var i = 0; i < monstermaps.length; i++) {
			selectionStr += "#L" + i + "#" + yun9 + monstermaps[i][2] + "#l\r\n";

		}
	} else if(selection == 6) {
		selectionStr += "#L1001# #v4001839# ,[#g刷星星#k] - #m701103030##l\r\n";	
		selectionStr += "#L1002# #v4036313# ,[#g刷金幣地圖#k] - #l\r\n\r\n";
		selectionStr += "#L1003# #v4001006# ,[#r刷火焰羽毛#k] -   #m410000041##l\r\n";
//		selectionStr += "#L1004# #v4034999# ,[#r刷黑色羽毛#k] - #m410000040##l\r\n";
	}

	selectionectedArea = selection;

	selection = npc.askMenuS(selectionStr);

	if(selection == 1000 || selection == 1001 || selection == 1002 || selection == 1003 || selection == 1004) {
		selectionectedArea = selection;
	}
	selectionectedMap = selection;
	if(selection==1002){
		var text ="   ------------#r刷金幣專區#k-------------\r\n";
		text += "#L0# #v4036313# ,[#r快速刷金幣地圖#k] - #m410000123##l\r\n";
		text += "#L1# #v4036313# ,[#b高級刷金幣地圖,需要一定實力#k] - #m450012440##l\r\n\r\n";
		let sel=npc.askMenu(text);
		if(sel==0){
			player.changeMap(410000123);
		}else{
			player.changeMap(450012440);
		}
		//萌新刷金幣 402000509
		

		//大神刷金幣 
	}else{
		let yes = npc.askYesNoS("看來這裡的事情都已經處理完了啊。您真的要移動嗎？" );
		if(yes > 0) {
			if(selectionectedMap >= 0) {
				if(selectionectedArea == 0) {

					player.changeMap(maps[selectionectedMap][0]);
				} else if(selectionectedArea == 3) {

					player.changeMap(tiaotiaomaps[selectionectedMap][0]);
				} else if(selectionectedArea == 1000) {

					player.changeMap(450002302, 0);
				} else if(selectionectedArea == 1001) {

					player.changeMap(701103030, 0);
				} else if(selectionectedArea == 1002) {
					
					


					

					
				} else if(selectionectedArea == 1003) {

					player.changeMap(410000041, 0);
				} else if(selectionectedArea == 1004) {

					player.changeMap(410000040, 0);

				} else {
					
						player.changeMap(monstermaps[selectionectedMap][0], 0);

					

				}
			}

		}
	}
	
} else {
	if(selection == 2) {
		player.changeMap(200000301, 0);

	} else if(selection == 3) {

		player.changeMap(100000000);

	} else if(selection == 4) {
		player.changeMap(925020000);
	} else if(selection == 5) {
		player.changeMap(925020001);
		player.dropMessage(-2, "您已經抵達武陵道場");
	} else if(selection == 7) {
		player.runScript("expands/bosswarp");
	} else if(selection == 8) {
		player.changeMap(910000000);
		player.dropMessage(-2, "您已經抵達自由市場");
	}
}
