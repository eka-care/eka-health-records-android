package eka.care.documents.ui.utility

import android.os.SystemClock
import android.view.View
import java.util.WeakHashMap

class EkaViewDebounceClickListener(
    private val action: () -> Unit,
    private val debounceInterval: Long = 1000L
) : View.OnClickListener {

    private val lastClickMap = WeakHashMap<View, Long>()

    override fun onClick(view: View?) {
        val prevClickTimeStamp = lastClickMap[view]
        val curTimeStamp = SystemClock.uptimeMillis()
        lastClickMap[view] = curTimeStamp
        if ((prevClickTimeStamp == null) || ((curTimeStamp - prevClickTimeStamp) > debounceInterval)) {
            action()
        }
    }
}