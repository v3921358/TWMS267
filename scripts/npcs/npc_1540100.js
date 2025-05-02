let sel = npc.askMenu("你想選擇何種送禮方式?\r\n" +   
    "#L2# #b推文獎勵箱 #k\r\n" +
    "#L3# #b3000單筆獎勵 #k\r\n#l" +
    "#L4# #b50000單筆獎勵 #k\r\n#l" +
	"#L5# #b10000單筆獎勵 #k\r\n#l" +
	"#L1# #b自定送禮 #k\r\n\r\n#l");


let rewardAccID = npc.askNumber("請輸入玩家帳號ID:", 1, 1, 32767); 
let counts = 1;  //固定數量為1

switch (sel) {
	
	case 1:
        let giftIItemId1 = npc.askNumber("請輸入道具ID:", 1, 1, 9999999999);
        let counts1 = npc.askNumber("請輸入道具數量:", 1, 1, 9999999999);
        let Message1 = "麥當勞歡樂送"; // 禮物信息
        player.sendAccReward(rewardAccID, giftIItemId1, counts1, Message1);
        npc.playerMessage(dsa, "已發送 " + Message1 + " 至玩家帳號ID " + rewardAccID1);
        break;
	
	case 2:
        let giftIItemId2 = 2431354; // 固定的禮物道具ID
        let Message2 = "推文獎勵箱"; // 固定的禮物信息
        player.sendAccReward(rewardAccID, giftIItemId2, counts, Message2);
		npc.playerMessage(5, " 送出了 " + Message2 + " 給玩家帳號ID " + rewardAccID);
        npc.playerMessage("已發送 " + Message2 + " 至玩家帳號ID " + rewardAccID2);
        break;
	
    case 3:
        let giftIItemId3 = 2434596; // 固定的禮物道具ID
        let Message3 = "3000單筆獎勵"; // 固定的禮物信息
        player.sendAccReward(rewardAccID, giftIItemId3, counts, Message3);
		npc.playerMessage(5, " 送出了 " + Message2 + " 給玩家帳號ID " + rewardAccID);
        npc.playerMessage("已發送 " + Message2 + " 至玩家帳號ID " + rewardAccID2);
        break;
      
    case 4:
        let giftIItemId4 = 2434596; // 固定的禮物道具ID
        let Message4 = "5000單筆獎勵"; // 固定的禮物信息
        player.sendAccReward(rewardAccID, giftIItemId4, counts, Message4);
		npc.playerMessage(5, " 送出了 " + Message2 + " 給玩家帳號ID " + rewardAccID);
        npc.playerMessage("已發送 " + Message3 + " 至玩家帳號ID " + rewardAccID3);
        break;
      
    case 5:
        let giftIItemId5 = 2434596; // 固定的禮物道具ID
        let Message5 = "10000單筆獎勵"; // 禮物信息
        player.sendAccReward(rewardAccID, giftIItemId5, counts, Message5);
		npc.playerMessage(5, " 送出了 " + Message2 + " 給玩家帳號ID " + rewardAccID);
        npc.playerMessage("已發送 " + Message4 + " 至玩家帳號ID " + rewardAccID4);
        break;
		
}
