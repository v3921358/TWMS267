import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";

var  monstermaps = Array(
	Array(100020000,0,color.淡粉紅+ pic.金右+" 芽苞山丘"),
    Array(100040300,0,color.淡粉紅+ pic.金右+" 石巨人寺院III"),
    Array(103020420,0,color.淡粉紅+ pic.金右+" 二號線III區域"),
    Array(105010000,0,color.淡粉紅+ pic.金右+" 寧靜的沼澤"),
    Array(260020610,0,color.淡粉紅+ pic.金右+" 日落之路;沉睡沙漠"),
    Array(240011000,0,color.淡粉紅+ pic.金右+" 燃燒地圖;東邊森林"),
    Array(240010520,0,color.淡粉紅+ pic.金右+" 天空之巢III"),
    Array(240020300,0,color.淡粉紅+ pic.金右+" 冰冷死亡戰場"),
    Array(240040320,0,color.淡粉紅+ pic.金右+" 黑翼龍巢穴"),
    Array(240040512,0,color.淡粉紅+ pic.金右+" 龍之巢穴II"),
    Array(221030640,0,color.淡粉紅+ pic.金右+" UFO走廊HO1"),
    Array(273040100,0,color.淡粉紅+ pic.金右+" 遺棄挖掘地區II "),
    Array(450001010,0,color.淡粉紅+ pic.金右+" 風化的開心之地"),
	Array(450001111,0,color.淡粉紅+ pic.金右+" 岩石和火焰領土"),
	Array(450001216,0,color.淡粉紅+ pic.金右+" 安息的洞穴下側"),
	Array(450014300,0,color.淡粉紅+ pic.金右+" 隱藏研究列車"),
	Array(450002010,0,color.淡粉紅+ pic.金右+" 啾樂森林深處"),
	Array(450003300,0,color.淡粉紅+ pic.金右+" 雞群遊戲場I"),
	Array(450003420,0,color.淡粉紅+ pic.金右+" 本色之處III"),
	Array(450005430,0,color.淡粉紅+ pic.金右+" 洞穴的下路"),
    Array(450006230,0,color.淡粉紅+ pic.金右+" 影子跳舞處IV"),
	Array(450007110,0,color.淡粉紅+ pic.金右+" 鏡光大海II"),
    Array(450016010,0,color.淡粉紅+ pic.金右+" 光芒所及之處I"),
	Array(450016110,0,color.淡粉紅+ pic.金右+" 無限沉淪深海I"),
	Array(450009330,0,color.淡粉紅+ pic.金右+" 虛空海浪III"),
	Array(450011540,0,color.淡粉紅+ pic.金右+" 苦痛迷宮地帶IV"),
	Array(450012340,0,color.淡粉紅+ pic.金右+" 世界終結之處I-V"),
    Array(450012440,0,color.淡粉紅+ pic.金右+" 世界終結之處II-V"),
	Array(410003040,0,color.淡粉紅+ pic.金右+" 支配的荒野I"),
	Array(410003090,0,color.淡粉紅+ pic.金右+" 汽車的戲院"),
	Array(410003200,0,color.淡粉紅+ pic.金右+" 橫貫列車"),
    Array(410000520,0,color.淡粉紅+ pic.金右+" 海邊岩石地帶I"),
	Array(410000590,0,color.淡粉紅+ pic.金右+" 西方城牆I"),
	Array(410000700,0,color.淡粉紅+ pic.金右+" 圖書館I區域"),
	Array(410000840,0,color.淡粉紅+ pic.金右+" 燃燒的圖書館I區域"),
	Array(410007002,0,color.淡粉紅+ pic.金右+" 通往城門的路I"),
	Array(410007006,0,color.淡粉紅+ pic.金右+" 被佔領的巷道I"),
	Array(410007012,0,color.淡粉紅+ pic.金右+" 陽光灑落的實驗室I"),
	Array(410007015,0,color.淡粉紅+ pic.金右+" 大門深鎖的實驗室I"),
	Array(410007026,0,color.淡粉紅+ pic.金右+" 桃源境-春I"),
	Array(410007029,0,color.淡粉紅+ pic.金右+" 桃源境-夏I"),
	Array(410007032,0,color.淡粉紅+ pic.金右+" 桃源境-秋I"),
	Array(410007035,0,color.淡粉紅+ pic.金右+" 桃源境-冬I")
	
);
var selStr = "";
for (var i = 0; i < monstermaps.length; i++) {
    selStr += pic.楷體+"#e\r\n#L" + i + "#" + monstermaps[i][2] + "";
}
let select = npc.askMenu(selStr);
player.changeMap(monstermaps[select][0]);
            


