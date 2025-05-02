/// <reference no-default-lib="true"/>
type byte = number
type short = number
type int = number
type long = number
type double = number
type Integer = number
type String = string
type Object = any
type List<K extends keyof any> = any
type Map<K extends keyof any, V> = {
    [P in K]: V;
} & {
    size(): number
    get(key: K): V | undefined
}

declare interface Point {
    x: number
    y: number
}

declare interface EventManipulator {

    setVariables(variables: Map<String, Object>): void;

    getVariable(key: String): Object;

    init(attachment: Object): void;

    playerDied(player: ScriptPlayer): void;

    playerDisconnected(player: ScriptPlayer): void;

    playerChangedMap(player: ScriptPlayer, map: ScriptField): void;

    partyMemberDischarged(player: ScriptPlayer): void;

    mobDied(mob: ScriptMob, player: ScriptPlayer): void;

    timerExpired(timerId: String): void;

    playerHit(player: ScriptPlayer, mob: ScriptMob, damage: long): void;

    playerPickupItem(player: ScriptPlayer, itemId: int): void;

    deinit(): void;

}

declare interface PlayerScriptInteraction extends ScriptBase {

    getNpc(): int;

    getEvent(): ScriptEvent;

    makeEvent(script: String, attachment: Object): ScriptEvent;

    getEvent(event: String): ScriptEvent;

    getChannelPlayers(): List<MapleCharacter>;

    resetRememberedMap(variable: String): int[];

    rememberMap(variable: String): void;

    getRememberedMap(variable: String): String;

    makeItemWithId(itemId: int): Item;

    setVariable(key: String, value: Object): void;

    getVariable(key: String): Object;

    httpGet(url: String): String;

    httpPost(url: String, body: String): String;

    startBossUI(BossType: int, difficulty: int[]): void;

    pecketToolUI(): int;

    guildDisband(guildId: int): void;

    sendPacket(): void;

    setDelay(delay: int): void;

    setSpirtValue(value: int): void;

    setSelMapLoad(): void;

    setSelMapLoadNext(): void;

    setSelMapLoadParty(): void;

    showHint(message: String, heigth: int, width: int): void;

    sendAccReward(accountId: int, itemId: int, amount: long, desc: String): MapleCharacter;

    sendAccRewardPeriod(accountId: int, day: int, itemId: int, amount: long, desc: String): MapleCharacter;

    getMonsterInfo(): MapleMonsterInformationProvider;

    getReactor(id: int): MapleReactor;

    getMonster(mobId: int): MapleMonster;

    getEliteMonster(id: int): MapleMonster;

    getEliteMonster(mobId: int, stats: MapleMonsterStats): MapleMonster;

    getEliteMonster(mobId: int, stats: MapleMonsterStats, eliteGrade: int): MapleMonster;

    getEliteMonster(mobId: int, stats: MapleMonsterStats, eliteGrade: int, eliteType: int): MapleMonster;

    getCurrentTime(): long;

    getAllHotTimeItems(): List<Pair<Integer, Integer>>;

    getRaffleMainReward(type: int): List<RaffleItem>;

    getItemInfo(): MapleItemInformationProvider;

    sendReward(accountId: int, characterId: int, start: long, end: long, itemId: int, amount: long, desc: String): MapleCharacter;

    runScript(scriptName: String): void;

    runScript(npcId: int, scriptName: String): void;

    showUnityPortal(): void;

    SayEldasMessage(Notice: String): void;

    eventSay(Notice: String): void;

    addEdraCount(Count: int): void;

    CreatGuildName(): void;

    playMusicBox(music: String): void;

    playMusicBox2(music: String): void;

    playMusicBox3(music: String): void;

    playMusicBox4(music: String): void;

    playMusicBox5(music: String): void;

    WeatherMessage(fieldMessage: String, type: int, ms: int): void;

    isQuestStarted(questId: int): boolean;

    isQuestFinished(questId: int): boolean;

    isQuestCompleted(questId: int): boolean;

    getQuest(): int;

    isStart(): boolean;

    forceStartQuest(): void;

    forceStartQuest(isWorldShare: boolean): void;

    forceStartQuest(customData: String): void;

    forceStartQuest(customData: String, isWorldShare: boolean): void;

    forceCompleteQuest(): void;

    forceCompleteQuest(questId: int): void;

    forceCompleteQuest(isWorldShare: boolean): void;

    resetQuest(): void;

    getQuestCustomData(): String;

    setQuestCustomData(customData: String): void;

    showCompleteQuestEffect(): void;

    spawnNpcForPlayer(npcId: int, x: int, y: int): void;

    gainGachaponItem(id: int, quantity: int): int;

    gainGachaponItem(id: int, quantity: int, msg: String): int;

    gainGachaponItem(id: int, quantity: int, msg: String, smega: boolean): int;

    gainGachaponItem(id: int, quantity: int, msg: String, rareness: int): int;

    gainGachaponItem(id: int, quantity: int, msg: String, rareness: int, period: long): int;

    gainGachaponItem(id: int, quantity: int, msg: String, rareness: int, buy: boolean): int;

    gainGachaponItem(id: int, quantity: int, msg: String, rareness: int, buy: boolean, period: long): int;

}

declare interface ScriptBase {

    broadcastNotice(message: String): void;

    broadcastNoticeWithoutPrefix(message: String): void;

    broadcastPlayerNotice(type: int, message: String): void;

    broadcastPopupSay(npcid: int, time: int, msg: String, sound: String): void;

    broadcastItemMessage(name: String, message: String, item: Item, color: int): void;

    customSqlInsert(sql: String, values: Object): void;

    customSqlUpdate(sql: String, values: Object): int;

    customSqlResult(sql: String, values: Object): List<Map<String, Object>>;

    addItemReward(itemId: int, quantity: int, message: String, duration: int): void;

    setGlobalVariable(key: String, value: Object): void;

    getGlobalVariable(key: String): Object;

    debug(s: Object): void;

}

declare interface ScriptEvent {

    getScriptInterface(): EventManipulator;

    getVariables(): Map<String, Object>;

    setVariable(key: String, value: Object): void;

    getVariable(key: String): Object;

    clearVariables(): void;

    makeMap(mapId: int): ScriptField;

    initMap(mapIds: int[]): void;

    getMap(mapId: int): ScriptField;

    destroyMap(map: ScriptField): void;

    destroyMaps(): void;

    startTimer(key: String, millisDelay: int): void;

    stopTimer(key: String): void;

    stopTimers(): void;

    getChannel(): int;

    destroyEvent(): void;

    runScript(player: ScriptPlayer, scriptName: String, npcId: int): void;

    broadcastWeatherEffectNotice(s: String, n: int, n2: int): void;

    getName(): String;

}

declare interface ScriptField {

    reset(): void;

    getMonsterSize(): int;

    startDojangRandTimer(sec: int, wait: int): void;

    spawnRune(type: int): void;

    reset(level: int): void;

    overrideFieldLimit(var: int): void;

    showWeatherEffectNotice(msg: String, type: int, duration: int): void;

    changeBGM(name: String): void;

    clearMobs(): void;

    clearDrops(): void;

    createObtacleAtom(count: int, type1: int, type2: int, DamageRang: int, SpeedRang: int): void;

    destroyTempNpc(npcId: int): void;

    spawnTempNpc(npcId: int, x: int, y: int): void;

    getId(): int;

    getInstanceId(): int;

    getNumPlayersInArea(id: int): int;

    getPlayerCount(): int;

    makeMob(mobId: int): ScriptMob;

    spawnMob(mobId: int, x: int, y: int): void;

    spawnMob(Mob: ScriptMob, x: int, y: int): void;

    portalEffect(name: String, i: int): void;

    resetMobsSpawns(): void;

    screenEffect(name: String): void;

    scriptProgressMessage(msg: String): void;

    setNoSpawn(value: boolean): void;

    showTimer(seconds: double): void;

    closeTimer(): void;

    getPlayers(): List<ScriptPlayer>;

    endFieldEvent(): void;

    getName(): void;

    setReactorState(name: String, state: byte): void;

    getReactorStateId(var1: String): int;

    getEventMobCount(): int;

    getEventMobCountById(mobId: int): int;

    blowWeather(itemId: int, msg: String, time: int): void;

    blowWeather(itemId: int, msg: String): void;

    getEvent(): ScriptEvent;

    setEvent(event: ScriptEvent): void;

    getCharacters(): List<MapleCharacter>;

    startFieldEvent(): void;

    startDemianField(): void;

    startLucidField(): void;

    startDuskField(): void;

    startJinField(): void;

    startAngelField(): void;

    startWillField(): void;

    startSerenField(): void;

    startKalosField(): void;

    startBlackMageField(): void;

    startBlackMageField_II(): void;

    startBlackMageField_III(): void;

    startBlackMageField_IV(): void;

    StartKarNingField(): void;

}

declare interface ScriptGuild {

    getId(): int;

    getName(): String;

    getCapacity(): int;

    getLevel(): int;

    GainGP(diff: int): void;

    getSkillLevel(skillId: int): int;

    increaseCapacity(amount: int): boolean;

}

declare interface ScriptHelper extends ScriptBase {

    newPoint(x: int, y: int): Point;

    getItemName(itemId: int): String;

    itemExists(itemId: int): boolean;

    itemEquip(equipId: int): Equip;

    formatDate(format: String): String;

    formatDate(timestamp: long, format: String): String;

    formatDate(date: Date, format: String): String;

    getBytes(str: String): byte[];

    shutdown(time: int): void;

    worldBroadcastNotice(notice: String): void;

    worldBroadcastMessage(notice: String): void;

    channelBroadcastItemMessage(channelID: int, message: String, itemId: int, time: int): void;

    channelBroadcastNotice(channelID: int, notice: String): void;

    channelBroadcastMessage(channelID: int, notice: String): void;

    randInt(arg0: int): int;

    getStringDate(format: String): String;

    getStringDate(timestamp: long, format: String): String;

    getWeekStart(timestamp: long): long;

    getWeekEnd(timestamp: long): long;

}

declare interface ScriptItem {

    copy(): Item;

    getItemId(): int;

    setItemId(id: int): void;

    getItemType(): byte;

    asEquip(): Equip;

    asPet(): MaplePet;

    asFamiliarCard(): FamiliarCard;

    getAttribute(): int;

    setAttribute(attribute: int): void;

    getQuantity(): short;

    setQuantity(quantity: short): void;

    getDateExpire(): long;

    setExpiration(expire: long): void;

    getSN(): int;

    setSN(sn: int): void;

    getInventoryId(): long;

    setInventoryId(ui: long): void;

    isTradeAvailable(): boolean;

    isTradeBlock(): boolean;

    isAccountSharable(): boolean;

    getItemName(): String;

    isCash(): boolean;

    getOption(pos: int): int;

    setOption(pos: int, option: int): void;

}

declare interface ScriptMob {

    getMob(): MapleMonster;

    changeBaseHp(maxHp: long): void;

    getDataId(): int;

    getEntityId(): int;

    getHp(): long;

    setHp(maxHp: long): void;

    getMapId(): int;

    getMaxHp(): long;

    setMaxHp(maxHp: long): void;

    getLevel(): int;

    setForcedMobStat(level: int, rate: int): void;

    setHpLimitPercent(hpLimitPercent: double): void;

    getForcedMobStat(): ForcedMobStat;

    setZoneDataType(nCurZoneDataType: int): void;

    heal(heal: long): void;

    disableSpawnRevives(): void;

    getHPPercent(): int;

    setReduceDamageR(reduceDamageR: double): void;

    getPosition(): Point;

    disableDrops(): void;

    setInvincible(invincible: boolean): void;

    setRemoveAfter(var1: int): void;

    setAppearType(var1: short): void;

}

declare interface ScriptNpc extends PlayerScriptInteraction {

    getNpcId(): int;

    getItemId(): int;

    sendScriptMessage(text: String, nmt: int): Object;

    sendGeneralSay(text: String, nmt: int, hasNext: boolean): int;

    sendGeneralSay(message: String, nmt: int): int;

    askQuiz(type: byte, title: String, problemText: String, hintText: String, min: int, max: int, time: int): String;

    askSpeedQuiz(type: byte, quizType: int, answer: int, correctAnswers: int, remaining: int, time: int): String;

    askText(message: String, defaultText: String, minLength: short, maxLength: short): String;

    askBoxText(def: String, columns: short, rows: short): String;

    askNumber(message: String, def: long, min: long, max: long): long;

    askPet(message: String, list: List<Item>): int;

    askAvatar(message: String, itemId: int, secondLookValue: int, srcBeauty: int, styles: int[]): int;

    askAvatarZero(message: String, itemId: int, srcBeauty: int, srcBeauty2: int, styles: int[], styles2: int[]): int;

    askAngelicBuster(): boolean;

    askAvatarMixColor(cardID: int, msg: String, secondLookValue: int, srcBeauty: int): int;

    askAvatarRandomMixColor(msg: String): boolean;

    askAvatarRandomMixColor(itemID: Integer, secondLookValue: Integer, msg: String): boolean;

    sayAvatarMixColorChanged(msg: String, srcBeauty: int, drtBeauty: int, srcBeauty2: int, drtBeauty2: int): int;

    sayAvatarMixColorChanged(msg: String, itemID: Integer, srcBeauty: int, drtBeauty: int, srcBeauty2: int, drtBeauty2: int): int;

    askConfirmAvatarChange(srcBeauty: int, srcBeauty2: int): boolean;

    askConfirmAvatarChange(itemID: Integer, secondLookValue: Integer, srcBeauty: int, srcBeauty2: int): boolean;

    sendOkN(text: String): int;

    sayN(text: String): int;

    sendOkN(text: String, idd: int): int;

    sayN(text: String, idd: int): int;

    sendOkS(text: String, type: byte): int;

    sayS(text: String, type: byte): int;

    sendOkS(text: String, type: byte, idd: int): int;

    sendPlayerToNpc(text: String): int;

    sayPlayerToNpc(text: String): int;

    sendNextS(text: String, type: byte): int;

    sayNextS(text: String, type: byte): int;

    sendNextS(text: String, type: byte, idd: int): int;

    sendNextN(text: String): int;

    sayNextN(text: String): int;

    sendNextN(text: String, type: byte, idd: int): int;

    sendPrevS(text: String, type: byte): int;

    sayPrevS(text: String, type: byte): int;

    sendPrevS(text: String, type: byte, idd: int): int;

    sendPrevN(text: String): int;

    sayPrevN(text: String): int;

    sendPrevN(text: String, type: byte): int;

    sayPrevN(text: String, type: byte): int;

    sendPrevN(text: String, type: byte, idd: int): int;

    PlayerToNpc(text: String): int;

    sendNextPrevS(text: String, type: byte): int;

    sayNextPrevS(text: String, type: byte): int;

    sendNextPrevS(text: String, type: byte, idd: int): int;

    sendNextPrevN(text: String): int;

    sayNextPrevN(text: String): int;

    sendNextPrevN(text: String, type: byte): int;

    sayNextPrevN(text: String, type: byte): int;

    sendNextPrevN(text: String, type: byte, idd: int): int;

    sendAcceptDecline(text: String): int;

    sayAcceptDecline(text: String): int;

    sendAcceptDeclineNoESC(text: String): int;

    sayAcceptDeclineNoESC(text: String): int;

    askAcceptDecline(text: String): int;

    askAcceptDecline(text: String, id: int): int;

    askAcceptDeclineNoESC(text: String): int;

    askAcceptDeclineNoESC(text: String, id: int): int;

    askMapSelection(sel: String): int;

    sendSimple(text: String): int;

    sendSimple(text: String, id: int): int;

    sendSimpleS(text: String, type: byte): int;

    sendSimpleS(text: String, type: byte, idd: int): int;

    sendSimpleN(text: String): int;

    sendSimpleN(text: String, type: byte, idd: int): int;

    askAvatar(text: String, styles: int[], card: int, isSecond: boolean): int;

    sendStyle(text: String, styles: int[], card: int, isSecond: boolean): int;

    sendAStyle(text: String, styles: int[], card: int): int;

    sendGetNumber(text: String, def: long, min: long, max: long): int;

    sendGetText(text: String): String;

    sendGetText(text: String, id: int): String;

    sendPlayerOk(text: String): int;

    sendPlayerOk(text: String, type: byte, npcId: int): int;

    sendPlayerPrev(text: String, type: byte, npcId: int): int;

    sendPlayerNext(text: String): int;

    sendPlayerNext(text: String, type: byte, npcId: int): int;

    sendPlayerNextPrev(text: String): int;

    sendPlayerNextPrev(text: String, type: byte, npcId: int): int;

    sendRevivePet(text: String): int;

    sendPlayerStart(text: String): int;

    sendSlideMenu(type: int, sel: String): int;

    getSlideMenuSelection(type: int): String;

    getSlideMenuDataIntegers(type: int, selection: int): int[];

    sendOk(s: String): int;

    say(s: String): int;

    sendOk(s: String, n: int): int;

    say(s: String, n: int): int;

    sendOk(message: String, bLeft: boolean): int;

    say(message: String, bLeft: boolean): int;

    sendOkNoESC(message: String): int;

    sayNoESC(message: String): int;

    sendOkNoESC(message: String, bLeft: boolean): int;

    sayNoESC(message: String, bLeft: boolean): int;

    sendOkS(s: String): int;

    sayS(s: String): int;

    sendOkS(s: String, b: boolean): int;

    sayS(s: String, b: boolean): int;

    sendOkE(s: String): int;

    sayE(s: String): int;

    sendOkE(s: String, n: int): int;

    sayE(s: String, n: int): int;

    sendOkENoESC(s: String): int;

    sayENoESC(s: String): int;

    sendOkENoESC(s: String, n: int): int;

    sayENoESC(s: String, n: int): int;

    sayENoESC(s: String, n: int, n2: int): int;

    sendNext(s: String): int;

    sayNext(s: String): int;

    sendNext(s: String, n: int): int;

    sayNext(s: String, n: int): int;

    sendNext(s: String, b: boolean): int;

    sayNext(s: String, b: boolean): int;

    sendNextNoESC(s: String): int;

    sayNextNoESC(s: String): int;

    sendNextNoESC(s: String, b: boolean): int;

    sayNextNoESC(s: String, b: boolean): int;

    sendNextNoESC(s: String, n: int): int;

    sayNextNoESC(s: String, n: int): int;

    sendNextS(s: String): int;

    sayNextS(s: String): int;

    sendNextS(s: String, b: boolean): int;

    sayNextS(s: String, b: boolean): int;

    sendNextSNoESC(s: String): int;

    sayNextSNoESC(s: String): int;

    sendNextE(s: String): int;

    sayNextE(s: String): int;

    sendNextE(s: String, n: int): int;

    sayNextE(s: String, n: int): int;

    sendNextENoESC(s: String): int;

    sayNextENoESC(s: String): int;

    sendNextENoESC(s: String, n: int): int;

    sayNextENoESC(s: String, n: int): int;

    sendNextENoESC(s: String, n: int, n2: int): int;

    sayNextENoESC(s: String, n: int, n2: int): int;

    sendPrev(s: String): int;

    sayPrev(s: String): int;

    sendPrevS(s: String): int;

    sayPrevS(s: String): int;

    sendPrevE(s: String): int;

    sayPrevE(s: String): int;

    sendPrevE(s: String, n: int): int;

    sayPrevE(s: String, n: int): int;

    sendPrevENoESC(s: String): int;

    sayPrevENoESC(s: String): int;

    sendPrevENoESC(s: String, n: int): int;

    sayPrevENoESC(s: String, n: int): int;

    sendPrevENoESC(s: String, n: int, n2: int): int;

    sayPrevENoESC(s: String, n: int, n2: int): int;

    sendNextPrev(s: String): int;

    sayNextPrev(s: String): int;

    sendNextPrev(s: String, b: boolean): int;

    sayNextPrev(s: String, b: boolean): int;

    sendNextPrev(s: String, n: int): int;

    sayNextPrev(s: String, n: int): int;

    sendNextPrevNoESC(s: String): int;

    sayNextPrevNoESC(s: String): int;

    sendNextPrevNoESC(s: String, b: boolean): int;

    sayNextPrevNoESC(s: String, b: boolean): int;

    sendNextPrevNoESC(s: String, n: int): int;

    sayNextPrevNoESC(s: String, n: int): int;

    sendNextPrevS(s: String): int;

    sayNextPrevS(s: String): int;

    sendNextPrevS(s: String, b: boolean): int;

    sayNextPrevS(s: String, b: boolean): int;

    sendNextPrevSNoESC(s: String): int;

    sayNextPrevSNoESC(s: String): int;

    sendNextPrevSNoESC(s: String, b: boolean): int;

    sayNextPrevSNoESC(s: String, b: boolean): int;

    sendNextPrevE(s: String): int;

    sayNextPrevE(s: String): int;

    sendNextPrevE(s: String, n: int): int;

    sayNextPrevE(s: String, n: int): int;

    sendNextPrevENoESC(s: String): int;

    sayNextPrevENoESC(s: String): int;

    sendNextPrevENoESC(s: String, n: int): int;

    sayNextPrevENoESC(s: String, n: int): int;

    sendNextPrevENoESC(s: String, n: int, n2: int): int;

    sayNextPrevENoESC(s: String, n: int, n2: int): int;

    askReplace(s: String): int;

    askYesNo(s: String): int;

    askYesNo(s: String, n: int): int;

    askYesNo(s: String, b: boolean): int;

    askYesNoNoESC(s: String): int;

    askYesNoNoESC(s: String, b: boolean): int;

    askYesNoS(s: String): int;

    askYesNoS(s: String, b: boolean): int;

    askYesNoE(s: String): int;

    askYesNoE(s: String, n: int): int;

    askMenu(s: String): int;

    askMenu(s: String, n: int): int;

    askMenu(s: String, b: boolean): int;

    askMenuNoESC(s: String): int;

    askMenuNoESC(s: String, b: boolean): int;

    askMenuNoESC(s: String, n: int): int;

    askMenuS(s: String): int;

    askMenuE(s: String): int;

    askMenuE(s: String, b: boolean): int;

    askMenuA(s: String): int;

    askMenuA(s: String, b: boolean): int;

    askMenuA(msg: String, diffnpc: int): int;

    askAccept(s: String): int;

    askAccept(msg: String, diffNpcID: int): int;

    askAccept(s: String, bLeft: boolean): int;

    askAcceptNoESC(s: String): int;

    askAcceptNoESC(s: String, b: boolean): int;

    askAcceptS(s: String): int;

    askAcceptE(s: String): int;

    askText(s: String, n: short, n2: short): void;

    askTextNoESC(s: String, n: short, n2: short): void;

    askTextNoESC(s: String, def: String, n: short, n2: short): String;

    askTextS(s: String, n: short, n2: short): String;

    askTextS(s: String, def: String, n: short, n2: short): String;

    askTextE(s: String, n: short, n2: short): String;

    askTextE(s: String, def: String, n: short, n2: short): String;

    askNumber(s: String, n: int, n2: int, n3: int): int;

    askNumberKeypad(n: int): int;

    askUserSurvey(n: int, s: String): int;

    askNumberS(s: String, n: int, n2: int, n3: int): int;

    askNumberE(s: String, n: int, n2: int, n3: int): int;

    askBoxText(s: String, s2: String, n: short, n2: short): String;

    askBoxTextS(s: String, s2: String, n: short, n2: short): String;

    askBoxTextE(s: String, s2: String, n: short, n2: short): String;

    askSlideMenu(n: int, s: String): int;

    askAvatar(message: String, array: int[], needItem: int, isangel: boolean, isbeta: boolean): int;

    askAndroid(s: String, array: int[], n: int): int;

    askPetRevive(s: String): int;

    getAllPetItem(): List<Item>;

    askSelectMenu(n: int): int;

    askSelectMenu(n: int, n2: int, array: String[]): int;

    askPetEvolution(s: String, list: List<Item>): int;

    askPetAll(s: String): int;

    askPetAll(s: String, list: List<Item>): int;

    sayImage(array: String[]): int;

    askQuiz(b: boolean, n: int, n2: int, s: String, s2: String, s3: String): String;

    askSpeedQuiz(b: boolean, n: int, n2: int, n3: int, n4: int, n5: int): String;

    askICQuiz(b: boolean, s: String, s2: String, n: int): String;

    askOlympicQuiz(b: boolean, n: int, n2: int, n3: int, n4: int, n5: int): String;

    sendGetText(text: String, def: String, col: int, line: int): String;

    sendOkIllu(s: String, n: int, n2: int, b: boolean): int;

    sayIllu(s: String, n: int, n2: int, b: boolean): int;

    sendOkIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sayIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sendNextIllu(s: String, n: int, n2: int, b: boolean): int;

    sayNextIllu(s: String, n: int, n2: int, b: boolean): int;

    sendNextIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sayNextIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sendPrevIllu(s: String, n: int, n2: int, b: boolean): int;

    sayPrevIllu(s: String, n: int, n2: int, b: boolean): int;

    sendPrevIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sayPrevIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sendNextPrevIllu(s: String, n: int, n2: int, b: boolean): int;

    sayNextPrevIllu(s: String, n: int, n2: int, b: boolean): int;

    sendNextPrevIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sayNextPrevIlluNoESC(s: String, n: int, n2: int, b: boolean): int;

    sendSay(type: byte, npcId: int, n3: int, u2: int, bPrev: boolean, bNext: boolean, sText: String, n5: int): int;

    sendOk(s: String, n: int, j906: ScriptParam, bLeft: boolean): int;

    sendOkS(s: String, j906: ScriptParam, b: boolean): int;

    sendOkE(msg: String, npcId: int, j906: ScriptParam, b: boolean, n2: int): int;

    sendNext(s: String, n: int, j906: ScriptParam, b: boolean): int;

    sendNextS(s: String, j906: ScriptParam, b: boolean): int;

    sendNextE(s: String, n: int, j906: ScriptParam, b: boolean, n2: int): int;

    sendPrev(s: String, n: int, j906: ScriptParam, b: boolean): int;

    sendPrevS(s: String, j906: ScriptParam, b: boolean): int;

    sendPrevE(s: String, n: int, j906: ScriptParam, b: boolean, n2: int): int;

    sendNextPrev(s: String, n: int, j906: ScriptParam, b: boolean): int;

    sendNextPrevS(s: String, j906: ScriptParam, b: boolean): int;

    sendNextPrevE(s: String, n: int, j906: ScriptParam, b: boolean, n2: int): int;

    askYesNo(b: byte, n: int, n2: int, n3: int, s: String): int;

    askYesNo(s: String, n: int, j906: ScriptParam, b: boolean): int;

    sendYesNo(text: String): int;

    sendYesNo(text: String, idd: int): int;

    sendYesNoS(text: String, type: byte): int;

    sendYesNoS(text: String, type: byte, idd: int): int;

    sendYesNoN(text: String): int;

    sendYesNoN(text: String, idd: int): int;

    askYesNoS(s: String, j906: ScriptParam, b: boolean): int;

    askYesNoE(s: String, n: int, j906: ScriptParam, b: boolean): int;

    askMenu(b: byte, n: int, diffnpc: int, n3: int, s: String): int;

    askMenu(s: String, n: int, j906: ScriptParam, b: boolean): int;

    askMenuS(s: String, j906: ScriptParam, b: boolean): int;

    askMenuE(s: String, n: int, j906: ScriptParam, b: boolean): int;

    askMenuA(s: String, diffnpc: int, j906: ScriptParam, b: boolean): int;

    askAccept(b: byte, n: int, diffnpc: int, n3: int, s: String): int;

    askAccept(s: String, diffnpc: int, j906: ScriptParam, b: boolean): int;

    askAcceptS(s: String, j906: ScriptParam, b: boolean): int;

    askAcceptE(s: String, diffnpc: int, j906: ScriptParam, b: boolean): int;

    askText(b: byte, n: int, n2: int, n3: int, nLenMin: short, nLenMax: short, sMsg: String, sMsgDefault: String): String;

    askText(sMsg: String, n: int, j906: ScriptParam, nLenMin: short, nLenMax: short, bLeft: boolean, sMsgDefault: String): String;

    askTextS(s: String, n: short, n2: short, b: boolean, sMsgDefault: String): String;

    askTextE(s: String, n: short, n2: short, b: boolean, sMsgDefault: String): String;

    askNumber(b: byte, n: int, n2: int, n3: int, n4: long, n5: long, n6: long, s: String): int;

    askNumber(s: String, n: int, n2: int, n3: int, n4: int, b: boolean): int;

    askNumberS(s: String, n: int, n2: int, n3: int, b: boolean): int;

    askNumberE(s: String, n: int, n2: int, n3: int, b: boolean): int;

    askBoxText(b: byte, n: int, n2: int, n3: int, n4: short, n5: short, s: String, s2: String): String;

    askBoxText(s: String, s2: String, n: int, n2: short, n3: short, b: boolean): String;

    askBoxTextS(s: String, s2: String, n: short, n2: short, b: boolean): String;

    askBoxTextE(s: String, s2: String, n: short, n2: short, b: boolean): String;

    askSlideMenu(b: byte, n: int, n2: int, n3: int, s: String): int;

    askAvatar(b: byte, n: int, n2: int, b2: boolean, b3: boolean, array: int[], s: String): int;

    askAvatarZero(cardID: int, array: int[], array2: int[], s: String): void;

    askAvatarZero(nSpeakerTypeID: byte, nSpeakerTemplateID: int, cardID: int, array: int[], array2: int[], s: String): int;

    askAndroid(cardID: int, array: int[], s: String): void;

    askAndroid(nSpeakerTypeID: byte, nSpeakerTemplateID: int, cardID: int, array: int[], s: String): int;

    askPet(b: byte, n: int, list: List<Item>, s: String): int;

    askSelectMenu(b: byte, n: int, n2: int, array: String[]): int;

    askPetEvolution(b: byte, n: int, list: List<Item>, s: String): int;

    askPetAll(b: byte, n: int, list: List<Item>, s: String): int;

    sayImage(b: byte, n: int, n2: int, n3: short, array: String[]): int;

    sendSayIllu(b: byte, n: int, n2: int, n3: int, b2: boolean, b3: boolean, s: String, n4: int, n5: int, b4: boolean): int;

    sendOkIllu(s: String, n: int, j906: ScriptParam, b: boolean, n2: int, n3: int, b2: boolean): int;

    sendNextIllu(s: String, n: int, j906: ScriptParam, b: boolean, n2: int, n3: int, b2: boolean): int;

    sendPrevIllu(s: String, n: int, j906: ScriptParam, b: boolean, n2: int, n3: int, b2: boolean): int;

    sendNextPrevIllu(s: String, n: int, j906: ScriptParam, b: boolean, n2: int, n3: int, b2: boolean): int;

    askQuiz(b: byte, n: int, n2: int, n3: int, b2: boolean, n4: int, n5: int, s: String, s2: String, s3: String): String;

    askSpeedQuiz(b: byte, n: int, n2: int, n3: int, b2: boolean, n4: int, n5: int, n6: int, n7: int, n8: int): String;

    askICQuiz(b: byte, n: int, n2: int, n3: int, b2: boolean, s: String, s2: String, n4: int): String;

    askOlympicQuiz(b: byte, n: int, n2: int, n3: int, b2: boolean, nType: int, nQuestion: int, nCorrect: int, nRemain: int, tRemainInitialQuiz: int): String;

    askNumberKeypad(b: byte, n: int, n2: int, n3: int, n4: int): int;

    askUserSurvey(b: byte, n: int, n2: int, n3: int, n4: int, s: String): int;

    id(npcId: int): ScriptNpc;

    overrideSpeakerId(npcId: int): ScriptNpc;

    noEsc(): ScriptNpc;

    me(): ScriptNpc;

    npcFlip(): ScriptNpc;

    meFlip(): ScriptNpc;

    npcRightSide(): ScriptNpc;

    replace(): ScriptNpc;

    line(line: int): ScriptNpc;

    ui(ui: int): ScriptNpc;

    uiMax(): ScriptNpc;

    prev(): ScriptNpc;

    next(): ScriptNpc;

    defText(defText: String): ScriptNpc;

    minLen(minLen: int): ScriptNpc;

    maxLen(maxLen: int): ScriptNpc;

    defNum(defNum: int): ScriptNpc;

    minNum(minNum: int): ScriptNpc;

    maxNum(maxNum: int): ScriptNpc;

    time(time: int): ScriptNpc;

    delay(delay: int): ScriptNpc;

    cardId(cardId: int): ScriptNpc;

    col(col: int): ScriptNpc;

    styles(styles: int[]): ScriptNpc;

    styles2(styles: int[]): ScriptNpc;

    defSel(defSel: int): ScriptNpc;

    speakerType(speakerType: int): ScriptNpc;

    items(list: List<Item>): ScriptNpc;

    setSrcBeautyX(setSrcBeauty: int): ScriptNpc;

    setSrcBeautyX2(setSrcBeauty: int): ScriptNpc;

    secondLookValueX(secondLookValue: int): ScriptNpc;

    sayX(text: String): void;

    sayImageX(text: String[]): void;

    askTextX(text: String): String;

    askBoxTextX(text: String): String;

    askMenuX(text: String): int;

    askYesNoX(text: String): boolean;

    askAcceptX(text: String): boolean;

    askNumberX(message: String): long;

    askAngelicBusterX(): boolean;

    askAvatarX(message: String): int;

    askPetX(message: String): long;

    askAndroidX(message: String): long;

    askAvatarZeroX(message: String): int;

    askAvatarMixColorX(msg: String): int;

    askAvatarRandomMixColorX(msg: String): boolean;

    sayAvatarMixColorChangedX(msg: String): int;

    askConfirmAvatarChangeX(): boolean;

    resetNpc(): void;

    openWebUI(n: int, sUOL: String, sURL: String): void;

    openWeb(sURL: String): void;

    createParty(): void;

    disbandParty(): void;

    startQuest(): void;

    completeQuest(): void;

    makeEvent(script: String, attachment: Object): ScriptEvent;

    getPosition(): short;

    used(): boolean;

    used(q: int): boolean;

    showPopupSay(npcid: int, time: int, msg: String, sound: String): void;

    getItemInfo(): MapleItemInformationProvider;

    getSearchData(type: int, search: String): Map<Integer, String>;

    searchData(type: int, search: String): String;

    saying(message: String): void;

}

declare interface ScriptParty {

    getId(): int;

    changeMap(mapId: int): void;

    changeMap(mapId: int, portal: int): void;

    changeMap(map: ScriptField): void;

    getLocalMembers(): List<ScriptPlayer>;

    getLeader(): ScriptPlayer;

    getMembersCount(): int;

    getMembersCount(minLevel: int, maxLevel: int): int;

    isAllMembersAllowedPQ(pqLog: String, maxEnter: int): boolean;

    getNotAllowedMember(pqLog: String, maxEnter: int): ScriptPlayer;

    isAllMembersHasItem(itemId: int, quantity: int): boolean;

    getNotHasItemMember(itemId: int, quantity: int): String;

    loseItem(itemId: int, quantity: int): void;

    loseItem(itemId: int): void;

}

declare interface ScriptPlayer extends PlayerScriptInteraction {

    gainErda(itemid: int): void;

    teachskill(skillid: int, level: int): void;

    addEdraSoul(amount: int): void;

    addByItem(item: Item): void;

    setFace(face: int): void;

    setHair(hair: int): void;

    setSkin(skin: int): void;

    setAndroidFace(face: int): void;

    setAndroidHair(hair: int): void;

    setAndroidSkin(skin: int): void;

    inc(map: Map<SecondaryStat, Integer>, stat: SecondaryStat, val: int): void;

    getId(): int;

    dissociateClient(): void;

    addPopupSay(npcId: int, duration: int, msg: String, sound: String): void;

    addPopupSay(npcId: int, showTimer: int, msg: String): void;

    getItemQuantity(item: int): int;

    setMaxHp(hp: int): void;

    setMaxMp(hp: int): void;

    addPQLog(key: String): void;

    addPQLog(key: String, value: int, resetDays: int): void;

    getPQLog(key: String): int;

    resetPQLog(key: String): void;

    getQuestRecordEx(quest: int): String;

    getQuestRecord(quest: int, key: String): String;

    getQuestEntryData(quest: int): String;

    updateQuestRecordEx(questid: int, data: String): void;

    updateQuestRecordEx(questid: int, data: int): void;

    updateQuestRecord(questid: int, key: String, value: String): void;

    updateQuestRecord(questid: int, key: String, value: int): void;

    updateQuestRecord(questid: int, key: int, value: String): void;

    updateQuestRecord(questid: int, key: int, value: int): void;

    setQuestData(quest: int, data: String): void;

    cancelItemEffect(itemId: int): void;

    cancelSkillEffect(skillId: int): void;

    canGainItem(itemId: int, quantity: int): boolean;

    changeMap(mapId: int): void;

    changeMap(mapId: int, portal: int): void;

    changeMap(mapId: int, portal: String): void;

    changeMap(map: ScriptField): void;

    changeMap(map: ScriptField): void;

    changeMap(map: ScriptField, x: int, y: int): void;

    changeChannelAndMap(channel: int, mapId: int): void;

    startQuest(questId: int): void;

    startQuest(questId: int, npcId: int): void;

    startQuest(questId: int, npcId: int, data: String): void;

    completeQuest(questId: int, npcId: int): void;

    forfeitQuest(questId: int): void;

    createAlliance(allianceName: String): boolean;

    getAllianceId(): int;

    getAllianceCapacity(): int;

    getAllianceRank(): String[];

    getGuildId(): int;

    getGuildCapacity(): int;

    getGuildContribution(): int;

    hasGuild(): boolean;

    getGuildRank(): int;

    createGuild(name: String): int;

    disbandGuild(): void;

    dropAlertNotice(message: String): void;

    dropMessage(type: int, message: String): void;

    getLevel(): int;

    setLevel(newLevel: int): void;

    gainAp(gain: short): void;

    gainBuddySlots(gain: short): void;

    gainCloseness(gain: short): void;

    gainInventorySlots(type: byte, addSlot: int): void;

    gainExp(gain: long): void;

    gainItem(itemId: int, quantity: int): boolean;

    gainItem(itemId: int, quantity: short, duration: long): boolean;

    updateItem(item: Item): void;

    updateItem(slot: short, item: Item): void;

    gainPetItem(itemId: int, day: int): boolean;

    gainItem(item: Item): boolean;

    gainMeso(gain: long): void;

    gainSp(skillbook: int, gain: short): void;

    gainSp(gain: short): void;

    getAccountId(): int;

    getCharacterId(): int;

    getAmountOfItem(itemId: int): int;

    getAmountOfItem(itemId: int, checkEquipped: boolean): int;

    getBuddyCapacity(): byte;

    getChannel(): int;

    getDex(): short;

    getFace(): int;

    getFreeSlots(type: byte): short;

    getGender(): int;

    getEvent(): ScriptEvent;

    getEvent(name: String): ScriptEvent;

    setEvent(event: ScriptEvent): void;

    showTimer(seconds: double): void;

    showTimer(time: int, elapseTime: int): void;

    pauseShowTimer(type: boolean, time: int, elapseTime: int): void;

    showTimer(left: boolean, seconds: int): void;

    closeTimer(): void;

    setDeathCount(nDeathCount: int): void;

    showDeathCount(): void;

    hasItem(itemId: int): boolean;

    hasItem(itemId: int, quantity: int): boolean;

    getName(): String;

    getNowOnlineTime(): int;

    getOnlineTime(): int;

    getSkillLevel(skillId: int): int;

    changeSkillLevel(skillId: int, newLevel: byte): void;

    getSp(skillId: int): int;

    getSubJob(): short;

    setSubJob(var1: short): void;

    getJob(): short;

    setJob(var1: int): void;

    getWorld(): int;

    getWorldShareRecord(quest: int): String;

    getWorldShareRecord(quest: int, key: String): String;

    updateWorldShareRecord(questid: int, data: String): void;

    updateWorldShareRecord(questid: int, key: String, value: String): void;

    hasEquipped(itemId: int): boolean;

    hasMeso(min: long): boolean;

    loseMesos(quantity: long): void;

    getMeso(): long;

    getCash(): long;

    getPoint(): long;

    getHyPay(type: int): int;

    modifyCashShopCurrency(type: int, value: int): void;

    increaseAllianceCapacity(): void;

    setHp(hp: int): void;

    increaseMaxHp(delta: int): void;

    increaseMaxMp(delta: int): void;

    isGm(): boolean;

    isQuestCompleted(questId: int): boolean;

    isQuestStarted(questId: int): boolean;

    loseInvSlot(type: byte, slot: short): void;

    loseItem(itemId: int): void;

    loseItem(itemId: int, quantity: int): void;

    maxSkills(): void;

    openUI(uiId: int): void;

    closeUI(uiId: int): void;

    openUIWithOption(uiId: int, option: int): void;

    openURL(sURL: String): void;

    openWebUI(sURL: String): void;

    openWebUI(id: int, uiPath: String, url: String): void;

    playExclSoundWithDownBGM(data: String): void;

    playSoundWithMuteBGM(wzPath: String): void;

    removeAdditionalEffect(): void;

    resetHyperSkill(): void;

    resetHyperStatSkill(pos: int): void;

    resetSkills(): void;

    resetVSkills(): void;

    resetStats(str: short, dex: short, _int: short, luk: short): void;

    revivePet(uniqueId: long, itemId: int): boolean;

    screenEffect(name: String): void;

    scriptProgressItemMessage(itemId: int, msg: String): void;

    scriptProgressMessage(msg: String): void;

    setAvatarLook(items: int[]): void;

    setDirectionMod(bSet: boolean): void;

    setDirection(bSet: boolean): void;

    setFaceOff(nFaceItemID: int): void;

    setForcedAction(n2: int, n3: int): void;

    setForcedFlip(n2: int): void;

    setForcedInput(n2: int): void;

    setForcedMove(n2: int, n3: int): void;

    setInGameCurNodeEventEnd(inGameCurNode: boolean): void;

    setInGameDirectionMode(b: boolean, b2: boolean, b3: boolean, b4: boolean): void;

    inGameDirection22(var: int): void;

    sendDirectionEvent(type: String, var: int): void;

    showNpcEffectPlay(n1: int, string: String, n2: int, n3: int, n4: int, n7: int, n8: int, n9: int, n10: int, str: String): void;

    setLayerBlind(b: boolean, n: int, n2: int): void;

    setLayerBlind(enable: boolean, n: int, r: int, g: int, b: int, n2: int, unk3: int): void;

    setLayerBlindWhite(b: boolean, n: int, n2: int): void;

    setLayerMove(n: int, s: String, n2: int, n3: int): void;

    setLayerOff(n: int, s: String): void;

    setLayerOn(n: int, s: String, n2: int, n3: int, n4: int, s2: String, n5: int): void;

    setStandAloneMode(enable: boolean): void;

    setStaticScreenMessage(n: int, s: String, b: boolean): void;

    setUserEmotionLocal(n: int, n2: int): void;

    setVansheeMode(n2: int): void;

    changeBGM(name: String): void;

    showAvatarOriented(s: String, toOther: boolean): void;

    showAvatarOrientedRepeat(b: boolean, s: String): void;

    showBlindEffect(b: boolean): void;

    showDoJangRank(): void;

    showProgressMessageFont(msg: String, fontNameType: int, fontSize: int, fontColorType: int, fadeOutDelay: int): void;

    showReservedEffect(screenCoord: boolean, rx: int, ry: int, data: String): void;

    showScreenAutoLetterBox(s: String, n: int): void;

    showScreenDelayedEffect(s: String, n: int): void;

    showSpineScreen(endDelay: int, path: String, aniamtionName: String, str: String): void;

    offSpineScreen(str: String, val: int): void;

    showSystemMessage(msg: String): void;

    showSpouseMessage(type: int, msg: String): void;

    showTopScreenEffect(s: String, n: int): void;

    showWeatherEffectNotice(s: String, n: int, n2: int): void;

    soundEffect(s: String, vol: int, n1: int, n2: int): void;

    soundEffect(s: String): void;

    destroyTempNpc(npcId: int): void;

    spawnTempNpc(npcId: int, x: int, y: int): void;

    teleport(n: int, n2: int, x: int, y: int): void;

    teleportPortal(n: int, portal: int): void;

    teleportPortal(n: int, portal: String): void;

    teleportToPortalId(portalID: int): void;

    teleportToPortalId(n: int, portalID: int): void;

    updateDamageSkin(id: int): void;

    UseItemEffect(itemId: int): void;

    useSkillEffect(skillId: int, level: int): void;

    useSkillEffect(skillId: int, level: int, duration: int): void;

    useMobSkillEffect(skillId: int, level: int, duration: int): void;

    hasEffect(sourceId: int): boolean;

    getPosition(): Point;

    setActionBar(id: int): void;

    modifyHonor(val: int): void;

    getAccountName(): String;

    getMapId(): int;

    gainRandVSkill(): boolean;

    gainRandVSkill(nCoreType: int, indieJob: boolean, onlyJob: boolean): boolean;

    gainVCoreSkill(vcoreoid: int, nCount: int): boolean;

    runNpc(npcId: int, shopId: int): void;

    openShop(npcId: int, shopId: int): void;

    showSpecialUI(b: boolean, s: String): void;

    showAchieveRate(): void;

    setAchieveRate(var1: int): void;

    zeroTag(beta: boolean): void;

    setKeyValue(key: String, value: String): void;

    getKeyValue(key: String): String;

    getIntKeyValue(key: String): int;

    addTrait(t: String, e: int): void;

    completeMobCollection(): void;

    registerMobCollection(mobId: int): void;

    checkMobCollection(mobId: int): boolean;

    checkMobCollection(s: String): boolean;

    handleRandCollection(s: int): void;

    registerMobCollectionQuest(s: int): void;

    hasAndroid(): boolean;

    getAndroidFace(): int;

    getAndroidHair(): int;

    getAndroidSkin(): int;

    updateTowerRank(stage: int, time: int): void;

    showTextEffect(message: String, second: int, posY: int): void;

    getJobCategory(): int;

    removeWeaponSoul(): void;

    removeBuffs(): void;

    getMaxHp(): int;

    getMaxMp(): int;

    getExpNeededForLevel(): long;

    customBuff(buffName: String, levels: String): void;

    showScreenShaking(mapID: int, stop: boolean): void;

    showBalloonMsg(path: String, duration: int): void;

    showStageClear(n: int): void;

    startBurn(type: int, time: long): void;

    getMarriageId(): int;

    setMarriageId(playerid: int): void;

    getDressingRoomSlot(style: int): int;

    setDressingRoomSlot(style: int, slot: int): boolean;

    increaseTrunkCapacity(gain: int): boolean;

    enterCS(): void;

    increaseDamageSkinCapacity(): void;

    enableEquipSlotExt(days: int): void;

    runAntiMacro(): boolean;

    getFame(): int;

    setFame(fame: int): void;

    updateFamiliars(): void;

    getFamiliars(): List<MonsterFamiliar>;

    setHexaCoreLevel(coreId: int, level: int): void;

    clearHexaSkills(): void;

    createParty(): void;

    disbandParty(): void;

    isQuestFinished(questId: int): boolean;

    getSpace(type: int): short;

    gainItemAndEquip(itemId: int, slot: short): void;

    getMapleJob(): MapleJob;

    getMapleJobById(id: int): MapleJob;

    getAllMapleJobs(): MapleJob[];

    getFreeAllSlots(slot: int): boolean;

    removeSlot(invType: int, slot: short, quantity: short): void;

    giveItemForPlayerName(name: String, itemId: int, quantity: int): void;

    giveItemForMap(mapId: int, itemId: int, quantity: int): void;

    givePointForPlayerName(name: String, type: String, quantity: int): void;

    gmInvincible(): void;

    gmHide(): void;

    gmKillMap(): void;

    gmDebug(debug: String): void;

    getOnlinePlayersNum(): int;

    gmGetMob(): void;

    KickForPlayerName(all: boolean, name: String): void;

    getBeginner(): short;

    gmCooldown(): void;

    gmSeparation(times: int): void;

    autoAttack(trigger: boolean): void;

    ban(name: String, reason: String, mac: boolean): void;

    gmExpRate(channel: int, rate: int): void;

    gmDropRate(channel: int, rate: int): void;

    gmSpawn(mobId: int, count: int, hp: int): void;

    getInventorySlot(type: byte, slot: short): ScriptItem;

    broadcastGachaponMessage(notice: String, item: Item, text: String): void;

    unban(name: String): void;

    openPacketUIByAdmin(): void;

    reloadSkill(): void;

    gainVCraftCore(quantity: int): void;

    reload(type: int): void;

    hasMesos(meso: int): boolean;

    addHyPay(hypay: int): int;

    getModifyMileage(): int;

    getMaplePoints(): int;

    getMaplePoints(onlyMPoint: boolean): int;

    getMileage(): int;

    modifyMileage(quantity: int): int;

    modifyMileage(quantity: int, type: int): int;

    modifyMileage(quantity: int, log: String): int;

    modifyMileage(quantity: int, show: boolean): int;

    modifyMileage(quantity: int, type: int, show: boolean, limitMax: boolean, log: String): int;

    updateHypay(): int;

    addPayReward(hypay: int): int;

    delPayReward(pay: int): int;

    updateReward(): void;

    getReward(id: int): MapleReward;

    addReward(acc: boolean, type: int, amount: long, item: int, desc: String): void;

    addReward(acc: boolean, start: long, end: long, type: int, amount: long, itemId: int, desc: String): void;

    addReward(accid: int, cid: int, start: long, end: long, type: int, amount: long, itemId: int, desc: String): void;

    deleteReward(id: int): void;

    deleteReward(id: int, update: boolean): void;

    makeNpc(npcid: int): void;

    getTempValues(): Map<String, Object>;

    setFamiliarsChanged(change: boolean): void;

    getSummonedFamiliar(): MonsterFamiliar;

    removeFamiliarsInfo(n: int): void;

    addFamiliarsInfo(monsterFamiliar: MonsterFamiliar): void;

    initFamiliar(cbr: MonsterFamiliar): void;

    removeFamiliar(): void;

    getEventCount(eventId: String): int;

    getEventCount(eventId: String, type: int): int;

    getEventCount(eventId: String, type: int, resetDay: int): int;

    setEventCount(eventId: String): void;

    setEventCount(eventId: String, type: int): void;

    setEventCount(eventId: String, type: int, count: int): void;

    setEventCount(eventId: String, type: int, count: int, date: int, updateTime: boolean): void;

    resetEventCount(eventId: String): void;

    resetEventCount(eventId: String, type: int): void;

    chatbyEffect(showMS: int, message: String): void;

    isBurning(): void;

}

declare interface ScriptPortal extends PlayerScriptInteraction {

    getName(): String;

    getId(): int;

    getPosition(): Point;

    abortWarp(): void;

    warped(): boolean;

    playPortalSE(): void;

    resetRememberedMap(variable: String): int[];

    rememberMap(variable: String): void;

}

declare interface ScriptReactor extends PlayerScriptInteraction {

    getName(): String;

    getDataId(): int;

    getPosition(): Point;

}

declare interface ScriptServer extends ScriptBase {

    startTimer(key: String, millisDelay: int): void;

    stopTimer(key: String): void;

    stopTimers(): void;

}

