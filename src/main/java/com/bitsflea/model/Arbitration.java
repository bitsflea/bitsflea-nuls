package com.bitsflea.model;

import java.math.BigInteger;
import java.util.List;

import io.nuls.contract.sdk.Address;

/**
 * 仲裁记录
 */
public class Arbitration {
    /**
     * 记录id
     * 由原告地址hashCode+被告地址hashCode+时间戳
     */
    public BigInteger id;
    /**
     * 原告地址
     */
    public Address plaintiff;
    /**
     * 被告地址
     */
    public Address defendant;
    /**
     * 商品id
     */
    public long pid;
    /**
     * 订单id
     */
    public BigInteger orderId;
    /**
     * 仲裁类型,值对应: ArbitType
     */
    public short type;
    /**
     * 仲裁状态,值对应: ArbitStatus
     */
    public short status;
    /**
     * 描述，一个存储于IPFS上的json的url
     * 格式：{title:"",description:"",images:[]}
     */
    public String description;
    /**
     * 证明内容,一个存储于IPFS上的json的url
     * 格式：{description:"",images:[]}
     */
    public String proofContent;
    /**
     * 仲裁结果，一个存储于IPFS上的json的url
     * 格式：{description:"",images:[]}
     */
    public String results;
    /**
     * 胜诉方地址
     */
    public Address winner;
    /**
     * 开始时间
     */
    public long startTime;
    /**
     * 结束时间
     */
    public long endTime;
    /**
     * 参与的评审员
     */
    public List<Reviewer> reviewers;

    /**
     * 仲裁类型枚举
     */
    public static class ArbitType {
        /**
         * 订单纠纷
         */
        public static final short AT_ORDER = 0;
        /**
         * 投诉评审员
         */
        public static final short AT_COMPLAINT = 100;
        /**
         * 举报商品
         */
        public static final short AT_PRODUCT = 200;
        /**
         * 举报其他非法信息
         */
        public static final short AT_ILLEGAL_INFO = 300;

        public static boolean isValid(short val) {
            switch (val) {
                case AT_ORDER:
                case AT_COMPLAINT:
                case AT_PRODUCT:
                case AT_ILLEGAL_INFO:
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * 仲裁状态枚举
     */
    public static class ArbitStatus {
        /**
         * 申请中
         */
        public static final short AS_APPLY = 0;
        /**
         * 等待中
         */
        public static final short AS_WAIT = 100;
        /**
         * 处理中
         */
        public static final short AS_PROCESSING = 200;
        /**
         * 已完成
         */
        public static final short AS_COMPLETED = 300;
    }
}
