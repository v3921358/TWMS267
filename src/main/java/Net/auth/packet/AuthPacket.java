package Net.auth.packet;

import Config.configs.Config;
import Config.constants.ServerConstants;
import Net.auth.Auth;
import tools.data.MaplePacketLittleEndianWriter;

public class AuthPacket {

    public static byte[] getPing() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(ClientOpcode.AliveCheckResult);
        mplew.writeLong(System.currentTimeMillis());
        return mplew.getPacket();
    }

    public static byte[] connectionSuccess(long tick) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(ClientOpcode.ConnectionSuccessResponse);
        mplew.writeLong(tick);
        return mplew.getPacket();
    }

    public static byte[] machineCodeResponse(long tick) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(ClientOpcode.MachineCodeResponse);
        mplew.writeLong(tick);
        mplew.writeInt(Auth.getEncryptedMachineCode().length);
        mplew.write(Auth.getEncryptedMachineCode());
        mplew.writeInt(Auth.getEncryptedUUID().length);
        mplew.write(Auth.getEncryptedUUID());
        return mplew.getPacket();
    }

    public static byte[] authChangeRequest(long tick, String user, String sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(ClientOpcode.AuthChangeRequest);
        mplew.writeLong(tick);
        mplew.writeInt(Auth.getEncryptedMachineCode().length);
        mplew.write(Auth.getEncryptedMachineCode());
        mplew.writeInt(Auth.getEncryptedUUID().length);
        mplew.write(Auth.getEncryptedUUID());
        mplew.writeMapleAsciiString(user);
        mplew.writeMapleAsciiString(sn);
        return mplew.getPacket();
    }

    public static byte[] startServerRequest(long tick) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(ClientOpcode.StartServerRequest);
        mplew.writeLong(tick);
        mplew.writeShort(ServerConstants.MapleMajor);
        mplew.writeInt(Config.getServerBuildVersion());
        return mplew.getPacket();
    }

    public static byte[] reportAttackErrorRequest(int skillId, byte[] packet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(ClientOpcode.ReportAttackErrorRequest);
        mplew.writeShort(ServerConstants.MapleMajor);
        mplew.writeInt(Config.getServerBuildVersion());
        mplew.write(1);
        mplew.writeInt(skillId);
        mplew.writeInt(packet.length);
        mplew.write(packet);
        return mplew.getPacket();
    }

    public static byte[] uploadScriptRequest(byte[] fileContent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(ClientOpcode.UploadScriptRequest);
        mplew.write(fileContent);
        return mplew.getPacket();
    }
}
