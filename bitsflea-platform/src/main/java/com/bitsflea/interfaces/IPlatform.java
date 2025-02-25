package com.bitsflea.interfaces;

import java.math.BigInteger;
import java.util.Map;

import com.bitsflea.model.Arbitration;
import com.bitsflea.model.Coin;
import com.bitsflea.model.Global;
import com.bitsflea.model.ProductAudit;
import com.bitsflea.model.Reviewer;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.MultyAssetValue;
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
     * @param reasons  评审备忘(可以是存储于IPFS的一个json)
     */
    void review(BigInteger pid, boolean isDelist, String reasons);

    /**
     * 清理过期未支付订单,这个方法任何人可以调用
     * 在清理时，下单人将会被扣除信用分
     * 根据合约计算资源每一次只会清理一部分
     * 调用者会得到积分奖励
     */
    void cleanOrder();

    /**
     * 申请仲裁
     * 
     * @param defendant   被告地址
     * @param pid         商品id
     * @param orderId     订单id
     * @param type        仲裁类型
     * @param description 仲裁内容描述(存储于IPFS的json)
     */
    void applyArbit(Address defendant, BigInteger pid, BigInteger orderId, short type, String description);

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
     */
    void updateArbit(BigInteger id, String proofContent);

    /**
     * 为仲裁投票
     * 
     * @param id    仲裁记录id
     * @param agree true 表示支持原告，false表示支持被告
     */
    void voteArbit(BigInteger id, boolean agree);

    /**
     * 存入积分到工资池
     * 
     * @param amount
     */
    void depositSalaryPool(BigInteger amount);

    /**
     * 存入积分到引荐奖励池
     * 
     * @param amount
     */
    void depositRefPool(BigInteger amount);

    /**
     * 存入积分到系统奖励池
     * 
     * @param amount
     */
    void depositSysPool(BigInteger amount);

    /**
     * 获取指定id的商品审核记录
     * 
     * @param id
     * @return
     */
    ProductAudit getProductAudit(BigInteger id);

    /**
     * 获取指定id的评审员
     * 
     * @param uid
     * @return
     */
    Reviewer getReviewer(Address uid);

    /**
     * 获取全局配置信息
     * 
     * @return
     */
    Global getGlobal();

    /**
     * 获取支持的token信息
     * 
     * @return
     */
    Map<String, Coin> getCoins();

    /**
     * 获取平台收入的token信息
     * 
     * @return
     */
    Map<String, MultyAssetValue> getIncomeTokens();

    /**
     * 获取指定id的仲裁记录
     * 
     * @param id
     * @return
     */
    Arbitration getArbit(BigInteger id);

    /**
     * 获取所有仲裁记录
     * 
     * @return
     */
    Map<BigInteger, Arbitration> getArbits();

    /**
     * 检查电话是否注册
     * 
     * @param phoneHash
     * @return
     */
    boolean checkPhone(String phoneHash);

    /**
     * 添加要支持的新asset,或者设置rate
     * 平台所有者才能调用
     * 
     * @param assetChainId
     * @param assetId
     * @param rate         制成比例
     */
    void addCoin(Integer assetChainId, Integer assetId, short rate);

    /**
     * 生成一个新的仲裁id
     * 
     * @param plaintiff 原告人地址
     * @param defendant 被告人地址
     * @return
     */
    BigInteger newArbitId(Address plaintiff, Address defendant);

    /**
     * 获取平台积分地址
     * 
     * @return
     */
    Address getPoint();

    /**
     * 设置最大评审员数量
     * 只有合约所有者能操作
     * 
     * @param count
     */
    void setReviewMaxCount(Integer count);

    /**
     * 设置平台佣金比例与引荐佣金比例
     * 
     * @param feeRate     平台佣金比例
     * @param refCommRate 引荐佣金比例
     */
    void setRate(Short feeRate, Short refCommRate);

    /**
     * 设置是否开始交易奖励
     * 
     * @param open true表示开启,默认关闭
     */
    void setTradeReward(Boolean open);

    /**
     * 设置奖励数值
     * 
     * @param refAward     引荐奖励
     * @param publishAward 发布商品奖励
     * @param voteAward    投票奖励
     * @param clearAward   清理数据奖励
     */
    void setAwards(BigInteger refAward, BigInteger publishAward, BigInteger voteAward, BigInteger clearAward);

    /**
     * 设置评审奖励数值
     * 
     * @param reviewSalaryProduct 审核商品奖励
     * @param reviewSalaryDispute 处理纠纷奖励
     */
    void setSalary(BigInteger reviewSalaryProduct, BigInteger reviewSalaryDispute);
}
