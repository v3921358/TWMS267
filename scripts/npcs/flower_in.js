let recipientID = player.getAccountId();
let itemID = npc.askNumber("輸入itemID", 1, 1, 99999999);
let quantity = npc.askNumber("輸入數量", 1, 1, 99999999);


npc.sendAccReward(recipientID, itemID, quantity, "");
player.dropMessage(-1, "已發送"+message+"至玩家帳號ID " + recipientID);
player.dropMessage(5, "已發送"+message+"至玩家帳號ID ：【 " + recipientID + " 】！");

//player.addEdraSoul(20999)
//npc.gainItem(4009547, 1000)
//
///* 6轉 */
//player.completeQuest(38601)
//player.completeQuest(38602)
//player.completeQuest(38603)
//player.completeQuest(38604)
//player.completeQuest(38605)
//player.completeQuest(38606)
//player.completeQuest(38607)
//player.completeQuest(38608)
//player.completeQuest(38609)
//player.completeQuest(38612)
//player.completeQuest(38618)
//player.completeQuest(38619)
//player.completeQuest(38620)
//player.completeQuest(38621)
//player.completeQuest(38622)
//player.completeQuest(38623)
//player.completeQuest(1488)
//
//npc.say("恭喜完成6轉轉職！")
