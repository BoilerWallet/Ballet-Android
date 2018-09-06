package com.boilertalk.ballet.networking;

import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.toolbox.iResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class SettingsMarkdownAPI {

    private static String getOpenSourceLibraries() throws MalformedURLException, IOException {
        String url = "https://storage.googleapis.com/boilertalk/Ballet/OpenSourceLibraries.md";

        String res = null;

        InputStream is = new java.net.URL(url).openStream();
        java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        res = s.hasNext() ? s.next() : "";

        return res;
    }

    public static void getOpenSourceLibrariesAsync(iResult<String> result) {
        GeneralAsyncTask<Void, String> openSourceAsync = new GeneralAsyncTask<>();
        openSourceAsync.setBackgroundCompletion((voids) -> {
            try {
                return getOpenSourceLibraries();
            } catch (Exception error) {
                error.printStackTrace();
                return null;
            }
        });
        openSourceAsync.setPostExecuteCompletion(result::onResult);

        openSourceAsync.execute();
    }
}
