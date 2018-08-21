package com.boilertalk.ballet.networking;

import android.os.AsyncTask;
import android.util.Log;

import com.boilertalk.ballet.toolbox.iResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class EtherscanAPI {
    private static final String etherscan_base_address = "https://api-ropsten.etherscan.io";
    private static final String etherscan_tx_pq =
            "/api?module=account&action=txlist&address=$ADDRESS$&sort=asc&startblock=$STARTBLOCK$&page=$PAGE$&offset" +
                    "=$PAGE_SIZE$";

    private String walletAddress;
    private int pageSize;
    private int currPage = 0;
    private long firstBlockNr = 0;

    public EtherscanAPI(String walletAddress, int pageSize) {
        this.walletAddress = walletAddress;
        this.pageSize = pageSize;
    }

    public void reset() {
        currPage = 0;
        firstBlockNr = 0;
    }

    public ArrayList<EtherscanTransaction> getNextPage() {
        String res = null;
        ArrayList<EtherscanTransaction> page = new ArrayList<>();

        try {
            InputStream is = new java.net.URL(etherscan_base_address + etherscan_tx_pq
                    .replace("$ADDRESS$", walletAddress)
                    .replace("$PAGE$", Integer.toString(currPage))
                    .replace("$PAGE_SIZE$", Integer.toString(pageSize))
                    .replace("$STARTBLOCK$", Long.toString(firstBlockNr))).openStream();
            java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            res = s.hasNext() ? s.next() : "";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("EtherscanAPI", "request: " + etherscan_base_address + etherscan_tx_pq
                .replace("$ADDRESS$", walletAddress)
                .replace("$PAGE$", Integer.toString(currPage))
                .replace("$PAGE_SIZE$", Integer.toString(pageSize))
                .replace("$STARTBLOCK$", Long.toString(firstBlockNr))
        );
        Log.d("EtherscanAPI", "result of request: " + res);

        try {
            JSONObject jsoob = new JSONObject(res);
            JSONArray jsar = jsoob.getJSONArray("result");
            for(int i = 0; i < jsar.length(); i++) {
                JSONObject tx = jsar.getJSONObject(i);
                EtherscanTransaction es = new EtherscanTransaction(tx.getLong("blockNumber"),
                        tx.getString("from"), tx.getString("to"), tx.getLong("value"), tx
                        .getLong("timeStamp"), tx.getString("hash"));
                if(!page.contains(es)) {
                    page.add(es);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(currPage == 0) {
            if(page.size() > 0) {
                firstBlockNr = page.get(0).blockNumber;
            }
        }

        currPage++;
        return page;
    }

    public void async_getNextPage(final iResult<ArrayList<EtherscanTransaction>> rcb) {
        new AsyncTask<Void, Void, ArrayList<EtherscanTransaction>>() {
            @Override
            protected ArrayList<EtherscanTransaction> doInBackground(Void... n) {
                return getNextPage();
            }
            @Override
            protected void onPostExecute(ArrayList<EtherscanTransaction> etl) {
                rcb.onResult(etl);
            }
        }.execute();
    }
}
