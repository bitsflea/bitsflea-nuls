import { env, sdk, contract } from "./config.js"
import { parseNULS } from "nuls-api-v2";

import * as assert from 'assert';
import * as tools from "./tools.js";

describe('Referrer', function () {
    this.timeout(200000);

    let bitsflea;   // contract
    let point;      // contract

    let alice;
    let bob;
    let HanMeimei;

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

    it("Referral Rewards", async () => {
        let result = await bitsflea.getGlobal();
        assert.ok(!!result, "Failed to get global status");

        const encryptKey = result.encryptKey;
        let _HanMeimei = await bitsflea.getUser(HanMeimei.sender);
        if (!_HanMeimei) {
            const phone = "18580599990";
            let nickname = "HanMeimei";
            let phoneHash = tools.getHash(phone);
            let phoneEncrypt = tools.encrypt(env.KEY_HANMEIMEI, encryptKey, phone);
            let referrer = alice.sender;
            let head = "bafkreigwtvsing3bzjhmhhfv3nnerll5ouw2xjizqytt6qppctv7wbteam";
            let extendInfo = "bafkreihcv2kpbmdjdf7rq3lcnvon6yc4k6fsuxwhsaxe7ue7ft4b3lzwpm";

            // transfer
            await point.connect(sdk.accountPri).transfer(HanMeimei.sender, parseNULS(500));
            await sdk.waitingTx(await sdk.transfer(HanMeimei.sender, parseNULS(10)));

            let balance1 = await point.balanceOf(alice.sender);
            result = await bitsflea.connect(HanMeimei.accountPri).regUser(nickname, phoneHash, phoneEncrypt, referrer, head, extendInfo);
            await sdk.waitingResult(result);
            let balance2 = await point.balanceOf(alice.sender);
            assert.equal(balance2.minus(balance1).toString(10), parseNULS(100).toString(10), "alice balance error");
        }
    });

    it("Trading Commission", async () => {
        let pid = await bitsflea.newProductId(sdk.sender);
        pid = pid.toString(10);

        let postage = parseNULS(10).toString();
        let price = parseNULS(100).toString();

        let txHash = await bitsflea.connect(sdk.accountPri).publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},0,0`, `${price},0,0`);
        await sdk.waitingResult(txHash);

        // bob review product
        await sdk.waitingResult(await bitsflea.connect(bob.accountPri).review(pid, false, "合格"));

        // HanMeimei buy product
        let orderId = await bitsflea.newOrderId(HanMeimei.sender, pid);
        orderId = orderId.toString(10);
        await sdk.waitingResult(await bitsflea.connect(HanMeimei.accountPri).placeOrder(orderId, 1, null));
        // HanMeimei pay order
        let pointAddress = await bitsflea.getPoint();
        let point = await sdk.contract(pointAddress);
        await sdk.waitingResult(await point.connect(HanMeimei.accountPri).transferAndCall(bitsflea.address, parseNULS(110).toString(10), orderId));
        // owner shipment
        await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).shipments(orderId, "123456789"));
        let [u1, u2, b1, incomeTokens] = await Promise.all([
            bitsflea.getUser(sdk.sender),
            bitsflea.getUser(HanMeimei.sender),
            point.balanceOf(alice.sender),
            bitsflea.getIncomeTokens()
        ]);
        // console.log("incomeTokens:", incomeTokens);
        // HanMeimei confirm receipt
        await sdk.waitingResult(await bitsflea.connect(HanMeimei.accountPri).confirmReceipt(orderId));
        let [u12, u22, b12, incomeTokens2] = await Promise.all([
            bitsflea.getUser(sdk.sender),
            bitsflea.getUser(HanMeimei.sender),
            point.balanceOf(alice.sender),
            bitsflea.getIncomeTokens()
        ]);
        // console.log("incomeTokens2:", incomeTokens2);
        assert.equal(u1.creditValue + 5, u12.creditValue, "owner creditValue error");
        assert.equal(u2.creditValue + 5, u22.creditValue, "HanMeimei creditValue error");

        let total = parseNULS(110);
        let income = total.times(50).div(1000);
        let reward = income.times(50).div(1000);
        assert.equal(b12.minus(b1).toString(10), reward.toString(10), "ref reward error");

        let platformIncome = BigInt(incomeTokens2['0-0'].value) - BigInt(incomeTokens['0-0'].value);
        assert.equal(platformIncome.toString(), income.minus(reward).toString(10), "platformIncome error");

    });
});