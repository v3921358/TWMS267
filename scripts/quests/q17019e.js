// 
// Quest ID:17019
// 快來領取挑戰世界獎勵！&lt;4>


npc.ui(1).uiMax().next().noEsc().sayX("你已經完成了與多爾切地區的所有貿易啦!! ");
npc.ui(1).uiMax().next().noEsc().sayX("現在開啟貿易地圖的話,你會發現你能前往魯納地去了。 ");
npc.ui(1).uiMax().next().noEsc().sayX("希望你以後能透過去更多的地區貿易來幫助凱梅爾茲復興。");
player.gainExp(357844);
npc.completeQuest();