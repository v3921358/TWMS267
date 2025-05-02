import { pic } from "scripts/expands/pic.js";

let firstHairId = 30000;
let lastHairId = 68537;

let selection = npc.askMenuA("#fc0xFF87CEEB#"+pic.聖誕鈴鐺+"歡迎來到自選髮型功能NPC ：\r\n\r\n"+pic.驚嘆號金色+"#r.如兌換馬上斷線請立即回報髮型代碼。\r\n\r\n\r\n#L0##b〔 開始兌換 〕#l\r\n\t\r\n");

switch (selection) {
    case 0:
        var changeHairNumber = npc.askNumber("#b請輸入想變更的髮型代碼#r(單次使用花費樂豆點數:150)：", firstHairId, firstHairId, lastHairId);
        if(player.getCash() >= 150) {
            player.setHair(changeHairNumber);
            player.modifyCashShopCurrency(1, -150);
            npc.askMenuE("變更髮型完成。")
        }
        break;
}