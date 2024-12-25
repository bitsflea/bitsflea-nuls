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

    before(async function () {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
        carol = sdk.account(env.KEY_CAROL);
        lilei = sdk.account(env.KEY_LILEI);

        let pointAddress = await bitsflea.getPoint();
        point = await sdk.contract(pointAddress);
    });

    describe("Review product", () => {

        let pid;

        beforeEach(async () => {
            pid = await bitsflea.newProductId(sdk.sender);
            pid = pid.toString(10);

            let postage = parseNULS(10).toString();
            let price = parseNULS(100).toString();

            let txHash = await bitsflea.connect(sdk.accountPri).publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
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

            let txHash = await bitsflea.connect(lilei.accountPri).publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
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

});