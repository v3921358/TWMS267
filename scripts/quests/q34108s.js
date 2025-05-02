/*     
 *
 *  功能：[消亡旅途]火焰地帶的巨大懸崖
 *

 */

npc.next().sayX("除了這面巨大巖壁，似乎並沒有其他出路，走到死衚衕了……最好還是先沿著這個巖壁往上爬吧。");
let selection = npc.askYesNo("#ho#也是這麼想的吧？那我們就往上爬吧。");
if (selection == 1) {
    npc.startQuest();
} else {
    npc.next().sayX("除了沿著巖壁往上爬，似乎沒有其他出路了。");
}