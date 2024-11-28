package com.bitsflea.model;

import java.util.Map;

import io.nuls.contract.sdk.Address;

/**
 * 评审员记录
 */
public class Reviewer {
    /**
     * 评审员地址
     */
    public Address uid;
    /**
     * 创建时间
     */
    public long createTime;
    /**
     * 最后活动时间
     */
    public long lastActiveTime;
    /**
     * 赞成票数
     */
    public int approveCount;
    /**
     * 反对票
     */
    public int againstCount;
    /**
     * 已经参与投票的用户
     * Map<地址hashCode,bool>
     */
    public Map<Integer, Boolean> voted;
}
