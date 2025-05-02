package Net.server.api.data;

public class BestCashShopItems {

    private int SN;
    private int total_purchases;

    /**
     * Returns the Serial Number of the specific cash item
     *
     * @return the sN
     */
    public int getSN() {
        return SN;
    }

    /**
     * Returns the amount of purchases for the cash item
     *
     * @return the total_purchases
     */
    public int getTotalPurchases() {
        return total_purchases;
    }
}
