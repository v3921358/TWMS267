var ttt6 = "#fUI/UIWindow.img/PvP/Scroll/enabled/next2#";
var tz12 = "#fUI/GuildMark/Mark/Etc/00009020/8#"; //紅星

var ttt = "#fUI/UIWindow/Quest/icon2/7#"; //"+ttt+"//美化1
var ttt2 = "#fUI/UIWindow/Quest/icon6/7#"; ////美化2
var ttt3 = "#fUI/UIWindow/Quest/icon3/6#"; //"+ttt3+"//美化圓
var ttt4 = "#fUI/UIWindow/Quest/icon5/1#"; //"+ttt4+"//美化New
var ttt5 = "#fUI/UIWindow/Quest/icon0#"; ////美化!
var ttt7 = "#fUI/Basic/BtHide3/mouseOver/0#"; //"+ttt6+"//美化會員
var wi14 = "#fEffect/CharacterEff/1051294/1/4#"; //黃色星星
var ttt6 = "#fUI/UIWindowBT.img/WorldMap/BtQsearch/mouseOver/0#";

var 殘暴炎魔 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/1/Icon/normal/0#";
var 培羅德 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/16/Icon/normal/0#";
var 天狗 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/0/Icon/normal/0#";
var 桃樂絲 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/106/Icon/normal/0#";
var 梅格奈斯 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/10/Icon/normal/0#";
var 拉圖斯 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/22/Icon/normal/0#";
var 希拉 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/3/Icon/normal/0#";
var 庫洛斯 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/20/Icon/normal/0#";
var 卡翁 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/21/Icon/normal/0#";
var 森蘭丸 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/17/Icon/normal/0#";
var 世界樹四王 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/7/Icon/normal/0#";
var 闇黑龍王 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/2/Icon/normal/0#";
var 阿卡伊農 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/9/Icon/normal/0#";
var 皮卡啾 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/11/Icon/normal/0#";
var 希格諾斯 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/12/Icon/normal/0#";
var 史烏 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/13/Icon/normal/0#";
var 死靈戴米安 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/15/Icon/normal/0#";
var 露希妲 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/19/Icon/normal/0#";
var 威爾 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/23/Icon/normal/0#";
var 戴斯克 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/26/Icon/normal/0#";
var 真希拉 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/24/Icon/normal/0#";
var 頓凱爾 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/27/Icon/normal/0#";
var 黑魔法師 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/25/Icon/normal/0#";
var 賽蓮 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/28/Icon/normal/0#";
var 天使綠水靈 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/29/Icon/normal/0#";
var 卡洛斯 = "#fUI/UIWindow2.img/UserList/Main/Boss/BossList/30/Icon/normal/0#";

var pqName = "";

var mapList = Array(
	Array(殘暴炎魔, 211042300),
	Array(梅格奈斯, 401060000),
	Array(希拉, 262030000),
	Array(森蘭丸, 807300100),
	Array(世界樹四王, 105200000),
	Array(闇黑龍王, 240050400),
    Array(阿卡伊農, 272030300),
	Array(希格諾斯, 271040000),
	Array(史烏, 350060300),
	Array(死靈戴米安, 105300303),
	Array(露希妲, 450003600),
	Array(威爾, 450007240),
	Array(戴斯克, 450009301),
	Array(真希拉, 450011990),
	Array(頓凱爾, 450012200),
	Array(黑魔法師, 450012500),
	Array(賽蓮, 410000670),
	Array(天使綠水靈, 160000000),
	Array(卡洛斯, 410005005)
);

var selStr = "#d#e\t透過我能夠前往挑戰BOSS：\r\n\\r\n";
for(var i = 0; i < mapList.length; i++) {
	selStr += "#L" + i + "# #r#e#n#b" + mapList[i][0] + "#r#k#l#n";
}
selStr += "\r\n\r\n\r\n";
let selection = npc.askMenu(selStr);
player.changeMap(mapList[selection][1], 0); //傳送地圖
