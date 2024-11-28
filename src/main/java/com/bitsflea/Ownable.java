package com.bitsflea;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.View;
import lombok.AllArgsConstructor;
import lombok.Data;

import static io.nuls.contract.sdk.Utils.require;
import static io.nuls.contract.sdk.Utils.emit;

public class Ownable {

    /**
     * 合约归属地址
     */
    protected Address owner;

    public Ownable() {
        this.owner = Msg.sender();
    }

    @View
    public Address viewOwner() {
        return owner;
    }

    protected void onlyOwner() {
        require(Msg.sender().equals(owner), Error.ONLY_OWNER_EXECUTE);
    }

    /**
     * 转让合约所有权
     *
     * @param newOwner
     */
    public void transferOwnership(Address newOwner) {
        onlyOwner();
        emit(new TransferOwnershipEvent(owner, newOwner));
        owner = newOwner;
    }

    /**
     * 放弃合约
     */
    public void renounceOwnership() {
        onlyOwner();
        emit(new TransferOwnershipEvent(owner, null));
        owner = null;
    }

    /**
     * 所有权转移事件
     */
    @Data
    @AllArgsConstructor
    class TransferOwnershipEvent implements Event {
        // 先前拥有者
        private Address previousOwner;
        // 新的拥有者
        private Address newOwner;
    }

}
