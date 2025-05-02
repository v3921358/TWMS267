package Net.server;

public class RaffleItem {

    private int id, period, itemId, quantity, chance, type;
    private boolean smega, allow;

    public RaffleItem(int id, int period, int itemId, int quantity, int chance, boolean smega, int type, boolean allow) {
        this.id = id;
        this.period = period;
        this.itemId = itemId;
        this.quantity = quantity;
        this.chance = chance;
        this.smega = smega;
        this.type = type;
        this.allow = allow;
    }

    public int getId() {
        return id;
    }

    public int getPeriod() {
        return period;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getChance() {
        return chance;
    }

    public boolean isSmega() {
        return smega;
    }

    public int getType() {
        return type;
    }

    public boolean isAllow() {
        return allow;
    }

    public void setId(int value) {
        this.id = value;
    }

    public void setPeriod(int value) {
        this.period = value;
    }

    public void setItemId(int value) {
        this.itemId = value;
    }

    public void setQuantity(int value) {
        this.quantity = value;
    }

    public void setChance(int value) {
        this.chance = value;
    }

    public void setSmega(boolean value) {
        this.smega = value;
    }

    public void setType(int value) {
        this.type = value;
    }

    public void setAllow(boolean value) {
        this.allow = value;
    }
}
