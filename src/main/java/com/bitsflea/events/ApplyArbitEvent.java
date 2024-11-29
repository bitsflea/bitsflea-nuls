package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 申请仲裁事件
 */
@Data
@AllArgsConstructor
public class ApplyArbitEvent implements Event {
    /**
     * 仲裁id
     */
    private BigInteger aid;
    /**
     * 订单id
     */
    private BigInteger oid;
    /**
     * 商品id
     */
    private BigInteger pid;
}
