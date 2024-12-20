import { env, sdk, contract } from "./config.js"

import * as assert from 'assert';
import * as tools from "./tools.js";

describe('User', function () {
    this.timeout(200000);

    let bitsflea;
    let alice;
    let bob;
    this.beforeAll(async () => {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
    });

    it('Register a new user', async function () {

        let result = await bitsflea.getGlobal();
        assert.ok(!!result, "Failed to get global status");

        const phone = "18580599999";
        let nickname = "necklace";
        let phoneHash = tools.getHash(phone);
        let phoneEncrypt = tools.encrypt(env.KEY_SENDER, result.encryptKey, phone);
        let referrer = "";
        let head = "";

        result = await bitsflea.regUser(nickname, phoneHash, phoneEncrypt, referrer, head);
        console.log("regUser:", result);

        await sdk.waitingResult(result);

        result = await bitsflea.getUser(sdk.sender);
        console.log("getUser:", result);

        assert.equal(result.nickname, nickname, "nickname error");
    });

    it("Set Profile", async function () {
        let info = await bitsflea.getUser(sdk.sender);
        // console.log("info:", info);
        let txHash = await bitsflea.setProfile(null, "http://1110");

        await sdk.waitingResult(txHash);

        info = await bitsflea.getUser(sdk.sender);
        // console.log("info:", info);
        assert.equal(info.head, "http://1110", "head error");
    });
});