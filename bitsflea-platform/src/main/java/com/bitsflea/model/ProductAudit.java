package com.bitsflea.model;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;

/**
 * 审核商品记录
 */
public class ProductAudit {
    
    /**
     * 商品id
     */
    public BigInteger pid;
    /**
     * 审核人地址
     */
    public Address reviewer;
    /**
     * 是否下架
     */
    public boolean isDelist;
    /**
     * 审核备忘内容(存储于IPFS的一个json的url)
     */
    public String details;
    /**
     * 审核提交时间
     */
    public long reviewTime;
}
