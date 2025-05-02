import {pic} from "scripts/expands/pic.js";
import {color} from "scripts/expands/color.js";

let sel = npc.askMenu(pic.楷體+"#e歡迎使用福利中心功能:\r\n #L1# "+pic.愛心方框+" 等級獎勵#l\r\n #L2# "+pic.愛心方框+" 線上獎勵#l\r\n");
switch (sel){
    case 1:
        player.runScript("福利中心/等級獎勵");
        break;
    case 2:
        player.runScript("福利中心/線上獎勵");
        break;
}