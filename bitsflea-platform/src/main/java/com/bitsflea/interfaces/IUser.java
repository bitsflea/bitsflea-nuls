package com.bitsflea.interfaces;

import java.util.List;

import com.bitsflea.model.User;

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
     * @param extendInfo   用户扩展信息
     */
    void regUser(@Required String nickname,
            @Required String phoneHash,
            @Required String phoneEncrypt,
            Address referrer,
            String head,
            String extendInfo);

    /**
     * 设置用户信息
     * 
     * @param nickname 昵称
     * @param head     头像
     */
    void setProfile(String nickname, String head, String extendInfo);

    /**
     * 申请成为评审员
     */
    void appReviewer();

    /**
     * 获取指定id的用户
     * 
     * @param uid
     * @return
     */
    User getUser(Address uid);

    /**
     * 更新手机号
     * 
     * @param phoneHash
     * @param phoneEncrypt
     */
    void updatePhone(@Required String phoneHash, @Required String phoneEncrypt);

    /**
     * 根据用户地址获取用户
     * @param ids
     * @return
     */
    List<User> getUsersByIds(String[] ids);
}
