package com.bitsflea.events;

import java.math.BigInteger;

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
     * 订单号
     */
    private BigInteger oid;
    /**
     * 状态
     */
    private short status;
    /**
     * 完成时间
     */
    private long time;

}
