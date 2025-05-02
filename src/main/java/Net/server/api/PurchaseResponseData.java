/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * @author Tyler
 */
public class PurchaseResponseData {

    private final boolean success = false;
    private String error;
    private PurchaseInfo purchase_info;
    private ItemDetails item_details;

    public class PurchaseInfo {

        private int product_id;
        private int user_id;
        private int previous_lp_total;
        private int current_lp_total;
        private String order_id;

        public int getProductId() {
            return product_id;
        }

        public int getUserId() {
            return user_id;
        }

        public int getPreviousLPTotal() {
            return previous_lp_total;
        }

        public int getCurrentLPTotal() {
            return current_lp_total;
        }

        public String getOrderId() {
            return order_id;
        }
    }

    public class ItemDetails {

        private Item[] items;

        public Item[] getItems() {
            return items;
        }

        public class Item {

            private int item_id;
            private String item_name;
            private String item_description;
            private int item_status;
            private int item_price;
            private int category_id;

            public int getItemId() {
                return item_id;
            }

            public String getItemName() {
                return item_name;
            }

            public String getItemDescription() {
                return item_description;
            }

            public int getItemStatus() {
                return item_status;
            }

            public int getItemPrice() {
                return item_price;
            }

            public int getCategoryId() {
                return category_id;
            }
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public PurchaseInfo getPurchaseInfo() {
        return purchase_info;
    }

    public ItemDetails getItemDetails() {
        return item_details;
    }
}
