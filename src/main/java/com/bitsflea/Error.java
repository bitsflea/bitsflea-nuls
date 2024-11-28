package com.bitsflea;

/**
 * 错误编码定义
 */
public class Error {
    /**
     * 参数错误
     */
    public static final String PARAMETER_ERROR = "1001";
    /**
     * 已经注册
     */
    public static final String ALREADY_REGISTERED = "10001";

    /**
     * 已经是评审员
     */
    public static final String ALREADY_REVIEWER = "10002";
    /**
     * 用户不存在
     */
    public static final String USER_NOT_EXIST = "10003";
    /**
     * 用户已经被锁定
     */
    public static final String USER_LOCKED = "10004";

    /**
     * 商品已经存在
     */
    public static final String PRODUCT_ALREADY_EXISTS = "20001";
    /**
     * 商品库存不能为0
     */
    public static final String TOO_LITTLE_INVENTORY = "20002";
    /**
     * 商品id无效
     */
    public static final String INVALID_PRODUCT_ID = "20003";
    /**
     * 销售方式无效
     */
    public static final String INVALID_SALE_METHOD = "20004";
    /**
     * 收货方式无效
     */
    public static final String INVALID_PICKUP_METHOD = "20005";
    /**
     * 商品不存在
     */
    public static final String PRODUCT_DOES_NOT_EXIST = "20006";
    /**
     * 商品不是normal状态
     */
    public static final String PRODUCT_NOT_NORMAL_STATUS = "20007";
    /**
     * 商品不是Msg.sender()的
     */
    public static final String PRODUCT_IS_NOT_YOURS = "20008";
    /**
     * 订单id无效
     */
    public static final String INVALID_ORDER_ID = "20009";
    /**
     * 不能买自己发布的商品
     */
    public static final String PRODUCT_CANT_BUY_YOUR_OWN = "20010";
    /**
     * 无效的订单状态
     */
    public static final String INVALID_ORDER_STATUS = "20011";
    /**
     * 订单不是你的
     */
    public static final String ORDER_IS_NOT_YOURS = "20012";
    /**
     * 运单号太长了
     */
    public static final String INVALID_WAYBILL_NUMBER = "20013";
    /**
     * 无效的退货说明
     */
    public static final String INVALID_REASONS = "20014";
    /**
     * 商品不支持退货
     */
    public static final String PRODUCT_NOT_SUPPORT_RETURNS = "20015";
    /**
     * 无效的退货状态
     */
    public static final String INVALID_RETURN_STATUS = "20016";
    /**
     * 不能再延期，已经越过延期次数
     */
    public static final String NO_FURTHER_EXTENSION = "20017";
    /**
     * 支付方式不一致
     */
    public static final String INCONSISTENT_PAYMENT_METHODS = "20018";
    /**
     * 转出积分失败
     */
    public static final String FAILED_TRANSFER_POINTS = "20019";
    /**
     * 无效金额
     */
    public static final String INVALID_AMOUNT = "20020";
    /**
     * 只允许合约所有者执行
     */
    public static final String ONLY_OWNER_EXECUTE = "20021";
    /**
     * 无效的资产
     */
    public static final String INVALID_ASSET = "20022";
    /**
     * 非法调用合约
     */
    public static final String ILLEGAL_CALL = "20023";
}
