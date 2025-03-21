import * as dotenv from 'dotenv';
dotenv.config();

import { NULSAPI } from "nuls-api-v2";

export const env = process.env;
export const contract = "NULSd6Hh1LacznpTiQ7K7vKbbnqARVRriZgZM";
export const sdk = new NULSAPI({ rpcURL: "https://api.nuls.io/jsonrpc", isBeta: false, accountPri: env.KEY_SENDER });