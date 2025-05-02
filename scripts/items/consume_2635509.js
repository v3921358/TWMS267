let arcBoxItem = npc.getItemId();
let hasBoxCount = player.getItemQuantity(arcBoxItem);

let sel = npc.askMenu("物品欄中有#b#i" + arcBoxItem + "##t" + arcBoxItem + ":##k道具有 #b" + hasBoxCount + "個#k。\r\n#L0# #b#e在物品欄中領取裝備道具#n#k#l");

if (sel == 0) {
    let count = npc.askNumber("#b要兌換幾個祕法符文?", 1 , 0 , hasBoxCount);
    switch (arcBoxItem) {
        case 2635509:
            player.gainItem(1712001, count);
            break;
        case 2635510:
            player.gainItem(1712002, count);
            break;
        case 2635511:
            player.gainItem(1712003, count);
            break;
        case 2635512:
            player.gainItem(1712004, count);
            break;
        case 2635513:
            player.gainItem(1712005, count);
            break;
        case 2635514:
            player.gainItem(1712006, count);
            break;
    }
    player.loseItem(arcBoxItem, count);
}
