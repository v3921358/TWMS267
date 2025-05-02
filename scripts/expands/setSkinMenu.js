import { pic } from "scripts/expands/pic.js";
import { color } from "scripts/expands/color.js";

let ttt6 ="#fUI/UIWindow.img/PvP/Scroll/enabled/next2#";

let text ="";
text+="#L1#"+pic.咖波+"前往查詢道具代碼網站#l\r\n";
text+="#L2#"+pic.咖波+"前往查看喜歡的臉型#l\r\n";
text+="#L3#"+pic.咖波+"前往查看喜歡的髮型#l\r\n\r\n";
text += "﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉\r\n";
text+="#L4#"+pic.咖波+"自定義選擇髮型 (#r 輸入代碼#k )#l\r\n\r\n";
text+="#L5#"+pic.咖波+"自定義選擇膚色 (#r 輸入代碼#k )#l\r\n\r\n";
text+="#L6#"+pic.咖波+"自定義選擇臉型 (#r 輸入代碼#k )#l\r\n\r\n";
text+="#L7#"+pic.咖波+"自定義選擇機器人髮型 (#r 輸入代碼#k )#l\r\n\r\n";
text+="#L8#"+pic.咖波+"自定義選擇機器人臉型 (#r 輸入代碼#k )#l\r\n\r\n";
text+="#L9#"+pic.咖波+"自定義選擇機器人膚色 (#r 輸入代碼#k )#l\r\n\r\n";
let selection = npc.askMenuA(text);
switch (selection) {
    case 1:
        player.openURL("https://bingfeng.tw/wz/item.html");
        break;
    case 2:
        player.openURL("https://forum.gamer.com.tw/Co.php?bsn=07650&sn=6152476");
        break;
    case 3:
        player.openURL("https://forum.gamer.com.tw/Co.php?bsn=07650&sn=6152110");
        break;
    case 4:
        player.runScript('changeSkin/setHair');
        break;
    case 5:
        player.runScript('changeSkin/setSkin');
        break;
    case 6:
        player.runScript('changeSkin/setFace');
        break;
    case 7:
        player.runScript('changeSkin/setAndroidHair');
        break;
    case 8:
        player.runScript('changeSkin/setAndroidFace');
        break;
    case 9:
        player.runScript('changeSkin/setAndroidSkin');
        break;
}