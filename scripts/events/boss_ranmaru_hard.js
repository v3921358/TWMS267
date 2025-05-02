let EXIT_MAP = 807300100;

let PHASE_1 = 807300210;
let PHASE_2 = 807300220;

let DEATH_COUNT = 5;

let party;
let members;
let endTime;
let practiceMode;



function init(attachment) {
	[party, practiceMode] = attachment;
	event.initMap([PHASE_1, PHASE_2]);
	event.setPracticeMode(true);
	members = party.getLocalMembers();
	event.getMap(PHASE_1).reset();
	event.getMap(PHASE_2).reset();
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
		case 9421577:
			event.startTimer("Spawn_P", 60 * 1000);
			break;
		case 9421578:
			event.startTimer("Spawn_B", 60 * 1000);
			break;
		case 9421579:
			event.startTimer("Spawn_R", 60 * 1000);
			break;
	}
}


function playerDisconnected(player) {
	removePlayer(player.getCharacterId(), false);
}


function playerChangedMap(player, destination) {
	switch (destination.getId()) {
		case PHASE_1:
		case PHASE_2:
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
	let map = event.getMap(FIELD_1);
	switch (key) {
		case "Spawn_P"://束縛的陰陽師
			let pCount = map.getEventMobCountById(9421577);
			if (pCount <= 0) {
				map.spawnMob(9421577, -824, -169);
			}
			break;
		case "Spawn_B"://治癒的陰陽師
			let bCount = map.getEventMobCountById(9421578);
			if (bCount <= 0) {
				map.spawnMob(9421578, -65, -135);
			}
			break;
		case "Spawn_R"://紅焰的陰陽師
			let rCount = map.getEventMobCountById(9421579);
			if (rCount <= 0) {
				map.spawnMob(9421579, -367, -298);
				event.startTimer("Spawn_Fire", 25 * 1000);
			}
			break;
		case "Spawn_Fire":
			if (map.getEventMobCountById(9421579) > 0 && fireCount < 15) {
				map.spawnMob(9421585, 545 - (fireCount * 120), 123);
				map.blowWeather(5120026, "紅焰的陰陽師召喚了巨大的火焰！", 4);
				fireCount++;
				event.startTimer("Spawn_Fire", 25 * 1000);
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
