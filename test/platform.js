import { env, sdk, contract } from "./config.js"

import * as assert from 'assert';
import * as tools from "./tools.js";
import { parseNULS } from "nuls-api-v2";

describe("Platform", function () {
    this.timeout(200000);

    let bitsflea;   // contract
    
    let alice;  // reviewer
    let bob;    // reviewer
    let carol;  // reviewer

    before(async function () {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
        carol = sdk.account(env.KEY_CAROL);
    });

});