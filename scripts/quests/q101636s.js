/* NpcId.9010010, quest:101636, time:2024-06-19 23:33:19 */
npc.next().sayX("#h0#，你好？\r\n\r\n我收到了一封#e#r信#k#n，\r\n收件者好像是寫#e#b你的名字#n#k！");

if(npc.next().askYesNo("看了一下封面發現好像是#r#e邀請函#k#n，要親自打看看嗎？")) {
npc.next().sayX("好，給你邀請函。打開看看！");
npc.completeQuest();
player.openUI(1468);
}
