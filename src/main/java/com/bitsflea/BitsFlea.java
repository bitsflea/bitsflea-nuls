package com.bitsflea;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;
import static io.nuls.contract.sdk.Utils.assetDecimals;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.bitsflea.events.CancelOrderEvent;
import com.bitsflea.events.ConfirmReceiptEvent;
import com.bitsflea.events.CreateOrderEvent;
import com.bitsflea.events.DelistProductEvent;
import com.bitsflea.events.PayOrderEvent;
import com.bitsflea.events.PublishProductEvent;
import com.bitsflea.events.RegUserEvent;
import com.bitsflea.events.ReturnEvent;
import com.bitsflea.events.ShipmentsEvent;
import com.bitsflea.interfaces.IMarket;
import com.bitsflea.interfaces.INRC1363Receiver;
import com.bitsflea.interfaces.IUser;
import com.bitsflea.model.Global;
import com.bitsflea.model.Order;
import com.bitsflea.model.Product;
import com.bitsflea.model.ProductReturn;
import com.bitsflea.model.Reviewer;
import com.bitsflea.model.Coin;
import com.bitsflea.model.User;
import com.bitsflea.utils.Helper;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Block;
import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.MultyAssetValue;
import io.nuls.contract.sdk.annotation.JSONSerializable;
import io.nuls.contract.sdk.annotation.Payable;
import io.nuls.contract.sdk.annotation.PayableMultyAsset;
import io.nuls.contract.sdk.annotation.View;
import io.nuls.contract.sdk.event.DebugEvent;
import io.nuls.contract.sdk.token.NRC20Wrapper;

/**
 * 部署前必须先部署平台积分合约
 */
public class BitsFlea extends Ownable implements Contract, IUser, IMarket, INRC1363Receiver {

    /**
     * 平台积分小数位数
     */
    private int pointDecimals = 8;
    /**
     * 平台积分NRC20
     */
    private NRC20Wrapper point;
    /**
     * 全局数据配置
     */
    private Global global;
    /**
     * 商品数据
     */
    private Map<BigInteger, Product> products;
    /**
     * 订单数据
     */
    private Map<BigInteger, Order> orders;
    /**
     * 用户数据
     */
    private Map<Address, User> users;
    /**
     * 评审员数据
     */
    private Map<Address, Reviewer> reviewers;
    /**
     * 退货数据
     */
    private Map<BigInteger, ProductReturn> returnList;
    /**
     * 受支持的token
     */
    private Map<String, Coin> coins;
    /**
     * 平台总收入
     */
    private Map<String, MultyAssetValue> incomeTokens;

    /**
     * 用于计算比例的分母
     */
    private static final BigInteger DENOMINATOR = BigInteger.valueOf(1000);
    /**
     * 定义链id
     */
    private static final int CHAIN_ID = 2;

    public BitsFlea() {
        global = new Global();
        products = new HashMap<BigInteger, Product>();
        users = new HashMap<Address, User>();
        reviewers = new HashMap<Address, Reviewer>();
        orders = new HashMap<BigInteger, Order>();
        returnList = new HashMap<BigInteger, ProductReturn>();

        coins = new HashMap<String, Coin>();
        coins.put("0-0", new Coin(0, 0, (short) 50));

        point = new NRC20Wrapper(new Address("tNULSeBaN882J8VivXbkGiHdEzq4FGip1t1DWt"));
        pointDecimals = point.decimals();

        incomeTokens = new HashMap<String, MultyAssetValue>();
    }

    @View
    @JSONSerializable
    public Global getGlobal() {
        return global;
    }

    @View
    @JSONSerializable
    public ProductReturn getProductReturn(BigInteger oid) {
        return returnList.get(oid);
    }

    @View
    @JSONSerializable
    public Order getOrder(BigInteger oid) {
        return orders.get(oid);
    }

    @View
    @JSONSerializable
    public User getUser(Address uid) {
        return users.get(uid);
    }

    @View
    @JSONSerializable
    public Reviewer getReviewer(Address uid) {
        return reviewers.get(uid);
    }

    @View
    @JSONSerializable
    public Product geProduct(BigInteger pid) {
        return products.get(pid);
    }

    /**
     * 检查指定用户是否已经被锁定
     * 
     * @param user
     * @return 返回true为已经锁定
     */
    private boolean isLock(User user) {
        return user.status == User.UserStatus.LOCK;
    }

    /**
     * 检查指定用户是否已经被锁定
     * 
     * @param uid 用户地址
     * @return 返回true为已经锁定
     */
    @View
    public boolean isLock(Address uid) {
        require(users.containsKey(uid), Error.USER_NOT_EXIST);
        return isLock(users.get(uid));
    }

    /**
     * 生成一个新的商品id
     * 
     * @return 返回生成的商品id
     */
    @View
    public BigInteger newProductId() {
        return BigInteger.valueOf(Msg.sender().hashCode()).shiftLeft(64).or(BigInteger.valueOf(Block.timestamp()));
    }

    /**
     * 生成一个新的订单id
     * 
     * @param pid 商品id
     * @return 返回生成的订单id
     */
    @View
    public BigInteger newOrderId(BigInteger pid) {
        BigInteger oid = BigInteger.valueOf(Msg.sender().hashCode());
        oid = oid.shiftLeft(96).or(pid);
        oid = oid.shiftLeft(64).or(BigInteger.valueOf(Block.timestamp()));
        return oid;
    }

    @Override
    public void regUser(String nickname, String phoneHash, String phoneEncrypt, Address referrer, String head) {
        Address uid = Msg.sender();
        require(!users.containsKey(uid), Error.ALREADY_REGISTERED);
        User user = new User();
        user.nickname = nickname;
        user.phoneHash = phoneHash;
        user.phoneEncrypt = phoneEncrypt;
        if (referrer != null) {
            user.referrer = referrer;
        }
        user.uid = uid;
        user.head = head;
        user.isReviewer = false;
        user.creditValue = global.creditBaseScore;
        user.lastActiveTime = Block.timestamp();
        user.status = User.UserStatus.NONE;
        user.encryptKey = Msg.senderPublicKey();
        users.put(uid, user);

        global.totalUsers += 1;

        emit(new RegUserEvent(uid, nickname, phoneEncrypt));
    }

    @Override
    public void setProfile(String nickname, String head) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        User user = users.get(uid);
        if (nickname != null && !nickname.isEmpty()) {
            user.nickname = nickname;
        }
        if (head != null && !head.isEmpty()) {
            user.head = head;
        }
    }

    @Override
    public void appReviewer() {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        require(!reviewers.containsKey(uid), Error.ALREADY_REVIEWER);
        Reviewer reviewer = new Reviewer();
        reviewer.uid = uid;
        reviewer.createTime = Block.timestamp();
        reviewer.lastActiveTime = Block.timestamp();
        reviewer.approveCount = 0;
        reviewer.againstCount = 0;
        reviewer.voted = new HashMap<Integer, Boolean>();
        reviewers.put(uid, reviewer);
    }

    @Override
    public void publish(BigInteger pid, int category, String description, boolean isNew, boolean isRetail,
            boolean isReturns,
            String position, short saleMethod, int stockCount, short pickupMethod, String postage, String price) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        Helper.checkProductId(pid, uid);
        require(!products.containsKey(pid), Error.PRODUCT_ALREADY_EXISTS);
        require(stockCount > 0, Error.TOO_LITTLE_INVENTORY);
        require(Product.SaleMethod.isValid(saleMethod), Error.INVALID_SALE_METHOD);
        require(Product.PickupMethod.isValid(pickupMethod), Error.INVALID_PICKUP_METHOD);

        Product product = new Product();
        product.postage = Helper.parseAsset(postage);
        product.price = Helper.parseAsset(price);
        require(product.price.getAssetChainId() == product.postage.getAssetChainId()
                && product.price.getAssetId() == product.postage.getAssetId(), Error.INCONSISTENT_PAYMENT_METHODS);
        require(product.postage.getValue().compareTo(BigInteger.ZERO) >= 0
                && product.price.getValue().compareTo(BigInteger.ZERO) >= 0, Error.INVALID_AMOUNT);

        product.uid = uid;
        product.pid = pid;
        product.category = category;
        product.description = description;
        product.isNew = isNew;
        product.isRetail = isRetail;
        product.isReturns = isReturns;
        product.position = position;
        product.saleMethod = saleMethod;
        product.status = Product.ProductStatus.PUBLISH;
        product.stockCount = stockCount;
        product.pickupMethod = pickupMethod;
        product.publishTime = Block.timestamp();

        this.products.put(product.pid, product);

        emit(new PublishProductEvent(pid, product.uid));
    }

    @Override
    public void delist(BigInteger pid) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        Helper.checkProductId(pid, uid);
        require(products.containsKey(pid), Error.PRODUCT_DOES_NOT_EXIST);
        Product product = products.get(pid);
        require(product.status == Product.ProductStatus.NORMAL, Error.PRODUCT_NOT_NORMAL_STATUS);
        require(product.uid == uid, Error.PRODUCT_IS_NOT_YOURS);

        product.status = Product.ProductStatus.DELISTED;

        emit(new DelistProductEvent(pid, uid));
    }

    @Override
    public void placeOrder(BigInteger pid, BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        Helper.checkProductId(pid, uid);
        Helper.checkOrderId(orderId, pid, uid);

        require(products.containsKey(pid), Error.INVALID_PRODUCT_ID);
        Product product = products.get(pid);
        require(product.status == Product.ProductStatus.NORMAL, Error.PRODUCT_NOT_NORMAL_STATUS);
        require(product.uid != uid, Error.PRODUCT_CANT_BUY_YOUR_OWN);

        Order order = new Order();
        order.oid = orderId;
        order.pid = pid;
        order.seller = product.uid;
        order.buyer = uid;
        order.amount = product.price;
        order.postage = product.postage;
        order.status = Order.OrderStatus.OS_PENDING_PAYMENT;
        order.createTime = Block.timestamp();
        order.payTimeOut = Block.timestamp() + global.payTimeOut;
        orders.put(orderId, order);

        product.status = Product.ProductStatus.LOCKED;

        emit(new CreateOrderEvent(orderId, pid, order.seller, order.buyer));
    }

    @Override
    public void cancelOrder(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);

        BigInteger pid = Helper.getPidByOrderId(orderId);
        require(products.containsKey(pid), Error.INVALID_PRODUCT_ID);

        Order order = orders.get(orderId);
        require(order.buyer == uid, Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_PAYMENT, Error.INVALID_ORDER_STATUS);

        Product product = products.get(pid);
        product.status = Product.ProductStatus.NORMAL;

        orders.remove(orderId);

        emit(new CancelOrderEvent(orderId, pid, uid));
    }

    @Override
    public void shipments(BigInteger orderId, String number) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);
        require(number.length() <= 64, Error.INVALID_WAYBILL_NUMBER);

        Order order = orders.get(orderId);
        require(order.seller == uid, Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_SHIPMENT, Error.INVALID_ORDER_STATUS);

        order.shipmentNumber = number;
        order.shipTime = Block.timestamp();
        order.status = Order.OrderStatus.OS_PENDING_RECEIPT;
        order.receiptTimeOut = Block.timestamp() + global.receiptTimeOut;

        // 处理信用分
        if (Block.timestamp() > order.shipTimeOut) {
            subCredit(uid, global.creditShipmentsTimeout);
        }

        emit(new ShipmentsEvent(orderId, number));
    }

    @Override
    public void reShipments(BigInteger orderId, String number) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId) && returnList.containsKey(orderId), Error.INVALID_ORDER_ID);
        require(number.length() <= 64, Error.INVALID_WAYBILL_NUMBER);

        Order order = orders.get(orderId);
        require(order.buyer == uid, Error.ORDER_IS_NOT_YOURS);

        ProductReturn pr = returnList.get(orderId);
        require(pr.status == ProductReturn.ReturnStatus.RS_PENDING_SHIPMENT, Error.INVALID_RETURN_STATUS);

        pr.shipmentsNumber = number;
        pr.shipTime = Block.timestamp();
        pr.receiptTimeOut = Block.timestamp() + global.receiptTimeOut;
        pr.status = ProductReturn.ReturnStatus.RS_PENDING_RECEIPT;

        // 处理信用分
        if (Block.timestamp() > pr.shipTimeOut) {
            subCredit(uid, global.creditShipmentsTimeout);
        }

        emit(new ShipmentsEvent(orderId, number));
    }

    @Override
    public void confirmReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);

        Order order = orders.get(orderId);
        require(order.buyer == uid, Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_RECEIPT, Error.INVALID_ORDER_STATUS);

        order.endTime = Block.timestamp();
        order.receiptTime = Block.timestamp();
        order.status = Order.OrderStatus.OS_COMPLETED; // 先修改状态，防止重入

        Product product = products.get(order.pid);
        product.status = Product.ProductStatus.COMPLETED; // 如果是可以零售时，在这里扣除锁定数量

        completeOrder(order);

        emit(new ConfirmReceiptEvent(orderId, order.seller, order.buyer));
    }

    @Override
    public void reConfirmReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId) && returnList.containsKey(orderId), Error.INVALID_ORDER_ID);

        Order order = orders.get(orderId);
        require(order.seller == uid, Error.ORDER_IS_NOT_YOURS);

        order.status = Order.OrderStatus.OS_CANCELLED;

        ProductReturn pr = returnList.get(orderId);
        require(pr.status == ProductReturn.ReturnStatus.RS_PENDING_RECEIPT, Error.INVALID_RETURN_STATUS);

        pr.receiptTime = Block.timestamp();
        pr.status = ProductReturn.ReturnStatus.RS_COMPLETED;
        pr.endTime = Block.timestamp();

        Product product = products.get(order.pid);
        product.status = Product.ProductStatus.NORMAL; // TODO: 如果是零售，这里可以还原数量

        // 处理信用分
        if (Block.timestamp() > pr.receiptTimeOut) {
            subCredit(order.seller, global.creditConfirmReceiptTimeout);
        }
        // 退款(合约中的邮费退给卖家，即两次邮费各承担一次)
        transfer(order.buyer, order.amount);
        transfer(order.seller, order.postage);
    }

    @Override
    public void returns(BigInteger orderId, String reasons) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);
        require(reasons.length() <= 300, Error.INVALID_REASONS);

        Order order = orders.get(orderId);
        require(order.status == Order.OrderStatus.OS_PENDING_RECEIPT, Error.INVALID_ORDER_STATUS);

        require(products.containsKey(order.pid), Error.INVALID_PRODUCT_ID);
        Product product = products.get(order.pid);
        require(product.isReturns, Error.PRODUCT_NOT_SUPPORT_RETURNS);

        order.status = Order.OrderStatus.OS_RETURN;

        ProductReturn pr = new ProductReturn();
        pr.oid = orderId;
        pr.pid = order.pid;
        pr.status = ProductReturn.ReturnStatus.RS_PENDING_SHIPMENT;
        pr.reasons = reasons;
        pr.createTime = Block.timestamp();
        pr.shipTimeOut = Block.timestamp() + global.shipTimeOut;
        returnList.put(orderId, pr);

        emit(new ReturnEvent(orderId, order.pid, uid));
    }

    @Override
    public void deferReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);

        Order order = orders.get(orderId);
        require(order.buyer == uid, Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_RECEIPT, Error.INVALID_ORDER_STATUS);
        require(order.delayedCount < global.maxDeferrTimes, Error.NO_FURTHER_EXTENSION);

        order.delayedCount += 1;
        order.receiptTimeOut = Block.timestamp() + global.receiptTimeOut;
    }

    @Override
    public void reDeferReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId) && returnList.containsKey(orderId), Error.INVALID_ORDER_ID);
        Order order = orders.get(orderId);
        require(order.seller == uid, Error.ORDER_IS_NOT_YOURS);

        ProductReturn pr = returnList.get(orderId);
        require(pr.status == ProductReturn.ReturnStatus.RS_PENDING_RECEIPT, Error.INVALID_RETURN_STATUS);
        require(pr.delayedCount < global.maxDeferrTimes, Error.NO_FURTHER_EXTENSION);

        pr.delayedCount += 1;
        pr.receiptTimeOut = Block.timestamp() + global.receiptTimeOut;
    }

    @Payable
    @PayableMultyAsset
    public void payOrder(BigInteger orderId) {
        BigInteger amount = Msg.value();
        if (amount.compareTo(BigInteger.ZERO) > 0) {
            MultyAssetValue aValue = new MultyAssetValue(amount, CHAIN_ID, 1);
            payOrder(orderId, aValue);
            return;
        }
        MultyAssetValue[] aValues = Msg.multyAssetValues();
        if (aValues.length > 0 && aValues[0].getValue().compareTo(BigInteger.ZERO) > 0) {
            payOrder(orderId, aValues[0]);
            return;
        }
    }

    @Override
    public boolean onTransferReceived(Address operator, Address from, BigInteger value, String remark) {
        if (Msg.sender().equals(point.getNrc20Token())) {
            BigInteger orderId = new BigInteger(remark);
            MultyAssetValue aValue = new MultyAssetValue(value, 0, 0);
            payOrder(orderId, aValue);
            return true;
        }
        return false;
    }

    /************************************
     * 私有方法
     ********************************************/
    /**
     * 支付订单(私有方法)
     * 
     * @param orderId
     * @param value
     */
    private void payOrder(BigInteger orderId, MultyAssetValue value) {
        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);

        Order order = orders.get(orderId);
        require(order.status == Order.OrderStatus.OS_PENDING_PAYMENT, Error.INVALID_ORDER_STATUS);
        require(order.amount.getAssetChainId() == value.getAssetChainId() &&
                order.amount.getAssetId() == value.getAssetId(), Error.INVALID_ASSET);
        require(order.amount.getValue().compareTo(value.getValue()) == 0, Error.INVALID_AMOUNT);

        order.status = Order.OrderStatus.OS_PENDING_SHIPMENT;
        order.payTime = Block.timestamp();
        order.shipTimeOut = Block.timestamp() + global.shipTimeOut;

        if (Block.timestamp() > order.payTimeOut) {
            subCredit(order.buyer, global.creditPayTimeOut);
        } else {
            addCredit(order.buyer, global.creditPay);
        }

        emit(new PayOrderEvent(orderId, value.getValue(), Msg.sender()));
    }

    /**
     * 完成订单的相关处理逻辑
     * 
     * @param order 订单对象
     */
    private void completeOrder(Order order) {
        User seller = users.get(order.seller);
        User buyer = users.get(order.buyer);
        if (seller != null && seller.uid == order.seller) {
            seller.sellTotal += 1;
        }
        if (buyer != null && buyer.uid == order.buyer) {
            buyer.buyTotal += 1;
        }

        // 处理信用分
        addCredit(seller, global.creditCompleteTransaction);
        addCredit(buyer, global.creditCompleteTransaction);
        if (order.receiptTimeOut < Block.timestamp()) {
            subCredit(buyer, global.creditConfirmReceiptTimeout);
        }

        settlement(order, seller, buyer);

        tradeRewardPoints(order, seller, buyer);
    }

    /**
     * 结算订单相关逻辑
     * 
     * @param order  订单对象
     * @param seller 卖家用户对象
     * @param buyer  买家用户对象
     */
    private void settlement(Order order, User seller, User buyer) {
        BigInteger total = order.amount.getValue().add(order.postage.getValue());
        BigInteger income = total.multiply(BigInteger.valueOf(global.feeRate)).divide(DENOMINATOR);
        BigInteger amount = total.subtract(income);
        if (buyer.referrer != null && !isLock(buyer.referrer)) {
            BigInteger refcomm = income.multiply(BigInteger.valueOf(global.refCommRate)).divide(DENOMINATOR);
            income = income.subtract(refcomm);
            // 转给引荐人
            transfer(buyer.referrer, refcomm, order.amount.getAssetChainId(), order.amount.getAssetId());
        }
        // 转给卖家
        transfer(seller.uid, amount, order.amount.getAssetChainId(), order.amount.getAssetId());
        // 转给佣金地址
        transfer(global.commission, income, order.amount.getAssetChainId(), order.amount.getAssetId());
        // 记录收益
        String tokenKey = Coin.getTokenKey(order.amount.getAssetChainId(), order.amount.getAssetId());
        if (incomeTokens.containsKey(tokenKey)) {
            MultyAssetValue asset = incomeTokens.get(tokenKey);
            BigInteger value = asset.getValue();
            asset.setValue(value.add(income));
        } else {
            MultyAssetValue asset = new MultyAssetValue(income, order.amount.getAssetChainId(),
                    order.amount.getAssetId());
            incomeTokens.put(tokenKey, asset);
        }
    }

    /**
     * 转出资产
     * 
     * @param to           接收资产的地址
     * @param amount       数量
     * @param assetChainId 资产链id
     * @param assetId      资产id
     */
    private void transfer(Address to, BigInteger amount, int assetChainId, int assetId) {
        if (assetChainId == 0) {
            boolean result = point.transfer(to, amount);
            require(result, Error.FAILED_TRANSFER_POINTS);
        } else {
            to.transfer(amount, assetChainId, assetId);
        }
    }

    /**
     * 转出资产
     * 
     * @param to     接收资产的地址
     * @param amount 资产
     */
    private void transfer(Address to, MultyAssetValue amount) {
        transfer(to, amount.getValue(), amount.getAssetChainId(), amount.getAssetId());
    }

    /**
     * 扣取信用分
     * 
     * @param user  用户对象
     * @param value 要扣除的分值
     */
    private void subCredit(User user, int value) {
        user.creditValue -= value < 0 ? -value : value;
        if (user.creditValue < 0) {
            user.creditValue = 0;
        }
        // 处理评审员
        if (user.creditValue < global.creditReviewerLimit && user.isReviewer) {
            user.isReviewer = false;
            reviewers.remove(user.uid);
        }
        // 处理锁定账号
        if (user.creditValue <= 0) {
            user.status = User.UserStatus.LOCK;
        }
    }

    /**
     * 扣取信用分
     * 
     * @param uid   用户地址
     * @param value 要扣除的分值
     */
    private void subCredit(Address uid, int value) {
        User user = users.get(uid);
        subCredit(user, value);
    }

    /**
     * 添加信用分
     * 
     * @param user  用户对象
     * @param value 分值
     */
    private void addCredit(User user, int value) {
        user.creditValue += value < 0 ? -value : value;
    }

    private void addCredit(Address uid, int value) {
        User user = users.get(uid);
        addCredit(user, value);
    }

    /**
     * 完成交易奖励积分
     * 
     * @param order  订单对象
     * @param seller 卖家对象
     * @param buyer  买家对象
     */
    private void tradeRewardPoints(Order order, User seller, User buyer) {
        int decimals;
        if (order.amount.getAssetChainId() == 0) {
            decimals = pointDecimals;
        } else {
            decimals = assetDecimals(order.amount.getAssetChainId(), order.amount.getAssetId());
        }
        String key = Coin.getTokenKey(order.amount.getAssetChainId(), order.amount.getAssetId());
        BigInteger rate = BigInteger.valueOf(coins.get(key).transactionAwardRate);
        BigInteger val = order.amount.getValue();
        val = val.multiply(rate).divide(DENOMINATOR); // 计算比率
        if (decimals > pointDecimals) {
            val = val.divide(BigInteger.valueOf(10).pow(decimals - pointDecimals));
        } else if (pointDecimals > decimals) {
            val = val.multiply(BigInteger.valueOf(10).pow(pointDecimals - decimals));
        }
        if (val.compareTo(BigInteger.valueOf(1)) > 0
                && global.sysPool.compareTo(val.multiply(BigInteger.valueOf(2))) >= 0) {
            global.sysPool = global.sysPool.subtract(val.multiply(BigInteger.valueOf(2)));
            point.transfer(seller.uid, val);
            point.transfer(buyer.uid, val);
        }
    }
}