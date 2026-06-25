package com.interiordesign3d.data.repository

import com.interiordesign3d.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In production this would connect to a remote API / Firebase.
 * For now it provides rich sample data and local DesignRoom operations.
 */
class FurnitureRepository {

    private val _catalog = MutableStateFlow(sampleFurnitureCatalog())

    fun getAllFurniture(): Flow<List<FurnitureItem>> = _catalog

    fun getFurnitureByCategory(category: FurnitureCategory): Flow<List<FurnitureItem>> =
        _catalog.map { list -> list.filter { it.category == category } }

    fun searchFurniture(query: String): Flow<List<FurnitureItem>> =
        _catalog.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }

    fun getFurnitureById(id: String): FurnitureItem? =
        _catalog.value.find { it.id == id }

    // ─── Sample Catalog Data ──────────────────────────────────────────────────

    private fun sampleFurnitureCatalog(): List<FurnitureItem> = listOf(
        // Sofas
        FurnitureItem("sofa_01", "Osaka 3-Seat Sofa", FurnitureCategory.SOFA,
            "Muji", 1299.0, "thumbnail/sofa_osaka.jpg", "models/sofa_osaka.glb",
            220f, 85f, 75f,
            listOf("#F5F0EB","#2C2C2C","#8B7355","#C4A882"),
            "Minimalist Japanese-inspired sofa with clean lines.",
            listOf("minimalist","japanese","linen")),
        FurnitureItem("sofa_02", "Cloud Sectional", FurnitureCategory.SOFA,
            "West Elm", 2499.0, "thumbnail/sofa_cloud.jpg", "models/sofa_cloud.glb",
            310f, 180f, 80f,
            listOf("#E8E0D5","#6B5B4E","#3A3A3A"),
            "Deep, sink-in comfort with modular configuration.",
            listOf("cozy","sectional","modern")),
        FurnitureItem("sofa_03", "Loft Chesterfield", FurnitureCategory.SOFA,
            "Restoration Hardware", 3200.0, "thumbnail/sofa_chesterfield.jpg", "models/sofa_chesterfield.glb",
            200f, 90f, 78f,
            listOf("#4A3728","#8B0000","#1A1A2E"),
            "Classic tufted Chesterfield in premium leather.",
            listOf("classic","leather","luxury")),

        // Chairs
        FurnitureItem("chair_01", "Arc Lounge Chair", FurnitureCategory.CHAIR,
            "HAY", 699.0, "thumbnail/chair_arc.jpg", "models/chair_arc.glb",
            75f, 78f, 82f,
            listOf("#E8C4A0","#2C2C2C","#4A7C59","#B5451B"),
            "Sculptural lounge chair with wood base.",
            listOf("scandinavian","lounge","designer")),
        FurnitureItem("chair_02", "Woven Rattan Chair", FurnitureCategory.CHAIR,
            "CB2", 449.0, "thumbnail/chair_rattan.jpg", "models/chair_rattan.glb",
            70f, 72f, 88f,
            listOf("#C4A882","#8B6914"),
            "Natural rattan for bohemian interiors.",
            listOf("boho","rattan","natural")),
        FurnitureItem("chair_03", "Eames-style Armchair", FurnitureCategory.CHAIR,
            "Herman Miller", 1899.0, "thumbnail/chair_eames.jpg", "models/chair_eames.glb",
            66f, 71f, 84f,
            listOf("#2C1810","#1A1A1A","#F5F0EB"),
            "Iconic mid-century modern armchair with ottoman.",
            listOf("mid-century","iconic","leather")),

        // Tables
        FurnitureItem("table_01", "Slab Dining Table", FurnitureCategory.TABLE,
            "Crate & Barrel", 1599.0, "thumbnail/table_slab.jpg", "models/table_slab.glb",
            200f, 90f, 76f,
            listOf("#8B6914","#2C1810","#1A1A1A"),
            "Live-edge solid walnut dining table for 6–8.",
            listOf("dining","walnut","live-edge")),
        FurnitureItem("table_02", "Marble Coffee Table", FurnitureCategory.TABLE,
            "Zara Home", 799.0, "thumbnail/table_marble.jpg", "models/table_marble.glb",
            120f, 65f, 42f,
            listOf("#F5F0EB","#2C2C2C","#C0C0C0"),
            "Carrara marble top with brushed brass legs.",
            listOf("luxury","marble","coffee")),
        FurnitureItem("table_03", "Tulip Side Table", FurnitureCategory.TABLE,
            "Knoll", 450.0, "thumbnail/table_tulip.jpg", "models/table_tulip.glb",
            51f, 51f, 52f,
            listOf("#F5F0EB","#2C2C2C"),
            "Classic pedestal side table.",
            listOf("mid-century","pedestal","classic")),

        // Beds
        FurnitureItem("bed_01", "Float Platform Bed", FurnitureCategory.BED,
            "IKEA", 899.0, "thumbnail/bed_float.jpg", "models/bed_float.glb",
            180f, 200f, 30f,
            listOf("#F5F0EB","#2C2C2C","#8B7355"),
            "Low-profile platform bed with hidden storage.",
            listOf("minimalist","platform","storage")),
        FurnitureItem("bed_02", "Canopy Bed", FurnitureCategory.BED,
            "Pottery Barn", 2299.0, "thumbnail/bed_canopy.jpg", "models/bed_canopy.glb",
            180f, 210f, 220f,
            listOf("#F5F0EB","#C4A882","#2C2C2C"),
            "Four-poster canopy bed with linen curtains.",
            listOf("romantic","luxury","canopy")),

        // Lamps
        FurnitureItem("lamp_01", "Arc Floor Lamp", FurnitureCategory.LAMP,
            "Flos", 599.0, "thumbnail/lamp_arc.jpg", "models/lamp_arc.glb",
            50f, 50f, 220f,
            listOf("#C0C0C0","#F5C518","#2C2C2C"),
            "Iconic arched floor lamp, brushed steel.",
            listOf("iconic","modern","reading")),
        FurnitureItem("lamp_02", "Wabi-Sabi Table Lamp", FurnitureCategory.LAMP,
            "Menu", 189.0, "thumbnail/lamp_wabi.jpg", "models/lamp_wabi.glb",
            25f, 25f, 45f,
            listOf("#E8C4A0","#F5F0EB","#8B6914"),
            "Organic ceramic base with linen shade.",
            listOf("japandi","ceramic","ambient")),

        // Rugs
        FurnitureItem("rug_01", "Geometric Wool Rug", FurnitureCategory.RUG,
            "Loloi", 349.0, "thumbnail/rug_geo.jpg", "models/rug_geo.glb",
            240f, 170f, 1f,
            listOf("#E8C4A0","#2C2C2C","#8B7355","#4A7C59"),
            "Hand-tufted geometric wool rug 8x6.",
            listOf("geometric","wool","living-room")),
        FurnitureItem("rug_02", "Moroccan Beni Ourain", FurnitureCategory.RUG,
            "Anthropologie", 599.0, "thumbnail/rug_beni.jpg", "models/rug_beni.glb",
            300f, 200f, 2f,
            listOf("#F5F0EB","#2C2C2C"),
            "Authentic Berber-style shaggy rug.",
            listOf("moroccan","boho","shag")),

        // Plants
        FurnitureItem("plant_01", "Fiddle Leaf Fig", FurnitureCategory.PLANT,
            "The Sill", 89.0, "thumbnail/plant_fig.jpg", "models/plant_fig.glb",
            50f, 50f, 150f,
            listOf("#4A7C59","#8B6914"),
            "Statement indoor tree for bright spaces.",
            listOf("biophilic","statement","tropical")),
        FurnitureItem("plant_02", "Snake Plant Cluster", FurnitureCategory.PLANT,
            "Bloomscape", 45.0, "thumbnail/plant_snake.jpg", "models/plant_snake.glb",
            30f, 30f, 80f,
            listOf("#4A7C59","#2C5F2E"),
            "Low-maintenance architectural succulent.",
            listOf("low-maintenance","air-purifying","modern"))
    )
}

// ─── Color Palette Repository ─────────────────────────────────────────────────

object ColorPaletteRepository {
    val palettes = listOf(
        ColorPalette("p1","Warm Sand","#C4A882","#8B7355","#F5C518","#F5F0EB", DesignStyle.SCANDINAVIAN),
        ColorPalette("p2","Midnight Modern","#1A1A2E","#16213E","#E94560","#0F3460", DesignStyle.MODERN),
        ColorPalette("p3","Forest Calm","#2C5F2E","#4A7C59","#F5C518","#F0EBE3", DesignStyle.JAPANDI),
        ColorPalette("p4","Desert Rose","#C68642","#8B4513","#E8C4A0","#FFF8F0", DesignStyle.BOHEMIAN),
        ColorPalette("p5","Industrial Edge","#3A3A3A","#2C2C2C","#B5451B","#E8E0D5", DesignStyle.INDUSTRIAL),
        ColorPalette("p6","Coastal Breeze","#4A90D9","#87CEEB","#FFFFFF","#F0F8FF", DesignStyle.COASTAL),
        ColorPalette("p7","Mid-Century Teal","#2A9D8F","#E9C46A","#F4A261","#264653", DesignStyle.MID_CENTURY),
        ColorPalette("p8","Minimalist White","#FFFFFF","#F5F5F5","#2C2C2C","#FAFAFA", DesignStyle.MINIMALIST)
    )
}
