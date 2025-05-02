if(!player.hasItem(4001017)){
    player.showSystemMessage("沒有召喚殘暴炎魔所需的材料。我剛好有，就給你吧。");
    player.gainItem(4001017,1);
} else {
player.changeMap(211042402, 0);
}