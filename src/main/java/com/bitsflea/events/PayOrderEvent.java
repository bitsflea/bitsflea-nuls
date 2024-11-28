package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 成功支付订单事件
 */
@Data
@AllArgsConstructor
public class PayOrderEvent implements Event {
    /**
     * 订单id
     */
    private BigInteger oid;
    /**
     * 支付金额
     */
    private BigInteger amount;
    /**
     * 支付人
     */
    private Address payer;
}
