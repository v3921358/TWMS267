let EXIT_MAP = 240050500;

let PHASE_1 = 240060000;
let PHASE_2 = 240060100;
let PHASE_3 = 240060200;

let DEATH_COUNT = 5;

let party;
let members;
let endTime;
let practiceMode;



function init(attachment) {
	[party, practiceMode] = attachment;
	event.initMap([PHASE_1, PHASE_2, PHASE_3]);
	event.setPracticeMode(true);
	members = party.getLocalMembers();
	event.getMap(PHASE_1).reset();
	event.getMap(PHASE_2).reset();
	event.getMap(PHASE_3).reset();
	event.startTimer("kick", 30 * 60 * 1000);
	endTime = new Date().getTime() + 30 * 60 * 1000;
	event.setVariable("members", members);
	event.setVariable("endTime", endTime);
	for (let i = 0; i < members.length; i++) {
		members[i].setEvent(event);
		members[i].changeMap(PHASE_1);
		members[i].setDeathCount(DEATH_COUNT);
	}
}


function mobDied(mob) {
	switch (mob.getDataId()) {
		case 8810000:
			event.setVariable("boss1", true);
			break;
		case 8810001:
			event.setVariable("boss2", true);
			break;
		case 8810018:
			event.setVariable("boss3", true);
			break

	}
}


function playerDisconnected(player) {
	removePlayer(player.getCharacterId(), false);
}


function playerChangedMap(player, destination) {
	switch (destination.getId()) {
		case PHASE_1:
		case PHASE_2:
		case PHASE_3:
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
		case "Hontale":
			let map = event.getMap(PHASE_3);
			map.changeBGM("Bgm14/HonTale");
			let boss = map.makeMob(8810215);
			map.spawnMob(boss, 92, 260);
			break;
		case "kick":
			kick();
			break;
	}
}


function deinit() {
	event.getMap(PHASE_1).endFieldEvent();
	event.getMap(PHASE_2).endFieldEvent();
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
