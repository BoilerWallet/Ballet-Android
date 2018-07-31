package com.boilertalk.ballet.toolbox;

import android.os.AsyncTask;

public class GeneralAsyncTask<Params, Result> extends AsyncTask<Params, Object, Result> {

    public interface DoInBackground<Result, Params> {

        Result doInBackground(Params[] objects);
    }

    public interface OnPostExecute<Result> {

        void onPostExecute(Result o);
    }

    private DoInBackground<Result, Params> backgroundCompletion;

    private OnPostExecute<Result> postExecuteCompletion;

    public void setBackgroundCompletion(DoInBackground<Result, Params> backgroundCompletion) {
        this.backgroundCompletion = backgroundCompletion;
    }

    public void setPostExecuteCompletion(OnPostExecute<Result> postExecuteCompletion) {
        this.postExecuteCompletion = postExecuteCompletion;
    }

    @Override
    protected Result doInBackground(Params[] objects) {
        if (backgroundCompletion != null) {
            return backgroundCompletion.doInBackground(objects);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Result o) {
        if (postExecuteCompletion != null) {
            postExecuteCompletion.onPostExecute(o);
        }
    }
}
