import { pic } from "scripts/expands/pic.js";

let first = 20000;
let last = 58818;

let selection = npc.askMenuA("#fc0xFF87CEEB#"+pic.聖誕鈴鐺+"歡迎來到自選臉型功能NPC ：\r\n\r\n"+pic.驚嘆號金色+"#r.如兌換馬上斷線請立即回報臉型代碼。\r\n\r\n\r\n#L0##b〔 開始兌換 〕#l\r\n\t\r\n");

switch (selection) {
    case 0:
        var number = npc.askNumber("#b請輸入想變更的臉型代碼#r(單次使用花費樂豆點數:150)：", first, first, last);
        if(player.getCash() >= 150) {
            player.setFace(number);
            player.modifyCashShopCurrency(1, -150);
            npc.askMenuE("變更臉型完成。")
        }
        break;
}