### 全局参数
## 一、佣金
- 1.交易佣金“feeRate=500”，5%。
- 2.引荐佣金“refCommRate=4000”，40%

## 一、积分奖励
- 1.引荐奖励“refAward=100”，100BTF，由引荐奖励池决定。
- 2.发布商品奖励“publishAward=50”，50BTF，由系统奖励池决定。
- 3.投票奖励“voteAward=10”，10BTF，由系统奖励池决定。
- 4.清理数据“clearAward=10”，10BTF，由系统奖励池决定。
- 5.完成交易奖励"transactionAwardRate"，由交易币种transactionAwardRate参数与系统奖励池决定

## 二、信用分
- 1.基础信用分“creditBaseScore=500”
- 2.参与引荐信用分下限“creditRefLimit=500”
- 3.评审员信用分下限“creditReviewerLimit=500”
- 4.完成一笔交易加分“creditCompleteTransaction=5”
- 5.确认收货超时扣分“creditConfirmReceiptTimeout=5”
- 6.发货超时扣分“creditShipmentsTimeout=5”
- 7.支付超时扣分“creditPayTimeOut=5”
- 8.按时支付加分“creditPay=2”
- 9.成功发布商品加分“creditPublish=1”
- 10.发布商品审核未通过扣分“creditInvalidPublish=5”
- 11.仲裁失败方扣分“arbitLosing=100”

## 三、评审员
- 1.最少评审员“reviewMinCount=3”
- 2.单个仲裁最多评审员“arbitMaxCount=5”
- 3.最多评审员“reviewMaxCount=3000”
- 4.审核一个商品奖励“reviewSalaryProduct=50”，50BTF,工资池决定。
- 5.处理一个纠纷奖励"reviewSalaryDispute=200", 200BTF,工资池决定。
