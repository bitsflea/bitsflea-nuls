package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 完成订单事件
 */
@Data
@AllArgsConstructor
public class CompleteOrderEvent implements Event {
    /**
     * 卖家
     */
    private Address seller;
    /**
     * 买家
     */
    private Address buyer;
    /**
     * 订单号
     */
    private BigInteger oid;
}
