package com.interiordesign3d.ui.screens

// ─── Furniture catalog (Kenney kit — full set) ─────────────────────────────────
// key = glb file name in assets/models (and PlacedFurniture.furnitureId).
// preview = png in assets/previews. footprintM = natural width in meters (model is
// scaled to this; `scale` adjusts afterwards). wallMounted items auto-enable wall mount.

data class CatalogItem(
    val key: String,
    val label: String,
    val footprintM: Float,
    val wallMounted: Boolean = false,
) {
    val preview get() = "previews/$key.png"
    val model get() = "models/$key.glb"
}

data class CatalogGroup(val title: String, val items: List<CatalogItem>)

private fun fi(key: String, label: String, foot: Float, wall: Boolean = false) =
    CatalogItem(key, label, foot, wall)

val FURNITURE_CATALOG: List<CatalogGroup> = listOf(
    CatalogGroup("Bàn & Ghế", listOf(
        fi("chair", "Ghế", 0.5f),
        fi("chairCushion", "Ghế đệm", 0.5f),
        fi("chairRounded", "Ghế tròn", 0.5f),
        fi("chairModernCushion", "Ghế hiện đại", 0.6f),
        fi("chairModernFrameCushion", "Ghế khung", 0.6f),
        fi("chairDesk", "Ghế làm việc", 0.6f),
        fi("stoolBar", "Ghế bar", 0.4f),
        fi("stoolBarSquare", "Ghế bar vuông", 0.4f),
        fi("loungeChair", "Ghế thư giãn", 0.8f),
        fi("loungeChairRelax", "Ghế nằm", 0.9f),
        fi("loungeDesignChair", "Ghế thiết kế", 0.8f),
        fi("bench", "Băng ghế", 1.2f),
        fi("benchCushion", "Băng ghế đệm", 1.2f),
        fi("benchCushionLow", "Băng ghế thấp", 1.2f),
        fi("loungeSofa", "Sofa", 1.4f),
        fi("loungeSofaLong", "Sofa dài", 2.2f),
        fi("loungeSofaCorner", "Sofa góc", 1.8f),
        fi("loungeSofaOttoman", "Sofa ottoman", 1.6f),
        fi("loungeDesignSofa", "Sofa thiết kế", 1.6f),
        fi("loungeDesignSofaCorner", "Sofa TK góc", 1.9f),
        fi("table", "Bàn", 1.2f),
        fi("tableRound", "Bàn tròn", 1.0f),
        fi("tableGlass", "Bàn kính", 1.2f),
        fi("tableCloth", "Bàn khăn trải", 1.2f),
        fi("tableCross", "Bàn chân chéo", 1.1f),
        fi("tableCrossCloth", "Bàn chéo khăn", 1.1f),
        fi("tableCoffee", "Bàn trà", 0.9f),
        fi("tableCoffeeGlass", "Bàn trà kính", 0.9f),
        fi("tableCoffeeSquare", "Bàn trà vuông", 0.8f),
        fi("tableCoffeeGlassSquare", "Bàn trà kính vuông", 0.8f),
        fi("desk", "Bàn làm việc", 1.2f),
        fi("deskCorner", "Bàn góc", 1.5f),
        fi("sideTable", "Bàn phụ", 0.5f),
        fi("sideTableDrawers", "Bàn phụ ngăn kéo", 0.5f),
        fi("kitchenBar", "Quầy bar", 1.0f),
        fi("kitchenBarEnd", "Quầy bar góc", 0.7f),
    )),
    CatalogGroup("Phòng ngủ", listOf(
        fi("bedDouble", "Giường đôi", 1.6f),
        fi("bedSingle", "Giường đơn", 1.0f),
        fi("bedBunk", "Giường tầng", 1.2f),
        fi("cabinetBed", "Tủ đầu giường", 0.5f),
        fi("cabinetBedDrawer", "Tủ đầu giường ngăn", 0.5f),
        fi("cabinetBedDrawerTable", "Tủ + bàn", 0.6f),
        fi("bookcaseClosedWide", "Tủ quần áo", 1.2f),
        fi("bookcaseClosed", "Tủ kín", 0.9f),
        fi("bookcaseClosedDoors", "Tủ có cửa", 0.9f),
        fi("bookcaseOpen", "Kệ sách", 0.9f),
        fi("bookcaseOpenLow", "Kệ sách thấp", 0.9f),
        fi("coatRack", "Móc treo tường", 0.6f, wall = true),
        fi("coatRackStanding", "Cây treo đồ", 0.6f),
        fi("pillow", "Gối", 0.5f),
        fi("pillowLong", "Gối ôm", 0.6f),
        fi("pillowBlue", "Gối xanh", 0.5f),
        fi("pillowBlueLong", "Gối ôm xanh", 0.6f),
    )),
    CatalogGroup("Đồ điện tử", listOf(
        fi("televisionModern", "TV", 1.2f, wall = true),
        fi("televisionVintage", "TV cổ", 0.8f),
        fi("televisionAntenna", "TV ăng-ten", 0.7f),
        fi("cabinetTelevision", "Kệ TV", 1.4f),
        fi("cabinetTelevisionDoors", "Kệ TV có cửa", 1.4f),
        fi("computerScreen", "Màn hình", 0.5f),
        fi("computerKeyboard", "Bàn phím", 0.4f),
        fi("computerMouse", "Chuột", 0.1f),
        fi("laptop", "Laptop", 0.4f),
        fi("speaker", "Loa", 0.4f),
        fi("speakerSmall", "Loa nhỏ", 0.2f),
        fi("radio", "Radio", 0.3f),
    )),
    CatalogGroup("Nhà bếp", listOf(
        fi("kitchenFridge", "Tủ lạnh", 0.7f),
        fi("kitchenFridgeSmall", "Tủ lạnh nhỏ", 0.6f),
        fi("kitchenFridgeLarge", "Tủ lạnh lớn", 0.9f),
        fi("kitchenFridgeBuiltIn", "Tủ lạnh âm", 0.7f),
        fi("kitchenStove", "Bếp ga", 0.7f),
        fi("kitchenStoveElectric", "Bếp điện", 0.7f),
        fi("kitchenSink", "Bồn rửa", 0.7f),
        fi("kitchenCabinet", "Tủ bếp", 0.7f),
        fi("kitchenCabinetDrawer", "Tủ bếp ngăn", 0.7f),
        fi("kitchenCabinetCornerInner", "Tủ bếp góc trong", 0.7f),
        fi("kitchenCabinetCornerRound", "Tủ bếp góc tròn", 0.7f),
        fi("kitchenCabinetUpper", "Tủ bếp treo", 0.7f, wall = true),
        fi("kitchenCabinetUpperLow", "Tủ treo thấp", 0.7f, wall = true),
        fi("kitchenCabinetUpperDouble", "Tủ treo đôi", 1.0f, wall = true),
        fi("kitchenCabinetUpperCorner", "Tủ treo góc", 0.7f, wall = true),
        fi("hoodModern", "Máy hút mùi", 0.7f, wall = true),
        fi("hoodLarge", "Máy hút mùi lớn", 0.9f, wall = true),
        fi("kitchenMicrowave", "Lò vi sóng", 0.5f),
        fi("kitchenCoffeeMachine", "Máy cà phê", 0.3f),
        fi("kitchenBlender", "Máy xay", 0.2f),
        fi("toaster", "Lò nướng", 0.3f),
        fi("washer", "Máy giặt", 0.6f),
        fi("dryer", "Máy sấy", 0.6f),
        fi("washerDryerStacked", "Máy giặt sấy", 0.6f),
    )),
    CatalogGroup("Nhà tắm & VS", listOf(
        fi("toilet", "Bồn cầu", 0.5f),
        fi("toiletSquare", "Bồn cầu vuông", 0.5f),
        fi("bathtub", "Bồn tắm", 1.6f),
        fi("shower", "Vòi sen", 0.9f),
        fi("showerRound", "Vòi sen tròn", 0.9f),
        fi("bathroomSink", "Lavabo", 0.5f),
        fi("bathroomSinkSquare", "Lavabo vuông", 0.5f),
        fi("bathroomCabinet", "Tủ phòng tắm", 0.6f),
        fi("bathroomCabinetDrawer", "Tủ PT ngăn", 0.6f),
        fi("bathroomMirror", "Gương", 0.6f, wall = true),
    )),
    CatalogGroup("Trang trí & đèn", listOf(
        fi("pottedPlant", "Cây cảnh", 0.5f),
        fi("plantSmall1", "Cây nhỏ 1", 0.3f),
        fi("plantSmall2", "Cây nhỏ 2", 0.3f),
        fi("plantSmall3", "Cây nhỏ 3", 0.3f),
        fi("books", "Sách", 0.3f),
        fi("bear", "Gấu bông", 0.3f),
        fi("cardboardBoxClosed", "Thùng kín", 0.4f),
        fi("cardboardBoxOpen", "Thùng mở", 0.4f),
        fi("trashcan", "Thùng rác", 0.3f),
        fi("rugRectangle", "Thảm chữ nhật", 1.6f),
        fi("rugSquare", "Thảm vuông", 1.4f),
        fi("rugRound", "Thảm tròn", 1.4f),
        fi("rugRounded", "Thảm bo tròn", 1.5f),
        fi("rugDoormat", "Thảm chùi chân", 0.7f),
        fi("lampRoundFloor", "Đèn cây tròn", 0.4f),
        fi("lampSquareFloor", "Đèn cây vuông", 0.4f),
        fi("lampRoundTable", "Đèn bàn tròn", 0.3f),
        fi("lampSquareTable", "Đèn bàn vuông", 0.3f),
        fi("lampWall", "Đèn tường", 0.4f, wall = true),
        fi("lampSquareCeiling", "Đèn trần", 0.5f, wall = true),
        fi("ceilingFan", "Quạt trần", 0.9f, wall = true),
    )),
)

private val CATALOG_BY_KEY: Map<String, CatalogItem> =
    FURNITURE_CATALOG.flatMap { it.items }.associateBy { it.key }

fun catalogItem(key: String): CatalogItem? = CATALOG_BY_KEY[key]

// ─── Wall / floor surface presets (choosing one replaces ALL walls / the floor) ──

data class SurfacePreset(val label: String, val model: String, val colorHex: String)

val WALL_PRESETS = listOf(
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
