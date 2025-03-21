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
        let head = "bafkreigwtvsing3bzjhmhhfv3nnerll5ouw2xjizqytt6qppctv7wbteam";
        let extendInfo = "bafkreihcv2kpbmdjdf7rq3lcnvon6yc4k6fsuxwhsaxe7ue7ft4b3lzwpm";

        result = await bitsflea.regUser(nickname, phoneHash, phoneEncrypt, referrer, head, extendInfo);
        console.log("regUser:", result);

        await sdk.waitingResult(result);

        result = await bitsflea.getUser(sdk.sender);
        console.log("getUser:", result);

        assert.equal(result.nickname, nickname, "nickname error");
    });

    it("Set Profile", async function () {
        let info = await bitsflea.getUser(sdk.sender);
        // console.log("info:", info);
        let txHash = await bitsflea.setProfile(null, "https://ix-marketing.imgix.net/genfill.png?auto=format,compress&w=3038", "bafkreidvtlajlk4l5osidgd6yautr7iflbhxm5yxfillc6d2aa3upn65yu");

        await sdk.waitingResult(txHash);

        info = await bitsflea.getUser(sdk.sender);
        // console.log("info:", info);
        assert.equal(info.head, "https://ix-marketing.imgix.net/genfill.png?auto=format,compress&w=3038", "head error");
    });
});