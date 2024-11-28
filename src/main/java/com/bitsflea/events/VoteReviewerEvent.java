package com.bitsflea.events;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 为评审员投票事件
 */
@Data
@AllArgsConstructor
public class VoteReviewerEvent implements Event {
    /**
     * 投票人
     */
    private Address voter;
    /**
     * 评审员
     */
    private Address reviewer;
    /**
     * 赞成票数
     */
    private int approveCount;
    /**
     * 反对票数
     */
    private int againstCount;
}
