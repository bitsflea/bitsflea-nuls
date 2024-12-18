package com.bitsflea.utils;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.MultyAssetValue;
import static io.nuls.contract.sdk.Utils.require;
import static io.nuls.contract.sdk.Utils.revert;
import static io.nuls.contract.sdk.Utils.sha3;

import java.math.BigInteger;

import com.bitsflea.Error;

public class Helper {

    /**
     * 返回地址hash的前4个字节(32位)
     * @param addr
     * @return
     */
    public static BigInteger getHashCode(Address addr) {
        String hash = sha3(addr.toString().getBytes());
        BigInteger hc = new BigInteger(hash, 16);
        return hc.shiftRight(224);
    }

    /**
     * 将以豆号分隔的字符串格式化为MultyAssetValue,
     * 当assetChainId=0,assetId=0时表示平台资产(积分)
     * 
     * @param str 格式为：value,assetChainId,assetId
     * @return
     */
    public static MultyAssetValue parseAsset(String str) {
        String[] data = str.split(",");
        require(data.length == 3, Error.PARAMETER_ERROR);
        try {
            return new MultyAssetValue(new BigInteger(data[0]), new Integer(data[1]), new Integer(data[2]));
        } catch (Exception e) {
            revert(Error.PARAMETER_ERROR);
        }
        return null;
    }

    /**
     * 检查商品id的合法性
     * 
     * @param pid 商品id
     * @param uid 发布者地址
     */
    public static void checkProductId(BigInteger pid, Address uid) {
        int uHash = getHashCode(uid).intValue();
        int pHash = pid.shiftRight(64).intValue();
        require(uHash == pHash, Error.PRODUCT_INVALID_ID);
    }

    /**
     * 检查订单id的合法性
     * 
     * @param orderId 订单id
     * @param pid     商品id
     * @param uid     下单者地址
     */
    public static void checkOrderId(BigInteger orderId, BigInteger pid, Address uid) {
        int uHash = getHashCode(uid).intValue();
        int oHash = orderId.shiftRight(160).intValue();
        require(uHash == oHash, Error.INVALID_ORDER_ID);

        BigInteger p1 = getPidByOrderId(orderId);
        require(p1 == pid, Error.INVALID_ORDER_ID);
    }

    /**
     * 从订单id中提取商品id
     * 
     * @param orderId 订单id
     * @return
     */
    public static BigInteger getPidByOrderId(BigInteger orderId) {
        return orderId.shiftRight(64).and(BigInteger.valueOf(2).pow(96).subtract(BigInteger.ONE));
    }
}
