package com.bitsflea;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;
import static io.nuls.contract.sdk.Utils.assetDecimals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bitsflea.events.AddCategoryEvent;
import com.bitsflea.events.ApplyArbitEvent;
import com.bitsflea.events.ArbitUpdateEvent;
import com.bitsflea.events.CancelOrderEvent;
import com.bitsflea.events.CompleteOrderEvent;
import com.bitsflea.events.CreateOrderEvent;
import com.bitsflea.events.CreateReviewerEvent;
import com.bitsflea.events.DelistProductEvent;
import com.bitsflea.events.PayOrderEvent;
import com.bitsflea.events.PublishProductEvent;
import com.bitsflea.events.RegUserEvent;
import com.bitsflea.events.ReturnEvent;
import com.bitsflea.events.ReviewProductEvent;
import com.bitsflea.events.ShipmentsEvent;
import com.bitsflea.events.UpdateUserEvent;
import com.bitsflea.events.VoteReviewerEvent;
import com.bitsflea.interfaces.IMarket;
import com.bitsflea.interfaces.INRC1363Receiver;
import com.bitsflea.interfaces.INRC1363Spender;
import com.bitsflea.interfaces.IPlatform;
import com.bitsflea.interfaces.IUser;
import com.bitsflea.model.Global;
import com.bitsflea.model.Order;
import com.bitsflea.model.Product;
import com.bitsflea.model.Product.Categories;
import com.bitsflea.model.ProductAudit;
import com.bitsflea.model.ProductReturn;
import com.bitsflea.model.Reviewer;
import com.bitsflea.model.Arbitration;
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
import io.nuls.contract.sdk.annotation.Required;
import io.nuls.contract.sdk.annotation.View;
import io.nuls.contract.sdk.token.NRC20Wrapper;

/**
 * 部署前必须先部署平台积分合约
 */
public class BitsFlea extends Ownable
        implements Contract, IPlatform, IUser, IMarket, INRC1363Receiver, INRC1363Spender {

    private static final BigInteger E = BigInteger.valueOf(10);
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
    private LinkedHashMap<BigInteger, Order> orders;
    /**
     * 用户数据
     */
    private Map<Address, User> users;
    /**
     * 评审员数据
     */
    private Map<Address, Reviewer> reviewers;
    /**
     * 商品审核记录
     */
    private Map<BigInteger, ProductAudit> productAudits;
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
     * 仲裁数据
     */
    private Map<BigInteger, Arbitration> arbits;
    /**
     * 已经注册的手机
     */
    private Map<String, Boolean> phones;

    /**
     * 商品类型
     */
    private Map<Integer, Categories> categories;

    /**
     * 是否完成引荐奖励
     */
    private Map<Address, Boolean> hasRefer;

    /**
     * 用于计算比例的分母
     */
    private static final BigInteger DENOMINATOR = BigInteger.valueOf(10000);
    /**
     * 定义链id
     * 部署前必须修改此设置
     */
    private static final int CHAIN_ID = 2;

    public BitsFlea(Address pointAddr) {
        global = new Global();
        global.encryptKey = "0349252f2e857ffac203cfead86e0f5b4156465000aa6e4d25d56be7ccb705a19b";
        global.commission = new Address("NULSd6Hgi72qSbAUm7PePnKnxcN1SkySVjUGM");
        phones = new HashMap<String, Boolean>();
        products = new HashMap<BigInteger, Product>();
        users = new HashMap<Address, User>();
        reviewers = new HashMap<Address, Reviewer>();
        productAudits = new HashMap<BigInteger, ProductAudit>();
        orders = new LinkedHashMap<BigInteger, Order>();
        returnList = new HashMap<BigInteger, ProductReturn>();
        arbits = new HashMap<BigInteger, Arbitration>();

        coins = new HashMap<String, Coin>();
        coins.put("0-0", new Coin(0, 0, 250));

        incomeTokens = new HashMap<String, MultyAssetValue>();
        categories = new HashMap<Integer, Categories>();

        hasRefer = new HashMap<Address, Boolean>();

        require(pointAddr != null);
        point = new NRC20Wrapper(pointAddr);
        pointDecimals = point.decimals();
    }

    /**
     * 添加要支持的新asset,或者设置rate
     * 平台所有者才能调用
     * 
     * @param assetChainId
     * @param assetId
     * @param rate
     */
    @Override
    public void addCoin(Integer assetChainId, Integer assetId, int rate) {
        require(rate >= 0, Error.PARAMETER_ERROR);

        onlyOwner();

        String key = assetChainId.toString() + "-" + assetId.toString();
        if (coins.containsKey(key)) { // 设置rate
            Coin coin = coins.get(key);
            coin.transactionAwardRate = rate;
        } else { // 添加
            Coin coin = new Coin(assetChainId, assetId, rate);
            coins.put(key, coin);
        }
    }

    @View
    @Override
    public boolean checkPhone(String phoneHash) {
        if (phones.containsKey(phoneHash)) {
            return phones.get(phoneHash);
        }
        return false;
    }

    @View
    @JSONSerializable
    @Override
    public Map<BigInteger, Arbitration> getArbits() {
        return arbits;
    }

    @View
    @JSONSerializable
    @Override
    public Arbitration getArbit(BigInteger id) {
        return arbits.get(id);
    }

    @View
    @JSONSerializable
    @Override
    public Map<String, MultyAssetValue> getIncomeTokens() {
        return incomeTokens;
    }

    @View
    @JSONSerializable
    @Override
    public Map<String, Coin> getCoins() {
        return coins;
    }

    @View
    @JSONSerializable
    @Override
    public Global getGlobal() {
        return global;
    }

    @View
    @JSONSerializable
    @Override
    public ProductReturn getProductReturn(BigInteger oid) {
        return returnList.get(oid);
    }

    @View
    @JSONSerializable
    @Override
    public Order getOrder(BigInteger oid) {
        return orders.get(oid);
    }

    @View
    @JSONSerializable
    @Override
    public User getUser(Address uid) {
        return users.get(uid);
    }

    @View
    @JSONSerializable
    @Override
    public Reviewer getReviewer(Address uid) {
        return reviewers.get(uid);
    }

    @View
    @JSONSerializable
    @Override
    public Product getProduct(BigInteger pid) {
        return products.get(pid);
    }

    @View
    @JSONSerializable
    @Override
    public Map<Integer, Categories> getCategories() {
        return categories;
    }

    @View
    @JSONSerializable
    @Override
    public ProductAudit getProductAudit(BigInteger id) {
        return productAudits.get(id);
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
     * @param sender 商品所有者地址
     * @return 返回生成的商品id
     */
    @View
    @Override
    public BigInteger newProductId(Address sender) {
        BigInteger hc = Helper.getHashCode(sender);
        return hc.shiftLeft(64).or(BigInteger.valueOf(Block.timestamp()));
    }

    /**
     * 生成一个新的仲裁id
     * 
     * @param plaintiff 原告人地址
     * @param defendant 被告人地址
     * @return
     */
    @View
    @Override
    public BigInteger newArbitId(Address plaintiff, Address defendant) {
        BigInteger aid = Helper.getHashCode(plaintiff);
        aid = aid.shiftLeft(32).or(Helper.getHashCode(defendant));
        aid = aid.shiftLeft(64).or(BigInteger.valueOf(Block.timestamp()));
        return aid;
    }

    /**
     * 生成一个新的订单id
     * 
     * @param sender 下单人地址
     * @param pid    商品id
     * @return 返回生成的订单id
     */
    @View
    @Override
    public BigInteger newOrderId(Address sender, BigInteger pid) {
        BigInteger oid = Helper.getHashCode(sender);
        oid = oid.shiftLeft(96).or(pid);
        oid = oid.shiftLeft(64).or(BigInteger.valueOf(Block.timestamp()));
        return oid;
    }

    @Override
    public void regUser(String nickname, String phoneHash, String phoneEncrypt, Address referrer, String head,
            String extendInfo) {
        Address uid = Msg.sender();
        require(!users.containsKey(uid) && !phones.containsKey(phoneHash), Error.ALREADY_REGISTERED);

        User user = new User();
        user.nickname = nickname;
        user.phoneHash = phoneHash;
        user.phoneEncrypt = phoneEncrypt;
        // 处理引荐
        if (referrer != null) {
            if (users.containsKey(referrer)) {
                User refer = users.get(referrer);
                if (!isLock(refer) && refer.creditValue >= global.creditRefLimit) {
                    user.referrer = referrer;
                    // referPoints(referrer, global.refAward);
                }
            }
        }
        user.uid = uid;
        user.head = head;
        user.isReviewer = false;
        user.creditValue = global.creditBaseScore;
        user.lastActiveTime = Block.timestamp();
        user.status = User.UserStatus.NONE;
        user.encryptKey = Msg.senderPublicKey();
        user.extendInfo = extendInfo;
        users.put(uid, user);

        global.totalUsers += 1;

        phones.put(phoneHash, true);

        emit(new RegUserEvent(uid, nickname, phoneEncrypt));
    }

    @Override
    public void updatePhone(@Required String phoneHash, @Required String phoneEncrypt) {
        Address uid = Msg.sender();
        require(users.containsKey(uid) && !phones.containsKey(phoneHash), Error.PARAMETER_ERROR);

        User user = users.get(uid);
        phones.remove(user.phoneHash);
        user.phoneHash = phoneHash;
        user.phoneEncrypt = phoneEncrypt;
        phones.put(phoneHash, true);
        user.lastActiveTime = Block.timestamp();

        emit(new UpdateUserEvent(uid));
    }

    @Override
    public void setProfile(String nickname, String head, String extendInfo) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        User user = users.get(uid);
        if (nickname != null && !nickname.isEmpty()) {
            user.nickname = nickname;
        }
        if (head != null && !head.isEmpty()) {
            user.head = head;
        }
        if (extendInfo != null && !extendInfo.isEmpty()) {
            user.extendInfo = extendInfo;
        }
        user.lastActiveTime = Block.timestamp();
        emit(new UpdateUserEvent(uid));
    }

    @Override
    public void appReviewer() {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        require(!reviewers.containsKey(uid), Error.ALREADY_REVIEWER);
        require(reviewers.size() < global.reviewMaxCount, Error.REVIEWER_UPPER_LIMIT);

        Reviewer reviewer = new Reviewer();
        reviewer.uid = uid;
        reviewer.createTime = Block.timestamp();
        reviewer.lastActiveTime = Block.timestamp();
        reviewer.approveCount = 0;
        reviewer.againstCount = 0;
        reviewer.voted = new HashMap<Address, Boolean>();
        reviewers.put(uid, reviewer);

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();
        emit(new CreateReviewerEvent(uid, reviewer.createTime));
    }

    @Override
    public void publish(BigInteger pid, int category, String name, String description, boolean isNew, boolean isRetail,
            boolean isReturns,
            String position, short saleMethod, int stockCount, short pickupMethod, String postage, String price) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        Helper.checkProductId(pid, uid);
        require(!products.containsKey(pid), Error.PRODUCT_ALREADY_EXISTS);
        require(categories.containsKey(category), Error.PRODUCT_INVALID_CATEGORY);
        require(stockCount > 0, Error.TOO_LITTLE_INVENTORY);
        require(Product.SaleMethod.isValid(saleMethod), Error.INVALID_SALE_METHOD);
        require(Product.PickupMethod.isValid(pickupMethod), Error.INVALID_PICKUP_METHOD);

        Product product = new Product();
        product.postage = Helper.parseAsset(postage);
        product.price = Helper.parseAsset(price);
        require(coins.containsKey(product.postage.getAssetChainId() + "-" + product.postage.getAssetId()),
                Error.INVALID_ASSET);
        require(product.price.getAssetChainId() == product.postage.getAssetChainId()
                && product.price.getAssetId() == product.postage.getAssetId(), Error.INCONSISTENT_PAYMENT_METHODS);
        require(product.postage.getValue().compareTo(BigInteger.ZERO) >= 0
                && product.price.getValue().compareTo(BigInteger.ZERO) >= 0, Error.INVALID_AMOUNT);

        product.uid = uid;
        product.pid = pid;
        product.category = category;
        product.name = name;
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

        products.put(product.pid, product);

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new PublishProductEvent(pid, product.uid));
    }

    @Override
    public void addStockCount(BigInteger pid, int count) {
        require(count > 0, Error.PARAMETER_ERROR);
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        Helper.checkProductId(pid, uid);

        require(products.containsKey(pid), Error.PRODUCT_DOES_NOT_EXIST);
        Product product = products.get(pid);
        require(product.status == Product.ProductStatus.NORMAL || product.status == Product.ProductStatus.COMPLETED,
                Error.PRODUCT_INVALID_STATUS);

        product.stockCount += count;
        if (product.status == Product.ProductStatus.LOCKED || product.status == Product.ProductStatus.COMPLETED) {
            product.status = Product.ProductStatus.NORMAL;
        }

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();
    }

    @Override
    public void delist(BigInteger pid) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        Helper.checkProductId(pid, uid);
        require(products.containsKey(pid), Error.PRODUCT_DOES_NOT_EXIST);
        Product product = products.get(pid);
        require(product.status <= Product.ProductStatus.NORMAL, Error.PRODUCT_INVALID_STATUS);
        require(product.uid.equals(uid), Error.PRODUCT_IS_NOT_YOURS);

        product.status = Product.ProductStatus.DELISTED;

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new DelistProductEvent(pid, uid));
    }

    @Override
    public void placeOrder(BigInteger orderId, int quantity, String receiptInfo) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        Helper.checkOrderId(orderId, uid);

        BigInteger pid = Helper.getPidByOrderId(orderId);
        require(products.containsKey(pid), Error.PRODUCT_INVALID_ID);
        Product product = products.get(pid);
        if (product.isRetail) {
            require(quantity > 0 && quantity <= product.stockCount, Error.PARAMETER_ERROR);
        } else {
            require(quantity == product.stockCount, Error.PARAMETER_ERROR);
        }
        require(product.status == Product.ProductStatus.NORMAL, Error.PRODUCT_INVALID_STATUS);
        require(!product.uid.equals(uid), Error.PRODUCT_CANT_BUY_YOUR_OWN);
        require(!isLock(product.uid), Error.USER_LOCKED);

        Order order = new Order();
        order.oid = orderId;
        order.pid = pid;
        order.seller = product.uid;
        order.buyer = uid;
        order.quantity = quantity;
        order.amount = new MultyAssetValue(product.price.getValue().multiply(BigInteger.valueOf(quantity)),
                product.price.getAssetChainId(), product.price.getAssetId());
        order.postage = product.postage;
        order.status = Order.OrderStatus.OS_PENDING_PAYMENT;
        order.createTime = Block.timestamp();
        order.payTimeOut = Block.timestamp() + global.payTimeOut;
        order.receiptInfo = receiptInfo;
        orders.put(orderId, order);

        product.stockCount -= quantity;
        if (product.stockCount == 0) {
            product.status = Product.ProductStatus.LOCKED;
        }

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new CreateOrderEvent(orderId, pid, order.seller, order.buyer, order.amount, order.postage,
                order.createTime, order.payTimeOut, receiptInfo));
    }

    @Override
    public void cancelOrder(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);

        BigInteger pid = Helper.getPidByOrderId(orderId);
        require(products.containsKey(pid), Error.PRODUCT_INVALID_ID);

        Order order = orders.get(orderId);
        require(order.buyer.equals(uid), Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_PAYMENT, Error.INVALID_ORDER_STATUS);

        // 处理信用分
        if (order.payTimeOut < Block.timestamp()) {
            subCredit(uid, global.creditPayTimeOut);
        }

        Product product = products.get(pid);
        product.stockCount += order.quantity;
        if (product.status == Product.ProductStatus.LOCKED) {
            product.status = Product.ProductStatus.NORMAL;
        }

        orders.remove(orderId);

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new CancelOrderEvent(orderId, pid, uid, Block.timestamp()));
    }

    @Override
    public void releaseOrder(BigInteger oid) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        require(orders.containsKey(oid), Error.INVALID_ORDER_ID);

        Order order = orders.get(oid);
        require(order.seller.equals(uid), Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_PAYMENT, Error.INVALID_ORDER_STATUS);
        require(order.payTimeOut < Block.timestamp(), Error.INVALID_ORDER_STATUS);

        // 处理信用分
        subCredit(order.buyer, global.creditPayTimeOut);

        BigInteger pid = order.pid;
        Product product = products.get(pid);
        product.stockCount += order.quantity;
        if (product.status == Product.ProductStatus.LOCKED) {
            product.status = Product.ProductStatus.NORMAL;
        }

        orders.remove(oid);

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new CancelOrderEvent(oid, pid, uid, Block.timestamp()));
    }

    @Override
    public void shipments(BigInteger orderId, String number) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);
        require(number.length() <= 64, Error.INVALID_WAYBILL_NUMBER);

        Order order = orders.get(orderId);
        require(order.seller.equals(uid), Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_SHIPMENT, Error.INVALID_ORDER_STATUS);

        order.shipmentNumber = number;
        order.shipTime = Block.timestamp();
        order.status = Order.OrderStatus.OS_PENDING_RECEIPT;
        order.receiptTimeOut = Block.timestamp() + global.receiptTimeOut;

        // 处理信用分
        if (Block.timestamp() > order.shipTimeOut) {
            subCredit(uid, global.creditShipmentsTimeout);
        }

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new ShipmentsEvent(orderId, number, order.status, order.shipTime));
    }

    @Override
    public void reShipments(BigInteger orderId, String number) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId) && returnList.containsKey(orderId), Error.INVALID_ORDER_ID);
        require(number.length() <= 64, Error.INVALID_WAYBILL_NUMBER);

        Order order = orders.get(orderId);
        require(order.buyer.equals(uid), Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_RETURN, Error.INVALID_ORDER_STATUS);

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

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new ShipmentsEvent(orderId, number, pr.status, pr.shipTime));
    }

    @Override
    public void confirmReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);

        Order order = orders.get(orderId);
        require(order.buyer.equals(uid), Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_RECEIPT, Error.INVALID_ORDER_STATUS);

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        completeOrder(order);
    }

    @Override
    public void reConfirmReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId) && returnList.containsKey(orderId), Error.INVALID_ORDER_ID);

        Order order = orders.get(orderId);
        require(order.seller.equals(uid), Error.ORDER_IS_NOT_YOURS);

        order.endTime = Block.timestamp();
        order.status = Order.OrderStatus.OS_CANCELLED;
        order.clearTime = Block.timestamp() + global.clearOrderTime;

        ProductReturn pr = returnList.get(orderId);
        require(pr.status == ProductReturn.ReturnStatus.RS_PENDING_RECEIPT, Error.INVALID_RETURN_STATUS);

        pr.receiptTime = Block.timestamp();
        pr.status = ProductReturn.ReturnStatus.RS_COMPLETED;
        pr.endTime = Block.timestamp();

        Product product = products.get(order.pid);
        product.status = Product.ProductStatus.NORMAL;
        product.stockCount += order.quantity;

        // 处理信用分
        if (Block.timestamp() > pr.receiptTimeOut) {
            subCredit(order.seller, global.creditConfirmReceiptTimeout);
        }

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        // 退款(合约中的邮费退给卖家，即两次邮费各承担一次)
        transfer(order.buyer, order.amount);
        transfer(order.seller, order.postage);

        emit(new CancelOrderEvent(orderId, pr.pid, order.buyer, user.lastActiveTime));
    }

    @Override
    public void returns(BigInteger orderId, String reasons) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);
        require(reasons.length() <= 300, Error.INVALID_REASONS);

        Order order = orders.get(orderId);
        require(order.status == Order.OrderStatus.OS_PENDING_RECEIPT, Error.INVALID_ORDER_STATUS);

        require(products.containsKey(order.pid), Error.PRODUCT_INVALID_ID);
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

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new ReturnEvent(orderId, order.pid, uid));
    }

    @Override
    public void deferReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);

        Order order = orders.get(orderId);
        require(order.buyer.equals(uid), Error.ORDER_IS_NOT_YOURS);
        require(order.status == Order.OrderStatus.OS_PENDING_RECEIPT, Error.INVALID_ORDER_STATUS);
        require(order.delayedCount < global.maxDeferrTimes, Error.NO_FURTHER_EXTENSION);

        order.delayedCount += 1;
        order.receiptTimeOut = Block.timestamp() + global.receiptTimeOut;

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();
    }

    @Override
    public void reDeferReceipt(BigInteger orderId) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(orders.containsKey(orderId) && returnList.containsKey(orderId), Error.INVALID_ORDER_ID);
        Order order = orders.get(orderId);
        require(order.seller.equals(uid), Error.ORDER_IS_NOT_YOURS);

        ProductReturn pr = returnList.get(orderId);
        require(pr.status == ProductReturn.ReturnStatus.RS_PENDING_RECEIPT, Error.INVALID_RETURN_STATUS);
        require(pr.delayedCount < global.maxDeferrTimes, Error.NO_FURTHER_EXTENSION);

        pr.delayedCount += 1;
        pr.receiptTimeOut = Block.timestamp() + global.receiptTimeOut;

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();
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

    @Override
    public boolean onApprovalReceived(Address owner, BigInteger value, String data) {
        if (Msg.sender().equals(point.getNrc20Token())) {
            return true;
        }
        return false;
    }

    @Override
    public void voteReviewer(Address reviewer, boolean isSupport) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);
        require(!uid.equals(reviewer), Error.CANT_VOTE_FOR_YOURSELF);
        require(reviewers.containsKey(reviewer) && users.containsKey(reviewer), Error.REVIEWER_NOT_EXIST);

        Reviewer rer = reviewers.get(reviewer);
        require(!rer.voted.containsKey(uid), Error.REVIEWER_YOU_ALREADY_VOTED);
        if (isSupport) {
            require(rer.approveCount < 100, Error.REVIEWER_100_CAN_VOTE);
            rer.approveCount += 1;
        } else {
            require(rer.againstCount < 100, Error.REVIEWER_100_CAN_VOTE);
            rer.againstCount += 1;
        }
        rer.voted.put(uid, true);
        User user = users.get(reviewer);
        if (rer.approveCount - rer.againstCount > 0 && user.creditValue >= global.creditReviewerLimit) {
            user.isReviewer = true;
        } else {
            user.isReviewer = false;
        }

        User opUser = users.get(uid);
        opUser.lastActiveTime = Block.timestamp();

        bonusPoints(uid, global.voteAward);

        emit(new VoteReviewerEvent(uid, reviewer, rer.approveCount, rer.againstCount));
    }

    @Override
    public void review(BigInteger pid, boolean isDelist, String reasons) {
        Address uid = Msg.sender();
        require(!isLock(uid), Error.USER_LOCKED);

        require(products.containsKey(pid), Error.PRODUCT_DOES_NOT_EXIST);
        require(!productAudits.containsKey(pid), Error.REVIEWER_ALREADY_AUDIT);
        require(checkReviewer(uid), Error.REVIEWER_YOU_ARE_NOT);

        Product product = products.get(pid);
        require(product.status == Product.ProductStatus.PUBLISH, Error.PRODUCT_INVALID_STATUS);
        require(!product.uid.equals(uid), Error.REVIEWER_FOR_YOURSELF);

        if (isDelist) {
            require(reasons != null && !reasons.isEmpty(), Error.REVIEWER_NO_REASON);
            product.status = Product.ProductStatus.DELISTED;
            subCredit(product.uid, global.creditInvalidPublish);
        } else {
            User user = users.get(product.uid);
            user.postsTotal += 1;

            product.status = Product.ProductStatus.NORMAL;
            addCredit(product.uid, global.creditPublish);
            bonusPoints(product.uid, global.publishAward);
        }
        product.reviewer = uid;

        ProductAudit pa = new ProductAudit();
        pa.pid = pid;
        pa.reviewer = uid;
        pa.isDelist = isDelist;
        pa.details = reasons;
        pa.reviewTime = Block.timestamp();
        productAudits.put(pid, pa);

        User opUser = users.get(uid);
        opUser.lastActiveTime = Block.timestamp();

        // 评审员工资
        salaryPoints(uid, global.reviewSalaryProduct);

        emit(new ReviewProductEvent(pid, uid, pa.isDelist, pa.reviewTime));
    }

    @Override
    public void cleanOrder() {
        int sum = 50;
        int count = 0;
        int clearCount = 0;
        Iterator<Entry<BigInteger, Order>> iterator = orders.entrySet().iterator();
        while (iterator.hasNext() && count < sum) {
            Order order = iterator.next().getValue();
            if (order.clearTime <= 0) { // 只清理支付超时的，并扣除信用分
                if (order.status == Order.OrderStatus.OS_PENDING_PAYMENT && order.payTimeOut < Block.timestamp()) {
                    subCredit(order.buyer, global.creditPayTimeOut);
                    iterator.remove();
                    clearCount++;
                }
            } else { // 清理过期不再保留的订单
                if (order.clearTime < Block.timestamp()) {
                    iterator.remove();
                    clearCount++;
                }
            }
            count++;
        }
        if (clearCount > 0)
            bonusPoints(Msg.sender(), global.clearAward);
    }

    @Override
    public void applyArbit(Address defendant, BigInteger pid, BigInteger orderId, short type, String description) {
        Address plaintiff = Msg.sender();
        require(!isLock(plaintiff), Error.USER_LOCKED);

        require(users.containsKey(defendant), Error.USER_NOT_EXIST);
        require(Arbitration.ArbitType.isValid(type), Error.ARBIT_INVALID_TYPE);
        require(description != null && !description.isEmpty(), Error.PARAMETER_ERROR);

        if (type == Arbitration.ArbitType.AT_ORDER) {
            if (orderId != null) {
                require(orders.containsKey(orderId), Error.INVALID_ORDER_ID);
            } else {
                return;
            }
            Order order = orders.get(orderId);
            if (plaintiff.equals(order.buyer)) { // 买家发起仲裁
                require(order.status == Order.OrderStatus.OS_PENDING_RECEIPT, Error.INVALID_ORDER_STATUS);
                order.status = Order.OrderStatus.OS_ARBITRATION;
            } else if (plaintiff.equals(order.seller)) { // 卖家发起仲裁
                require(order.status == Order.OrderStatus.OS_RETURN, Error.INVALID_ORDER_STATUS);
                require(returnList.containsKey(orderId), Error.INVALID_ORDER_ID);
                ProductReturn pr = returnList.get(orderId);
                pr.status = ProductReturn.ReturnStatus.RS_ARBITRATION;
            } else {
                return;
            }
        } else if (type == Arbitration.ArbitType.AT_COMPLAINT) {
            User pUser = users.get(plaintiff);
            require(!pUser.isReviewer, Error.ARBIT_COMPLAINT_ONLY_USER);
        } else if (type == Arbitration.ArbitType.AT_PRODUCT) {
            if (pid != null) {
                require(products.containsKey(pid), Error.PRODUCT_INVALID_ID);
                Product product = products.get(pid);
                product.status = Product.ProductStatus.DELISTED;
            } else {
                return;
            }
        } else if (type == Arbitration.ArbitType.AT_ILLEGAL_INFO) {
            // 处理一些未定义的信息
        } else {
            return;
        }

        Arbitration arb = new Arbitration();
        arb.id = newArbitId(plaintiff, defendant);
        arb.plaintiff = plaintiff;
        arb.defendant = defendant;
        arb.pid = pid;
        arb.orderId = orderId;
        arb.type = type;
        arb.status = Arbitration.ArbitStatus.AS_APPLY;
        arb.description = description;
        arb.createTime = Block.timestamp();
        arbits.put(arb.id, arb);

        User user = users.get(plaintiff);
        user.lastActiveTime = Block.timestamp();

        emit(new ApplyArbitEvent(arb.id));
    }

    @Override
    public void inArbit(BigInteger id) {
        Address uid = Msg.sender();
        require(checkReviewer(uid), Error.REVIEWER_YOU_ARE_NOT);
        require(arbits.containsKey(id), Error.PARAMETER_ERROR);

        Arbitration arb = arbits.get(id);
        require(!arb.defendant.equals(uid) && !arb.plaintiff.equals(uid), Error.ARBIT_CANNOT_PARTICIPATE);
        require(arb.status < Arbitration.ArbitStatus.AS_COMPLETED, Error.ARBIT_INVALID_STATUS);

        if (arb.reviewers == null) {
            arb.reviewers = new ArrayList<Address>();
        } else {
            require(arb.reviewers.size() < global.arbitMaxCount, Error.REVIEWER_UPPER_LIMIT);
            require(!arb.reviewers.contains(uid), Error.ARBIT_ALREADY_IN);
        }
        arb.reviewers.add(uid);
        if (arb.reviewers.size() >= global.reviewMinCount) {
            arb.status = Arbitration.ArbitStatus.AS_PROCESSING;
            arb.startTime = Block.timestamp();
        } else {
            arb.status = Arbitration.ArbitStatus.AS_WAIT;
        }

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        emit(new ArbitUpdateEvent(id, arb.status, uid));
    }

    @Override
    public void updateArbit(BigInteger id, String proofContent) {
        Address uid = Msg.sender();
        require(checkReviewer(uid), Error.REVIEWER_YOU_ARE_NOT);
        require(arbits.containsKey(id), Error.PARAMETER_ERROR);

        Arbitration arb = arbits.get(id);
        require(arb.reviewers.contains(uid), Error.ARBIT_ONLY_REVIEWER);
        require(arb.status < Arbitration.ArbitStatus.AS_COMPLETED, Error.ARBIT_INVALID_STATUS);
        if (proofContent != null && !proofContent.isEmpty()) {
            arb.proofContent = proofContent;
            emit(new ArbitUpdateEvent(id, arb.status, uid));
        }

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();
    }

    @Override
    public void voteArbit(BigInteger id, boolean agree) {
        Address uid = Msg.sender();
        require(checkReviewer(uid), Error.REVIEWER_YOU_ARE_NOT);
        require(arbits.containsKey(id), Error.PARAMETER_ERROR);

        Arbitration arb = arbits.get(id);
        require(arb.status == Arbitration.ArbitStatus.AS_PROCESSING, Error.ARBIT_INVALID_STATUS);
        require(arb.proofContent != null && !arb.proofContent.isEmpty(), Error.ARBIT_NO_PROOF_PROVIDED);

        if (arb.alreadyVoted == null) {
            arb.alreadyVoted = new ArrayList<Address>();
        } else {
            require(!arb.alreadyVoted.contains(uid), Error.ARBIT_ALREADY_VOTED);
        }
        if (agree) {
            arb.agreeCount += 1;
        } else {
            arb.disagreeCount += 1;
        }
        arb.alreadyVoted.add(uid);

        User user = users.get(uid);
        user.lastActiveTime = Block.timestamp();

        // 计算投票结果
        if (arb.alreadyVoted.size() >= arb.reviewers.size() * 2 / 3 + 1) {
            arb.status = Arbitration.ArbitStatus.AS_COMPLETED;
            // 判定逻辑，支持原告的票数必须大于支持被告的票数才为原告胜
            if (arb.agreeCount > arb.disagreeCount) {
                arb.winner = arb.plaintiff;
            } else {
                arb.winner = arb.defendant;
            }
            arb.status = Arbitration.ArbitStatus.AS_COMPLETED;
            arb.endTime = Block.timestamp();

            // 处理对应的数据
            if (arb.type == Arbitration.ArbitType.AT_ORDER) {
                Order order = orders.get(arb.orderId);
                if (arb.winner.equals(order.buyer)) { // 买家赢
                    // 进入退货流程
                    order.status = Order.OrderStatus.OS_RETURN;
                    ProductReturn pr = new ProductReturn();
                    pr.oid = order.oid;
                    pr.pid = order.pid;
                    pr.status = ProductReturn.ReturnStatus.RS_PENDING_SHIPMENT;
                    pr.reasons = arb.description;
                    pr.createTime = Block.timestamp();
                    pr.shipTimeOut = Block.timestamp() + global.shipTimeOut;
                    returnList.put(order.oid, pr);
                    emit(new ReturnEvent(order.oid, order.pid, uid));

                    // 扣卖家信用分
                    subCredit(order.seller, global.arbitLosing);
                } else { // 卖家赢
                    completeOrder(order);

                    // 扣买家信用分
                    subCredit(order.buyer, global.arbitLosing);
                }
            } else if (arb.type == Arbitration.ArbitType.AT_COMPLAINT) {
                // 扣被告信用分
                subCredit(arb.winner.equals(arb.plaintiff) ? arb.defendant : arb.plaintiff, global.arbitLosing);
            } else if (arb.type == Arbitration.ArbitType.AT_PRODUCT) {
                // 扣除输家信用分
                subCredit(arb.winner.equals(arb.plaintiff) ? arb.defendant : arb.plaintiff, global.arbitLosing);
                if (arb.winner.equals(arb.plaintiff)) {
                    Product product = products.get(arb.pid);
                    product.status = Product.ProductStatus.DELISTED;
                    emit(new DelistProductEvent(arb.pid, uid));
                }
            } else if (arb.type == Arbitration.ArbitType.AT_ILLEGAL_INFO) {
                subCredit(arb.winner.equals(arb.plaintiff) ? arb.defendant : arb.plaintiff, global.arbitLosing);
            }
            // 评审员工资
            int size = arb.alreadyVoted.size();
            BigInteger total = global.reviewSalaryDispute.multiply(BigInteger.valueOf(size));
            if (global.salaryPool.compareTo(total) >= 0) {
                global.salaryPool = global.salaryPool.subtract(total);
                List<BigInteger> vals = new ArrayList<BigInteger>();
                for (int m = 0; m < arb.alreadyVoted.size(); m++) {
                    vals.add(global.reviewSalaryDispute);
                }
                boolean result = Helper.batchTransfer(point.getNrc20Token(),
                        arb.alreadyVoted.toArray(new Address[size]),
                        vals.toArray(new BigInteger[size]));
                require(result, Error.BATCHTRANSFER_FAILED);
            }
        }

        emit(new ArbitUpdateEvent(id, arb.status, uid));
    }

    /************************************
     * 私有方法
     ********************************************/

    /**
     * 检查是否是评审员
     * 
     * @param addr
     */
    private boolean checkReviewer(Address addr) {
        User user = users.get(addr);
        if (user != null) {
            if (user.creditValue >= global.creditReviewerLimit) {
                return user.isReviewer;
            } else {
                if (user.isReviewer) {
                    user.isReviewer = false;
                    reviewers.remove(addr);
                }
            }
        }
        return false;
    }

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
        require(order.amount.getValue().add(order.postage.getValue()).compareTo(value.getValue()) == 0,
                Error.INVALID_AMOUNT);

        order.status = Order.OrderStatus.OS_PENDING_SHIPMENT;
        order.payTime = Block.timestamp();
        order.shipTimeOut = Block.timestamp() + global.shipTimeOut;

        if (Block.timestamp() > order.payTimeOut) {
            subCredit(order.buyer, global.creditPayTimeOut);
        } else {
            addCredit(order.buyer, global.creditPay);
        }

        User user = users.get(order.buyer);
        user.lastActiveTime = Block.timestamp();

        emit(new PayOrderEvent(orderId, order.status, order.payTime, order.shipTimeOut));
    }

    /**
     * 完成订单的相关处理逻辑
     * 
     * @param order 订单对象
     */
    private void completeOrder(Order order) {
        order.endTime = Block.timestamp();
        order.receiptTime = Block.timestamp();
        order.status = Order.OrderStatus.OS_COMPLETED; // 先修改状态，防止重入
        order.clearTime = Block.timestamp() + global.clearOrderTime;

        Product product = products.get(order.pid);
        if (product.stockCount == 0) {
            product.status = Product.ProductStatus.COMPLETED;
        }

        User seller = users.get(order.seller);
        User buyer = users.get(order.buyer);
        if (seller != null) {
            seller.sellTotal += 1;
        }
        if (buyer != null) {
            buyer.buyTotal += 1;
            // 处理买家引荐奖励
            if (buyer.referrer != null) {
                Boolean has = hasRefer.get(buyer.uid);
                if (has == null || has == false) {
                    hasRefer.put(buyer.uid, true);
                    referPoints(buyer.referrer, global.refAward);
                }
            }
        }

        // 处理信用分
        addCredit(seller, global.creditCompleteTransaction);
        addCredit(buyer, global.creditCompleteTransaction);
        if (order.receiptTimeOut < Block.timestamp()) {
            subCredit(buyer, global.creditConfirmReceiptTimeout);
        }

        settlement(order, seller, buyer);

        tradeRewardPoints(order, seller, buyer);
        emit(new CompleteOrderEvent(order.oid, order.status, order.endTime));
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
        if (income.compareTo(BigInteger.ZERO) > 0 && buyer.referrer != null && !isLock(buyer.referrer)) {
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
        if (amount == null || amount.compareTo(BigInteger.ZERO) <= 0)
            return;
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
     * 奖励积分
     * 
     * @param to    得奖人
     * @param value 金额
     */
    private void bonusPoints(Address to, BigInteger value) {
        if (global.sysPool.compareTo(value) >= 0) {
            global.sysPool = global.sysPool.subtract(value);
            transfer(to, value, 0, 0);
        }
    }

    /**
     * 发放工资
     * 
     * @param to    得薪人
     * @param value 金额
     */
    private void salaryPoints(Address to, BigInteger value) {
        if (global.salaryPool.compareTo(value) >= 0) {
            global.salaryPool = global.salaryPool.subtract(value);
            transfer(to, value, 0, 0);
        }
    }

    /**
     * 发放引荐奖励
     * 
     * @param to    引荐人
     * @param value 金额
     */
    private void referPoints(Address to, BigInteger value) {
        if (global.refPool.compareTo(value) >= 0) {
            global.refPool = global.refPool.subtract(value);
            transfer(to, value, 0, 0);
        }
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
        emit(new UpdateUserEvent(user.uid));
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
        if (user.creditValue > global.creditMax) {
            user.creditValue = global.creditMax;
        }
        emit(new UpdateUserEvent(user.uid));
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
        if (!global.tradeReward)
            return;
        int decimals = pointDecimals;
        if (order.amount.getAssetChainId() != 0)
            decimals = assetDecimals(order.amount.getAssetChainId(), order.amount.getAssetId());
        String key = Coin.getTokenKey(order.amount.getAssetChainId(), order.amount.getAssetId());
        BigInteger amount = order.amount.getValue();
        amount = amount.multiply(E.pow(4)).divide(E.pow(decimals)); // 只保留4位小数
        BigInteger rate = BigInteger.valueOf(coins.get(key).transactionAwardRate);
        amount = amount.multiply(rate).divide(DENOMINATOR).multiply(E.pow(pointDecimals - 4)); // 计算比率
        if (amount.compareTo(BigInteger.ZERO) > 0
                && global.sysPool.compareTo(amount.multiply(BigInteger.valueOf(2))) >= 0) {
            global.sysPool = global.sysPool.subtract(amount.multiply(BigInteger.valueOf(2)));
            // point.transfer(seller.uid, val);
            // point.transfer(buyer.uid, val);
            boolean result = Helper.batchTransfer(point.getNrc20Token(),
                    new Address[] { seller.uid, buyer.uid },
                    new BigInteger[] { amount, amount });
            require(result, Error.BATCHTRANSFER_FAILED);
        }
    }

    @Override
    public void addCategory(int id, String view, int parent) {
        onlyOwner();
        require(!categories.containsKey(id), Error.PRODUCT_CATEGORY_ALREADY_EXISTS);
        require(parent == 0 || categories.containsKey(parent), Error.PARAMETER_ERROR);
        require(view != null && !view.isEmpty(), Error.PARAMETER_ERROR);

        Categories c = new Categories();
        c.id = id;
        c.view = view;
        c.parent = parent;
        categories.put(id, c);

        emit(new AddCategoryEvent(Msg.sender(), id, view, parent));
    }

    @Override
    public void depositSalaryPool(BigInteger amount) {
        if (amount.compareTo(BigInteger.ZERO) > 0) {
            boolean result = point.transferFrom(Msg.sender(), Msg.address(), amount);
            if (result) {
                global.salaryPool = global.salaryPool.add(amount);
            }
        }
    }

    @Override
    public void depositRefPool(BigInteger amount) {
        if (amount.compareTo(BigInteger.ZERO) > 0) {
            boolean result = point.transferFrom(Msg.sender(), Msg.address(), amount);
            if (result) {
                global.refPool = global.refPool.add(amount);
            }
        }
    }

    @Override
    public void depositSysPool(BigInteger amount) {
        if (amount.compareTo(BigInteger.ZERO) > 0) {
            boolean result = point.transferFrom(Msg.sender(), Msg.address(), amount);
            if (result) {
                global.sysPool = global.sysPool.add(amount);
            }
        }
    }

    @View
    @Override
    public Address getPoint() {
        return point.getNrc20Token();
    }

    @Override
    public void setReviewMaxCount(Integer count) {
        onlyOwner();
        if (global.reviewMaxCount != count) {
            global.reviewMaxCount = count;
        }
    }

    @Override
    public void setRate(Short feeRate, Short refCommRate) {
        onlyOwner();
        if (feeRate != null) {
            global.feeRate = feeRate;
        }
        if (refCommRate != null) {
            global.refCommRate = refCommRate;
        }
    }

    @Override
    public void setTradeReward(Boolean open) {
        onlyOwner();
        global.tradeReward = open;
    }

    @Override
    public void setAwards(BigInteger refAward, BigInteger publishAward, BigInteger voteAward, BigInteger clearAward) {
        onlyOwner();
        if (refAward != null) {
            global.refAward = refAward;
        }
        if (publishAward != null) {
            global.publishAward = publishAward;
        }
        if (voteAward != null) {
            global.voteAward = voteAward;
        }
        if (clearAward != null) {
            global.clearAward = clearAward;
        }
    }

    @Override
    public void setSalary(BigInteger reviewSalaryProduct, BigInteger reviewSalaryDispute) {
        onlyOwner();
        if (reviewSalaryProduct != null) {
            global.reviewSalaryProduct = reviewSalaryProduct;
        }
        if (reviewSalaryDispute != null) {
            global.reviewSalaryDispute = reviewSalaryDispute;
        }
    }

    @Override
    public void setCreditScore(String key, int value) {
        onlyOwner();
        require(value >= 0, Error.PARAMETER_ERROR);
        switch (key) {
            case "creditBaseScore":
                global.creditBaseScore = value;
                break;
            case "creditMax":
                global.creditMax = value;
                break;
            case "creditRefLimit":
                global.creditRefLimit = value;
                break;
            case "creditReviewerLimit":
                global.creditReviewerLimit = value;
                break;
            case "creditCompleteTransaction":
                global.creditCompleteTransaction = value;
                break;
            case "creditConfirmReceiptTimeout":
                global.creditConfirmReceiptTimeout = value;
                break;
            case "creditShipmentsTimeout":
                global.creditShipmentsTimeout = value;
                break;
            case "creditPayTimeOut":
                global.creditPayTimeOut = value;
                break;
            case "creditPay":
                global.creditPay = value;
                break;
            case "creditPublish":
                global.creditPublish = value;
                break;
            case "creditInvalidPublish":
                global.creditInvalidPublish = value;
                break;
            case "arbitLosing":
                global.arbitLosing = value;
                break;
            default:
                break;
        }
    }

    @View
    @JSONSerializable
    @Override
    public List<User> getUsersByIds(String[] ids) {
        List<User> list = new ArrayList<User>();
        for (int i = 0; i < ids.length; i++) {
            Address id = new Address(ids[i]);
            User u = users.get(id);
            if (u != null) {
                list.add(u);
            }
        }
        return list;
    }

    @View
    @JSONSerializable
    @Override
    public List<Product> getProductsByIds(String[] ids) {
        List<Product> list = new ArrayList<Product>();
        for (int i = 0; i < ids.length; i++) {
            BigInteger id = new BigInteger(ids[i]);
            Product p = products.get(id);
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    @View
    public Boolean getHasRefer(Address uid) {
        if (hasRefer.containsKey(uid))
            return hasRefer.get(uid);
        return false;
    }

    public void setCommissionAddr(Address addr) {
        onlyOwner();
        global.commission = addr;
    }

    public void setEncryptKey(String key) {
        onlyOwner();
        global.encryptKey = key;
    }
}