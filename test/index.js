import * as assert from 'assert';
import * as tools from "./tools.js";

describe('Bitsflea', function () {
    this.timeout(200000);

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
});