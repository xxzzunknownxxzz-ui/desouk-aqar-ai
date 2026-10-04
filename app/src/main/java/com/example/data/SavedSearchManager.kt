package com.example.data

import android.content.Context
import com.example.data.ai.AiSearchCriteria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SavedSearchItem(
    val id: String,
    val title: String,
    val category: String? = null,
    val type: String? = null,
    val district: String? = null,
    val maxPrice: Double? = null,
    val minArea: Double? = null,
    val rooms: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class SavedSearchManager(context: Context) {

    private val prefs = context.getSharedPreferences("desouk_saved_searches", Context.MODE_PRIVATE)
    private val _savedSearches = MutableStateFlow<List<SavedSearchItem>>(emptyList())
    val savedSearches: StateFlow<List<SavedSearchItem>> = _savedSearches.asStateFlow()

    init {
        loadSearches()
    }

    private fun loadSearches() {
        val raw = prefs.getString("searches_json", "[]") ?: "[]"
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SavedSearchItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SavedSearchItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        category = obj.optString("category").takeIf { it.isNotBlank() },
                        type = obj.optString("type").takeIf { it.isNotBlank() },
                        district = obj.optString("district").takeIf { it.isNotBlank() },
                        maxPrice = if (obj.has("maxPrice")) obj.getDouble("maxPrice") else null,
                        minArea = if (obj.has("minArea")) obj.getDouble("minArea") else null,
                        rooms = if (obj.has("rooms")) obj.getInt("rooms") else null,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _savedSearches.value = list
        } catch (_: Exception) {
            _savedSearches.value = emptyList()
        }
    }

    fun saveSearch(criteria: AiSearchCriteria): SavedSearchItem {
        val titleParts = mutableListOf<String>()
        criteria.category?.let { titleParts.add(it) }
        criteria.type?.let { titleParts.add(it) }
        criteria.district?.let { titleParts.add("في $it") }
        criteria.maxPrice?.let { titleParts.add("لحد ${it.toLong()} ج") }
        criteria.rooms?.let { titleParts.add("$it غرف") }

        val title = if (titleParts.isNotEmpty()) titleParts.joinToString(" • ") else "بحث عقاري مخصص"
        val item = SavedSearchItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            category = criteria.category,
            type = criteria.type,
            district = criteria.district,
            maxPrice = criteria.maxPrice,
            minArea = criteria.minArea,
            rooms = criteria.rooms
        )

        val updated = listOf(item) + _savedSearches.value
        saveList(updated)
        return item
    }

    fun deleteSearch(id: String) {
        val updated = _savedSearches.value.filter { it.id != id }
        saveList(updated)
    }

    private fun saveList(list: List<SavedSearchItem>) {
        _savedSearches.value = list
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            item.category?.let { obj.put("category", it) }
            item.type?.let { obj.put("type", it) }
            item.district?.let { obj.put("district", it) }
            item.maxPrice?.let { obj.put("maxPrice", it) }
            item.minArea?.let { obj.put("minArea", it) }
            item.rooms?.let { obj.put("rooms", it) }
            obj.put("timestamp", item.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("searches_json", arr.toString()).apply()
    }
}
