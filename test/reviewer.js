import { env, sdk, contract } from "./config.js"

import * as assert from 'assert';
import * as tools from "./tools.js";

describe('Bitsflea', function () {
    this.timeout(200000);

    let bitsflea;
    let alice;
    let bob;
    let carol;
    let lilei;

    before(async function () {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
        carol = sdk.account(env.KEY_CAROL);
        lilei = sdk.account(env.KEY_LILEI);
    });

    describe("Reviewer", function () {

        before(async function () {
            let result = await bitsflea.getGlobal();
            assert.ok(!!result, "Failed to get global status");

            const encryptKey = result.encryptKey;
            let _bob = await bitsflea.getUser(bob.sender);
            if (!_bob) {
                const phone = "18580599998";
                let nickname = "bob";
                let phoneHash = tools.getHash(phone);
                let phoneEncrypt = tools.encrypt(env.KEY_BOB, encryptKey, phone);
                let referrer = "";
                let head = "";

                result = await bitsflea.connect(bob.accountPri).regUser(nickname, phoneHash, phoneEncrypt, referrer, head);
                await sdk.waitingResult(result);
            }
            let _alice = await bitsflea.getUser(alice.sender);
            if (!_alice) {
                const phone = "18580599997";
                let nickname = "alice";
                let phoneHash = tools.getHash(phone);
                let phoneEncrypt = tools.encrypt(env.KEY_ALICE, encryptKey, phone);
                let referrer = "";
                let head = "";

                result = await bitsflea.connect(alice.accountPri).regUser(nickname, phoneHash, phoneEncrypt, referrer, head);
                await sdk.waitingResult(result);
            }
            let _carol = await bitsflea.getUser(carol.sender);
            if (!_carol) {
                const phone = "18580599996";
                let nickname = "carol";
                let phoneHash = tools.getHash(phone);
                let phoneEncrypt = tools.encrypt(env.KEY_CAROL, encryptKey, phone);
                let referrer = "";
                let head = "";

                result = await bitsflea.connect(carol.accountPri).regUser(nickname, phoneHash, phoneEncrypt, referrer, head);
                await sdk.waitingResult(result);
            }
            let _lilei = await bitsflea.getUser(lilei.sender);
            if (!_lilei) {
                const phone = "18580599995";
                let nickname = "lilei";
                let phoneHash = tools.getHash(phone);
                let phoneEncrypt = tools.encrypt(env.KEY_LILEI, encryptKey, phone);
                let referrer = "";
                let head = "";

                result = await bitsflea.connect(lilei.accountPri).regUser(nickname, phoneHash, phoneEncrypt, referrer, head);
                await sdk.waitingResult(result);
            }

        });

        it("Vote reviewer", async function () {
            let [_alice, _bob, _carol] = await Promise.all([
                bitsflea.getUser(alice.sender),
                bitsflea.getUser(bob.sender),
                bitsflea.getUser(carol.sender)
            ]);

            if (!_alice.isReviewer) {
                let reviewer = await bitsflea.getReviewer(alice.sender);
                if (!reviewer) {
                    await sdk.waitingResult(await bitsflea.connect(alice.accountPri).appReviewer());
                }

                // 30001
                await bitsflea.connect(alice.accountPri).voteReviewer(alice.sender, true).catch(reason => {
                    assert.equal(reason, "30001", "30001 error");
                });
                // 30002
                await bitsflea.connect(alice.accountPri).voteReviewer(bob.sender, true).catch(reason => {
                    assert.equal(reason, "30002", "30002 error");
                });

                // Can vote successfully
                await sdk.waitingResult(await bitsflea.connect(bob.accountPri).voteReviewer(alice.sender, true));
            }

            _alice = await bitsflea.getUser(alice.sender);
            assert.ok(_alice.isReviewer, "isReviewer error");

            // 30004
            await bitsflea.connect(bob.accountPri).voteReviewer(alice.sender, true).catch(reason => {
                assert.equal(reason, "30004", "30004 error");
            });

            if (!_bob.isReviewer) {
                await sdk.waitingResult(await bitsflea.connect(bob.accountPri).appReviewer());
                await bitsflea.connect(alice.accountPri).voteReviewer(bob.sender, true);
            }
            if (!_carol.isReviewer) {
                await sdk.waitingResult(await bitsflea.connect(carol.accountPri).appReviewer());
                await bitsflea.connect(sdk.accountPri).voteReviewer(carol.sender, true);
            }
        });
    });
});