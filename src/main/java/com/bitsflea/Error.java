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
    public static final String PRODUCT_INVALID_ID = "20003";
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
     * 商品状态不正确
     */
    public static final String PRODUCT_INVALID_STATUS = "20007";
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
    /**
     * 商品类型已经存在
     */
    public static final String PRODUCT_CATEGORY_ALREADY_EXISTS = "20024";
    /**
     * 无效的商品类型
     */
    public static final String PRODUCT_INVALID_CATEGORY = "20025";

    /**
     * 不能投自己票
     */
    public static final String CANT_VOTE_FOR_YOURSELF = "30001";
    /**
     * 评审员记录不存在
     */
    public static final String REVIEWER_NOT_EXIST = "30002";
    /**
     * 最多只能100人参与投票
     */
    public static final String REVIEWER_100_CAN_VOTE = "30003";
    /**
     * 你已经为此评审员投过票了
     */
    public static final String REVIEWER_YOU_ALREADY_VOTED = "30004";
    /**
     * 你不是评审员
     */
    public static final String REVIEWER_YOU_ARE_NOT = "30005";
    /**
     * 评审员下架商品时没有提供原因
     */
    public static final String REVIEWER_NO_REASON = "30006";
    /**
     * 不能审核自己的商品
     */
    public static final String REVIEWER_FOR_YOURSELF = "30007";
    /**
     * 商品已经审核
     */
    public static final String REVIEWER_ALREADY_AUDIT = "30008";
    /**
     * 无效的仲裁类型
     */
    public static final String ARBIT_INVALID_TYPE = "30009";
    /**
     * 投诉评审员只能由普通用户发起
     */
    public static final String ARBIT_COMPLAINT_ONLY_USER = "30010";
    /**
     * 不能参与自己的仲裁处理
     */
    public static final String ARBIT_CANNOT_PARTICIPATE = "30011";
    /**
     * 无效的仲裁状态
     */
    public static final String ARBIT_INVALID_STATUS = "30012";
    /**
     * 你已经在仲裁中
     */
    public static final String ARBIT_ALREADY_IN = "30013";
    /**
     * 你已经投票过了
     */
    public static final String ARBIT_ALREADY_VOTED = "30014";

    /**
     * 评审员数量已经达到上限
     */
    public static final String REVIEWER_UPPER_LIMIT = "30015";
}
