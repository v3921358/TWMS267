// 
// Quest ID:32241
// 巨大古樹之岔路


npc.ui(1).uiMax().meFlip().next().sayX("嗯……那麼，現在該做什麼呢？");
let selection = npc.noEsc().askMenuX("#b#L0#吃飯。#l\r\n#L1#去練級。#l\r\n#L2#睡覺。#l\r\n#L3#學習。#l\r\n#L4#找別的事情做。#l");
switch (selection) {
    case 0:
        npc.ui(1).meFlip().sayX("好吧，我們吃飯吧。先吃了飯再考慮別的。");
        break;
    case 1:
        npc.ui(1).meFlip().sayX("好吧，先練級吧，現在這個等級還很弱！");
        break;
    case 2:
        npc.ui(1).meFlip().sayX("好吧，先睡一覺吧。只有睡得好，才會長個兒，腦子也會變聰明。");
        break;
    case 3:
        npc.ui(1).meFlip().sayX("哈哈哈！我也！相當無聊。");
        break;
    case 4:
        npc.ui(1).uiMax().meFlip().next().sayX("好吧，找點別的事情做做！\r\n不過，去哪兒找事情做呢？我總感覺有事情可做，可真正想做的時候又不知道該做什麼了。");
        if (npc.ui(1).uiMax().npcFlip().askAcceptX("先去#b六岔路口#k看看吧？因為那裡是金銀島的中心地。先去那裡看看，然後再決定要去哪裡吧。\r\n\r\n#r(接受後，立即移動到地圖。)#k")) {
            npc.startQuest();
            player.changeMap(910400200, 0);
        } else {
            npc.ui(1).uiMax().meFlip().next().sayX("嗯，真的想要走出的時候，腿腳卻疼痛起來，真是麻煩。要不就這樣放棄？");
        }
        break;
}

