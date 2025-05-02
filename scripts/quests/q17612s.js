// 
// Quest ID:17612
// [凱梅爾茲共和國] 一些誤會


npc.ui(1).uiMax().meFlip().next().sayX("那個,村長。我想話想對你說。");
npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("你來得正好。今天我們捕到了很多魚。如果每天都像今天這樣就好了。");
npc.ui(1).uiMax().meFlip().next().sayX("嗯…真是太好了…那個…我想跟你說...");
let selection = npc.id(9390201).ui(1).uiMax().npcFlip().noEsc().askMenuX("啊,對了。你說你有話要說,對吧。你說吧。是什麼事情?\r\n#b\r\n#L0# (不…還是再仔細想想比較好。)#l\r\n#L1# (嗯,告訴他實情吧。)#l");

if (selection == 0) {
    npc.ui(1).meFlip().sayX("啊,沒什麼。 (我得先整理下思路,再跟他說。)");
} else if (selection == 1) {
    npc.ui(1).uiMax().meFlip().next().sayX("其實我不是單純的冒險家。我是奉冒險島世界的西格諾斯女皇的命令，為了和這裡建立友好關係而來。");
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("你這是什麼話啊？你不是冒險家，是冒險島世界的女皇派來的？");
    npc.ui(1).uiMax().meFlip().next().sayX("沒錯。如果我一開始就說出事實，我擔心會產生不必要的誤會。所以打算在村長的身邊多待一會兒再表明我們的立場，但是您對遭遇了暴風雨的我如此熱情親切，我也沒有辦法再隱瞞下去了。");
    // Unhandled User Effect [UserEffect_SkillAffected] Packet: 04 40 FD 13 00 01
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("等下，為什麼會有誤會呢……而且，你為什麼要和我說這些呢？我只不過是個村長而已啊。");
    npc.ui(1).uiMax().meFlip().next().sayX("但是，村長不是這個村子地位最高的人嗎？這裡是凱梅爾茲，那和凱梅爾茲地位最高的人……");
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("……哈哈哈哈哈！你，看來你是誤會了啊。");
    npc.ui(1).uiMax().meFlip().next().sayX("……您這是……什麼意思？");
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("我在這個村子裡確實是地位最高的人。不過凱梅爾茲可不只有我們貝里村啊。這裡只是個小漁村啊。如果你是要傳達這些話，那就應該去凱梅爾茲共和國的#e首都#n，#e#b桑凱梅爾茲#n#k啊！");
    npc.ui(1).uiMax().meFlip().next().sayX("桑……凱梅爾茲……？");
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("沒錯！我看你不像是在說謊，你還是去找凱梅爾茲共和國的首領#b吉爾伯特·達尼爾拉#k大人吧。看來你對桑凱梅爾茲一無所知啊？");
    npc.ui(1).uiMax().meFlip().next().sayX("所以說……除了這個村子，還有更大的村子啊。");
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("什麼村子啊！桑凱梅爾茲是個很大的城市！那是凱梅爾茲共和國的首都，應有盡有！雖然我不知道為什麼大家都喜歡去那個複雜的地方生活……這個貝里村真是最適合生活了……");
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("這樣吧。正好凱梅爾茲最大的商團#b達尼爾拉商團#k的船停泊在這裡。船長是首領的二兒子，他正在就在村子裡，你快去見見他吧。");
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("然後，你這段時間幫助了我們村子，這是謝禮。");
    ///根據職業給與對應的 裝備
    let itemId = 1004228 + player.getJobCategory();
    if (player.gainItem(itemId, 1)) {
        npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("這算是我給你的禮物。反正我也不需要這個東西了……");
        npc.completeQuest();
        player.gainExp(630724);
    } else {
        npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("你的揹包滿了，清理下吧！");
    }


}