package com.boilertalk.ballet.toolbox;

import android.content.Context;
import android.util.Log;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class SSLHelper {
    public static void initializeSSLContext(Context mContext){
        Log.d("AA", "BB");
        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        /*try {
            ProviderInstaller.installIfNeeded(mContext.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }*/
    }
}
