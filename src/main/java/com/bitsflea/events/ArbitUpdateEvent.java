package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 仲裁更新事件
 */
@Data
@AllArgsConstructor
public class ArbitUpdateEvent implements Event {
    /**
     * 仲裁id
     */
    private BigInteger aid;
    /**
     * 新的状态
     */
    private short status;
    /**
     * 操作者地址
     */
    private Address operator;
}
