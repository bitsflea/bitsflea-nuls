import * as dotenv from 'dotenv';
dotenv.config();

import { NULSAPI } from "nuls-api-v2";

export const env = process.env;
export const contract = "tNULSeBaMzYcrWNadQdCunL8JLhKXrvVzDGrFE";
export const sdk = new NULSAPI({ rpcURL: "https://beta.api.nuls.io/jsonrpc", isBeta: true, accountPri: env.KEY_SENDER });