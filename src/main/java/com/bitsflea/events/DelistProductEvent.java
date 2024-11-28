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
    private BigInteger pid;
    private Address uid;
}
