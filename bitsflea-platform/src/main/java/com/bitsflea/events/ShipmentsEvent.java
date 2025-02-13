package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 发货事件
 */
@Data
@AllArgsConstructor
public class ShipmentsEvent implements Event {
    /**
     * 订单id
     */
    private BigInteger oid;
    /**
     * 运单号
     */
    private String number;
    /**
     * 订单新状态
     */
    private short status;
    /**
     * 操作时间
     */
    private long time;
}
