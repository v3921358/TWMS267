/*      

 NPC類型:    綜合NPC

 */
var  數字1 = "#fEffect/OnUserEff/demian/stigma/number/1#";//1
var  數字2 = "#fEffect/OnUserEff/demian/stigma/number/2#";//2
var  數字3 = "#fEffect/OnUserEff/demian/stigma/number/3#";//3
var  數字4 = "#fEffect/OnUserEff/demian/stigma/number/4#";//4
var  數字5 = "#fEffect/OnUserEff/demian/stigma/number/5#";//5
var  數字6 = "#fEffect/OnUserEff/demian/stigma/number/6#";//6
var  數字7 = "#fEffect/OnUserEff/demian/stigma/number/7#";//7
var  數字8 = "#fEffect/OnUserEff/demian/stigma/number/8#";//8
var  數字9 = "#fEffect/OnUserEff/demian/stigma/number/9#";//9

var  slot = Array();
var  dsa = "";


let selection = showInventoryMenu()


let itemSelection = showInventoryItemsMenu(selection);

if(itemSelection > -1) {
    let itemCount = npc.minNum(1).maxNum(player.getPlayer().getItemQuantity(itemSelection)).askNumber("#d請輸入你要#r''刪除物品#d的數量 :\r\n\r\n");


    if (itemCount > 30000) {
        npc.sendOk("#e#r一次性刪除道具的數量不能超過30000個#k");
    } else {
        player.loseItem(itemSelection, itemCount);
        npc.say(dsa + "#i" + itemSelection + ":# 已經#r被系統刪除#k !");
    }
}

function showInventoryMenu(){
    var  zyms = "";
    zyms = "玩家 : #b#h0# #k這邊可以#r刪除#k欄位的道具 :\r\n";
    zyms += "==========================================\r\n";
    zyms += "#L6#"+數字1+"#r .#r直接清空特定欄位#k#l\r\n\r\n";
    zyms += "==========================================\r\n";
    zyms += "#L1#"+數字2+"#r .#d刪除裝備欄道具#k#l\r\n\r\n";
    zyms += "#L2#"+數字3+"#r .#d刪除消耗欄道具#k#l\r\n\r\n";
    zyms += "#L4#"+數字4+"#r .#d刪除其他欄道具#k#l\r\n\r\n";
    zyms += "#L3#"+數字5+"#r .#d刪除裝飾欄道具#k#l\r\n\r\n";
    zyms += "#L5#"+數字6+"#r .#d刪除特殊欄道具#k#l\r\n\r\n";
    zyms += "#L7#"+數字7+"#r .#d刪除時裝欄道具#k#l\r\n\r\n";
    zyms += "==========================================\r\n";
    return npc.askMenu(zyms);
}

function showInventoryItemsMenu(selection){
    if (selection !== 6) { //刪除裝備欄道具
        return selectItemFormInventory(selection === 1 ? 100 : 400, selection);
    } else{
        npc.runScript(2008,"expands/JT/Menu/清除系統/清除系統All");
        return  -1;
    }
}


function selectItemFormInventory(dsd ,inventoryEnum){
    let itemSelection = -1;
    let cm = player.getPlayer();
    var  avail = "";
    for (var  i = 0; i < 128; i++) {
        if (npc.getInventory(inventoryEnum).getItem(i) != null) {
            avail += "#L" + npc.getInventory(inventoryEnum).getItem(i).getItemId() + "# #z" + npc.getInventory(inventoryEnum).getItem(i).getItemId() + "# #i" + npc.getInventory(inventoryEnum).getItem(i).getItemId() + ":##l\r\n";
        }
        slot.push(i);
    }
    itemSelection = npc.askMenu(dsa + "#b請選擇需要刪除的道具:\r\n#b" + avail);

    return itemSelection;
}
