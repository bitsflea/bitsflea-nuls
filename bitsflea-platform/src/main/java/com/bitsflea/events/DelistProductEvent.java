package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 下架商品事件
 */
@Data
@AllArgsConstructor
public class DelistProductEvent implements Event {
    /**
     * 商品id
     */
    private BigInteger pid;
    /**
     * 商品所属人地址
     */
    private Address uid;
}
