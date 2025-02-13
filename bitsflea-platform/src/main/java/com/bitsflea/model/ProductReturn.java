package com.bitsflea.model;

import java.math.BigInteger;

// import io.nuls.contract.sdk.MultyAssetValue;

/**
 * 商品退货记录
 */
public class ProductReturn {
    /**
     * 订单id
     */
    public BigInteger oid;
    /**
     * 商品id
     */
    public BigInteger pid;
    /**
     * 订单金额
     */
    // public MultyAssetValue orderAmount;
    /**
     * 状态
     */
    public short status;
    /**
     * 退货原因,实质为存储在IPFS上的一个url,内容为json格式
     */
    public String reasons;
    /**
     * 运单号
     */
    public String shipmentsNumber;
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
         * 完成
         */
        public static final short RS_COMPLETED = 200;
        /**
         * 取消
         */
        public static final short RS_CANCELLED = 300;
        /**
         * 仲裁中
         */
        public static final short RS_ARBITRATION = 400;
    }
}
