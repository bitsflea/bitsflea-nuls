package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 取消订单事件
 */
@Data
@AllArgsConstructor
public class CancelOrderEvent implements Event {
    /**
     * 订单id
     */
    private BigInteger oid;
    /**
     * 商品id
     */
    private BigInteger pid;
    /**
     * 买家
     */
    private Address buyer;
    /**
     * 取消时间
     */
    private long time;
}
