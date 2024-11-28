package com.bitsflea.model;

import io.nuls.contract.sdk.Address;

/**
 * 用户记录
 */
public class User {
    /**
     * 用户地址
     */
    public Address uid;
    /**
     * 状态，值对应 UserStatus
     */
    public short status;
    /**
     * 是否是评审员
     */
    public boolean isReviewer;
    /**
     * 昵称
     */
    public String nickname;
    /**
     * 头像
     */
    public String head;
    /**
     * 电话号hash
     */
    public String phoneHash;
    /**
     * 加密过后的电话
     */
    public String phoneEncrypt;
    /**
     * 引荐人地址
     */
    public Address referrer;
    /**
     * 信用分
     */
    public int creditValue;
    /**
     * 最后活跃时间
     */
    public long lastActiveTime;
    /**
     * 发布商品总数量
     */
    public int postsTotal = 0;
    /**
     * 卖出商品总数量
     */
    public int sellTotal = 0;
    /**
     * 买入商品总数量
     */
    public int buyTotal = 0;
    /**
     * 引荐总数量
     */
    public int referralTotal = 0;
    /**
     * 加密用公钥,为地址公钥
     */
    public String encryptKey;

    /**
     * 用户状态
     */
    public static class UserStatus {
        /**
         * 正常
         */
        public static final short NONE = 0;
        /**
         * 锁定
         */
        public static final short LOCK = 1;
    }

}
