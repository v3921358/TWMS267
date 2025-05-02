/*     
 *  
 *  [拉克蘭]會合
 *  

 */
npc.next().sayX("我聽說你要游到下游去，如果我們成功解放這夢之城市，你也能達成目的。");
let selection = npc.askYesNo("怎麼樣，你願意祝我們一臂之力嗎？");
if (selection == 1) {
    npc.startQuest();
}

function end(mode, type, selection) {
    npc.completeQuest();
    qm.dispose();
}

