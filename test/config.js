import * as dotenv from 'dotenv';
dotenv.config();

import { NULSAPI } from "nuls-api-v2";

export const env = process.env;
export const contract = "tNULSeBaN4FAh3mQ7QiejpM8LeL1a47wrT3FaK";
export const sdk = new NULSAPI({ rpcURL: "http://beta.api.nuls.io/jsonrpc", isBeta: true, accountPri: env.KEY_SENDER });