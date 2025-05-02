// 
// Quest ID:17679
// [凱梅爾茲共和國] 對決! 普賽依!


npc.ui(1).uiMax().meFlip().next().sayX("現在我就讓你們這些傢伙的海盜行為到達截止日期。");
npc.ui(1).uiMax().next().sayX("嗯?見過這麼腦袋不靈光的朋友嗎。看來你還不太明白你現在的處境啊。");
npc.ui(1).uiMax().meFlip().next().sayX("才不呢,我可是非常清楚呢。我要抓的傢伙們全部聚集在這裡,反而是我們要說聲謝謝呢。");
npc.ui(1).uiMax().next().sayX("哼,真是個趾高氣昂的傢伙。我就等著看你能裝得意裝到什麼時候。");
if (npc.askYesNoE("(話是那麼說,可是該怎麼辦呢。現在就立馬攻擊嗎?", true)) {
    for (let i = 0; i < 10; i++) {
        map.spawnMob(9390815, 32, 198);
    }
    for (let i = 0; i < 10; i++) {
        map.spawnMob(9390816, 32, 198);
    }
    map.spawnMob(9390817, 32, 198);
    npc.startQuest();
} else {
    npc.ui(1).meFlip().sayX("(還是先等等。現在進攻還有些輕率。)");

}