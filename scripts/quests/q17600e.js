//
// Quest ID:17600
// [凱梅爾茲共和國] 南哈特的呼喚


npc.id(1540451).ui(1).uiMax().npcFlip().next().sayX("你來了啊，#b#e#h0#……#n#k\r\n女皇等了你很久了。請你快點去見她吧。");
npc.completeQuest();
player.startQuest(17710, 0);//Quest Name:第1章.從聖地出發……
// Unhandled Stat Changed [CS_EXP] Packet: 00 00 00 00 01 00 00 00 00 00 AF 9B 87 00 00 00 00 00 FF 00 00 00 00 
player.gainExp(556227);