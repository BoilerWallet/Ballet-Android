package com.boilertalk.ballet.toolbox;

import org.spongycastle.jcajce.provider.digest.Keccak;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class Keyutils {

    public static ECKeyPair ramdomECKeyPair() {
        byte[] num = new byte[2];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(num);
        short kded = ByteBuffer.wrap(num).getShort();
        int dded = kded < 0 ? kded * (-1) + Short.MAX_VALUE : kded;
        int num2 = dded  + 49;
        byte[] pks = new byte[num2];
        sr.nextBytes(pks);

        Keccak.Digest256 kk256dg = new Keccak.Digest256();
        //Hex.toHexString(kk256dg.digest("abba".getBytes()));
        kk256dg.digest(pks);

        return ECKeyPair.create(pks);
    }
}
