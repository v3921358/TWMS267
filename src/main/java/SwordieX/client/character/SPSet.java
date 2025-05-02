package SwordieX.client.character;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SPSet {
    private byte jobLevel;
    private int sp;

    public SPSet() {
    }

    public SPSet(byte jobLevel, int sp) {
        this.jobLevel = jobLevel;
        this.sp = sp;
    }

    public void addSp(int sp) {
        setSp(getSp() + sp);
    }
}
