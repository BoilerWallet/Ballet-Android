package com.boilertalk.ballet.toolbox;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.github.curioustechizen.ago.RelativeTimeTextView;

public class WorkingRelativeTimeView extends RelativeTimeTextView{
    public WorkingRelativeTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WorkingRelativeTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected CharSequence getRelativeTimeDisplayString(long referenceTime, long now) {
        Log.d("tIME", "Ref " + referenceTime + ", now " + now);
        return super.getRelativeTimeDisplayString(referenceTime, now);
    }
}
