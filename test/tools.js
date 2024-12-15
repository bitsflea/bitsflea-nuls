import CryptoJS from "crypto-js";
import elliptic from "elliptic";

const Elliptic = elliptic.ec;
const ec = new Elliptic("secp256k1");

export function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

export function getHash(data) {
    return CryptoJS.SHA256(data).toString(CryptoJS.enc.Hex);
}

export function getPublic(privateKey) {
    return ec.keyFromPrivate(privateKey).getPublic(true, "hex");
}

export function createShareKey(priKey, pubKey) {
    let aKey = ec.keyFromPrivate(priKey, "hex");
    let bKey = ec.keyFromPublic(pubKey, "hex").getPublic();
    const sharedKey = aKey.derive(bKey).toString(16);
    return CryptoJS.enc.Hex.parse(sharedKey);
}

export function encrypt(priKey, pubKey, msg) {
    const keyHex = createShareKey(priKey, pubKey);
    const iv = CryptoJS.lib.WordArray.random(16);
    const encrypted = CryptoJS.AES.encrypt(msg, keyHex, { iv }).ciphertext;
    return iv.concat(encrypted).toString(CryptoJS.enc.Base64);
}

export function decrypt(priKey, pubKey, encryptedMsg) {
    const encryptedWordArray = CryptoJS.enc.Base64.parse(encryptedMsg);
    const ivFromEncrypted = CryptoJS.lib.WordArray.create(encryptedWordArray.words.slice(0, 4), 16);
    const ciphertext = CryptoJS.lib.WordArray.create(encryptedWordArray.words.slice(4));
    const keyHex = createShareKey(priKey, pubKey);
    return CryptoJS.AES.decrypt(
        { ciphertext },
        keyHex,
        { iv: ivFromEncrypted }
    ).toString(CryptoJS.enc.Utf8);
}

export function getEvent(txResult, eventName = null, contractAddress = null) {
    if (eventName == null || eventName == "") return null;
    if ("events" in txResult) {
        if (contractAddress == null) {
            contractAddress = txResult.contractAddress;
        }
        for (let event of txResult.events) {
            let str = `"event":"${eventName}"`;
            if (event.includes(str)) {
                const obj = JSON.parse(event);
                if (obj.contractAddress === contractAddress)
                    return obj
            }
        }
    }
    return null;
}