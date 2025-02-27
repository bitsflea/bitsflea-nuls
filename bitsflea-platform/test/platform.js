import { env, sdk, contract } from "./config.js"

import * as assert from 'assert';
import * as tools from "./tools.js";
import { parseNULS, getEvent } from "nuls-api-v2";

describe("Platform", function () {
    this.timeout(200000);

    let bitsflea;   // contract
    let point;      // contract

    let alice;  // reviewer
    let bob;    // reviewer
    let carol;  // reviewer
    let lilei;  // user

    const description = "bagaaieran3gqmu65wp4fjccrgidryjyfdxkubvm2fihe6u52qztirfxi56xq";
    const location = "34.0522,-118.2437|US,Los Angeles,California";

    before(async function () {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
        carol = sdk.account(env.KEY_CAROL);
        lilei = sdk.account(env.KEY_LILEI);

        let pointAddress = await bitsflea.getPoint();
        point = await sdk.contract(pointAddress);
    });

    /*
    describe("Review product", () => {

        let pid;

        beforeEach(async () => {
            pid = await bitsflea.newProductId(sdk.sender);
            pid = pid.toString(10);

            let postage = parseNULS(10).toString();
            let price = parseNULS(100).toString();

            let txHash = await bitsflea.connect(sdk.accountPri).publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
                `${postage},0,0`, `${price},0,0`);
            await sdk.waitingResult(txHash);
        });

        it("Check the points after normal review", async () => {
            console.log("Product Id:", pid);
            let u1 = await bitsflea.getUser(sdk.sender);
            let aliceBalance1 = await point.balanceOf(alice.sender);
            let senderBalance1 = await point.balanceOf(sdk.sender);

            await sdk.waitingResult(await bitsflea.connect(alice.accountPri).review(pid, false, ""));

            let aliceBalance2 = await point.balanceOf(alice.sender);
            let senderBalance2 = await point.balanceOf(sdk.sender);
            let u2 = await bitsflea.getUser(sdk.sender);

            assert.ok(aliceBalance2.minus(aliceBalance1).toString(10) == "5000000000", "Salary error");
            assert.ok(senderBalance2.minus(senderBalance1).toString(10) == "5000000000", "publishAward error");
            assert.ok(u2.creditValue - u1.creditValue == 1, "creditPublish error");
        });

        it("Delist and sub credit", async () => {
            console.log("Product Id:", pid);

            let u1 = await bitsflea.getUser(sdk.sender);
            await sdk.waitingResult(await bitsflea.connect(alice.accountPri).review(pid, true, "商品违规"));
            let u2 = await bitsflea.getUser(sdk.sender);
            assert.ok(u1.creditValue - u2.creditValue == 5, "creditInvalidPublish error");
        });
    });

    describe("Clean Order", () => {
        it("clean order", async () => {
            let balance1 = await point.balanceOf(alice.sender)
            let result = await sdk.waitingResult(await bitsflea.connect(alice.accountPri).cleanOrder());
            let balance2 = await point.balanceOf(alice.sender)
            assert.ok(balance2.gte(balance1), "clean order error");
        });
    });

    describe("Arbitration", () => {

        it("Product arbit", async () => {
            // publish product
            let pid = await bitsflea.newProductId(lilei.sender);
            pid = pid.toString(10);

            let postage = parseNULS(10).toString();
            let price = parseNULS(100).toString();

            let txHash = await bitsflea.connect(lilei.accountPri).publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
                `${postage},0,0`, `${price},0,0`);
            await sdk.waitingResult(txHash);

            // applyArbit report lilei
            let result = await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).applyArbit(lilei.sender, pid, null, 200, "商品违规"));
            let event = getEvent(result, "ApplyArbitEvent");
            console.debug("event:", event);
            let aid = event.payload.aid;
            let arbit = await bitsflea.getArbit(aid);
            console.debug("arbit:", arbit);

            assert.equal(arbit.type, 200, "arbit type error");
            assert.equal(arbit.status, 0, "arbit status error");
            assert.equal(arbit.id, aid, "arbit id error");

            // Participation in arbitration bob,alice,carol
            await bitsflea.connect(sdk.accountPri).inArbit(aid).catch(reason => {
                assert.equal(reason, "30005", "30005 error");
            });

            await sdk.waitingResult(await bitsflea.connect(bob.accountPri).inArbit(aid));
            arbit = await bitsflea.getArbit(aid);
            assert.equal(arbit.status, 100, "bob in arbit status error");

            await sdk.waitingResult(await bitsflea.connect(alice.accountPri).inArbit(aid));
            arbit = await bitsflea.getArbit(aid);
            assert.equal(arbit.status, 100, "alice in arbit status error");

            // vote fail 30012
            await bitsflea.voteArbit(aid, true).catch(reason => {
                assert.equal(reason, "30012", "30012 error");
            });

            // updateArbit fail 30016
            await bitsflea.connect(carol.accountPri).updateArbit(aid, "仲裁说明及证明材料").catch(reason => {
                assert.equal(reason, "30016", "30016 error");
            });

            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).inArbit(aid));
            arbit = await bitsflea.getArbit(aid);
            assert.equal(arbit.status, 200, "carol in arbit status error");

            // vote fail 30017
            await bitsflea.voteArbit(aid, true).catch(reason => {
                assert.equal(reason, "30017", "30017 error");
            });

            // updateArbit fail
            await bitsflea.connect(sdk.accountPri).updateArbit(aid, "仲裁说明及证明材料").catch(reason => {
                assert.equal(reason, "30005", "30005 error");
            });

            // updateArbit Success
            await sdk.waitingResult(await bitsflea.connect(alice.accountPri).updateArbit(aid, "仲裁说明及证明材料"));
            arbit = await bitsflea.getArbit(aid);
            assert.equal(arbit.proofContent, "仲裁说明及证明材料", "update arbit error");


            // bob,alice,carol start voting for arbitration
            await sdk.waitingResult(await bitsflea.connect(alice.accountPri).voteArbit(aid, true));

            await bitsflea.connect(alice.accountPri).voteArbit(aid, true).catch(reason => {
                assert.equal(reason, "30014", "30014 error");
            });

            await bitsflea.connect(bob.accountPri).voteArbit(aid, true);
            let [b1, b2, b3, u] = await Promise.all([
                point.balanceOf(bob.sender),
                point.balanceOf(alice.sender),
                point.balanceOf(carol.sender),
                bitsflea.getUser(lilei.sender)
            ]);
            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).voteArbit(aid, true, { gasLimitTimes: 5 }));
            let [b11, b12, b13, u1, p] = await Promise.all([
                point.balanceOf(bob.sender),
                point.balanceOf(alice.sender),
                point.balanceOf(carol.sender),
                bitsflea.getUser(lilei.sender),
                bitsflea.getProduct(pid)
            ]);

            assert.equal(u.creditValue - u1.creditValue, 100, "creditValue error");
            assert.equal(b11.minus(b1).toString(10), "20000000000", "b1 Salary error");
            assert.equal(b12.minus(b2).toString(10), "20000000000", "b2 Salary error");
            assert.equal(b13.minus(b3).toString(10), "20000000000", "b3 Salary error");
            assert.equal(p.status, 300, "product status error");
        });
    });
    */

    describe("Arbitration Order", async () => {
        let pid;
        let orderId;

        let postage = parseNULS(10).toString();
        let price = parseNULS(100).toString();

        before(async () => {
            // lilei publish product
            pid = await bitsflea.newProductId(lilei.sender);
            pid = pid.toString(10);

            let txHash = await bitsflea.connect(lilei.accountPri).publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
                `${postage},0,0`, `${price},0,0`);
            await sdk.waitingResult(txHash);

            // alice review product
            await sdk.waitingResult(await bitsflea.connect(alice.accountPri).review(pid, false, "合格"));
        });

        /*
        it("buyer winner arbit", async () => {
            // owner buy product
            orderId = await bitsflea.newOrderId(sdk.sender, pid);
            orderId = orderId.toString(10);
            await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).placeOrder(orderId, 1, null));
            // owner pay order
            let pointAddress = await bitsflea.getPoint();
            let point = await sdk.contract(pointAddress);
            await sdk.waitingResult(await point.connect(sdk.accountPri).transferAndCall(bitsflea.address, parseNULS(110).toString(10), orderId));
            // lilei shipment
            await sdk.waitingResult(await bitsflea.connect(lilei.accountPri).shipments(orderId, "123456789"));
            // owner apply arbit
            let result = await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).applyArbit(lilei.sender, null, orderId, 0, "卖家不想退货"));
            let event = getEvent(result, "ApplyArbitEvent");
            let aid = event.payload.aid;
            // in arbitration bob,alice,carol
            await bitsflea.connect(bob.accountPri).inArbit(aid);
            await bitsflea.connect(alice.accountPri).inArbit(aid);
            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).inArbit(aid));
            // carol update arbit
            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).updateArbit(aid, "仲裁说明及证明材料"));
            // bob, alice, carol start voting for arbit
            await bitsflea.connect(bob.accountPri).voteArbit(aid, true);
            await bitsflea.connect(alice.accountPri).voteArbit(aid, true);
            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).voteArbit(aid, true, { gasLimitTimes: 3 }));
            // check
            let [order, returns] = await Promise.all([
                bitsflea.getOrder(orderId),
                bitsflea.getProductReturn(orderId)
            ]);
            assert.equal(order.status, 800, "order status error");
            assert.equal(returns.status, 0, "returns status error");
            // owner shipments
            await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).reShipments(orderId, "12345678966"));
            // get balance
            let [buyerB, sellerB] = await Promise.all([
                point.balanceOf(sdk.sender),
                point.balanceOf(lilei.sender)
            ]);
            // lilei confirm receipt
            await sdk.waitingResult(await bitsflea.connect(lilei.accountPri).reConfirmReceipt(orderId));
            // check
            let [o, r, p, buyerB2, sellerB2] = await Promise.all([
                bitsflea.getOrder(orderId),
                bitsflea.getProductReturn(orderId),
                bitsflea.getProduct(pid),
                point.balanceOf(sdk.sender),
                point.balanceOf(lilei.sender)
            ]);
            assert.equal(o.status, 200, "order status error");
            assert.equal(r.status, 200, "returns status error");
            assert.equal(p.status, 100, "product status error");
            assert.equal(buyerB2.minus(buyerB).toString(10), price, "buyer balance error");
            assert.equal(sellerB2.minus(sellerB).toString(10), postage, "seller balance error");
        });
        */

        it("seller winner arbit", async () => {
            // owner buy product
            orderId = await bitsflea.newOrderId(sdk.sender, pid);
            orderId = orderId.toString(10);
            await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).placeOrder(orderId, 1, null));
            // owner pay order
            let pointAddress = await bitsflea.getPoint();
            let point = await sdk.contract(pointAddress);
            await sdk.waitingResult(await point.connect(sdk.accountPri).transferAndCall(bitsflea.address, parseNULS(110).toString(10), orderId));
            // lilei shipment
            await sdk.waitingResult(await bitsflea.connect(lilei.accountPri).shipments(orderId, "123456789"));
            // owner apply arbit
            let result = await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).applyArbit(lilei.sender, null, orderId, 0, "卖家不想退货"));
            let event = getEvent(result, "ApplyArbitEvent");
            let aid = event.payload.aid;
            // in arbitration bob,alice,carol
            await bitsflea.connect(bob.accountPri).inArbit(aid);
            await bitsflea.connect(alice.accountPri).inArbit(aid);
            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).inArbit(aid));
            // carol update arbit
            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).updateArbit(aid, "仲裁说明及证明材料"));
            // bob, alice, carol start voting for arbit
            await bitsflea.connect(bob.accountPri).voteArbit(aid, false);
            await bitsflea.connect(alice.accountPri).voteArbit(aid, false);
            // get balance
            let [sellerB] = await Promise.all([
                point.balanceOf(lilei.sender)
            ]);
            await sdk.waitingResult(await bitsflea.connect(carol.accountPri).voteArbit(aid, false, { gasLimitTimes: 5 }));
            // check
            let [order, sellerB2, p, g] = await Promise.all([
                bitsflea.getOrder(orderId),
                point.balanceOf(lilei.sender),
                bitsflea.getProduct(pid),
                bitsflea.getGlobal()
            ]);
            let total = parseNULS(110);
            let income = total.minus(total.times(50).div(1000));
            if (g.tradeReward === "true") {
                let reward = parseNULS(100).times(50).div(1000);
                income = income.plus(reward);
            }
            assert.equal(order.status, 600, "order status error");
            assert.ok(sellerB2.minus(sellerB).eq(income), "seller balance error");
            assert.equal(p.status, 200, "product status error");
        });
    });

});