import { env, sdk, contract } from "./config.js"
import { parseNULS } from "nuls-api-v2";

import * as assert from 'assert';
import * as tools from "./tools.js";

describe('ChainAsset', function () {
    this.timeout(200000);

    let bitsflea;   // contract
    let point;      // contract

    let alice;
    let bob;
    let HanMeimei;

    let transactionAwardRate = 100;

    let commissionAddr = "tNULSeBaMg3uA6d68rchxgu6a1jrGw1GQwkBBJ";

    const description = "bagaaieran3gqmu65wp4fjccrgidryjyfdxkubvm2fihe6u52qztirfxi56xq";
    const location = "34.0522,-118.2437|US,Los Angeles,California";

    before(async () => {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
        HanMeimei = sdk.account(env.KEY_HANMEIMEI);

        let pointAddress = await bitsflea.getPoint();
        point = await sdk.contract(pointAddress);
    });

    it("Failed to add chain assets", async () => {
        await bitsflea.connect(alice.accountPri).addCoin(5, 1, 100).catch(reason => {
            assert.equal(reason, "20021", "20021 error");
        });
    });

    it("Add chain assets successfully", async () => {
        let coins = await bitsflea.getCoins();
        console.log("coins:", coins)
        if ("5-1" in coins === false) {
            await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).addCoin(5, 1, transactionAwardRate));
            coins = await bitsflea.getCoins();
            console.log("coins:", coins)
            let newCoin = coins['5-1'];
            assert.ok(!!newCoin, "add coin error");
            assert.equal(newCoin.chainId, 5, "chainId error");
            assert.equal(newCoin.assetId, 1, "assetId error");
            assert.equal(newCoin.transactionAwardRate, transactionAwardRate, "transactionAwardRate error");
        }
    });

    it("Trading Commission", async () => {
        // Please make sure HanMeimei has 5-1 assets before execution
        // owner publish product
        let pid = await bitsflea.newProductId(sdk.sender);
        pid = pid.toString(10);

        let postage = parseNULS(0.1).toString();
        let price = parseNULS(1).toString();

        let txHash = await bitsflea.connect(sdk.accountPri).publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},5,1`, `${price},5,1`);
        await sdk.waitingResult(txHash);

        // bob review product
        await sdk.waitingResult(await bitsflea.connect(bob.accountPri).review(pid, false, "合格"));

        // HanMeimei buy product
        let orderId = await bitsflea.newOrderId(HanMeimei.sender, pid);
        orderId = orderId.toString(10);
        await sdk.waitingResult(await bitsflea.connect(HanMeimei.accountPri).placeOrder(orderId, 1, null));
        // HanMeimei pay order
        await sdk.waitingResult(await bitsflea.payOrder(orderId, {
            multyAssetArray: [{
                value: parseNULS(1.1),
                assetChainId: 5,
                assetId: 1
            }]
        }));
        // owner shipment
        await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).shipments(orderId, "123456789"));
        let [u1, u2, b1, b2, b3, b4, incomeTokens] = await Promise.all([
            bitsflea.getUser(sdk.sender),
            bitsflea.getUser(HanMeimei.sender),
            sdk.getAvailableBalance(alice.sender, 5, 1),
            sdk.getAvailableBalance(commissionAddr, 5, 1),
            point.balanceOf(sdk.sender),
            point.balanceOf(HanMeimei.sender),
            bitsflea.getIncomeTokens()
        ]);
        // console.log("incomeTokens:", incomeTokens);
        // HanMeimei confirm receipt
        await sdk.waitingResult(await bitsflea.connect(HanMeimei.accountPri).confirmReceipt(orderId));
        let [u12, u22, b12, b22, b32, b42, incomeTokens2] = await Promise.all([
            bitsflea.getUser(sdk.sender),
            bitsflea.getUser(HanMeimei.sender),
            sdk.getAvailableBalance(alice.sender, 5, 1),
            sdk.getAvailableBalance(commissionAddr, 5, 1),
            point.balanceOf(sdk.sender),
            point.balanceOf(HanMeimei.sender),
            bitsflea.getIncomeTokens()
        ]);
        // console.log("incomeTokens2:", incomeTokens2);
        // Credit score
        assert.equal(u1.creditValue + 5, u12.creditValue, "owner creditValue error");
        assert.equal(u2.creditValue + 5, u22.creditValue, "HanMeimei creditValue error");

        // trading point reward
        let pointReward = parseNULS(1).times(transactionAwardRate).div(1000);
        assert.equal(b32.minus(b3).toString(10), pointReward.toString(), "owner point balance error");
        assert.equal(b42.minus(b4).toString(10), pointReward.toString(), "HanMeimei point balance error");

        let total = parseNULS(1.1);
        let income = total.times(50).div(1000);
        let reward = income.times(50).div(1000);
        // referrer Trading Commission
        assert.equal(b12.minus(b1).toString(10), reward.toString(10), "ref reward error");

        let platformIncome = BigInt(0);
        if ("5-1" in incomeTokens) {
            platformIncome = BigInt(incomeTokens2['5-1'].value) - BigInt(incomeTokens['5-1'].value);
        } else {
            platformIncome = BigInt(incomeTokens2['5-1'].value);
        }

        // Platform revenue
        assert.equal(platformIncome.toString(), income.minus(reward).toString(10), "platformIncome error");
        assert.equal(b22.minus(b2).toString(10), platformIncome.toString(), "platform 5-1 balance error");
    });
});