package com.bitsflea.model;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;

public class Global {
    /**
     * 注册用户总数
     */
    public int totalUsers;
    /**
     * 交易佣金费率(平台收取),100%=1000
     */
    public short feeRate = 50;
    /**
     * 引荐佣金率,100%=1000
     * 引荐佣金从平台抽佣中支出
     */
    public short refCommRate = 50;
    /**
     * 佣金存放地址
     */
    public Address commission;

    /*************************** 积分相关 ***************************/
    /**
     * 工资池
     */
    public BigInteger salaryPool = BigInteger.ZERO;
    /**
     * 引荐奖励池
     */
    public BigInteger refPool = BigInteger.ZERO;
    /**
     * 系统奖励池
     */
    public BigInteger sysPool = BigInteger.ZERO;
    /**
     * 每引荐一个用户的奖励,100积分
     */
    public BigInteger refAward = new BigInteger("10000000000");
    /**
     * 每发布一个商品的奖励
     */
    public BigInteger publishAward = new BigInteger("5000000000");
    /**
     * 每参与一次投票的奖励
     */
    public BigInteger voteAward = new BigInteger("1000000000");
    /**
     * 调用清理数据一次的奖励
     */
    public BigInteger clearAward = new BigInteger("1000000000");

    /*************************** 信用分相关 *******************************/
    /**
     * 基础信用分
     */
    public int creditBaseScore = 500;
    /**
     * 参与引荐的信用分下限
     */
    public int creditRefLimit = 500;
    /**
     * 参与评审员的信用分下限
     */
    public int creditReviewerLimit = 500;
    /**
     * 完成交易加分
     */
    public int creditCompleteTransaction = 5;
    /**
     * 确认收货超时扣分
     */
    public int creditConfirmReceiptTimeout = 5;
    /**
     * 发货超时扣分
     */
    public int creditShipmentsTimeout = 5;
    /**
     * 支付超时扣分
     */
    public int creditPayTimeOut = 5;
    /**
     * 按时支付加分
     */
    public int creditPay = 2;
    /**
     * 成功发布商品加分
     */
    public int creditPublish = 1;
    /**
     * 发布商品时审核未通过扣分
     */
    public int creditInvalidPublish = 5;
    /**
     * 仲裁失败方扣分
     */
    public int arbitLosing = 100;

    /****************************** 评审员相关 ********************************/
    /**
     * 至少3名评审员
     */
    public int reviewMinCount = 3;
    /**
     * 在单个仲裁中最多5名
     */
    public int arbitMaxCount = 5;
    /**
     * 最多3000名评审员
     */
    public int reviewMaxCount = 3000;
    /**
     * 评审一个商品的薪资
     * 积分精度为8, 所以等于50积分
     */
    public BigInteger reviewSalaryProduct = new BigInteger("5000000000");
    /**
     * 处理一个纠纷的薪资
     */
    public BigInteger reviewSalaryDispute = new BigInteger("20000000000");

    /********************************* 其他 **************************************/
    /**
     * 支付超时时间(秒)
     */
    public int payTimeOut = 8 * 60 * 60;
    /**
     * 发货超时时间(秒)
     */
    public int shipTimeOut = 24 * 60 * 60;
    /**
     * 确认收货超时时间(秒)
     */
    public int receiptTimeOut = 7 * 24 * 60 * 60;
    /**
     * 最多延期次数
     * 用于延期确认收货
     */
    public int maxDeferrTimes = 3;
    /**
     * 加密用公钥
     */
    public String encryptKey;
    /**
     * 当订单完成或者取消后，只保留的秒数
     */
    public int clearOrderTime = 3 * 24 * 60 * 60;
}
