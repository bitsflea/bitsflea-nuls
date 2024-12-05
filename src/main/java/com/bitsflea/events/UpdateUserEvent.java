package com.bitsflea.events;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户更新事件
 */
@Data
@AllArgsConstructor
public class UpdateUserEvent implements Event {
    /**
     * 用户地址
     */
    private Address uid;
}
