package Client.hexa;

import lombok.Getter;

@Getter
public class ErdaConsumeOption {
    private final int erda;
    private final int fragments;

    public ErdaConsumeOption(int erda, int fragments) {
        this.erda = erda;
        this.fragments = fragments;
    }
}
