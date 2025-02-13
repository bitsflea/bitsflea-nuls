import { env, sdk, contract } from "./config.js"

import * as assert from 'assert';
import * as tools from "./tools.js";
import { parseNULS } from "nuls-api-v2";

describe("Platform", function () {
    this.timeout(200000);

    let bitsflea;
    let point;

    before(async function () {
        bitsflea = await sdk.contract(contract);
        let pointAddress = await bitsflea.getPoint();
        point = await sdk.contract(pointAddress);
    });

    describe("Deposit pool", function () {
        it("Can only deposit points", async () => {
            let refPool = parseNULS(100000000);
            let salaryPool = parseNULS(200000000);
            let sysPool = parseNULS(350000000);

            let allAmount = refPool.plus(salaryPool).plus(sysPool);
            let balance = await point.balanceOf(sdk.sender);
            console.log("balance:", balance);
            assert.ok(BigInt(balance) >= BigInt(allAmount.toString(10)), "Insufficient balance");

            await sdk.waitingResult(await point.approve(bitsflea.address, allAmount.toString(10)));

            await sdk.waitingResult(await bitsflea.depositRefPool(refPool.toString(10)));
            await sdk.waitingResult(await bitsflea.depositSalaryPool(salaryPool.toString(10)));
            await sdk.waitingResult(await bitsflea.depositSysPool(sysPool.toString(10)));

            let state = await bitsflea.getGlobal();
            assert.ok(BigInt(state.salaryPool) >= BigInt(salaryPool.toString(10)), "salary pool error");
            assert.ok(BigInt(state.refPool) >= BigInt(refPool.toString(10)), "ref pool error");
            assert.ok(BigInt(state.sysPool) >= BigInt(sysPool.toString(10)), "sys pool error");
        });
    });
});