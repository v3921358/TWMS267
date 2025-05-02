//
// Quest ID:17643
// [凱梅爾茲共和國] 海.盜.出.現


npc.next().sayX("怎麼這麼寧靜。該不會是海盜們有所察覺撤退了吧。");
npc.ui(1).uiMax().next().sayX("說不準哦。說不定會那樣呢。但是才沒過幾天再等等看吧。");
if (npc.askYesNo("嗯..就那樣吧。你做好了隨時和海盜交戰的準備嗎?")) {
    npc.startQuest();
    for (let i = 0; i < 30; i++) {
        map.spawnMob(9390815, -332 + i * 20, 198);
    }
    npc.id(9390217).ui(1).uiMax().npcFlip().next().noEsc().sayX("是海盜!!海盜出現了!!");
    npc.ui(1).uiMax().next().noEsc().sayX("終於上鉤了!!");
    npc.id(9390217).ui(1).uiMax().npcFlip().next().noEsc().sayX("海盜們朝我們商船過來了!!");
    npc.ui(1).uiMax().next().noEsc().sayX("去吧!#h0# !!");
} else {
    npc.ui(1).sayX("那麼就做好隨時應戰的準備如何。");

}
