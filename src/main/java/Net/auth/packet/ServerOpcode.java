package Net.auth.packet;

import tools.data.WritableIntValueHolder;

public enum ServerOpcode implements WritableIntValueHolder {
    AliveAckRequest(0),
    MachineCodeResult(1),
    AuthChangeResult(2),
    GetMachineCodeRequest(3),
    SetMapleAESKey(4),
    SetOpcodeValues(5),
    SetEndata(6),
    SetOpcodeEncryptionData(7),
    StartServerResponse(8),
    TimeCheckError(9),
    VersionOutdated(10),
    PermissionsResponse(11),
    CloudScriptResponse(12),
    ForbiddenMobResponse(13),
    MasterAuthChangeResult(101),
    MasterAskAuthListResult(102),
    ExceptionOccurred(103),
    ;
    private final short value;

    ServerOpcode(int value) {
        this.value = (short) value;
    }

    @Override
    public short getValue() {
        return value;
    }

    @Override
    public short getCode() {
        return value;
    }

    @Override
    public void setValue(short newval) {
    }

    @Override
    public void setValue(Short code) {

    }

    public static ServerOpcode get(short val) {
        for (ServerOpcode opcode : values()) {
            if (opcode.value == val) {
                return opcode;
            }
        }
        return null;
    }
}
