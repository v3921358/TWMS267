package Config.constants;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassFetcher;
import Client.stat.PlayerStats;
import Client.status.MonsterStatus;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.劍士;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.法師;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.皇家騎士團_技能群組.*;
import Net.server.buffs.MapleStatEffect;
import tools.Randomizer;

import java.util.*;

public class SkillConstants {


    public MapleClient c;

    public MapleClient getClient() {
        return c;
    }

    /*
     * 內在能力系統
     */
    private static final int[] innerSkills = new int[]{
            70000036, // 攻擊一般怪物時增加傷害
            70000039, // 給予狀態異常的敵人追加傷害
            70000000, // 力量 增加
            70000001, // 敏捷增加
            70000002, // 智力增加
            70000003, // 幸運增加
            70000051, // 增加力量、敏捷
            70000052, // 增加力量、智力
            70000053, // 增加力量、幸運
            70000054, // 增加敏捷、智力
            70000055, // 增加敏捷、幸運
            70000056, // 增加智力、幸運
            70000057, // 增加敏捷、力量
            70000058, // 增加智力、力量
            70000059, // 增加幸運、力量
            70000060, // 增加智力、敏捷
            70000061, // 增加幸運、敏捷
            70000062, // 增加幸運、智力
            70000015, // 所有能力值增加
            70000021, // 力量轉換為敏捷
            70000022, // 敏捷轉換為力量
            70000023, // 智力轉換為幸運
            70000024, // 幸運轉換為敏捷
            70000031, // 最大HP 增加一定比率
            70000032, // 最大MP 增加一定比率
            70000006, // 防禦力增加
            70000005, // 移動速度增加
            70000048, // 增加加持持續時間
            70000049, // 增加道具掉落率
            70000050, // 增加楓幣獲得量
            70000012, // 攻擊力增加
            70000013, // 魔力增加
            70000043, // 爆擊機率增加
            70000035, // 攻擊Boss怪物時，增加傷害
            70000041, // 把防禦力轉換為傷害
            70000025, // 一定等級時攻擊力增加
            70000026, // 一定等級時魔力增加
            70000008, // 最大 HP增加
            70000008, // 最大 MP增加
            70000029, // 防禦力固定比率增加
            70000045, // 以一定機率未套用冷卻時間
            70000016, // 攻擊速度增加
            70000046, // 被動技能等級增加 1
            70000047 // 增加攻擊技能目標
    };

    public static boolean isRecoveryIncSkill(int skillId) {
        switch (skillId) {
            case 英雄.強化恢復:
            case 凱撒.自我恢復:
            case 米哈逸.魔力恢復:
                return true;
        }
        return false;
    }

    public int skillid;

    public static boolean isLinkedAttackSkill(int skillId) {
        return getLinkedAttackSkill(skillId) != skillId;
    }


    public static int getLinkedAttackSkill(int skillId) {
        AbstractSkillHandler handler = SkillClassFetcher.getHandlerBySkill(skillId);
        if (handler != null) {
            int linkedSkillID = handler.getLinkedSkillID(skillId);
            if (linkedSkillID != -1) {
                return linkedSkillID;
            }
        }
        switch (skillId) {
            case 21001010:
            case 21000007:
                return 21000006;

            //------------------------ 英雄
            case 1141501:
                return 英雄.HEXA_聖劍降臨;
            case 1120017:
                return 1121008;
            case 1141001:
                return 1141000;
            case 175121002:
                return 175121001;
            case 400011140:
                return 400011027;
            case 500001004:
            case 500001003:
            case 500001002:
            case 500001001:
                return 500001000;
            //--------------------
            case 101000100:
            case 101000101:
            case 101000102:
                return 101000103;
            case 101000202:
            case 101000201:
            case 101000200:
                return 101000203;

            case 80011261:
                return 80011261;


            case 101100201:
            case 101100202:
            case 101101200:
                return 101100203;
            case 101110201:
            case 101110204:
                return 101110203;
            case 101120101:
                return 101120100;
            case 101120103:
                return 101120102;
            case 101120105:
            case 101120106:
                return 101120104;
            case 101120203:
                return 101120202;
            case 400031021:
                return 400031020;
            case 101120205:
            case 101120206:
                return 101120204;
            case 101120200:
                return 101121200;
            case 100001266:
            case 100001269:
                return 100001265;
            case 11001025:
                return 11120117;

            case 80002644: {
                return 80002632;
            }
            case 80011615: {
                return 80011615;
            }
            case 80011612: {
                return 80011613;
            }
            case 80011611: {
                return 80011610;
            }
            case 80011609: {
                return 80011608;
            }
            case 80011607: {
                return 80011606;
            }
            case 80011605: {
                return 80011604;
            }
            case 80011603: {
                return 80011602;
            }
            case 21110003: {
                return 21111013;
            }
            case 21110006: {
                return 21111014;
            }
            case 21120005: {
                return 21121013;
            }
            case 21121055:
            case 21121056: {
                return 21120052;
            }
            case 21110007:
            case 21110008:
            case 21110015: {
                return 21110002;
            }
            case 21120009:
            case 21120010:
            case 21120015: {
                return 21120002;
            }
            case 35101009:
            case 35101010: {
                return 35100008;
            }
            case 35111009:
            case 35111010: {
                return 35111001;
            }
            case 35121013: {
                return 35111004;
            }
            case 5710012: {
                return 5711002;
            }
            case 80002887: {
                return 80001757;
            }
            case 80012015: {
                return 80011993;
            }
            case 400001064:
                return 400001036;
            case 162101006:
            case 162101007:
                return 162100005;
            case 162101003:
            case 162101004:
                return 162100002;
            case 162121044:
                return 162121043;
            case 400021131:
                return 400021130;
            case 162121003:
            case 162121004:
                return 162120002;
            case 162121006:
            case 162121007:
                return 162120005;
            case 162121009:
            case 162121010:
                return 162120008;
            case 162111010:
                return 162111002;
            case 162101009:
            case 162101010:
            case 162101011:
                return 162100008;
            case 162121012:
            case 162121013:
            case 162121014:
            case 162121015:
            case 162121016:
            case 162121017:
            case 162121018:
            case 162121019:
                return 162120011;
            case 135001004:
            case 135003003:
            case 135003004:
                return 135001003;
            case 400041023:
            case 400041024:
            case 400041080:
                return 400041022;
            case 400001052:
                return 400001007;
            case 131002025:
                return 131001025;
            case 131002026:
            case 131003026:
                return 131001026;
            case 131002015:
                return 131001015;
            case 131002023:
            case 131003023:
            case 131004023:
            case 131005023:
            case 131006023:
                return 131001023;
            case 131002022:
            case 131003022:
            case 131004022:
            case 131005022:
            case 131006022:
                return 131001022;
            case 131001113:
            case 131001213:
            case 131001313:
                return 131001013;
            case 132001017:
            case 133001017:
                return 131001017;
            case 63001003:
            case 63001005:
                return 63001002;
            case 135002018:
                return 135001018;
            case 400001060:
                return 400001059;
            case 14001031:
                return 14001023;
            case 155121004:
                return 155121102;
            case 400011065:
                return 400011055;
            case 400011092:
            case 400011093:
            case 400011094:
            case 400011095:
            case 400011096:
            case 400011097:
            case 400011103:
                return 400011091;
            case 37120055:
            case 37120056:
            case 37120057:
            case 37120058:
            case 37120059:
                return 37121052;
            case 37110001:
            case 37110002:
                return 37111000;
            case 14001030:
                return 14001026;
            case 2121055:
                return 2121052;
            case 400001011:
                return 400001010;
            case 400041056:
                return 400041055;
            case 400021100:
            case 400021111:
                return 400021099;
            case 400041058:
                return 400041057;
            case 400011135:
                return 400011134;
            case 400021097:
            case 400021098:
            case 400021104:
                return 400021096;
            case 400011119:
            case 400011120:
                return 400011118;
            case 400051069:
                return 400051068;
            case 400011111:
                return 400011110;
            case 400021088:
            case 400021089:
                return 400021087;
            case 400011113:
            case 400011114:
            case 400011115:
            case 400011129:
                return 400011112;
            case 400021112:
                return 400021094;
            case 400031045:
                return 400031044;
            case 400011085:
                return 400011047;
            case 400051079:
                return 400051078;
            case 400051075:
            case 400051076:
            case 400051077:
                return 400051074;
            case 400011132:
                return 400011131;
            case 400011122:
                return 400011121;
            case 400031059:
                return 400031058;
            case 400041060:
                return 400041059;
            case 400051059:
            case 400051060:
            case 400051061:
            case 400051062:
            case 400051063:
            case 400051064:
            case 400051065:
            case 400051066:
            case 400051067:
                return 400051058;
            case 400031047:
            case 400031048:
            case 400031049:
            case 400031050:
            case 400031051:
                return 400031057;
            case 400051071:
                return 400051070;
            case 400041070:
            case 400041071:
            case 400041072:
            case 400041073:
                return 400041069;
            case 400041076:
            case 400041077:
            case 400041078:
                return 400041075;
            case 400041062:
            case 400041079:
                return 400041061;
            case 5120021:
                return 5121013;
            case 25111211:
                return 25111209;
            case 400031031:
                return 400031030;
            case 400031054:
                return 400031053;
            case 400031056:
                return 400031055;
            case 30001078:
            case 30001079:
            case 30001080:
                return 30001068;
            case 61121026:
                return 61121102;
            case 400001040:
            case 400001041:
                return 400001039;
            case 400041051:
                return 400041050;
            case 400001044:
                return 400001043;
            case 151101004:
            case 151101010:
                return 151101003;
            case 131001001:
            case 131001002:
            case 131001003:
                return 131001000;
            case 131001106:
            case 131001206:
            case 131001306:
            case 131001406:
            case 131001506:
                return 131001006;
            case 131001107:
            case 131001207:
            case 131001307:
                return 131001007;
            case 24121010:
                return 24121003;
            case 24111008:
                return 24111006;
            case 151101007:
            case 151101008:
                return 151101006;
            case 142120001:
                return 142120000;
            case 142110003:
                return 142111002;
            case 400041049:
                return 400041048;
            case 400041053:
                return 400041052;
            case 37000009:
                return 37001001;
            case 37100008:
                return 37100007;
            case 151001003:
                return 151001002;
            case 400001051:
            case 400001053:
            case 400001054:
            case 400001055:
                return 400001050;
            case 95001000:
                return 3111013;
            case 400031018:
            case 400031019:
                return 400031017;
            case 164111016:
                return 164111003;
            case 164111001:
            case 164111002:
            case 164111009:
            case 164111010:
            case 164111011:
                return 164110000;
            case 400001047:
            case 400001048:
            case 400001049:
                return 400001046;
            case 164001002:
                return 164001001;
            case 151121011:
                return 151121004;
            case 164101001:
            case 164101002:
                return 164100000;
            case 164101004:
                return 164101003;
            case 164121001:
            case 164121002:
            case 164121014:
                return 164120000;
            case 164121004:
                return 164121003;
            case 164121015:
                return 164121008;
            case 164120007:
                return 164121007;
            case 164121044:
                return 164121043;
            case 164121011:
            case 164121012:
                return 164121006;
            case 164111004:
            case 164111005:
            case 164111006:
                return 164111003;
            case 400031035:
                return 400031034;
            case 400031038:
            case 400031039:
            case 400031040:
            case 400031041:
            case 400031042:
            case 400031043:
                return 400031037;
            case 31011004:
            case 31011005:
            case 31011006:
            case 31011007:
                return 31011000;
            case 31201007:
            case 31201008:
            case 31201009:
            case 31201010:
                return 31201000;
            case 31211007:
            case 31211008:
            case 31211009:
            case 31211010:
                return 31211000;
            case 31221009:
            case 31221010:
            case 31221011:
            case 31221012:
                return 31221000;
            case 3311011:
                return 3311010;
            case 3011006:
            case 3011007:
            case 3011008:
                return 3011005;
            case 3301009:
                return 3301008;
            case 3301004:
                return 3301003;
            case 3321003:
            case 3321004:
            case 3321005:
            case 3321006:
            case 3321007:
                return 3320002;
            case 3321036:
            case 3321037:
            case 3321038:
            case 3321039:
            case 3321040:
                return 3321035;
            case 3321016:
            case 3321017:
            case 3321018:
            case 3321019:
            case 3321020:
            case 3321021:
                return 3321014;
            case 21000004:
                return 21001009;
            case 142100010:
                return 142101009;
            case 142100008:
                return 142101002;
            case 27120211:
                return 27121201;
            case 33121255:
                return 33121155;
            case 33101115:
                return 33101215;
            case 37000005:
                return 37001004;
            case 400011074:
            case 400011075:
            case 400011076:
                return 400011073;
            case 33001202:
                return 33001102;
            case 152000009:
                return 152000007;
            case 152001005:
                return 152001004;
            case 152120002:
                return 152120001;
            case 152101000:
            case 152101004:
                return 152101003;
            case 152121006:
                return 152121005;
            case 400051019:
            case 400051020:
                return 400051018;
            case 152110004:
            case 152120016:
            case 152120017:
                return 152001001;
            case 400021064:
            case 400021065:
                return 400021063;
            case 1100012:
                return 1101012;
            case 1111014:
                return 1111008;
            case 2100010:
                return 2101010;
            case 61111114:
            case 61111221:
                return 61111008;
            case 14121055:
            case 14121056:
                return 14121054;
            case 61121220:
                return 61121015;
            case 400031008:
            case 400031009:
                return 400031007;
            case 142120030:
                return 142121030;
            case 400051039:
            case 400051052:
            case 400051053:
                return 400051038;
            case 400021043:
            case 400021044:
            case 400021045:
                return 400021042;
            case 400051049:
            case 400051050:
                return 400051040;
            case 400040006:
                return 400041006;
            case 155001204:
                return 155001104;
            case 400031026:
            case 400031027:
                return 400031025;
            case 61121222:
                return 61121105;
            case 400020046:
            case 400020051:
            case 400021013:
            case 400021014:
            case 400021015:
            case 400021016:
                return 400021012;
            case 61121116:
            case 61121124:
            case 61121221:
            case 61121223:
            case 61121225:
                return 61121104;
            case 400011002:
                return 400011001;
            case 400010030:
                return 400011031;
            case 400051051:
                return 400051041;
            case 400021077:
                return 400021070;
            case 2120013:
                return 2121007;
            case 2220014:
                return 2221007;
            case 32121011:
                return 32121004;
            case 400011059:
            case 400011060:
            case 400011061:
                return 400011058;
            case 400021075:
            case 400021076:
                return 400021074;
            case 400011033:
            case 400011034:
            case 400011035:
            case 400011036:
            case 400011037:
            case 400011067:
                return 400011032;
            case 400011080:
            case 400011081:
            case 400011082:
                return 400011079;
            case 400011084:
                return 400011083;
            case 21120026:
                return 21120019;
            case 400020009:
            case 400020010:
            case 400020011:
            case 400021010:
            case 400021011:
                return 400021008;
            case 400041026:
            case 400041027:
                return 400041025;
            case 400040008:
            case 400041019:
                return 400041008;
            case 400041003:
            case 400041004:
            case 400041005:
                return 400041002;
            case 400051045:
                return 400051044;
            case 400011078:
                return 400011077;
            case 400031016:
                return 400031015;
            case 400031013:
            case 400031014:
                return 400031012;
            case 400011102:
                return 400011090;
            case 400020002:
                return 400021002;
            case 22140023:
                return 22140014;
            case 22140024:
                return 22140015;
            case 22141012:
                return 22140022;
            case 22110014:
            case 22110025:
                return 22110014;
            case 22170061:
                return 22170060;
            case 22170093:
                return 22170064;
            case 22171083:
                return 22171080;
            case 22170094:
                return 22170065;
            case 400011069:
                return 400011068;
            case 400031033:
                return 400031032;
            case 25121133:
                return 25121131;
            case 23121015:
                return 23121014;
            case 24120055:
                return 24121052;
            case 31221014:
                return 31221001;
            case 400021031:
            case 400021040:
                return 400021030;
            case 4120019:
                return 4120018;
            case 37000010:
                return 37001001;
            case 155001000:
                return 155001001;
            case 155001009:
                return 155001104;
            case 155100009:
                return 155101008;
            case 155101002:
                return 155101003;
            case 155101013:
            case 155101015:
            case 155101101:
            case 155101112:
                return 155101100;
            case 155101114:
                return 155101104;
            case 155101214:
                return 155101204;
            case 155101201:
            case 155101212:
                return 155101200;
            case 155111002:
            case 155111111:
                return 155111102;
            case 155111103:
            case 155111104:
                return 155111105;
            case 155111106:
                return 155111102;
            case 155111211:
            case 155111212:
                return 155111202;
            case 155121002:
                return 155121102;
            case 155121003:
                return 155121005;
            case 155121006:
            case 155121007:
                return 155121306;
            case 155121215:
                return 155121202;
            case 400041010:
            case 400041011:
            case 400041012:
            case 400041013:
            case 400041014:
            case 400041015:
                return 400041009;
            case 400011099:
                return 400011098;
            case 400011101:
                return 400011100;
            case 400011053:
                return 400011052;
            case 400001016:
                return 400001013;
            case 400021029:
                return 400021028;
            case 400030002:
                return 400031002;
            case 400021049:
            case 400021050:
                return 400021041;
            case 14000027:
            case 14000028:
            case 14000029:
                return 14001027;
            case 4100011:
            case 4100012:
                return 4101011;
            case 5211015:
            case 5211016:
                return 5211011;
            case 5220023:
            case 5220024:
            case 5220025:
                return 5221022;
            case 51001006:
            case 51001007:
            case 51001008:
            case 51001009:
            case 51001010:
            case 51001011:
            case 51001012:
            case 51001013:
                return 51001005;
            case 25120115:
                return 25120110;
            case 5201005:
                return 5201011;
            case 5320011:
                return 5321004;
            case 33001008:
            case 33001009:
            case 33001010:
            case 33001011:
            case 33001012:
            case 33001013:
            case 33001014:
            case 33001015:
                return 33001007;
            case 65120011:
                return 65121011;
            case 400041034:
                return 400041033;
            case 400041036:
                return 400041035;
            case 21110027:
            case 21110028:
            case 21111021:
                return 21110020;
            case 100000276:
            case 100000277:
                return 100000267;
            case 400001025:
            case 400001026:
            case 400001027:
            case 400001028:
            case 400001029:
            case 400001030:
                return 400001024;
            case 400001015:
                return 400001014;
            case 400011013:
            case 400011014:
                return 400011012;
            case 400001022:
                return 400001019;
            case 400021033:
            case 400021052:
                return 400021032;
            case 400041016:
                return 4001344;
            case 400041017:
                return 4111010;
            case 400041018:
                return 4121013;
            case 400051003:
            case 400051004:
            case 400051005:
                return 400051002;
            case 400051025:
            case 400051026:
                return 400051024;
            case 400051023:
                return 400051022;
            case 2321055:
                return 2321052;
            case 5121055:
                return 5121052;
            case 61111220:
                return 61111002;
            case 12001027:
            case 12001028:
                return 12000023;
            case 36121013:
            case 36121014:
                return 36121002;
            case 36121011:
            case 36121012:
                return 36121001;
            case 400010010:
                return 400011010;
            case 10001253:
            case 10001254:
            case 14001026:
                return 10000252;
            case 142000006:
                return 142001004;
            case 4321001:
                return 4321000;
            case 33101006:
            case 33101007:
                return 33101005;
            case 33101008:
                return 33101004;
            case 35121011:
                return 35121009;
            case 3000008:
            case 3000009:
            case 3000010:
                return 3001007;
            case 32001007:
            case 32001008:
            case 32001009:
            case 32001010:
            case 32001011:
                return 32001001;
            case 64001007:
            case 64001008:
            case 64001009:
            case 64001010:
            case 64001011:
            case 64001012:
                return 64001000;
            case 64001013:
                return 64001002;
            case 64100001:
                return 64100000;
            case 64001006:
                return 64001001;
            case 64101008:
                return 64101002;
            case 64111012:
                return 64111004;
            case 64121012:
            case 64121013:
            case 64121014:
            case 64121015:
            case 64121017:
            case 64121018:
            case 64121019:
                return 64121001;
            case 64121022:
            case 64121023:
            case 64121024:
                return 64121021;
            case 64121016:
                return 64121003;
            case 64121055:
                return 64121053;
            case 5300007:
                return 5301001;
            case 23101007:
                return 23101001;
            case 31001006:
            case 31001007:
            case 31001008:
                return 31000004;
            case 30010183:
            case 30010184:
            case 30010186:
                return 30010110;
            case 25000001:
                return 25001000;
            case 25000003:
                return 25001002;
            case 25100001:
            case 25100002:
                return 25101000;
            case 25100010:
                return 25100009;
            case 25110001:
            case 25110002:
            case 25110003:
                return 25111000;
            case 25120001:
            case 25120002:
            case 25120003:
                return 25121000;
            case 1111002:
                return 1101013;
            case 3120019:
                return 3111009;
            case 5201013:
            case 5201014:
                return 5201012;
            case 5210016:
            case 5210017:
            case 5210018:
                return 5210015;
            case 11121055:
                return 11121052;
            case 12120011:
                return 12121001;
            case 12121055:
                return 12121054;
            case 12120013:
            case 12120014:
                return 12121004;
            case 14101029:
                return 14101028;
            case 61110211:
            case 61120007:
            case 61121217:
                return 61101002;
            case 61120008:
                return 61111008;
            case 61121201:
                return 61121100;
            case 65111007:
                return 65111100;
            case 36111009:
            case 36111010:
                return 36111000;
        }
        if (skillId == 155101204) {
            return 155101104;
        }
        return skillId;
    }

    public static boolean isForceIncrease(int skillId) {
        switch (skillId) {
            case 31000004:
            case 31001006:
            case 31001007:
            case 31001008:
            case 31100007:
            case 31110010:
            case 31120011:
                return true;
        }
        return false;
    }

    public static boolean is超越攻擊(int skillId) {
        switch (skillId) {
            case 惡魔復仇者.超越_十文字斬:
            case 惡魔復仇者.超越_十文字斬_1:
            case 惡魔復仇者.超越_十文字斬_2:
            case 惡魔復仇者.超越_十文字斬_3:
            case 惡魔復仇者.超越_十文字斬_4:
            case 惡魔復仇者.超越_惡魔風暴:
            case 惡魔復仇者.超越_惡魔風暴_1:
            case 惡魔復仇者.超越_惡魔風暴_2:
            case 惡魔復仇者.超越_惡魔風暴_3:
            case 惡魔復仇者.超越_惡魔風暴_4:
            case 惡魔復仇者.超越_月光斬:
            case 惡魔復仇者.超越_月光斬_1:
            case 惡魔復仇者.超越_月光斬_2:
            case 惡魔復仇者.超越_月光斬_3:
            case 惡魔復仇者.超越_月光斬_4:
            case 惡魔復仇者.超越_逆十文字斬:
            case 惡魔復仇者.超越_逆十文字斬_1:
            case 惡魔復仇者.超越_逆十文字斬_2:
            case 惡魔復仇者.超越_逆十文字斬_3:
            case 惡魔復仇者.超越_逆十文字斬_4:
                return true;
        }
        return false;
    }

    public static int getMPEaterForJob(int job) {
        switch (job) {
            case 210:
            case 211:
            case 212:
                return 火毒.魔力吸收;
            case 220:
            case 221:
            case 222:
                return 冰雷.魔力吸收;
            case 230:
            case 231:
            case 232:
                return 主教.魔力吸收;
        }
        return 火毒.魔力吸收; //魔力吸收 Default, in case GM
    }

    public static boolean isPyramidSkill(int skill) {
        return JobConstants.is零轉職業(skill / 10000) && skill % 10000 == 1020;
    }

    public static boolean isInflationSkill(int skill) {
        return JobConstants.is零轉職業(skill / 10000) && (skill % 10000 >= 1092 && skill % 10000 <= 1095);
    }

    public static boolean isMulungSkill(int skill) {
        return JobConstants.is零轉職業(skill / 10000) && (skill % 10000 == 1009 || skill % 10000 == 1010 || skill % 10000 == 1011);
    }

    public static boolean isIceKnightSkill(int skill) {
        return JobConstants.is零轉職業(skill / 10000) && (skill % 10000 == 1098 || skill % 10000 == 99 || skill % 10000 == 100 || skill % 10000 == 103 || skill % 10000 == 104 || skill % 10000 == 1105);
    }

    public static boolean is騎乘技能(int skill) {
        return JobConstants.is零轉職業(skill / 10000) && skill % 10000 == 1004;
    }

    public static int getAttackDelay(int skillId, Skill skill) {
        switch (skillId) {
            case 火毒.致命毒霧:
                return 0;
            case 3111009:
            case 33121009:
            case 35111004:
            case 35121005:
            case 35121013:
            case 箭神.箭座:
            case 精靈遊俠.伊修塔爾之環:
            case 惡魔復仇者.蝙蝠群:
            case 黑騎士.拉曼查之槍:
            case 槍神.海盜砲擊艇:
            case 槍神.海盜砲擊艇_1:
            case 槍神.海盜砲擊艇_2:
            case 槍神.砲艇標記:
                return 40;
            case 夜使者.三飛閃:
                return 99;
            case 破風使者.寒冰亂舞:
            case 破風使者.天空之歌:
                return 120;
            case 幻影俠盜.炫目卡牌:
            case 幻影俠盜.死神卡牌:
            case 幻影俠盜.連犽突進:
            case 幻影俠盜.卡牌風暴:
            case 卡莉.魔咒_混亂:
            case 卡莉.藝術_阿斯特拉:
            case 夜光.晨星殞落:
            case 傑諾.追縱火箭:
            case 凱撒.意志之劍:
            case 凱撒.進階意志之劍:
            case 凱撒.意志之劍_變身:
            case 凱撒.進階意志之劍_變身:
            case 夜使者.刺客刻印_飛鏢:
            case 夜使者.夜使者的標記:
                return 30;
            case 32121003:
            case 冰雷.冰鋒刃:
                return 180;
            case 夜光.晨星殞落_爆炸:
            case 凱撒.龍劍風:
            case 凱撒.展翅飛翔:
                return 210;
            case 21110007:
            case 21110008:
            case 21120009:
            case 21120010:
                return 390;
            case 惡魔殺手.惡魔狂斬1:
            case 惡魔殺手.惡魔狂斬2:
            case 惡魔殺手.惡魔狂斬3:
                return 270;
            case 惡魔殺手.變形:
                return 510;
            case 凱撒.劍龍連斬:
            case 凱撒.劍龍連斬_1:
            case 凱撒.劍龍連斬_2:
                return 240;
            case 天使破壞者.繼承人:
            case 天使破壞者.靈魂震動:
                return 180;
            case 傑諾.疾風劍舞:
                return 120;
            case 0: // Normal Attack, TODO delay for each weapon type
                return 330;
        }
        if (skill != null && skill.getSkillType() == 3) {
            return 0; //final attack
        }
        if (skill != null && skill.getDelay() > 0 && skillId != 21101003 && skillId != 33101004 && skillId != 32111010 && skillId != 火毒.瞬間移動精通 && skillId != 冰雷.瞬間移動精通 && skillId != 主教.瞬間移動精通 && skillId != 22161005 && skillId != 機甲戰神.戰鬥機器_巨人錘 && skillId != 22150004 && skillId != 22181004) {
            return skill.getDelay();
        }
        return 330; // Default usually
    }

    /*
     * 管理員技能
     */
    public static boolean isAdminSkill(int skillId) {
        int jobId = skillId / 10000;
        return jobId == 800 || jobId == 900;
    }

    /*
     * 特殊技能
     */
    public static boolean isSpecialSkill(int skillId) {
        int jobId = skillId / 10000;
        return jobId == 7000 || jobId == 7100 || jobId == 8000 || jobId == 9000 || jobId == 9100 || jobId == 9200 || jobId == 9201 || jobId == 9202 || jobId == 9203 || jobId == 9204;
    }

    public static boolean isApplicableSkill(int skillId) {
        return ((skillId < 80000000 || skillId >= 100000000) && (skillId % 10000 < 8000 || skillId % 10000 > 8006) && !is天使祝福戒指(skillId)) || skillId >= 92000000 || (skillId >= 80000000 && skillId < 80020000); //no additional/decent skills
    }

    public static boolean isApplicableSkill_(int skillId) { //not applicable to saving but is more of temporary
        for (int i : PlayerStats.pvpSkills) {
            if (skillId == i) {
                return true;
            }
        }
        return (skillId >= 90000000 && skillId < 92000000) || (skillId % 10000 >= 8000 && skillId % 10000 <= 8003) || is天使祝福戒指(skillId);
    }

    public static boolean isNoDelaySkill(int skillId) {
        switch (skillId) {
            case 狂狼勇士.強化連擊:
            case 火毒.瞬間移動精通:
            case 冰雷.瞬間移動精通:
            case 主教.瞬間移動精通:
            case 機甲戰神.合金盔甲_人型:
            case 機甲戰神.合金盔甲_戰車:
            case 機甲戰神.戰鬥機器_巨人錘:
            case 龍魔導士.龍之捷:
            case 龍魔導士.龍之火花:
            case 龍魔導士.龍之捷_1:
            case 龍魔導士.龍之捷_2:
            case 龍魔導士.風之捷_攻擊:
            case 龍魔導士.歐尼斯的意志:
            case 影武者.修羅:
            case 狂豹獵人.召喚美洲豹_銀灰:
            case 狂豹獵人.召喚美洲豹_暗黃:
            case 狂豹獵人.召喚美洲豹_血紅:
            case 狂豹獵人.召喚美洲豹_紫光:
            case 狂豹獵人.召喚美洲豹_深藍:
            case 狂豹獵人.召喚美洲豹_傑拉:
            case 狂豹獵人.召喚美洲豹_白雪:
            case 狂豹獵人.召喚美洲豹_歐尼斯:
            case 狂豹獵人.召喚美洲豹_地獄裝甲:
            case 煉獄巫師.死神:
            case 煉獄巫師.死神契約I:
            case 煉獄巫師.死神契約II:
            case 煉獄巫師.死神契約III:
            case 煉獄巫師.黑暗閃電:
            case 隱月.小狐仙:
            case 凱內西斯.永恆壞滅:
            case 冰雷.冰鋒刃:
            case 暗影神偷.暗影霧殺:
            case 惡魔殺手.變形:
            case 凱撒.意志之劍:
            case 凱撒.進階意志之劍:
            case 凱內西斯.心靈領域:
            case 凱內西斯.終極技_BPM:
            case 狂豹獵人.連弩陷阱:
            case 狂豹獵人.鑽孔集裝箱:
            case 80011133:// MX-131.支援射擊
                return true;
        }
        return false;
    }

    public static boolean isNoApplyAttack(int skillId) {
        switch (skillId) {
            case 80002890: // 轉移之輪
            case 阿戴爾.復原:
            case 阿戴爾.乙太風暴:
            case 墨玄.墨玄_三轉_密技_移形換位:
            case 菈菈.山之種子:
            case 主教.群體治癒:
                return true;
        }
        return false;
    }

    public static boolean is召喚獸戒指(int skillID) {
        switch (skillID) {
            case 1085:
            case 1087:
            case 80000052:
            case 80000053:
            case 80000054:
            case 80000086:
            case 80000155:
            case 80001154:
            case 80001262:
            case 80001518:
            case 80001519:
            case 80001520:
            case 80001521:
            case 80001522:
            case 80001523:
            case 80001524:
            case 80001525:
            case 80001526:
            case 80001527:
            case 80001528:
            case 80001529:
            case 80001530:
            case 80001531:
            case 80010067:
            case 80010068:
            case 80010069:
            case 80010070:
            case 80010071:
            case 80010072:
            case 80010075:
            case 80010076:
            case 80010077:
            case 80010078:
            case 80010079:
            case 80010080:
            case 80011103:
            case 80011104:
            case 80011105:
            case 80011106:
            case 80011107:
            case 80011108: {
                return true;
            }
            default: {
                return dZ(getSkillRoot(skillID)) && (skillID % 10000 == 1085 || skillID % 10000 == 1087 || skillID % 10000 == 1090 || skillID % 10000 == 1179);
            }
        }
//        return is天使祝福戒指(skillId);
    }

    public static boolean is天使祝福戒指(int skillId) {
        return JobConstants.is零轉職業(skillId / 10000) && (skillId % 10000 == 1085 || skillId % 10000 == 1087 || skillId % 10000 == 1090 || skillId % 10000 == 1179);
    }

    public static boolean is天氣戒指(int skillId) {
        return JobConstants.is零轉職業(skillId / 10000) && (skillId / 10000 == 8001) && (skillId % 10000 >= 67 && skillId % 10000 <= 80);
    }

    /*
     * 角色卡系統
     * 1 = B
     * 2 = A
     * 3 = S
     * 4 = SS
     */
    public static int getCardSkillLevel(int level) {
        if (level >= 60 && level < 100) {
            return 2;
        } else if (level >= 100 && level < 200) {
            return 3;
        } else if (level >= 200) {
            return 4;
        }
        return 1;
    }

    /*
     * 技能的模式
     */
    public static int getLuminousSkillMode(int skillId) {
        switch (skillId) {
            case 夜光.星星閃光:
            case 夜光.光明長槍:
            case 夜光.光柱爆發:
            case 夜光.光箭:
            case 夜光.閃亮救贖:
            case 夜光.極速反射:
                return 夜光.光蝕; //光明技能 20040216 - 太陽火焰 - 使用充滿光明的光之魔法後，造成額外傷害。每次施展魔法時，恢復一定比例的體力，MP使用量減少50%。
            case 夜光.黑暗球體:
            case 夜光.黑暗之眼:
            case 夜光.黑暗之錨:
            case 夜光.晨星殞落:
            case 夜光.暗黑烈焰:
            case 夜光.晨星殞落_爆炸:
                return 夜光.暗蝕; //黑暗技能 20040217 - 月蝕 - 使用充滿黑暗的暗之魔法後，造成額外傷害。每次施展魔法時，恢復一定比例的體力，MP使用量減少50%。
            case 夜光.死神鐮刀:
            case 夜光.絕對擊殺:
                return 夜光.平衡_光明; //平衡技能 20040219 - 平衡 - 使用光明和黑暗完美平衡的穩如泰山，並使所有傷害減至1。使用光明、黑暗，混合魔法時產生額外傷害。無冷卻時間，施展光明攻擊魔法時，恢復一定比例的體力；施展黑暗攻擊魔法時，不消耗MP。
        }
        return -1;
    }

    public static int getSoulMasterAttackMode(int skillid) {
        switch (skillid) {
            case 聖魂劍士.月光分裂_I:
            case 聖魂劍士.月光分裂_II:
            case 聖魂劍士.月光分裂_III:
            case 聖魂劍士.天體觀測III:
            case 聖魂劍士.雙重精通_沉月:
            case 聖魂劍士.光輝衝刺:
            case 聖魂劍士.月光之舞_空中old: {
                return 1;
            }
            case 聖魂劍士.烈日狂斬_I:
            case 聖魂劍士.烈日狂斬_II:
            case 聖魂劍士.烈日狂斬_III:
            case 聖魂劍士.皇家衝擊:
            case 聖魂劍士.天體觀測:
            case 聖魂劍士.宇宙轟炸:
            case 聖魂劍士.旭日:
            case 聖魂劍士.熾烈突擊:
            case 聖魂劍士.雙重狂斬: {

                return 2;
            }
        }
        return -1;
    }

    public static boolean isShowForgenBuff(SecondaryStat buff) {
        switch (buff) {
            case CoalitionSupportSoldierStorm:
            case IndieMDF:
            case DarkSight:
            case SoulArrow:
            case Stun:
            case Poison:
            case Seal:
            case Darkness:
            case ComboCounter:
            case BlessedHammer:
            case BlessedHammerActive:
            case WeaponCharge:
            case ShadowPartner:
            case Weakness:
            case Curse:
            case Slow:
            case Morph:
            case Stance:
            case Attract:
            case NoBulletConsume:
            case BanMap:
            case Ghost:
            case Barrier:
            case ReverseInput:
            case RespectPImmune:
            case RespectMImmune:
            case DefenseState:
            case DojangBerserk:
            case DojangInvincible:
            case DojangShield:
            case WindBreakerFinal:
            case HideAttack:
            case RepeatEffect:
            case StopPortion:
            case StopMotion:
            case Fear:
            case HiddenPieceOn:
            case MagicShield:
            case Flying:
            case Frozen:
            case DrawBack:
            case NotDamaged:
            case FinalCut:
            case Dance:
            case Sneak:
            case Mechanic:
            case BlessingArmor:
            case Beholder:
            case Inflation:
            case Web:
            case DisOrder:
            case Thread:
            case Team:
            case Explosion:
            case PvPRaceEffect:
            case WeaknessMdamage:
            case Frozen2:
            case Shock:
            case HolyMagicShell:
            case DamAbsorbShield:
            case DevilishPower:
            case SpiritLink:
            case Event:
            case Lapidification:
            case PyramidEffect:
            case KeyDownMoving:
            case IgnoreTargetDEF:
            case Invisible:
            case Judgement:
            case Magnet:
            case MagnetArea:
            case GuidedArrow:
            case StraightForceAtomTargets:
            case LefBuffMastery:
            case TempSecondaryStat:
            case KeyDownAreaMoving:
            case Larkness:
            case StackBuff:
            case AntiMagicShell:
            case SmashStack:
            case ReshuffleSwitch:
            case StopForceAtomInfo:
            case SoulGazeCriDamR:
            case PowerTransferGauge:
            case AffinitySlug:
            case MobZoneState:
            case ComboUnlimited:
            case SoulExalt:
            case IgnorePImmune:
            case IgnoreAllAbout:
            case IceAura:
            case FireAura:
            case VengeanceOfAngel:
            case HeavensDoor:
            case BleedingToxin:
            case IgnoreMobDamR:
            case Asura:
            case MegaSmasher:
            case ReturnTeleport:
            case CapDebuff:
            case OverloadCount:
            case SurplusSupply:
            case NewFlying:
            case AmaranthGenerator:
            case CygnusElementSkill:
            case StrikerHyperElectric:
            case Translucence:
            case PoseType:
            case CosmicForge:
            case ElementSoul:
            //case GlimmeringTime:
            case FullSoulMP:
            case ElementalCharge:
            case Reincarnation:
            case NaviFlying:
            case QuiverCatridge:
            case UserControlMob:
            case ImmuneBarrier:
            case ZeroAuraStr:
            case SpiritGuard:
            case JaguarSummoned:
            case BMageAuraYellow:
            case DarkLighting:
            case AttackCountX:
            case FireBarrier:
            case Frenzy:
            case ShadowSpear:
            case MastemaGuard:
            case BattlePvP_Helena_Mark:
            case BattlePvP_LangE_Protection:
            case PinkbeanRollingGrade:
            case MichaelSoulLink:
            case MichaelStanceLink:
            case KinesisPsychicEnergeShield:
            case Fever:
            case AdrenalinBoost:
            case RWVulkanPunch:
            case RWMagnumBlow:
            case RWBarrier:
            case MahaInstall:
            case TransformOverMan:
            case EnergyBust:
            case LightningUnion:
            case BulletParty:
            case BishopPray:
            case Kinesis_DustTornado:
            case FifthAdvWarriorShield:
            case FreudBlessing:
            case OverloadMode:
            case FifthSpotLight:
            case OutSide:
            case LefGloryWing:
            case ConvertAD:
            case EtherealForm:
            case ReadyToDie:
            case Cr2CriDamR:
            case HitStackDamR:
            case BuffControlDebuff:
            case DispersionDamage:
            case HarmonyLink:
            case LefFastCharge:
            case BattlePvP_Ryude_Frozen:
            case RepeatEffect2:
//            case ShamanMode:
//            case Chachacha:
            case AntiEvilShield:
            case BladeStanceMode:
            case BladeStancePower:
            case SelfHyperBodyIncPAD:
            case SelfHyperBodyMaxHP:
            case SelfHyperBodyMaxMP:
            case CriticalBuffAdd:
            case BossDamageRate:
            case KenjiCounter:
            case SkillDeployment:
//            case WaterSmashTeam:
//            case WaterSmashClass:
//            case WaterSmashBuffCount:
            case DashSpeed:
            case DashJump:
            case RideVehicle:
            case PartyBooster:
            case GuidedBullet:
            case Undead:
            case RelicGauge:
            case RideVehicleExpire:
            case SecondAtomLockOn: {
                return true;
            }
        }
        return false;
    }

    public static boolean isMovementAffectingStat(SecondaryStat buffStat) {
        switch (buffStat) {
            case Speed:
            case Jump:
            case Stun:
            case Weakness:
            case Slow:
            case Morph:
            case Ghost:
            case BasicStatUp:
            case Attract:
            case DashSpeed:
            case DashJump:
            case Flying:
            case Frozen:
            case Frozen2:
            case Lapidification:
            case IndieSpeed:
            case IndieJump:
            case KeyDownMoving:
            case Mechanic:
            case Magnet:
            case MagnetArea:
            case VampDeath:
            case VampDeathSummon:
            case GiveMeHeal:
            case DarkTornado:
            case NewFlying:
            case NaviFlying:
            case UserControlMob:
            case Dance:
            case SelfWeakness:
            case BattlePvP_Helena_WindSpirit:
            case BattlePvP_LeeMalNyun_ScaleUp:
            case TouchMe:
            case IndieForceSpeed:
            case IndieForceJump:
            case DarkSight:
            case Shock:
            case SmashStack:
            case FireAura:
            case CapDebuff:
            case RideVehicle:
                return true;
        }
        return false;
    }

    public static boolean isWriteBuffIntValue(SecondaryStat buffStat) {
        switch (buffStat) {
            case FifthAdvWarriorShield:
            case ShadowPartner:
            case MagnetArea:
            case SpiritLink:
            case SoulGazeCriDamR:
            case PowerTransferGauge:
            case ReturnTeleport:
            case ImmuneBarrier:
            case QuiverCatridge:
            case NaviFlying:
            case Dance:
            case RideVehicle:
            case RideVehicleExpire:
            case RWBarrier:
            case MegaSmasher:
            case Cyclone:
            case CarnivalDefence:
            case DojangLuckyBonus:
            case VampDeath:
            case BossShield:
            case SetBaseDamage:
            case DotHealHPPerSecond:
            case DotHealMPPerSecond:
            case SetBaseDamageByBuff:
            case PairingUser: {
                return true;
            }
        }
        return false;
    }

    public static boolean isSpecialStackBuff(SecondaryStat buffStat) {
        switch (buffStat) {
            case DashSpeed:
            case DashJump:
            case GuidedBullet:
            case Undead:
            case RideVehicleExpire:
            case RideVehicle:
            case PartyBooster:
            case SecondAtomLockOn:
            case Curse: {
                return true;
            }
        }
        return false;
    }

    public static boolean is美洲豹(int skillId) {
        switch (skillId) {
            case 狂豹獵人.召喚美洲豹_銀灰:
            case 狂豹獵人.召喚美洲豹_暗黃:
            case 狂豹獵人.召喚美洲豹_血紅:
            case 狂豹獵人.召喚美洲豹_紫光:
            case 狂豹獵人.召喚美洲豹_深藍:
            case 狂豹獵人.召喚美洲豹_傑拉:
            case 狂豹獵人.召喚美洲豹_白雪:
            case 狂豹獵人.召喚美洲豹_歐尼斯:
            case 狂豹獵人.召喚美洲豹_地獄裝甲:
                return true;
        }
        return false;
    }

    public static boolean isRuneSkill(int skillid) {
        switch (getLinkedAttackSkill(skillid)) {
            case 80001428:
            case 80001430:
            case 80001432:
            case 80001752:
            case 80001753:
            case 80001754:
            case 80001755:
            case 80001757:
            case 80001762: {
                return true;
            }
        }
        return false;
    }

    public static boolean isGeneralSkill(int skillid) {
        if (skillid == 龍魔導士.龍神之怒) {
            return false;
        }
        switch (skillid) {
            case 80011133:
            case 80001242:
            case 80001429:
            case 80001431:
            case 80001761:
            case 80001762: {
                return true;
            }
        }
        return skillid % 10000 == 1095 || skillid % 10000 == 1094;
    }

    public static Map<Integer, Integer> TeachSkillMap = new LinkedHashMap<>();

    static {
        TeachSkillMap.put(英雄.無形的信任_英雄, 英雄.無形的信任_英雄_傳授);
        TeachSkillMap.put(聖騎士.無形的信任_聖騎士, 聖騎士.無形的信任_聖騎士_傳授);
        TeachSkillMap.put(黑騎士.無形的信任_黑騎士, 黑騎士.無形的信任_黑騎士_傳授);
        TeachSkillMap.put(火毒.實戰的知識_火毒大魔導士, 火毒.實戰的知識_火毒大魔導士_傳授);
        TeachSkillMap.put(冰雷.實戰的知識_冰雷大魔導士, 冰雷.實戰的知識_冰雷大魔導士_傳授);
        TeachSkillMap.put(主教.實戰的知識_主教, 主教.實戰的知識_主教_傳授);
        TeachSkillMap.put(箭神.探險家的好奇心_箭神, 箭神.探險家的好奇心_箭神_傳授);
        TeachSkillMap.put(神射手.探險家的好奇心_神射手, 神射手.探險家的好奇心_神射手_傳授);
        TeachSkillMap.put(開拓者.探險家的好奇心_開拓者, 開拓者.探險家的好奇心_開拓者_傳授);
        TeachSkillMap.put(夜使者.小偷的狡詐_夜使者, 夜使者.小偷的狡詐_夜使者_傳授);
        TeachSkillMap.put(暗影神偷.小偷的狡詐_暗影神偷, 暗影神偷.小偷的狡詐_暗影神偷_傳授);
        TeachSkillMap.put(影武者.小偷的狡詐_影武者, 影武者.小偷的狡詐_影武者_傳授);
        TeachSkillMap.put(拳霸.海盜的祝福_拳霸, 拳霸.海盜的祝福_拳霸_傳授);
        TeachSkillMap.put(槍神.海盜的祝福_槍神, 槍神.海盜的祝福_槍神_傳授);
        TeachSkillMap.put(重砲指揮官.海盜的祝福_重砲指揮官, 重砲指揮官.海盜的祝福_重砲指揮官_傳授);
        TeachSkillMap.put(聖魂劍士.西格諾斯祝福_劍士, 聖魂劍士.西格諾斯祝福_劍士_傳授);
        TeachSkillMap.put(烈焰巫師.西格諾斯祝福_法師, 烈焰巫師.西格諾斯祝福_法師_傳授);
        TeachSkillMap.put(破風使者.西格諾斯祝福_弓箭手, 破風使者.西格諾斯祝福_弓箭手_傳授);
        TeachSkillMap.put(暗夜行者.西格諾斯祝福_盜賊, 暗夜行者.西格諾斯祝福_盜賊_傳授);
        TeachSkillMap.put(閃雷悍將.西格諾斯祝福_海盜, 閃雷悍將.西格諾斯祝福_海盜_傳授);
        TeachSkillMap.put(狂狼勇士.連續擊殺優勢, 狂狼勇士.連續擊殺優勢_傳授);
        TeachSkillMap.put(龍魔導士.輪之堅持, 龍魔導士.輪之堅持_傳授);
        TeachSkillMap.put(精靈遊俠.精靈的祝福, 精靈遊俠.精靈的祝福_傳授);
        TeachSkillMap.put(幻影俠盜.致命本能, 幻影俠盜.致命本能_傳授);
        TeachSkillMap.put(夜光.滲透, 夜光.滲透_傳授);
        TeachSkillMap.put(隱月.死裡逃生, 隱月.死裡逃生_傳授);
        TeachSkillMap.put(惡魔殺手.惡魔之怒, 惡魔殺手.後續待發);
        TeachSkillMap.put(惡魔復仇者.狂暴鬥氣, 惡魔復仇者.狂暴鬥氣_傳授);
        TeachSkillMap.put(爆拳槍神.自由精神_爆拳槍神, 爆拳槍神.自由精神_爆拳槍神_傳授);
        TeachSkillMap.put(煉獄巫師.自由精神_煉獄巫師, 煉獄巫師.自由精神_煉獄巫師_傳授);
        TeachSkillMap.put(狂豹獵人.自由精神_狂豹獵人, 狂豹獵人.自由精神_狂豹獵人_傳授);
        TeachSkillMap.put(機甲戰神.自由精神_機甲戰神, 機甲戰神.自由精神_機甲戰神_傳授);
        TeachSkillMap.put(傑諾.合成邏輯, 傑諾.合成邏輯_傳授);
        TeachSkillMap.put(劍豪.疾風傳授, 劍豪.疾風傳授_傳授);
        TeachSkillMap.put(陰陽師.紫扇傳授, 陰陽師.紫扇傳授_傳授);
        TeachSkillMap.put(米哈逸.光之守護, 米哈逸.光之守護_傳授);
        TeachSkillMap.put(凱撒.鋼鐵意志, 凱撒.鋼鐵意志_傳授);
        TeachSkillMap.put(凱殷.事前準備, 凱殷.事前準備_傳授);
        TeachSkillMap.put(卡蒂娜.集中狂攻, 卡蒂娜.集中狂攻_傳授);
        TeachSkillMap.put(天使破壞者.靈魂契約, 天使破壞者.靈魂契約_傳授);
        TeachSkillMap.put(神之子.時之祝福, 神之子.時之祝福_傳授);
        TeachSkillMap.put(凱內西斯.判斷, 凱內西斯.判斷_傳授);
        TeachSkillMap.put(阿戴爾.貴族, 阿戴爾.貴族_傳授);
        TeachSkillMap.put(伊利恩.戰鬥的流動, 伊利恩.戰鬥的流動_傳授);
        TeachSkillMap.put(卡莉.天賦, 卡莉.天賦_傳授);
        TeachSkillMap.put(亞克.無我, 亞克.無我_傳授);
        TeachSkillMap.put(菈菈.大自然夥伴, 菈菈.大自然夥伴_傳授);
        TeachSkillMap.put(虎影.自信心, 虎影.自信心_傳授);
        TeachSkillMap.put(墨玄.氣魄, 墨玄.氣魄_傳授);
        TeachSkillMap.put(琳恩.守護神的保佑, 琳恩.精靈集中_傳授);
    }

    public static boolean isExtraSkill(int skillid) {
        int group = skillid % 10000;
        switch (group) {
            case 8000:
            case 8001:
            case 8002:
            case 8003:
            case 8004:
            case 8005:
            case 8006: {
                return true;
            }
        }
        return false;
    }

    public static int getCooldownLinkSourceId(final int skillId) {
        switch (skillId) {
            case 米哈逸.光輝聖劍_1:
            case 米哈逸.光輝聖劍_2:
            case 米哈逸.光輝聖劍_3:
            case 米哈逸.光輝聖劍_4:
            case 米哈逸.光輝聖劍_5:
                return 米哈逸.光輝聖劍;
            case 暗影神偷.黑影切斷_1:
            case 暗影神偷.黑影切斷_2:
                return 暗影神偷.黑影切斷;
            case 聖騎士.祝福之鎚_強化:
                return 聖騎士.祝福之鎚;
            case 狂狼勇士.揮動瑪哈_1:
                return 狂狼勇士.揮動瑪哈;
            case 劍豪.百人一閃:
                return 劍豪.疾風五月雨刃;
            case 陰陽師.鬼夜叉_大鬼封魂陣_1:
                return 陰陽師.鬼夜叉_大鬼封魂陣;
            case 開拓者.連段襲擊_釋放:
            case 開拓者.連段襲擊_爆破:
            case 開拓者.連段襲擊_轉移:
                return 開拓者.連段襲擊;
            case 開拓者.古代神矢_釋放:
            case 開拓者.古代神矢_爆破:
            case 開拓者.古代神矢_轉移:
                return 開拓者.古代神矢;
            case 開拓者.黑曜石屏障_釋放:
            case 開拓者.黑曜石屏障_爆破:
            case 開拓者.黑曜石屏障_轉移:
                return 開拓者.黑曜石屏障;
            case 開拓者.遺跡解放_釋放:
            case 開拓者.遺跡解放_爆破:
            case 開拓者.遺跡解放_轉移:
                return 開拓者.遺跡解放;
            case 狂豹獵人.狂豹之怒:
                return 狂豹獵人.閃光雨;
            case 幻影俠盜.間隙破壞_1:
                return 幻影俠盜.間隙破壞;
            case 通用V核心.超新星通用.格蘭蒂斯女神的祝福:
                return 通用V核心.格蘭蒂斯通用.格蘭蒂斯女神的祝福;
        }
        return skillId;
    }

    public static byte getLinkSkillslevel(Skill skill, int cid, int defchrlevel) {
        if (skill == null) {
            return 0;
        }
        int chrlevel;
        if (cid > 0 && skill.isLinkSkills()) {
            chrlevel = MapleCharacter.getLevelbyid(cid);
        } else if (skill.isTeachSkills()) {
            chrlevel = defchrlevel;
        } else {
            return 0;
        }
        if (skill.getMaxLevel() == 5) {
            if (chrlevel < 110) {
                return 0;
            }
        } else {
            if (chrlevel < 70) {
                return 0;
            }
        }
        switch (skill.getMaxLevel()) {
            case 1: {
                return 1;
            }
            case 5: {
                if (chrlevel >= 200) {
                    return 5;
                }
                if (chrlevel >= 175) {
                    return 4;
                }
                if (chrlevel >= 150) {
                    return 3;
                }
                if (chrlevel >= 125) {
                    return 2;
                }
                return 1;
            }
            default: {
                if (chrlevel >= 120) {
                    return 2;
                }
                return 1;
            }
        }
    }

    /*
     * 種族特性本能技能
     */
    public static boolean isTeachSkills(int id) {
        return TeachSkillMap.containsKey(id);
    }

    /*
     * 鏈接技能技能
     */
    public static boolean isLinkSkills(int id) {
        for (int skillId : TeachSkillMap.values()) {
            if (skillId == id) {
                return true;
            }
        }
        return false;
    }

    public static int[] getTeamTeachSkills(int skillid) {
        switch (skillid) {
            case 貴族.西格諾斯祝福: {
                return new int[]{聖魂劍士.西格諾斯祝福_劍士_傳授, 烈焰巫師.西格諾斯祝福_法師_傳授, 破風使者.西格諾斯祝福_弓箭手_傳授, 暗夜行者.西格諾斯祝福_盜賊_傳授, 閃雷悍將.西格諾斯祝福_海盜_傳授};
            }
            case 市民.自由精神: {
                return new int[]{煉獄巫師.自由精神_煉獄巫師_傳授, 狂豹獵人.自由精神_狂豹獵人_傳授, 機甲戰神.自由精神_機甲戰神_傳授, 爆拳槍神.自由精神_爆拳槍神_傳授};
            }
            case 劍士.無形的信任: {
                return new int[]{英雄.無形的信任_英雄_傳授, 聖騎士.無形的信任_聖騎士_傳授, 黑騎士.無形的信任_黑騎士_傳授};
            }
            case 法師.實戰的知識: {
                return new int[]{火毒.實戰的知識_火毒大魔導士_傳授, 冰雷.實戰的知識_冰雷大魔導士_傳授, 主教.實戰的知識_主教_傳授};
            }
            case 弓箭手.探險家的好奇心: {
                return new int[]{箭神.探險家的好奇心_箭神_傳授, 神射手.探險家的好奇心_神射手_傳授, 開拓者.探險家的好奇心_開拓者_傳授};
            }
            case 盜賊.小偷的狡詐: {
                return new int[]{夜使者.小偷的狡詐_夜使者_傳授, 暗影神偷.小偷的狡詐_暗影神偷_傳授, 影武者.小偷的狡詐_影武者_傳授};
            }
            case 海盜.海盜的祝福: {
                return new int[]{重砲指揮官.海盜的祝福_重砲指揮官_傳授, 拳霸.海盜的祝福_拳霸_傳授, 槍神.海盜的祝福_槍神_傳授};
            }
        }
        return null;
    }

    public static int getTeamTeachSkillId(int skillid) {
        switch (skillid) {
            case 聖魂劍士.西格諾斯祝福_劍士_傳授:
            case 烈焰巫師.西格諾斯祝福_法師_傳授:
            case 破風使者.西格諾斯祝福_弓箭手_傳授:
            case 暗夜行者.西格諾斯祝福_盜賊_傳授:
            case 閃雷悍將.西格諾斯祝福_海盜_傳授: {
                return 貴族.西格諾斯祝福;
            }
            case 煉獄巫師.自由精神_煉獄巫師_傳授:
            case 狂豹獵人.自由精神_狂豹獵人_傳授:
            case 機甲戰神.自由精神_機甲戰神_傳授:
            case 爆拳槍神.自由精神_爆拳槍神_傳授: {
                return 市民.自由精神;
            }
            case 英雄.無形的信任_英雄_傳授:
            case 聖騎士.無形的信任_聖騎士_傳授:
            case 黑騎士.無形的信任_黑騎士_傳授: {
                return 劍士.無形的信任;
            }
            case 火毒.實戰的知識_火毒大魔導士_傳授:
            case 冰雷.實戰的知識_冰雷大魔導士_傳授:
            case 主教.實戰的知識_主教_傳授: {
                return 法師.實戰的知識;
            }
            case 箭神.探險家的好奇心_箭神_傳授:
            case 神射手.探險家的好奇心_神射手_傳授:
            case 開拓者.探險家的好奇心_開拓者_傳授: {
                return 弓箭手.探險家的好奇心;
            }
            case 夜使者.小偷的狡詐_夜使者_傳授:
            case 暗影神偷.小偷的狡詐_暗影神偷_傳授:
            case 影武者.小偷的狡詐_影武者_傳授: {
                return 盜賊.小偷的狡詐;
            }
            case 重砲指揮官.海盜的祝福_重砲指揮官_傳授:
            case 拳霸.海盜的祝福_拳霸_傳授:
            case 槍神.海盜的祝福_槍神_傳授: {
                return 海盜.海盜的祝福;
            }
            case 劍士.無形的信任:
            case 法師.實戰的知識:
            case 弓箭手.探險家的好奇心:
            case 盜賊.小偷的狡詐:
            case 海盜.海盜的祝福:
            case 貴族.西格諾斯祝福:
            case 市民.自由精神: {
                return 1;
            }
        }
        return 0;
    }

    public static int getTeachSkillId(int skillid) {
        for (Map.Entry<Integer, Integer> entry : TeachSkillMap.entrySet()) {
            if (entry.getValue() == skillid) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static int getLinkSkillId(int skillid) {
        if (TeachSkillMap.containsKey(skillid)) {
            return TeachSkillMap.get(skillid);
        }
        return -1;
    }

    public static int getStolenHyperSkillColltime(int skillId) {
        switch (skillId) {
            case 英雄.劍士意念:
                return 300;
            case 聖騎士.神域護佑:
                return 600;
            case 黑騎士.黑暗飢渴:
                return 180;
            case 火毒.火靈結界:
                return 75;
            case 冰雷.冰雪結界:
                return 90;
            case 主教.復仇天使:
                return 300;
            case 箭神.戰鬥準備:
                return 120;
            case 神射手.專注弱點:
                return 150;
            case 夜使者.出血毒素:
                return 120;
            case 暗影神偷.翻轉硬幣:
                return 45;
            case 拳霸.暴能續發:
                return 75;
            case 槍神.撫慰甘露:
                return 60;
            default:
                return 0;
        }
    }

    // 是否天破技能
    public static boolean isAngelRebornSkill(int skillID) {
        switch (skillID) {
            case 天使破壞者.泡沫之星:
            case 天使破壞者.刺殺爆破:
            case 天使破壞者.靈魂探求者:
            case 天使破壞者.流星:
            case 天使破壞者.原始咆嘯:
            case 天使破壞者.三位一體: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static List<Integer> getUnstableMemorySkillsByJob(short job) {
        final ArrayList<Integer> list = new ArrayList<>();
        list.add(法師.魔靈彈);
        switch (job) {
            case 212: {
                list.add(火毒.魔火焰彈);
                list.add(火毒.毒霧);
                list.add(火毒.精神強化);
                list.add(火毒.末日烈焰);
                list.add(火毒.致命毒霧);
                list.add(火毒.自然力重置);
                list.add(火毒.火焰之襲);
                list.add(火毒.火流星);
                list.add(火毒.炙焰毒火);
                list.add(火毒.魔力無限);
                list.add(火毒.召喚火魔);
                list.add(火毒.楓葉祝福);
                list.add(火毒.楓葉淨化);
                break;
            }
            case 222: {
                list.add(冰雷.冰錐劍);
                list.add(冰雷.電閃雷鳴);
                list.add(冰雷.精神強化);
                list.add(冰雷.極速詠唱);
                list.add(冰雷.冰風暴);
                list.add(冰雷.閃電球);
                list.add(冰雷.閃電連擊);
                list.add(冰雷.暴風雪);
                list.add(冰雷.冰鋒刃);
                list.add(冰雷.魔力無限);
                list.add(冰雷.召喚冰魔);
                list.add(冰雷.楓葉祝福);
                list.add(冰雷.楓葉淨化);
                break;
            }
            case 232: {
                list.add(主教.群體治癒);
                list.add(主教.天使祝福);
                list.add(主教.神聖之箭);
                list.add(主教.聖光);
                list.add(主教.神聖之泉);
                list.add(主教.淨化);
                list.add(主教.神聖祈禱);
                list.add(主教.天使之箭);
                list.add(主教.核爆術);
                list.add(主教.復甦之光);
                list.add(主教.魔力無限);
                list.add(主教.召喚聖龍);
                list.add(主教.進階祝福);
                list.add(主教.楓葉祝福);
                list.add(主教.楓葉淨化);
                break;
            }
        }
        return list;
    }

    public static boolean isMoveImpactStatus(MonsterStatus monsterStatus) {
        switch (monsterStatus) {
            case Speed:
            case Stun:
            case Freeze:
            case Seal:
            case Web:
            case RiseByToss: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    /*
     * MonsterStatus是否為鎖怪狀態
     */
    public static boolean isSmiteStatus(MonsterStatus monsterStatus) {
        switch (monsterStatus) {
            case Smite:
            case Freeze:
            case Web:
            case RiseByToss: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static int getDiceValue(int i, int value, MapleStatEffect effect) {
        int result = 0;
        while (value > 0) {
            int dice = value % 10;
            value /= 10;
            switch (i) {
                case 7: {
                    result += dice == 2 ? effect.getPddR() : 0;
                    break;
                }
                case 0: {
                    result += dice == 3 ? effect.getS() : 0;
                    break;
                }
                case 1: {
                    result += dice == 4 ? effect.getCritical() : 0;
                    break;
                }
                case 11: {
                    result += dice == 5 ? effect.getDamR() : 0;
                    break;
                }
                case 16: {
                    result += dice == 6 ? effect.getExpR() : 0;
                    break;
                }
                case 17: {
                    result += dice == 7 ? effect.getIgnoreMobpdpR() : 0;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isRapidAttackSkill(int skillID) {
        final Skill skill = SkillFactory.getSkill(skillID);
        return skill != null && skill.isRapidAttack();
    }

    public static int getSkillRoot(int id) {
        int root = id / 10000;
        if (root == 8000) {
            root = id / 100;
        }
        return root;
    }

    public static int dY(int n) {
        if (dZ(n) || n % 100 == 0 || n == 501 || n == 3101 || n == 508) {
            return 1;
        }
        if (JobConstants.is龍魔導士(n)) {
            switch (n) {
                case 2200:
                case 2210: {
                    return 1;
                }
                case 2211:
                case 2212:
                case 2213: {
                    return 2;
                }
                case 2214:
                case 2215:
                case 2216: {
                    return 3;
                }
                case 2217:
                case 2218: {
                    return 4;
                }
                default: {
                    return 0;
                }
            }
        } else {
            if (JobConstants.is影武者(n)) {
                n = n % 10 / 2;
            } else {
                n %= 10;
            }
            if (n <= 2) {
                return n + 2;
            }
            return 0;
        }
    }

    public static boolean dZ(final int n) {
        boolean b;
        if (n > 6002) {
            if (n == 8001 || n == 13000) {
                return true;
            }
            b = (n == 14000 || n == 15000 || n == 15001);
        } else {
            if (n >= 6000) {
                return true;
            }
            if (n <= 4002) {
                return n >= 4001 || n <= 3002 && (n >= 3001 || n >= 2001 && n <= 2005) || n - 40000 > 5 && n % 1000 == 0;
            }
            b = (n == 5000);
        }
        return b || (n - 40000 > 5 && (n % 1000 == 0 || n / 100 == 8000));
    }

    public static boolean isPassiveAttackSkill(int skillId) {
        switch (skillId) {
            case 火毒.火靈結界:
            case 冰雷.冰鋒刃:
            case 箭神.箭座:
            case 暗影神偷.暗影霧殺:
            case 影武者.隱藏刀:
            case 拳霸.衝擊波:
            case 拳霸.海龍之魂:
            case 槍神.船員指令:
            case 重砲指揮官.火藥桶破壞:
            case 烈焰巫師.極致熾烈_1:
            case 烈焰巫師.極致熾烈:
            case 破風使者.暴風加護:
            case 暗夜行者.星塵:
            case 暗夜行者.星塵爆炸:
            case 夜光.晨星殞落_爆炸:
            case 夜光.晨星殞落:
            case 惡魔殺手.惡魔追擊:
            case 惡魔殺手.變形:
            case 惡魔復仇者.盾牌追擊_攻擊:
            case 煉獄巫師.黑暗閃電:
            case 爆拳槍神.旋轉加農砲精通:
            case 凱撒.龍劍風:
            case 1141002:
            case 英雄.劍之幻象:
            case 英雄.劍之幻象_1:
            case 英雄.劍之幻象_2:
            case 天使破壞者.靈魂探求者_攻擊:
            case 80002890:
            case 神之子.狂風千刃:
            case 阿戴爾.追蹤:
            case 伊利恩.技藝_暗器:
            case 伊利恩.即刻反應_文明爭戰Ⅱ:
            case 伊利恩.技藝_暗器Ⅱ:
            case 伊利恩.技藝_暗器Ⅱ_1:
            case 伊利恩.榮耀之翼_強化暗器_2:
            case 通用V核心.騎士團通用.西格諾斯槍兵陣:
            case 黑騎士.斷罪之槍:
            case 聖騎士.祝福之鎚:
            case 聖騎士.祝福之鎚_強化:
            case 聖魂劍士.極樂之境:
            case 惡魔復仇者.次元之刃_1:
            case 阿戴爾.無限:
            case 烈焰巫師.炙熱元素火焰:
            case 陰陽師.雪女招喚:
            case 陰陽師.雪女招喚_1:
            case 火毒.劇毒新星:
            case 火毒.劇毒新星_1:
            case 通用V核心.弓箭手通用.追蹤箭頭:
            case 破風使者.狂風呼嘯:
            case 破風使者.狂風呼嘯_1:
            case 箭神.殘影之矢:
            case 箭神.殘影之矢_1:
            case 破風使者.風轉奇想:
            case 暗影神偷.滅殺刃影:
            case 暗影神偷.滅殺刃影_1:
            case 暗影神偷.滅殺刃影_2:
            case 暗影神偷.滅殺刃影_3:
            case 幻影俠盜.命運鬼牌:
            case 幻影俠盜.鬼牌_1:
            case 夜使者.風魔手裏劍:
            case 影武者.炎獄修羅斬:
            case 夜使者.達克魯的秘傳:
            case 暗影神偷.音速狂襲:
            case 閃雷悍將.神雷合一:
            case 重砲指揮官.超級巨型加農砲彈:
            case 拳霸.海龍螺旋:
            case 機甲戰神.微型導彈箱:
            case 機甲戰神.合金盔甲_火力全開:
                return true;

            default:
                return false;

        }
    }

    public static Map<Integer, Integer> hn() {
        final HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        switch (Randomizer.nextInt(8)) {
            case 0: {
                hashMap.put(隱月.巨浪打擊, 0);
                hashMap.put(隱月.巨浪打擊_2, 720);
                break;
            }
            case 1: {
                hashMap.put(隱月.剛刃絞殺_下, 0);
                break;
            }
            case 2: {
                hashMap.put(隱月.銷魂屏障, 0);
                break;
            }
            case 3: {
                hashMap.put(隱月.剛刃絞殺_迴, 0);
                break;
            }
            case 4: {
                hashMap.put(隱月.爆流拳, 0);
                hashMap.put(隱月.爆流拳_1, 360);
                hashMap.put(隱月.爆流拳_2, 720);
                hashMap.put(隱月.爆流拳_3, 1080);
                break;
            }
            case 5: {
                hashMap.put(隱月.鬼斬, 0);
                break;
            }
            case 6: {
                hashMap.put(隱月.死魂烙印, 0);
                break;
            }
            case 7: {
                hashMap.put(隱月.精靈的化身_1, 0);
                break;
            }
        }
        return hashMap;
    }

    public static boolean eD(final int n) {
        switch (n) {
            case 龍魔導士.迅捷_回來吧:
            case 龍魔導士.風之捷:
            case 龍魔導士.風之捷_1:
            case 龍魔導士.風之捷_攻擊:
            case 龍魔導士.潛水_回來吧:
            case 龍魔導士.閃雷之捷:
            case 龍魔導士.閃雷之躍:
            case 龍魔導士.閃雷之捷_攻擊:
            case 龍魔導士.閃雷之躍_攻擊:
            case 龍魔導士.氣息_回來吧:
            case 龍魔導士.大地氣息:
            case 龍魔導士.風之氣息:
            case 龍魔導士.塵土之躍:
            case 龍魔導士.迅捷_回來吧_1:
            case 龍魔導士.大地氣息_攻擊: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static boolean eF(final int n) {
        switch (n) {
            case 狂豹獵人.召喚美洲豹_銀灰:
            case 狂豹獵人.召喚美洲豹_暗黃:
            case 狂豹獵人.召喚美洲豹_血紅:
            case 狂豹獵人.召喚美洲豹_紫光:
            case 狂豹獵人.召喚美洲豹_深藍:
            case 狂豹獵人.召喚美洲豹_傑拉:
            case 狂豹獵人.召喚美洲豹_白雪:
            case 狂豹獵人.召喚美洲豹_歐尼斯:
            case 狂豹獵人.召喚美洲豹_地獄裝甲:
            case 狂豹獵人.爪攻擊:
            case 狂豹獵人.歧路:
            case 狂豹獵人.音暴:
            case 狂豹獵人.美洲豹靈魂:
            case 狂豹獵人.閃光雨:
            case 狂豹獵人.狂豹之怒: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static boolean eH(int ee) {
        switch (ee = getLinkedAttackSkill(ee)) {
            case 狂豹獵人.雙重射擊:
            case 狂豹獵人.三重射擊:
            case 狂豹獵人.瘋狂射擊:
            case 狂豹獵人.狂野機關砲: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static int eM(final int n) {
        switch (n) {
            case 80000086: {
                return 2023189;
            }
            case 86:
            case 91: {
                return 2022746;
            }
            case 88: {
                return 2022747;
            }
            case 80000052: {
                return 2023148;
            }
            case 80000053: {
                return 2023149;
            }
            case 80000054: {
                return 2023150;
            }
            case 80000155: {
                return 2022823;
            }
            default: {
                return -1;
            }
        }
    }

    public static boolean ej(int skillId) {
        int n = skillId / 10000;
        if (skillId / 10000 == 8000) {
            n = skillId / 100;
        }
        return n >= 800000 && n <= 800099 || n == 8001;
    }

    public static boolean i0(final int n) {
        if (n <= 龍魔導士.龍之躍_攻擊) {
            if (n == 龍魔導士.龍之躍_攻擊) {
                return true;
            }
            if (n <= 龍魔導士.龍之捷) {
                return n >= 龍魔導士.風之環 || (n >= 龍魔導士.龍之捷_1 && n <= 龍魔導士.龍之捷_2);
            }
            return n == 龍魔導士.回來吧;
        } else {
            if (n > 龍魔導士.龍之氣息) {
                return n == 龍魔導士.龍之捷_3 || n == 龍魔導士.聖龍突襲;
            }
            return n >= 龍魔導士.地之環 || (n >= 龍魔導士.閃雷之環 && n <= 龍魔導士.龍之躍);
        }
    }

    public static int getSkillByJob(int skillId, int job) {
        return skillId + JobConstants.getBeginner((short) job) * 10000;
    }

    public static boolean isMasterLevelSkill(int skillId) {
        int skillRoot; // edi
        int jobLevel; // ebx
        if (is4thNotNeedMasterLevel(skillId) > 0
                || (skillId / 1000000 == 92 && (skillId % 10000 == 0))
                || isMakingSkillRecipe(skillId)
                || isCommonSkill(skillId)
                || isNoviceSkill(skillId)
                || isFieldAttackSKill(skillId)) {
            return false;
        }
        skillRoot = getSkillRootFromSkill(skillId);
        jobLevel = getJobLevel(skillRoot);
        return (skillRoot > 40005 || skillRoot < 40000)
                && skillId != 42120024
                && (isAddedSpDualAndZeroSkill(skillId) || jobLevel == 4 && !JobConstants.is神之子(skillRoot));
    }

    private static int is4thNotNeedMasterLevel(int skillID) {
        boolean v1; // zf
        if (skillID > 重砲指揮官.楓葉淨化) {
            if (skillID > 狂豹獵人.狂暴天性) {
                if (skillID <= 伊利恩.技藝_子彈Ⅱ) {
                    if (skillID == 伊利恩.技藝_子彈Ⅱ || skillID == 機甲戰神.雙倍幸運骰子 || skillID == 米哈逸.戰鬥大師) {
                        return 1;
                    }
                    v1 = skillID == 80001913;
                    if (v1) {
                        return 1;
                    }
                    return 0;
                }
                if (skillID > 伊利恩.水晶技能_德烏斯_1) {
                    v1 = skillID == 伊利恩.雷普勇士的意志;
                    if (v1) {
                        return 1;
                    }
                    return 0;
                }
                if (skillID != 伊利恩.水晶技能_德烏斯_1 && (skillID < 伊利恩.完成祝福標誌 || skillID > 伊利恩.完成詛咒之印)) {
                    return 0;
                }
            } else if (skillID != 狂豹獵人.狂暴天性) {
                if (skillID > 龍魔導士.楓葉淨化) {
                    if (skillID == 精靈遊俠.進階光速雙擊 || skillID == 精靈遊俠.勇士的意志) {
                        return 1;
                    }
                    v1 = skillID - 精靈遊俠.勇士的意志 == 3;
                    if (v1) {
                        return 1;
                    }
                    return 0;
                }
                if (skillID != 龍魔導士.楓葉淨化) {
                    if (skillID > 狂狼勇士.終極研究II) {
                        v1 = skillID == 狂狼勇士.楓葉淨化;
                    } else {
                        if (skillID >= 狂狼勇士.動力精通II || skillID == 狂狼勇士.快速移動) {
                            return 1;
                        }
                        v1 = skillID - 狂狼勇士.快速移動 == 3;
                    }
                    if (v1) {
                        return 1;
                    }
                    return 0;
                }
            }
            return 1;
        }
        if (skillID == 重砲指揮官.楓葉淨化) {
            return 1;
        }
        if (skillID > 影武者.疾速) {
            if (skillID > 槍神.雙倍幸運骰子) {
                if (skillID == 槍神.海盜砲擊艇 || skillID == 重砲指揮官.雙倍幸運骰子) {
                    return 1;
                }
                v1 = skillID == 重砲指揮官.雙胞胎猴子;
                if (v1) {
                    return 1;
                }
                return 0;
            }
            if (skillID != 槍神.雙倍幸運骰子) {
                if (skillID > 拳霸.雙倍幸運骰子) {
                    v1 = skillID == 槍神.進攻姿態;
                    if (v1) {
                        return 1;
                    }
                    return 0;
                }
                if (skillID < 拳霸.防禦姿態) {
                    v1 = skillID == 影武者.致命的飛毒殺;
                    if (v1) {
                        return 1;
                    }
                    return 0;
                }
            }
            return 1;
        }
        if (skillID == 影武者.疾速) {
            return 1;
        }
        if (skillID > 2321010) {
            if (skillID == 神射手.射擊術 || skillID == 夜使者.鏢術精通) {
                return 1;
            }
            v1 = skillID == 暗影神偷.貪婪;
            if (v1) {
                return 1;
            }
            return 0;
        }
        if (skillID == 2321010) {
            return 1;
        }
        if (skillID > 2121009) {
            v1 = skillID == 2221009;
        } else {
            if (skillID == 2121009 || skillID == 英雄.戰鬥精通) {
                return 1;
            }
            v1 = skillID == 黑騎士.闇靈復仇;
        }
        if (v1) {
            return 1;
        }
        return 0;
    }

    private static boolean isMakingSkillRecipe(int recipeID) {
        int v1; // esi
        boolean result; // eax
        result = false;
        if (recipeID / 1000000 == 92 || recipeID % 10000 > 0) {
            v1 = 10000 * (recipeID / 10000);
            if (v1 / 1000000 == 92 && ((v1 % 10000) == 0)) {
                result = true;
            }
        }
        return result;
    }

    private static boolean isCommonSkill(int nSkillID) {
        int branch; // eax
        branch = nSkillID / 10000;
        if (nSkillID / 10000 == 8000) {
            branch = nSkillID / 100;
        }
        return branch >= 800000 && branch <= 800099;
    }

    private static boolean isNoviceSkill(int skillID) {
        int branch; // ecx
        boolean result; // eax
        branch = skillID / 10000;
        if (skillID / 10000 == 8000) {
            branch = skillID / 100;
        }
        return JobConstants.is零轉職業(branch);
    }

    private static boolean isFieldAttackSKill(int skillID) {
        int v1;
        if (skillID == 0 || (skillID & 0x80000000) != 0) {
            return false;
        }
        v1 = skillID / 10000;
        if (skillID / 10000 == 8000) {
            v1 = skillID / 100;
        }
        return v1 == 9500;
    }

    private static boolean isAddedSpDualAndZeroSkill(int skillId) {
        if (skillId == 神之子.進階碎地猛擊 || skillId == 神之子.進階暴風裂擊) {
            return true;
        }
        if (skillId == 神之子.進階旋風落葉斬 || skillId == 神之子.進階迴旋之刃 || skillId == 神之子.進階旋風) {
            return true;
        }
        if (skillId == 神之子.進階旋風急轉彎) {
            return true;
        }
        if (skillId == 神之子.進階武器投擲) {
            return true;
        }
        if (skillId == 影武者.荊棘特效 || skillId == 影武者.短刀護佑) {
            return true;
        }
        if (skillId == 影武者.替身術 || skillId == 影武者.狂刃風暴 || skillId == 影武者.翔空落葉斬) {
            return true;
        }
        return skillId == 影武者.暗影迴避;
    }

    private static int getJobLevel(int job) {
        int result; // eax
        int dual_job_level; // esi

        if (JobConstants.is零轉職業(job) || (job % 100) == 0 || job == 501 || job == 3101 || job == 301 || job == 508) {
            return 1;
        }
        if (JobConstants.is龍魔導士(job)) {
            return JobConstants.get龍魔轉數(job);
        }
        if (JobConstants.is影武者(job)) {
            result = 0;
            dual_job_level = (job - 430) / 2;
            if (dual_job_level <= 2) {
                result = dual_job_level + 2;
            }
        } else {
            result = 0;
            if ((job % 10) <= 2) {
                result = job % 10 + 2;
            }
        }
        return result;
    }

    private static int getSkillRootFromSkill(int nSkillID) {
        int result; // eax
        result = nSkillID / 10000;
        if (nSkillID / 10000 == 8000) {
            result = (nSkillID / 100);
        }
        return result;
    }

    public static boolean hD(int n) {
        boolean b = (n / 1000000 != 92 || n % 10000 != 0) && (n = 10000 * (n / 10000)) / 1000000 == 92 && n % 10000 == 0;
        return b;
    }

    public static int hA(final int n) {
        int n2 = n / 10000;
        if (n / 10000 == 8000) {
            n2 = n / 100;
        }
        return n2;
    }

    public static boolean sub_140A60E40(int id) {
        boolean result;
        if (sub_140A36D60(id) && sub_140A37730(id)) {
            result = id == 152141001;
        } else {
            result = false;
        }
        return result;
    }

    public static boolean sub_140A36D60(int id) {
        boolean v2 = false;
        if (id > 61121217) {
            int v3 = id - 400011058;
            if (v3 == 0)
                return true;
            v2 = v3 == 1;
        } else {
            if (id == 61121217
                    || id == 61101002
                    || id == 61110211
                    || id == 61120007
                    || id == 36001005
                    || id == 36100010
                    || id == 36110012) {
                return true;
            }
            v2 = id == 36120015;
        }
        if (v2
                || id == 4100012
                || id == 4120019
                || id == 35101002
                || id == 35110017
                || sub_140A60DA0(id)) {
            return true;
        }
        boolean v4;
        if (id > 155141002) {
            if (id == 155141009 || id == 155141013)
                return true;
            v4 = id == 155141018;
        } else {
            if (id == 155141002 || id == 155001000 || id == 155101002 || id == 155111003)
                return true;
            v4 = id == 155121003;
        }
        boolean v5 = false;
        if (!v4 && !sub_140AA5580(id) && !sub_140A3EC80(id)
                && id != 3011004
                && id != 3300002
                && id != 3321003) {
            if (id > 36110004) {
                if (id > 175121017) {
                    if (id > 400041010) {
                        if (id > 400051017) {
                            int v34 = id - 400051087;
                            if (v34 == 0)
                                return true;
                            int v35 = v34 - 100009928;
                            if (v35 == 0)
                                return true;
                            int v36 = v35 - 2;
                            if (v36 == 0)
                                return true;
                            v5 = v36 == 17;
                        } else {
                            if (id == 400051017)
                                return true;
                            int v31 = id - 400041023;
                            if (v31 == 0)
                                return true;
                            int v32 = v31 - 15;
                            if (v32 == 0)
                                return true;
                            int v33 = v32 - 11;
                            if (v33 == 0)
                                return true;
                            v5 = v33 == 19;
                        }
                    } else {
                        if (id == 400041010)
                            return true;
                        if (id > 400031021) {
                            int v28 = id - 400031022;
                            if (v28 == 0)
                                return true;
                            int v29 = v28 - 7;
                            if (v29 == 0)
                                return true;
                            int v30 = v29 - 2;
                            if (v30 == 0)
                                return true;
                            v5 = v30 == 23;
                        } else {
                            if (id == 400031021)
                                return true;
                            int v24 = id - 400011131;
                            if (v24 == 0)
                                return true;
                            int v25 = v24 - 9870;
                            if (v25 == 0)
                                return true;
                            int v26 = v25 - 44;
                            if (v26 == 0)
                                return true;
                            int v27 = v26 - 9955;
                            if (v27 == 0)
                                return true;
                            v5 = v27 == 20;
                        }
                    }
                } else {
                    if (id == 175121017)
                        return true;
                    if (id > 142110011) {
                        if (id > 152141002) {
                            if (id == 155100009 || id == 164101004 || id == 164120007)
                                return true;
                            v5 = id == 175121007;
                        } else {
                            if (id == 152141002)
                                return true;
                            int v20 = id - 152001001;
                            if (v20 == 0)
                                return true;
                            int v21 = v20 - 119000;
                            if (v21 == 0)
                                return true;
                            int v22 = v21 - 1;
                            if (v22 == 0)
                                return true;
                            int v23 = v22 - 20998;
                            if (v23 == 0)
                                return true;
                            v5 = v23 == 1;
                        }
                    } else {
                        if (id == 142110011
                                || id == 80001890
                                || id == 42110002
                                || id == 65111007
                                || id == 65120011
                                || id == 65141502
                                || id == 80001588
                                || id == 80002811
                                || id == 112110005
                                || id == 131003016) {
                            return true;
                        }
                        v5 = id == 135002015;
                    }
                }
            } else {
                if (id == 36110004)
                    return true;
                if (id > 13100027) {
                    if (id > 14120018) {
                        if (id == 25100010
                                || id == 14120020
                                || id == 24100003
                                || id == 24120002
                                || id == 24121011
                                || id == 25120115
                                || id == 25141505
                                || id == 31221014) {
                            return true;
                        }
                        v5 = id == 31241001;
                    } else {
                        if (id == 14120018)
                            return true;
                        if (id > 13121017) {
                            int v17 = id - 14000028;
                            if (v17 == 0)
                                return true;
                            int v18 = v17 - 1;
                            if (v18 == 0)
                                return true;
                            int v19 = v18 - 110005;
                            if (v19 == 0)
                                return true;
                            v5 = v19 == 1;
                        } else {
                            if (id == 13121017)
                                return true;
                            int v13 = id - 13101022;
                            if (v13 == 0)
                                return true;
                            int v14 = v13 - 9000;
                            if (v14 == 0)
                                return true;
                            int v15 = v14 - 5;
                            if (v15 == 0)
                                return true;
                            int v16 = v15 - 9976;
                            if (v16 == 0)
                                return true;
                            v5 = v16 == 7;
                        }
                    }
                } else {
                    if (id == 13100027)
                        return true;
                    if (id > 12110030) {
                        if (id > 12121059) {
                            int v10 = id - 12141001;
                            if (v10 == 0)
                                return true;
                            int v11 = v10 - 1;
                            if (v11 == 0)
                                return true;
                            int v12 = v11 - 2;
                            if (v12 == 0)
                                return true;
                            v5 = v12 == 1;
                        } else {
                            if (id == 12121059)
                                return true;
                            int v6 = id - 12120010;
                            if (v6 == 0)
                                return true;
                            int v7 = v6 - 7;
                            if (v7 == 0)
                                return true;
                            int v8 = v7 - 2;
                            if (v8 == 0)
                                return true;
                            int v9 = v8 - 1;
                            if (v9 == 0)
                                return true;
                            v5 = v9 == 1037;
                        }
                    } else {
                        if (id == 12110030
                                || id == 4210014
                                || id == 3100010
                                || id == 3120017
                                || id == 3300005
                                || id == 3301009
                                || id == 3321037
                                || id == 4220021
                                || id == 12000026
                                || id == 12100028) {
                            return true;
                        }
                        v5 = id == 12110028;
                    }
                }
            }
            return v5;
        }
        return true;
    }

    public static boolean sub_140A60DA0(int id) {
        return (id - 152141004) <= 2 || id == 152110004 || id == 152120016 || id == 155121003 || id == 155141018;
    }

    public static boolean sub_140AA5580(int a1) {
        return a1 == 22141017 || a1 == 22170070 || a1 == 63111010 || a1 == 155111207;
    }

    public static boolean sub_140A3EC80(int a1) {
        return false;
    }

    public static boolean sub_140A60DE0(int id) {
        boolean result = false;
        if (((id - 152141004) <= 2 || id == 152110004 || id == 152120016 || id == 155121003 || id == 155141018) && sub_140A37730(id)) {
            result = id == 152141006;
        }
        return result;
    }

    public static boolean sub_140A37730(int id) {
        boolean v1;
        int v2;

        if (id > 152141005) {
            if (id == 162121010 || id == 152141006 || id == 162101011 || id == 162111005 || id == 162121019)
                return true;
            v1 = id == 400021122;
        } else {
            if (id == 152141005)
                return true;
            if (id > 31241001) {
                v2 = id - 152141001;
                if (v2 == 0)
                    return true;
                v1 = v2 == 1;
            } else {
                if (id == 31241001 || id == 2121052 || id == 3341002)
                    return true;
                v1 = id == 11121018;
            }
        }
        return v1;
    }


    public static boolean isKeyDownSkill(int skillID) {
        switch (skillID) {
            case 0:
            case 神之子.進階旋風_吸收:
            case 槍神.死亡板機:
            case 槍神.槍彈盛宴:
            case 傑諾.滅世雷射光:
            case 影武者.修羅:
                return false;
        }
        Skill skill = SkillFactory.getSkill(skillID);
        return skill != null && skill.isChargeSkill();
/*        boolean v1;

        if ( skillID > 64001000 )
        {
            if ( skillID > 80011387 )
            {
                if ( skillID > 131001020 )
                {
                    if ( skillID > 400021061 )
                    {
                        if ( skillID == 400031046 || skillID == 400041006 || skillID == 400041009 )
                            return true;
                        v1 = skillID == 400051024;
                    }
                    else
                    {
                        if ( skillID == 400021061
                                || skillID == 131001021
                                || skillID == 142111010
                                || skillID == 400011028
                                || skillID == 400011072 )
                        {
                            return true;
                        }
                        v1 = skillID == 400011091;
                    }
                }
                else
                {
                    if ( skillID == 131001020 )
                        return true;
                    if ( skillID > 101110102 )
                    {
                        if ( skillID == 131001004 )
                            return true;
                        v1 = skillID == 131001008;
                    }
                    else
                    {
                        if ( skillID == 101110102
                                || skillID == 80012134
                                || skillID == 80012142
                                || skillID == 80012143
                                || skillID == 95001001 )
                        {
                            return true;
                        }
                        v1 = skillID == 101110101;
                    }
                }
            }
            else
            {
                if ( skillID == 80011387 )
                    return true;
                if ( skillID > 80003077 )
                {
                    if ( skillID > 80011362 )
                    {
                        if ( skillID == 80011366 || skillID == 80011371 || skillID == 80011381 )
                            return true;
                        v1 = skillID == 80011382;
                    }
                    else
                    {
                        if ( skillID == 80011362
                                || skillID == 80003087
                                || skillID == 80003302
                                || skillID == 80003370
                                || skillID == 80011040 )
                        {
                            return true;
                        }
                        v1 = skillID == 80011051;
                    }
                }
                else
                {
                    if ( skillID == 80003077 )
                        return true;
                    if ( skillID > 80001887 )
                    {
                        if ( skillID == 80002685 || skillID == 80002780 || skillID == 80002785 || skillID == 80003075 )
                            return true;
                        v1 = skillID == 80003076;
                    }
                    else
                    {
                        if ( skillID == 80001887
                                || skillID == 64001007
                                || skillID == 64001008
                                || skillID == 64121002
                                || skillID == 80001363 )
                        {
                            return true;
                        }
                        v1 = skillID == 80001836;
                    }
                }
            }
        }
        else
        {
            if ( skillID == 64001000 )
                return true;
            if ( skillID > 25111005 )
            {
                if ( skillID > 33141000 )
                {
                    if ( skillID > 36121000 )
                    {
                        if ( skillID == 37121052 || skillID == 42121000 || skillID == 42121100 )
                            return true;
                        v1 = skillID == 60011216;
                    }
                    else
                    {
                        if ( skillID == 36121000
                                || skillID == 33141001
                                || skillID == 33141002
                                || skillID == 35101002
                                || skillID == 35110017 )
                        {
                            return true;
                        }
                        v1 = skillID == 36101001;
                    }
                }
                else
                {
                    if ( skillID == 33141000 )
                        return true;
                    if ( skillID > 31101000 )
                    {
                        if ( skillID == 31111005 || skillID == 31211001 || skillID == 33121009 || skillID == 33121114 )
                            return true;
                        v1 = skillID == 33121214;
                    }
                    else
                    {
                        if ( skillID == 31101000
                                || skillID == 25121030
                                || skillID == 27101202
                                || skillID == 27111100
                                || skillID == 30021238 )
                        {
                            return true;
                        }
                        v1 = skillID == 31001000;
                    }
                }
            }
            else
            {
                if ( skillID == 25111005 )
                    return true;
                if ( skillID > 14111006 )
                {
                    if ( skillID > 23121000 )
                    {
                        if ( skillID == 23141000 || skillID == 24121000 || skillID == 24121005 )
                            return true;
                        v1 = skillID == 24141000;
                    }
                    else
                    {
                        if ( skillID == 23121000
                                || skillID == 14121004
                                || skillID == 20041226
                                || skillID == 21120018
                                || skillID == 21121029 )
                        {
                            return true;
                        }
                        v1 = skillID == 22171083;
                    }
                }
                else
                {
                    if ( skillID == 14111006 )
                        return true;
                    if ( skillID > 3141000 )
                    {
                        if ( skillID == 3141001 || skillID == 3201012 || skillID == 4341002 || skillID == 13121001 )
                            return true;
                        v1 = skillID == 13141000;
                    }
                    else
                    {
                        if ( skillID == 3141000
                                || skillID == 2221011
                                || skillID == 2221052
                                || skillID == 3101008
                                || skillID == 3111013 )
                        {
                            return true;
                        }
                        v1 = skillID == 3121020;
                    }
                }
            }
        }
        if ( !v1
                && skillID != 5221004
                && skillID != 5241000
                && skillID != 5241001
                && skillID != 80001389
                && skillID != 80001390
                && skillID != 80001391
                && skillID != 80001392
                && skillID != 80001587
                && skillID != 80001629
                && skillID != 80002458
                // TODO: 不知道是甚麼??
                // && !sub_140AC90F0(skillID, 11)
                 )
        {
            return false;
        }
        return true;*/
    }

    public static boolean isEvanForceSkill(int skillID) { // is_evan_force_skill
        switch (skillID) {
            case 龍魔導士.回來吧:
            case 龍魔導士.風之環:
            case 龍魔導士.龍之捷:
            case 龍魔導士.龍之捷_1:
            case 龍魔導士.龍之捷_2:
            case 龍魔導士.龍之捷_3:
            case 龍魔導士.閃雷之環:
            case 龍魔導士.龍之躍:
            case 龍魔導士.龍之躍_攻擊:
            case 龍魔導士.地之環:
            case 龍魔導士.龍之氣息:
            case 龍魔導士.元素滅殺破:
            case 龍魔導士.聖龍突襲:
                return true;
        }
        return false;
    }

    public static boolean isSuperNovaSkill(int skillID) {
        return skillID == 暗影神偷.暗影霧殺 || skillID == 天使破壞者.超級超新星;
    }

    public static boolean isRushBombSkill(int skillID) {
        switch (skillID) {
            case 龍魔導士.閃雷之躍:
            case 凱撒.展翅飛翔:
            case 80011564:
            case 開拓者.渡鴉風暴:
            case 重砲指揮官.火藥桶破壞_爆炸:
            case 暗夜行者.星塵:
            case 暗夜行者.星塵爆炸:
            case 夜光.晨星殞落_爆炸:
            case 卡蒂娜.召喚_炸裂迴旋_1:
            case 陰陽師.引渡亡靈:
            case 凱撒.龍劍風:
            case 陰陽師.亡靈召喚:
            case 龍魔導士.閃雷之躍_攻擊:
            case 惡魔復仇者.蝙蝠群:
            case 重砲指揮官.火藥桶破壞:
            case 烈焰巫師.極致熾烈:
            case 暗夜行者.暗影蝙蝠II:
            case 暗夜行者.暗影蝙蝠III:
            case 80011380:
            case 80011386:
            case 80002300:
            case 凱撒.展翅飛翔_變身:
            case 卡蒂娜.召喚_炸裂迴旋:
            case 80002247:
            case 通用V核心.騎士團通用.西格諾斯槍兵陣:
            case 破風使者.狂風呼嘯:
            case 破風使者.狂風呼嘯_1:
            case 神之子.進階暴風裂擊_漩渦:
            case 神之子.狂風千刃_漩渦:
            case 神之子.暴風裂擊_漩渦:
            case 菈菈.蜿蜒的山脊_1:
                return true;

        }
        return false;
        /*boolean v1 = false;
        if ( skillID > 80002247 )
        {
            if ( skillID > 101141010 )
            {
                if ( skillID > 400031004 )
                {
                    int v13 = skillID - 400031036;
                    if (v13 == 0)
                        return true;
                    int v14 = v13 - 31;
                    if (v14 == 0)
                        return true;
                    v1 = v14 == 1;
                }
                else
                {
                    if ((skillID - 400031003) <= 1 || skillID == 400001018 )
                    return true;
                    v1 = skillID == 400021131;
                }
            }
            else
            {
                if ( skillID == 101141010 )
                    return true;
                if ( skillID > 101120200 )
                {
                    int v11 = skillID - 101120203;
                    if (v11 == 0)
                        return true;
                    int v12 = v11 - 2;
                    if (v12 == 0)
                        return true;
                    v1 = v12 == 20802;
                }
                else
                {
                    if ( skillID == 101120200 )
                        return true;
                    int v8 = skillID - 80002300;
                    if (v8 == 0)
                        return true;
                    int v9 = v8 - 9080;
                    if (v9 == 0)
                        return true;
                    int v10 = v9 - 6;
                    if (v10 == 0)
                        return true;
                    v1 = v10 == 178;
                }
            }
        }
        else
        {
            if ( skillID == 80002247 )
                return true;
            if ( skillID > 31201001 )
            {
                if ( skillID > 61111100 )
                {
                    int v6 = skillID - 61111113;
                    if (v6 == 0)
                        return true;
                    int v7 = v6 - 105;
                    if (v7 == 0)
                        return true;
                    v1 = v7 == 2989784;
                }
                else
                {
                    if ( skillID == 61111100 )
                        return true;
                    int v4 = skillID - 40021186;
                    if (v4 == 0)
                        return true;
                    int v5 = v4 - 2098814;
                    if (v5 == 0)
                        return true;
                    v1 = v5 == 3;
                }
            }
            else
            {
                if ( skillID == 31201001 )
                    return true;
                if ( skillID > 14101028 )
                {
                    int v2 = skillID - 15101028;
                    if (v2 == 0)
                        return true;
                    int v3 = v2 - 7038987;
                    if (v3 == 0)
                        return true;
                    v1 = v3 == 9;
                }
                else
                {
                    if ( skillID == 14101028 || skillID == 2221012 || skillID == 5301001 || skillID == 11101029 )
                        return true;
                    v1 = skillID == 12121001;
                }
            }
        }
        if ( !v1 )
            return false;
        return true;
*/
    }

    public static boolean isZeroSkill(int skillID) {
        int prefix; // edx

        prefix = skillID / 10000;
        if (skillID / 10000 == 8000 || skillID / 10000 == 8001)
            prefix = skillID / 100;
        return prefix == 10000 || prefix == 10100 || prefix == 10110 || prefix == 10111 || prefix == 10112;
    }

    public static boolean isUsercloneSummonedAbleSkill(int skillID) {
        switch (skillID) {
            case 聖魂劍士.天體觀測:
            case 聖魂劍士.宇宙物質:
            case 聖魂劍士.皇家衝擊:
            case 聖魂劍士.天體觀測II:
            case 聖魂劍士.天體觀測III:
            case 聖魂劍士.雙重精通_沉月:
            case 聖魂劍士.宇宙轟炸:
            case 聖魂劍士.雙重精通_旭日:
            case 聖魂劍士.光輝衝刺:
            case 聖魂劍士.月光之舞_空中old:
            case 聖魂劍士.雙重狂斬:
            case 聖魂劍士.熾烈突擊:
            case 暗夜行者.雙飛斬:
            case 暗夜行者.三連投擲:
            case 暗夜行者.四連投擲:
            case 暗夜行者.五倍緩慢:
            case 暗夜行者.星塵:
            case 暗夜行者.星塵爆炸:
            case 暗夜行者.五連投擲:
            case 暗夜行者.四倍緩慢:
            case 暗夜行者.五連投擲_爆擊機率:
            case 暗夜行者.暗影投擲:
            case 暗夜行者.暗影投擲_1:
            case 精靈遊俠.急速雙擊:
            case 精靈遊俠.最終一擊:
            case 精靈遊俠.精準光速神弩:
            case 精靈遊俠.昇龍刺擊:
            case 精靈遊俠.旋風月光翻轉_2轉:
            case 精靈遊俠.騰空踢擊:
            case 精靈遊俠.光速雙擊:
            case 精靈遊俠.落葉旋風射擊:
            case 精靈遊俠.獨角獸射擊:
            case 精靈遊俠.旋風突進:
            case 精靈遊俠.伊修塔爾之環:
            case 精靈遊俠.傳說之槍:
            case 精靈遊俠.閃電之鋒:
            case 精靈遊俠.進階光速雙擊:
            case 精靈遊俠.憤怒天使:
            case 精靈遊俠.伊里加爾的氣息:
            case 精靈遊俠.旋風月光翻轉:
            case 皮卡啾.皮卡啾攻擊:
            case 皮卡啾.皮卡啾攻擊_1:
            case 皮卡啾.皮卡啾攻擊_2:
            case 皮卡啾.皮卡啾攻擊_3:
            case 皮卡啾.咕嚕咕嚕:
            case 皮卡啾.雨傘:
            case 皮卡啾.天空豆豆:
            case 皮卡啾.超烈焰溜溜球:
            case 皮卡啾.超烈焰溜溜球_1:
            case 皮卡啾.粉紅天怒:
            case 皮卡啾.音波攻擊:
            case 皮卡啾.皮卡啾攻擊_4:
            case 皮卡啾.皮卡啾攻擊_5:
            case 皮卡啾.皮卡啾攻擊_6:
            case 皮卡啾.咕嚕咕嚕_1:
            case 皮卡啾.天空豆豆空中:
            case 皮卡啾.電吉他:
            case 皮卡啾.天空豆豆地上:
            case 皮卡啾.哨子:
            case 皮卡啾.紅喇叭:
            case 皮卡啾.超烈焰溜溜球_2:
            case 131001201:
            case 131001202:
            case 131001203:
                return true;
        }

        boolean v3;
        boolean v8;
        boolean v22;

        if (skillID <= 101110200) {
            if (skillID == 101110200)
                return true;
            if (skillID <= 23111002) {
                if (skillID == 23111002)
                    return true;
                if (skillID <= 14141000) {
                    if (skillID == 14141000)
                        return true;
                    if (skillID <= 14101029) {
                        if (skillID == 14101029)
                            return true;
                        if (skillID > 14101020) {
                            int v4 = skillID - 14101021;
                            if (v4 == 0)
                                return true;
                            v3 = v4 == 7;
                        } else {
                            if (skillID == 14101020)
                                return true;
                            int v1 = skillID - 11111130;
                            if (v1 == 0)
                                return true;
                            int v2 = v1 - 100;
                            if (v2 == 0)
                                return true;
                            v3 = v2 == 2889790;
                        }
                        return v3;
                    }
                    int v5 = skillID - 14111020;
                    if (v5 == 0)
                        return true;
                    int v6 = v5 - 1;
                    if (v6 == 0)
                        return true;
                    int v7 = v6 - 9024;
                    if (v7 == 0)
                        return true;
                    int v9 = v7 - 956;
                    v8 = v9 == 0;
                    if (v8)
                        return true;
                    v3 = v9 == 1;
                    return v3;
                }
                if (skillID <= 23101000) {
                    if (skillID == 23101000)
                        return true;
                    int v10 = skillID - 14141001;
                    if (v10 == 0)
                        return true;
                    int v11 = v10 - 1;
                    if (v11 == 0)
                        return true;
                    int v12 = v11 - 1;
                    if (v12 == 0)
                        return true;
                    int v13 = v12 - 8859997;
                    if (v13 == 0)
                        return true;
                    v3 = v13 == 99004;
                    return v3;
                }
                int v14 = skillID - 23101001;
                if (v14 == 0)
                    return true;
                int v15 = v14 - 6;
                if (v15 == 0)
                    return true;
                int v16 = v15 - 8999;
                if (v16 == 0)
                    return true;
                int v9 = v16 - 994;
                v8 = v9 == 0;
                if (v8)
                    return true;
                v3 = v9 == 1;
                return v3;
            }
            if (skillID <= 101000201) {
                if (skillID == 101000201)
                    return true;
                if (skillID <= 23121011) {
                    if (skillID == 23121011)
                        return true;
                    int v17 = skillID - 23111003;
                    if (v17 == 0)
                        return true;
                    int v18 = v17 - 9010;
                    if (v18 == 0)
                        return true;
                    int v19 = v18 - 987;
                    if (v19 == 0)
                        return true;
                    int v9 = v19 - 2;
                    v8 = v9 == 0;
                    if (v8)
                        return true;
                    v3 = v9 == 1;
                    return v3;
                }
                int v20 = skillID - 23121052;
                if (v20 == 0)
                    return true;
                int v21 = v20 - 19948;
                if (v21 == 0)
                    return true;
                int v23 = v21 - 77859100;
                v22 = v23 == 0;
                if (v22)
                    return true;
                int v24 = v23 - 1;
                if (v24 == 0)
                    return true;
                v3 = v24 == 99;
                return v3;
            }
            if (skillID <= 101100201) {
                if (skillID == 101100201)
                    return true;
                int v25 = skillID - 101001100;
                if (v25 == 0)
                    return true;
                int v26 = v25 - 100;
                if (v26 == 0)
                    return true;
                int v23 = v26 - 98900;
                v22 = v23 == 0;
                if (v22)
                    return true;
                int v24 = v23 - 1;
                if (v24 == 0)
                    return true;
                v3 = v24 == 99;
                return v3;
            }
            int v27 = skillID - 101101100;
            if (v27 == 0)
                return true;
            int v28 = v27 - 100;
            if (v28 == 0)
                return true;
            int v29 = v28 - 8901;
            if (v29 == 0)
                return true;
            int v30 = v29 - 1;
            if (v30 == 0)
                return true;
            v3 = v30 == 2;
            return v3;
        }
        if (skillID > 131001000) {
            if (skillID <= 131001313) {
                if (skillID != 131001313) {
                    switch (skillID) {
                        case 131001001:
                        case 131001002:
                        case 131001003:
                        case 131001004:
                        case 131001005:
                        case 131001008:
                        case 131001010:
                        case 131001011:
                        case 131001012:
                        case 131001013:
                        case 131001101:
                        case 131001102:
                        case 131001103:
                        case 131001104:
                        case 131001108:
                        case 131001113:
                        case 131001201:
                        case 131001202:
                        case 131001203:
                        case 131001208:
                        case 131001213:
                            return true;
                        default:
                            return false;
                    }
                }
                return true;
            }
            int v39 = skillID - 131002010;
            if (v39 == 0)
                return true;
            int v40 = v39 - 269029014;
            if (v40 == 0)
                return true;
            int v9 = v40 - 10035;
            v8 = v9 == 0;
            if (v8)
                return true;
            v3 = v9 == 1;
            return v3;
        }
        if (skillID == 131001000)
            return true;
        if (skillID <= 101120200) {
            if (skillID == 101120200)
                return true;
            if (skillID > 101111200) {
                int v33 = skillID - 101120100;
                if (v33 == 0)
                    return true;
                int v34 = v33 - 2;
                if (v34 == 0)
                    return true;
                v3 = v34 == 2;
            } else {
                if (skillID == 101111200)
                    return true;
                int v31 = skillID - 101110202;
                if (v31 == 0)
                    return true;
                int v32 = v31 - 1;
                if (v32 == 0)
                    return true;
                v3 = v32 == 897;
            }
            return v3;
        }
        if (skillID > 101141000) {
            switch (skillID) {
                case 101141001:
                case 101141003:
                case 101141006:
                case 101141007:
                case 101141008:
                case 101141009:
                    return true;
                default:
                    return false;
            }
        }
        if (skillID != 101141000) {
            int v35 = skillID - 101120201;
            if (v35 > 0) {
                int v36 = v35 - 1;
                if (v36 > 0) {
                    int v37 = v36 - 2;
                    if (v37 > 0) {
                        int v38 = v37 - 896;
                        if (v38 > 0) {
                            v3 = v38 == 100;
                            return v3;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isScreenCenterAttackSkill(int skillID) {
        switch (skillID) {
            case 80001431:
            case 80011562:
            case 神之子.暗影之雨:
            case 狂狼勇士.瑪哈的領域:
            case 破風使者.季風:
            case 暗夜行者.道米尼奧:
            case 閃雷悍將.海神降臨:
            case 幻影俠盜.玫瑰四重曲:
            case 400011124:
            case 400011125:
            case 400011126:
            case 400011127:
                return true;
        }
        return false;
    }

    public static boolean isAranFallingStopSkill(int skillID) {
        switch (skillID) {
            case 狂狼勇士.比耀德:
            case 狂狼勇士.終極之矛:
            case 狂狼勇士.比耀德_1:
            case 狂狼勇士.終極之矛_1:
            case 狂狼勇士.比耀德_2擊:
            case 狂狼勇士.終極之矛_2:
            case 狂狼勇士.比耀德_3擊:
            case 狂狼勇士.空中震撼:
            case 狂狼勇士.粉碎震撼:
            case 狂狼勇士.粉碎震撼_1:
            case 狂狼勇士.粉碎震撼_2:
            case 狂狼勇士.空中震撼_1:
            case 狂狼勇士.空中震撼_2:
            case 80001925:
            case 80001926:
            case 80001927:
            case 80001936:
            case 80001937:
            case 80001938:
                return true;
            default:
                return false;
        }
    }

    public static int getHyperAPByLevel(int level) {
        return level >= 140 ? level / 10 - 11 : 0;
    }

    public static int getHyperStatAPNeedByLevel(int level) {
        switch (level) {
            case 11:
                return 50;
            case 12:
                return 65;
            case 13:
                return 80;
            case 14:
                return 95;
            case 15:
                return 110;
            default:
                return level < 5 ? (int) Math.pow(2.0, level - 1) : (level - 3) * 5;
        }
    }

    public static int getHyperAP(MapleCharacter chr) {
        int ap = 0;
        if (chr.getLevel() >= 140) {
            for (int i = 140; i <= chr.getLevel(); i++) {
                ap += getHyperAPByLevel(i);
            }
        }
        for (Map.Entry<Integer, SkillEntry> entry : chr.getSkills().entrySet()) {
            Skill skill;
            if (entry.getValue().skillevel > 0 && (skill = SkillFactory.getSkill(entry.getKey())) != null && skill.isHyperStat()) {
                for (int i = 1; i <= entry.getValue().skillevel; i++) {
                    ap -= getHyperStatAPNeedByLevel(i);
                }
            }
        }
        return ap;
    }

    public static boolean isKeydownSkillRectMoveXY(int skillID) {
        return skillID == 破風使者.寒冰亂舞;
    }

    public static boolean isFieldAttackObjSkill(int skillId) {
        if (skillId <= 0) {
            return false;
        }
        int prefix = skillId / 10000;
        if (skillId / 10000 == 8000) {
            prefix = skillId / 100;
        }
        return prefix == 9500;
    }

    public static int getRandomInnerSkill() {
        return innerSkills[Randomizer.nextInt(innerSkills.length)];
    }

    /**
     * 放開技能鍵後開始CD
     *
     * @param skillId
     * @return
     */
    public static boolean isKeydownSkillCancelGiveCD(int skillId) {
        switch (skillId) {
            case 陰陽師.破邪連擊符:
            case 陰陽師.妖繪釋放:
            case 幻影俠盜.幻影斗蓬:
                return true;
            default:
                return false;
        }
    }

    /**
     * 開關技能
     *
     * @param skillId
     * @return
     */
    public static boolean isOnOffSkill(int skillId) {
        switch (skillId) {
            case 夜光.強化閃光瞬步:
            case 煉獄巫師.黃色光環:
            case 煉獄巫師.紅色光環:
            case 煉獄巫師.藍色光環:
            case 煉獄巫師.黑色光環:
            case 煉獄巫師.減益效果光環:
            case 煉獄巫師.瞬間移動爆發:
            case 煉獄巫師.黑暗閃電:
            case 煉獄巫師.煉獄鬥氣:
            case 狂狼勇士.強化連擊:
                return true;
            default:
                return false;
        }
    }

    public static int getKeydownSkillCancelReduceTime(SecondaryStatValueHolder mbsvh) {
        if (mbsvh == null || mbsvh.effect == null) {
            return 0;
        }
        return getKeydownSkillCancelReduceTime(mbsvh.effect.getSourceId(), mbsvh.getLeftTime());
    }

    public static int getKeydownSkillCancelReduceTime(int skillID, int leftTime) {
        switch (skillID) {
            case 阿戴爾.護堤:
            case 菈菈.山環抱:
                return 3500 * (leftTime / 1000);
            case 虎影.仙技_夢遊桃源:
                return 9700 * (leftTime / 1000);
            default:
                return 0;
        }
    }
}
