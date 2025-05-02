package SwordieX.client.character.keys;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Keymapping {
    private byte type;
    private int action;

    public Keymapping(byte type, int action) {
        this.type = type;
        this.action = action;
    }
}
