package com.bitsflea.model;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.MultyAssetValue;

/**
 * 商品数据存储类
 */
public class Product {

    /**
     * 商品id
     * 由发布者地址的hashCode(32位)+时间戳(64位)
     */
    public BigInteger pid;
    /**
     * 商品所属人地址
     */
    public Address uid;
    /**
     * 商品详细说明, 存储在IPFS的url
     * 格式如下:
     * {"title":"","description":"","photos":[]}
     */
    public String description;
    /**
     * 商品分类id
     */
    public int category;
    /**
     * 商品状态，值对应ProductStatus
     */
    public short status;
    /**
     * 是否全新
     */
    public boolean isNew;
    /**
     * 是否支持退货
     */
    public boolean isReturns;
    /**
     * 审核人地址
     */
    public Address reviewer;
    /**
     * 销售方式, 值对应: SaleMethod
     */
    public short saleMethod;
    /**
     * 商品价格
     */
    public MultyAssetValue price;
    /**
     * 取货方式,值对应: PickupMethod
     */
    public short pickupMethod;
    /**
     * 库存数量
     */
    public int stockCount;
    /**
     * 是否零售
     */
    public boolean isRetail;
    /**
     * 邮费
     */
    public MultyAssetValue postage;
    /**
     * 位置
     */
    public String position;
    /**
     * 发布时间
     */
    public long publishTime;

    /**
     * 商品状态
     */
    public static class ProductStatus {
        /**
         * 发布中
         */
        public static final short PUBLISH = 0;
        /**
         * 正常
         */
        public static final short NORMAL = 100;
        /**
         * 完成交易
         */
        public static final short COMPLETED = 200;
        /**
         * 下架
         */
        public static final short DELISTED = 300;
        /**
         * 锁定中
         */
        public static final short LOCKED = 400;
    }

    /**
     * 商品分类
     */
    public class Categories {
        public int id;
        public String view;
        public int parent;
    }

    /**
     * 销售方式
     */
    public static class SaleMethod {
        public static final short BUY_NOW = 0;
        public static final short AUCTION = 1;

        public static boolean isValid(short val) {
            switch (val) {
                case BUY_NOW:
                case AUCTION:
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * 取货方式
     */
    public static class PickupMethod {
        // 自提
        public static final short PICK_UP = 0;
        // 邮寄
        public static final short MAIL = 1;

        public static boolean isValid(short val) {
            switch (val) {
                case PICK_UP:
                case MAIL:
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * 商品拍卖信息
     */
    public class ProductAuction {
        /*
         * 商品id
         */
        public long pid;

        /**
         * 保证金
         */
        public BigInteger security;

        /**
         * 加价幅度
         */
        public BigInteger markup;
        /**
         * 当前价格
         */
        public BigInteger currentPrice;
        /**
         * 已拍人次
         */
        public int auctionTimes;
        /**
         * 最后出价人地址
         */
        public Address lastPriceUser;
        /**
         * 开拍时间
         */
        public long startTime;
        /**
         * 结束时间
         */
        public long endTime;
    }
}
