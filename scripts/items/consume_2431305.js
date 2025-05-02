/*：
 腳本功能：武器箱子（轉職贈送）
 */

if ( player.getLevel() >= 10) {
     player.loseItem(2431305, 1);
     Gift(player.getJob());
     npc.say("已將新手支援武器發送至您的裝備欄位。");
} else {
    npc.say("您未達領取資格。");
}
function Gift(job) {
    switch (job) {
        case 100:
        case 5100:
            player.gainItem(1302135, 1); // 挑戰的單手劍 - 等級10
            break;
        case 1100:
        case 6100:
            player.gainItem(1402078, 1); // 挑戰的雙手劍 - 等級10
            break;
        case 110:
        case 1110:
        case 6110:
            player.gainItem(1402081, 1); // 挑戰的雙手劍 - 等級25
            break;
        case 111:
        case 1111:
        case 6111:
            player.gainItem(1402084, 1); // 挑戰的雙手劍 - 等級70
            break;
        case 112:
        case 1112:
        case 6112:
            player.gainItem(1402075, 1); // 挑戰的雙手劍 - 等級100
            break;
        case 120:
            player.gainItem(1312052, 1); // 挑戰的單手斧頭 - 等級25
            break;
        case 121:
            player.gainItem(1312055, 1); // 挑戰的單手斧頭 - 等級70
            break;
        case 122:
            player.gainItem(1312057, 1); // 挑戰的單手斧頭 - 等級100
            break;
        case 5110:
            player.gainItem(1302138, 1); // 挑戰的單手劍 - 等級25
            break;
        case 5111:
            player.gainItem(1302141, 1); // 挑戰的單手劍 - 等級70
            break;
        case 5112:
            player.gainItem(1302078, 1); // 挑戰的單手劍 - 等級100
            break;
        case 130:
            player.gainItem(1432071, 1); // 挑戰的長槍 - 等級25
            break;
        case 131:
            player.gainItem(1432074, 1); // 挑戰的長槍 - 等級70
            break;
        case 132:
            player.gainItem(1432076, 1); // 挑戰的長槍 - 等級100
            break;
        case 16200:
            player.gainItem(1354020, 1); // 樸素的四玉飾品
        case 200:
        case 1200:
        case 2200:
            player.gainItem(1372063, 1); // 挑戰的金屬短杖 - 等級10
            break;
        case 16210:
            player.gainItem(1354021, 1); // 隱約的四玉飾品
        case 210:
        case 220:
        case 230:
        case 1210:
        case 2211:
            player.gainItem(1372066, 1); // 挑戰的金屬短杖 - 等級25
            break;
        case 16211:
            player.gainItem(1354022, 1); // 華麗的四玉飾品
        case 211:
        case 221:
        case 231:
        case 1211:
        case 2214:
            player.gainItem(1372069, 1); // 挑戰的金屬短杖 - 等級70
            break;
        case 16212:
            player.gainItem(1354023, 1); // 發光的四玉飾品
        case 212:
        case 222:
        case 232:
        case 1212:
        case 2217:
            player.gainItem(1372072, 1); // 挑戰的金屬短杖 - 等級100
            break;
        case 300:
        case 1300:
            player.gainItem(1452093, 1); // 挑戰的弓 - 等級10
            player.gainItem(2060000, 1000); //弓矢
            break;
        case 3300:
            player.gainItem(1462078, 1); // 挑戰的弩弓 - 等級10
            player.gainItem(2061000, 1000); //弩矢
            break;
        case 310:
        case 1310:
            player.gainItem(1452096, 1); // 挑戰的弓 - 等級25
            break;
        case 311:
        case 1311:
            player.gainItem(1452099, 1); // 挑戰的弓 - 等級70
            break;
        case 312:
        case 1312:
            player.gainItem(1452101, 1); // 挑戰的弓 - 等級100
            break;
        case 320:
            player.gainItem(2061000, 1000); //弩矢
        case 3310:
            player.gainItem(1462081, 1); // 挑戰的弩弓 - 等級25
            break;
        case 321:
        case 3311:
            player.gainItem(1462084, 1); // 挑戰的弩弓 - 等級70
            break;
        case 322:
        case 3312:
            player.gainItem(1462086, 1); // 挑戰的弩弓 - 等級100
            break;
        case 301:
            player.gainItem(1592000, 1); // 馬拉德古代之弓
            player.gainItem(1353700, 1); // 潛能遺跡
            break;
        case 330:
            player.gainItem(1592001, 1); // 阿修爾古代之弓
            player.gainItem(1353701, 1); // 覺醒遺跡
            break;
        case 331:
            player.gainItem(1592003, 1); // 阿瓦里斯古代之弓
            player.gainItem(1353702, 1); // 三位一體遺跡
            break;
        case 332:
            player.gainItem(1592007, 1); // 以弗索古代之弓
            player.gainItem(1353703, 1); // 完美遺跡
            break;
        case 400:
            player.gainItem(1332104, 1); // 挑戰的短刀 - 等級10
            if (im.getPlayer().getSubcategory() == 1) {
                break;
            }
        case 1400:
            player.gainItem(1472104, 1); // 挑戰的拳套 - 等級10
            player.gainItem(2070015, 1000); // 新手盜賊的飛鏢
            break;
        case 410:
        case 1410:
            player.gainItem(1472107, 1); // 挑戰的拳套 - 等級25
            break;
        case 411:
        case 1411:
            player.gainItem(1472110, 1); // 挑戰的拳套 - 等級70
            break;
        case 412:
        case 1412:
            player.gainItem(1472112, 1); // 挑戰的拳套 - 等級100
            break;
        case 431: //30
            player.gainItem(1342001, 1); // 地天刀
        case 420:
            player.gainItem(1332107, 1); // 挑戰的短刀 - 等級25
            break;
        case 433: //60
            player.gainItem(1342004, 1); // 修羅刀
        case 421:
            player.gainItem(1332110, 1); // 挑戰的短刀 - 等級70
            break;
        case 434: //100
            player.gainItem(1342008, 1); // 流星刀
        case 422:
            player.gainItem(1332115, 1); // 挑戰的短刀 - 等級100
            break;
        case 430: //20
            player.gainItem(1342000, 1); // 無名刀
            player.gainItem(1332043, 1); // 水晶刃
            break;
        case 432: //45
            player.gainItem(1342002, 1); // 倚天刀
            player.gainItem(1332045, 1); // 雙翼刃
            break;
        case 500:
        case 508:
        case 3500:
            player.gainItem(1492066, 1); // 挑戰的槍 - 等級10
            if (job != 500) {
                break;
            }
            player.gainItem(2330000, 1000); //銀子彈
        case 1500:
        case 2500:
        case 15500:
            if (job == 2500) {
                player.gainItem(1353100, 1); // 青色狐狸珠子
            }
            player.gainItem(1482066, 1); // 挑戰的指虎 - 等級10
            break;
        case 510:
        case 1510:
        case 2510:
        case 15510:
            if (job == 2510) {
                player.gainItem(1353101, 1); // 綠色狐狸珠子
            }
            player.gainItem(1482069, 1); // 挑戰的指虎 - 等級25
            break;
        case 511:
        case 1511:
        case 2511:
        case 15511:
            if (job == 2511) {
                player.gainItem(1353102, 1); // 紅色狐狸珠子
            }
            player.gainItem(1482072, 1); // 挑戰的指虎 - 等級70
            break;
        case 512:
        case 1512:
        case 2512:
        case 15512:
            if (job == 2512) {
                player.gainItem(1353103, 1); // 金色狐狸珠子
            }
            player.gainItem(1482074, 1); // 挑戰的指虎 - 等級100
            break;
        case 520:
        case 570:
        case 3510:
            player.gainItem(1492069, 1); // 挑戰的槍 - 等級25
            break;
        case 521:
        case 571:
        case 3511:
            player.gainItem(1492072, 1); // 挑戰的槍 - 等級70
            break;
        case 522:
        case 572:
        case 3512:
            player.gainItem(1492074, 1); // 挑戰的槍 - 等級100
            break;
        case 2100:
            player.gainItem(1442096, 1); // 挑戰的矛 - 等級10
            break;
        case 2110:
            player.gainItem(1442099, 1); // 挑戰的矛 - 等級25
            break;
        case 2111:
            player.gainItem(1442102, 1); // 挑戰的矛 - 等級70
            break;
        case 2112:
            player.gainItem(1442105, 1); // 挑戰的矛 - 等級100
            break;
        case 2300:
            player.gainItem(1522000, 1); // 雙重貝西斯
            player.gainItem(1352000, 1); // 魔法箭
            break;
        case 2310:
            player.gainItem(1522004, 1); // 雙刃風
            player.gainItem(1352001, 1); // 鋒利的魔法箭
            break;
        case 2311:
            player.gainItem(1522008, 1); // 絕世月光
            player.gainItem(1352002, 1); // 永遠的魔法箭
            break;
        case 2312:
            player.gainItem(1522012, 1); // 雷緹娜
            player.gainItem(1352003, 1); // 無限的魔法箭
            break;
        case 2400:
            player.gainItem(1362001, 1); // 初學者手杖
            player.gainItem(1352100, 1); // 魔幻卡牌
            break;
        case 2410:
            player.gainItem(1362005, 1); // 盜賊手杖
            player.gainItem(1352101, 1); // 幽靈卡牌
            break;
        case 2411:
            player.gainItem(1362009, 1); // 強力魔法杖
            player.gainItem(1352102, 1); // 天蘿卡牌
            break;
        case 2412:
            player.gainItem(1362013, 1); // 莎莉絲手杖
            player.gainItem(1352103, 1); // 新者卡牌
            break;
        case 2700:
            player.gainItem(1212001, 1); // 行星
			player.gainItem(1352400, 1); // 星光彈
            break;
        case 2710:
            player.gainItem(1212002, 1); // 金色閃耀
            break;
        case 2711:
            player.gainItem(1212004, 1); // 志願者
            break;
        case 2712:
            player.gainItem(1212008, 1); // 閃光金翼
            break;
        case 3100:
            player.gainItem(1322077, 1); // 挑戰的單手鈍器 - 等級10
            break;
        case 3110:
            player.gainItem(1322080, 1); // 挑戰的單手鈍器 - 等級25
            break;
        case 3111:
            player.gainItem(1322083, 1); // 挑戰的單手鈍器 - 等級70
            break;
        case 3112:
            player.gainItem(1322085, 1); // 挑戰的單手鈍器 - 等級100
            break;
        case 3200:
            player.gainItem(1382085, 1); // 挑戰的金屬長杖 - 等級10
            break;
        case 3210:
            player.gainItem(1382088, 1); // 挑戰的金屬長杖 - 等級25
            break;
        case 3211:
            player.gainItem(1382091, 1); // 挑戰的金屬長杖 - 等級70
            break;
        case 3212:
            player.gainItem(1382094, 1); // 挑戰的金屬長杖 - 等級100
            break;
        case 3600:
            player.gainItem(1242001, 1); // 不對稱之魂
            break;
        case 3610:
            player.gainItem(1242002, 1); // 山脊切割者
            break;
        case 3611:
            player.gainItem(1242004, 1); // 梅克洛短刀
            break;
        case 3612:
            player.gainItem(1242008, 1); // 地獄骷髏
            break;
        case 3700:
            player.gainItem(1582000, 1); // 鐵製重拳槍
            player.gainItem(1353400, 1); // 爆發能力<0號>
            break;
        case 3710:
            player.gainItem(1582001, 1); // 超能不速之客重拳槍
            player.gainItem(1353401, 1); // 爆發能力<1號>
            break;
        case 3711:
            player.gainItem(1582003, 1); // 巨型重拳槍
            player.gainItem(1353402, 1); // 爆發能力<2號>
            break;
        case 3712:
            player.gainItem(1582007, 1); // 巨人錘重拳槍
            player.gainItem(1353403, 1); // 爆發能力<3號>
            break;
        case 6500:
            player.gainItem(1222001, 1); // 紫色煙霧
            break;
        case 6510:
            player.gainItem(1222002, 1); // 粉色煙霧
            break;
        case 6511:
            player.gainItem(1222004, 1); // 傑德日出環
            break;
        case 6512:
            player.gainItem(1222008, 1); // 綠色龍靈魂
            break;
        case 3101:
            player.gainItem(1232001, 1); // 藍色復仇者
            break;
        case 3120:
            player.gainItem(1232002, 1); // 深處記憶體
            break;
        case 3121:
            player.gainItem(1232004, 1); // 普倫悲傷
            break;
        case 3122:
            player.gainItem(1232008, 1); // 殘忍復仇
            break;
        case 501:
            player.gainItem(1532045, 1); // 無聲加農炮
            break;
        case 530:
            player.gainItem(1532049, 1); // 火箭筒
            break;
        case 531:
            player.gainItem(1532053, 1); // 火焰噴射器
            break;
        case 532:
            player.gainItem(1532087, 1); // 混沌阿貝羅
            break;
        case 4100:
            player.gainItem(1542000, 1); // 鐵刀
            break;
        case 4110:
            player.gainItem(1542002, 1); // 虎徹
            break;
        case 4111:
            player.gainItem(1542005, 1); // 砂塵
            break;
        case 4112:
            player.gainItem(1542009, 1); // 金剛刀
            break;
        case 4200:
            player.gainItem(1552000, 1); // 鐵扇
            break;
        case 4210:
            player.gainItem(1552002, 1); // 三紋扇
            break;
        case 4211:
            player.gainItem(1552005, 1); // 爽倉扇
            break;
        case 4212:
            player.gainItem(1552009, 1); // 蒼天流麗
            break;
        case 14200:
            player.gainItem(1353200, 1); // 西洋棋—士兵
            break;
        case 14210:
            player.gainItem(1262001, 1); // 測試用ESP限制器
            player.gainItem(1353201, 1); // 西洋棋—騎士
            break;
        case 14211:
            player.gainItem(1262003, 1); // 衛波輪ESP限制器
            player.gainItem(1353202, 1); // 西洋棋—城堡
            break;
        case 14212:
            player.gainItem(1262007, 1); // 大門ESP限制器
            player.gainItem(1353203, 1); // 西洋棋—女王
            break;
        case 15200:
            player.gainItem(1282000, 1); // 銀月魔法護腕
            break;
        case 15210:
            player.gainItem(1282001, 1); // 馬提斯魔法護腕
            break;
        case 15211:
            player.gainItem(1282003, 1); // 羅比斯魔法護腕
            break;
        case 15212:
            player.gainItem(1282007, 1); // 菲里亞斯魔法護腕
            break;
        case 6400:
            player.gainItem(1272000, 1); // 德梅勒魁里歐
            break;
        case 6410:
            player.gainItem(1272001, 1); // 德被奴斯
            break;
        case 6411:
            player.gainItem(1272003, 1); // 德馬爾提
            break;
        case 6412:
            player.gainItem(1272007, 1); // 德聶普諾
            break;
        case 11210:
            player.gainItem(1252001, 1); // 幻獸棒
            player.gainItem(1352810, 1); // 小小密語
            break;
        case 11210:
            player.gainItem(1252002, 1); // 分配棒
            player.gainItem(1352811, 1); // 好友的密語
            break;
        case 11210:
            player.gainItem(1252004, 1); // 黑豹棒
            player.gainItem(1352812, 1); // 守護的密語
            break;
        case 11210:
            player.gainItem(1252008, 1); // 鹘權杖
            player.gainItem(1352813, 1); // 進化大自然的密語
            break;
        case 16400:
            player.gainItem(1292000, 1); // 黑雲扇
            player.gainItem(1353800, 1); // 瑪瑙扇墜
            break;
        case 16410:
            player.gainItem(1292001, 1); // 小扇
            player.gainItem(1353801, 1); // 青金石扇墜
            break;    
        case 16411:
            player.gainItem(1292003, 1); // 暴風扇
            player.gainItem(1353802, 1); // 剛玉扇墜
            break;    
        case 16412:
            player.gainItem(1292007, 1); // 太平扇
            player.gainItem(1353803, 1); // 月長石扇墜
            break;
        case 15100:
            player.gainItem(1213000, 1); // 耐力
            player.gainItem(1354000, 1); // 特雷斯手鐲
            break;
        case 15110:
            player.gainItem(1213001, 1); // 榮譽
            player.gainItem(1354001, 1); // 伊博魁手鐲
            break;
        case 15111:
            player.gainItem(1213003, 1); // 奉獻
            player.gainItem(1354002, 1); // 正規手鐲
            break;
        case 15112:
            player.gainItem(1213007, 1); // 自豪
            player.gainItem(1354003, 1); // 高貴手鐲
            break;
        case 17500:
            player.gainItem(1403000, 1); // 老舊武拳
            player.gainItem(1352860, 1); // 老舊拳環
            break;
        case 17510:
            player.gainItem(1403001, 1); // 修行者武拳
            player.gainItem(1352861, 1); // 入門拳環
            break;
        case 17511:
            player.gainItem(1403003, 1); // 武俠武拳
            player.gainItem(1352862, 1); // 鬥志拳環
            break;
        case 17512:
            player.gainItem(1403007, 1); // 武烈武拳
            player.gainItem(1352863, 1); // 極拳拳環
            break;
        case 6300:
            player.gainItem(1214000, 1); // 黑暗凝視
            player.gainItem(1354010, 1); // D基本武器腰封
            break;
        case 6310:
            player.gainItem(1214001, 1); // 暗刺
            player.gainItem(1354011, 1); // D30A武器腰封
            break;
        case 6311:
            player.gainItem(1214003, 1); // 反諷攻擊
            player.gainItem(1354012, 1); // D60高級武器腰封
            break;
        case 6312:
            player.gainItem(1214007, 1); // 貪婪放射
            player.gainItem(1354013, 1); // D100自訂武器腰封
            break;
        case 15400:
            player.gainItem(1404000, 1); // 埃內特環刃
            player.gainItem(1354030, 1); // 行星能量手環
            break;
        case 15410:
            player.gainItem(1404001, 1); // 阿奎拉環刃
            player.gainItem(1354031, 1); // 榮耀能量手環
            break;
        case 15411:
            player.gainItem(1404003, 1); // 拉切爾塔
            player.gainItem(1354032, 1); // 燦爛能量手環
            break;
        case 15412:
            player.gainItem(1404007, 1); // 克魯阿普斯
            player.gainItem(1354033, 1); // 無限能量手環
            break;
    }
}

