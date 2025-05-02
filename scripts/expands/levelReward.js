/*  This is mada by Kent
 *  This source is made by Funms Team
 *  功能：等級送禮
 *  @Author Kent
 */
var  暗夜 ="#fEffect/ItemEff/1004718/effect/default/3#";//暗夜
var  星閃 ="#fEffect/ItemEff/1005393/effect/backDefault/3#";//星閃
var  小臉 ="#fEffect/ItemEff/1012634/effect/default/1#";//小臉
var  bossid = "等級禮包";
var  giftLevel = Array(10,50,100,150,180,200,210,220,230,240,250,260,270,275,280,285,290,295,300);
//Array(道具ID,道具數量,禮包編號,[力,智,敏,幸,HP,MP,物攻,魔攻,防禦,王,無,跳,移速,迴避,怪物傷害,星力]),
var  giftContent = Array(

    //10級               [力,智,敏,幸,HP,MP,物攻,魔攻,防禦,王,無,跳,移速,迴避,怪物傷害,星力]),
    Array(1202193, 1, 0),//輪迴碑石
    Array(5002414, 1, 0),//托特
    Array(5002415, 1, 0),//貝拉
    Array(5002416, 1, 0),//迷你死亡
    Array(1142358, 1, 0, [15,15,15,15,0,0,15,15,0,0,0,0,0,0,0,0]),//可愛的新手勳章 15屬性15攻擊
    Array(1112942, 1, 0, [15,15,15,15,0,0,15,15,0,0,0,0,0,0,0,5]),//歡樂指環 15屬性15攻擊
    Array(1112942, 1, 0, [15,15,15,15,0,0,15,15,0,0,0,0,0,0,0,5]),//歡樂指環 15屬性15攻擊
    Array(5044010, 1, 0),//永恆瞬移石
    Array(2450000, 2, 0),//獵人的幸運 x2
    //50級
    Array(1012287, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//傳說楓之谷臉飾 20攻20屬
    Array(1032041, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//楓葉赤光耳環 20攻20屬
    Array(1182263, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//V慶典胸章 20攻20屬
    Array(1122058, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//休菲凱曼的混亂項鍊 20攻20屬
    Array(1190509, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//橘子能源 20攻20屬
    Array(1022067, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//黑狐狸猴眼飾 20攻20屬
    Array(1102041, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//冒險家粉色披風 20攻20屬
    Array(1112429, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//冒險家魔法之戒 20攻20屬
    Array(1112660, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//冒險家勇者戒指 20攻20屬
    Array(1112585, 1, 1, [20,20,20,20,0,0,20,20,0,0,0,0,0,0,0,0]),//天使祝福戒指 20攻20屬
    Array(2450000, 3, 1),//獵人的幸運
    //100級
    Array(2630494, 1, 2),//9週年黑色武器選擇箱
    Array(2450000, 5, 2),//獵人的幸運
    Array(1003863, 1, 2, [30,30,30,30,0,0,30,30,0,0,0,0,0,0,0,15]),//九週年黑色之帽 30攻30屬15星
    Array(1012376, 1, 2, [30,30,30,30,0,0,30,30,0,0,0,0,0,0,0,15]),//九週年黑色樹膠 30攻30屬15星
    Array(1052612, 1, 2, [30,30,30,30,0,0,30,30,0,0,0,0,0,0,0,15]),//九週年黑色套裝 30攻30屬15星
    Array(1102562, 1, 2, [30,30,30,30,0,0,30,30,0,0,0,0,0,0,0,15]),//九週年黑色披肩 30攻30屬15星
    Array(1113034, 1, 2, [30,30,30,30,0,0,30,30,0,0,0,0,0,0,0,15]),//九週年黑色楓葉戒指 30攻30屬15星
    Array(1122252, 1, 2, [30,30,30,30,0,0,30,30,0,0,0,0,0,0,0,15]),//九週年黑色墜飾 30攻30屬15星
    Array(1132228, 1, 2, [30,30,30,30,0,0,30,30,0,0,0,0,0,0,0,15]),//九週年黑色鎖扣 30攻30屬15星
    //150級
    Array(1142541, 1, 3, [40,40,40,40,0,0,40,40,0,0,0,0,0,0,0,0]),//最強等級*勳章
    Array(1032220, 1, 3, [15,15,15,15,0,0,15,15,0,0,0,0,0,0,0,15]),//初級培羅德耳環 15攻15屬15星
    Array(1113072, 1, 3, [15,15,15,15,0,0,15,15,0,0,0,0,0,0,0,15]),//初級培羅德戒指 15攻15屬15星
    Array(1122264, 1, 3, [15,15,15,15,0,0,15,15,0,0,0,0,0,0,0,15]),//初級培羅德墜飾 15攻15屬15星
    Array(1132243, 1, 3, [15,15,15,15,0,0,15,15,0,0,0,0,0,0,0,15]),//初級培羅德腰帶 15攻15屬15星
    Array(2049384, 1, 3),//星力15星強化券
    //180
    Array(1052723, 1, 4, [120,120,120,120,0,0,240,240,0,0,0,0,0,0,0,0]),//滅龍騎士盔甲 240攻120屬
    Array(5062030, 10, 4),//恢復方塊 x10
    Array(2450000, 5, 4),//獵人的幸運 x5
    Array(2049384, 1, 4),//星力15星強化券
    //200級
    Array(3700542, 1, 5),//王牌冒險家*稱號
    Array(1142357, 1, 5, [50,50,50,50,0,0,50,50,0,0,0,0,0,0,0,0]),//忠實楓迷勳章*勳章 50攻50屬
    Array(2450000, 5, 5),//獵人的幸運
    Array(5060025, 1, 5),//魔法豎琴 x1
    //210級
    Array(2450000, 5, 6),//獵人的幸運 X5
    Array(5060025, 2, 6),//魔法豎琴 x2
    //220級
    Array(2450000, 5, 7),//獵人的幸運 X5
    Array(5060025, 2, 7),//魔法豎琴 x2
    //230級
    Array(3700543, 1, 8),//大師冒險家*稱號
    Array(1142362, 1, 8, [70,70,70,70,0,0,70,70,0,0,0,0,0,0,0,0]),//武神傳承*勳章 70攻70屬
    Array(5060025, 3, 8),//魔法豎琴 x3
    Array(2644008, 1, 8),//追加1星強化券100%(23星)
    Array(2450000, 5, 8),//獵人的幸運 x5
    //240級
    Array(5062030, 30, 9),//恢復方塊 x30
    Array(2450000, 5, 9),//獵人的幸運 x5
    //250級
    Array(3014005, 1, 10),//榮譽的象徵椅子
    Array(1142940, 1, 10, [90,90,90,90,0,0,90,90,0,0,0,0,0,0,0,0]),//現在我也是英雄*勳章 90攻90屬10星
    Array(3700033, 1, 10),//A級獵人資格證
    Array(2644008, 2, 10),//追加1星強化券100%(23星)
    Array(2450000, 5, 10),//獵人的幸運 x5
    Array(5062030, 30, 10),//恢復方塊 x30
    //260
    Array(3700657, 1, 11),//聯盟戰地銀*稱號
    Array(1202237, 1, 11, [35,35,35,35,0,0,35,35,0,0,0,0,0,0,0,0]),//貝奧武夫的痕跡
    Array(1202238, 1, 11, [35,35,35,35,0,0,35,35,0,0,0,0,0,0,0,0]),//萬事的痕跡
    Array(1202239, 1, 11, [35,35,35,35,0,0,35,35,0,0,0,0,0,0,0,0]),//阿德勒的痕跡
    Array(5060025, 6, 11),//魔法豎琴 x6
    Array(2644008, 2, 11),//追加1星強化券100%(23星) x2
    Array(2450000, 10, 11),//獵人的幸運 x10
    Array(5062030, 30, 11),//恢復方塊 x30
    //270
    Array(3700034, 1, 12),//s級獵人資格證*稱號
    Array(2450000, 10, 12),//獵人的幸運 x10
    Array(2472000, 10, 12),//白金槌子 x10
    Array(5062030, 30, 12),//恢復方塊 x30
    //275
    Array(2644008, 2, 13),//追加1星強化券100%(23星) x2
    Array(2450000, 10, 13),//獵人的幸運 x10
    Array(5062030, 30, 13),//恢復方塊 x30
    //280
    Array(2644008, 2, 14),//追加1星強化券100%(23星) x2
    Array(2644047, 2, 14),//突破1星強化券100%(21星) x2
    Array(2450000, 10, 14),//獵人的幸運 x10
    Array(2472000, 10, 14),//白金槌子 x10
    Array(5062030, 30, 14),//恢復方塊 x30
    //285
    Array(2644008, 2, 15),//追加1星強化券100%(23星) x2
    Array(2644047, 1, 15),//突破1星強化券100%(21星) x1
    Array(2644053, 1, 15),//突破1星強化券100%(22星) x1
    Array(2644059, 1, 15),//突破1星強化券100%(23星) x1
    Array(2450000, 10, 15),//獵人的幸運 x10
    Array(2472000, 10, 15),//白金槌子 x10
    Array(5062030, 30, 15),//恢復方塊 x30
    //290
    Array(2644008, 2, 16),//追加1星強化券100%(23星) x2
    Array(2644047, 2, 16),//突破1星強化券100%(21星) x2
    Array(2644053, 2, 16),//突破1星強化券100%(22星) x2
    Array(2644059, 2, 16),//突破1星強化券100%(23星) x2
    Array(2450000, 20, 16),//獵人的幸運 x20
    Array(2472000, 10, 16),//白金槌子 x10
    Array(5062030, 30, 16),//恢復方塊 x30
    //295
    Array(2644008, 2, 17),//追加1星強化券100%(23星) x2
    Array(2644053, 2, 17),//突破1星強化券100%(22星) x2
    Array(2644059, 2, 17),//突破1星強化券100%(23星) x2
    Array(2450000, 20, 17),//獵人的幸運 x20
    Array(2472000, 10, 17),//白金槌子 x10
    Array(5062030, 30, 17),//恢復方塊 x30
    //300lv
    Array(3700655, 1, 18),//聯盟戰地高手*稱號
    Array(1143012, 1, 18, [160,160,160,160,0,0,160,160,0,50,50,0,0,0,0,20]),//頂點的王者*勳章 150攻150屬10星
    Array(5060025, 15, 18),//魔法豎琴 x15
    Array(2644064, 4, 18),//突破1星強化券50%(24星) x4
    Array(2472000, 10, 18),//白金槌子 x10
    Array(5062030, 30, 18),//恢復方塊 x30
);
var  giftId = -1;
var  giftToken = Array();
var  gifts = null;

let sel = showLevelMenu();

let isAccept = showGiftAcceptView(sel);

if(isAccept){
    completeView();
}else{
    npc.say("下次等您有空再來領取");
}

function showLevelMenu(){
    var  text = "\r\n";
    for (var  key in giftLevel) {
        var  tips = "";
        giftToken[key] = false;
        if (player.getLevel() >= giftLevel[key]) {
            if (player.getPlayer().getDaysPQLog(bossid + key, 1) === 0) {
                tips = "#d【可領取】";
                giftToken[key] = true;
            } else {
                tips = "#g【已領取】#b";
            }
        } else {
            tips = "#r【未領取】#b";
        }
        text += "#k#L" + (parseInt(key)) + "#"+小臉+"達到 #r#e" + giftLevel[key] + "#n#k 級等級 " + tips + "#l#k\r\n";
    }

    return npc.askMenu(text);
}

function showGiftAcceptView(selectionLevel){
    giftId = parseInt(selectionLevel);
    var  text = "#r#e" + giftLevel[giftId] + "#n#b級禮包內容：\r\n";
    gifts = getGift(giftId);
    for (var  key in gifts) {
        var  itemId = gifts[key][0];
        var  itemQuantity = gifts[key][1];
        text += "#v" + itemId + "##b#t" + itemId + "##k #rx " + itemQuantity + "#k\r\n";
    }
    text += "\r\n#d是否現在就領取該禮包？#k";

    return npc.askAccept(text);
}

function completeView(){
    if (giftId !== -1 && gifts !== null) {
        let mP = player.getPlayer();
        if (mP.getSpace(1) < 8 || mP.getSpace(2) < 8 || mP.getSpace(3) < 8 || mP.getSpace(4) < 8 || mP.getSpace(5) < 9) {
            npc.say("您的背包空間不足，請保證每個欄位至少8格的空間，以避免領取失敗。");
        }
        var  job = player.getJob();
        if (giftToken[giftId] && mP.getPQLog(bossid + key, 1) == 0) {
            for (var  key in gifts) {
                var  itemId = gifts[key][0];
                var  itemQuantity = gifts[key][1];

                //幫裝備改能力
                if( gifts[key][3]){
                    等級獎勵改能力(itemId,gifts[key][3]);
                }else if( !gifts[key][3]){
                    player.gainItem(itemId, itemQuantity);
                }
            }
            mP.setPQLog(bossid + (giftId), 1, 1);
            npc.say("恭喜您，領取成功！快打開包裹看看吧！");
        } else {
            npc.say("您已經領過了該禮包或者等級未達到要求，無法領取。");
        }
    } else {
        npc.say("領取錯誤！請聯繫管理員！");
    }
}

function getGift(id) {
    var  lastGiftContent = Array();
    for (var  key in giftContent) {
        if (giftContent[key][2] === id)
            lastGiftContent.push(giftContent[key]);
    }
    return lastGiftContent;
}


function 等級獎勵改能力(獲得道具,素質){
    var  獲得數量 = 1;
    var  str = "";
    if (player.canGainItem(獲得道具,獲得數量) === false && 獲得道具 !== 0)
        str += "#r空間不足       : 無法獲得 #i"+獲得道具+"# #e"+獲得數量+"#n個";

    if( str !== "" ){
        //確認失敗，顯示缺少物品
        npc.say( str );
        return false;
    }else{

        //給予裝備
        var  力量 = 素質[0];
        var  智力 = 素質[1];
        var  敏捷 = 素質[2];
        var  幸運 = 素質[3];

        var  血量 = 素質[4];
        var  魔量 = 素質[5];
        var  物攻 = 素質[6];
        var  魔攻 = 素質[7];

        var  防禦 = 素質[8];
        var  王傷 = 素質[9];
        var  無視 = 素質[10];

        var  跳躍力 = 素質[11];
        var  移動速度 = 素質[12];
        var  迴避 = 素質[13];
        var  攻擊怪物傷害 = 素質[14];
        var  星力 = 素質[15];

        var  str = "";
        if (player.canGainItem(獲得道具,獲得數量)===false && 獲得道具 !== 0)
            str += "#r空間不足       : 無法獲得 #i"+獲得道具+"# #e"+獲得數量+"#n個";

        if( str !== "" ){
            //確認失敗，顯示缺少物品
            npc.say( str );
            return false;
        }else{

            //給予裝備
            if(獲得道具 !== 0 && 獲得數量!== 0){
                var  itemId = 獲得道具;
                // var  ii = player.ii();
                //var  toDrop = ii.randomizeStats(ii.getEquipById(itemId)).copy(); // 生成一個Eq
                var  toDrop = sh.itemEquip(itemId).copy(); // 生成一個Eq
                if (力量 != null)
                    toDrop.setStr(力量);
                if (智力 != null)
                    toDrop.setInt(智力);
                if (敏捷 != null)
                    toDrop.setDex(敏捷);
                if (幸運 != null)
                    toDrop.setLuk(幸運);

                if (血量 != null)
                    toDrop.setHp(血量);
                if (魔量 != null)
                    toDrop.setMp(魔量);

                if (物攻 != null)
                    toDrop.setPad(物攻);
                if (魔攻 != null)
                    toDrop.setMad(魔攻);

                if (防禦 != null)
                    toDrop.setPdd(防禦);

                if (王傷 != null)
                    toDrop.setBossDamage(王傷);
                if (無視 != null)
                    toDrop.setIgnorePDR(無視);

                if (跳躍力 != null)
                    toDrop.setJump(跳躍力);
                if (移動速度 != null)
                    toDrop.setSpeed(移動速度);
                if (迴避 != null)
                    toDrop.setAvoid(迴避);
                if (攻擊怪物傷害 != null)
                    toDrop.setTotalDamage(攻擊怪物傷害);
                if (星力!=null){
                    toDrop.setEnhance(星力);
                }

                //toDrop.setExpiration(java.lang.System.currentTimeMillis() + period); // 期限
                player.gainItem(toDrop);
            }
            return true;
        }
    }
}
