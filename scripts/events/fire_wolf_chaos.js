let EXIT_MAP = 993000600;
let PHASE_1 = 993000510;
let endTime;
let actStage;
let time = 30 * 1000
/**
 * 內置hook函數
 * makeEvent時執行
 */
function init(attachment) {
	actStage = attachment
	event.startTimer("kick", time);
	endTime = new Date().getTime() + time;
	actStage.setEvent(event);
	actStage.changeMap(PHASE_1);
	actStage.setVariable("fire_wolf_damage", 0);
}
/**
 * 內置hook函數
 * 擊殺怪物後執行
 */
function mobDied(mob) {
	switch (mob.getDataId()) {
		case 9832023:
			// 擊殺後退場
			event.stopTimer("kick");
			event.startTimer("kick", 5 * 1000);
			// 發獎勵
			break;
	}
}
/**
 * 內置hook函數
 * 角色斷線執行
 */
function playerDisconnected(player) {
	player.setEvent(null);
}
/**
 * 內置hook函數
 * 角色更換地圖執行
 */
function playerChangedMap(player, destination) {
	if (destination.getId() != PHASE_1) {
		player.setEvent(null);
	} else {
		player.showTimer((endTime - new Date().getTime()) / 1000);
	}
}
/**
 * 內置hook函數
 * 隊伍減員執行
 */
function partyMemberDischarged(party, player) {
}
/**
 * 內置函數
 * event單例計時器
 */
function timerExpired(key) {
	switch (key) {
		case "kick":
			kick();
			break;
		case "leave":
			event.destroyEvent();
			actStage.setEvent(null)
			actStage.changeMap(EXIT_MAP)
			break;
	}
}
/**
 * 內置hook函數
 * 調用event.destroyEvent()時執行
 */
function deinit() {
	// event.destroyEvent();
}
/**
 * 內置hook函數
 * 角色死亡時執行
 */
function playerDied(player) {
}
/**
 * 內置hook函數
 * 角色進行攻擊時執行
 */
function playerHit(player, mob, damage) {
	sh.debug(damage)
	var allDamage = player.getVariable("fire_wolf_damage") + damage;
	sh.debug(allDamage)
	player.setVariable("fire_wolf_damage", allDamage);
}
/**
 * 內置hook函數
 * 角色撿起道具時執行
 */
function playerPickupItem(player, itemId) {
}
/**
 * 非內置函數
 * event中踢出某個角色
 */
function removePlayer(playerId, changeMap) {
}
/**
 * 非內置函數
 * event中踢出所有角色並銷毀event
 */
function kick() {
	actStage.setEvent(null)
	actStage.changeMap(EXIT_MAP)
}