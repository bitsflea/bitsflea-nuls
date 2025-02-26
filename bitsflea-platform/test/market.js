import { env, sdk, contract } from "./config.js"

import * as assert from 'assert';
import * as tools from "./tools.js";
import { parseNULS } from "nuls-api-v2";

describe("Market", function () {
    this.timeout(200000);

    let bitsflea;
    let alice;  // reviewer
    let bob;

    let pid;
    let delistPid;
    const description = "bagaaieran3gqmu65wp4fjccrgidryjyfdxkubvm2fihe6u52qztirfxi56xq";
    const location = "34.0522,-118.2437|US,Los Angeles,California";

    before(async function () {
        bitsflea = await sdk.contract(contract);
        alice = sdk.account(env.KEY_ALICE);
        bob = sdk.account(env.KEY_BOB);
    });

    it("Add category", async () => {
        let txHash = await bitsflea.addCategory(1, "数码", 0).catch(reason => {
            if (reason !== "20024") {
                throw reason;
            }
        });
        if (txHash) {
            let txResult = await sdk.waitingResult(txHash);
            let event = tools.getEvent(txResult, "AddCategoryEvent");
            assert.equal(1, event.payload.id, "id error");
            assert.equal("数码", event.payload.view, "id error");
        }
    });

    it("Publish product", async () => {
        pid = await bitsflea.newProductId(sdk.sender);
        pid = pid.toString(10);
        console.log("pid:", pid);

        let postage = parseNULS(10).toString();
        let price = parseNULS(100).toString();
        console.log(`postage: ${postage} price: ${price}`);

        let txHash = await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},0,0`, `${price},0,0`);

        let txResult = await sdk.waitingResult(txHash);
        let event = tools.getEvent(txResult, "PublishProductEvent");
        assert.ok(pid == event.payload.pid, "pid error");
        assert.ok(sdk.sender == event.payload.uid, "uid error");

        await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},0,0`, `${price},0,0`).catch(reason => {
                assert.equal(reason, "20001", "20001 error");
            });

        pid = await bitsflea.newProductId(sdk.sender);
        pid = pid.toString(10);

        await bitsflea.publish(pid, 2, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},0,0`, `${price},0,0`).catch(reason => {
                assert.equal(reason, "20025", "20025 error");
            });

        await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 0, 1,
            `${postage},0,0`, `${price},0,0`).catch(reason => {
                assert.equal(reason, "20002", "20002 error");
            });

        await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 2, 1, 1,
            `${postage},0,0`, `${price},0,0`).catch(reason => {
                assert.equal(reason, "20004", "20004 error");
            });

        await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 1, 2,
            `${postage},0,0`, `${price},0,0`).catch(reason => {
                assert.equal(reason, "20005", "20005 error");
            });

        await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},3,1`, `${price},3,1`).catch(reason => {
                assert.equal(reason, "20022", "20022 error");
            });

        await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},0,0`, `${price},3,1`).catch(reason => {
                assert.equal(reason, "20018", "20018 error");
            });

        await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `-1,0,0`, `${price},0,0`).catch(reason => {
                assert.equal(reason, "20020", "20020 error");
            });
    });

    it("Review product", async () => {
        let product = await bitsflea.getProduct(pid);
        if (product) {
            if (product.status == 0) {
                let txHash = await bitsflea.connect(alice.accountPri).review(pid, false, "商品合格");
                await sdk.waitingResult(txHash);
                product = await bitsflea.getProduct(pid);
            }
            assert.equal(product.status, 100, "product status error");
        }
    });

    it("Delist product", async () => {
        let product = await bitsflea.getProduct(pid);
        if (product) {
            let txHash = await bitsflea.delist(pid);
            await sdk.waitingResult(txHash);
            product = await bitsflea.geProduct(pid);
            assert.equal(product.status, 300, "product status error");
        }
        delistPid = pid;
    });

    it("placeOrder", async () => {
        console.log("pid:", pid);
        pid = await bitsflea.newProductId(sdk.sender);
        pid = pid.toString(10);


        let postage = parseNULS(10).toString();
        let price = parseNULS(100).toString();
        await sdk.waitingResult(await bitsflea.publish(pid, 1, pid, description, true, false, true, location, 0, 1, 1,
            `${postage},0,0`, `${price},0,0`));

        console.log("pid:", pid);
        await sdk.waitingResult(await bitsflea.connect(alice.accountPri).review(pid, false, "商品合格"));

        let orderId = await bitsflea.newOrderId(bob.sender, delistPid);
        orderId = orderId.toString(10);
        console.log("orderId:", orderId);
        await bitsflea.connect(bob.accountPri).placeOrder(orderId, 1, null).catch(reason => {
            assert.equal(reason, "20007", "20007 error");
        });

        orderId = await bitsflea.newOrderId(sdk.sender, pid);
        orderId = orderId.toString(10);
        console.log("orderId:", orderId);
        await bitsflea.connect(sdk.accountPri).placeOrder(orderId, 1, null).catch(reason => {
            assert.equal(reason, "20010", "20010 error");
        });

        orderId = await bitsflea.newOrderId(bob.sender, pid);
        orderId = orderId.toString(10);
        console.log("orderId:", orderId);
        await bitsflea.connect(bob.accountPri).placeOrder(`1${orderId}`, 1, null).catch(reason => {
            assert.equal(reason, "20009", "20009 error");
        });

        // Success placed an order
        let txHash = await bitsflea.connect(bob.accountPri).placeOrder(orderId, 1, null);
        await sdk.waitingResult(txHash);

        let [product, order] = await Promise.all([
            bitsflea.getProduct(pid),
            bitsflea.getOrder(orderId)
        ]);
        console.log("order:", order);
        assert.equal(product.status, 400, "product status error");
        assert.equal(order.status, 0, "order status error");
        assert.equal(order.amount.value, price, "order price error");
        assert.equal(order.postage.value, postage, "order postage error");
        assert.equal(order.buyer, bob.sender, "order buyer error");
        assert.equal(order.seller, sdk.sender, "order seller error");

        // Pay order
        let pointAddress = await bitsflea.getPoint();
        let point = await sdk.contract(pointAddress);
        await sdk.waitingResult(await point.transfer(bob.sender, parseNULS(220).toString(10)));

        // 20020
        await point.connect(bob.accountPri).transferAndCall(bitsflea.address, parseNULS(10).toString(10), orderId).catch(reason => {
            assert.equal(reason, "20020", "20020 error");
        });

        await sdk.waitingResult(await point.transferAndCall(bitsflea.address, parseNULS(110).toString(10), orderId));

        order = await bitsflea.getOrder(orderId);
        assert.equal(order.status, 300, "order status error");

        // 20011
        await point.transferAndCall(bitsflea.address, parseNULS(110).toString(10), orderId).catch(reason => {
            assert.equal(reason, "20011", "20011 error");
        });

        // Shipping
        await bitsflea.connect(alice.accountPri).shipments(orderId, "123456789").catch(reason => {
            assert.equal(reason, "20012", "20012 error");
        });

        await bitsflea.connect(sdk.accountPri).shipments(`1${orderId}`, "123456789").catch(reason => {
            assert.equal(reason, "20009", "20009 error");
        });

        await bitsflea.shipments(orderId, "b29f4b2201860e128c27ba4f86642d461c975ff254b233950b42c6ef11aef3d4a").catch(reason => {
            assert.equal(reason, "20013", "20013 error");
        });

        await sdk.waitingResult(await bitsflea.shipments(orderId, "123456789"));
        order = await bitsflea.getOrder(orderId);
        assert.equal(order.status, 400, "order status error");

        await bitsflea.shipments(orderId, "123456789").catch(reason => {
            assert.equal(reason, "20011", "20011 error");
        });

        //Receiving
        await bitsflea.confirmReceipt(`1${orderId}`).catch(reason => {
            assert.equal(reason, "20009", "20009 error");
        });

        await bitsflea.confirmReceipt(orderId).catch(reason => {
            assert.equal(reason, "20012", "20012 error");
        });

        await sdk.waitingResult(await bitsflea.connect(bob.accountPri).confirmReceipt(orderId));
        order = await bitsflea.getOrder(orderId);
        assert.equal(order.status, 600, "order status error");

        await bitsflea.connect(bob.accountPri).confirmReceipt(orderId).catch(reason => {
            assert.equal(reason, "20011", "20011 error");
        });
    });

    it("placeOrder stockCount", async () => {
        pid = await bitsflea.newProductId(sdk.sender);
        pid = pid.toString(10);
        console.log("pid:", pid);

        let postage = parseNULS(10).toString();
        let price = parseNULS(100).toString();
        await sdk.waitingResult(await bitsflea.connect(sdk.accountPri).publish(pid, 1, pid, description, true, true, true, location, 0, 2, 1,
            `${postage},0,0`, `${price},0,0`));

        await sdk.waitingResult(await bitsflea.connect(alice.accountPri).review(pid, false, "商品合格"));

        let orderId = await bitsflea.newOrderId(bob.sender, pid);
        orderId = orderId.toString(10);
        console.log("orderId:", orderId);

        let txHash = await bitsflea.connect(bob.accountPri).placeOrder(orderId, 1, null);
        await sdk.waitingResult(txHash);

        let [product, order] = await Promise.all([
            bitsflea.getProduct(pid),
            bitsflea.getOrder(orderId)
        ]);
        console.log("order:", order);
        assert.equal(product.status, 100, "product status error");
        assert.equal(order.status, 0, "order status error");
        assert.equal(order.amount.value, price, "order price error");
        assert.equal(order.postage.value, postage, "order postage error");
        assert.equal(order.buyer, bob.sender, "order buyer error");
        assert.equal(order.seller, sdk.sender, "order seller error");

        orderId = await bitsflea.newOrderId(bob.sender, pid);
        orderId = orderId.toString(10);
        console.log("orderId:", orderId);

        txHash = await bitsflea.connect(bob.accountPri).placeOrder(orderId, 1, null);
        await sdk.waitingResult(txHash);

        [product, order] = await Promise.all([
            bitsflea.getProduct(pid),
            bitsflea.getOrder(orderId)
        ]);
        console.log("order:", order);
        assert.equal(product.status, 400, "product status error");
        assert.equal(order.status, 0, "order status error");
        assert.equal(order.amount.value, price, "order price error");
        assert.equal(order.postage.value, postage, "order postage error");
        assert.equal(order.buyer, bob.sender, "order buyer error");
        assert.equal(order.seller, sdk.sender, "order seller error");
    });

});