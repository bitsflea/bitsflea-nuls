package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 审核商品事件
 */
@Data
@AllArgsConstructor
public class ReviewProductEvent implements Event {
    /**
     * 商品id
     */
    private BigInteger pid;
    /**
     * 审核人地址
     */
    private Address reviewer;
    /**
     * 是否下架商品
     */
    private boolean isDelist;
    /**
     * 评审时间戳
     */
    private long reviewTime;
}
