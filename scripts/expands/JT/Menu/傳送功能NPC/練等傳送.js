import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";


var  monstermaps = Array(
    Array(10000,0,""+pic.黑框愛心+"#fs16#"+color.淡粉紅+" 楓之島(新手地圖)           lv 0~0"),
    Array(100040300,0,""+pic.黑框愛心+"#fs16#"+color.淡粉紅+" 石巨人寺院III              lv 10~30"),
    Array(103020420,0,""+pic.黑框愛心+"#fs16#"+color.淡粉紅+" 二號線III區域              lv 40~60"),
    Array(105010000,0,""+pic.黑框愛心+"#fs16#"+color.淡粉紅+" 寧靜的沼澤                 lv 50~75"),
    Array(260020610,0,""+pic.黑框愛心+"#fs16#"+color.淡粉紅+" 日落之路;沉睡沙漠          lv 85~100"),
    Array(240011000,0,""+pic.黑框愛心+"#fs16#"+color.亮綠+" 燃燒地圖;東邊森林          lv 100~107"),
    Array(240010520,0,""+pic.黑框愛心+"#fs16#"+color.亮綠+" 5星力;天空之巢III          lv 107~115"),
    Array(240020300,0,""+pic.黑框愛心+"#fs16#"+color.亮綠+" 15星力;冰冷死亡戰場        lv 115~125"),
    Array(240040320,0,""+pic.黑框愛心+"#fs16#"+color.亮綠+" 65星力;黑翼龍巢穴          lv 140~150"),
    Array(240040512,0,""+pic.黑框愛心+"#fs16#"+color.亮綠+" 70星力;龍之巢穴II          lv 150~170"),
    Array(221030640,0,""+pic.黑框愛心+"#fs16#"+color.亮綠+" 140星力;UFO走廊HO1         lv 170~190"),
    Array(273040100,0,""+pic.黑框愛心+"#fs16#"+color.亮綠+" 燃燒地圖;遺棄挖掘地區II     lv 190~200"),
    Array(450001010,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+" 30-ARC 風化的開心之地       lv 200~210"),
	Array(450001111,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+" 30-ARC 岩石和火焰領土       lv 200~210"),
	Array(450001216,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+" 60-ARC 安息的洞穴下側       lv 210~220"),
	Array(450014300,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"100-ARC 隱藏研究列車         lv 220~225"),
	Array(450002010,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"100-ARC 啾樂森林深處         lv 220~225"),
	Array(450003300,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"210-ARC 雞群遊戲場I          lv 220~230"),
	Array(450003420,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"210-ARC 本色之處III          lv 225~235"),
	Array(450005430,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"360-ARC 洞穴的下路           lv 240~250"),
    Array(450006230,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"480-ARC 影子跳舞處IV         lv 240~250"),
	Array(450007110,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"600-ARC 鏡光大海II           lv 240~250"),
    Array(450016010,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"600-ARC 光芒所及之處I        lv 245~260"),
	Array(450016110,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"600-ARC 無限沉淪深海I        lv 245~260"),
	Array(450009330,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"730-ARC 虛空海浪III          lv 250~260"),
	Array(450011540,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"730-ARC 苦痛迷宮地帶IV       lv 260~270"),
	Array(450012340,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"880-ARC 世界終結之處I-V      lv 270~280"),
    Array(450012440,0,""+pic.黑框愛心+"#fs16#"+color.藍綠+"880-ARC 世界終結之處II-V     lv 270~280"),
	Array(410003040,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"130-AUT 支配的荒野I          lv 270~300"),
	Array(410003090,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"160-AUT 汽車的戲院           lv 270~300"),
	Array(410003200,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"200-AUT 橫貫列車             lv 270~300"),
    Array(410000520,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"50-AUT 海邊岩石地帶I         lv 270~300"),
	Array(410000590,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"50-AUT 西方城牆I             lv 270~300"),
	Array(410000700,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"50-AUT 圖書館I區域           lv 270~300"),
	Array(410000840,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"100-AUT 燃燒的圖書館I區域    lv 270~300"),
	Array(410007002,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"130-AUT 通往城門的路I    lv 270~300"),
	Array(410007006,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"160-AUT 被佔領的巷道I    lv 270~300"),
	Array(410007012,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"200-AUT 陽光灑落的實驗室I    lv 270~300"),
	Array(410007015,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"200-AUT 大門深鎖的實驗室I    lv 270~300"),
	Array(410007026,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"230-AUT 桃源境-春I    lv 270~300"),
	Array(410007029,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"230-AUT 桃源境-夏I    lv 270~300"),
	Array(410007032,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"230-AUT 桃源境-秋I    lv 270~300"),
	Array(410007035,0,""+pic.黑框愛心+"#fs16#"+color.淡紫+"230-AUT 桃源境-冬I    lv 270~300")
	
); 

var selStr = "";
for (var i = 0; i < monstermaps.length; i++) {
    selStr += "\r\n#L" + i + "#" + monstermaps[i][2] + "";
}

let select = npc.askMenu(selStr);
player.changeMap(monstermaps[select][0]);
npc.say("你已到達選擇傳送的地圖！\r\n如有發現怪物異常可回報！");
            


