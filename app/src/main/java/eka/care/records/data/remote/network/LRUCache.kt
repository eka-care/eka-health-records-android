package eka.care.records.data.remote.network

internal class LRUCache<K, V>(  // Maximum number of items in the cache.
    private val capacity: Int
) :
    LinkedHashMap<K, V>(capacity + 1, 1.0f, true) {
    override fun removeEldestEntry(p0: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > capacity
    }
}