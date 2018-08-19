package com.boilertalk.ballet.networking;

import android.content.Context;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.MathContext;

public class EthGasInfo {
    public double fastestPrice;
    public double fastPrice;
    public double averagePrice;
    public double safeLowPrice;

    public double fastestTime;
    public double fastTime;
    public double averageTime;
    public double safeLowTime;

    public EthGasInfo(double fastestPrice, double fastPrice, double averagePrice, double safeLowPrice,
                      double fastestTime, double fastTime, double averageTime, double safeLowTime) {

        this.fastestPrice = fastestPrice;
        this.fastPrice = fastPrice;
        this.averagePrice = averagePrice;
        this.safeLowPrice = safeLowPrice;

        this.fastestTime = fastestTime;
        this.fastTime = fastTime;
        this.averageTime = averageTime;
        this.safeLowTime = safeLowTime;
    }

    public String getFormattedInfoString(Context context, double gwei) {
        String speed, wait, price;
        if(gwei < averagePrice) {
            speed = context.getString(R.string.send_low_speed);
            wait = timeString(context, safeLowTime);
        } else if(gwei < fastPrice) {
            speed = context.getString(R.string.send_average_speed);
            wait = timeString(context, averageTime);
        } else if(gwei < fastestPrice) {
            speed = context.getString(R.string.send_fast_speed);
            wait = timeString(context, fastTime);
        } else {
            speed = context.getString(R.string.send_fastest_speed);
            wait = timeString(context, fastestTime);
        }
        MathContext mc = new MathContext(4);
        price = Convert.fromWei(Convert.toWei(new BigDecimal(gwei), Convert.Unit.GWEI), Convert.Unit.ETHER).round(mc).toPlainString();

        if(VariableHolder.getInstance().activeUrl().isMainnet()) {
            return context.getString(R.string.send_gas_price_fmt)
                    .replace("$SPEED$", speed)
                    .replace("$PRICE$", price + " " + context.getString(R.string.unit_ETH))
                    .replace("$WAIT$", wait);
        } else {
            return context.getString(R.string.send_gas_price_fmt_nomainnet)
                    .replace("$SPEED$", speed)
                    .replace("$PRICE$", price + " " + context.getString(R.string.unit_ETH));
        }

    }

    private String timeString(Context context, double time) {
        String str = "";
        if(time < 1.0) {
            str = "~" + Integer.toString((int) Math.round(60 * time)) + context.getString(R.string.unit_seconds);
        } else if(time < 60.0){
            str = "~" + Integer.toString((int) Math.round(time)) + context.getString(R.string.unit_minutes);
        } else {
            str = "~" + Integer.toString((int) Math.round(time/60.0)) + context.getString(R.string.unit_hours);
        }

        return str;
    }
}
