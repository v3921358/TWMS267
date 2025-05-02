let EXIT_MAP = 350060300;

let PHASE_1_DIR = 350068000;
let PHASE_1 = 350068100;
let PHASE_2_DIR = 350068200;
let PHASE_2 = 350068300;
let PHASE_3_DIR = 350068400;
let PHASE_3 = 350068500;
let PHASE_4 = 350068600;

let DEATH_COUNT = 5;

let party;
let members;
let endTime;
let practiceMode;



function init(attachment) {
	[party, practiceMode] = attachment;
	event.initMap([PHASE_1_DIR, PHASE_1, PHASE_2_DIR, PHASE_2, PHASE_3_DIR, PHASE_3, PHASE_4]);
	event.setPracticeMode(true);
	members = party.getLocalMembers();
	event.getMap(PHASE_1).reset();
	event.getMap(PHASE_2).reset();
	event.getMap(PHASE_3).reset();
	event.getMap(PHASE_4).reset();
	event.startTimer("kick", 30 * 60 * 1000);
	endTime = new Date().getTime() + 30 * 60 * 1000;
	event.setVariable("members", members);
	event.setVariable("endTime", endTime);
	for (let i = 0; i < members.length; i++) {
		members[i].setEvent(event);
		members[i].changeMap(PHASE_1);
		// 動畫地圖
		// members[i].changeMap(PHASE_1_DIR);
		members[i].setDeathCount(DEATH_COUNT);
	}
}


function mobDied(mob) {
	switch (mob.getDataId()) {
		case 8881200:
			event.getMap(PHASE_1).endFieldEvent();
			event.startTimer("To_Stage_2", 3000);
			break;
		case 8881201:
			event.getMap(PHASE_2).endFieldEvent();
			event.startTimer("To_Stage_3", 5000);
			break;
		case 8881202:
			event.getMap(PHASE_3).endFieldEvent();
			event.startTimer("To_Stage_4", 5000);
			break;
	}
}


function playerDisconnected(player) {
	removePlayer(player.getCharacterId(), false);
}


function playerChangedMap(player, destination) {
	switch (destination.getId()) {
		case PHASE_1_DIR:
		case PHASE_1:
		case PHASE_2_DIR:
		case PHASE_2:
		case PHASE_3_DIR:
		case PHASE_3:
		case PHASE_4:
			player.showTimer((endTime - new Date().getTime()) / 1000);
			player.showDeathCount();
			break;
		default:
			removePlayer(player.getCharacterId(), false);
			break;
	}
}


function partyMemberDischarged(party, player) {
	if (party.getLeader() === player.getCharacterId()) {
		kick()
	} else {
		removePlayer(player.getCharacterId(), true)
	}
}


function timerExpired(key) {
	switch (key) {
		case "To_Stage_2":
			for (let i = 0; i < members.length; i++) {
				members[i].changeMap(PHASE_2);
			}
			break;
		case "To_Stage_3":
			for (let i = 0; i < members.length; i++) {
				members[i].changeMap(PHASE_3);
			}
			break;
		case "To_Stage_4":
			for (let i = 0; i < members.length; i++) {
				members[i].changeMap(PHASE_4);
			}
			break;
		case "kick":
			kick();
			break;
	}
}


function deinit() {
	event.getMap(PHASE_1).endFieldEvent();
	event.getMap(PHASE_2).endFieldEvent();
	event.getMap(PHASE_3).endFieldEvent();
}


function playerDied(player) {
}


function playerHit(player, mob, damage) {

}


function playerPickupItem(player, itemId) {
}



function removePlayer(playerId, changeMap) {
	for (let i = 0; i < members.length; i++) {
		if (members[i].getCharacterId() === playerId) {
			members[i].setDeathCount(-1)
			members[i].setEvent(null)
			if (changeMap)
				members[i].changeMap(EXIT_MAP)
			members.splice(i, 1)
			break
		}
	}
	if (members.length <= 0) {
		event.destroyEvent()
	}
}


function kick() {
	for (const member of members) {
		member.setDeathCount(-1)
		member.setEvent(null)
		member.changeMap(EXIT_MAP)
	}
	event.destroyEvent()
}
