package eka.care.records.data.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

// Source https://developer.android.com/develop/connectivity/network-ops/reading-network-state#restrictions
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun getNetworkCapabilities(context: Context): Map<String, Any> {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return mapOf("activeNetwork" to false)
    val capabilities = connectivityManager.getNetworkCapabilities(network)
        ?: return mapOf("capabilities" to "empty")

    return mapOf(
        "INTERNET" to capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
        "VALIDATED" to capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
        "WIFI" to capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
        "CELLULAR" to capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
        "VPN" to capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN),
        "ETHERNET" to capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET),
    )
}