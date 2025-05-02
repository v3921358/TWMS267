// ID:[220080100]
// MapName:時間塔的本源

let event;
switch (map.getId()) {
    case 220080100:
        event = player.getEvent("boss_papulatus_easy");
        if (event != null && event.getVariable("boss") == null) {
            event.setVariable("boss", true);
            map.changeBGM("Bgm09/TimeAttack");
            player.showSpouseMessage(6, `用時間裂縫碎片填補了玩具城動力室的時間裂縫。`)
            event.startTimer("Event_Action", 3 * 1000);
        }
        break;
    case 220080200:
        event = player.getEvent("boss_papulatus_normal");
        if (event != null && event.getVariable("boss") == null) {
            event.setVariable("boss", true);
            map.changeBGM("Bgm09/TimeAttack");
            player.showSpouseMessage(6, `用時間裂縫碎片填補了玩具城動力室的時間裂縫。`)
            event.startTimer("Event_Action", 3 * 1000);
        }
        break;
    case 220080300:
        event = player.getEvent("boss_papulatus_chaos");
        if (event != null && event.getVariable("boss") == null) {
            event.setVariable("boss", true);
            map.changeBGM("Bgm09/TimeAttack");
            player.showSpouseMessage(6, `用時間裂縫碎片填補了玩具城動力室的時間裂縫。`)
            event.startTimer("Event_Action", 3 * 1000);
        }
        break;
}
