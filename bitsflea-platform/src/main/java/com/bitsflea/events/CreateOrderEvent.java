package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.MultyAssetValue;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 订单创建事件
 */
@Data
@AllArgsConstructor
public class CreateOrderEvent implements Event {
    /**
     * 订单id
     */
    private BigInteger oid;
    /**
     * 商品id
     */
    private BigInteger pid;
    /**
     * 卖家
     */
    private Address seller;
    /**
     * 买家
     */
    private Address buyer;
    /**
     * 商品金额
     */
    private MultyAssetValue amount;
    /**
     * 邮费金额
     */
    private MultyAssetValue postage;
    /**
     * 创建时间
     */
    private long createTime;
    /**
     * 支付超时时间
     */
    private long payTimeOut;
}
