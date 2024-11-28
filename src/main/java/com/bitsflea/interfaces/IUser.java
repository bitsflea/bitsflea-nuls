package com.bitsflea.interfaces;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.annotation.Required;

public interface IUser {
    /**
     * 注册新用户
     * 
     * @param nickname     昵称
     * @param phoneHash    电话号hash
     * @param phoneEncrypt 加密的电话号
     * @param referrer     引荐人地址
     * @param head         用户头像
     */
    void regUser(@Required String nickname,
            @Required String phoneHash,
            @Required String phoneEncrypt,
            Address referrer,
            String head);

    /**
     * 设置用户信息
     * 
     * @param nickname 昵称
     * @param head     头像
     */
    void setProfile(String nickname, String head);

    /**
     * 申请成为评审员
     */
    void appReviewer();
}
