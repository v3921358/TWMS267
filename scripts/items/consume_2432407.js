var result = player.getModifyMileage();
if (result == 0) {
    player.modifyMileage(1);
    player.updateQuestRecord(18192, "val", "1");
    player.updateQuestRecord(18192, "tDate", "20/05/30/23/35");
    player.updateQuestRecord(18192, "last", "20/05/30");
    player.updateQuestRecord(18192, "count", "1");
    player.showSystemMessage(" 1 紅利積累！凌晨12點以前一定要在現金商城內清算。");
    player.showSystemMessage("[里程累積現況]共"+player.getModifyMileage()+"點數。");
    player.showSystemMessage("累積20次之後，進入現金商店來結算里程後累積。");
    player.showSystemMessage("今天沒有在現金商城內清算的紅利會在明天消失。積累當天晚上 11點 59分以前要到現金商城裡清算。");
    player.updateQuestRecord(18282, "count", "1");
    player.showSystemMessage("從里程包裹中獲得 1里程.");
    player.loseItem(2432407,1);
} else {
    player.modifyMileage(1);
    player.updateQuestRecord(18192, "val", "1");
    player.updateQuestRecord(18192, "tDate", "20/05/30/23/35");
    player.updateQuestRecord(18192, "last", "20/05/30");
    player.updateQuestRecord(18192, "count", "1");
    player.showSystemMessage(" 1 紅利積累！凌晨12點以前一定要在現金商城內清算。");
    player.showSystemMessage("[里程累積現況]共"+player.getModifyMileage()+"點數。");
    player.showSystemMessage("今天沒有在現金商城內清算的紅利會在明天消失。積累當天晚上 11點 59分以前要到現金商城裡清算。");
    player.loseItem(2432407,1);
}