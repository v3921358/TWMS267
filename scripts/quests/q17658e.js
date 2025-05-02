// 
// Quest ID:17658
// [凱梅爾茲共和國] 克萊爾的故事


npc.ui(1).uiMax().next().sayX("父親是為了我的幸福什麼都肯去做的人。但是那份心意有些過頭了。父親想把我嫁到海本王族去。");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("嫁過去!!??");
npc.ui(1).uiMax().next().sayX("是的,所以父親把我送到了海本王族人們接受教育的地方去留學。可是我討厭那樣。也討厭沒有愛情的婚姻,連海本王國的思想都讓我討厭。還有..");
npc.ui(1).uiMax().meFlip().next().sayX("還有?");
npc.ui(1).uiMax().next().sayX("還有?其實我想系統地學習魔法!");
npc.ui(1).uiMax().meFlip().next().sayX("學魔法?");
npc.ui(1).uiMax().next().sayX("是的，成為冒險島世界最出色的魔法師是我的夢想。所以我一個人偷偷地自學了。");
npc.ui(1).uiMax().meFlip().next().sayX("啊哈,所以你才會用那麼多魔法啊。");
npc.ui(1).uiMax().next().sayX("是的,可是自學總會到達極限的。所以我決心瞞著父親到魔法王國去留學。但是我這個年紀的女孩一個人想掙到那麼大筆的留學費用有點難以實現。所以...那個...");
npc.ui(1).uiMax().meFlip().next().sayX("啊,好了,你不說我也明白了。我明白你的意思了。");
npc.ui(1).uiMax().next().sayX("好的,謝謝你。會實現的。");
npc.ui(1).uiMax().meFlip().next().sayX("可是克萊爾小姐的父親為什麼咬定我是殺人犯呢?");
npc.ui(1).uiMax().next().sayX("是啊。這我也不是很清楚,我想或許跟海本王國有所關聯。父親最近好像給海本王族寄去了很多禮物和書信。");
npc.completeQuest();
player.gainExp(1058907);