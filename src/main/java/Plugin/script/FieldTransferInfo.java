package Plugin.script;

import Client.MapleCharacter;
import Net.server.maps.MapleMap;

public class FieldTransferInfo {
    private int fieldId, portal;
    private boolean init;
    private boolean field;

    public FieldTransferInfo() {
        init = true;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        init = false;
        this.fieldId = fieldId;
    }

    public int getPortal() {
        return portal;
    }

    public void setPortal(int portal) {
        this.portal = portal;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public boolean isField() {
        return field;
    }

    public void setField(boolean field) {
        this.field = field;
    }

    public void warp(MapleCharacter chr) {
        setInit(true);
        chr.changeMap(getFieldId(), getPortal());
    }

    public void warp(MapleMap field) {
        setInit(true);
        for (MapleCharacter chr : field.getAllCharactersThreadsafe()) {
            chr.changeMap(getFieldId(), getPortal());
        }
    }


}
