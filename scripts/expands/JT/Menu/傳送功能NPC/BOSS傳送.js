let eff1 = "#fEffect/CharacterEff/1112924/0/0#";
let eff2 = "#fEffect/CharacterEff/1112925/0/0#";
let eff3 = "#fEffect/CharacterEff/1112924/0/0#";
let eff4 = "#fEffect/CharacterEff/1112925/0/0#";//小紅心
let cat1 = "#fItem/Pet/5000000.img/alert/0#";
let cat2 = "#fItem/Pet/5000000.img/fly/0#";
let cat3 = "#fItem/Pet/5000000.img/cry/0#";
let tu1 = "#fItem/Cash/0501.img/05010045/effect/stand1/1#";
let tu2 = "#fItem/Cash/0501.img/05010002/effect/default/7#";
let tu3 = "#fUI/NameTag/medal/758/w#";
let tu4 = "#fEffect/SetEff.img/245/effect/28#";
let tu5 = "#fUI/NameTag/medal/758/e#";
let tu6 = "#fUI/NameTag/medal/758/c#";
let fy = "#fUI/RunnerGame.img/RunnerGameUI/UI/Point/1#";
let fy1 = "#fUI/RunnerGame.img/RunnerGameUI/UI/Point/2#";
let dz = "#fUI/GuildMark/Mark/Etc/00009020/1#";
let dz1 = "#fEffect/CharacterEff/1003393/1/0#";
let star = "#fEffect/CharacterEff/1051294/1/1#";
let easy = "#fUI/UIWindow4.img/typingDefense/Info/Mode/0#";
let normal = "#fUI/UIWindow4.img/typingDefense/Info/Mode/1#";
let hard = "#fUI/UIWindow4.img/typingDefense/Info/Mode/2#";

let bossList = [
    ["炎魔", " #v1002357# #v1012478# #v1022231# ", 211042300],
    ["闇黑龍王", " #v1122076# #v1032241# #v1113149# ", 240050400],
    ["皮卡啾", " #v1132272# #v1162025# #v1022232# ", 270050000],
    ["凡雷恩", " #v1113089# #v1032227# #v1122274# ", 211070000],
    ["阿卡伊農", " #v1122150# #v1051309# #v1050253# ", 272020110],
    ["希拉", " #v1162009# #v1032136# ", 262030000],
    ["西格諾斯", " #v1152155# #v1113070# #v1032216# ", 271040000],
    ["拉圖斯", " #v1022277# #v3016206# ", 220080000],
    ["森蘭丸", " #v1012478# #v1022231# ", 807300100],
    ["濃姬", " #v2630594# #v3010864# ", 811000999],
    ["露塔必思", " #v4310065# #v1402196# #v1042387# ", 105200000],
    ["培羅德", " #v4310288# #v1122264# #v1122265# #v1122266# #v1122267# ", 863010000],
    ["梅格耐斯", " #v1402179# #v1182087# #v1102485# ", 401060000],
    ["史烏", " #v4310156# #v1402251# #v1052882# #v1012632# ", 350060300],
    ["戴米安", " #v4310156# #v1402251# #v1052882# #v1022278# ", 105300303],
    ["大綠水靈", " #v1113313# #v1113316# ", 160000000],
    ["露希妲", " #v4310249# #v1012757# #v1132308# #v1662111# #v1402259# #v1053063# ", 450003600],
    ["威爾", " #v4310249# #v1012757# #v2633926# #v2438412# #v1402259# #v1053063# ", 450007240],
    ["戴斯克", " #v4310249# #v1032330# #v1113306# #v4001893# #v1402259# #v1053063# ", 450009301],
    ["真希拉", " #v4310249# #v1122443# #v1122430# #v4001894# #v1402259# #v1053063# ", 450011990],
    ["頓凱爾", " #v4310249# #v1032330# #v1032316# #v4001893# #v1402259# #v1053063# #v1113332# ", 450012200],
    ["黑魔法師", " #v1182285# #v2439614# #v4036458# #v1402259# #v1053063# #v2644200# ", 450012500],
    ["賽蓮", " #v1122443# #v2633927# #v2632972# #v2644201# ", 410000670],
    ["卡洛斯", " #v2636606# #v2634472# #v2644202# #v1662206# ", 410005005],
    ["咖凌", " #v2636607# #v2635747# #v2644203# #v1662198# ", 410007025]
];

// 定義填充函數
function padBossName(name, length) {
    while (name.length < length) {
        name += "　"; // 使用全形空白填充
    }
    return name;
}

// 假設每個名稱需要填充到5個全形字寬度
var maxLength = 4;

while (true) {
var GC1 = "#fEffect/CharacterEff.img/1082700/2/0#";
var SW1 = "#fEffect/CharacterEff.img/1112960/0/0#";
var SW2 = "#fEffect/CharacterEff.img/1112960/0/1#";
var SW3 = "#fEffect/CharacterEff.img/1112960/0/2#";
var SW4 = "#fEffect/CharacterEff.img/1112960/0/3#";

var XC01 = "#fUI/NameTag.img/292/w#";
var XC02 = "#fUI/NameTag.img/292/c#";
var XC03 = "#fUI/NameTag.img/292/e#";

selStr = " #e魔 王 傳 送" + "\r\n"; 
 
    for (var i = 0; i < bossList.length; i++) {
        if (i % 4 == 0 && i != 0) {
            selStr += "\r\n";
        }
        selStr += "#L" + i + "#" + padBossName(bossList[i][0], maxLength) + "";
    }
    selStr += "\r\n";

    let selected = npc.askMenu(selStr);
    let txt = "#b是否確認挑戰:#r" + bossList[selected][0] + "\r\n#fs16#" + bossList[selected][0] + "#b主要掉落：" + bossList[selected][1] + "";
    let YN = npc.askYesNo(txt);
    if (YN) {

		player.changeMap(bossList[selected][2], 0);
        break;
    }
}
