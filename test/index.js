import * as dotenv from 'dotenv';
import { NULSAPI } from "nuls-api-v2";
import * as assert from 'assert';
import * as tools from "./tools.js";

dotenv.config();

const env = process.env;
const sender = "tNULSeBaMo7JMx1mvKtgVFfPPvo73ZU5zVNXFU";
const contract = "tNULSeBaN9n44X77oPFENk2XUSWYKZPAHCwZL6";
const sdk = new NULSAPI({ rpcURL: "http://beta.api.nuls.io/jsonrpc", isBeta: true, sender });

describe('Bitsflea', function () {

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
        it('Register a new user', async function () {
            this.timeout(20000);

            let result = await sdk.invokeView(contract, "getGlobal", "", []);
            assert.ok(!!result, "Failed to get global status");

            const phone = "18580599999";
            let nickname = "necklace";
            let phoneHash = tools.getHash(phone);
            let phoneEncrypt = tools.encrypt(env.KEY_SENDER, result.encryptKey, phone);
            let referrer = "";
            let head = "";

            result = await sdk.callContract(env.KEY_SENDER, {
                contractAddress: contract,
                methodName: "regUser",
                methodDesc: "(String nickname, String phoneHash, String phoneEncrypt, Address referrer, String head) return void",
                args: [nickname, phoneHash, phoneEncrypt, referrer, head],
                value: 0
            });
            console.log("regUser:", result);

            result = await sdk.invokeView(contract, "getUser", "", [sender]);
            console.log("getUser:", result);

            assert.equal(result.nickname, nickname);
        });

        it("Set Profile", async function () {
            this.timeout(20000);
        });
    });

});