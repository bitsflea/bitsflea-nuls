package com.bitsflea.model;

import lombok.AllArgsConstructor;

/**
 * 支持的token
 */
@AllArgsConstructor
 public class Coin {
    /**
     * 链id
     */
    public int chainId;
    /**
     * 资产id
     */
    public int assetId;
    /**
     * 完成交易的奖励率，100%=10000
     */
    public int transactionAwardRate;

    public static String getTokenKey(Integer chainId, Integer assetId) {
        return chainId.toString() + "-" + assetId.toString();
    }
}
