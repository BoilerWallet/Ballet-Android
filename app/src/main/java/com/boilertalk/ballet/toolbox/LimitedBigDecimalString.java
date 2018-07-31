package com.boilertalk.ballet.toolbox;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class LimitedBigDecimalString {
    public static String createString(int maxDigits, BigDecimal value) {
        value = value.stripTrailingZeros();
        if(value.precision() > maxDigits) {
            value.round(new MathContext(maxDigits));
        }
        return value.toString();
    }
}
