package com.bitsflea.events;

import java.math.BigInteger;

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
     * 新的状态
     */
    private short status;
    /**
     * 支付时间
     */
    private long payTime;
    /**
     * 发货超时时间
     */
    private long shipTimeOut;
}
