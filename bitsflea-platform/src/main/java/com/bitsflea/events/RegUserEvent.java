package com.bitsflea.events;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 注册新用户事件
 */
@Data
@AllArgsConstructor
public class RegUserEvent implements Event {
    /**
     * 用户地址
     */
    private Address uid;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 加密用公钥
     */
    private String encryptKey;
}
