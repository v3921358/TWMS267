import { pic } from "scripts/expands/pic.js";
import { color } from "scripts/expands/color.js";


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

let selStr = "";

const menuList = [
    {
        標題: "快捷傳送",
        腳本: "tel",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
    },
    {
        標題: "福利中心",
        腳本: "福利中心/福利中心選單",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
    },
    {
        標題: "快速轉職",
        腳本: "changeJob",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
        換行: 2,
    },
    {
        標題: "一鍵任務",
        腳本: "EasyQuest",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
    },
    {
        標題: "外觀功能",
        腳本: "setSkinMenu",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
    },
    {
        標題: "儲值福利",
        腳本: "累積儲值/累積儲值選單",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
        換行: 2,
    },
    {
        標題: "傷害字型",
        腳本: "damageSkin",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
    },
    {
        標題: "萌獸圖鑑",
        腳本: "openFam",
        顏色: getRandC(),
        前綴: pic.橘圈圈,
        後綴: pic.橘圈圈,
        換行: 3,
    },
]


let time = player.getOnlineTime(); ///取得線上時間
let accid = player.getAccountId(); ///取得帳號ID
let Nx2 = player.getPoint(); ///取得楓葉點數
let Nx1 = player.getCash(); ///取得樂豆點數

//置頂圖樣
selStr += pic.小金1 + " #d在線時間：#r" + time + " #d分鐘\r\n"
selStr += pic.小金2 + " #d帳號ID：#r" + accid + "#d 號\r\n";
selStr += pic.小金3 + " #d楓葉點數：#r" + Nx2 + "#d 點 \r\n";
selStr += pic.小金4 + " #d樂豆點數：#r" + Nx1 + "#d 點 \r\n\r\n";

menuList.forEach(
    function (item, index) {
        if (typeof (item.顯示) !== "undefined" && item.顯示 == false && !player.isGm())
            return
        selStr += "#L" + index + "#"
        if (typeof (item.顏色) !== "undefined" && item.顏色 != "")
            selStr += "#fc0xFF" + item.顏色 + "#"
        if (typeof (item.粗體) !== "undefined" && item.粗體 == true)
            selStr += "#e"
        if (typeof (item.前綴) !== "undefined")
            selStr += item.前綴
        if (typeof (item.標題) !== "undefined")
            selStr += item.標題
        if (typeof (item.後綴) !== "undefined")
            selStr += item.後綴
        if (typeof (item.粗體) !== "undefined" && item.粗體 == true)
            selStr += "#n"
        if (typeof (item.顏色) !== "undefined" && item.顏色 != "")
            selStr += "#k"
        selStr += "#l"
        if (typeof (item.換行) !== "undefined" && item.換行 != "") {
            let tempNextLine = item.換行
            while (tempNextLine > 0) {
                selStr += "\r\n"
                tempNextLine--
            }
        }

    }
)
//置底圖樣
selStr += pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + pic.咖波 + "\r\n";


let sel = npc.askMenu(selStr)
if (sel != -1) player.runScript(menuList[sel].腳本);


function getRandC() {
    var letters = '1230ABC'.split('');
    var color = '';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 7)];
    }
    return color;
}
