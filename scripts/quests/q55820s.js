/* MapleShark msb Creat Script tools */

npc.next().sayX("哈囉，#b#h0#！好久不見，最近過得好嗎？");
if (npc.askYesNo("最近西門町與不夜城有了變化。可以花一點時間聽我說嗎？#r(接取任務後會自動移動到地圖。)#k")) {
    player.changeMap(740000100);
    npc.completeQuest();
} else { npc.sayX("如果想重新接取任務,請記得找我"); }
