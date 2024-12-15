import * as dotenv from 'dotenv';
import { NULSAPI, parseNULS } from "nuls-api-v2";
import * as assert from 'assert';
import * as tools from "./tools.js";

dotenv.config();

const env = process.env;
const contract = "tNULSeBaMvvyjUG6HWpTXmFVVGpQ8Upcqmo5qG";
const sdk = new NULSAPI({ rpcURL: "http://beta.api.nuls.io/jsonrpc", isBeta: true, accountPri: env.KEY_SENDER });

describe('Bitsflea', function () {
    this.timeout(200000);

    let bitsflea;
    let alice;
    beforeEach(async () => {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
    });


    describe("Crypto", function () {
        it("Shared key symmetric encryption", function () {
            const msg = "158359999";

            const aPriKey = "40e26972916bddb6f1bbb536f6b864a8701dd2306b0cc8638418cb677c3e50e3";
            const aPubKey = tools.getPublic(aPriKey);
            const bPriKey = "b46d1de5c5635a18becc5ce98a35b1d8236af19ee697010ca1eb57c6b85a7034";
            const bPubKey = tools.getPublic(bPriKey);

            const encrypted = tools.encrypt(aPriKey, bPubKey, msg);
            const decrypted = tools.decrypt(bPriKey, aPubKey, encrypted);
            assert.equal(msg, decrypted);
        });
    });

    describe("User", function () {

        /*
        it('Register a new user', async function () {
            this.timeout(20000);

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

            await tools.sleep(11000);

            result = await bitsflea.getUser(sdk.sender);
            console.log("getUser:", result);

            assert.equal(result.nickname, nickname, "nickname error");
        });
        */

        /*
        it("Set Profile", async function () {
            let info = await bitsflea.getUser(sdk.sender);
            // console.log("info:", info);
            await bitsflea.setProfile(null, "http://1110");

            await tools.sleep(10000);

            info = await bitsflea.getUser(sdk.sender);
            // console.log("info:", info);
            assert.equal(info.head, "http://1110", "head error");
        });
        */

        /*
        it("appReviewer", async () => {
            await bitsflea.appReviewer();

            await tools.sleep(10000);

            let reviewer = await bitsflea.getReviewer(sdk.sender);
            console.log("reviewer:", reviewer);
            assert.equal(sdk.sender, reviewer.uid, "uid error");
        });
        */
    });

    describe("Market", function () {

        /*
        it("Add category", async () => {
            let txHash = await bitsflea.addCategory(1, "数码", 0);
            await tools.sleep(11000);

            let txResult = await sdk.getContractTxResult(txHash);
            let event = tools.getEvent(txResult, "AddCategoryEvent");
            assert.equal(1, event.payload.id, "id error");
            assert.equal("数码", event.payload.view, "id error");

        });
        */

        it("Publish product", async () => {
            let pid = await bitsflea.newProductId(sdk.sender);
            pid = pid.toString(10);
            console.log("pid:", pid);

            let postage = parseNULS(10).toString();
            let price = parseNULS(100).toString();
            console.log(`postage: ${postage} price: ${price}`);

            let txHash = await bitsflea.publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
                `${postage},0,0`, `${price},0,0`);

            await tools.sleep(10000);

            let txResult = await sdk.getContractTxResult(txHash);
            let event = tools.getEvent(txResult, "PublishProductEvent");
            assert.ok(pid == event.payload.pid, "pid error");
            assert.ok(sdk.sender == event.payload.uid, "uid error");

            await bitsflea.publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
                `${postage},0,0`, `${price},0,0`).catch(reason => {
                    assert.equal(reason, "20001", "20001 error");
                });

            pid = await bitsflea.newProductId(sdk.sender);
            pid = pid.toString(10);

            await bitsflea.publish(pid, 2, "description", true, false, true, "position", 0, 1, 1,
                `${postage},0,0`, `${price},0,0`).catch(reason => {
                    assert.equal(reason, "20025", "20025 error");
                });

            await bitsflea.publish(pid, 1, "description", true, false, true, "position", 0, 0, 1,
                `${postage},0,0`, `${price},0,0`).catch(reason => {
                    assert.equal(reason, "20002", "20002 error");
                });

            await bitsflea.publish(pid, 1, "description", true, false, true, "position", 2, 1, 1,
                `${postage},0,0`, `${price},0,0`).catch(reason => {
                    assert.equal(reason, "20004", "20004 error");
                });

            await bitsflea.publish(pid, 1, "description", true, false, true, "position", 0, 1, 2,
                `${postage},0,0`, `${price},0,0`).catch(reason => {
                    assert.equal(reason, "20005", "20005 error");
                });

            await bitsflea.publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
                `${postage},3,1`, `${price},3,1`).catch(reason => {
                    assert.equal(reason, "20022", "20022 error");
                });

            await bitsflea.publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
                `${postage},0,0`, `${price},3,1`).catch(reason => {
                    assert.equal(reason, "20018", "20018 error");
                });

            await bitsflea.publish(pid, 1, "description", true, false, true, "position", 0, 1, 1,
                `-1,0,0`, `${price},0,0`).catch(reason => {
                    assert.equal(reason, "20020", "20020 error");
                });
        });

    });

});