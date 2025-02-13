package com.bitsflea.events;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 创建评审员事件
 */
@Data
@AllArgsConstructor
public class CreateReviewerEvent implements Event {
    /**
     * 申请人地址
     */
    private Address uid;
    /**
     * 创建时间
     */
    private long createTime;
}
