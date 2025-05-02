/*    
 *  
 *  功能：[拉克蘭]誰是“清醒者”？2
 *  

 */

npc.next().sayX("“清醒著”是誰？");
let selection = npc.askMenuX("你說什麼？\r\n\r\n#b#L0#美女面具#l\r\n#b#L1#紳士貓面具#l\r\n#b#L2#蝦蝦面具#l");
if (selection == 0) {
    npc.ui(1).sayX("你肯定看錯了！");
} else if (selection == 1) {
    npc.ui(1).sayX("嗯，你肯定看錯了！");
} else {
    npc.ui(1).sayX("原來如此。和蝦蝦面具對話，試著說服他吧。");
    npc.startQuest();
}

