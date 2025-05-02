package connection.packet;

import Client.MapleClient;
import Config.configs.ServerConfig;
import Config.constants.ServerConstants;
import Opcode.Headler.OutHeader;
import Packet.PacketHelper;
import Server.ServerType;
import connection.OutPacket;
import SwordieX.enums.LoginType;
import SwordieX.util.Position;
import SwordieX.util.container.Tuple;
import SwordieX.world.World;
import tools.StringUtil;

import java.util.Map;
import java.util.Set;

public class Login {

    /**
     * 發送一個handshake封包
     *
     * @param mapleVersion 客戶端版本
     * @param siv          發送iv
     * @param riv          接收iv
     * @param type         伺服器類型
     * @return 封包
     */
    public static OutPacket sendConnect(short mapleVersion, byte[] siv, byte[] riv, ServerType type) {
        int version = mapleVersion + ((type == ServerType.ChatServer) ? 159 : 0);
        byte mapleRegion = ServerConstants.MapleRegion;
        if (ServerConstants.TestServer) {
            switch (mapleRegion) {
                case 1:
                case 5:
                    mapleRegion += 1;
                    break;
                default:
                    mapleVersion = Short.parseShort("1" + String.valueOf(mapleVersion));
                    break;
            }
        }

        OutPacket oPacket = new OutPacket(version);

        if (type != ServerType.LoginServer) {
            oPacket.encodeInt(version);
        } else {
            oPacket.encodeString(ServerConfig.MapleMinor + ":1");
        }
        oPacket.encodeArr(siv);
        oPacket.encodeArr(riv);
        oPacket.encodeByte(mapleRegion);
        if (type != ServerType.LoginServer) {
            oPacket.encodeByte(4);
        } else {
            int loginSrvMapleMinor = Integer.parseInt(String.valueOf(mapleVersion) + StringUtil.getLeftPaddedStr(ServerConfig.MapleMinor, '0', 2));
            oPacket.encodeByte(0);
            oPacket.encodeShort(mapleVersion);
            oPacket.encodeShort(mapleVersion);
            oPacket.encodeShort(0);
            oPacket.encodeArr(siv);
            oPacket.encodeArr(riv);
            oPacket.encodeByte(mapleRegion);
            oPacket.encodeString(ServerConfig.MapleMinor + ":1");
            oPacket.encodeInt(loginSrvMapleMinor);
            oPacket.encodeInt(loginSrvMapleMinor);
            oPacket.encodeInt(0);
            oPacket.encodeShort(1);
            oPacket.encodeByte(5);
        }
        OutPacket newPacket = new OutPacket(oPacket.getLength());
        newPacket.encodeArr(oPacket.getData());

        return newPacket;
    }

    /**
     * 發送Login的Ping封包.
     *
     * @return 封包
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    public static OutPacket sendAliveReq() {
        return new OutPacket(OutHeader.CLIENT_ALIVE.getValue());
    }

    /**
     * 發送一個客戶端到GameServer的Ping封包
     *
     * @return 封包
     */

    public static OutPacket sendPingCheckResultClientToGame() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_PingCheckResult_ClientToGame.getValue());
        outPacket.encodeArr(new byte[46]);
        return outPacket;
    }

    public static OutPacket sendGuidRequest() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_AliveReq);
        return outPacket;
    }

    /**
     * 發送安全性(NGS)封包
     *
     * @return 封包
     */
    public static OutPacket sendSecurityPacket() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SecurityPacket);
        outPacket.encodeArr(new byte[5]);
        return outPacket;
    }

    /**
     * 發送伺服器參數設定
     *
     * @return 封包
     */
    public static OutPacket sendServerValues() {
        OutPacket oPacket = new OutPacket(OutHeader.LP_ServerValue);
        oPacket.encodeInt(30);
        oPacket.encodeInt(0x00000001);
        oPacket.encodeInt(0x00000015);
        oPacket.encodeInt(0x00000001);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000002);
        oPacket.encodeInt(0x00000019);
        oPacket.encodeInt(0x00000064);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000003);
        oPacket.encodeInt(0x000003E8);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000004);
        oPacket.encodeInt(0x000003E9);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000005);
        oPacket.encodeInt(0x000003EA);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000006);
        oPacket.encodeInt(0x000003F2);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000000A);
        oPacket.encodeInt(0x00000444);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000000B);
        oPacket.encodeInt(0x00002712);
        oPacket.encodeInt(0x00000007);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000000C);
        oPacket.encodeInt(0x00002713);
        oPacket.encodeInt(0x00000001);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000000D);
        oPacket.encodeInt(0x00002722);
        oPacket.encodeInt(0x00000001);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000000F);
        oPacket.encodeInt(0x00002751);
        oPacket.encodeInt(0x00000001);
        oPacket.encodeString("AllServer");
        oPacket.encodeInt(0x00000010);
        oPacket.encodeInt(0x0000272F);
        oPacket.encodeInt(0x000000FA);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000015);
        oPacket.encodeInt(0x000000CD);
        oPacket.encodeInt(0x00000001);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000017);
        oPacket.encodeInt(0x00002733);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000018);
        oPacket.encodeInt(0x00000119);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000001A);
        oPacket.encodeInt(0x000000C9);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000001B);
        oPacket.encodeInt(0x0000012F);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000001C);
        oPacket.encodeInt(0x0000006F);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000001D);
        oPacket.encodeInt(0x0000013C);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000001E);
        oPacket.encodeInt(0x00000144);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000001F);
        oPacket.encodeInt(0x00000142);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000020);
        oPacket.encodeInt(0x00002754);
        oPacket.encodeInt(0x00000001);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000021);
        oPacket.encodeInt(0x000000A3);
        oPacket.encodeInt(0x00007530);
        oPacket.encodeString("Project|Center|WorldID|0");
        oPacket.encodeInt(0x00000024);
        oPacket.encodeInt(0x00002750);
        oPacket.encodeInt(0x0000000A);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000025);
        oPacket.encodeInt(0x00000237);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000026);
        oPacket.encodeInt(0x00000238);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000027);
        oPacket.encodeInt(0x00000220);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000028);
        oPacket.encodeInt(0x000000E8);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x00000029);
        oPacket.encodeInt(0x0000049D);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");
        oPacket.encodeInt(0x0000002A);
        oPacket.encodeInt(0x000001B1);
        oPacket.encodeInt(0x00000000);
        oPacket.encodeString("All");

        return oPacket;
    }

    /**
     * 發送伺服器服務狀態
     *
     * @return 封包
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    public static OutPacket sendServerEnvironment() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_ServerEnvironment);
        outPacket.encodeInt(9);
        outPacket.encodeInt(1);
        outPacket.encodeInt(10006);
        outPacket.encodeString("BlackList|400011068,400011069,31221002");
        outPacket.encodeString("AllServer");
        outPacket.encodeInt(2);
        outPacket.encodeInt(1020);
        outPacket.encodeString("2000:1|3500:2|5000:3|6500:4|8000:5|99999:6");
        outPacket.encodeString("All");
        outPacket.encodeInt(3);
        outPacket.encodeInt(10005);
        outPacket.encodeString("Cluster:0,Trade:0,TrustHackLog:1,HackLog:1,SpecialMobClear:1,BossHuntWorker:1,NoHuntLevelUp:1,UrusHunt:1,PartyWork:0,SeedHacker:1,Macro:1,CubeSpeedHack:1,Casino:1,NGSBypass:1,AbnormalAccess:1");
        outPacket.encodeString("All");
        outPacket.encodeInt(4);
        outPacket.encodeInt(10015);
        outPacket.encodeString("20240919");
        outPacket.encodeString("All");
        outPacket.encodeInt(5);
        outPacket.encodeInt(10001);
        outPacket.encodeString("867151000,867164700,867164701,867164702");
        outPacket.encodeString("All");
        outPacket.encodeInt(6);
        outPacket.encodeInt(10003);
        outPacket.encodeString("9401217,9401726");
        outPacket.encodeString("All");
        outPacket.encodeInt(7);
        outPacket.encodeInt(10002);
        outPacket.encodeString("2350001,2433328,2510720,2510721,2637201,5062500,5222123");
        outPacket.encodeString("All");
        outPacket.encodeInt(8);
        outPacket.encodeInt(10016);
        outPacket.encodeString("WhiteList|80011247,80011248,80011249,80011250,80011251");
        outPacket.encodeString("All");
        outPacket.encodeInt(9);
        outPacket.encodeInt(10004);
        outPacket.encodeShort(0);
        outPacket.encodeString("All");
        return outPacket;
    }


    /**
     * 發送要求檢查檔案列表
     *
     * @return 封包
     */
    public static OutPacket sendWZCheckList() {
        OutPacket oPacket = new OutPacket(OutHeader.LP_FileCheckList);
        oPacket.encodeInt(686);
        oPacket.encodeString("Data\\Base\\Base.wz");
        oPacket.encodeString("Data\\Base\\Base_000.wz");
        oPacket.encodeString("Data\\Character\\Accessory\\Accessory.wz");
        oPacket.encodeString("Data\\Character\\Accessory\\Accessory_000.wz");
        oPacket.encodeString("Data\\Character\\Accessory\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Accessory\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Afterimage\\Afterimage.wz");
        oPacket.encodeString("Data\\Character\\Afterimage\\Afterimage_000.wz");
        oPacket.encodeString("Data\\Character\\Afterimage\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Afterimage\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Android\\Android.wz");
        oPacket.encodeString("Data\\Character\\Android\\Android_000.wz");
        oPacket.encodeString("Data\\Character\\Android\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Android\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\ArcaneForce\\ArcaneForce.wz");
        oPacket.encodeString("Data\\Character\\ArcaneForce\\ArcaneForce_000.wz");
        oPacket.encodeString("Data\\Character\\ArcaneForce\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\ArcaneForce\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\AuthenticForce\\AuthenticForce.wz");
        oPacket.encodeString("Data\\Character\\AuthenticForce\\AuthenticForce_000.wz");
        oPacket.encodeString("Data\\Character\\AuthenticForce\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\AuthenticForce\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Bits\\Bits.wz");
        oPacket.encodeString("Data\\Character\\Bits\\Bits_000.wz");
        oPacket.encodeString("Data\\Character\\Bits\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Bits\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Cap\\Cap.wz");
        oPacket.encodeString("Data\\Character\\Cap\\Cap_000.wz");
        oPacket.encodeString("Data\\Character\\Cap\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Cap\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Cape\\Cape.wz");
        oPacket.encodeString("Data\\Character\\Cape\\Cape_000.wz");
        oPacket.encodeString("Data\\Character\\Cape\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Cape\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Character.wz");
        oPacket.encodeString("Data\\Character\\Character_000.wz");
        oPacket.encodeString("Data\\Character\\Coat\\Coat.wz");
        oPacket.encodeString("Data\\Character\\Coat\\Coat_000.wz");
        oPacket.encodeString("Data\\Character\\Coat\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Coat\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Dragon\\Dragon.wz");
        oPacket.encodeString("Data\\Character\\Dragon\\Dragon_000.wz");
        oPacket.encodeString("Data\\Character\\Dragon\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Dragon\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Face\\Face.wz");
        oPacket.encodeString("Data\\Character\\Face\\Face_000.wz");
        oPacket.encodeString("Data\\Character\\Face\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Face\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Familiar\\Familiar.wz");
        oPacket.encodeString("Data\\Character\\Familiar\\Familiar_000.wz");
        oPacket.encodeString("Data\\Character\\Familiar\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Familiar\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Glove\\Glove.wz");
        oPacket.encodeString("Data\\Character\\Glove\\Glove_000.wz");
        oPacket.encodeString("Data\\Character\\Glove\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Glove\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Hair\\Hair.wz");
        oPacket.encodeString("Data\\Character\\Hair\\Hair_000.wz");
        oPacket.encodeString("Data\\Character\\Hair\\Hair_001.wz");
        oPacket.encodeString("Data\\Character\\Hair\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Hair\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Longcoat\\Longcoat.wz");
        oPacket.encodeString("Data\\Character\\Longcoat\\Longcoat_000.wz");
        oPacket.encodeString("Data\\Character\\Longcoat\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Longcoat\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Mechanic\\Mechanic.wz");
        oPacket.encodeString("Data\\Character\\Mechanic\\Mechanic_000.wz");
        oPacket.encodeString("Data\\Character\\Mechanic\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Mechanic\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Pants\\Pants.wz");
        oPacket.encodeString("Data\\Character\\Pants\\Pants_000.wz");
        oPacket.encodeString("Data\\Character\\Pants\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Pants\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\PetEquip\\PetEquip.wz");
        oPacket.encodeString("Data\\Character\\PetEquip\\PetEquip_000.wz");
        oPacket.encodeString("Data\\Character\\PetEquip\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\PetEquip\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Ring\\Ring.wz");
        oPacket.encodeString("Data\\Character\\Ring\\Ring_000.wz");
        oPacket.encodeString("Data\\Character\\Ring\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Ring\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Shield\\Shield.wz");
        oPacket.encodeString("Data\\Character\\Shield\\Shield_000.wz");
        oPacket.encodeString("Data\\Character\\Shield\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Shield\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Shoes\\Shoes.wz");
        oPacket.encodeString("Data\\Character\\Shoes\\Shoes_000.wz");
        oPacket.encodeString("Data\\Character\\Shoes\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Shoes\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\SkillSkin\\SkillSkin.wz");
        oPacket.encodeString("Data\\Character\\SkillSkin\\SkillSkin_000.wz");
        oPacket.encodeString("Data\\Character\\SkillSkin\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\SkillSkin\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\TamingMob.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\TamingMob_000.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Character\\TamingMob\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Character\\Totem\\Totem.wz");
        oPacket.encodeString("Data\\Character\\Totem\\Totem_000.wz");
        oPacket.encodeString("Data\\Character\\Totem\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Totem\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\Weapon.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\Weapon_000.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Character\\Weapon\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Character\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Character\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Effect\\Effect.wz");
        oPacket.encodeString("Data\\Effect\\Effect_000.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_008.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_009.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_010.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_011.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_012.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_013.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_014.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_015.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_016.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_017.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_018.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_019.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_020.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_021.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_022.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_023.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_024.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_025.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_026.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_027.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_028.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_029.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_030.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_031.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_032.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_033.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_034.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_035.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_036.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_037.wz");
        oPacket.encodeString("Data\\Effect\\_Canvas\\_Canvas_038.wz");
        oPacket.encodeString("Data\\Etc\\Achievement\\Achievement.wz");
        oPacket.encodeString("Data\\Etc\\Achievement\\AchievementData\\AchievementData.wz");
        oPacket.encodeString("Data\\Etc\\Achievement\\AchievementData\\AchievementData_000.wz");
        oPacket.encodeString("Data\\Etc\\Achievement\\Achievement_000.wz");
        oPacket.encodeString("Data\\Etc\\Android\\Android.wz");
        oPacket.encodeString("Data\\Etc\\Android\\Android_000.wz");
        oPacket.encodeString("Data\\Etc\\Etc.wz");
        oPacket.encodeString("Data\\Etc\\Etc_000.wz");
        oPacket.encodeString("Data\\Etc\\GuildCastleDecoration\\GuildCastleDecoration.wz");
        oPacket.encodeString("Data\\Etc\\GuildCastleDecoration\\GuildCastleDecoration_000.wz");
        oPacket.encodeString("Data\\Etc\\GuildCastleDecoration\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Etc\\GuildCastleDecoration\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Etc\\MExplorerReactor\\MExplorerReactor.wz");
        oPacket.encodeString("Data\\Etc\\MExplorerReactor\\MExplorerReactor_000.wz");
        oPacket.encodeString("Data\\Etc\\MExplorerReactor\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Etc\\MExplorerReactor\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Etc\\PL_Obstacle\\PL_Obstacle.wz");
        oPacket.encodeString("Data\\Etc\\PL_Obstacle\\PL_Obstacle_000.wz");
        oPacket.encodeString("Data\\Etc\\PL_Obstacle\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Etc\\PL_Obstacle\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Etc\\RoguelikeReactor\\RoguelikeReactor.wz");
        oPacket.encodeString("Data\\Etc\\RoguelikeReactor\\RoguelikeReactor_000.wz");
        oPacket.encodeString("Data\\Etc\\RoguelikeReactor\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Etc\\RoguelikeReactor\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Etc\\Script\\Script.wz");
        oPacket.encodeString("Data\\Etc\\Script\\Script_000.wz");
        oPacket.encodeString("Data\\Etc\\WZ2Lua\\WZ2Lua.wz");
        oPacket.encodeString("Data\\Etc\\WZ2Lua\\WZ2Lua_000.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Etc\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Item\\Cash\\Cash.wz");
        oPacket.encodeString("Data\\Item\\Cash\\Cash_000.wz");
        oPacket.encodeString("Data\\Item\\Cash\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Item\\Cash\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Item\\Cash\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Item\\Consume\\Consume.wz");
        oPacket.encodeString("Data\\Item\\Consume\\Consume_000.wz");
        oPacket.encodeString("Data\\Item\\Consume\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Item\\Consume\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Item\\Etc\\Etc.wz");
        oPacket.encodeString("Data\\Item\\Etc\\Etc_000.wz");
        oPacket.encodeString("Data\\Item\\Etc\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Item\\Etc\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Item\\Install\\Install.wz");
        oPacket.encodeString("Data\\Item\\Install\\Install_000.wz");
        oPacket.encodeString("Data\\Item\\Install\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Item\\Install\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Item\\Install\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Item\\Install\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Item\\Install\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Item\\Install\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Item\\Item.wz");
        oPacket.encodeString("Data\\Item\\Item_000.wz");
        oPacket.encodeString("Data\\Item\\Pet\\Pet.wz");
        oPacket.encodeString("Data\\Item\\Pet\\Pet_000.wz");
        oPacket.encodeString("Data\\Item\\Pet\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Item\\Pet\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Item\\Special\\Special.wz");
        oPacket.encodeString("Data\\Item\\Special\\Special_000.wz");
        oPacket.encodeString("Data\\Item\\Special\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Item\\Special\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Item\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Item\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Back\\Back.wz");
        oPacket.encodeString("Data\\Map\\Back\\Back_000.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_008.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_009.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_010.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_011.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_012.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_013.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_014.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_015.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_016.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_017.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_018.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_019.wz");
        oPacket.encodeString("Data\\Map\\Back\\_Canvas\\_Canvas_020.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map0\\Map0.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map0\\Map0_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map0\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map0\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map1\\Map1.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map1\\Map1_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map1\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map1\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map2\\Map2.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map2\\Map2_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map2\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map2\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map3\\Map3.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map3\\Map3_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map3\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map3\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map4\\Map4.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map4\\Map4_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map4\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map4\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map5\\Map5.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map5\\Map5_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map6\\Map6.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map6\\Map6_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map6\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map6\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map7\\Map7.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map7\\Map7_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map7\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map7\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map8\\Map8.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map8\\Map8_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map8\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map8\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map9\\Map9.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map9\\Map9_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map9\\Map9_001.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map9\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map9\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Map\\Map_000.wz");
        oPacket.encodeString("Data\\Map\\Map.wz");
        oPacket.encodeString("Data\\Map\\Map_000.wz");
        oPacket.encodeString("Data\\Map\\Obj\\Obj.wz");
        oPacket.encodeString("Data\\Map\\Obj\\Obj_000.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_008.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_009.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_010.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_011.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_012.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_013.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_014.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_015.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_016.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_017.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_018.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_019.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_020.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_021.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_022.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_023.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_024.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_025.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_026.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_027.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_028.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_029.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_030.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_031.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_032.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_033.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_034.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_035.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_036.wz");
        oPacket.encodeString("Data\\Map\\Obj\\_Canvas\\_Canvas_037.wz");
        oPacket.encodeString("Data\\Map\\Tile\\Tile.wz");
        oPacket.encodeString("Data\\Map\\Tile\\Tile_000.wz");
        oPacket.encodeString("Data\\Map\\Tile\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\Tile\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\WorldMap\\WorldMap.wz");
        oPacket.encodeString("Data\\Map\\WorldMap\\WorldMap_000.wz");
        oPacket.encodeString("Data\\Map\\WorldMap\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\WorldMap\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Map\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\AbyssExpeditionMob.wz");
        oPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\AbyssExpeditionMob_000.wz");
        oPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Mob\\BossPattern\\BossPattern.wz");
        oPacket.encodeString("Data\\Mob\\BossPattern\\BossPattern_000.wz");
        oPacket.encodeString("Data\\Mob\\BossPattern\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Mob\\BossPattern\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Mob\\BossPattern\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Mob\\BossPattern\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Mob\\MExplorerMob\\MExplorerMob.wz");
        oPacket.encodeString("Data\\Mob\\MExplorerMob\\MExplorerMob_000.wz");
        oPacket.encodeString("Data\\Mob\\MExplorerMob\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Mob\\MExplorerMob\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Mob\\Mob.wz");
        oPacket.encodeString("Data\\Mob\\Mob_000.wz");
        oPacket.encodeString("Data\\Mob\\Mob_001.wz");
        oPacket.encodeString("Data\\Mob\\QuestCountGroup\\QuestCountGroup.wz");
        oPacket.encodeString("Data\\Mob\\QuestCountGroup\\QuestCountGroup_000.wz");
        oPacket.encodeString("Data\\Mob\\RoguelikeMob\\RoguelikeMob.wz");
        oPacket.encodeString("Data\\Mob\\RoguelikeMob\\RoguelikeMob_000.wz");
        oPacket.encodeString("Data\\Mob\\RoguelikeMob\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Mob\\RoguelikeMob\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Mob\\RoguelikeMob\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_008.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_009.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_010.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_011.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_012.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_013.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_014.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_015.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_016.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_017.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_018.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_019.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_020.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_021.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_022.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_023.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_024.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_025.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_026.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_027.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_028.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_029.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_030.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_031.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_032.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_033.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_034.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_035.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_036.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_037.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_038.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_039.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_040.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_041.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_042.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_043.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_044.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_045.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_046.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_047.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_048.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_049.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_050.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_051.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_052.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_053.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_054.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_055.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_056.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_057.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_058.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_059.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_060.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_061.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_062.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_063.wz");
        oPacket.encodeString("Data\\Mob\\_Canvas\\_Canvas_064.wz");
        oPacket.encodeString("Data\\Morph\\Morph.wz");
        oPacket.encodeString("Data\\Morph\\Morph_000.wz");
        oPacket.encodeString("Data\\Morph\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Morph\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Npc\\Npc.wz");
        oPacket.encodeString("Data\\Npc\\Npc_000.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_008.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_009.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_010.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_011.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_012.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_013.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_014.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_015.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_016.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_017.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_018.wz");
        oPacket.encodeString("Data\\Npc\\_Canvas\\_Canvas_019.wz");
        oPacket.encodeString("Data\\Quest\\Quest.wz");
        oPacket.encodeString("Data\\Quest\\QuestData\\QuestData.wz");
        oPacket.encodeString("Data\\Quest\\QuestData\\QuestData_000.wz");
        oPacket.encodeString("Data\\Quest\\Quest_000.wz");
        oPacket.encodeString("Data\\Reactor\\Reactor.wz");
        oPacket.encodeString("Data\\Reactor\\Reactor_000.wz");
        oPacket.encodeString("Data\\Reactor\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Reactor\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Reactor\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\AbyssExpedition.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\AbyssExpedition_000.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\Skill.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\Skill_000.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Skill\\AbyssExpedition\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Skill\\Dragon\\Dragon.wz");
        oPacket.encodeString("Data\\Skill\\Dragon\\Dragon_000.wz");
        oPacket.encodeString("Data\\Skill\\Dragon\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Skill\\Dragon\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Skill\\MobSkill\\MobSkill.wz");
        oPacket.encodeString("Data\\Skill\\MobSkill\\MobSkill_000.wz");
        oPacket.encodeString("Data\\Skill\\MobSkill\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Skill\\MobSkill\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Skill\\MobSkill\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Skill\\MobSkill\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Skill\\Roguelike\\Roguelike.wz");
        oPacket.encodeString("Data\\Skill\\Roguelike\\Roguelike_000.wz");
        oPacket.encodeString("Data\\Skill\\Roguelike\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Skill\\Roguelike\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Skill\\Roguelike\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Skill\\Skill.wz");
        oPacket.encodeString("Data\\Skill\\Skill_000.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_008.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_009.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_010.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_011.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_012.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_013.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_014.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_015.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_016.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_017.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_018.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_019.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_020.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_021.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_022.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_023.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_024.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_025.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_026.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_027.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_028.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_029.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_030.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_031.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_032.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_033.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_034.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_035.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_036.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_037.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_038.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_039.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_040.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_041.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_042.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_043.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_044.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_045.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_046.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_047.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_048.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_049.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_050.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_051.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_052.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_053.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_054.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_055.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_056.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_057.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_058.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_059.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_060.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_061.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_062.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_063.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_064.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_065.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_066.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_067.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_068.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_069.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_070.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_071.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_072.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_073.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_074.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_075.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_076.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_077.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_078.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_079.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_080.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_081.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_082.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_083.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_084.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_085.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_086.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_087.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_088.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_089.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_090.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_091.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_092.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_093.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_094.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_095.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_096.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_097.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_098.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_099.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_100.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_101.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_102.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_103.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_104.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_105.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_106.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_107.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_108.wz");
        oPacket.encodeString("Data\\Skill\\_Canvas\\_Canvas_109.wz");
        oPacket.encodeString("Data\\Sound\\Sound.wz");
        oPacket.encodeString("Data\\Sound\\Sound_000.wz");
        oPacket.encodeString("Data\\Sound\\Sound_001.wz");
        oPacket.encodeString("Data\\Sound\\Sound_002.wz");
        oPacket.encodeString("Data\\Sound\\Sound_003.wz");
        oPacket.encodeString("Data\\Sound\\Sound_004.wz");
        oPacket.encodeString("Data\\Sound\\Sound_005.wz");
        oPacket.encodeString("Data\\Sound\\Sound_006.wz");
        oPacket.encodeString("Data\\Sound\\Sound_007.wz");
        oPacket.encodeString("Data\\Sound\\Sound_008.wz");
        oPacket.encodeString("Data\\Sound\\Sound_009.wz");
        oPacket.encodeString("Data\\Sound\\Sound_010.wz");
        oPacket.encodeString("Data\\Sound\\Sound_011.wz");
        oPacket.encodeString("Data\\Sound\\Sound_012.wz");
        oPacket.encodeString("Data\\Sound\\Sound_013.wz");
        oPacket.encodeString("Data\\Sound\\Sound_014.wz");
        oPacket.encodeString("Data\\Sound\\Sound_015.wz");
        oPacket.encodeString("Data\\Sound\\Sound_016.wz");
        oPacket.encodeString("Data\\Sound\\Sound_017.wz");
        oPacket.encodeString("Data\\Sound\\Sound_018.wz");
        oPacket.encodeString("Data\\Sound\\Sound_019.wz");
        oPacket.encodeString("Data\\Sound\\Sound_020.wz");
        oPacket.encodeString("Data\\Sound\\Sound_021.wz");
        oPacket.encodeString("Data\\Sound\\Sound_022.wz");
        oPacket.encodeString("Data\\Sound\\Sound_023.wz");
        oPacket.encodeString("Data\\Sound\\Sound_024.wz");
        oPacket.encodeString("Data\\Sound\\Sound_025.wz");
        oPacket.encodeString("Data\\Sound\\Sound_026.wz");
        oPacket.encodeString("Data\\Sound\\Sound_027.wz");
        oPacket.encodeString("Data\\Sound\\Sound_028.wz");
        oPacket.encodeString("Data\\Sound\\Sound_029.wz");
        oPacket.encodeString("Data\\Sound\\Sound_030.wz");
        oPacket.encodeString("Data\\Sound\\Sound_031.wz");
        oPacket.encodeString("Data\\Sound\\Sound_032.wz");
        oPacket.encodeString("Data\\Sound\\Sound_033.wz");
        oPacket.encodeString("Data\\Sound\\Sound_034.wz");
        oPacket.encodeString("Data\\Sound\\Sound_035.wz");
        oPacket.encodeString("Data\\Sound\\Sound_036.wz");
        oPacket.encodeString("Data\\Sound\\Sound_037.wz");
        oPacket.encodeString("Data\\Sound\\Sound_038.wz");
        oPacket.encodeString("Data\\Sound\\Sound_039.wz");
        oPacket.encodeString("Data\\Sound\\Sound_040.wz");
        oPacket.encodeString("Data\\Sound\\Sound_041.wz");
        oPacket.encodeString("Data\\Sound\\Sound_042.wz");
        oPacket.encodeString("Data\\Sound\\Sound_043.wz");
        oPacket.encodeString("Data\\Sound\\Sound_044.wz");
        oPacket.encodeString("Data\\Sound\\Sound_045.wz");
        oPacket.encodeString("Data\\Sound\\Sound_046.wz");
        oPacket.encodeString("Data\\Sound\\Sound_047.wz");
        oPacket.encodeString("Data\\String\\String.wz");
        oPacket.encodeString("Data\\String\\String_000.wz");
        oPacket.encodeString("Data\\String\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\String\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\TamingMob\\TamingMob.wz");
        oPacket.encodeString("Data\\TamingMob\\TamingMob_000.wz");
        oPacket.encodeString("Data\\UI\\UI.wz");
        oPacket.encodeString("Data\\UI\\UI_000.wz");
        oPacket.encodeString("Data\\UI\\WZ2Lua\\Roguelike\\Roguelike.wz");
        oPacket.encodeString("Data\\UI\\WZ2Lua\\Roguelike\\Roguelike_000.wz");
        oPacket.encodeString("Data\\UI\\WZ2Lua\\WZ2Lua.wz");
        oPacket.encodeString("Data\\UI\\WZ2Lua\\WZ2Lua_000.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_000.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_001.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_002.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_003.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_004.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_005.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_006.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_007.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_008.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_009.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_010.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_011.wz");
        oPacket.encodeString("Data\\UI\\_Canvas\\_Canvas_012.wz");

        return oPacket;
    }

    /**
     * 發送要求檢查檔案列表
     *
     * @return 封包
     */
    public static OutPacket sendFileCheckList() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_WzCheckList);
        outPacket.encodeInt(336); // change int 336
        outPacket.encodeString("Canvas.dll");
        outPacket.encodeString("CrashReporter_64.dll");
        outPacket.encodeString("D3DX9_43.dll");
        outPacket.encodeString("Data\\Base\\Base.ini");
        outPacket.encodeString("Data\\Base\\Base.wz");
        outPacket.encodeString("Data\\Base\\Base_000.wz");
        outPacket.encodeString("Data\\Character\\Accessory\\Accessory.ini");
        outPacket.encodeString("Data\\Character\\Accessory\\Accessory.wz");
        outPacket.encodeString("Data\\Character\\Accessory\\Accessory_000.wz");
        outPacket.encodeString("Data\\Character\\Afterimage\\Afterimage.ini");
        outPacket.encodeString("Data\\Character\\Afterimage\\Afterimage.wz");
        outPacket.encodeString("Data\\Character\\Afterimage\\Afterimage_000.wz");
        outPacket.encodeString("Data\\Character\\Android\\Android.ini");
        outPacket.encodeString("Data\\Character\\Android\\Android.wz");
        outPacket.encodeString("Data\\Character\\Android\\Android_000.wz");
        outPacket.encodeString("Data\\Character\\ArcaneForce\\ArcaneForce.ini");
        outPacket.encodeString("Data\\Character\\ArcaneForce\\ArcaneForce.wz");
        outPacket.encodeString("Data\\Character\\ArcaneForce\\ArcaneForce_000.wz");
        outPacket.encodeString("Data\\Character\\AuthenticForce\\AuthenticForce.ini");
        outPacket.encodeString("Data\\Character\\AuthenticForce\\AuthenticForce.wz");
        outPacket.encodeString("Data\\Character\\AuthenticForce\\AuthenticForce_000.wz");
        outPacket.encodeString("Data\\Character\\Bits\\Bits.ini");
        outPacket.encodeString("Data\\Character\\Bits\\Bits.wz");
        outPacket.encodeString("Data\\Character\\Bits\\Bits_000.wz");
        outPacket.encodeString("Data\\Character\\Cap\\Cap.ini");
        outPacket.encodeString("Data\\Character\\Cap\\Cap.wz");
        outPacket.encodeString("Data\\Character\\Cap\\Cap_000.wz");
        outPacket.encodeString("Data\\Character\\Cape\\Cape.ini");
        outPacket.encodeString("Data\\Character\\Cape\\Cape.wz");
        outPacket.encodeString("Data\\Character\\Cape\\Cape_000.wz");
        outPacket.encodeString("Data\\Character\\Character.ini");
        outPacket.encodeString("Data\\Character\\Character.wz");
        outPacket.encodeString("Data\\Character\\Character_000.wz");
        outPacket.encodeString("Data\\Character\\Coat\\Coat.ini");
        outPacket.encodeString("Data\\Character\\Coat\\Coat.wz");
        outPacket.encodeString("Data\\Character\\Coat\\Coat_000.wz");
        outPacket.encodeString("Data\\Character\\Dragon\\Dragon.ini");
        outPacket.encodeString("Data\\Character\\Dragon\\Dragon.wz");
        outPacket.encodeString("Data\\Character\\Dragon\\Dragon_000.wz");
        outPacket.encodeString("Data\\Character\\Face\\Face.ini");
        outPacket.encodeString("Data\\Character\\Face\\Face.wz");
        outPacket.encodeString("Data\\Character\\Face\\Face_000.wz");
        outPacket.encodeString("Data\\Character\\Familiar\\Familiar.ini");
        outPacket.encodeString("Data\\Character\\Familiar\\Familiar.wz");
        outPacket.encodeString("Data\\Character\\Familiar\\Familiar_000.wz");
        outPacket.encodeString("Data\\Character\\Glove\\Glove.ini");
        outPacket.encodeString("Data\\Character\\Glove\\Glove.wz");
        outPacket.encodeString("Data\\Character\\Glove\\Glove_000.wz");
        outPacket.encodeString("Data\\Character\\Hair\\Hair.ini");
        outPacket.encodeString("Data\\Character\\Hair\\Hair.wz");
        outPacket.encodeString("Data\\Character\\Hair\\Hair_000.wz");
        outPacket.encodeString("Data\\Character\\Hair\\Hair_001.wz");
        outPacket.encodeString("Data\\Character\\Longcoat\\Longcoat.ini");
        outPacket.encodeString("Data\\Character\\Longcoat\\Longcoat.wz");
        outPacket.encodeString("Data\\Character\\Longcoat\\Longcoat_000.wz");
        outPacket.encodeString("Data\\Character\\Mechanic\\Mechanic.ini");
        outPacket.encodeString("Data\\Character\\Mechanic\\Mechanic.wz");
        outPacket.encodeString("Data\\Character\\Mechanic\\Mechanic_000.wz");
        outPacket.encodeString("Data\\Character\\Pants\\Pants.ini");
        outPacket.encodeString("Data\\Character\\Pants\\Pants.wz");
        outPacket.encodeString("Data\\Character\\Pants\\Pants_000.wz");
        outPacket.encodeString("Data\\Character\\PetEquip\\PetEquip.ini");
        outPacket.encodeString("Data\\Character\\PetEquip\\PetEquip.wz");
        outPacket.encodeString("Data\\Character\\PetEquip\\PetEquip_000.wz");
        outPacket.encodeString("Data\\Character\\Ring\\Ring.ini");
        outPacket.encodeString("Data\\Character\\Ring\\Ring.wz");
        outPacket.encodeString("Data\\Character\\Ring\\Ring_000.wz");
        outPacket.encodeString("Data\\Character\\Shield\\Shield.ini");
        outPacket.encodeString("Data\\Character\\Shield\\Shield.wz");
        outPacket.encodeString("Data\\Character\\Shield\\Shield_000.wz");
        outPacket.encodeString("Data\\Character\\Shoes\\Shoes.ini");
        outPacket.encodeString("Data\\Character\\Shoes\\Shoes.wz");
        outPacket.encodeString("Data\\Character\\Shoes\\Shoes_000.wz");
        outPacket.encodeString("Data\\Character\\SkillSkin\\SkillSkin.ini");
        outPacket.encodeString("Data\\Character\\SkillSkin\\SkillSkin.wz");
        outPacket.encodeString("Data\\Character\\SkillSkin\\SkillSkin_000.wz");
        outPacket.encodeString("Data\\Character\\TamingMob\\TamingMob.ini");
        outPacket.encodeString("Data\\Character\\TamingMob\\TamingMob.wz");
        outPacket.encodeString("Data\\Character\\TamingMob\\TamingMob_000.wz");
        outPacket.encodeString("Data\\Character\\Totem\\Totem.ini");
        outPacket.encodeString("Data\\Character\\Totem\\Totem.wz");
        outPacket.encodeString("Data\\Character\\Totem\\Totem_000.wz");
        outPacket.encodeString("Data\\Character\\Weapon\\Weapon.ini");
        outPacket.encodeString("Data\\Character\\Weapon\\Weapon.wz");
        outPacket.encodeString("Data\\Character\\Weapon\\Weapon_000.wz");
        outPacket.encodeString("Data\\Effect\\Effect.ini");
        outPacket.encodeString("Data\\Effect\\Effect.wz");
        outPacket.encodeString("Data\\Effect\\Effect_000.wz");
        outPacket.encodeString("Data\\Etc\\Achievement\\Achievement.ini");
        outPacket.encodeString("Data\\Etc\\Achievement\\Achievement.wz");
        outPacket.encodeString("Data\\Etc\\Achievement\\AchievementData\\AchievementData.ini");
        outPacket.encodeString("Data\\Etc\\Achievement\\AchievementData\\AchievementData.wz");
        outPacket.encodeString("Data\\Etc\\Achievement\\AchievementData\\AchievementData_000.wz");
        outPacket.encodeString("Data\\Etc\\Achievement\\Achievement_000.wz");
        outPacket.encodeString("Data\\Etc\\Android\\Android.ini");
        outPacket.encodeString("Data\\Etc\\Android\\Android.wz");
        outPacket.encodeString("Data\\Etc\\Android\\Android_000.wz");
        outPacket.encodeString("Data\\Etc\\Etc.ini");
        outPacket.encodeString("Data\\Etc\\Etc.wz");
        outPacket.encodeString("Data\\Etc\\Etc_000.wz");
        outPacket.encodeString("Data\\Etc\\GuildCastleDecoration\\GuildCastleDecoration.ini");
        outPacket.encodeString("Data\\Etc\\GuildCastleDecoration\\GuildCastleDecoration.wz");
        outPacket.encodeString("Data\\Etc\\GuildCastleDecoration\\GuildCastleDecoration_000.wz");
        outPacket.encodeString("Data\\Etc\\MExplorerReactor\\MExplorerReactor.ini");
        outPacket.encodeString("Data\\Etc\\MExplorerReactor\\MExplorerReactor.wz");
        outPacket.encodeString("Data\\Etc\\MExplorerReactor\\MExplorerReactor_000.wz");
        outPacket.encodeString("Data\\Etc\\PL_Obstacle\\PL_Obstacle.ini");
        outPacket.encodeString("Data\\Etc\\PL_Obstacle\\PL_Obstacle.wz");
        outPacket.encodeString("Data\\Etc\\PL_Obstacle\\PL_Obstacle_000.wz");
        outPacket.encodeString("Data\\Etc\\RoguelikeReactor\\RoguelikeReactor.ini");
        outPacket.encodeString("Data\\Etc\\RoguelikeReactor\\RoguelikeReactor.wz");
        outPacket.encodeString("Data\\Etc\\RoguelikeReactor\\RoguelikeReactor_000.wz");
        outPacket.encodeString("Data\\Etc\\Script\\Script.ini");
        outPacket.encodeString("Data\\Etc\\Script\\Script.wz");
        outPacket.encodeString("Data\\Etc\\Script\\Script_000.wz");
        outPacket.encodeString("Data\\Etc\\WZ2Lua\\WZ2Lua.ini");
        outPacket.encodeString("Data\\Etc\\WZ2Lua\\WZ2Lua.wz");
        outPacket.encodeString("Data\\Etc\\WZ2Lua\\WZ2Lua_000.wz");
        outPacket.encodeString("Data\\Item\\Cash\\Cash.ini");
        outPacket.encodeString("Data\\Item\\Cash\\Cash.wz");
        outPacket.encodeString("Data\\Item\\Cash\\Cash_000.wz");
        outPacket.encodeString("Data\\Item\\Consume\\Consume.ini");
        outPacket.encodeString("Data\\Item\\Consume\\Consume.wz");
        outPacket.encodeString("Data\\Item\\Consume\\Consume_000.wz");
        outPacket.encodeString("Data\\Item\\Etc\\Etc.ini");
        outPacket.encodeString("Data\\Item\\Etc\\Etc.wz");
        outPacket.encodeString("Data\\Item\\Etc\\Etc_000.wz");
        outPacket.encodeString("Data\\Item\\Install\\Install.ini");
        outPacket.encodeString("Data\\Item\\Install\\Install.wz");
        outPacket.encodeString("Data\\Item\\Install\\Install_000.wz");
        outPacket.encodeString("Data\\Item\\Item.ini");
        outPacket.encodeString("Data\\Item\\Item.wz");
        outPacket.encodeString("Data\\Item\\Item_000.wz");
        outPacket.encodeString("Data\\Item\\Pet\\Pet.ini");
        outPacket.encodeString("Data\\Item\\Pet\\Pet.wz");
        outPacket.encodeString("Data\\Item\\Pet\\Pet_000.wz");
        outPacket.encodeString("Data\\Item\\Special\\Special.ini");
        outPacket.encodeString("Data\\Item\\Special\\Special.wz");
        outPacket.encodeString("Data\\Item\\Special\\Special_000.wz");
        outPacket.encodeString("Data\\Map\\Back\\Back.ini");
        outPacket.encodeString("Data\\Map\\Back\\Back.wz");
        outPacket.encodeString("Data\\Map\\Back\\Back_000.wz");
        outPacket.encodeString("Data\\Map\\Map.ini");
        outPacket.encodeString("Data\\Map\\Map.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map0\\Map0.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map0\\Map0.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map0\\Map0_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map1\\Map1.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map1\\Map1.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map1\\Map1_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map2\\Map2.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map2\\Map2.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map2\\Map2_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map3\\Map3.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map3\\Map3.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map3\\Map3_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map4\\Map4.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map4\\Map4.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map4\\Map4_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map5\\Map5.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map5\\Map5.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map5\\Map5_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map6\\Map6.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map6\\Map6.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map6\\Map6_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map7\\Map7.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map7\\Map7.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map7\\Map7_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map8\\Map8.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map8\\Map8.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map8\\Map8_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map9\\Map9.ini");
        outPacket.encodeString("Data\\Map\\Map\\Map9\\Map9.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map9\\Map9_000.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map9\\Map9_001.wz");
        outPacket.encodeString("Data\\Map\\Map\\Map_000.wz");
        outPacket.encodeString("Data\\Map\\Map_000.wz");
        outPacket.encodeString("Data\\Map\\Obj\\Obj.ini");
        outPacket.encodeString("Data\\Map\\Obj\\Obj.wz");
        outPacket.encodeString("Data\\Map\\Obj\\Obj_000.wz");
        outPacket.encodeString("Data\\Map\\Tile\\Tile.ini");
        outPacket.encodeString("Data\\Map\\Tile\\Tile.wz");
        outPacket.encodeString("Data\\Map\\Tile\\Tile_000.wz");
        outPacket.encodeString("Data\\Map\\WorldMap\\WorldMap.ini");
        outPacket.encodeString("Data\\Map\\WorldMap\\WorldMap.wz");
        outPacket.encodeString("Data\\Map\\WorldMap\\WorldMap_000.wz");
        outPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\AbyssExpeditionMob.ini");
        outPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\AbyssExpeditionMob.wz");
        outPacket.encodeString("Data\\Mob\\AbyssExpeditionMob\\AbyssExpeditionMob_000.wz");
        outPacket.encodeString("Data\\Mob\\BossPattern\\BossPattern.ini");
        outPacket.encodeString("Data\\Mob\\BossPattern\\BossPattern.wz");
        outPacket.encodeString("Data\\Mob\\BossPattern\\BossPattern_000.wz");
        outPacket.encodeString("Data\\Mob\\MExplorerMob\\MExplorerMob.ini");
        outPacket.encodeString("Data\\Mob\\MExplorerMob\\MExplorerMob.wz");
        outPacket.encodeString("Data\\Mob\\MExplorerMob\\MExplorerMob_000.wz");
        outPacket.encodeString("Data\\Mob\\Mob.ini");
        outPacket.encodeString("Data\\Mob\\Mob.wz");
        outPacket.encodeString("Data\\Mob\\Mob_000.wz");
        outPacket.encodeString("Data\\Mob\\Mob_001.wz");
        outPacket.encodeString("Data\\Mob\\QuestCountGroup\\QuestCountGroup.ini");
        outPacket.encodeString("Data\\Mob\\QuestCountGroup\\QuestCountGroup.wz");
        outPacket.encodeString("Data\\Mob\\QuestCountGroup\\QuestCountGroup_000.wz");
        outPacket.encodeString("Data\\Mob\\RoguelikeMob\\RoguelikeMob.ini");
        outPacket.encodeString("Data\\Mob\\RoguelikeMob\\RoguelikeMob.wz");
        outPacket.encodeString("Data\\Mob\\RoguelikeMob\\RoguelikeMob_000.wz");
        outPacket.encodeString("Data\\Morph\\Morph.ini");
        outPacket.encodeString("Data\\Morph\\Morph.wz");
        outPacket.encodeString("Data\\Morph\\Morph_000.wz");
        outPacket.encodeString("Data\\Npc\\Npc.ini");
        outPacket.encodeString("Data\\Npc\\Npc.wz");
        outPacket.encodeString("Data\\Npc\\Npc_000.wz");
        outPacket.encodeString("Data\\Quest\\Quest.ini");
        outPacket.encodeString("Data\\Quest\\Quest.wz");
        outPacket.encodeString("Data\\Quest\\QuestData\\QuestData.ini");
        outPacket.encodeString("Data\\Quest\\QuestData\\QuestData.wz");
        outPacket.encodeString("Data\\Quest\\QuestData\\QuestData_000.wz");
        outPacket.encodeString("Data\\Quest\\Quest_000.wz");
        outPacket.encodeString("Data\\Reactor\\Reactor.ini");
        outPacket.encodeString("Data\\Reactor\\Reactor.wz");
        outPacket.encodeString("Data\\Reactor\\Reactor_000.wz");
        outPacket.encodeString("Data\\Skill\\AbyssExpedition\\AbyssExpedition.ini");
        outPacket.encodeString("Data\\Skill\\AbyssExpedition\\AbyssExpedition.wz");
        outPacket.encodeString("Data\\Skill\\AbyssExpedition\\AbyssExpedition_000.wz");
        outPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\Skill.ini");
        outPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\Skill.wz");
        outPacket.encodeString("Data\\Skill\\AbyssExpedition\\Skill\\Skill_000.wz");
        outPacket.encodeString("Data\\Skill\\Dragon\\Dragon.ini");
        outPacket.encodeString("Data\\Skill\\Dragon\\Dragon.wz");
        outPacket.encodeString("Data\\Skill\\Dragon\\Dragon_000.wz");
        outPacket.encodeString("Data\\Skill\\MobSkill\\MobSkill.ini");
        outPacket.encodeString("Data\\Skill\\MobSkill\\MobSkill.wz");
        outPacket.encodeString("Data\\Skill\\MobSkill\\MobSkill_000.wz");
        outPacket.encodeString("Data\\Skill\\Roguelike\\Roguelike.ini");
        outPacket.encodeString("Data\\Skill\\Roguelike\\Roguelike.wz");
        outPacket.encodeString("Data\\Skill\\Roguelike\\Roguelike_000.wz");
        outPacket.encodeString("Data\\Skill\\Skill.ini");
        outPacket.encodeString("Data\\Skill\\Skill.wz");
        outPacket.encodeString("Data\\Skill\\Skill_000.wz");
        outPacket.encodeString("Data\\String\\String.ini");
        outPacket.encodeString("Data\\String\\String.wz");
        outPacket.encodeString("Data\\String\\String_000.wz");
        outPacket.encodeString("Data\\TamingMob\\TamingMob.ini");
        outPacket.encodeString("Data\\TamingMob\\TamingMob.wz");
        outPacket.encodeString("Data\\TamingMob\\TamingMob_000.wz");
        outPacket.encodeString("Data\\UI\\UI.ini");
        outPacket.encodeString("Data\\UI\\UI.wz");
        outPacket.encodeString("Data\\UI\\UI_000.wz");
        outPacket.encodeString("Data\\UI\\WZ2Lua\\Roguelike\\Roguelike.ini");
        outPacket.encodeString("Data\\UI\\WZ2Lua\\Roguelike\\Roguelike.wz");
        outPacket.encodeString("Data\\UI\\WZ2Lua\\Roguelike\\Roguelike_000.wz");
        outPacket.encodeString("Data\\UI\\WZ2Lua\\WZ2Lua.ini");
        outPacket.encodeString("Data\\UI\\WZ2Lua\\WZ2Lua.wz");
        outPacket.encodeString("Data\\UI\\WZ2Lua\\WZ2Lua_000.wz");
        outPacket.encodeString("Dwarf.dll");
        outPacket.encodeString("DwarfAxe.exe");
        outPacket.encodeString("Gr2D_DX9.dll");
        outPacket.encodeString("HybridCore64.dll");
        outPacket.encodeString("MachineIdLib.dll");
        outPacket.encodeString("MapleSecurePC64.dll");
        outPacket.encodeString("MapleStory.exe");
        outPacket.encodeString("NGClient64.aes");
        outPacket.encodeString("NameSpace.dll");
        outPacket.encodeString("NexonAnalytics64.dll");
        outPacket.encodeString("NxOverlay\\CrashReporter_64.dll");
        outPacket.encodeString("NxOverlay\\DwarfAxe.exe");
        outPacket.encodeString("NxOverlay\\chrome_100_percent.pak");
        outPacket.encodeString("NxOverlay\\chrome_200_percent.pak");
        outPacket.encodeString("NxOverlay\\chrome_elf.dll");
        outPacket.encodeString("NxOverlay\\d3dcompiler_47.dll");
        outPacket.encodeString("NxOverlay\\icudtl.dat");
        outPacket.encodeString("NxOverlay\\libEGL.dll");
        outPacket.encodeString("NxOverlay\\libGLESv2.dll");
        outPacket.encodeString("NxOverlay\\libcef.dll");
        outPacket.encodeString("NxOverlay\\locales\\am.pak");
        outPacket.encodeString("NxOverlay\\locales\\ar.pak");
        outPacket.encodeString("NxOverlay\\locales\\bg.pak");
        outPacket.encodeString("NxOverlay\\locales\\bn.pak");
        outPacket.encodeString("NxOverlay\\locales\\ca.pak");
        outPacket.encodeString("NxOverlay\\locales\\cs.pak");
        outPacket.encodeString("NxOverlay\\locales\\da.pak");
        outPacket.encodeString("NxOverlay\\locales\\de.pak");
        outPacket.encodeString("NxOverlay\\locales\\el.pak");
        outPacket.encodeString("NxOverlay\\locales\\en-GB.pak");
        outPacket.encodeString("NxOverlay\\locales\\en-US.pak");
        outPacket.encodeString("NxOverlay\\locales\\es-419.pak");
        outPacket.encodeString("NxOverlay\\locales\\es.pak");
        outPacket.encodeString("NxOverlay\\locales\\et.pak");
        outPacket.encodeString("NxOverlay\\locales\\fa.pak");
        outPacket.encodeString("NxOverlay\\locales\\fi.pak");
        outPacket.encodeString("NxOverlay\\locales\\fil.pak");
        outPacket.encodeString("NxOverlay\\locales\\fr.pak");
        outPacket.encodeString("NxOverlay\\locales\\gu.pak");
        outPacket.encodeString("NxOverlay\\locales\\he.pak");
        outPacket.encodeString("NxOverlay\\locales\\hi.pak");
        outPacket.encodeString("NxOverlay\\locales\\hr.pak");
        outPacket.encodeString("NxOverlay\\locales\\hu.pak");
        outPacket.encodeString("NxOverlay\\locales\\id.pak");
        outPacket.encodeString("NxOverlay\\locales\\it.pak");
        outPacket.encodeString("NxOverlay\\locales\\ja.pak");
        outPacket.encodeString("NxOverlay\\locales\\kn.pak");
        outPacket.encodeString("NxOverlay\\locales\\ko.pak");
        outPacket.encodeString("NxOverlay\\locales\\lt.pak");
        outPacket.encodeString("NxOverlay\\locales\\lv.pak");
        outPacket.encodeString("NxOverlay\\locales\\ml.pak");
        outPacket.encodeString("NxOverlay\\locales\\mr.pak");
        outPacket.encodeString("NxOverlay\\locales\\ms.pak");
        outPacket.encodeString("NxOverlay\\locales\\nb.pak");
        outPacket.encodeString("NxOverlay\\locales\\nl.pak");
        outPacket.encodeString("NxOverlay\\locales\\pl.pak");
        outPacket.encodeString("NxOverlay\\locales\\pt-BR.pak");
        outPacket.encodeString("NxOverlay\\locales\\pt-PT.pak");
        outPacket.encodeString("NxOverlay\\locales\\ro.pak");
        outPacket.encodeString("NxOverlay\\locales\\ru.pak");
        outPacket.encodeString("NxOverlay\\locales\\sk.pak");
        outPacket.encodeString("NxOverlay\\locales\\sl.pak");
        outPacket.encodeString("NxOverlay\\locales\\sr.pak");
        outPacket.encodeString("NxOverlay\\locales\\sv.pak");
        outPacket.encodeString("NxOverlay\\locales\\sw.pak");
        outPacket.encodeString("NxOverlay\\locales\\ta.pak");
        outPacket.encodeString("NxOverlay\\locales\\te.pak");
        outPacket.encodeString("NxOverlay\\locales\\th.pak");
        outPacket.encodeString("NxOverlay\\locales\\tr.pak");
        outPacket.encodeString("NxOverlay\\locales\\uk.pak");
        outPacket.encodeString("NxOverlay\\locales\\vi.pak");
        outPacket.encodeString("NxOverlay\\locales\\zh-CN.pak");
        outPacket.encodeString("NxOverlay\\locales\\zh-TW.pak");
        outPacket.encodeString("NxOverlay\\resources.pak");
        outPacket.encodeString("NxOverlay\\snapshot_blob.bin");
        outPacket.encodeString("NxOverlay\\swiftshader\\libEGL.dll");
        outPacket.encodeString("NxOverlay\\swiftshader\\libGLESv2.dll");
        outPacket.encodeString("NxOverlay\\v8_context_snapshot.bin");
        outPacket.encodeString("NxOverlay_x64.dll");
        outPacket.encodeString("PCOM.dll");
        outPacket.encodeString("ResMan.dll");
        outPacket.encodeString("Shape2D.dll");
        outPacket.encodeString("Sound.dll");
        outPacket.encodeString("WzMss.dll");
        outPacket.encodeString("ZLZ64.dll");
        outPacket.encodeString("cef.pak");
        outPacket.encodeString("cef_100_percent.pak");
        outPacket.encodeString("cef_200_percent.pak");
        outPacket.encodeString("cef_extensions.pak");
        outPacket.encodeString("chrome_elf.dll");
        outPacket.encodeString("d3dcompiler_43.dll");
        outPacket.encodeString("d3dcompiler_47.dll");
        outPacket.encodeString("d3dx9_31.dll");
        outPacket.encodeString("devtools_resources.pak");
        outPacket.encodeString("icudtl.dat");
        outPacket.encodeString("jypc.dll");
        outPacket.encodeString("libEGL.dll");
        outPacket.encodeString("libGLESv2.dll");
        outPacket.encodeString("libcef.dll");
        outPacket.encodeString("libcurl.dll");
        outPacket.encodeString("locales\\am.pak");
        outPacket.encodeString("locales\\ar.pak");
        outPacket.encodeString("locales\\bg.pak");
        outPacket.encodeString("locales\\bn.pak");
        outPacket.encodeString("locales\\ca.pak");
        outPacket.encodeString("locales\\cs.pak");
        outPacket.encodeString("locales\\da.pak");
        outPacket.encodeString("locales\\de.pak");
        outPacket.encodeString("locales\\el.pak");
        outPacket.encodeString("locales\\en-GB.pak");
        outPacket.encodeString("locales\\en-US.pak");
        outPacket.encodeString("locales\\es-419.pak");
        outPacket.encodeString("locales\\es.pak");
        outPacket.encodeString("locales\\et.pak");
        outPacket.encodeString("locales\\fa.pak");
        outPacket.encodeString("locales\\fi.pak");
        outPacket.encodeString("locales\\fil.pak");
        outPacket.encodeString("locales\\fr.pak");
        outPacket.encodeString("locales\\gu.pak");
        outPacket.encodeString("locales\\he.pak");
        outPacket.encodeString("locales\\hi.pak");
        outPacket.encodeString("locales\\hr.pak");
        outPacket.encodeString("locales\\hu.pak");
        outPacket.encodeString("locales\\id.pak");
        outPacket.encodeString("locales\\it.pak");
        outPacket.encodeString("locales\\ja.pak");
        outPacket.encodeString("locales\\kn.pak");
        outPacket.encodeString("locales\\ko.pak");
        outPacket.encodeString("locales\\lt.pak");
        outPacket.encodeString("locales\\lv.pak");
        outPacket.encodeString("locales\\ml.pak");
        outPacket.encodeString("locales\\mr.pak");
        outPacket.encodeString("locales\\ms.pak");
        outPacket.encodeString("locales\\nb.pak");
        outPacket.encodeString("locales\\nl.pak");
        outPacket.encodeString("locales\\pl.pak");
        outPacket.encodeString("locales\\pt-BR.pak");
        outPacket.encodeString("locales\\pt-PT.pak");
        outPacket.encodeString("locales\\ro.pak");
        outPacket.encodeString("locales\\ru.pak");
        outPacket.encodeString("locales\\sk.pak");
        outPacket.encodeString("locales\\sl.pak");
        outPacket.encodeString("locales\\sr.pak");
        outPacket.encodeString("locales\\sv.pak");
        outPacket.encodeString("locales\\sw.pak");
        outPacket.encodeString("locales\\ta.pak");
        outPacket.encodeString("locales\\te.pak");
        outPacket.encodeString("locales\\th.pak");
        outPacket.encodeString("locales\\tr.pak");
        outPacket.encodeString("locales\\uk.pak");
        outPacket.encodeString("locales\\vi.pak");
        outPacket.encodeString("locales\\zh-CN.pak");
        outPacket.encodeString("locales\\zh-TW.pak");
        outPacket.encodeString("natives_blob.bin");
        outPacket.encodeString("nmcogame64.dll");
        outPacket.encodeString("nps64.dll");
        outPacket.encodeString("snapshot_blob.bin");
        outPacket.encodeString("swiftshader\\libEGL.dll");
        outPacket.encodeString("swiftshader\\libGLESv2.dll");
        outPacket.encodeString("v8_context_snapshot.bin");

        return outPacket;
    }

    /**
     * 發送檔案檢查結果封包
     *
     * @param result 檢查結果
     * @return 封包
     */
    public static OutPacket sendFileCheckResult(int result) {
        return new OutPacket(OutHeader.LP_FileCheckBad);
    }

    /**
     * 發送安全代碼封包 v267
     */
    public static OutPacket sendSecurityCodePacket() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SecurityCodePacket);
        outPacket.encodeByte(1);
        outPacket.encodeByte(0);
        return outPacket;
    }

    public static OutPacket sendSecurityCodePacket_Attach() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_CheckLogin);
        return outPacket;
    }

    public static OutPacket AliveReq() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_AliveReq);
        return outPacket;
    }


    /**
     * 發送未知封包
     *
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    public static OutPacket LP_CheckLogin() {
        return new OutPacket(OutHeader.LP_CheckLogin);
    }


    /**
     * 發送Hotfix的封包
     *
     * @param data  hotfix檔案
     * @param check 雜湊值(hash)
     * @return 封包
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    public static OutPacket sendHotfix(int size, int index, byte[] data, String check) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_ClientSocket_hotfix.getValue());
//        List<Integer> pks = new ArrayList<>();
//        if (data.length > 0 && check.length() >= 8) {
//            if (index == 0) {
//                int ret = size * 2;
//                byte[] bytes = ByteBuffer.allocate(4).putInt(ret).array();
//                int len = bytes.length - 1;
//                for (int i = 0; i <= len; i++) {
//                    if ((bytes[(i + 1) > len ? i : (i + 1)] & 0x7F) > 0) {
//                        int x = ((ret >> ((len - i) * 7)));
//                        ret -= (x & 0x7F) << ((len - i) * 7);
//                        if (x != 0) {
//                            pks.add(x);
//                        }
//                    }
//                }
//                for (int i = pks.size() - 1; 0 <= i; i--) {
//                    outPacket.encodeByte((i == 0) ? (pks.get(i)) : (pks.get(i) - 128));
//                }
//
//                outPacket.encodeArr(HexTool.getByteArrayFromHexString(check)); // file check (4 byte)
//            }
//            outPacket.encodeArr(data);
//        } else {
//            outPacket.encodeByte(0); // 0 = 不更新
//        }
        if (!check.isEmpty()) {
            if (index == 0) {
                outPacket.encodeZigZagVarints(size);
                outPacket.encodeArr(check); // file check (4 byte)
            }
            outPacket.encodeArr(data);
        } else {
            outPacket.encodeByte(0); // 0 = 不更新
        }

        return outPacket;
    }

    /**
     * 發送私人伺服器驗證封包
     *
     * @param pid 執行序id
     * @return 封包
     */
    public static OutPacket sendPrivateServerPacket(int pid) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_PrivateServerPacket);

        outPacket.encodeInt((pid ^ OutHeader.LP_PrivateServerPacket.getValue()));

        return outPacket;
    }

    /**
     * 發送選擇性別的封包
     *
     * @return 封包
     */
    public static OutPacket sendChooseGender() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_ChooseGender);
        outPacket.encodeByte(1);
        return outPacket;
    }

    /**
     * 發送選擇性別成功封包
     *
     * @return 封包
     */
    public static OutPacket sendSetGender(boolean success) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_GenderSet);
        outPacket.encodeByte(success);
        return outPacket;
    }

    /**
     * 發送登入遊戲帳密檢查結果的封包
     *
     * @param msg     檢查結果
     * @param client  客戶端
     * @param relogin 是否為重新登入
     * @return 封包
     */
    public static OutPacket sendCheckPasswordResult(LoginType msg, MapleClient client, boolean relogin) {
        OutPacket outPacket;
        if (relogin) {
            outPacket = new OutPacket(OutHeader.LP_AccountInfoResult.getValue());
        } else {
            outPacket = new OutPacket(OutHeader.LP_CheckPasswordResult.getValue());
        }
        outPacket.encodeByte(msg.getValue());
        outPacket.encodeString("");
        if (msg == LoginType.Success) {
            if (!relogin) {
                outPacket.encodeString(client.getAccountName());
                outPacket.encodeLong(client.getAccID());
            }
            outPacket.encodeInt(client.getAccID());
            /* 是否為管理員帳號
             * 效果1 - 不受地圖使用位移技能限制
             * 效果2 - 可以使用/前綴指令
             * 效果3 - 不受部分異常狀態/怪物BUFF影響
             * 效果4 - 無法丟棄道具
             * 效果5 - 上線提示「該帳號限制道具和楓幣移動，請至認證信箱了解詳情，並聯絡客服中心。」
             */
            outPacket.encodeInt(0);
            outPacket.encodeInt(client.getAccID());
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeByte(0);
            outPacket.encodeInt(255);
            outPacket.encodeInt(3);
            outPacket.encodeInt(0);
            outPacket.encodeShort(0);
            outPacket.encodeByte(0);
            outPacket.encodeString(client.getSecurityAccountName());

            outPacket.encodeInt(-16711680);
            outPacket.encodeInt(16777473);
            outPacket.encodeInt(16842753);
            outPacket.encodeInt(65792);
            outPacket.encodeInt(16777473);
            outPacket.encodeInt(16842753);
            outPacket.encodeInt(65792);
            outPacket.encodeInt(16777473);
            outPacket.encodeInt(16842753);
            outPacket.encodeInt(65792);
            outPacket.encodeInt(16777473);
            outPacket.encodeInt(16842753);
            outPacket.encodeInt(65536);
            outPacket.encodeInt(257);
            outPacket.encodeInt(16842753);
            outPacket.encodeInt(65792);
            outPacket.encodeInt(16777473);
            outPacket.encodeInt(16842753);
            outPacket.encodeInt(65792);
            outPacket.encodeInt(16777473);
            outPacket.encodeInt(16777217);
            outPacket.encodeInt(65792);
            outPacket.encodeInt(16777473);
            outPacket.encodeInt(16842753);
            outPacket.encodeInt(65792);
            outPacket.encodeInt(257);
            outPacket.encodeInt(-1);
            outPacket.encodeByte(0);
        } else if (msg == LoginType.Blocked) {
            outPacket.encodeString("");
            outPacket.encodeByte(0); // nReason // banType
            outPacket.encodeFT(PacketHelper.getTime(-2)); // Tempban 日期處理 -- 64位長, 100 ns的間隔從 1/1/1601.
            outPacket.encodeString("帳號已被封鎖"); // reason
        } else if (msg == LoginType.WaitOTP) {
            outPacket.encodeByte(1);
        } else {
            outPacket.encodeInt(0);
        }

        return outPacket;
    }

    /**
     * 發送世界資訊的封包
     *
     * @param world       世界物件
     * @param channelLoad 頻道負載資訊
     * @param stringInfos 頻道提示訊息
     * @return 封包
     */
    public static OutPacket sendWorldInformation(World world, Map<Integer, Integer> channelLoad, Set<Tuple<Position, String>> stringInfos) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_WorldInformation.getValue());

        int worldId = world == null || world.getWorldId() == null ? -1 : world.getWorldId().getVal();
        outPacket.encodeByte(worldId);
        if (worldId >= 0) {
            outPacket.encodeString(world.getName());
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeByte(0);
            outPacket.encodeByte(0); // boolean 雪吉拉 X 皮卡啾 伺服器
            outPacket.encodeByte(world.getWorldState()); // ServerConfig.LOGIN_SERVERSTATUS 伺服器狀態  0 無 1活動 2新的 3hot
            outPacket.encodeString(world.getWorldEventDescription()); // 伺服器公告牌信息

            int lastChannel = 1;
            Set<Integer> channels = channelLoad.keySet();
            for (int i = 30; i > 0; i--) {
                if (channels.contains(i)) {
                    lastChannel = i;
                    break;
                }
            }
            outPacket.encodeByte(lastChannel);
            int load;
            for (int i = 1; i <= lastChannel; i++) {
                outPacket.encodeString(world.getWorldId().name() + "-" + i); // 頻道名字 = 伺服器名字 - 頻道編號
                if (!channels.contains(i)) {
                    load = 1;
                } else {
                    /* 這裡5決定限制人數數量 */
                    load = Math.min(20 ,channelLoad.get(i));
                }
                outPacket.encodeInt(load + ServerConfig.LOGIN_DEFAULTUSERLIMIT); // 頻道連接人數
                outPacket.encodeByte(world.getWorldId().getVal()); // 伺服器ID
                outPacket.encodeByte(i - 1); // 頻道編號
                outPacket.encodeByte(0); // isAdultChannel
            }

            outPacket.encodeShort(stringInfos == null ? 0 : stringInfos.size());
            if (stringInfos != null) {
                for (Tuple<Position, String> stringInfo : stringInfos) {
                    outPacket.encodePosition(stringInfo.getLeft());
                    outPacket.encodeString(stringInfo.getRight());
                }
            }

            outPacket.encodeInt(0);

            boolean a2 = true;
            outPacket.encodeByte(a2);
            if (a2) {
                byte[] unk1 = new byte[]{
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        2
                };
                outPacket.encodeInt(unk1.length);
                for (int i = unk1.length; i > 0; i--) {
                    outPacket.encodeByte(i - 1); // index
                    outPacket.encodeByte(unk1[i - 1]);
                    outPacket.encodeByte(0);
                }
            }

        } else {
            outPacket.encodeByte(false);
            outPacket.encodeByte(false);
            outPacket.encodeInt(-1);
            outPacket.encodeInt(-1);
        }

        return outPacket;
    }

    /**
     * 發送世界資訊結束封包
     *
     * @return 封包
     */
    public static OutPacket sendWorldInformationEnd() {
        return sendWorldInformation(null, null, null);
    }

    /**
     * 發送世界狀態資訊封包。
     *
     * @param status 伺服器狀態。
     * @return 伺服器狀態數據包。
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    public static OutPacket sendWorldStatusResult(int serverId, int status) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_WorldStatusResult.getValue());
        /*
         * 可能的值 status:
         * 0 - 沒有消息
         * 1 - 當前世界連接數量較多，這可能會導致登錄遊戲時有些困難。
         * 2 - 當前世界上的連接已到達最高限制。請選擇別的伺服器進行遊戲或稍後再試。
         */
        outPacket.encodeByte(status);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeInt(serverId);
        outPacket.encodeInt(-1); // 0 - 燃燒伺服器提示; 1 - 皮卡啾伺服器提示

        return outPacket;
    }

    /**
     * 發送設定客戶端金鑰封包
     *
     * @return
     */
    public static OutPacket sendSetClientKey() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SetClientKey);
        outPacket.encodeLong(199);
        return outPacket;
    }


    /**
     * 如標題 頻道類別(世界ID)
     * 艾麗亞 0X00 = 0
     * 普麗特 0X01 = 1
     * 琉德 0X02 = 2
     * 優依娜 0X03 = 3
     * 愛麗西亞 0X04 = 4
     * 殺人鯨 0X06 = 6
     * Reboot 0X2D = 45
     *
     * @param world
     * @return
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    public static OutPacket sendSetPhysicalWorldID(int world) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SetPhysicalWorldID.getValue());
        outPacket.encodeInt(world); // V267 +
        outPacket.encodeInt(world);
        return outPacket;
    }

    public static OutPacket sendSetPhysicalWorldAttach(String Loca) {
        OutPacket say = new OutPacket(OutHeader.LP_VersionRegion.getValue());
        say.encodeString(Loca);
        return say;
    }
}
