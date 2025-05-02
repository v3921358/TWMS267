let EXIT_MAP = 450012500;

let PHASE_1_DIR = 450013000;
let PHASE_1 = 450013100;

let PHASE_2_DIR = 450013200;
let PHASE_2 = 450013300;

let PHASE_3_DIR = 450013400;
let PHASE_3 = 450013500;

let PHASE_4_DIR = 450013600;
let PHASE_4 = 450013700;

let PHASE_Final = 450013750;

let DEATH_COUNT = 12;

let party;
let members;
let endTime;
let practiceMode;

function init(attachment) {
	[party, practiceMode] = attachment;
	event.initMap([PHASE_1_DIR, PHASE_1, PHASE_2_DIR, PHASE_2, PHASE_3_DIR, PHASE_3, PHASE_4, PHASE_4_DIR, PHASE_Final]);
	event.setPracticeMode(true);
	members = party.getLocalMembers();
	event.getMap(PHASE_1).reset();
	event.getMap(PHASE_2).reset();
	event.getMap(PHASE_3).reset();
	event.getMap(PHASE_4).reset();
	event.getMap(PHASE_Final).reset();
	event.startTimer("kick", 30 * 60 * 1000);
	endTime = new Date().getTime() + 30 * 60 * 1000;
	event.setVariable("members", members);
	event.setVariable("endTime", endTime);
	for (let i = 0; i < members.length; i++) {
		members[i].setEvent(event);
		members[i].changeMap(PHASE_1_DIR);
		members[i].setDeathCount(DEATH_COUNT);
	}
}


function mobDied(mob) {
	switch (mob.getDataId()) {
		case 8880500:
		case 8880501:
			event.getMap(PHASE_1).endFieldEvent();
			event.startTimer("To_Stage_2", 3000);
			break;
		case 8880502:
			event.getMap(PHASE_2).endFieldEvent();
			event.startTimer("To_Stage_3", 5000);
			break;
		case 8880503:
			event.getMap(PHASE_3).endFieldEvent();
			event.startTimer("To_Stage_4", 5000);
			break;
		case 8880519:
			event.getMap(PHASE_4).endFieldEvent();
			event.startTimer("To_Stage_final", 5000);
			break;
		case 8880518:
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
		case PHASE_4_DIR:
		case PHASE_4:
		case PHASE_Final:
			player.showTimer((endTime - new Date().getTime()) / 1000);
			player.showDeathCount();
			player.openUI(1204);
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
				members[i].changeMap(PHASE_2_DIR);
			}
			break;
		case "To_Stage_3":
			for (let i = 0; i < members.length; i++) {
				members[i].changeMap(PHASE_3_DIR);
			}
			break;
		case "To_Stage_4":
			for (let i = 0; i < members.length; i++) {
				members[i].changeMap(PHASE_4_DIR);
			}
			break;
		case "To_Stage_final":
			for (let i = 0; i < members.length; i++) {
				members[i].changeMap(PHASE_Final);
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
	event.getMap(PHASE_4).endFieldEvent();
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
