package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
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
}
