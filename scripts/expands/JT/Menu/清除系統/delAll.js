var  status;
var  text;
var  column = new Array("裝備", "消耗", "裝飾", "其他", "特殊", "時裝");

var  藍圈圈 = "#fEffect/CharacterEff.img/1082588/3/0#"; //藍圈圈


text = "#d請注意此操作#r不可逆#d，誤刪無法取回 : #l\r\n#b";
for (var  i = 1; i <= 6; i++) {
    text += "#L" + i + "#"+藍圈圈+" #d清除#r " + column[i-1] + "欄#d 的所有道具 "+藍圈圈+"#l\r\n\r\n";
}
let sel = npc.askMenu(text);

let Yes = npc.askYesNo("#d確定是否#r刪除#d" + column[sel-1] + "欄的所有道具?");

if(Yes){
    let cm = player.getPlayer();
    for (var  i = 0; i < 128; i++) {
        if (npc.getInventory(sel).getItem(i) != null) {
            npc.removeAll(npc.getInventory(sel).getItem(i).getItemId());
        }
    }

    npc.say("已經幫您清除欄位所有道具");
}else{
    npc.say("有需要時再來找我");
}

