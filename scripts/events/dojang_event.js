let EXIT_MAP = 925020002;

let FIELD_RESET = 925070000;

let STAGE_START = 925070100;
let STAGE_END = 925080000;

let TOTAL_TIME = 900;

let actStage;

let timeCountDown;
let stopEndCount;



function init(attachment) {

    event.makeMap(925070000);
    for (let i = 0; i < 100; i++) {
        event.makeMap(STAGE_START + i * 100);
    }
    actStage = attachment
    timeCountDown = TOTAL_TIME;

    actStage.updateQuestRecord(3847, "Nenter", sh.getStringDate("yy/MM/dd/HH/mm"));
    actStage.updateQuestRecord(3847, "NFloor", "0");
    actStage.updateQuestRecord(3847, "NResult", "start");
    actStage.startQuest(7279, 0, "1");//Quest Name:Unknown

    actStage.updateQuestRecordEx(65197, "");
    actStage.setEvent(event);
    let portalId = parseInt(Math.random() * 5);
    actStage.changeMap(FIELD_RESET, portalId);
}


function mobDied(mob) {
    let map = event.getMap(mob.getMapId());
    // dojang.respawn(mob, map);
    let diff = map.getId() - FIELD_RESET;
    let ss = parseInt(diff / 100);
    if (map.getEventMobCount() <= 0) {
        event.setVariable("Stage_" + ss, "1");

        map.setReactorState("door", 1);
        actStage.scriptProgressMessage("透過傳送點前往下一個關卡後，過關紀錄才會登錄在排名。");
        actStage.scriptProgressMessage("已擊敗對手，計時器將暫停計時10秒。");
        actStage.soundEffect("Dojang/clear");
        actStage.showScreenAutoLetterBox("dojang/end/clear", -1);

        stopEndCount = 10;
        event.stopTimer("CountDown");
        event.startTimer("StopTick", 1000);
        actStage.pauseShowTimer(true, timeCountDown, 10);
    }
}


function playerDisconnected(player) {
    player.setEvent(null);
    event.destroyEvent();
}


function playerChangedMap(player, destination) {
    if (destination.getId() == FIELD_RESET) {
        player.showTimer(timeCountDown, 0);
        player.pauseShowTimer(true, timeCountDown, 120);
        event.startTimer("StartUp", 2 * 60 * 1000);
    } else if (destination.getId() >= STAGE_START && destination.getId() <= STAGE_END) {
        let diff = destination.getId() - FIELD_RESET;
        let ss = parseInt(diff / 100 - 1);
        event.stopTimer("StopTick");//停止咕咕鐘倒計時
        event.startTimer("CountDown", 1000);
        let logTime = TOTAL_TIME - timeCountDown;
        if (ss > 0) {
            //Gain Point
            player.showSystemMessage("10點數已獲得。");
            let point = Number(player.getQuestRecord(3887, "point")) + 10;
            player.updateQuestRecord(3887, "point", point);

            //Update log
            let logFloor = Number(player.getQuestRecord(100464, "Floor"));
            let oldTime = Number(player.getQuestRecord(100464, "Time"));
            if (ss > logFloor) {
                player.updateQuestRecord(100464, "Floor", ss);
                player.updateQuestRecord(100464, "Time", logTime);
                player.showSystemMessage("已達成本週最高紀錄。");
            }
            if (ss == logFloor && oldTime < logTime) {
                player.updateQuestRecord(100464, "Time", logTime);
            }

            player.updateQuestRecord(3847, "NFloor", ss);
            player.updateQuestRecord(3847, "NTime", logTime);
        }
        player.showTimer(TOTAL_TIME, logTime);
        player.pauseShowTimer(false, timeCountDown, 0);
    } else {
        player.setEvent(null);
        event.destroyEvent();
    }
}


function partyMemberDischarged(party, player) {

}


function timerExpired(key) {
    switch (key) {
        case "StopTick":
            stopEndCount = Math.max(0, stopEndCount - 1);
            if (stopEndCount <= 5) {
                actStage.scriptProgressMessage(stopEndCount == 0 ? "咕咕鐘重新開始。" : stopEndCount + "秒後咕咕鐘將會重新開始。");
            }
            if (stopEndCount <= 0) {
                actStage.pauseShowTimer(false, timeCountDown, 0);
                event.startTimer("CountDown", 1000);
            } else {
                event.startTimer("StopTick", 1000);
            }
            break;
        case "CountDown":
            timeCountDown = Math.max(0, timeCountDown - 1);
            if (timeCountDown <= 0) {
                kick();
            } else {
                event.startTimer("CountDown", 1000);
            }
            break;
        case "StartUp":
            let portalId = parseInt(Math.random() * 5);
            actStage.changeMap(STAGE_START, portalId);
            break;
        case "kick":
            kick();
            break;
    }
}


function deinit() {
    actStage.setEvent(null);
}


function playerDied(player) {
}


function playerHit(player, mob, damage) {

}


function playerPickupItem(player, itemId) {
}



function removePlayer(playerId, changeMap) {
}


function kick() {
    actStage.setEvent(null);
    actStage.changeMap(EXIT_MAP, 0);
    event.destroyEvent();
}
