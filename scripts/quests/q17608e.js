// 
// Quest ID:17608
// [凱梅爾茲共和國] 平安(?) 到達的地方


npc.ui(1).uiMax().meFlip().next().sayX("你好……請問，我能問你個問題嗎？");
npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("天啊！！你真是太狼狽了啊！你遇到暴風雨了嗎？你能活下來，運氣真是太好了！");
npc.ui(1).uiMax().meFlip().next().sayX("啊，是的……我為了去凱梅爾茲，坐上了船，但是遇到了暴風雨。老人家你能告訴我這裡是哪裡，怎麼才能去凱梅爾茲嗎？");
npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("哎呀，你的運氣真的很好啊！如果你是來找凱梅爾茲，那就找對了，你現在所在的地方就是凱梅爾茲。而且，這裡是凱梅爾茲最適宜生活的貝里村。");
npc.ui(1).uiMax().meFlip().next().sayX("啊……是嗎？能找到這裡真是太好了。");
npc.ui(1).uiMax().meFlip().next().sayX("#b(雖然宣佈成立了共和國，但還是個普通的漁村啊……果然，傳聞和現實還是有很大的差距。)#k");
npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("自我介紹下吧，我是這個村的村長貝里。我們家世世代代都生活在這裡，都是這裡的村長。所以我叫貝里，哈哈。既然你是來找凱梅爾茲的，那你就是我的客人了！你來凱梅爾茲有什麼事嗎？");
npc.ui(1).uiMax().meFlip().next().sayX("啊，你好，我……");
player.setInGameCurNodeEventEnd(true);
//player.setTemporarySkillSet(0);
player.setInGameDirectionMode(true, false, false, false);
player.setLayerBlind(true, 120, 0, 0, 0, 500, 0);
npc.id(1540451).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face1##b(……為了避免這樣的誤會，我們必須非常慎重地接近他們。如果可以，在轉達女皇大人的資訊之前，最好能夠先獲得他們的信賴……)#k");
player.setLayerBlind(false, 0, 0, 0, 0, 500, 0);
// Unhandled Ingame Direction Event [22] Packet: 16 BC 02 00 00 
//player.setTemporarySkillSet(0);
player.setInGameDirectionMode(false, true, false, false);
npc.ui(1).uiMax().meFlip().next().sayX("#b(按照南哈特所說，現在馬上就說出我的身份似乎有些危險，我還是先獲得他們的信任吧)#k");
npc.ui(1).uiMax().meFlip().next().sayX("……我是在冒險島世界各地遊歷的冒險家。我聽說了凱梅爾茲的故事，就想來看看，卻在快要到達的時候遇到了風浪，差點就死了。幸好能夠安全到達了凱梅爾茲……");
npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("差點就出大事了啊。總之，只要來到凱梅爾茲的冒險家就是我們村子的客人。我能讓你舒舒服服地休息一下。你經歷了這麼大的事故，身體肯定也很疲憊了，你就當這裡是你自己家，好好休息吧。");
npc.ui(1).uiMax().meFlip().next().sayX("真的可以嗎？我們還是第一次見面呢，你相信我嗎？");
npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("哈哈哈,沒關係的。你到了我這個年紀就會知道,上了年紀的人挺有看人的眼光的。我看你的樣子就知道你是個善良的人。還有,你不是說你遇到了風浪嘛。我一輩子都與大海為伴,大海送來的客人,我怎麼能坐視不管呢,哈哈哈");
npc.ui(1).uiMax().meFlip().next().sayX("#b(……太好了，他似乎是個好人。既然這位是凱梅爾茲的村長，我們再積累一些信任，然後提出締結和約的事情就行了。)#k");
npc.completeQuest();
// Unhandled Stat Changed [CS_EXP] Packet: 00 00 00 00 01 00 00 00 00 00 BC B1 AA 00 00 00 00 00 FF 00 00 00 00 
player.gainExp(630724);