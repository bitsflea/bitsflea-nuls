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

    before(async function () {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
        carol = sdk.account(env.KEY_CAROL);

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
            let pid = await bitsflea.newProductId(sdk.sender);
            pid = pid.toString(10);

            let postage = parseNULS(10).toString();
            let price = parseNULS(100).toString();

            let txHash = await bitsflea.connect(sdk.accountPri).publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
                `${postage},0,0`, `${price},0,0`);
            await sdk.waitingResult(txHash);

            // applyArbit
            let result = await sdk.waitingResult(await bitsflea.connect(alice.accountPri).applyArbit(carol.sender, pid, null, 200, "商品违规"));
            let event = getEvent(result, "ApplyArbitEvent");
            console.debug("event:", event);
            let aid = event.payload.aid;
            let arbit = await bitsflea.getArbit(aid);
            console.debug("arbit:", arbit);
        });
    });

});