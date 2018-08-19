package com.boilertalk.ballet.networking;

import android.util.Log;

import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.toolbox.VariableHolder;
import com.boilertalk.ballet.toolbox.iResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class EthGasStationAPI {
    private static final String ethGasStationAddress = "https://ethgasstation.info/json/ethgasAPI.json";
    public static EthGasInfo getGasInfo() {
        if(VariableHolder.getInstance().activeUrl().isMainnet()) {
            String res = null;

            try {
                InputStream is = new java.net.URL(ethGasStationAddress).openStream();
                java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
                res = s.hasNext() ? s.next() : "";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("EthGasStationAPI", "result of request: " + res);

            double fastestPrice;
            double fastPrice;
            double averagePrice;
            double safeLowPrice;
            double fastestTime;
            double fastTime;
            double averageTime;
            double safeLowTime;
            EthGasInfo result = null;
            try {
                JSONObject jsoob = new JSONObject(res);
                fastestPrice = jsoob.getDouble("fastest");
                fastPrice = jsoob.getDouble("fast");
                averagePrice = jsoob.getDouble("average");
                safeLowPrice = jsoob.getDouble("safeLow");

                fastestTime = jsoob.getDouble("fastestWait");
                fastTime = jsoob.getDouble("fastWait");
                averageTime = jsoob.getDouble("avgWait");
                safeLowTime = jsoob.getDouble("safeLowWait");


                result = new EthGasInfo(fastestPrice, fastPrice, averagePrice, safeLowPrice,
                        fastestTime, fastTime, averageTime, safeLowTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        } else {
            return new EthGasInfo(
                    200.0,
                    50.0,
                    21.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0
                    );
        }
    }


    public static void async_getGasInfo(iResult<EthGasInfo> cb) {
        GeneralAsyncTask<Void, EthGasInfo> gat = new GeneralAsyncTask<>();
        gat.setBackgroundCompletion((Void) -> getGasInfo());
        gat.setPostExecuteCompletion((gasInfo) -> {
            cb.onResult(gasInfo);
        });
        gat.execute();
    }
}
