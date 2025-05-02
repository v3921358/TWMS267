/**
 *
 *
 */

if (player.hasItem(4031343, 1) && player.hasItem(4031344, 1)) {
    let ret = npc.askYesNo("幹得漂亮, 你確定要進行4轉了嗎?");
    if (ret == 1) {
        if (player.canGainItem(1142110, 1)) {
            switch (player.getJob()) {
                case 111:
                    player.setJob(112);
                    break;
                case 121:
                    player.setJob(122);
                    break;
                case 131:
                    player.setJob(132);
                    break;
            }
            npc.completeQuest();
            player.gainSp(3);
            player.loseItem(4031343);
            player.loseItem(4031344);
            player.gainItem(1142110, 1);
            npc.next().sayX("你已經成功4轉了, 恭喜你！");
        } else {
            npc.next().sayX("你先整理下你的裝備欄，需要1個空格！");
        }
    } else {
        npc.next().sayX("考慮好後再來找我吧。");
    }
}