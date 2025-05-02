/* NpcTalk-ID:2052006, Npc_Talk-任務:3475 */

npc.next().sayX("#fn標楷體#(滋滋...滋滋...)#fn# 這裡是...地球防衛總部... \r\n#b鬼牌計畫#k #fn標楷體#(...滋滋)#fn#開始。");
npc.next().sayX("#b鬼牌計畫？#k");
if (npc.askYesNo("#b#h0##k，你之所以會聽到此訊息，\r\n就表示本部發生危急狀況了！\r\n\r\n快點前來#b地球防衛總部#k的#b指揮室#k！\r\n#b(同意時會自動移動)#k")) {
    player.changeMap(221000300);
}
