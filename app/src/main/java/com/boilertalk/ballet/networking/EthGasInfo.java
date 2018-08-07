package com.boilertalk.ballet.networking;

import android.content.Context;

import com.boilertalk.ballet.R;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.MathContext;

public class EthGasInfo {
    public double fastestPrice;
    public double fastPrice;
    public double averagePrice;
    public double safeLowPrice;

    public EthGasInfo(double fastestPrice,
                      double fastPrice,
                      double averagePrice,
                      double safeLowPrice) {
        this.fastestPrice = fastestPrice;
        this.fastPrice = fastPrice;
        this.averagePrice = averagePrice;
        this.safeLowPrice = safeLowPrice;
    }

    public String getFormattedInfoString(Context context, double gwei) {
        String speed, wait, price;
        if(gwei < averagePrice) {
            speed = context.getString(R.string.send_low_speed);
        } else if(gwei < fastPrice) {
            speed = context.getString(R.string.send_average_speed);
        } else if(gwei < fastestPrice) {
            speed = context.getString(R.string.send_fast_speed);
        } else {
            speed = context.getString(R.string.send_fastest_speed);
        }
        MathContext mc = new MathContext(4);
        price = Convert.fromWei(Convert.toWei(new BigDecimal(gwei), Convert.Unit.GWEI), Convert.Unit.ETHER).round(mc).toPlainString();
        return context.getString(R.string.send_gas_price_fmt).replace("$SPEED$", speed).replace("$PRICE$", price + " " + context.getString(R.string.send_dimens_ETH));
    }
}
