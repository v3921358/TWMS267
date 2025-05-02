package Packet;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleEnumClass;
import Client.MaplePartTimeJob;
import Config.configs.ServerConfig;
import Config.constants.JobConstants;
import Config.constants.ServerConstants;
import Database.dao.AccountDao;
import Opcode.Headler.OutHeader;
import Server.ServerType;
import SwordieX.client.character.CharacterStat;
import SwordieX.client.character.avatar.AvatarLook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.StringUtil;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.List;

public class LoginPacket {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(LoginPacket.class);
    private CharacterStat characterStat;
    private AvatarLook avatarLook;
    private AvatarLook secondAvatarLook;

    /**
     * 發送一個hello packet
     *
     * @參數 mapleversion 客戶端版本
     * @參數 sendiv 發送
     * @參數 recviv 接收
     * @參數 testServer
     * @完畢
     */
    public static byte[] getHello(short mapleVersion, byte[] sendIv, byte[] recvIv, ServerType type) {
        MaplePacketLittleEndianWriter mplewHello = new MaplePacketLittleEndianWriter();

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

        mplewHello.writeShort(mapleVersion + (type == ServerType.ChatServer ? 159 : 0));
        if (type != ServerType.LoginServer) {
            mplewHello.writeInt(mapleVersion + (type == ServerType.ChatServer ? 159 : 0));
        } else {
            mplewHello.writeMapleAsciiString(ServerConfig.MapleMinor + (type == ServerType.LoginServer ? ":1" : ""));
        }
        mplewHello.write(recvIv);
        mplewHello.write(sendIv);
        mplewHello.write(mapleRegion);
        if (type != ServerType.LoginServer) {
            mplewHello.write(4);
        } else {
            int loginSrvMapleMinor = Integer.parseInt(String.valueOf(mapleVersion) + StringUtil.getLeftPaddedStr(ServerConfig.MapleMinor, '0', 2));
            mplewHello.write(0);
            mplewHello.writeShort(mapleVersion);
            mplewHello.writeShort(mapleVersion);
            mplewHello.writeShort(0);
            mplewHello.write(recvIv);
            mplewHello.write(sendIv);
            mplewHello.write(mapleRegion);
            mplewHello.writeMapleAsciiString(ServerConfig.MapleMinor + (type == ServerType.LoginServer ? ":1" : ""));
            mplewHello.writeInt(loginSrvMapleMinor);
            mplewHello.writeInt(loginSrvMapleMinor);
            mplewHello.writeInt(0);
            mplewHello.writeShort(1);
            mplewHello.write(5);
        }
        byte[] helloPacket = mplewHello.getPacket();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(helloPacket.length);
        mplew.write(helloPacket);
        return mplew.getPacket();
    }

    public static byte[] addConnection() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopBuyCharSlot.getValue());
        mplew.write(1);

        return mplew.getPacket();
    }

    /**
     * 發送一個登錄失敗的數據包。
     *
     * @param reason 登陸結果
     * @return 登陸反饋數據包
     * @參數 reason 測井在失敗的原因。
     * @返回 登錄失敗的數據包。
     */
    public static byte[] getLoginFailed(MapleEnumClass.AuthReply reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        mplew.writeShort(OutHeader.LP_CheckPasswordResult.getValue());
        mplew.write(reason.getCode());
        mplew.writeMapleAsciiString("");
        if (reason.getCode() == 84) {
            mplew.writeLong(PacketHelper.getTime(-2));
        } else if (reason.is(MapleEnumClass.AuthReply.GAME_CONNECTING_ACCOUNT)) { //prolly this
            mplew.writeZeroBytes(5);
        }
        return mplew.getPacket();
    }

    public static byte[] getLoginFailedBan(long timestampTill, int banType, String reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CheckPasswordResult.getValue());
        mplew.write(MapleEnumClass.AuthReply.GAME_ACCOUNT_BANNED.getCode());
        mplew.writeMapleAsciiString("");
        mplew.write(banType);
        mplew.writeLong(PacketHelper.getTime(timestampTill)); // Tempban 日期處理 -- 64位長, 100 ns的間隔從 1/1/1601.
        mplew.writeMapleAsciiString(reason);

        return mplew.getPacket();
    }

    public static final byte[] deleteCharResponse(int cid, int state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DeleteCharacterResult.getValue());
        mplew.writeInt(cid);
        mplew.write(state);
        mplew.write(false);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] deleteReservedCharResponse(int chrId, int state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReservedDeleteCharacterResult.getValue());
        mplew.writeInt(chrId);
        mplew.write(state);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

        return mplew.getPacket();
    }

    public static byte[] ReservedDeleteCharacterCancelResult(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReservedDeleteCharacterCancelResult.getValue());
        mplew.writeInt(chrId);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] secondPwError(byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        /*
         * 14 - Invalid password
         * 15 - Second password is incorrect
         */
        mplew.writeShort(OutHeader.SECONDPW_ERROR.getValue());
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] getCharList(List<MapleCharacter> chars, int charslots, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SelectWorldResult.getValue());
        if(chars.isEmpty()){
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
            mplew.writeInt(c.getWorldId()); // 0 = 艾麗亞
            mplew.writeInt(c.getChannel() - 1);
            mplew.writeBool(false);
            mplew.writeInt(2);
            mplew.writeInt(2);
            mplew.writeInt(2);
            mplew.writeInt(48); // can creat
            mplew.writeZeroBytes(13);
            mplew.write(1);
            mplew.writeInt(53); // all slot
            mplew.writeInt(0);
            mplew.writeInt(-1);
            mplew.writeZeroBytes(41);
            mplew.writeLong(199); // test key then change
        } else {
            int v6 = 0;
            mplew.write(v6);
            mplew.writeMapleAsciiString("");
            if (v6 == 61) {
                mplew.write(0);
                // recv 148
            }
            if (v6 == 131) {
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
            mplew.writeInt(c.getWorldId());
            mplew.writeInt(c.getChannel() - 1);
            mplew.write(false); // if ture client send 222
            mplew.writeInt(1);  // 具有角色就會是1 否則 2
            mplew.writeInt(1);  // 具有角色就會是1 否則 2
            mplew.writeInt(1);  // 具有角色就會是1 否則 2
            mplew.writeInt(charslots);
            mplew.write(0);
            mplew.write(0);

            List<Pair<Integer, Long>> deleteChrs = AccountDao.getPendingDeleteChrId(c.getAccID(), c.getWorldId());
            mplew.writeInt(deleteChrs.size());
            for (Pair<Integer, Long> delChr : deleteChrs) {
                mplew.writeInt(delChr.getLeft());
                long delTime = delChr.getRight();
                if (c.isGm()) {
                    delTime -= 2 * 24 * 60 * 60 * 1000L;
                }
                mplew.writeLong(PacketHelper.getTime(delTime));//刪除角色的時間，兩天後可以徹底刪除
            }

            // 排序的角色個數
            mplew.writeInt(chars.size());
            // 排序的角色ID
            chars.forEach(chr -> mplew.writeInt(chr.getId()));
            // 角色外觀個數
            mplew.write(chars.size());
            // 角色外觀訊息
            chars.forEach(chr -> chr.getAvatarData().encode(mplew));
            mplew.write(0);
            mplew.write(0);
            mplew.write(1);
            mplew.writeInt(charslots); //帳號當前可創建角色的總數
            mplew.writeInt(0); // 50級角色卡角色數量
            mplew.writeInt(-1);
            boolean fireAndice = false; // 變更角色名稱開關(在角色上方的)
            mplew.write(fireAndice); // 變更角色名稱開關(在角色上方的)
            if (fireAndice) {
                mplew.writeLong(130977216000000000L); // 開始
                mplew.writeLong(130990175990000000L); // 結束
                int c_size = 0;
                mplew.writeInt(c_size);
                if (c_size > 0) {
                    mplew.writeInt(0); // 無法更名的角色ID
                }
            }
            mplew.write(0); // nRenameCount
            mplew.write(0);

            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);

            mplew.writeInt(0);
            mplew.writeInt(0);

            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(false);
            mplew.write(false);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeLong(199); // test key then change
        }
        return mplew.getPacket();
    }


    public static final byte[] createCharResponse(int state, byte[] file) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Creat_New_Char_Check_Spw.getValue());
        mplew.write(state);
        switch (state) {
            case 0:
                mplew.write(1);
                mplew.write(JobConstants.JOB_ORDER);
                // 職業開放狀態
                for (int i = 0; i < ServerConstants.JOB_NAMELIST.length; i++) {
                    mplew.write(ServerConfig.WORLD_CLOSEJOBS.contains(ServerConstants.JOB_NAMELIST[i]) ? 0 : 1);
                    mplew.writeShort(1);
                }
                break;
            case 69: // 圖片識別驗證
                mplew.write(0);
                mplew.write(1);
                mplew.write(1);
                mplew.write(0);
                mplew.writeHexString("DE 1C 00 00 FF D8 FF E0 00 10 4A 46 49 46 00 01 01 00 00 01 00 01 00 00 FF DB 00 43 00 08 06 06 07 06 05 08 07 07 07 09 09 08 0A 0C 14 0D 0C 0B 0B 0C 19 12 13 0F 14 1D 1A 1F 1E 1D 1A 1C 1C 20 24 2E 27 20 22 2C 23 1C 1C 28 37 29 2C 30 31 34 34 34 1F 27 39 3D 38 32 3C 2E 33 34 32 FF DB 00 43 01 09 09 09 0C 0B 0C 18 0D 0D 18 32 21 1C 21 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 FF C0 00 11 08 00 40 00 D8 03 01 22 00 02 11 01 03 11 01 FF C4 00 1F 00 00 01 05 01 01 01 01 01 01 00 00 00 00 00 00 00 00 01 02 03 04 05 06 07 08 09 0A 0B FF C4 00 B5 10 00 02 01 03 03 02 04 03 05 05 04 04 00 00 01 7D 01 02 03 00 04 11 05 12 21 31 41 06 13 51 61 07 22 71 14 32 81 91 A1 08 23 42 B1 C1 15 52 D1 F0 24 33 62 72 82 09 0A 16 17 18 19 1A 25 26 27 28 29 2A 34 35 36 37 38 39 3A 43 44 45 46 47 48 49 4A 53 54 55 56 57 58 59 5A 63 64 65 66 67 68 69 6A 73 74 75 76 77 78 79 7A 83 84 85 86 87 88 89 8A 92 93 94 95 96 97 98 99 9A A2 A3 A4 A5 A6 A7 A8 A9 AA B2 B3 B4 B5 B6 B7 B8 B9 BA C2 C3 C4 C5 C6 C7 C8 C9 CA D2 D3 D4 D5 D6 D7 D8 D9 DA E1 E2 E3 E4 E5 E6 E7 E8 E9 EA F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FF C4 00 1F 01 00 03 01 01 01 01 01 01 01 01 01 00 00 00 00 00 00 01 02 03 04 05 06 07 08 09 0A 0B FF C4 00 B5 11 00 02 01 02 04 04 03 04 07 05 04 04 00 01 02 77 00 01 02 03 11 04 05 21 31 06 12 41 51 07 61 71 13 22 32 81 08 14 42 91 A1 B1 C1 09 23 33 52 F0 15 62 72 D1 0A 16 24 34 E1 25 F1 17 18 19 1A 26 27 28 29 2A 35 36 37 38 39 3A 43 44 45 46 47 48 49 4A 53 54 55 56 57 58 59 5A 63 64 65 66 67 68 69 6A 73 74 75 76 77 78 79 7A 82 83 84 85 86 87 88 89 8A 92 93 94 95 96 97 98 99 9A A2 A3 A4 A5 A6 A7 A8 A9 AA B2 B3 B4 B5 B6 B7 B8 B9 BA C2 C3 C4 C5 C6 C7 C8 C9 CA D2 D3 D4 D5 D6 D7 D8 D9 DA E2 E3 E4 E5 E6 E7 E8 E9 EA F2 F3 F4 F5 F6 F7 F8 F9 FA FF DA 00 0C 03 01 00 02 11 03 11 00 3F 00 F6 1B 8B B8 AE 2E 2C AD A5 8A 49 4C CC B7 11 02 A4 18 8A B2 B1 3B E3 C8 20 79 88 38 F9 4E 18 33 1C D5 F9 2E 0B DC 49 0A 33 05 0B B0 C9 0E 1C C7 21 C7 0C 30 70 70 CA 47 51 82 49 00 01 97 23 3B EA 12 15 9D BC A4 5F 2D A1 78 88 F9 B8 60 C8 DC 64 61 B0 7E F0 C8 00 15 21 81 59 1D 1A F6 08 DE 25 6D BB 99 5D D4 E5 5C 00 3E 5E 31 CA B3 73 90 78 3C 1E 71 4E D7 D7 FA FE B4 13 B7 51 7F D2 96 F3 85 46 B6 20 65 9A 4C 32 9F 9B 38 50 BC FF 00 00 FB DE BD 31 F3 36 13 2C 28 0C DF 38 2A 80 BA 07 CB 39 27 3F 21 CE D5 C9 1D CE 01 39 C0 5C D3 0C 91 5B 5E C7 19 53 12 BA A4 71 03 20 0B 23 7C E7 6A 2E EE AA A1 98 FC BC 8C 60 9D B8 0B 65 24 17 52 4F 72 90 BC 53 07 36 F2 09 06 1B 11 B3 6D E3 3D 0E 4B 0F 50 E0 F7 A5 CB D5 87 A1 36 C0 F3 EF 57 95 4A 31 DC 39 DA D9 51 C7 3C 63 A1 C8 EE 0F 3D 41 63 DC 18 18 2C C1 0B B0 0B 10 0E 15 A7 70 A5 8A AA 93 8E 80 9E BD 8E 78 19 35 E6 75 B4 88 C7 3D C4 8F FB CD D1 86 62 1D C0 25 F6 AE C5 CB 10 AA C3 68 DC 58 28 CE 72 49 92 E6 E1 32 B0 49 1C A6 43 2C 65 56 27 2B BB E7 E0 86 25 41 C0 52 CC A0 9F 94 10 41 04 03 28 71 D5 EB D0 2D D5 C4 05 44 E4 5F 1B 75 DE 26 3B B6 93 BB 0C 50 36 07 CD BB EE 91 9C 63 38 03 0E 9E F2 D9 2F 61 B6 7B B1 14 D8 32 79 7C 61 97 0D C3 12 38 E8 48 E4 13 B1 B1 90 AD 48 F3 2C 53 33 48 3C 80 AF BA 47 2A 4A 32 ED 6F 9B 70 E0 70 39 2D D3 18 EE A6 B3 EC 56 ED D4 5D 3A AC 12 EA 6C C5 DA 47 D9 35 BA 6D 3E 52 22 32 90 58 01 96 52 70 18 C8 46 41 C5 1B 02 51 B5 DE C5 A9 81 47 DE 97 02 CA 5B 9B 84 50 8E 88 DE 69 46 CB 60 0E 49 78 D0 8C 92 76 AA 83 81 B4 8A BA CA 65 B5 64 8D A4 B7 2C 85 55 94 2E E8 CE 31 90 08 23 23 E8 47 D4 53 16 1C B3 A1 77 F2 83 06 03 2E 18 36 E2 C7 E6 CF 2B C8 18 1C 00 08 E4 70 1A F0 4E D3 92 93 18 C3 3A B1 64 4F E0 5C 7C A7 24 8C 93 9C B0 1F 77 8E 08 0D 4F AA 10 B0 44 8F 68 F1 FD 95 ED 56 4C 96 40 42 30 2F F3 31 CA 1E 0E 58 E4 83 D7 27 3D CD 5D D6 F7 B3 A3 5D 09 50 17 43 6A B2 83 16 E3 B4 3E 40 CE 4B 7C AD 90 40 20 29 1B 70 49 66 CC A9 63 0C 91 B9 0C 26 7F 32 E2 79 AD B7 89 17 72 29 DF B0 01 F7 08 5D CD D0 28 24 30 53 57 63 FB 41 82 06 91 63 69 42 AF 98 0A ED F9 B8 C9 1C B6 30 37 71 93 E9 9E F4 3D 56 A8 6D 6B 72 35 1F BA 55 10 DC 30 69 B6 9C 4A C0 A8 53 80 C4 B1 07 6F CA 32 06 73 9F E2 04 92 D9 AD E5 49 1E 63 7D 76 51 CA AA C4 BE 50 54 25 87 20 95 07 EA 09 39 1D 89 A9 2F 25 30 D9 30 68 E4 9E 47 1B 04 50 38 8D E4 38 E7 69 66 18 38 C9 FB DD A9 C9 71 29 32 86 B4 98 18 D7 20 92 98 90 E5 86 17 E6 EB F2 83 CE 38 65 EF 90 06 93 5A 89 EA F5 1B 2E A1 6F 0B 91 2B F9 61 64 31 B3 4B FB B5 C8 8F CC 24 16 C6 E1 B4 75 5C E3 07 FB AD 8A 06 73 77 63 76 6F 74 BB A8 85 B4 EC AB 1B 2A CC F3 0F 54 1F 30 DA CA DB 7B 63 73 0E 31 9A BB 65 B1 9E 66 F9 1E 58 C8 81 E6 38 F3 1C 28 CF CE 36 8C 1C B3 10 07 18 60 47 0D 50 FD B9 ED 6D D6 EE F1 D8 C2 B6 F9 B8 74 51 B2 27 52 03 7C AB B9 B9 CB 67 E6 60 A1 3A F5 26 BD D7 D0 1A DE E5 99 1A E6 29 A2 11 43 E7 A4 8C 44 8E D2 05 F2 C6 72 38 C7 23 19 1C 73 9D A0 F0 4B 2A 45 0C 13 96 9D E2 B6 79 49 08 F2 20 0D 9F 2D DB 68 27 1F C2 D9 38 FE 12 4F D6 A3 6F B4 CB 74 4C 6C C0 40 16 36 19 28 AE 49 56 73 82 A7 38 50 30 41 39 2C CA 70 46 43 64 94 C2 64 96 49 DD A0 45 10 BC 10 A3 4A EA C5 86 D6 05 46 F0 4A B0 DD 9C E3 83 90 01 2C 97 61 D9 EC 24 67 13 3D D4 6E 24 92 78 9A 58 60 95 59 24 DB B5 06 06 E3 95 19 03 23 68 E5 86 70 47 2A 2E EF D6 E0 C6 DA 63 BC 22 55 45 94 4C 9B 8A 1C 82 E5 78 C6 08 07 03 3F 2B 03 D7 2A 26 D3 66 6B 8D 36 DE 57 59 81 64 1C CC 63 2E DE 8C 4C 64 A7 3D 7E 5E 39 ED D2 A3 61 24 CE FE 4C F7 11 89 8B A7 98 61 50 62 DA 0A FC B9 1F DE F9 81 60 CA 71 E8 C2 93 56 76 D8 4E CB 6D 49 8C 10 DD 7E F2 41 1C E8 C9 85 05 55 97 19 CF 1F 5F 97 BE 3E 51 4C 46 69 6D 61 95 D5 F1 2B 2B B0 1E 62 15 CE 0A 8D B8 DD FD D0 41 00 75 24 0E 45 56 B0 42 6E AE EE 1E 58 AE 1D 26 68 91 21 8D 40 81 49 04 8D C7 92 4F CA CD CF 51 80 32 39 B1 1C CF 24 05 E5 B7 44 BF 8E 22 5A 14 75 72 B9 CF 43 C6 55 8A F0 4E DC E3 9C 10 40 4A CD 5C 5E EB 5C D0 DD FF 00 5F D5 C6 4D 73 77 65 6C F2 CF 0F DA 4A 27 09 6B 1B 6F 90 AA 16 38 07 20 12 46 02 93 8E 9F 31 27 15 13 DC 1B 9B 71 0C 52 CA 2E 92 55 96 48 4C A8 92 A8 05 24 31 F0 0A 9E 19 14 F3 82 1B EF 73 9A B3 2A 4C F3 48 E4 44 8A AA 3E CE F2 E1 B6 4B F3 A9 3B 40 1D 8A FF 00 16 4E 48 C2 F7 8E E7 33 6D 90 5D 30 81 65 F9 9A 21 1E D4 08 43 12 E5 F3 D0 A3 2F CB C8 DF D0 11 B8 55 F4 2A DD 53 D4 66 96 F3 DB 59 C1 06 A2 56 19 9D CC 71 23 CF E6 13 8D C4 28 73 83 21 DA A5 B2 40 6D BD 72 43 1A 96 18 AF 61 96 28 C4 91 BD B2 EE DC D2 12 D2 75 3B 40 3C 67 82 39 3C 8D B8 3B 8B 6E 0F DF 24 AF 77 1C D1 C9 14 69 F2 A1 57 C7 9A 85 41 DC 08 C1 52 0E E1 D7 3C 67 B8 A0 21 FB 5B DD A2 E4 32 AC 25 44 41 5C E1 CF 2C CD C9 51 92 40 1E AC 46 ED C3 02 DF 51 DB B9 51 A6 81 64 B3 B4 B2 8D 3F 72 A4 C7 0C 32 EC D8 14 F9 47 08 08 57 54 DC 77 2E 7E 5C 2E 01 3B 68 A9 EE A7 82 74 55 5B B2 83 CD 50 BE 53 85 32 48 8F 9F 2C 36 70 79 46 56 5E B8 C8 38 A2 A1 C9 2D 1F F5 F8 14 96 9A 5B FA F5 26 78 CD D0 60 64 75 8B 2B B7 66 E8 DC 32 B1 39 CE 79 07 03 8C 60 8C E7 70 6C 53 53 ED 13 5D 86 96 18 D2 08 CB 6C 0C 37 3B 1E 00 60 41 C2 F1 BF 8C 12 43 2F 23 95 A6 B0 76 BE 67 4B 78 44 C9 B5 7C C6 53 96 88 F5 01 B1 C1 0C 09 DA 33 D0 67 1B 81 1C E7 8E 35 C9 74 8F 0B F9 F0 DA 9F ED 0D 44 25 AC 76 97 01 65 1B 98 1C A9 40 FB 49 E4 82 46 E0 4E 33 B8 55 D9 BB 19 36 B7 3A 1B 38 16 37 CC 31 AC 76 F1 0F 22 18 A3 88 C4 15 17 0B 82 A7 A8 05 49 52 30 30 DC 0E E6 4D 8D 07 9F F6 7B 76 79 36 17 57 92 4E 24 62 58 EC DC 49 60 01 3E 98 01 86 3A 60 71 97 7E 2B D7 74 38 F4 F4 D5 74 DD 3D B5 3D 45 92 DD 62 8A FD C9 47 62 DB 09 8B 61 01 41 3B 49 0C 49 C7 53 81 5D 89 98 43 BA 7D D1 AC 12 29 99 DA 66 F2 CA 00 14 74 DB 9C 01 92 4B 1C 83 81 D0 F0 72 B5 AF 9F E2 1C DC CF 5D C6 5A DA 5B 19 EE 24 67 17 17 3E 69 59 5D CA 96 50 1B 7A 21 00 00 02 86 1B 46 32 33 9C 92 C5 8C B2 38 B7 77 0A FB EE 1A 37 74 59 64 DA A4 29 F6 1C 01 BC 0C 80 4E 31 9C E0 52 93 1A C6 6E 21 F3 27 6F 28 32 22 4B 9F 30 00 71 8C B0 5C 9D DD 4F 5E 32 78 18 E0 FC 6D E2 0C 5C 41 E1 B9 EF 0C 6B 75 18 9B 52 36 D1 BC B2 5B C0 32 64 00 A8 C9 0C 08 00 ED 04 2A 92 73 91 88 9C 94 55 D1 76 D5 9B AD 7F 6B E2 01 1D 96 95 71 A7 5C 79 B2 AC DA 8B 5A CC 26 8F CB 52 A0 A3 32 FF 00 13 80 06 18 61 95 5C 76 AD 6F 28 4B 68 6E E5 86 58 A7 92 04 5B 8D 80 A3 B2 00 4E DF 94 B3 02 0B B1 01 4E 73 D0 F7 AE 1F C3 7E 16 F0 47 88 B4 A9 20 1A 45 B3 DC C3 28 17 70 B0 92 19 AD 8E 0E D8 CF 46 3B 46 10 9E 03 95 2C 49 6C D7 61 0C 9A 37 86 74 4B 18 E6 D4 A2 82 C1 02 45 6F 35 C4 EA AA DF 27 1F 37 00 E4 02 DC F7 FC 29 C7 99 D8 99 ED CA BF AF EB FA DC D4 48 7C A6 46 00 49 21 55 49 26 6C 07 60 01 C6 70 39 E4 9E 38 1C 9C 7A 53 2D 41 74 37 52 5A 88 6E 64 5D AC 1C 28 7D A0 B1 55 62 A5 81 C6 E3 DC F5 3D 33 8A C3 1E 33 D0 44 E1 13 5F B3 95 8D C7 94 63 8F 12 B4 87 CA DD B6 20 87 3C 9C 7F 78 96 CA 0E 4A E1 91 F8 96 C2 14 58 E1 D3 35 0B 58 62 B6 0B 03 7F 63 5D 7E EB D5 0A 08 80 0A 00 4F BA D8 38 23 8C 02 6E 51 6B 7D 02 EB A1 D0 7D F9 A5 68 59 D6 45 64 8D FC C5 7D 84 0F 98 ED 04 80 72 18 8D C3 BF 07 3B 71 4A EB 30 79 0C 4B 12 A9 1B B7 63 2C ED 82 39 1C 74 C2 F3 9E 79 1C 70 6B 05 35 99 04 06 51 A1 EA B7 D6 D3 4C 25 8A E1 E5 B4 D8 E1 98 79 65 03 4A B8 1F 77 68 20 1E 84 FC C4 92 AD 75 AA 31 B6 92 DF C3 B2 C6 6D 87 96 BF DA 57 F8 3F 36 14 10 22 F3 B7 1E 39 67 C1 00 9E 48 2D 44 A3 26 AC 3E 5B E8 6F 40 64 C0 0E F9 FD DA E5 5C 0D E0 F3 92 C4 1C 73 EC 31 C1 EB DA AA 47 6B 15 BD BC 51 AA 3D 83 98 C5 B4 70 C4 CE 14 82 58 1C 8C 80 9C 2E 38 01 71 D7 90 07 15 AE F8 E2 FB 41 8E CA C7 FB 3F 4F B3 6B D0 C2 39 2D AE 3C E3 6D C0 6C BC 32 2C 3B 78 61 F7 99 40 E4 9E 98 33 DB 6B AF 75 03 DB 3F 88 F4 00 27 00 1B 77 D1 A6 54 94 48 C4 33 20 69 87 98 8C C4 E5 C6 57 B9 EE 68 51 6D 79 02 4D 59 B3 B2 30 08 82 6E 82 4B B9 56 20 5A 72 23 0D 21 4F BA 1B EE 8C 92 C4 8E 02 83 9F BB C5 48 92 40 D7 23 70 89 2E 9D 5C 2A 9E 1D 91 5B 04 8C 80 48 C9 5F 6F 98 75 C8 27 0A 0F 0E 4A 6E E4 B9 4F 11 5E 47 76 A1 92 51 69 0D B2 22 33 91 23 00 A6 26 23 93 B8 6E 2C 40 62 72 49 24 B7 FB 12 D1 AE 4C F7 F7 9A DD C4 8E 91 C7 F6 C6 BA 6B 5C 82 D8 48 CA C0 63 CE 0B 93 92 87 1B 88 2D C6 02 77 6F DE 62 51 D6 DD 4C BF 15 78 BF 57 F0 D6 A1 6D 6C B6 96 F3 C1 A9 5D 79 36 D7 7E 66 F1 09 1B 41 43 16 13 9C E7 AC 9D 72 49 03 E5 1D 4C AA CD 71 6C 0C 12 AB BC C9 21 95 BE 61 1B 6C 6C 85 1F 38 5E 17 69 FB A3 12 1C 31 63 CF 9E 7C 49 F0 74 31 78 2E FA EA 16 BE 9E E2 D2 55 BA 12 5C 5E 4B 2A 32 97 20 AE D7 76 19 54 DA 33 81 90 07 3D 45 6F 78 73 C4 3A 7E AB A2 5B E9 DA 6D BF D9 56 5B 52 EB 1D 84 7B 31 BA 32 58 2F DD 58 DC 39 6E 79 5E 9C E5 B0 08 35 CD 25 7F EA DF F0 E6 B2 85 E2 B9 4E B4 16 B7 3B 72 A2 08 62 62 63 8E DD BA 67 E4 0B 83 D8 02 0A 80 49 C8 3C 74 32 5C 32 5B DB 3C A6 68 ED E3 8F F7 92 48 F8 0A AA 0E 58 9C F0 38 CF 3D BA D5 7B DB 95 86 F7 4F 84 A3 B4 93 4C C1 08 90 A2 8C 23 13 9E CD C6 70 9C 93 F7 B1 85 2C B0 58 5C 20 B8 8E 1B 9B D7 96 FB CB 5F 32 27 92 35 31 33 22 9D A5 10 F5 3B 1D 87 DE C6 1F 0D 8C 0A 68 C9 2D 76 26 D4 3C BD E8 AD 68 D2 06 68 99 A5 58 83 80 56 55 2A 08 C1 39 C9 2C 08 18 5C 12 4A 70 6A 3B 4D F7 32 DB 5D 2C 8B 14 66 13 B6 DC C3 24 4D 9C 8C 92 AC 57 20 00 00 DC 99 07 9E 33 8A B0 A1 9A 56 9E 3B 21 15 C3 9F 2D E4 90 2E 4C 68 E7 19 2A 49 20 86 76 51 DB 3C ED 24 8A 85 2D AE 15 04 0E 91 CD 66 DB C4 C9 70 E6 49 24 0C 33 9C 9F 94 7C C5 97 67 DD C1 18 2A 06 D2 9A 8B D5 FF 00 5E 82 24 64 6B 6B B6 99 E5 2D 6E DF 36 5E 53 98 DB 80 A8 AA 06 18 1D CC 79 39 CE 00 C8 C0 56 5D C6 97 33 A5 B0 DD 14 E0 99 96 E2 15 8D 9A 13 F7 41 F9 81 C1 65 DE B9 DA 46 03 0C 83 8C CA EC E1 52 08 A6 DF 71 16 C2 E5 F0 49 5E E4 A8 2B F7 80 60 0F 40 DC E0 81 8A 5F 2D 21 B8 92 EA EA 58 49 2E 12 07 64 0A 62 57 D8 3C BD DD F7 3A 83 DB 24 A8 C7 02 85 6D 90 F4 5A 8C 92 DD E5 8E 6B 74 89 16 17 97 0E 25 45 28 E8 40 2F 85 1D 77 12 C0 EE C1 C9 63 C8 C0 2D 82 47 22 11 7B 06 26 37 32 88 B7 3A 31 51 99 36 30 E9 D5 07 40 09 00 E0 E7 0C 6A E4 DB 84 4C 53 79 65 F9 80 4C 65 B1 CE 39 E3 9E 9F 8F 51 D6 A1 8D 64 17 2F 24 65 9A 29 5F E7 59 19 86 C2 01 04 A8 23 A1 C2 F0 30 3A B7 53 C9 7B 2B 0E ED 2B A1 96 D7 76 F7 3A 7C 7A 82 47 F2 98 CB E1 36 C8 CB DD 97 E4 2C 09 C8 C1 0A 4E 48 E3 34 53 B6 47 BE 64 08 6D 9A 59 86 5C 6D 06 72 15 4E 47 52 7E 55 DB CE 0E 14 E3 80 0D 14 36 93 D2 37 F9 DB F4 66 72 E7 E8 39 44 D0 4A 91 2A 17 B7 DA A8 A7 76 59 48 0D 92 E5 8E 48 E1 47 19 39 3C F1 C8 E2 4D 96 AB A9 78 DD 75 7D 56 C9 26 87 4C 46 5B 6B 2B 1B A4 91 A2 90 91 F3 49 B8 A7 2C BF 30 18 E9 8C E0 8C 1E C6 EA 1B 19 A4 92 1B 88 23 67 B8 55 89 C3 2E 3C E5 F9 88 42 4F DE 00 6F 25 79 E0 9C 8C 1E 72 EE 34 0F 0F 59 DB CD 3C 3A 62 D9 18 48 12 49 60 0D A3 91 C1 23 7A 14 2C BC 82 46 48 C8 E8 48 C5 34 EC CA 9A BA D5 68 73 1E 1E D3 2E B5 EF 1D DC EA FA B3 EA 49 FD 94 04 56 51 DD C0 22 79 10 EE F9 DB 08 A0 8C E7 1B 70 78 19 F7 E8 EE AD 75 DB 82 D6 57 9A BD 88 B5 91 42 CE D0 69 A6 36 DA D9 01 55 A5 92 48 D8 96 01 4A 95 3C 1E C4 AE 79 BD 23 45 BD D5 AF E7 BE 86 FA 74 86 36 01 16 E6 43 30 4E 47 47 6C B9 20 64 F2 C7 B0 E3 8C 5C D1 6F 75 29 B5 A3 04 17 D7 37 11 10 EA 1D F7 15 FB A4 82 72 1B 67 20 72 47 B7 39 C1 D7 97 F2 30 85 5E 56 E4 96 EC B5 AC 1B FD 2F C3 97 77 DF F0 93 EA 17 77 16 56 82 76 48 12 D0 79 A0 E4 87 20 C2 76 A9 21 B1 D7 85 EE 73 5C CD 9A DB 1D 17 4F D5 7C 4D 2D D4 05 22 37 B7 06 3D 56 E5 82 44 E0 F9 28 BB A5 CE 59 A3 0E 36 96 21 A3 1D 33 F2 F7 DE 22 9F 59 B3 B2 B8 BC D3 6E AC 15 62 84 95 B7 B8 B4 79 1A 59 79 DA A1 96 45 FB C4 AA 81 B4 9C 9E F9 C5 70 BA 87 85 B5 3B 5D 39 D5 BC 21 A1 DC 5C 6E 33 CD 73 A7 2A 87 C6 49 02 34 78 D7 1C 05 18 0E 4F CB 9E 4B 73 C9 52 53 8D DA EA 76 47 64 DF F5 FD 7E A7 2F E1 39 02 6A 97 BE 20 BC D2 A0 B9 FB 54 C1 E0 5B E0 D3 BC 4A 0E 41 0E E4 9D DD 3E 63 93 C5 77 FA 1E B7 A3 5A 5F 31 B0 F0 F4 36 2F 20 01 85 8D B2 33 3A 80 49 FB A1 4F A1 FE 2E 9C 0C 9A C9 D1 B5 BB DD 05 51 A5 D2 04 12 49 F3 15 BB 88 A4 DB 09 1C 67 3C 03 B7 FC 7A 57 72 4D A5 F5 8C 1A B4 0A F6 D7 13 98 D0 4F 10 8C 48 37 3A A9 04 B8 DA 7A 63 B9 C6 42 F2 45 45 0E 67 A2 7B 74 14 B5 F5 66 89 58 E4 F9 E7 B7 93 E6 11 1D AF F3 80 C1 B2 3E 50 48 0C A7 04 B0 F6 39 38 E1 96 FA 8A 4D 73 24 4E 12 22 AE B1 05 79 06 F3 29 4F 30 A6 07 1C 29 07 82 73 F3 76 19 33 FD 9D 42 2C 2A 02 40 81 36 24 59 4D BB 4E 71 90 7A 70 06 3A 63 20 E4 1C 54 12 35 EA BC 26 28 43 01 30 49 8B BE 4B 45 83 F3 28 C8 19 C9 5C E7 1C 06 C6 70 B9 E8 5E 64 3E 85 46 B9 49 F5 39 2E 2C 4C 57 26 14 6B 67 91 25 32 2C 72 87 5C C4 EA A4 EC 3C 8C 9D A7 00 65 B0 14 06 AB AA EB BF D9 33 1B A9 ED EF 67 8A 08 A5 2D F6 78 5C 82 32 D8 CA 00 4E E1 E5 80 1D B6 A1 0C C4 1E D5 7A 6B 94 B7 9E 3B 79 EE 2E E4 1F 65 66 CA 6C 26 61 B9 41 60 A8 3C CD CB 91 CA 80 A3 7F 3C E3 12 DA 14 BB B3 95 24 1E 75 AB 6F 8D 84 FC 9C 86 65 74 61 8C 15 18 C6 72 73 CF 27 A9 1E E2 8C E3 B3 47 9F 78 3B 4D B7 D7 DA F7 C6 DA CD CD 89 BC BA 94 24 4A 5C 32 D9 C6 0A FC B9 04 6C 97 6E 30 73 95 C8 3D 49 15 DB DC 69 D6 92 DE DB EA B6 96 56 97 37 F8 65 4B D6 45 2D 1A 30 20 10 46 37 00 48 18 DC 0E DD D8 24 F0 78 CB BC EB BA E3 C3 A7 C7 32 8B 89 03 BF 9D 26 F2 0E 00 39 39 20 2A F3 80 09 03 2D 8F BD 5B 76 BE 1B D4 D2 4B 78 63 BF 9A 2B 20 BB A5 18 11 92 49 E5 70 A4 EE 38 07 24 F1 F7 71 B8 67 1A BD 95 F4 FE BF AF BC DE 51 49 EA CE 9C 2B 44 6C E0 01 9C A8 F9 9C BB 8E 02 E3 39 E7 71 C9 1C 31 F5 39 25 6A 58 5A 1C CB 1C 52 07 31 B9 12 0D FB 8A 31 F9 B0 72 78 E1 81 03 B0 23 1C 62 A3 86 63 2A 24 DB 67 8B 1B D3 C8 90 00 58 83 8C FF 00 E3 A7 18 38 20 E7 9E 31 53 67 9B 7B 0A 7F 66 7E EE 64 59 AE 3C E6 F9 61 65 3B 93 0A 32 AD 26 EE 49 53 C6 D0 49 FB 99 CB 4D 59 96 BD 4A DA E6 93 05 F5 A4 CD 79 21 75 16 53 43 28 86 06 69 58 48 00 E3 6E 58 A8 C3 7C 98 6C 9D A7 AA 83 5E 37 E1 14 D5 F4 77 B4 B2 B9 D2 EE 23 9D 65 DA A6 69 4D B0 0A C7 B9 1F BC 4E A7 A2 9F A6 2B DB B5 19 27 97 CF B3 B5 BA 91 2E 4C 71 B2 0B 65 4F 32 2C BE 0B 33 38 65 0A 7D D7 38 57 DB B8 F0 3C F3 C6 9E 1E B7 87 50 8A 79 AE 26 BF 2E 36 4C F3 CA 08 69 07 38 68 D4 08 C7 CA CA 46 14 67 AF 5E 4E 55 15 9A 91 D1 4A 4D AE 47 D4 E8 FC 19 69 0F 87 3F B4 2C F5 5F 11 59 5D EB 77 57 02 E6 EA 25 98 2F 96 CF B4 00 10 9E 32 CC B8 3B 57 3B 94 63 A5 75 31 87 DB 14 32 48 F3 45 B0 C4 ED 2C 3F 34 AC 38 C9 C6 00 1F 2B 67 E5 C1 DC 31 8E 87 86 87 C6 10 69 96 50 DA E9 36 09 1C 43 93 11 8D 63 8E 32 57 95 45 40 38 DF F3 12 79 39 61 C6 46 3A DD 1B 58 6D 4E 24 59 12 38 AE 10 32 DC 44 CC 55 D1 C0 43 80 87 9C 6D 70 72 4F 19 5E B9 AB 4F 9B 6E 86 35 62 D6 AF AF F5 B1 34 10 3D 82 C9 27 95 73 73 3C 80 19 1C 4A 0F 98 57 0A 0E 09 55 56 2B 82 76 80 38 3E D9 B1 6D 1B A0 24 EE 58 DC 6F 11 C8 4B 3A 39 25 9B 2D B8 82 39 00 01 C0 C7 07 18 02 AE 9B 34 A2 19 26 B9 8E F2 29 26 93 79 82 E5 92 49 22 DC DB 42 E2 2C A8 4E 01 07 27 A9 2C 72 0D 4D 03 C3 A9 69 A4 1B 62 B0 4D 19 51 14 F1 8C 32 1C 81 95 F4 23 9D A7 07 07 04 03 91 54 EE 88 D5 B6 C9 22 B7 47 B6 71 71 6B 02 C9 70 A0 DC A2 7C EA CC 54 29 04 90 37 0C 00 32 40 C8 03 81 D2 92 F1 ED ED 63 6D 42 E1 82 2D AC 6E EF 20 4C 91 1E 32 C3 A1 38 E0 1C 0E A5 45 4A E2 46 66 40 59 55 90 E1 D4 0C A1 FC 7A F5 E3 8C 70 73 50 4A 63 76 BA 10 A4 C9 72 63 31 99 63 87 0C 70 32 36 B3 0D A7 1B F8 CE 46 77 7A 36 01 AD 65 76 32 4F 26 70 71 1C B3 B4 33 79 6F 0A 4C 0E 37 F0 77 8D D8 2A 15 F7 ED 39 E3 18 19 C0 A6 43 71 2A BC 93 BD B2 A4 2F 38 55 78 1B CD 13 23 2A 85 94 E1 73 9C E1 7D 00 04 93 81 91 61 D4 4D 00 73 24 C1 5D E3 75 06 21 95 E5 48 18 2B 90 38 E7 23 23 27 91 81 8A F3 A5 D3 DC 4F 2C 37 11 32 47 81 E5 43 17 EF B8 46 F9 77 17 DB 9C B2 B0 DC B8 03 39 07 21 82 51 B8 AC 9A B2 1F 04 8F 73 24 B6 F7 B0 C2 5A 39 49 50 03 60 ED 6D C8 40 60 32 42 98 C9 65 C8 0D 91 C6 28 A4 B8 73 73 72 B1 C1 6D 6F 70 D6 D3 2F 99 24 93 05 30 96 53 BB 6E 03 11 20 56 1C 1D B9 12 7D EE B4 52 9B 92 F8 57 E9 FA 31 A4 AD 66 CA B2 9F EC DB 01 7E 61 2A F1 A2 1B B2 B2 2C 41 11 54 13 B4 3B 14 0A 31 D3 76 00 2E 43 67 3B A8 78 A9 A0 BC 89 12 1B CB 30 F1 92 8E 39 79 79 23 E4 55 40 58 E4 81 90 07 55 1E 95 BC F1 90 B3 C9 67 25 B4 4C E1 CB 3B 45 B8 19 40 0A 19 B0 C3 20 05 C1 1D 4E 00 C8 C5 62 FF 00 61 E9 77 10 B4 B2 42 F7 17 17 13 4C 1E 48 E6 E5 1F 2E 4A 8C 95 07 69 05 47 1D 81 23 A9 A7 0B 26 44 AE D7 BB FE 5F 91 14 F1 4F 67 A1 9D 33 4D 68 E5 9D D0 4A 63 55 D9 2A C4 47 2C 55 9B 71 25 83 76 1E 98 C8 C9 93 48 D2 E2 D2 6C 2D AE 6E A5 96 DD D0 B5 CC ED BB 64 61 42 30 C4 84 F1 B4 06 CE 0F 70 0F F0 9A A7 27 86 A3 8E 49 EC 6D 75 1B 94 62 A8 67 5D BB 95 E3 91 D9 54 10 A7 3C 05 6C E4 63 E8 33 8D A8 6C CE 97 A6 3C D2 C6 D7 17 10 45 26 F3 0A 65 A7 5D C5 B1 B0 11 B9 CF BE 79 63 C9 C9 CE 8E 4A CE CC 88 C7 DE 4D AB 5B D0 87 50 96 6D 42 F0 47 69 11 95 2C 90 5C 32 37 CA 1E 72 3F 74 87 3C 7C A3 2E 54 E0 83 E5 11 5C BC 5E 29 D5 2C D1 21 D4 AD 56 E6 15 90 F3 20 2A C5 91 BA 06 1C 1D AC BE 87 91 5B D2 78 7F 57 96 C1 3F E2 6C B0 5E 93 24 B3 9B 75 65 59 65 63 9C 16 DD 9D AA 02 A0 24 12 02 FE 02 B2 E9 FE 27 B6 B7 26 DA FE DA FA DC 9D C1 1F 0C 67 53 EA 58 73 91 FE D7 4E 01 E9 5C 93 E7 BF 32 B9 D2 DA D8 C9 68 75 5F 17 EA 50 CB 2C 3E 45 B6 08 57 DB 85 54 CF 24 67 EF 1E DC 77 C7 4A EE 22 5F 22 C6 2B 7D 3B 69 48 59 23 0D 80 E3 68 7D AE 3E F0 F9 80 0D 93 D8 F6 62 36 9C 38 E5 D5 35 3B C8 74 FD 73 44 81 A2 2E B2 AC BE 50 91 23 2B 93 CE 77 0C 9C 63 A8 23 35 D0 4B 6A 6E 21 0C CC 6D EE 4A AF EF 22 21 CC 64 67 EE EE 52 3F 89 86 76 F4 35 74 A3 14 9B 5B 93 2D 50 B3 F9 F2 C8 D0 44 CD 12 98 CE E9 42 8C A9 39 C1 42 41 04 8C 74 23 1C 83 DB 06 19 22 3F DB 11 B7 DA 08 2F 1E 44 6B 1E 0E 10 F5 2D DC 7E F0 82 A7 3D 41 5D A4 12 6C 7D 9A 13 71 F6 B5 86 35 B9 2B E5 99 59 32 DB 33 92 B9 EB 8E 3E 99 E7 9A 8E EE 25 BD B0 F2 65 8A 55 F3 80 18 5D A5 E2 6E A1 81 E4 02 A4 64 1E 79 03 19 38 AB B7 4B 91 7B AD 48 A2 72 FA A3 5B BC D1 BC D0 41 1C AE 8A 7B B9 65 DF B4 82 54 7E ED 80 C3 73 96 C8 E0 13 06 A7 6A FA BE 94 EF A6 CF 6C 0C D0 E6 29 C7 CC 25 56 19 C6 E1 D1 5B 09 F3 0C F1 DB 81 53 B5 B9 9A 3F 25 A4 99 A3 B7 0E 85 50 C9 1B B9 2B F2 E1 F7 8D D8 56 20 9C 9C B6 0E 54 AD 3E E7 EC D1 25 C3 5D 13 2E F8 1B 7C 21 5A 4D F1 AE 73 B6 21 9D C7 0F 83 B4 65 B2 07 A0 AA BD 99 7A A7 E6 64 E8 5A 35 B6 95 63 E6 C9 75 0F DA 1A 60 8D 3C 6C A4 02 1F 6F 96 0B 0E E7 2A 7B E4 91 C1 C5 6C 4D 71 C3 5B 89 C4 73 A8 8C B3 98 F8 01 D8 A8 C6 78 C9 C1 00 64 E0 E3 20 E4 02 E9 C0 48 E5 92 47 02 3E 77 B4 8E 13 CA 4C 73 86 03 23 A6 79 3D FA 8C 0A 7E D7 0B 14 2A 19 00 50 4B C6 14 28 C1 1F 2E 0E 71 9E 7A 76 07 90 71 4D C9 B6 4B 77 DC AD 63 72 B7 39 8D 2F 63 91 E3 8E 23 2A 2C 7B 25 46 23 71 2E A4 E5 77 0C 7C A4 02 39 E7 D2 2D 32 D8 D9 D8 C5 6D 74 B0 40 93 28 44 B3 0E 24 08 DB 49 65 0E 40 32 64 02 C7 23 39 DC 72 47 49 A4 F2 E1 6D B2 6A AF 1B 87 52 DB 9A 30 70 F2 FC 8A 41 5E 84 E6 31 DC 83 D4 B6 08 7E CB 99 32 1D 21 68 C4 E1 97 CC CB 17 4C 03 9C 6D 1B 19 5B A7 DE E1 07 20 9F 94 77 57 64 DF 4B 11 5C 5B A2 09 67 8D D2 CE 59 DC 2B BC 9C 87 6E 51 0E 03 01 93 95 19 FB C4 61 78 20 6D C2 F1 2D D5 9C 09 68 BA E5 83 DC C8 20 26 36 82 66 48 DA 5E 37 82 BB 81 00 61 70 4E 4E 18 FB E7 72 73 67 63 A8 35 F5 DB DB C2 1E 32 89 3C F7 04 11 80 59 D5 55 B8 51 B5 37 1C 1E 76 E4 8F 97 35 2D 9D CA 5E 88 EE AD 66 0D 6B 2C 7B F0 C8 C1 9B 38 0A C3 38 DA B8 07 B7 39 04 63 BC C9 76 35 8B 6A D2 7A 9C 6D B8 D6 64 79 1F 4C F0 E4 56 52 04 11 F9 85 4C 6E 06 E2 CB 82 C4 03 D1 73 C1 CE 39 E0 81 5D 1E 9F A7 EA AE 84 6B 17 71 C8 C9 22 BC 52 5B 1D 8C 40 39 28 C4 01 F2 92 AA 70 3A E0 83 91 C5 5C 46 10 43 3C D6 FA 76 EF 2E 47 11 C7 0A 84 91 C9 6C B9 C3 ED 03 2D 93 9C E1 B0 1B 27 34 C6 FB 64 51 24 F7 B7 56 D0 42 96 8C 6E 9D 78 11 4A 00 F9 D0 B0 E1 70 5F 3B B3 D1 7F DA CA 4A DA A2 AE DA 7A 25 F9 93 C5 0A 5C DC 2D F7 DA 16 E2 22 AA D6 C1 40 28 80 83 97 07 B9 60 48 CE 71 8C 00 06 58 B3 AE 24 26 68 E3 85 44 B3 21 DC CB E7 6C D8 0A B8 52 C0 75 04 8C 74 3C F3 FC 34 B0 C8 26 96 E1 56 50 E8 AC 01 1B B0 D1 B6 06 54 8C 02 38 DA C3 24 93 BB B0 C5 47 1C 52 B5 AC 5F DA 7E 4C F2 83 19 22 28 9B 60 7F 97 90 A4 9E 8E 37 03 FC 23 1E 84 97 74 D5 CC 9A 56 69 95 2D 66 96 EE D9 05 DC 6B 05 B1 67 89 24 FB 44 D1 4A DD 11 72 AC AA C0 B6 5F B9 C1 0A 41 6C E4 4F A7 23 88 63 43 E6 3A 5B 66 04 9E 72 7C E9 42 80 A4 BE E4 1D 58 31 C8 E1 82 AB 02 43 60 41 6D 75 6E DA 64 5A 9D F4 CD 68 90 92 64 13 4C 63 48 5C 17 46 46 3F 2A B2 A9 25 41 23 07 6A B6 4F CA 6B 48 48 EA 23 12 44 43 32 FC C5 0E E5 56 E0 63 B1 3D 7A E3 A0 39 C5 3B B7 BA 09 2E 6D 6C 57 68 DE 59 85 C4 32 DD 2A DC 42 23 C0 00 08 B8 66 12 6D 7E 8D CE 08 C1 EA B9 18 1C 3A 3B B9 A3 86 57 BC 81 63 78 A3 12 32 DB B3 4D 91 B7 24 00 14 31 39 0C 06 17 90 06 39 24 06 DA E9 F1 5A DB C5 18 89 3C D2 14 4B 2C 20 A6 E2 09 7C 93 B8 B1 05 8B 1C 12 C4 97 39 CE 49 A7 AC 12 C3 75 18 B7 F2 52 CF 6B 6F 8F 6E 08 6C E4 6D C7 AE 4E 49 CF 41 81 C9 34 EE 13 76 76 8E D7 22 9A 6B 99 DA F6 DA D6 78 23 9E 30 9B 15 81 2C 01 E7 71 CF 4C FC CA 0E 18 02 A4 FC D8 2A 0A 72 CB 71 0D BD C9 36 A1 64 59 4A C2 1E E3 2B 28 63 95 3B 8F 2A 32 D8 C6 38 C1 00 11 8C 94 96 DA AF EB EF 45 A8 B9 3F 76 56 DB AA FD 53 3F FF D9");
                break;
            case 71:
                mplew.write(false);
                break;
        }

        return mplew.getPacket();
    }

    public static byte[] checkSPWExistResult(int unk1, int showChangePaswBtn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ChangeSPWResult.getValue());
        mplew.write(unk1);
        mplew.write(showChangePaswBtn);
        return mplew.getPacket();
    }
    public static byte[] checkSPWExistAttachResult() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RequestSPW.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] addNewCharEntry(MapleCharacter chr, boolean worked) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CreateNewCharacterResult.getValue());
        mplew.write(worked ? 0 : 1);
        mplew.writeInt(0);
        chr.getAvatarData().encode(mplew);
        mplew.write(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] charNameResponse(String charname, boolean nameUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CheckDuplicatedIDResult.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] charNameResponse(String charname, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CheckDuplicatedIDResult.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(type); //0 = 可創建 1 = 已被使用 2 = 名字無法使用 3 = 因未知原因失敗

        return mplew.getPacket();
    }

    public static byte[] updatePartTimeJob(MaplePartTimeJob partTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.PART_TIME.getValue());
        mplew.writeInt(partTime.getCharacterId());
        mplew.write(0);
        PacketHelper.addPartTimeJob(mplew, partTime);
        return mplew.getPacket();
    }

    /*
     * 顯示角色卡的數量上限
     * 默認是3 最高為6
     */
    public static byte[] showCharCards(int cards) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_CHAR_CARDS.getValue());
        mplew.writeInt(cards);

        return mplew.getPacket();
    }

    /*
     * 顯示角色樂豆點信息
     */
    public static byte[] ShowAccCash(int ACash, int mPoints) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_ACC_CASH.getValue());
        mplew.writeInt(ACash);
        mplew.writeInt(mPoints);

        return mplew.getPacket();
    }

    public static byte[] changePlayerKey(String key) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_IssueReloginCookie.getValue());
        mplew.writeMapleAsciiString(key);
        mplew.writeInt(0); // V264 +
        return mplew.getPacket();
    }
}
