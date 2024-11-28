package com.bitsflea.interfaces;

import java.math.BigInteger;

import io.nuls.contract.sdk.annotation.Required;

public interface IMarket {
    /**
     * 发布商品
     * 
     * @param pid          商品id
     * @param category     分类
     * @param description  描述json的url,一般存储于IPFS
     * @param isNew        是否为全新
     * @param isRetail     是否零售
     * @param isReturns    是否支持退货
     * @param position     位置
     * @param saleMethod   销售方式: 一口价、拍卖
     * @param stockCount   库存数量
     * @param pickupMethod 取货方式: 邮寄、自提
     * @param postage      邮费
     * @param price        价格
     */
    void publish(@Required BigInteger pid,
            @Required int category,
            @Required String description,
            @Required boolean isNew,
            @Required boolean isRetail,
            @Required boolean isReturns,
            @Required String position,
            @Required short saleMethod,
            @Required int stockCount,
            @Required short pickupMethod,
            @Required String postage,
            @Required String price);

    /**
     * 卖家下架商品
     * 
     * @param pid 商品id
     */
    void delist(@Required BigInteger pid);

    /**
     * 买家下订单, 普通订单, 即一个商品只卖一次(不考虑库存)
     * 合约会验证订单id中的商品id与下单人hashCode
     * 
     * @param pid     商品id
     * @param orderId 订单id
     */
    void placeOrder(@Required BigInteger pid, @Required BigInteger orderId);

    /**
     * 取消订单
     * 买家取消订单只能在未支付状态
     * 如果超过支付时间取消会被扣信用分
     * 
     * @param orderId 订单id
     */
    void cancelOrder(@Required BigInteger orderId);

    /**
     * 卖家发货
     * 
     * @param orderId 订单id
     * @param number  物流编号(非邮寄收货时可以为空)
     */
    void shipments(@Required BigInteger orderId, String number);

    /**
     * 买家退货时的发货
     * 
     * @param orderId 订单id
     * @param number  物流编号(退货实物时才会调用此方法)
     */
    void reShipments(@Required BigInteger orderId, @Required String number);

    /**
     * 确认收货
     * 
     * @param orderId 订单id
     */
    void confirmReceipt(@Required BigInteger orderId);

    /**
     * 退货时的确认收货,只有实物退货时才用使用
     * 
     * @param orderId 订单id
     */
    void reConfirmReceipt(@Required BigInteger orderId);

    /**
     * 买家发起退货
     * 
     * @param orderId 订单id
     * @param reasons 退货原因说明(实质为存储在IPFS上的一个url,内容为json格式)
     */
    void returns(@Required BigInteger orderId, String reasons);

    /**
     * 延迟收货
     * 
     * @param orderId 订单id
     */
    void deferReceipt(@Required BigInteger orderId);

    /**
     * 退货时的延迟收货
     * 
     * @param orderId 订单id
     */
    void reDeferReceipt(@Required BigInteger orderId);
    /**
     * 支付订单
     * @param orderId 订单id
     */
    void payOrder(BigInteger orderId);
}
