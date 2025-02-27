import { env, sdk, contract } from "./config.js"

const bitsflea = await sdk.contract(contract);

async function addCoins() {
    await bitsflea.addCoin(0, 0, 250)           // 1BTF 给出0.05BTF
    await bitsflea.addCoin(2, 1, 250)           // 1NULS 给出0.05BTF
    await bitsflea.addCoin(2, 201, 80000000)    // 1BTC 给出16000BTF
    await bitsflea.addCoin(2, 202, 2500000)     // 1ETH 给出500BTF
    await bitsflea.addCoin(5, 7, 1000)          // 1USDT 给出0.2BTF
    let hash = await bitsflea.addCoin(5, 1, 5)  // 1000NVT 给出1BTF
    await sdk.waitingResult(hash)

    let coins = await bitsflea.getCoins()
    console.log("coins:", coins)
}

async function addCategorys() {
    await bitsflea.addCategory(1, "Electronics", 0)
    await bitsflea.addCategory(2, "Toys", 0)
    await bitsflea.addCategory(3, "Beauty", 0)
    await bitsflea.addCategory(4, "Home", 0)
    await bitsflea.addCategory(5, "Fashion", 0)
    await bitsflea.addCategory(6, "Books", 0)
    let hash = await bitsflea.addCategory(7, "Virtual", 0)
    await sdk.waitingResult(hash)

    let categorys = await bitsflea.getCategories()
    console.log("categorys:", categorys)
}

async function main() {
    await addCoins()
    await addCategorys()
}

main().catch(console.error)