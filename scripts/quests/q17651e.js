// 
// Quest ID:17651
// [凱梅爾茲共和國] 僞裝成和平的威脅


npc.ui(1).uiMax().next().sayX("好的,您既然先開口提了,我也就直言相告了。");
npc.ui(1).uiMax().next().sayX("首先，凱梅爾茲共和國在和與海本王國建交的其他國家進行貿易時，只能交易我國指定的品目，並且我國要收取售價的1%作為酬金。");
npc.ui(1).uiMax().meFlip().next().sayX("你說什麼?那是什麼!!");
npc.id(9390203).ui(1).uiMax().npcFlip().next().sayX("等會,先聽完他說的。請繼續。");
npc.ui(1).uiMax().next().sayX("好的,第二點是,和我們海本交易時,有關交易包含的所有專案都必須由我們管理。");
npc.ui(1).uiMax().next().sayX("第三，現在貴國使用的航線實際上是在海本王國的管轄下，希望你們能夠支付航線使用的費用。");
npc.ui(1).uiMax().next().sayX("第四。兩國從今以後絕不相互猜忌並堅信兩國之間的友誼深厚。因此我們為了向凱梅爾茲共和國表達我們的誠意,我們將提供給能代表凱梅爾茲的首領親屬作為貴賓到我國留學的機會,可以花10年來學習我們的先進技術。");
npc.ui(1).uiMax().next().sayX("我說完了。您意下如何?");
npc.id(9390203).ui(1).uiMax().npcFlip().next().sayX("呃嗯... ");
let OBJECT_1 = player.spawnTempNpc(9390236, 130, 65, 1);//NpcName:OBJECT_1
player.destroyTempNpc(OBJECT_1);
player.setNpcSpecialAction(OBJECT_1, "summon", 0, false);
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("這是什麼無厘頭的話啊!!");
npc.ui(1).uiMax().next().sayX("嗯?這位是..?");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("我是凱梅爾茲共和國首領吉爾伯特·達尼爾拉的二兒子萊文·達尼爾拉!");
npc.ui(1).uiMax().next().sayX("見到你真的非常榮幸。關於萊文大人的傳聞我已是耳熟能詳了。優秀的船長...");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("吵死了!你膽敢在那裡玩花招啊!");
npc.ui(1).uiMax().next().sayX("耍花招,您這話說得太過分了吧。我是為了促進兩國之間的和平而來的。");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("和平?哈!讓我們成為你們這些傢伙的附屬國就是你所指的和平嗎!再不趕緊給我滾出去的話,我就讓你嚐嚐凱梅爾茲的厲害!");
npc.ui(1).uiMax().next().sayX("這這...可不能這樣啊。吉爾伯特首領。今天我們就先告辭了。明天再來拜見您。那時希望能聽到您的答覆。希望您會給我一個肯定的回答。");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("哼,放肆的傢伙。首領大人,我還有事沒有處理完先告辭了。");
player.destroyTempNpc(OBJECT_1);
npc.completeQuest();
player.gainExp(953667);