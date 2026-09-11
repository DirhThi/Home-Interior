package com.interiordesign3d.ui.screens

// ─── Furniture catalog (Quaternius q_ House pack + qf_ Furniture pack) ─────────────────────────────────
// key = glb file name in assets/models (and PlacedFurniture.furnitureId).
// preview = png in assets/previews. Models are rendered at natural size × unitScale × `scale`. mount decides floor / wall (auto wall-mount) / ceiling.

enum class MountType { FLOOR, WALL, CEILING }

data class CatalogItem(
    val key: String,
    val label: String,
    val mount: MountType = MountType.FLOOR,
    val wallHeightCm: Float = 120f,   // default mount height for WALL items
    val surface: Boolean = false,      // small items dropped over it rest on its top
    val unitScale: Float = if (key.startsWith("q_")) 0.5f else 1f,  // model units → metres (Quaternius packs are 2×)
) {
    val wallMounted get() = mount == MountType.WALL
    val preview get() = "previews/$key.webp"
    val model get() = "models/$key.glb"
}

data class CatalogGroup(val title: String, val items: List<CatalogItem>)

private fun fi(key: String, label: String, mount: MountType = MountType.FLOOR, h: Float = 120f, surface: Boolean = false, s: Float? = null) =
    if (s == null) CatalogItem(key, label, mount, h, surface) else CatalogItem(key, label, mount, h, surface, s)

val FURNITURE_CATALOG: List<CatalogGroup> = listOf(
    CatalogGroup("Bàn & Ghế", listOf(
        fi("q_chair_2", "Ghế 3"),
        fi("q_chair_3", "Ghế 4"),
        fi("q_couch_large2", "Sofa lớn 2"),
        fi("q_couch_large3", "Sofa lớn 3"),
        fi("qf_armchair", "Ghế bành cổ điển", s = 0.5f),
        fi("qf_chair_wood", "Ghế gỗ cổ điển"),
        fi("qf_sofa_brown", "Sofa da nâu", s = 0.5f),
        fi("qf_sofa_corner", "Sofa góc đen", s = 0.5f),
        fi("qf_sofa_navy", "Sofa xanh đậm", s = 0.5f),
        fi("qf_stool", "Ghế đẩu gỗ"),
        fi("qf_table", "Bàn gỗ cổ điển", surface = true),
        fi("qf_table_2", "Bàn gỗ cổ điển 2", surface = true),
        fi("qf_desk", "Bàn làm việc gỗ", surface = true),
        fi("qf_office_chair", "Ghế xoay xanh"),
        fi("q_chair_rlyhe93nne", "Ghế"),
        fi("q_chair", "Ghế 2"),
        fi("q_couch_large", "Sofa lớn"),
        fi("q_couch_medium_mwgq94zhdz", "Sofa vừa"),
        fi("q_couch_medium", "Sofa vừa 2"),
        fi("q_couch_small_x9msj0gtb5", "Sofa nhỏ"),
        fi("q_couch_small", "Sofa nhỏ 2"),
        fi("q_l_couch", "Sofa chữ L"),
        fi("q_stool", "Ghế đẩu"),
        fi("q_table_round_large", "Bàn tròn lớn", surface = true),
        fi("q_table_round_small_57w671wvs2", "Bàn tròn nhỏ", surface = true),
        fi("q_table_round_small", "Bàn tròn nhỏ 2", surface = true),
    )),
    CatalogGroup("Phòng ngủ", listOf(
        fi("q_bookshelf", "Kệ sách lớn"),
        fi("q_shelf_1", "Kệ đứng"),
        fi("q_shelf_2", "Kệ đứng 2"),
        fi("qf_bed_double", "Giường đôi cổ điển", s = 0.5f),
        fi("qf_bed_twin", "Giường đơn cổ điển", s = 0.5f),
        fi("qf_bookcase", "Tủ sách", s = 0.5f),
        fi("qf_closet", "Tủ quần áo gỗ", s = 0.5f),
        fi("qf_closet_short", "Tủ thấp", surface = true, s = 0.5f),
        fi("qf_night_stand", "Tủ đầu giường cổ điển", surface = true),
        fi("q_bed_king", "Giường king"),
        fi("q_bed_single", "Giường đơn"),
        fi("q_bunk_bed", "Giường tầng"),
        fi("q_curtains_double", "Rèm đôi", MountType.WALL, 140f),
        fi("q_drawer_8xzqezl2w3", "Tủ ngăn kéo", surface = true),
        fi("q_drawer_g1h0wnchqf", "Tủ ngăn kéo 2", surface = true),
        fi("q_drawer_n3eri89oeo", "Tủ ngăn kéo 3", surface = true),
        fi("q_drawer_t4udbyp90c", "Tủ ngăn kéo 4", surface = true),
        fi("q_drawer", "Tủ ngăn kéo 5", surface = true),
        fi("q_night_stand_08s1j15jcx", "Tủ đầu giường", surface = true),
        fi("q_night_stand_7cobkfclnv", "Tủ đầu giường 2", surface = true),
        fi("q_night_stand", "Tủ đầu giường 3", surface = true),
        fi("q_shelf_large", "Kệ lớn", surface = true),
        fi("q_shelf_small_tfdguv2rye", "Kệ nhỏ", surface = true),
        fi("q_shelf_small", "Kệ nhỏ 2", surface = true),
    )),
    CatalogGroup("Nhà bếp", listOf(
        fi("q_kitchen_cabinet1", "Tủ bếp", surface = true),
        fi("q_kitchen_cabinet2", "Tủ bếp 2", surface = true),
        fi("q_kitchen_cabinetsmall", "Tủ bếp nhỏ", surface = true),
        fi("q_kitchen_1drawers", "Tủ bếp 1 ngăn", surface = true),
        fi("q_kitchen_2drawers", "Tủ bếp 2 ngăn", surface = true),
        fi("q_kitchen_3drawers", "Tủ bếp 3 ngăn", surface = true),
        fi("q_kitchen_oven", "Lò nướng", surface = true),
        fi("q_kitchen_fridge", "Tủ lạnh"),
        fi("q_kitchen_sink", "Bồn rửa bếp"),
        fi("q_oven", "Lò nướng"),
        fi("q_square_plate", "Đĩa vuông"),
        fi("q_trashcan_large", "Thùng rác lớn"),
        fi("q_trashcan_small_zwytk2smba", "Thùng rác nhỏ"),
        fi("q_trashcan_small", "Thùng rác nhỏ 2"),
        fi("q_trashcan_xswahu252t", "Thùng rác"),
        fi("q_trashcan", "Thùng rác 2"),
        fi("q_washing_machine", "Máy giặt"),
    )),
    CatalogGroup("Nhà tắm & VS", listOf(
        fi("q_bathroom_toilet2", "Bồn cầu 2"),
        fi("q_bathroom_shower1", "Vòi sen"),
        fi("q_bathroom_mirror1", "Gương", MountType.WALL, 150f),
        fi("q_bathroom_mirror2", "Gương 2", MountType.WALL, 150f),
        fi("q_bathroom_sink", "Lavabo"),
        fi("q_bathroom_toilet_paper", "Giấy vệ sinh"),
        fi("q_bathtub", "Bồn tắm"),
        fi("q_toilet_paper_stack", "Chồng giấy VS"),
        fi("q_toilet", "Bồn cầu"),
        fi("q_towel_rack", "Giá khăn", MountType.WALL, 140f),
    )),
    CatalogGroup("Trang trí & đèn", listOf(
        fi("q_carpet_2", "Thảm 2"),
        fi("q_curtains_single", "Rèm đơn", MountType.WALL, 140f),
        fi("q_light_ceiling5", "Đèn trần 5", MountType.CEILING),
        fi("q_light_ceiling6", "Đèn trần 6", MountType.CEILING),
        fi("q_light_icosahedron2", "Đèn đa diện 2", MountType.CEILING),
        fi("q_light_stand2", "Đèn cây 2"),
        fi("o_phone_2", "Điện thoại", s = 0.1651f),
        fi("o_boxes", "Thùng carton", s = 0.5f),
        fi("q_table_lamp", "Đèn bàn"),
        fi("q_cactus", "Xương rồng"),
        fi("q_ceiling_light", "Đèn trần", MountType.CEILING),
        fi("q_dead_houseplant", "Dead"),
        fi("q_fireplace", "Lò sưởi"),
        fi("q_houseplant_iblx2jz90o", "Cây cảnh"),
        fi("q_houseplant_vtjh4irl4w", "Cây cảnh 2"),
        fi("q_houseplant_bfloqiv5up", "Cây cảnh 3"),
        fi("q_houseplant_dveij0xnpx", "Cây cảnh 4"),
        fi("q_houseplant_f6gpjbegg0", "Cây cảnh 5"),
        fi("q_houseplant", "Cây cảnh 6"),
        fi("q_lamp", "Đèn"),
        fi("q_light_ceiling_single", "Đèn trần đơn", MountType.CEILING),
        fi("q_light_ceiling_nnlnaidjih", "Đèn trần", MountType.CEILING),
        fi("q_light_ceiling_tooljdo5fi", "Đèn trần 2", MountType.CEILING),
        fi("q_light_ceiling", "Đèn trần 3", MountType.CEILING),
        fi("q_light_chandelier", "Light"),
        fi("q_light_cube_gjpaleplcy", "Đèn khối", MountType.CEILING),
        fi("q_light_cube", "Đèn khối 2", MountType.CEILING),
        fi("q_light_desk", "Đèn bàn"),
        fi("q_light_floor_ebqtooeh43", "Đèn cây"),
        fi("q_light_floor", "Đèn cây 2"),
        fi("q_light_icosahedron", "Đèn đa diện", MountType.CEILING),
        fi("q_light_stand", "Đèn đứng"),
        fi("q_round_rug", "Thảm tròn"),
        fi("q_rug", "Thảm"),
    )),
    CatalogGroup("Kiến trúc", listOf(
        fi("q_column_squarebig", "Cột vuông"),
        fi("q_column_squaresmall", "Cột vuông thấp"),
        fi("q_column_round", "Cột tròn"),
        fi("q_door_double", "Cửa đôi"),
        fi("q_door_8it1hh1oru", "Cửa"),
        fi("q_door_kgt4ztckrm", "Cửa 2"),
        fi("q_door_li93wgnjys", "Cửa 3"),
        fi("q_door_atrxvw0q9n", "Cửa 4"),
        fi("q_door_b0aifzp6vl", "Cửa 5"),
        fi("q_door_ihxwsn12p1", "Cửa 6"),
        fi("q_door_xufnanlzfe", "Cửa 7"),
        fi("q_door", "Cửa 8"),
        fi("q_window_large", "Cửa sổ lớn", MountType.WALL, 140f),
        fi("q_window_round", "Cửa sổ tròn", MountType.WALL, 140f),
        fi("q_window_small", "Cửa sổ nhỏ", MountType.WALL, 140f),
    )),
)

private val CATALOG_BY_KEY: Map<String, CatalogItem> =
    FURNITURE_CATALOG.flatMap { it.items }.associateBy { it.key }

fun catalogItem(key: String): CatalogItem? = CATALOG_BY_KEY[key]

/** Model attributions (CC-BY items require credit in-app). */
val MODEL_CREDITS: List<String> = listOf(
    "Quaternius (quaternius.com) — Ultimate House Interior Pack, Furniture Pack (CC0)",
    "ambientCG.com — texture tường & sàn (CC0)",
)

// ─── Wall / floor surface presets (choosing one replaces ALL walls / the floor) ──
// model = mat_*.glb (textured quad, ambientCG CC0); colorHex tints it; tileM = metres per texture repeat.

data class SurfacePreset(val label: String, val model: String, val colorHex: String = "#FFFFFF", val tileM: Float = 1f) {
    val preview get() = "previews/$model.webp"
    val isPaint get() = colorHex != "#FFFFFF"
}

val WALL_PRESETS = listOf(
    SurfacePreset("Kem ấm", "mat_paintedplaster017", "#F2E6D3", 2f),
    SurfacePreset("Trắng", "mat_paintedplaster017", "#F6F4F0", 2f),
    SurfacePreset("Be", "mat_paintedplaster017", "#E0D2BC", 2f),
    SurfacePreset("Xám", "mat_paintedplaster017", "#B9BDC2", 2f),
    SurfacePreset("Xanh nhạt", "mat_paintedplaster017", "#C5D6CF", 2f),
    SurfacePreset("Hồng nhạt", "mat_paintedplaster017", "#EAD3CF", 2f),
    SurfacePreset("Vữa thô", "mat_plaster001", tileM = 2f),
    SurfacePreset("Gạch đỏ", "mat_bricks059", tileM = 1.5f),
    SurfacePreset("Bê tông", "mat_concrete016", tileM = 2f),
    SurfacePreset("Gạch men", "mat_tiles101", tileM = 1.2f),
    SurfacePreset("Cẩm thạch", "mat_marble012", tileM = 2f),
)

val FLOOR_PRESETS = listOf(
    SurfacePreset("Gỗ sáng", "mat_woodfloor051", tileM = 1f),
    SurfacePreset("Gỗ vàng", "mat_woodfloor040", tileM = 1f),
    SurfacePreset("Gỗ nâu", "mat_woodfloor007", tileM = 1f),
    SurfacePreset("Gỗ mộc", "mat_woodfloor043", tileM = 1.2f),
    SurfacePreset("Gỗ ghép", "mat_woodfloor054", tileM = 1f),
    SurfacePreset("Cẩm thạch", "mat_marble012", tileM = 2f),
    SurfacePreset("Đá đen", "mat_marble006", tileM = 2f),
    SurfacePreset("Gạch caro", "mat_tiles074", tileM = 1.5f),
    SurfacePreset("Gạch hoa", "mat_tiles101", tileM = 1.2f),
    SurfacePreset("Gạch bông", "mat_tiles131", tileM = 1f),
    SurfacePreset("Thảm đỏ", "mat_carpet013", tileM = 1f),
    SurfacePreset("Thảm be", "mat_carpet008", tileM = 1f),
    SurfacePreset("Bê tông", "mat_concrete034", tileM = 2f),
    SurfacePreset("Đá lát", "mat_pavingstones070", tileM = 1.5f),
)
