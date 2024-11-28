package com.bitsflea.events;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 确认收货事件
 */
@Data
@AllArgsConstructor
public class ConfirmReceiptEvent implements Event {
    /**
     * 订单id
     */
    private BigInteger oid;
    /**
     * 卖家
     */
    private Address seller;
    /**
     * 买家
     */
    private Address buyer;

}
