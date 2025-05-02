/*     
 *
 *  功能：[拉克蘭]決戰
 *

 */

npc.ui(1).uiMax().meFlip().next().noEsc().sayX("現在真的結束了嗎？還是沒有？！！");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#這個城市的夢雖然還沒有消失，但目前看起來暫時結束了。");
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("之後要怎麼辦呢？");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#夢是夢，現實是現實。雖然不是馬上，但夢會漸漸變弱的。");
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("不過夢消失的話，你就……");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#天亮的時候，噩夢消散，這是不變的道理，。");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#居民們，成功救出了他們的話\r\n我的存在就完全有意義，所以就算消失也沒關係。");
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("……");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#當紅霧消散時，你也會為了使命而離開。");
npc.id(3003251).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#雖然是個艱辛的旅程，但總能平安結束的。");
player.startQuest(39360, 0, "1");
player.showProgressMessageFont("現在你可以在拉克蘭打獵獲取神祕徽章了。", 3, 20, 20, 0);//綠色的字
let newItem = player.makeItemWithId(1712003);
npc.broadcastGachaponMsgEx("[任務公告] 恭喜 " + player.getName() + " 完成了【神祕河-拉克蘭】，現在他可以去劇情任務介面進行領獎啦！" + newItem.getItemName(), newItem);
npc.completeQuest();