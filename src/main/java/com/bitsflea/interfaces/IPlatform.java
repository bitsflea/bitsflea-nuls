package com.bitsflea.interfaces;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.annotation.Required;

public interface IPlatform {
    /**
     * 为评审员投票
     * 
     * @param reviewer  评审员地址
     * @param isSupport 是否支持
     */
    void voteReviewer(@Required Address reviewer, @Required boolean isSupport);

    /**
     * 评审员评审商品
     * 
     * @param pid      商品id
     * @param isDelist 是否下架
     * @param memo     评审备忘
     */
    void review(long pid, boolean isDelist, String memo);

    /**
     * 清理过期未支付订单,这个方法任何可以调用
     * 在清理时，下单人将会被扣除信用分
     * 根据合约计算资源每一次只会清理一部分
     */
    void cleanOrder();

    /**
     * 申请仲裁
     * 
     * @param defendant   被告地址
     * @param pid         商品id
     * @param orderId     订单id
     * @param type        仲裁类型
     * @param description 仲裁内容描述
     */
    void applyArbit(Address defendant, long pid, BigInteger orderId, short type, String description);

    /**
     * 评审员参与仲裁
     * 
     * @param id 仲裁记录id
     */
    void inArbit(BigInteger id);

    /**
     * 更新仲裁结果
     * 
     * @param id           仲裁记录id
     * @param proofContent 证明内容
     * @param results      结果描述
     * @param winner       胜诉方地址
     */
    void updateArbit(BigInteger id, String proofContent, String results, Address winner);
}
