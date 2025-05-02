package Net.auth.packet;

import tools.data.WritableIntValueHolder;

public enum ClientOpcode implements WritableIntValueHolder {
    AliveCheckResult(0),
    MachineCodeResponse(1),
    AuthChangeRequest(2),
    ConnectionSuccessResponse(3),
    StartServerRequest(4),
    ReportAttackErrorRequest(5),
    UploadScriptRequest(8),
    ;
    private final short value;

    ClientOpcode(int value) {
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
}
