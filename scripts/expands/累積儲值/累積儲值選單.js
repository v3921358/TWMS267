import { pic } from "scripts/expands/pic.js";
import { color } from "scripts/expands/color.js";

let menu = "";
menu += "\t#b" + pic.wi14 + " 未刷餘額  #r" + player.getHyPay(1) + "#d元#k\r\n";
menu += "\t#b" + pic.wi14 + " 總共贊助  #r" + player.getHyPay(2) + "#d元#k\r\n";
menu += "--------------------------------------------------\r\n";
menu += "#fc0xFF9370DB#\t★ 違規項目 :     現金交易      ★#k\r\n";
menu += "#fc0xFF9370DB#\t★ 違規項目 : 外掛；腳本；改檔  ★#k\r\n";
menu += "#fc0xFF9370DB#\t★ 處罰規則 : 統一封鎖禁止帳號  ★#k\r\n";
menu += "--------------------------------------------------\r\n";
menu += "#L1#" + pic.彩斜星 + "【　   #r刷新持有餘額#k　   】#k#l\r\n";
menu += "#L2#" + pic.彩斜星 + "【　   #r領取累積贊助#k　   】#k#l\r\n";
let menuSel = npc.askMenu(menu);

switch (menuSel) {
    case 1:
        var text = "﹝#r注意!!!#k#b需要把餘額兌換成樂豆才可以#k#r領取#k#b贊助裝備#k﹞\r\n\r\n";
        text += "#fc0xFF4F9D9D#" + pic.wi14 + " 可用餘額持有：#r" + player.getHyPay(1) + "#k 元\r\n\r\n";
        text += "#fc0xFF4F9D9D#" + pic.wi14 + " 樂豆點數持有#r " + player.getCash() + "#k 點\r\n\r\n";
        text += "" + pic.綠圓框打勾 + "#r﹝確認沒問題，確定刷新請按我﹞#l\r\n\r\n\r\n";
        let change = npc.askYesNo(text);
        if(player.getHyPay(1) > 0){
            player.updateHypay();
            player.dropMessage(6, "刷新總贊助: " + player.getHyPay(2));
        } else {
            npc.say(pic.楷體+"#r持有餘額不足以進行操作。");
        }
        break;
    case 2:
        player.runScript("累積儲值/累積儲值領取");
        break;
}
