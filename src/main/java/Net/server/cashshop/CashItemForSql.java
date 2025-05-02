/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.cashshop;

/**
 * @author ODINMR
 */
public class CashItemForSql {

    private final int category, subcategory, parent, sn, itemid, flag, price, discountPrice, quantity, expire, gender, likes;
    private final String image;

    public CashItemForSql(int category, int subcategory, int parent, String image, int sn, int itemid, int flag, int price, int discountPrice, int quantity, int expire, int gender, int likes) {
        this.category = category;
        this.subcategory = subcategory;
        this.parent = parent;
        this.image = image;
        this.sn = sn;
        this.itemid = itemid;
        this.flag = flag;
        this.price = price;
        this.discountPrice = discountPrice;
        this.quantity = quantity;
        this.expire = expire;
        this.gender = gender;
        this.likes = likes;
    }

    /**
     * 獲取道具類別
     *
     * @return
     */
    public int getCategory() {
        return category;
    }

    /**
     * 獲取道具子類別
     *
     * @return
     */
    public int getSubCategory() {
        return subcategory;
    }

    /**
     * 獲取道具父系
     *
     * @return
     */
    public int getParent() {
        return parent;
    }

    /**
     * 獲取道具圖像
     *
     * @return
     */
    public String getImage() {
        return image;
    }

    /**
     * 獲取道具唯一編號
     *
     * @return
     */
    public int getSN() {
        return sn;
    }

    /**
     * 獲取道具ID
     *
     * @return
     */
    public int getItemId() {
        return itemid;
    }

    /**
     * 獲取道具標籤
     *
     * @return
     */
    public int getFlag() {
        return flag;
    }

    /**
     * 獲取道具價格
     *
     * @return
     */
    public int getPrice() {
        return price;
    }

    /**
     * 獲取道具折扣價格
     *
     * @return
     */
    public int getDiscountPrice() {
        return discountPrice;
    }

    /**
     * 獲取道具數量
     *
     * @return
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * 獲取道具過期時間
     *
     * @return
     */
    public int getExpire() {
        return expire;
    }

    /**
     * 獲取道具性別限制
     *
     * @return
     */
    public int getGender() {
        return gender;
    }

    /**
     * 獲取道具鏈接
     *
     * @return
     */
    public int getLikes() {
        return likes;
    }
}
