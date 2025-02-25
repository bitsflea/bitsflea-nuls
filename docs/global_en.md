### Global Parameters
## I. Points and Rewards
1. **Transaction Commission** `feeRate=50` – 5%.
2. **Referral Commission** `refCommRate=50` – 5%.
3. **Referral Reward** `refAward=100` – 100 BTF, determined by the referral reward pool.
4. **Publish Product Reward** `publishAward=50` – 50 BTF, determined by the system reward pool.
5. **Voting Reward** `voteAward=10` – 10 BTF, determined by the system reward pool.
6. **Data Cleanup Reward** `clearAward=10` – 10 BTF, determined by the system reward pool.
7. **The transaction reward "transactionAwardRate"**, is determined by the transaction currency transactionAwardRate parameter

## II. Credit Score
1. **Base Credit Score** `creditBaseScore=500`
2. **Referral Credit Score Lower Limit** `creditRefLimit=500`
3. **Reviewer Credit Score Lower Limit** `creditReviewerLimit=500`
4. **Complete Transaction** `creditCompleteTransaction=5`
5. **Confirm Receipt Timeout Deduction** `creditConfirmReceiptTimeout=5`
6. **Shipment Timeout Deduction** `creditShipmentsTimeout=5`
7. **Payment Timeout Deduction** `creditPayTimeOut=5`
8. **On-time Payment Bonus** `creditPay=2`
9. **Successful Product Publication Bonus** `creditPublish=1`
10. **Failed Product Publication Deduction** `creditInvalidPublish=5`
11. **Arbitration Losing Party Deduction** `arbitLosing=100`

## III. Reviewers
1. **Minimum Number of Reviewers** `reviewMinCount=3`
2. **Maximum Number of Reviewers per Arbitration** `arbitMaxCount=5`
3. **Maximum Number of Reviewers** `reviewMaxCount=3000`
4. **Reward for Reviewing a Product** `reviewSalaryProduct=50` – 50 BTF
5. **Reward for Handling a Dispute** `reviewSalaryDispute=200` – 200 BTF
