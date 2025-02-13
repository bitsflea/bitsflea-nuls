package io.nuls.contract.token.interfaces;

import java.math.BigInteger;
import io.nuls.contract.sdk.Address;

public interface INRC1363 {
    boolean transferAndCall(Address to, BigInteger value, String data);

    boolean transferFromAndCall(Address from, Address to, BigInteger value, String data);

    boolean approveAndCall(Address spender, BigInteger value, String data);
}
