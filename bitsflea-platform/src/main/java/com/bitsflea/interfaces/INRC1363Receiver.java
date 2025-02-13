package com.bitsflea.interfaces;

import java.math.BigInteger;

import io.nuls.contract.sdk.Address;

public interface INRC1363Receiver {
    boolean onTransferReceived(Address operator, Address from, BigInteger value, String remark);
}