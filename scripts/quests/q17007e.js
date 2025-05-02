//
// Quest ID:17007
// [凱梅爾茲交易所]第三次航行


if (player.gainItem(4310100, 1)) {
    npc.completeQuest();
    npc.ui(1).uiMax().next().noEsc().sayX("你剛剛透過貿易獲得了1個凱梅爾茲金幣。 ");
    npc.ui(1).uiMax().next().noEsc().sayX("為進行貿易所做的準備已經全部完成。怎麼樣?感覺馬上就能成為有錢人了吧?繼續這樣進行貿易的話,你的等級會提高,也會變得富有的。 ");
    npc.ui(1).noEsc().sayX("我會期待著你成為凱梅爾茲的大富豪的那一天。");
} else {
    npc.next().sayX("揹包空間不足，無法獲得金幣，請清理揹包其他欄！");
}
