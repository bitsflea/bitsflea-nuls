import * as assert from 'assert';
import * as tools from "./tools.js";
import { decrypt } from "nuls-sdk-js/src/crypto/eciesCrypto.js"
import CryptoJS from "crypto-js";

describe('Bitsflea', function () {
    this.timeout(200000);

    describe("Crypto", function () {
        it("Shared key symmetric encryption", function () {
            const msg = "158359999";
            //tNULSeBaN1pFZDRf5DzqGPqC4juCYExdbv1zKk

            const aPriKey = "768e3f0587303809ba4c80f2b7d1b15b839a9398439d02d8789e5c1a1962323c";
            const aPubKey = tools.getPublic(aPriKey);
            const bPriKey = "b46d1de5c5635a18becc5ce98a35b1d8236af19ee697010ca1eb57c6b85a7034";
            const bPubKey = tools.getPublic(bPriKey);

            const encrypted = tools.encrypt(aPriKey, bPubKey, msg);
            console.log(encrypted, bPubKey, Buffer.from(bPubKey,'hex').length)
            const decrypted = tools.decrypt(bPriKey, aPubKey, encrypted);
            assert.equal(msg, decrypted);
        });
    });
});