// 
// Quest ID:17655
// [凱梅爾茲共和國] 冤枉的指控


npc.ui(1).uiMax().next().sayX("這是哪兒啊?啊,你是..!!.");
npc.next().sayX("這是離桑凱梅爾茲很遠的地方。");
npc.id(9390202).ui(1).npcFlip().next().sayX("謝謝你幫助我。每次我遇到考驗的時候你都給予了我幫助。");
npc.next().sayX("我並不是想幫你才那樣的。不過你會給我謝禮的吧?");
npc.id(9390202).ui(1).npcFlip().next().sayX("那當然啦。但是..你不能告訴我你到底是誰嗎?");
npc.ui(1).uiMax().next().sayX("你,且看你竟然會使用這麼高難度的快速移動魔法應該不是平凡人。");
npc.next().sayX("咳嗯......");
npc.completeQuest();
player.gainExp(1058907);
