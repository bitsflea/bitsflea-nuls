package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 退货事件
 */
@Data
@AllArgsConstructor
public class ReturnEvent implements Event {
    /**
     * 订单id
     */
    private BigInteger oid;
    /**
     * 商品id
     */
    private BigInteger pid;
    /**
     * 买家地址
     */
    private Address buyer;
}
