// 
// Quest ID:17655
// [凱梅爾茲共和國] 冤枉的指控


npc.ui(1).uiMax().next().sayX("嗯?你怎麼也在這裡?你在這裡幹嘛呢?不對,那人..那不是海本的使節團嗎?");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("怎麼躺在那裡啊?#h0#究竟怎麼回事?");
npc.ui(1).uiMax().meFlip().next().sayX("啊,那個...");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("等會!!那人,已經死了?");
npc.ui(1).uiMax().next().sayX("你說什麼?真的嗎?");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("我知道了!是你這傢伙乾的!!從別的地方來的傢伙,你究竟對凱梅爾茲有什麼惡意竟然殺死了別的國家的使節團!!");
npc.ui(1).uiMax().meFlip().next().sayX("等等!我沒殺他!");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("吵死了!你這傢伙!!您在幹嘛呢。吉爾伯特首領!!你打算放任殺人犯逃跑嗎?");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("請稍等!#h0#沒理由會殺人的!");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("你竟然袒護從外地來的殺人犯!你這是要幹什麼!");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("你只會僅僅憑狀況就去斷定不認識的人是殺人犯嗎");
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("你這傢伙!!現在看來你們是一夥的啊!!吉爾伯特首領!你兒子現在所作所為難道不是叛逆行為嗎?!");
npc.ui(1).uiMax().next().sayX("...把兩人都逮捕了");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("首領!!");
player.showScreenAutoLetterBox("Map/EffectBT.img/dawnveil1/Clare", 0);
player.showScreenAutoLetterBox("Map/EffectBT.img/dawnveil1/Clare2", 0);
npc.id(9390206).ui(1).uiMax().npcFlip().next().sayX("搞,搞什麼呀這是!!眼,眼睛!!我的眼睛看不見了!!");
npc.ui(1).uiMax().next().sayX("請冷靜點!這隻是暫時的!這是有誰在搞鬼了!");
npc.id(9390204).ui(1).uiMax().npcFlip().next().sayX("你在幹嘛呢?你想被拖走嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("哦哦,哦,哦");
npc.startQuest();
player.changeMap(865030000, 0);