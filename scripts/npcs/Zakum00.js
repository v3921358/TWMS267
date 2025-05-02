var res = npc.askMenu("什麼．．．很好，你們看起來似乎夠格了。您要做什麼呢？#b#b#L0#前往調查廢礦洞穴。#l\r\n#b#L1#偵查殘暴炎魔副本。#l\r\n#b#L2#收下要獻給殘暴炎魔的祭品。#l\r\n#b#L3#前往冰原雪域。")
switch (res) {
    case 0:
        npc.say("尚未正式開放礦洞調查")
        break;
    case 1:
        player.changeMap(280020000);
        break;
    case 2:
        npc.next().sayX("喔,你需要獻給簡單炎魔的祭品?")
        npc.next().sayX("但是要呼喚殘暴炎魔的話需要祭品,我現在已經有太多了所以就直接給你吧..")
        player.gainItem(4001796, 1);
        player.gainItem(4001017, 1);
        break;
}