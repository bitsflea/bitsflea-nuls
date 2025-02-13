package com.bitsflea.interfaces;

import java.math.BigInteger;
import io.nuls.contract.sdk.Address;

public interface INRC1363Spender {
    boolean onApprovalReceived(Address owner, BigInteger value, String data);
}
