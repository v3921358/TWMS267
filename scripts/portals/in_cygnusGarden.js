let text = "#e#n#b#h0# #k前方就是#b希納斯#k的庭院了,那麼你想幹嘛???\r\n\r\n";//\r\n2、参加远征队必须有#b#t4032923##k才可以进行。\r\n";
text += "#L1##b前往#b希納斯庭院#k#l\r\n";
let sel = npc.askMenu(text, 2143004);
if (sel == 1) {
    npc.rememberMap("RETURN_MAP");
    player.changeMap(271040000, 2);
}
