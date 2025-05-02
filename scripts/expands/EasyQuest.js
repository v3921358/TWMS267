import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";

var text =pic.楷體 + pic.愛心氣泡+color.咖啡+"#e一鍵完成任務選單:\r\n\r\n";
text+="#b#L1#"+pic.金右+"[#r來了解回家卷軸吧#b]#l\r\n";
text+="#b#L18#"+pic.金右+"[#r我的小屋#b]#l\r\n";
text+="#b#L19#"+pic.金右+"[#r口袋道具任務#b]#l\r\n";
text+="#b#L2#"+pic.金右+"[#r打敗迷你龍#b]#l\r\n";
text+="#b#L3#"+pic.金右+"[#r打敗黃金翼龍#b]#l\r\n";
text+="#b#L4#"+pic.金右+"[#r消逝的旅途#b]#l\r\n";
text+="#b#L5#"+pic.金右+"[#r反轉城市#b]#l\r\n";
text+="#b#L6#"+pic.金右+"[#r啾啾艾爾蘭#b]#l\r\n";
text+="#b#L7#"+pic.金右+"[#r嚼嚼艾爾蘭#b]#l\r\n";
text+="#b#L8#"+pic.金右+"[#r拉契爾恩#b]#l\r\n";
text+="#b#L9#"+pic.金右+"[#r阿爾卡娜#b]#l\r\n";
text+="#b#L11#"+pic.金右+"[#r記憶沼澤-魔菈斯#b]#l\r\n";
text+="#b#L10#"+pic.金右+"[#r初始之海-艾斯佩拉#b]#l\r\n";
text+="#b#L12#"+pic.金右+"[#r利曼-泰涅布利斯#b]#l\r\n";
text+="#b#L13#"+pic.金右+"[#r星星墜落的深海-賽拉斯#b]#l\r\n";
text+="#b#L14#"+pic.金右+"[#r賽爾尼溫#b]#l\r\n";
text+="#b#L15#"+pic.金右+"[#r阿爾克斯#b]#l\r\n";
text+="#b#L16#"+pic.金右+"[#r另一種力量．真實力量#b]#l\r\n";
var selection = npc.noEsc().askMenu(text);
switch (selection) {
	case 1: ///來了解回家卷軸吧
		if (player.getPQLog("來了解回家卷軸吧") <= 0) {
			player.completeQuest(16880, 0);
			player.dropMessage(-2, "恭喜完成快來了解回家卷軸任務");
			npc.say("#fs12##b你完成了任務：\r\n任務名: - #r快來了解回家卷軸任務 #b-");
			player.addPQLog("來了解回家卷軸吧");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 2: ///聯盟戰地-[#r打敗迷你龍#b]
		if (player.getPQLog("打敗迷你龍") <= 0) {
			player.completeQuest(16011, 0);
			player.dropMessage(-2, "恭喜完成打敗迷你龍任務");
			npc.say("#fs12##b你完成了任務：\r\n任務名: - #r打敗迷你龍任務 #b-");
			player.addPQLog("打敗迷你龍");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 3: ///聯盟戰地-[#r打敗黃金翼龍#b]
		if (player.getPQLog("打敗黃金翼龍") <= 0) {
			player.completeQuest(16012, 0);
			player.dropMessage(-2, "恭喜完成打敗黃金翼龍任務");
			player.addPQLog("打敗黃金翼龍");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 4: ///ARC地區-[#r消逝的旅途#b]
		if (player.getPQLog("消逝的旅途") <= 0) {
			player.completeQuest(16012, 0);
			player.completeQuest(34100, 0);
			player.completeQuest(34100, 0);
			player.completeQuest(34101, 0);
			player.completeQuest(34102, 0);
			player.completeQuest(34103, 0);
			player.completeQuest(34104, 0);
			player.completeQuest(34105, 0);
			player.completeQuest(34106, 0);
			player.completeQuest(34107, 0);
			player.completeQuest(34108, 0);
			player.completeQuest(34109, 0);
			player.completeQuest(34110, 0);
			player.completeQuest(34111, 0);
			player.completeQuest(34112, 0);
			player.completeQuest(34113, 0);
			player.completeQuest(34114, 0);
			player.completeQuest(34115, 0);
			player.completeQuest(34116, 0);
			player.completeQuest(34117, 0);
			player.completeQuest(34118, 0);
			player.completeQuest(34119, 0);
			player.completeQuest(34120, 0);
			player.dropMessage(-2, "恭喜完成消逝的旅途地區章節任務");
			player.addPQLog("消逝的旅途");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 5: ///ARC地區-[#r反轉城市地區章節#b]
		if (player.getPQLog("反轉城市地區章節") <= 0) {
			player.completeQuest(37601, 0);
			player.completeQuest(37602, 0);
			player.completeQuest(37603, 0);
			player.completeQuest(37604, 0);
			player.completeQuest(37605, 0);
			player.completeQuest(37606, 0);
			player.completeQuest(37607, 0);
			player.completeQuest(37608, 0);
			player.completeQuest(37609, 0);
			player.completeQuest(37610, 0);
			player.completeQuest(37611, 0);
			player.completeQuest(37612, 0);
			player.completeQuest(37613, 0);
			player.completeQuest(37614, 0);
			player.completeQuest(37615, 0);
			player.completeQuest(37616, 0);
			player.completeQuest(37617, 0);
			player.completeQuest(37618, 0);
			player.completeQuest(37619, 0);
			player.completeQuest(37620, 0);
			player.dropMessage(-2, "恭喜完成反轉城市地區章節任務");
			player.addPQLog("反轉城市地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 6: ///ARC地區-[#r啾啾艾爾蘭地區章節#b]
		if (player.getPQLog("啾啾艾爾蘭地區章節") <= 0) {
			player.completeQuest(34200, 0);
			player.completeQuest(34201, 0);
			player.completeQuest(34202, 0);
			player.completeQuest(34203, 0);
			player.completeQuest(34204, 0);
			player.completeQuest(34205, 0);
			player.completeQuest(34206, 0);
			player.completeQuest(34207, 0);
			player.completeQuest(34208, 0);
			player.completeQuest(34209, 0);
			player.completeQuest(34210, 0);
			player.completeQuest(34211, 0);
			player.completeQuest(34212, 0);
			player.completeQuest(34213, 0);
			player.completeQuest(34214, 0);
			player.completeQuest(34215, 0);
			player.completeQuest(34216, 0);
			player.completeQuest(34217, 0);
			player.completeQuest(34218, 0);
			player.dropMessage(-2, "恭喜完成啾啾艾爾蘭地區章節任務");
			player.addPQLog("啾啾艾爾蘭地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 7: ///ARC地區-[#r嚼嚼艾爾蘭地區章節#b]
		if (player.getPQLog("嚼嚼艾爾蘭地區章節") <= 0) {
			player.completeQuest(37701, 0);
			player.completeQuest(37702, 0);
			player.completeQuest(37703, 0);
			player.completeQuest(37704, 0);
			player.completeQuest(37705, 0);
			player.completeQuest(37706, 0);
			player.completeQuest(37707, 0);
			player.completeQuest(37708, 0);
			player.completeQuest(37709, 0);
			player.completeQuest(37710, 0);
			player.completeQuest(37711, 0);
			player.completeQuest(37712, 0);
			player.completeQuest(37713, 0);
			player.completeQuest(37714, 0);
			player.completeQuest(37715, 0);
			player.completeQuest(37716, 0);
			player.completeQuest(37717, 0);
			player.completeQuest(37718, 0);
			player.completeQuest(37719, 0);
			player.completeQuest(37720, 0);
			player.completeQuest(37721, 0);
			player.completeQuest(37722, 0);
			player.completeQuest(37723, 0);
			player.completeQuest(37724, 0);
			player.completeQuest(37725, 0);
			player.completeQuest(37726, 0);
			player.dropMessage(-2, "恭喜完成嚼嚼艾爾蘭地區章節任務");
			player.addPQLog("嚼嚼艾爾蘭地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 8: ///ARC地區-[#r夢都-拉契爾恩地區章節#b]
		if (player.getPQLog("拉契爾恩地區章節") <= 0) {
			player.completeQuest(34300, 0);
			player.completeQuest(34301, 0);
			player.completeQuest(34302, 0);
			player.completeQuest(34303, 0);
			player.completeQuest(34304, 0);
			player.completeQuest(34305, 0);
			player.completeQuest(34306, 0);
			player.completeQuest(34307, 0);
			player.completeQuest(34308, 0);
			player.completeQuest(34309, 0);
			player.completeQuest(34310, 0);
			player.completeQuest(34311, 0);
			player.completeQuest(34312, 0);
			player.completeQuest(34313, 0);
			player.completeQuest(34314, 0);
			player.completeQuest(34315, 0);
			player.completeQuest(34316, 0);
			player.completeQuest(34317, 0);
			player.completeQuest(34318, 0);
			player.completeQuest(34319, 0);
			player.completeQuest(34320, 0);
			player.completeQuest(34321, 0);
			player.completeQuest(34322, 0);
			player.completeQuest(34323, 0);
			player.completeQuest(34324, 0);
			player.completeQuest(34325, 0);
			player.completeQuest(34326, 0);
			player.completeQuest(34327, 0);
			player.completeQuest(34328, 0);
			player.completeQuest(34329, 0);
			player.completeQuest(34330, 0);
			player.completeQuest(34331, 0);
			player.completeQuest(34332, 0);
			player.completeQuest(34333, 0);
			player.completeQuest(34334, 0);
			player.completeQuest(34336, 0);
			player.dropMessage(-2, "恭喜完成拉契爾恩地區章節任務");
			player.addPQLog("拉契爾恩地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 9: ///ARC地區-[#r精靈之森-阿爾卡娜地區章節#b]
		if (player.getPQLog("阿爾卡娜地區章節") <= 0) {
			player.completeQuest(34450, 0);
			player.completeQuest(34451, 0);
			player.completeQuest(34452, 0);
			player.completeQuest(34453, 0);
			player.completeQuest(34454, 0);
			player.completeQuest(34455, 0);
			player.completeQuest(34456, 0);
			player.completeQuest(34457, 0);
			player.completeQuest(34458, 0);
			player.completeQuest(34459, 0);
			player.completeQuest(34460, 0);
			player.completeQuest(34461, 0);
			player.completeQuest(34462, 0);
			player.completeQuest(34463, 0);
			player.completeQuest(34464, 0);
			player.completeQuest(34465, 0);
			player.completeQuest(34466, 0);
			player.completeQuest(34467, 0);
			player.completeQuest(34468, 0);
			player.completeQuest(34469, 0);
			player.completeQuest(34470, 0);
			player.completeQuest(34471, 0);
			player.completeQuest(34472, 0);
			player.completeQuest(34473, 0);
			player.completeQuest(34474, 0);
			player.completeQuest(34475, 0);
			player.completeQuest(34476, 0);
			player.completeQuest(34477, 0);
			player.completeQuest(34478, 0);
			player.dropMessage(-2, "恭喜完成阿爾卡娜地區章節任務");
			player.addPQLog("阿爾卡娜地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 10: ///ARC地區-[#r艾斯佩拉地區章節#b]
		if (player.getPQLog("艾斯佩拉地區章節") <= 0) {
			player.completeQuest(34269, 0);
			player.completeQuest(34561, 0);
			player.completeQuest(34562, 0);
			player.completeQuest(34563, 0);
			player.completeQuest(34564, 0);
			player.completeQuest(34565, 0);
			player.completeQuest(34566, 0);
			player.completeQuest(34567, 0);
			player.completeQuest(34568, 0);
			player.completeQuest(34569, 0);
			player.completeQuest(34570, 0);
			player.completeQuest(34571, 0);
			player.completeQuest(34572, 0);
			player.completeQuest(34573, 0);
			player.completeQuest(34574, 0);
			player.completeQuest(34575, 0);
			player.completeQuest(34576, 0);
			player.completeQuest(34577, 0);
			player.completeQuest(34578, 0);
			player.completeQuest(34579, 0);
			player.completeQuest(34580, 0);
			player.completeQuest(34581, 0);
			player.completeQuest(34582, 0);
			player.completeQuest(34583, 0);
			player.completeQuest(34584, 0);
			player.completeQuest(34585, 0);
			player.completeQuest(34586, 0);
			player.gainItem(2438412, 1);///鏡子世界核心
			player.dropMessage(-2, "恭喜完成艾斯佩拉地區章節任務");
			player.addPQLog("艾斯佩拉地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 11: ///ARC地區-[#r魔菈斯地區章節#b]
		if (player.getPQLog("魔菈斯地區章節") <= 0) {
			player.completeQuest(34230, 0);
			player.completeQuest(34231, 0);
			player.completeQuest(34232, 0);
			player.completeQuest(34233, 0);
			player.completeQuest(34234, 0);
			player.completeQuest(34235, 0);
			player.completeQuest(34236, 0);
			player.completeQuest(34237, 0);
			player.completeQuest(34238, 0);
			player.completeQuest(34239, 0);
			player.completeQuest(34240, 0);
			player.completeQuest(34241, 0);
			player.completeQuest(34242, 0);
			player.completeQuest(34243, 0);
			player.completeQuest(34244, 0);
			player.completeQuest(34245, 0);
			player.completeQuest(34246, 0);
			player.completeQuest(34249, 0);
			player.completeQuest(34250, 0);
			player.completeQuest(34251, 0);
			player.completeQuest(34252, 0);
			player.completeQuest(34253, 0);
			player.completeQuest(34254, 0);
			player.completeQuest(34255, 0);
			player.completeQuest(34256, 0);
			player.completeQuest(34257, 0);
			player.completeQuest(34258, 0);
			player.completeQuest(34259, 0);
			player.completeQuest(34260, 0);
			player.completeQuest(34261, 0);
			player.completeQuest(34262, 0);
			player.completeQuest(34263, 0);
			player.completeQuest(34264, 0);
			player.completeQuest(34265, 0);
			player.completeQuest(34266, 0);
			player.completeQuest(34267, 0);
			player.completeQuest(34268, 0);
			player.completeQuest(34269, 0);
			player.completeQuest(34272, 0);
			player.dropMessage(-2, "恭喜完成魔菈斯地區章節任務");
			player.addPQLog("魔菈斯地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 12: ///ARC地區-[#r泰涅布利斯地區章節#b]
		if (player.getPQLog("泰涅布利斯地區章節") <= 0) {
			player.completeQuest(35600, 0);
			player.completeQuest(35601, 0);
			player.completeQuest(35602, 0);
			player.completeQuest(35603, 0);
			player.completeQuest(35604, 0);
			player.completeQuest(35605, 0);
			player.completeQuest(35606, 0);
			player.completeQuest(35607, 0);
			player.completeQuest(35608, 0);
			player.completeQuest(35609, 0);
			player.completeQuest(35610, 0);
			player.completeQuest(35611, 0);
			player.completeQuest(35612, 0);
			player.completeQuest(35613, 0);
			player.completeQuest(35614, 0);
			player.completeQuest(35615, 0);
			player.completeQuest(35616, 0);
			player.completeQuest(35617, 0);
			player.completeQuest(35618, 0);
			player.completeQuest(35619, 0);
			player.completeQuest(35620, 0);
			player.completeQuest(35621, 0);
			player.completeQuest(35622, 0);
			player.completeQuest(35623, 0);
			player.completeQuest(35624, 0);
			player.completeQuest(35625, 0);
			player.completeQuest(35626, 0);
			player.completeQuest(35627, 0);
			player.completeQuest(35628, 0);
			player.completeQuest(35629, 0);
			player.completeQuest(35630, 0);
			player.completeQuest(35631, 0);
			player.completeQuest(35632, 0);
			player.completeQuest(35700, 0);
			player.completeQuest(35701, 0);
			player.completeQuest(35702, 0);
			player.completeQuest(35703, 0);
			player.completeQuest(35704, 0);
			player.completeQuest(35705, 0);
			player.completeQuest(35706, 0);
			player.completeQuest(35707, 0);
			player.completeQuest(35708, 0);
			player.completeQuest(35709, 0);
			player.completeQuest(35710, 0);
			player.completeQuest(35711, 0);
			player.completeQuest(35712, 0);
			player.completeQuest(35713, 0);
			player.completeQuest(35714, 0);
			player.completeQuest(35715, 0);
			player.completeQuest(35716, 0);
			player.completeQuest(35717, 0);
			player.completeQuest(35718, 0);
			player.completeQuest(35719, 0);
			player.completeQuest(35720, 0);
			player.completeQuest(35721, 0);
			player.completeQuest(35722, 0);
			player.completeQuest(35723, 0);
			player.completeQuest(35724, 0);
			player.completeQuest(35725, 0);
			player.completeQuest(35726, 0);
			player.completeQuest(35727, 0);
			player.completeQuest(35728, 0);
			player.completeQuest(35729, 0);
			player.completeQuest(35730, 0);
			player.completeQuest(35731, 0);
			player.completeQuest(35740, 0);
			player.completeQuest(35753, 0);
			player.completeQuest(35754, 0);
			player.completeQuest(35756, 0);
			player.completeQuest(35800, 0);
			player.completeQuest(35801, 0);
			player.completeQuest(35802, 0);
			player.completeQuest(35803, 0);
			player.completeQuest(35804, 0);
			player.completeQuest(35805, 0);
			player.completeQuest(35806, 0);
			player.completeQuest(35807, 0);
			player.completeQuest(35808, 0);
			player.completeQuest(35809, 0);
			player.completeQuest(35810, 0);
			player.completeQuest(35811, 0);
			player.completeQuest(35812, 0);
			player.completeQuest(35813, 0);
			player.completeQuest(35814, 0);
			player.completeQuest(35815, 0);
			player.completeQuest(35816, 0);
			player.completeQuest(35817, 0);
			player.dropMessage(-2, "恭喜完成泰涅布利斯地區章節任務");
			player.addPQLog("泰涅布利斯地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 13: ///ARC地區-[#r賽拉斯地區章節#b]
		if (player.getPQLog("賽拉斯地區章節") <= 0) {
			player.completeQuest(37901, 0);
			player.completeQuest(37902, 0);
			player.completeQuest(37903, 0);
			player.completeQuest(37904, 0);
			player.completeQuest(37905, 0);
			player.completeQuest(37906, 0);
			player.completeQuest(37907, 0);
			player.completeQuest(37908, 0);
			player.completeQuest(37909, 0);
			player.completeQuest(37910, 0);
			player.completeQuest(37911, 0);
			player.completeQuest(37912, 0);
			player.completeQuest(37913, 0);
			player.completeQuest(37914, 0);
			player.completeQuest(37915, 0);
			player.completeQuest(37916, 0);
			player.completeQuest(37917, 0);
			player.completeQuest(37918, 0);
			player.completeQuest(37919, 0);
			player.completeQuest(37920, 0);
			player.completeQuest(37921, 0);
			player.dropMessage(-2, "恭喜完成賽拉斯地區章節任務");
			player.addPQLog("賽拉斯地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 14: ///ARC地區-[#r賽爾尼溫地區章節#b]
		if (player.getPQLog("賽爾尼溫地區章節") <= 0) {
			player.completeQuest(39801, 0);
			player.completeQuest(39802, 0);
			player.completeQuest(39803, 0);
			player.completeQuest(39804, 0);
			player.completeQuest(39805, 0);
			player.completeQuest(39806, 0);
			player.completeQuest(39807, 0);
			player.completeQuest(39808, 0);
			player.completeQuest(39809, 0);
			player.completeQuest(39810, 0);
			player.completeQuest(39811, 0);
			player.completeQuest(39812, 0);
			player.completeQuest(39813, 0);
			player.completeQuest(39814, 0);
			player.completeQuest(39815, 0);
			player.completeQuest(39816, 0);
			player.completeQuest(39817, 0);
			player.completeQuest(39901, 0);
			player.completeQuest(39902, 0);
			player.completeQuest(39903, 0);
			player.completeQuest(39904, 0);
			player.completeQuest(39905, 0);
			player.completeQuest(39906, 0);
			player.completeQuest(39907, 0);
			player.completeQuest(39908, 0);
			player.completeQuest(39909, 0);
			player.completeQuest(39910, 0);
			player.completeQuest(39911, 0);
			player.completeQuest(39912, 0);
			player.completeQuest(39913, 0);
			player.completeQuest(39914, 0);
			player.completeQuest(39915, 0);
			player.completeQuest(39916, 0);
			player.completeQuest(39917, 0);
			player.completeQuest(39918, 0);
			player.completeQuest(39919, 0);
			player.gainItem(2632972, 1);///米特拉核心寶石
			player.dropMessage(-2, "恭喜完成賽爾尼溫地區章節任務");
			player.addPQLog("賽爾尼溫地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 15: ///ARC地區-[#r阿爾克斯地區章節#b]
		if (player.getPQLog("阿爾克斯地區章節") <= 0) {
			player.completeQuest(38101, 0);
			player.completeQuest(38102, 0);
			player.completeQuest(38103, 0);
			player.completeQuest(38104, 0);
			player.completeQuest(38105, 0);
			player.completeQuest(38106, 0);
			player.completeQuest(38107, 0);
			player.completeQuest(38108, 0);
			player.completeQuest(38109, 0);
			player.completeQuest(38110, 0);
			player.completeQuest(38111, 0);
			player.completeQuest(38112, 0);
			player.completeQuest(38113, 0);
			player.completeQuest(38114, 0);
			player.completeQuest(38115, 0);
			player.completeQuest(38116, 0);
			player.completeQuest(38117, 0);
			player.completeQuest(38118, 0);
			player.completeQuest(38119, 0);
			player.completeQuest(38120, 0);
			player.completeQuest(38122, 0);
			player.dropMessage(-2, "恭喜完成阿爾克斯地區章節任務");
			player.addPQLog("阿爾克斯地區章節");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 16: ///AUT欄位-[#另一種力量．真實力量#b]
		if (player.getPQLog("真實力量") <= 0) {
			player.completeQuest(1484, 0);
			player.dropMessage(-2, "恭喜完成開啟AUT欄位");
			player.addPQLog("真實力量");
		} else {
			npc.say(pic.楷體+"#e#r您已完成過此任務！");
		}
		break;
	case 18: ///新手頭頂燈泡-[#我的小屋#b]
		if (player.getPQLog("我的小屋") <= 0) {
			player.completeQuest(500763, 0);
			player.changeMap(500763, 0);
			player.dropMessage(-2, "恭喜你完成任務:我的小屋！");
			player.addPQLog("我的小屋");
		} else {
			npc.say("#fs14##r您已學習過此技能！");
		}
		break;
	case 19: ///新手頭頂燈泡-[#獻給有魅力之人的禮物#b]
		if (player.getPQLog("獻給有魅力之人的禮物") <= 0) {
			player.completeQuest(6500, 0);
			npc.say("#b恭喜你完成任務:獻給有魅力之人的禮物！");
			player.dropMessage(-2, "恭喜你完成任務:獻給有魅力之人的禮物！");
			player.addPQLog("獻給有魅力之人的禮物");
		} else {
			npc.say("#fs14##r您已完成過口袋任務！");
		}
		break;
}
player.runScript("EasyQuest");
