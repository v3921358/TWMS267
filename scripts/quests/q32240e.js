/**
 *
 *
 */

startScript();

function startScript() {
    npc.ui(1).uiMax().npcFlip().next().noEsc().sayX("#b冒險之書#k？你是說可以在這裡寫下我的冒險故事，對吧？");
    npc.ui(1).uiMax().npcFlip().next().noEsc().sayX("我正想開始進行冒險，現在馬上開始就行了。……哎？");
    if (player.canGainItem(4460004, 1)) {
        player.gainItem(4460004, 1);
        npc.completeQuest();
        player.setInGameCurNodeEventEnd(true);
        player.setInGameDirectionMode(true, true, false, false);
        player.showScreenAutoLetterBox("adventureStory/mapleLeaf/0", 0);
        npc.setDelay(1800);
        player.setInGameDirectionMode(false, false, false, false);
        player.setInGameCurNodeEventEnd(true);
        npc.ui(1).uiMax().npcFlip().next().noEsc().sayX("這是什麼楓葉？這麼看來，冒險島上存在著巨大的楓樹。這楓葉是跟隨我來到這裡的嗎？");
        npc.ui(1).uiMax().npcFlip().next().noEsc().sayX("這也算是一種紀念，好好珍藏吧。夾在#b 冒險之書#k裡的話，就不會弄皺了。");
    } else {
        npc.ui(1).uiMax().npcFlip().next().noEsc().sayX("先整理下揹包吧.");
    }
}
