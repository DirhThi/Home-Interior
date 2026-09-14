package com.interiordesign3d.data.plans

import android.content.Context
import com.interiordesign3d.data.models.FloorPlan
import kotlinx.serialization.json.Json

/** A floor plan shipped with the app, stored as the same JSON that `DesignRoom.floorPlanJson` holds. */
data class SamplePlan(val key: String, val label: String) {
    val asset get() = "plans/$key.json"
}

val SAMPLE_PLANS = listOf(
    SamplePlan("studio", "Studio"),
    SamplePlan("two_bedroom", "Two bedroom"),
    SamplePlan("l_shaped", "L-shaped apartment"),
    SamplePlan("townhouse", "Townhouse, ground floor"),
)

/** Raw JSON for [plan], or null if the asset is missing or does not decode. */
fun readSamplePlanJson(context: Context, plan: SamplePlan): String? = runCatching {
    val json = context.assets.open(plan.asset).bufferedReader().use { it.readText() }
    Json.decodeFromString<FloorPlan>(json)   // validate before it reaches the database
    json
}.getOrNull()

fun readSamplePlan(context: Context, plan: SamplePlan): FloorPlan? = runCatching {
    context.assets.open(plan.asset).bufferedReader().use { Json.decodeFromString<FloorPlan>(it.readText()) }
}.getOrNull()

/** Total floor area in m², for the plan card. */
fun FloorPlan.areaM2(): Float = rooms.sumOf { room ->
    var a = 0.0
    for (i in room.indices) {
        val p = nodes[room[i]]
        val q = nodes[room[(i + 1) % room.size]]
        a += p.x * q.y - q.x * p.y
    }
    kotlin.math.abs(a) / 2.0
}.toFloat() / 10_000f
