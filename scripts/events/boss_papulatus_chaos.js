let EXIT_MAP = 220080000;

let PHASE_1 = 220080200;

let DEATH_COUNT = 5;

let party;
let members;
let endTime;
let practiceMode;



function init(attachment) {
	[party, practiceMode] = attachment;
	event.initMap([PHASE_1]);
	event.setPracticeMode(true);
	members = party.getLocalMembers();
	event.getMap(PHASE_1).reset();
	event.getMap(PHASE_1).setNoSpawn(false)

	event.getMap(PHASE_1).setReactorState("boss", 0);
	event.getMap(PHASE_1).setReactorState("crack1", 1);
	event.getMap(PHASE_1).setReactorState("crack2", 1);
	event.getMap(PHASE_1).setReactorState("crack3", 1);
	event.getMap(PHASE_1).setReactorState("crack4", 1);
	event.getMap(PHASE_1).setReactorState("crack5", 1);
	event.getMap(PHASE_1).setReactorState("crack6", 1);

	event.startTimer("kick", 30 * 60 * 1000);
	endTime = new Date().getTime() + 30 * 60 * 1000;
	event.setVariable("members", members);
	event.setVariable("endTime", endTime);
	for (let i = 0; i < members.length; i++) {
		members[i].setEvent(event);
		members[i].changeMap(PHASE_1);
		members[i].showSpouseMessage(6, `用時間裂縫碎片填補了玩具城動力室的時間裂縫。`)
		members[i].setDeathCount(DEATH_COUNT);
	}
}


function mobDied(mob) {
	let map_1 = event.getMap(FIELD_1);
	switch (mob.getDataId()) {
		case 8500003:
		case 8500004:
			// let a = parseInt(Math.random() * 6);
			// map_1.dropItems(mob.getPosX(), mob.getPosY(), [2437659 + a, 999999]);
			break;
		case 8500007:
			// map_1.dropItems(mob.getPosX(), mob.getPosY(), [2437606, 999999]);
			break;
		case 8500008:
			// map_1.dropItems(mob.getPosX(), mob.getPosY(), [2437607, 999999]);
			break;
		case 8500012:
			map_1.startFieldEvent();
			break;

	}
}


function playerDisconnected(player) {
	removePlayer(player.getCharacterId(), false);
}


function playerChangedMap(player, destination) {
	switch (destination.getId()) {
		case PHASE_1:
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
		case "Event_Action":
			let field = event.getMap(FIELD_1);
			field.spawnMob(8500010, 0, 179);
			field.spawnMob(8500015, 445, 25);
			field.startFieldEvent();
			break;
		case "kick":
			kick();
			break;
	}
}


function deinit() {
	event.getMap(PHASE_1).endFieldEvent();
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
