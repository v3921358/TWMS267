package Client;

public class MemoEntry {
    public final String sender;
    public final String message;
    public final long timestamp;
    public final int pop;
    public final int id;

    public MemoEntry(final int id, final String sender, final String message, final long timestamp, final int pop) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.pop = pop;
    }
}
