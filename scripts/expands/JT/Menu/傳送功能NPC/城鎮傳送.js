



var isopenvip = false;
var icon = "#fEffect/CharacterEff/1082565/0/0#";
var icon1 = "#fUI/Gateway/WorldSelect/icon/0#";
var q = "#fUI/Basic/LevelNo/0#"; //[1]+1
var w = "#fUI/Basic/LevelNo/1#"; //[1]+1
var e = "#fUI/Basic/LevelNo/2#"; //[1]+1
var e1 = "#fUI/UIWindowBT/CharacterUI/CharacterInfo/BtStat/normal/0# ";
var e2 = "#fUI/UIWindow/Quest/icon8/0#";
var r = "#fUI/Basic/LevelNo/3#"; //[1]+1
var t = "#fUI/Basic/LevelNo/4#"; //[1]+1
var eff2 = "#fEffect/BasicEff/BornEmblem/0#";  //0
var ct13 = "#fEffect/BasicEff/BornEmblem/1#";  //1
var ct14 = "#fEffect/BasicEff/BornEmblem/2#";  //2
var ct15 = "#fEffect/BasicEff/BornEmblem/3#";  //3
var ct16 = "#fEffect/BasicEff/BornEmblem/4#";  //4
var ct17 = "#fEffect/BasicEff/BornEmblem/5#";  //5
var ct18 = "#fEffect/BasicEff/BornEmblem/6#";  //6
var ct19 = "#fEffect/BasicEff/BornEmblem/7#";  //7
var ct20 = "#fEffect/BasicEff/BornEmblem/8#";  //8
var ct21 = "#fEffect/BasicEff/BornEmblem/9#";  //9
var ct22 = "#fEffect/BasicEff/BornEmblem/10#";  //10
let ttt = "#fUI/UIWindow/Quest/icon2/7#";//"+ttt+"//美化1
let ttt2 = "#fUI/UIWindow/Quest/icon6/7#";////美化2
let ail = "#fEffect/BasicEff/MainNotice/Gamsper/Notify/4#"
let ail1 = "#fUI/GuildMark/Mark/Etc/00009001/1#"
let ail2 = "#fUI/NameTag/188/e#"
let ail3 = "#fUI/NameTag/188/c#"
let ail4 = "#fUI/NameTag/188/w#"
var eff3 = "#fEffect/CharacterEff/1112924/0/0#";
var eff4 = "#fEffect/CharacterEff/1112925/0/0#";//小紅心
let cat1 = "#fItem/Pet/5000000.img/alert/0#";
let cat2 = "#fItem/Pet/5000000.img/fly/0#";
let cat3 = "#fItem/Pet/5000000.img/cry/0#";
let tu1 = "#fItem/Cash/0501.img/05010045/effect/stand1/1#"
let tu2 = "#fItem/Cash/0501.img/05010002/effect/default/7#"
let tu3 = "#fUI/NameTag/medal/758/w#"
let tu4 = "#fEffect/SetEff.img/245/effect/28#"
let tu5 = "#fUI/NameTag/medal/758/e#"
let tu6 = "#fUI/NameTag/medal/758/c#"
let fy = "#fUI/RunnerGame.img/RunnerGameUI/UI/Point/1#";
let fy1 = "#fUI/RunnerGame.img/RunnerGameUI/UI/Point/2#";
let dz = "#fUI/GuildMark/Mark/Etc/00009020/1#";
let dz1 = "#fEffect/CharacterEff/1003393/1/0#";
let star = "#fEffect/CharacterEff/1051294/1/1#";
 var eff1 = "#fEffect/CharacterEff/1112924/0/0#";
var cl2 = "#fMap/MapHelper.img/weather/knitsWithWarmWinter/10#"; //黃色楓葉


var townmaps = Array(
	//Array(749050400, "#e轉蛋屋"),
	Array(101000000, "#e魔法森林"),
	Array(104000000, "維多利亞港"),
	Array(103000000, "墮落城市"),
	Array(102000000, "勇士之村"),
	Array(100000000, "弓箭手村"),
	Array(800000000, "菇菇神社"),
	Array(200000000, "天空之城"),
	Array(910001000, "梅斯特鎮"),
	Array(120000000, "鯨魚號碼頭"),
	Array(105000000, "奇幻村"),
	Array(220000000, "玩具城"),
	//Array(600000000, "新葉都市\r\n"),
	Array(211000000, "冰原雪域"),
	Array(251000000, "靈藥幻境"),
	Array(261000000, "瑪迦提亞城"),
	Array(230000000, "水之都"),
	Array(300000000, "艾靈森林"),
	Array(240000000, "神木村"),
	Array(260000000, "納希綠洲城"),
	Array(250000000, "桃花仙境"),
	Array(130000000, "耶雷弗")



);

var a = 0;
var selects = 0;
var MapType = -1;


var GC1 = "#fEffect/CharacterEff.img/1082700/2/0#";
var SW1 = "#fEffect/CharacterEff.img/1112960/0/0#";
var SW2 = "#fEffect/CharacterEff.img/1112960/0/1#";
var SW3 = "#fEffect/CharacterEff.img/1112960/0/2#";
var SW4 = "#fEffect/CharacterEff.img/1112960/0/3#";

var XC01 = "#fUI/NameTag.img/292/w#";
var XC02 = "#fUI/NameTag.img/292/c#";
var XC03 = "#fUI/NameTag.img/292/e#";


//selection=npc.ui(2).me().next().askMenuX(selStr)
selection = 0
//let Sel = npc.me().next().askMenuX(selStr);
var text = "#e請選擇你要接連的地方：\r\n#d" 
		for (var i = 0; i < townmaps.length; i++) {
			text += "#L" + i + "##k" + townmaps[i][1] + '#l\r\n';
		}
		MapType = 0 
if (MapType != -1) {
	selection = npc.askMenu(text);

	selects = selection;
	selection = npc.me().next().askYesNoX("在這裡的事情辦完了嗎？確定要去你想要去的地方了嗎？");

	if (selection) {
 
  
						player.changeMap(townmaps[selects][0], 0); 
 
	}
}

