/**
 *
 *
 */

let ret = npc.askYesNoNoEsc("很高興能在這裡見到你……我來把你變成飛俠吧。做好心理準備了嗎？臨陣退縮的人，不配成為飛俠。");
if (ret == 1) {
    npc.completeQuest();
    player.gainEquipInventorySlots(4);
    player.gainUseInventorySlots(4);
    player.gainSetupInventorySlots(4);
    player.gainEtcInventorySlots(4);
    player.gainCashInventorySlots(4);
    player.setJob(400);
    player.resetStats(4, 4, 4, 4);
    player.gainSp(3);
    if (player.canGainItem(1142107, 1)) {
        player.gainItem(1142107, 1);
    }
    if (player.canGainItem(1332063, 1)) {
        player.gainItem(1332063, 1);
    }
    if (player.canGainItem(1472000, 1)) {
        player.gainItem(1472000, 1);
    }
    if (player.canGainItem(1472063, 1)) {
        player.gainItem(1472063, 1);
    }
    if (player.canGainItem(2070008, 800)) {
        player.gainItem(2070008, 800);
    }
    npc.ui(1).uiMax().next().noEsc().sayX("成為飛俠的你已然變得更強。並且你也有了作為飛俠可使用的技能, 那就趕緊拿出來試一試吧。");
    npc.ui(1).uiMax().next().noEsc().sayX("另外, 你的能力值也得進行適當修改, 以便更加適合飛俠的特點。顯然對于飛俠而言, LUK才是核心屬性, 而DEX為補助屬性。若不清楚的話, 不妨使用#b自動分配#k也好。");
    npc.ui(1).uiMax().next().noEsc().sayX("為了紀念你已成為飛俠, 我還將適當給你增加揹包空間。你可以收集更多的武器和防具, 讓自己變得更加強大。");
    npc.ui(1).uiMax().next().noEsc().sayX("對了，有一點需要注意。雖然新手的時候沒關係，但是成為飛俠的瞬間開始，必須小心不要死掉……萬一死了的話，之前積累的經驗值可能會受到損失……");
    npc.ui(1).uiMax().next().noEsc().sayX("我能教你的只有這些……我送了你一些裝備，現在你去鍛鍊自己，讓自己變得更強吧。");
} else {
    npc.ui(1).uiMax().next().noEsc().sayX("哎呀……沒想到你這麼膽小。難道你失去成為飛俠的自信了嗎？");
}
