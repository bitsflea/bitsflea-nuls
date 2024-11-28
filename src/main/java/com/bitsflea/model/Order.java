package com.bitsflea.model;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.MultyAssetValue;

public class Order {
    /**
     * 订单id
     * 下单人地址的hashCode(32位)+商品id(96位)+时间戳(64位)
     */
    public BigInteger oid;
    /**
     * 商品id
     */
    public BigInteger pid;
    /**
     * 卖家
     */
    public Address seller;
    /**
     * 买家
     */
    public Address buyer;
    /**
     * 订单金额
     */
    public MultyAssetValue amount;
    /**
     * 邮费金额
     */
    public MultyAssetValue postage;
    /**
     * 订单状态,值对应 OrderStatus
     */
    public short status;
    /**
     * 物流单号
     */
    public String shipmentNumber;
    /**
     * 创建时间
     */
    public long createTime;
    /**
     * 支付时间
     */
    public long payTime;
    /**
     * 支付超时时间
     */
    public long payTimeOut;
    /**
     * 发货时间
     */
    public long shipTime;
    /**
     * 发货超时时间
     */
    public long shipTimeOut;
    /**
     * 收货时间
     */
    public long receiptTime;
    /**
     * 收货超时时间
     */
    public long receiptTimeOut;
    /**
     * 订单完成时间
     */
    public long endTime;
    /**
     * 延期收货次数
     */
    public int delayedCount;

    /**
     * 订单状态
     */
    public static class OrderStatus {
        /**
         * 待付款
         */
        public static final short OS_PENDING_PAYMENT = 0;
        /**
         * 待确认
         */
        public static final short OS_PENDING_CONFIRM = 100;
        /**
         * 已取消
         */
        public static final short OS_CANCELLED = 200;
        /**
         * 待发货
         */
        public static final short OS_PENDING_SHIPMENT = 300;
        /**
         * 待收货
         */
        public static final short OS_PENDING_RECEIPT = 400;
        /**
         * 待结算
         */
        public static final short OS_PENDING_SETTLE = 500;
        /**
         * 已完成
         */
        public static final short OS_COMPLETED = 600;
        /**
         * 仲裁中
         */
        public static final short OS_ARBITRATION = 700;
        /**
         * 退货中
         */
        public static final short OS_RETURN = 800;
    }

    /**
     * 结算状态
     */
    public static class SettlementStatus {
        /**
         * 待结算
         */
        public static final short OSS_NORMAL = 0;
        /**
         * 已支付
         */
        public static final short OSS_PAID = 100;
    }

    /**
     * 退货状态
     */
    public static class ReturnStatus {
        /**
         * 待发货
         */
        public static final short RS_PENDING_SHIPMENT = 0;
        /**
         * 待收货
         */
        public static final short RS_PENDING_RECEIPT = 100;
        /**
         * 已完成
         */
        public static final short RS_COMPLETED = 200;
        /**
         * 已取消
         */
        public static final short RS_CANCELLED = 300;
        /**
         * 仲裁中
         */
        public static final short RS_ARBITRATION = 400;
    }

    /**
     * 退货记录
     */
    public class ReturnGoods {
        /**
         * 订单id
         */
        public BigInteger orderId;
        /**
         * 商品
         */
        public long pid;
        /**
         * 订单金额
         */
        public MultyAssetValue orderAmount;
        /**
         * 状态,值对应 ReturnStatus
         */
        public short status;
        /**
         * 退货原因
         */
        public String reasons;
        /**
         * 物流单号
         */
        public String shipmentNumber;
        /**
         * 创建时间
         */
        public long createTime;
        /**
         * 发货时间
         */
        public long shipTime;
        /**
         * 发货超时时间
         */
        public long shipTimeOut;
        /**
         * 收货时间
         */
        public long receiptTime;
        /**
         * 收货超时时间
         */
        public long receiptTimeOut;
        /**
         * 完成时间
         */
        public long endTime;
        /**
         * 延期收货次数
         */
        public int delayedCount;
    }
}