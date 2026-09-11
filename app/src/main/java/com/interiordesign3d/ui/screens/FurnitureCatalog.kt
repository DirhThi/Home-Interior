package com.interiordesign3d.ui.screens

// ─── Furniture catalog (Kenney kit — full set) ─────────────────────────────────
// key = glb file name in assets/models (and PlacedFurniture.furnitureId).
// preview = png in assets/previews. Models are real-scale (1 unit = 1 m) and rendered at
// their natural size × `scale`. mount decides floor / wall (auto wall-mount) / ceiling.

enum class MountType { FLOOR, WALL, CEILING }

data class CatalogItem(
    val key: String,
    val label: String,
    val mount: MountType = MountType.FLOOR,
    val wallHeightCm: Float = 120f,   // default mount height for WALL items
    val surface: Boolean = false,      // small items dropped over it rest on its top
) {
    val wallMounted get() = mount == MountType.WALL
    val preview get() = "previews/$key.png"
    val model get() = "models/$key.glb"
}

data class CatalogGroup(val title: String, val items: List<CatalogItem>)

private fun fi(key: String, label: String, mount: MountType = MountType.FLOOR, h: Float = 120f, surface: Boolean = false) =
    CatalogItem(key, label, mount, h, surface)

val FURNITURE_CATALOG: List<CatalogGroup> = listOf(
    CatalogGroup("Bàn & Ghế", listOf(
        fi("chair", "Ghế"),
        fi("chairCushion", "Ghế đệm"),
        fi("chairRounded", "Ghế tròn"),
        fi("chairModernCushion", "Ghế hiện đại"),
        fi("chairModernFrameCushion", "Ghế khung"),
        fi("chairDesk", "Ghế làm việc"),
        fi("stoolBar", "Ghế bar"),
        fi("stoolBarSquare", "Ghế bar vuông"),
        fi("loungeChair", "Ghế thư giãn"),
        fi("loungeChairRelax", "Ghế nằm"),
        fi("loungeDesignChair", "Ghế thiết kế"),
        fi("bench", "Băng ghế"),
        fi("benchCushion", "Băng ghế đệm"),
        fi("benchCushionLow", "Băng ghế thấp"),
        fi("loungeSofa", "Sofa"),
        fi("loungeSofaLong", "Sofa dài"),
        fi("loungeSofaCorner", "Sofa góc"),
        fi("loungeSofaOttoman", "Sofa ottoman"),
        fi("loungeDesignSofa", "Sofa thiết kế"),
        fi("loungeDesignSofaCorner", "Sofa TK góc"),
        fi("table", "Bàn", surface = true),
        fi("tableRound", "Bàn tròn", surface = true),
        fi("tableGlass", "Bàn kính", surface = true),
        fi("tableCloth", "Bàn khăn trải", surface = true),
        fi("tableCross", "Bàn chân chéo", surface = true),
        fi("tableCrossCloth", "Bàn chéo khăn", surface = true),
        fi("tableCoffee", "Bàn trà", surface = true),
        fi("tableCoffeeGlass", "Bàn trà kính", surface = true),
        fi("tableCoffeeSquare", "Bàn trà vuông", surface = true),
        fi("tableCoffeeGlassSquare", "Bàn trà kính vuông", surface = true),
        fi("desk", "Bàn làm việc", surface = true),
        fi("deskCorner", "Bàn góc", surface = true),
        fi("sideTable", "Bàn phụ", surface = true),
        fi("sideTableDrawers", "Bàn phụ ngăn kéo", surface = true),
        fi("kitchenBar", "Quầy bar", surface = true),
        fi("kitchenBarEnd", "Quầy bar góc", surface = true),
    )),
    CatalogGroup("Phòng ngủ", listOf(
        fi("bedDouble", "Giường đôi"),
        fi("bedSingle", "Giường đơn"),
        fi("bedBunk", "Giường tầng"),
        fi("cabinetBed", "Tủ đầu giường", surface = true),
        fi("cabinetBedDrawer", "Tủ đầu giường ngăn", surface = true),
        fi("cabinetBedDrawerTable", "Tủ + bàn", surface = true),
        fi("bookcaseClosedWide", "Tủ quần áo"),
        fi("bookcaseClosed", "Tủ kín"),
        fi("bookcaseClosedDoors", "Tủ có cửa"),
        fi("bookcaseOpen", "Kệ sách"),
        fi("bookcaseOpenLow", "Kệ sách thấp", surface = true),
        fi("coatRack", "Móc treo tường", MountType.WALL, 150f),
        fi("coatRackStanding", "Cây treo đồ"),
        fi("pillow", "Gối"),
        fi("pillowLong", "Gối ôm"),
        fi("pillowBlue", "Gối xanh"),
        fi("pillowBlueLong", "Gối ôm xanh"),
    )),
    CatalogGroup("Đồ điện tử", listOf(
        fi("televisionModern", "TV", MountType.WALL, 110f),
        fi("televisionVintage", "TV cổ"),
        fi("televisionAntenna", "TV ăng-ten"),
        fi("cabinetTelevision", "Kệ TV", surface = true),
        fi("cabinetTelevisionDoors", "Kệ TV có cửa", surface = true),
        fi("computerScreen", "Màn hình"),
        fi("computerKeyboard", "Bàn phím"),
        fi("computerMouse", "Chuột"),
        fi("laptop", "Laptop"),
        fi("speaker", "Loa"),
        fi("speakerSmall", "Loa nhỏ"),
        fi("radio", "Radio"),
    )),
    CatalogGroup("Nhà bếp", listOf(
        fi("kitchenFridge", "Tủ lạnh"),
        fi("kitchenFridgeSmall", "Tủ lạnh nhỏ"),
        fi("kitchenFridgeLarge", "Tủ lạnh lớn"),
        fi("kitchenFridgeBuiltIn", "Tủ lạnh âm"),
        fi("kitchenStove", "Bếp ga"),
        fi("kitchenStoveElectric", "Bếp điện"),
        fi("kitchenSink", "Bồn rửa"),
        fi("kitchenCabinet", "Tủ bếp", surface = true),
        fi("kitchenCabinetDrawer", "Tủ bếp ngăn", surface = true),
        fi("kitchenCabinetCornerInner", "Tủ bếp góc trong", surface = true),
        fi("kitchenCabinetCornerRound", "Tủ bếp góc tròn", surface = true),
        fi("kitchenCabinetUpper", "Tủ bếp treo", MountType.WALL, 150f),
        fi("kitchenCabinetUpperLow", "Tủ treo thấp", MountType.WALL, 150f),
        fi("kitchenCabinetUpperDouble", "Tủ treo đôi", MountType.WALL, 150f),
        fi("kitchenCabinetUpperCorner", "Tủ treo góc", MountType.WALL, 150f),
        fi("hoodModern", "Máy hút mùi", MountType.WALL, 160f),
        fi("hoodLarge", "Máy hút mùi lớn", MountType.WALL, 160f),
        fi("kitchenMicrowave", "Lò vi sóng"),
        fi("kitchenCoffeeMachine", "Máy cà phê"),
        fi("kitchenBlender", "Máy xay"),
        fi("toaster", "Lò nướng"),
        fi("washer", "Máy giặt"),
        fi("dryer", "Máy sấy"),
        fi("washerDryerStacked", "Máy giặt sấy"),
    )),
    CatalogGroup("Nhà tắm & VS", listOf(
        fi("toilet", "Bồn cầu"),
        fi("toiletSquare", "Bồn cầu vuông"),
        fi("bathtub", "Bồn tắm"),
        fi("shower", "Vòi sen"),
        fi("showerRound", "Vòi sen tròn"),
        fi("bathroomSink", "Lavabo"),
        fi("bathroomSinkSquare", "Lavabo vuông"),
        fi("bathroomCabinet", "Tủ phòng tắm", surface = true),
        fi("bathroomCabinetDrawer", "Tủ PT ngăn", surface = true),
        fi("bathroomMirror", "Gương", MountType.WALL, 140f),
    )),
    CatalogGroup("Trang trí & đèn", listOf(
        fi("pottedPlant", "Cây cảnh"),
        fi("plantSmall1", "Cây nhỏ 1"),
        fi("plantSmall2", "Cây nhỏ 2"),
        fi("plantSmall3", "Cây nhỏ 3"),
        fi("books", "Sách"),
        fi("bear", "Gấu bông"),
        fi("cardboardBoxClosed", "Thùng kín"),
        fi("cardboardBoxOpen", "Thùng mở"),
        fi("trashcan", "Thùng rác"),
        fi("rugRectangle", "Thảm chữ nhật"),
        fi("rugSquare", "Thảm vuông"),
        fi("rugRound", "Thảm tròn"),
        fi("rugRounded", "Thảm bo tròn"),
        fi("rugDoormat", "Thảm chùi chân"),
        fi("lampRoundFloor", "Đèn cây tròn"),
        fi("lampSquareFloor", "Đèn cây vuông"),
        fi("lampRoundTable", "Đèn bàn tròn"),
        fi("lampSquareTable", "Đèn bàn vuông"),
        fi("lampWall", "Đèn tường", MountType.WALL, 170f),
        fi("lampSquareCeiling", "Đèn trần", MountType.CEILING),
        fi("ceilingFan", "Quạt trần", MountType.CEILING),
    )),
    CatalogGroup("Kiến trúc", listOf(
        fi("stairs", "Cầu thang"),
        fi("stairsCorner", "Cầu thang góc"),
        fi("stairsOpen", "Cầu thang hở"),
        fi("stairsOpenSingle", "Cầu thang hở đơn"),
        fi("doorway", "Khung cửa"),
        fi("doorwayFront", "Cửa chính"),
        fi("doorwayOpen", "Cửa mở"),
    )),
)

private val CATALOG_BY_KEY: Map<String, CatalogItem> =
    FURNITURE_CATALOG.flatMap { it.items }.associateBy { it.key }

fun catalogItem(key: String): CatalogItem? = CATALOG_BY_KEY[key]

// ─── Wall / floor surface presets (choosing one replaces ALL walls / the floor) ──

data class SurfacePreset(val label: String, val model: String, val colorHex: String)

val WALL_PRESETS = listOf(
    SurfacePreset("Kem ấm", "wall", "#F2E6D3"),
    SurfacePreset("Trắng", "wall", "#EFEAE3"),
    SurfacePreset("Be", "wall", "#E0D2BC"),
    SurfacePreset("Xám", "paneling", "#B9BDC2"),
    SurfacePreset("Xanh nhạt", "paneling", "#C5D6CF"),
    SurfacePreset("Tường lửng", "wallHalf", "#E6DFD4"),
)

val FLOOR_PRESETS = listOf(
    SurfacePreset("Gỗ sáng", "floorFull", "#C9A877"),
    SurfacePreset("Gỗ tối", "floorFull", "#8A5A35"),
    SurfacePreset("Xám", "floorFull", "#AFB2B6"),
    SurfacePreset("Trắng", "floorFull", "#E7E3DC"),
    SurfacePreset("Lát nửa", "floorHalf", "#CDBBA0"),
)
