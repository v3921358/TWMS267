// 
// Quest ID:17620
// [凱梅爾茲共和國] 禮尚往來


npc.next().sayX("好了,不要有負擔,你就告訴我吧。我能幫你些什麼?");
npc.ui(1).uiMax().next().sayX("嗯.. 那我就跟你直說吧。我想見見吉爾伯特·達尼爾拉。");
npc.next().sayX("...... 我沒有聽錯吧.. 你說什麼?");
npc.ui(1).uiMax().next().sayX("我想見#b吉.爾.伯.特.達.尼.爾.拉#k。");
npc.next().sayX("額.. 那個,我....");
npc.ui(1).uiMax().next().sayX("怎麼了?很難辦? ");
npc.next().sayX("不.. 不是說很難辦...");
npc.ui(1).uiMax().next().sayX("海上男子漢... 剛剛好像跟我說了些什麼..");
if (npc.askYesNo("......... 唉,不管了。好的。從這裡往東邊一直~ 走,經過噴泉之後,就能看到一個有金色點綴的白色建築物。那就是達尼爾拉商團的辦公室。吉爾伯特應該在那裡面。")) {
    // Response is Yes
    npc.next().sayX("我會跟商團提前打好招呼的,你應該能進得去。見到吉爾伯特的話,你一定要禮貌點。他在整個凱梅爾茲共和國都有著影響力。");
    npc.startQuest();
} else {
    // Response is No
    npc.ui(1).sayX("你還在思考嗎?思考好了之後,再告訴我吧。");
}