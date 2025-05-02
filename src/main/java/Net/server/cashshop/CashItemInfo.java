package Net.server.cashshop;

import Config.constants.enums.CashItemModFlag;

public class CashItemInfo {

    private final int itemId; //道具ID
    private final int count; //默認購買數量
    private final int price; //道具價格
    private final int meso;
    private final int originalPrice; //道具原價
    private final int sn; //道具的SN
    private final int period; //道具的持續時間 也就是道具購買有有時間限制
    private final int gender; //道具是否有性別限制
    private final byte csClass;
    private final byte priority;
    private final int termStart;
    private final int termEnd;
    private final boolean onSale; //道具是否出售
    private final boolean bonus; //是否幹什麼的獎金
    private final boolean refundable; //是否可以回購換成楓點？
    private final boolean discount; //道具是否打折出售
    private final int mileageRate; // 里程抵扣率
    private final boolean onlyMileage; // 可全里程購買
    private final int LimitMax;
    private final String wzName;

    public CashItemInfo(String wzName, int itemId, int count, int price, int originalPrice, int meso, int sn, int period, int gender, byte csClass, byte priority, int termStart, int termEnd, boolean sale, boolean bonus, boolean refundable, boolean discount, int mileageRate, boolean onlyMileage, int LimitMax) {
        this.wzName = wzName;
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.originalPrice = originalPrice;
        this.meso = meso;
        this.sn = sn;
        this.period = period;
        this.gender = gender;
        this.csClass = csClass;
        this.priority = priority;
        this.termStart = termStart;
        this.termEnd = termEnd;
        this.onSale = sale;
        this.bonus = bonus;
        this.refundable = refundable;
        this.discount = discount;
        this.mileageRate = mileageRate;
        this.onlyMileage = onlyMileage;
        this.LimitMax = LimitMax;
    }


    public String getName() {
        return this.wzName;
    }

    /**
     * 獲取道具ID
     *
     * @return
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * 獲取道具數量
     *
     * @return
     */
    public int getCount() {
        return count;
    }

    /*
     * 道具的價格
     * 暫時取最高價格
     */
    public int getPrice() {
        return price;
    }

    /*
     * 道具原始價格
     */
    public int getOriginalPrice() {
        return originalPrice;
    }

    public int getSN() {
        return sn;
    }

    public int getPeriod() {
        return period;
    }

    public int getGender() {
        return gender;
    }

    public boolean onSale() {
        CashModInfo modInfo = CashItemFactory.getInstance().getModInfo(sn);
        if (modInfo != null) {
            return modInfo.showUp;
        }
        return onSale;
    }

    public boolean genderEquals(int g) {
        return g == this.gender || this.gender == 2;
    }

    public boolean isBonus() {
        return bonus;
    }

    public boolean isRefundable() {
        return refundable;
    }

    public boolean isDiscount() {
        return discount;
    }

    public int getMeso() {
        return meso;
    }

    public byte getCsClass() {
        return csClass;
    }

    public byte getPriority() {
        return priority;
    }

    public int getTermStart() {
        return termStart;
    }

    public int getTermEnd() {
        return termEnd;
    }

    public int getMileageRate() {
        return mileageRate;
    }

    public boolean isOnlyMileage() {
        return onlyMileage;
    }

    public int getLimitMax() {
        return LimitMax;
    }

    public static class CashModInfo {

        private int price, originalPrice, mark, priority, sn, itemid, period, gender, count, meso, csClass, termStart, termEnd, fameLimit, levelLimit, categories;
        private long flags;
        private boolean showUp, packagez, base_new;
        private CashItemInfo cii;

        public CashModInfo(int sn, int price, int mark, boolean show, int itemid, int priority, boolean packagez, int period, int gender, int count, int meso, int csClass, int termStart, int termEnd, int fameLimit, int levelLimit, int categories, boolean base_new) {
            this.sn = sn;
            this.itemid = itemid;
            this.price = price;
            this.originalPrice = 0;
            this.mark = mark; //0 = new, 1 = sale, 2 = hot, 3 = event
            this.showUp = show;
            this.priority = priority;
            this.packagez = packagez;
            this.period = period;
            this.gender = gender;
            this.count = count;
            this.meso = meso;
            this.csClass = csClass; //0 = doesn't have, 1 = has, but false, 2 = has and true
            this.termStart = termStart;
            this.termEnd = termEnd;
            this.flags = 0;
            this.fameLimit = fameLimit;
            this.levelLimit = levelLimit;
            this.categories = categories;
            this.base_new = base_new;

            if (this.itemid > 0) {
                this.flags |= CashItemModFlag.ITEM_ID.getValue();
            }
            if (this.count > 0) {
                this.flags |= CashItemModFlag.COUNT.getValue();
            }
            if (this.price > 0) {
                this.flags |= CashItemModFlag.PRICE.getValue();
            }
            if (this.csClass > 0) {
                this.flags |= CashItemModFlag.BONUS.getValue();
            }
            if (this.priority >= 0) {
                this.flags |= CashItemModFlag.PRIORITY.getValue();
            }
            if (this.period > 0) {
                this.flags |= CashItemModFlag.PERIOD.getValue();
            }
            //0x40 = ?
            if (this.meso > 0) {
                this.flags |= CashItemModFlag.MESO.getValue();
            }
            if (this.gender >= 0) {
                this.flags |= CashItemModFlag.COMMODITY_GENDER.getValue();
            }
            this.flags |= CashItemModFlag.ON_SALE.getValue();
            if (this.mark >= -1 || this.mark <= 0xF) {
                this.flags |= CashItemModFlag.CLASS.getValue();
            }
            //0x2000, 0x4000, 0x8000, 0x10000, 0x20000, 0x100000, 0x80000 - ?
            if (this.fameLimit > 0) {
                this.flags |= CashItemModFlag.REQ_POP.getValue();
            }
            if (this.levelLimit > 0) {
                this.flags |= CashItemModFlag.REQ_LEV.getValue();
            }
            if (this.termStart > 0) {
                this.flags |= CashItemModFlag.TERM_START.getValue();
            }
            if (this.termEnd > 0) {
                this.flags |= CashItemModFlag.TERM_END.getValue();
            }
            if (this.categories > 0) {
                this.flags |= CashItemModFlag.CATEGORY_INFO.getValue();
            }
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getOriginalPrice() {
            return originalPrice;
        }

        public void setOriginalPrice(int originalPrice) {
            this.originalPrice = originalPrice;
        }

        public int getMark() {
            return mark;
        }

        public void setMark(int mark) {
            this.mark = mark;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getSn() {
            return sn;
        }

        public void setSn(int sn) {
            this.sn = sn;
        }

        public int getItemid() {
            return itemid;
        }

        public void setItemid(int itemid) {
            this.itemid = itemid;
        }

        public long getFlags() {
            return flags;
        }

        public void setFlags(long flags) {
            this.flags = flags;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        public int getGender() {
            return gender;
        }

        public void setGender(int gender) {
            this.gender = gender;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getMeso() {
            return meso;
        }

        public void setMeso(int meso) {
            this.meso = meso;
        }

        public int getCsClass() {
            return csClass;
        }

        public void setCsClass(int csClass) {
            this.csClass = csClass;
        }

        public int getTermStart() {
            return termStart;
        }

        public void setTermStart(int termStart) {
            this.termStart = termStart;
        }

        public int getTermEnd() {
            return termEnd;
        }

        public void setTermEnd(int termEnd) {
            this.termEnd = termEnd;
        }

        public int getFameLimit() {
            return fameLimit;
        }

        public void setFameLimit(int fameLimit) {
            this.fameLimit = fameLimit;
        }

        public int getLevelLimit() {
            return levelLimit;
        }

        public void setLevelLimit(int levelLimit) {
            this.levelLimit = levelLimit;
        }

        public int getCategories() {
            return categories;
        }

        public void setCategories(int categories) {
            this.categories = categories;
        }

        public boolean isShowUp() {
            return showUp;
        }

        public void setShowUp(boolean showUp) {
            this.showUp = showUp;
        }

        public boolean isPackagez() {
            return packagez;
        }

        public void setPackagez(boolean packagez) {
            this.packagez = packagez;
        }

        public boolean isBase_new() {
            return base_new;
        }

        public void setBase_new(boolean base_new) {
            this.base_new = base_new;
        }

        public CashItemInfo getCii() {
            return cii;
        }

        public void setCii(CashItemInfo cii) {
            this.cii = cii;
        }

        public CashItemInfo toCItem(CashItemInfo backup) {
            if (cii != null) {
                return cii;
            }
            final int item, c, price, expire, gen, likes;
            final boolean onSale;
            if (itemid <= 0) {
                item = backup.getItemId();
            } else {
                item = itemid;
            }
            if (count <= 0) {
                c = backup.getCount();
            } else {
                c = count;
            }
            if (meso <= 0) {
                if (this.price <= 0) {
                    price = backup.price;
                } else {
                    price = this.price;
                }
                if (this.price > 0 && (backup.originalPrice > 0 ? backup.originalPrice : backup.price) < this.price) {
                    flags |= CashItemModFlag.ORIGINAL_PRICE.getValue();
                    if (backup.price > 0 && backup.originalPrice > 0 && backup.originalPrice > backup.price) {
                        originalPrice = price * backup.originalPrice / backup.price;
                    } else {
                        originalPrice = price;
                    }
                } else {
                    originalPrice = backup.originalPrice;
                }
            } else {
                price = meso;
                originalPrice = backup.originalPrice;
            }
            if (period <= 0) {
                expire = backup.getPeriod();
            } else {
                expire = period;
            }
            if (gender < 0) {
                gen = backup.getGender();
            } else {
                gen = gender;
            }
            if (!showUp) {
                onSale = backup.onSale();
            } else {
                onSale = showUp;
            }

            cii = new CashItemInfo(backup.wzName, item, c, price, originalPrice, meso, sn, expire, gen, backup.csClass, backup.priority, backup.termStart, backup.termEnd, onSale, backup.bonus, backup.refundable, backup.discount, backup.mileageRate, backup.onlyMileage, backup.LimitMax);
            return cii;
        }
    }
}
