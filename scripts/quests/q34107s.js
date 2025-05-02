/*     
 *
 *  功能：[消亡旅途]渡過忘卻之湖的紙船
 *

 */

let selection = npc.askYesNoS("（船伕在招手，似是招呼我們上船。）");
if (selection == 1) {
    npc.startQuest();
} else {
    npc.ui(1).sayX("……（像是願意等待。）");
}