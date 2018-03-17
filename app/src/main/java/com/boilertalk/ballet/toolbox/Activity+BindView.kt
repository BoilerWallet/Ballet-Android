package com.boilertalk.ballet.toolbox

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View

/**
 * Created by koray on 17.03.18.
 */

fun <T: View> Activity.bind(@IdRes res : Int) : T {
    @Suppress("UNCHECKED_CAST")
    return findViewById(res)
}
