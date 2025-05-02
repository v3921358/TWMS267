/**
 * Roger's Apple (NPC 2000 Quest 1021) Start
 * Maple Road: Lower level of the Training Camp (Map 2)
 *
 * Gives advice to new players on how to use items.
 *
 *
 */

startScript();

function startScript() {
    npc.ui(1).uiMax().next().noEsc().sayX("你那樣幫我，而且剛才還打倒了怪物。看來#h # 你已經是一個像模像樣的冒險家了。你決定好選擇哪個職業了嗎？");
    npc.ui(1).uiMax().npcFlip().next().noEsc().sayX("#b哪個職業？#k");
    npc.ui(1).uiMax().next().noEsc().sayX("恩，現在開始，你要去的金銀島中，有另外五種職業可以進行轉職。嗯，我記得是……戰士、魔法師、弓箭手、飛俠和海盜。有這五種職業。");
    npc.ui(1).uiMax().npcFlip().next().noEsc().sayX("#b那些職業各有什麼不同呢？#k");
    npc.ui(1).uiMax().next().noEsc().sayX("首先，戰士的力量和體力很強，適合近距離戰鬥。同時，防禦力也相當出色，所以不會輕易倒下。魔法師與戰士不同，透過魔法進行戰鬥，所以相比力量，智力更加重要。因為使用魔法，所以可在遠距離一次攻擊多箇敵人。");
    npc.ui(1).uiMax().next().noEsc().sayX("說到遠距離攻擊，弓箭手才是最適合的。其可以在遠距離使用箭矢，並且控制距離的能力也相當出色。飛俠雖有不亞於戰士的近距離戰鬥能力，但在戰鬥時往往以速度為主，而非力量。");
    npc.ui(1).uiMax().next().noEsc().sayX("最後一個，海盜……其特徵很難用一句話來概括。海盜既能用體術發動近身攻擊，又能在遠距離用手槍或大炮進行攻擊。而且，無論哪種攻擊方式，都相當華麗和富有個性。");
    let sel = npc.noEsc().askMenuX("船長已經跟轉職官聯絡過了，只要你現在先決定好的話，待會兒一到港口就能立即收到轉職官的聯絡。#h #你要選擇什麼職業呢？\r\n#b#L1# 具備強大力量和防禦力的戰士#l\r\n#L2# 利用高超的智力使用魔法的魔法師#l\r\n#L3# 善於遠距離攻擊和控制距離的弓箭手#l\r\n#L4# 進行快速攻擊的飛俠#l \r\n#L5# 戰鬥風格華麗獨特的海盜#l#k");
    player.startQuest(1406, 0, sel);
    switch (sel) {
        case 1:
            npc.ui(1).uiMax().next().noEsc().sayX("嗯！#h #你一定能夠成為帥氣的戰士！");
            break;
        case 2:
            npc.ui(1).uiMax().next().noEsc().sayX("嗯！#h #你一定能夠成為帥氣的魔法師！");
            break;
        case 3:
            npc.ui(1).uiMax().next().noEsc().sayX("嗯！#h #你一定能夠成為帥氣的弓箭手！");
            break;
        case 4:
            npc.ui(1).uiMax().next().noEsc().sayX("嗯！#h #你一定能夠成為帥氣的飛俠！");
            break;
        case 5:
            npc.ui(1).uiMax().next().noEsc().sayX("嗯！#h #你一定能夠成為帥氣的海盜！");
            break;
    }
    if (sel == 1) {
        npc.ui(1).uiMax().next().noEsc().sayX("#h #成為戰士的話，那我要不要成為法師呢？雖然可能還做得不夠好，但也許能在遠處用魔法幫助別人。");
    } else if (sel == 1) {
        npc.ui(1).uiMax().next().noEsc().sayX("我想成為戰士。我不想一味接受別人的幫助，而是想在將來成為能夠獨當一面的冒險家。當然，如果我的力量可以幫到別人的話，那就更好了。");
    }
    player.setInGameCurNodeEventEnd(true);
    player.setInGameDirectionMode(true, false, false, false);
    player.setStandAloneMode(true);
    player.setForcedInput(0);
    player.soundEffect("advStory/whistle");
    npc.setDelay(208);
    player.showReservedEffect("Effect/Direction3.img/adventureStory/Scene2");
    npc.setDelay(3000);
    npc.ui(1).uiMax().next().noEsc().sayX("看來現在船要出發了！");
    player.gainExp(489);
    npc.completeQuest();
    player.setInGameDirectionMode(false, false, false, false);
    player.setStandAloneMode(false);
    player.changeMap(4000004, 0);
}
