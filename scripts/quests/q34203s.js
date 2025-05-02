/*     
 *  
 *  功能：[啾啾島]頂級大廚舔舔的祕製料理
 *  

 */


npc.next().sayX("#ho#！如果要製作出讓穆託滿意的#r它特色料理#k，我需要#b#v4034942:##z4034942:#20個#k！哈！");
let selection = npc.askYesNo("你能速去速回嗎？");
if (selection == 1) {
    npc.next().sayX("#b#v4034942:##z4034942:#可以去狩獵在村莊左側的#b五色花園#k地區的#b菠蘿鹿#k獲得！");
    npc.ui(1).sayX("沒有時間了，快去！");
    npc.startQuest();
}