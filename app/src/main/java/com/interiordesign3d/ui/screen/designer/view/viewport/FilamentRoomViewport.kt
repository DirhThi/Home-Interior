package com.interiordesign3d.ui.screen.designer.view.viewport

import com.interiordesign3d.data.catalog.*
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.roundToInt
import com.interiordesign3d.ui.screen.designer.*

import android.content.Context
import android.view.Choreographer
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.filament.Box
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.IndirectLight
import com.google.android.filament.MaterialInstance
import com.google.android.filament.LightManager
import com.google.android.filament.RenderableManager
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.VertexBuffer
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Utils
import com.interiordesign3d.data.models.Balcony
import com.interiordesign3d.data.models.FLOOR_SLAB_CM
import com.interiordesign3d.data.models.WALL_THICK_CM
import com.interiordesign3d.data.models.HOLE_MARGIN_CM
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.OpeningType
import com.interiordesign3d.data.models.Stair
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.models.RoofShape
import com.interiordesign3d.data.models.WallOpening
import com.interiordesign3d.data.models.WallPoint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

private val filamentReady: Boolean by lazy { Utils.init(); true }

private const val CM = 0.01f
private const val BRIDGE_NUDGE_CM = 0.15f
private val FLOOR_SLAB_M = FLOOR_SLAB_CM * CM
private const val OPENING_CASED = "doorway"      // cased opening: hole and reveal, no leaf
private const val DOOR_OPEN_DEG = 78f
private const val RAIL_H_M = 0.90f       // handrail above the nosing line
private const val RAIL_T_CM = 4f
private const val BALUSTER_T_CM = 3.5f
private const val POST_SPACING_CM = 40f   // sparser than a real balustrade; each post is geometry
private const val EAVES_CM = 25f
private const val ROOF_T_M = 0.16f
private const val PLOT_MARGIN_CM = 180f
private const val GROUND_DROP_M = 0.06f
private const val MIN_ZOOM = 0.4f        // pinch floor; also how far the camera can back off
private const val PARAPET_H_M = 0.95f
private const val FIELD_MODEL = "mat_grass005"
private const val FIELD_TINT = "#FFFFFF"
private const val FIELD_TILE_M = 3f
private const val OPEN_PLAN_MIN_CM = 200f        // a cased opening this wide loses its lintel
private const val DOOR_HEIGHT_M = 2.10f
private const val DOUBLE_DOOR_MIN_CM = 130f

/** Per-pack unit fix: Quaternius (q_*) models are authored at 2× real size; Kenney is 1 unit = 1 m. */
private fun packScale(furnitureId: String) = catalogItem(furnitureId)?.unitScale ?: 1f
private fun worldScale(f: PlacedFurniture) = f.scale * packScale(f.furnitureId)

@Composable
fun FilamentRoomViewport(
    floorPlan: FloorPlan,
    roomPolygons: List<List<WallPoint>>,
    placedFurniture: List<PlacedFurniture>,
    roomHeight: Float,
    activeLevel: Int = 0,
    stairModel: String = "mat_woodfloor007",
    stairColorHex: String = "#FFFFFF",
    stairTileM: Float = 1f,
    /** Outside view: every storey, plus a roof and the ground the house sits on. */
    exterior: Boolean = false,
    roofModel: String = "mat_concrete016",
    roofColorHex: String = "#7E7A76",
    roofTileM: Float = 2f,
    groundModel: String = "mat_pavingstones070",
    groundColorHex: String = "#FFFFFF",
    groundTileM: Float = 1.5f,
    shadows: Boolean = false,
    autoHideWalls: Boolean = true,
    backgroundColor: Color = Color(0xFFDAD5C8),
    onDropOpening: (roomIdx: Int, edgeIdx: Int, t: Float, widthCm: Float, furnitureId: String) -> Unit = { _, _, _, _, _ -> },
    onSelectFurniture: (String?) -> Unit = {},
    onSelectOpening: (String) -> Unit = {},
    onMoveFurniture: (String, Float, Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    if (!filamentReady) return
    val sceneRef = remember { mutableStateOf<RoomScene?>(null) }
    val onSelect = rememberUpdatedState(onSelectFurniture)
    val onMove = rememberUpdatedState(onMoveFurniture)
    val bgRef = rememberUpdatedState(backgroundColor)
    val onDrop = rememberUpdatedState(onDropOpening)
    val onPickOpening = rememberUpdatedState(onSelectOpening)

    // Pause/resume the render loop with the lifecycle so returning from background
    // re-attaches the surface and re-renders (otherwise the preview stays black).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> sceneRef.value?.resume()
                Lifecycle.Event.ON_PAUSE -> sceneRef.value?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val sv = SurfaceView(ctx)
            sceneRef.value = RoomScene(ctx, sv, bgRef.value, { onSelect.value(it) }, { id, x, z -> onMove.value(id, x, z) })
            sv
        },
        update = {
            sceneRef.value?.let { s ->
                s.setShadows(shadows)
                s.setAutoHideWalls(autoHideWalls)
                s.onDropOpening = { r, e, t, w, id -> onDrop.value(r, e, t, w, id) }
                s.onSelectOpening = { id -> onPickOpening.value(id) }
                s.update(floorPlan, activeLevel, roomPolygons, floorPlan.openings, placedFurniture, roomHeight,
                    stairModel, stairColorHex, stairTileM, exterior,
                    roofModel, roofColorHex, roofTileM, groundModel, groundColorHex, groundTileM)
            }
        },
        onRelease = { sceneRef.value?.destroy(); sceneRef.value = null }
    )
}

private class RoomScene(
    context: Context,
    private val surfaceView: SurfaceView,
    backdrop: Color,
    private val onSelect: (String?) -> Unit,
    private val onMove: (String, Float, Float) -> Unit,
) {
    val engine: Engine = Engine.create()
    private val renderer = engine.createRenderer()
    private val scene = engine.createScene()
    private val view = engine.createView()
    private val cameraEntity = EntityManager.get().create()
    private val camera = engine.createCamera(cameraEntity)
    private val displayHelper = DisplayHelper(context)
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private var swapChain: SwapChain? = null
    private var indirectLight: IndirectLight? = null

    private val materialProvider = UbershaderProvider(engine)
    private val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
    private val resourceLoader = ResourceLoader(engine)
    private val modelCache = HashMap<String, ByteBuffer>()
    private val ctx = context

    private val structureAssets = mutableListOf<FilamentAsset>()
    private val meshEntities = mutableListOf<Int>()          // procedural floor / wall boxes
    private val meshBuffers = mutableListOf<Pair<VertexBuffer, IndexBuffer>>()
    private val modelExtent = HashMap<String, FloatArray>()   // model path → authored w/h/d
    private val matAssets = HashMap<String, Pair<String, FilamentAsset>>()   // slot → (model, asset kept out of scene for its material)
    private class WallSeg(val nodeA: Int, val nodeB: Int, val exterior: Boolean,
                          val nx: Float, val nz: Float, val offCm: Float,
                          val mx: Float, val mz: Float, val entities: IntArray) {   // outward normal + world midpoint (m)
        fun touches(node: Int) = node == nodeA || node == nodeB
        fun isEdge(a: Int, b: Int) = (a == nodeA && b == nodeB) || (a == nodeB && b == nodeA)
    }
    private val wallSegs = mutableListOf<WallSeg>()
    private val openingWorld = LinkedHashMap<String, FloatArray>()   // opening id → world centre (m)
    private var wallHidden = BooleanArray(0)
    private class CornerSeg(val segs: IntArray, val entities: IntArray)   // post at a plan corner
    private val cornerSegs = mutableListOf<CornerSeg>()
    private var cornerHidden = BooleanArray(0)
    private var autoHideWalls = true
    var onDropOpening: (Int, Int, Float, Float, String) -> Unit = { _, _, _, _, _ -> }   // (nodeA, nodeB, t, widthCm, propId)
    var onSelectOpening: (String) -> Unit = {}
    private var wallFurnDirty = true                     // re-check wall-mounted furniture visibility
    private val furnitureHidden = HashSet<String>()      // furniture removed from scene with its hidden wall
    private val furnitureAssets = LinkedHashMap<String, FilamentAsset>()
    private val furnitureWorld = LinkedHashMap<String, FloatArray>()
    private val lights = mutableListOf<Int>()
    private var sunEntity = 0
    private var shadowsOn = false
    private var structSig = ""
    private val furnitureMeta = HashMap<String, PlacedFurniture>()   // last applied item per id
    @Volatile private var dirty = true                                 // render only when something changed
    private var roomHeightM = 2.6f
    private var polys: List<List<WallPoint>> = emptyList()
    private var planNodes: List<WallPoint> = emptyList()
    private var planRooms: List<List<Int>> = emptyList()
    private var planLevels: List<Int> = emptyList()

    private fun polysOnLevel(level: Int): List<List<WallPoint>> =
        planRooms.indices.filter { planLevels.getOrElse(it) { 0 } == level }
            .map { ri -> planRooms[ri].mapNotNull { planNodes.getOrNull(it) } }

    private var houseCx = 0f; private var houseCz = 0f

    private var azimuth = 35f; private var elevation = 28f; private var zoom = 1f
    private var centerX = 0f; private var centerY = 0.8f; private var centerZ = 0f
    private var radius = 6f
    /** Orbit parked when the other mode took over, so stepping back in returns the old viewpoint. */
    private var parkedOrbit: FloatArray? = null
    private var wasExterior: Boolean? = null
    private var eX = 0f; private var eY = 0f; private var eZ = 0f
    private val fwd = FloatArray(3); private val rgt = FloatArray(3); private val upv = FloatArray(3)
    private var vpW = 1f; private var vpH = 1f
    private val fovV = 50.0

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(t: Long) { choreographer.postFrameCallback(this); if (dirty) render(t) }
    }

    init {
        view.scene = scene
        view.camera = camera
        camera.setExposure(16f, 1f / 125f, 100f)
        // Backdrop comes from the theme so the viewport belongs to the rest of the screen.
        scene.skybox = Skybox.Builder()
            .color(srgbToLinear(backdrop.red), srgbToLinear(backdrop.green), srgbToLinear(backdrop.blue), 1.0f)
            .build(engine)
        // Perf: models are flat/unlit — drop the shadow pass, MSAA and dithering.
        view.setShadowingEnabled(false)
        view.setAntiAliasing(com.google.android.filament.View.AntiAliasing.FXAA)   // cheap, kills jaggies
        view.setDithering(com.google.android.filament.View.Dithering.NONE)
        // Dynamic resolution off: upscaling made edges blocky; the scene is light enough at native res.
        view.dynamicResolutionOptions = com.google.android.filament.View.DynamicResolutionOptions().apply { enabled = false }
        view.renderQuality = com.google.android.filament.View.RenderQuality().apply {
            hdrColorBuffer = com.google.android.filament.View.QualityLevel.LOW
        }
        // Cheap depth cues: SSAO darkens floor/wall junctions & under furniture; grading adds contrast.
        view.ambientOcclusionOptions = com.google.android.filament.View.AmbientOcclusionOptions().apply {
            enabled = true; quality = com.google.android.filament.View.QualityLevel.LOW
            radius = 0.35f; intensity = 1.0f; power = 1.5f
        }
        view.colorGrading = com.google.android.filament.ColorGrading.Builder()
            .contrast(1.12f).saturation(1.08f).build(engine)
        addLights()
        uiHelper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface)
                displayHelper.attach(renderer, surfaceView.display); dirty = true
            }
            override fun onDetachedFromSurface() {
                displayHelper.detach()
                swapChain?.let { engine.destroySwapChain(it); engine.flushAndWait(); swapChain = null }
            }
            override fun onResized(w: Int, h: Int) {
                vpW = w.toFloat(); vpH = h.toFloat()
                camera.setProjection(fovV, w.toDouble() / h.toDouble(), 0.05, 1000.0, Camera.Fov.VERTICAL)
                view.viewport = Viewport(0, 0, w, h); dirty = true
            }
        }
        uiHelper.attachTo(surfaceView)
        surfaceView.setOnTouchListener(::onTouch)
        choreographer.postFrameCallback(frameCallback)
    }

    private fun addLights() {
        // Lower ambient + a more side-on sun so the two visible walls shade differently (depth, less bleaching).
        indirectLight = IndirectLight.Builder()
            .irradiance(1, floatArrayOf(0.7f, 0.7f, 0.72f)).intensity(22_000f).build(engine)
        scene.indirectLight = indirectLight
        val sun = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1f, 0.97f, 0.92f).intensity(100_000f)
            .direction(0.65f, -0.8f, -0.3f).castShadows(true)
            .shadowOptions(LightManager.ShadowOptions().apply { mapSize = 512 })   // 1024 default is overkill here
            .build(engine, sun)
        scene.addEntity(sun); lights.add(sun); sunEntity = sun
        val fill = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.9f, 0.92f, 1f).intensity(18_000f)
            .direction(-0.5f, -0.7f, 0.5f).castShadows(false).build(engine, fill)
        scene.addEntity(fill); lights.add(fill)
    }

    fun update(
        plan: FloorPlan,
        activeLevel: Int,
        roomPolygons: List<List<WallPoint>>, openings: List<WallOpening>,
        furniture: List<PlacedFurniture>, roomHeightCm: Float,
        stairModel: String, stairColorHex: String, stairTileM: Float,
        exterior: Boolean,
        roofModel: String, roofColorHex: String, roofTileM: Float,
        groundModel: String, groundColorHex: String, groundTileM: Float,
    ) {
        val allPts = roomPolygons.flatten()
        if (allPts.isEmpty()) return
        polys = roomPolygons
        planNodes = plan.nodes
        planRooms = plan.rooms
        planLevels = plan.rooms.indices.map { plan.levelOf(it) }
        val minX = allPts.minOf { it.x }; val maxX = allPts.maxOf { it.x }
        val minZ = allPts.minOf { it.y }; val maxZ = allPts.maxOf { it.y }
        houseCx = (minX + maxX) / 2f; houseCz = (minZ + maxZ) / 2f
        val spanX = (maxX - minX).coerceAtLeast(1f); val spanZ = (maxZ - minZ).coerceAtLeast(1f)

        // Cheap fingerprint, not a description: this runs on every recomposition, and the old one
        // concatenated every polygon, opening, stair and surface into a fresh multi-kilobyte string.
        val sSig = roomPolygons.hashCode().toString() + "|" + plan.hashCode() + "|" +
                activeLevel + "|" + roomHeightCm.toInt() + "|" + exterior +
                "|" + stairModel + stairColorHex + "|" + roofModel + roofColorHex +
                "|" + groundModel + groundColorHex
        if (sSig != structSig) {
            structSig = sSig
            rebuildStructure(plan, activeLevel, roomHeightCm, stairModel, stairColorHex, stairTileM,
                exterior, roofModel, roofColorHex, roofTileM, groundModel, groundColorHex, groundTileM)
            // frame the room only when its geometry changes (keeps user's orbit otherwise)
            // Frame the whole stack, not one storey — otherwise a two-storey plan opens with the
            // camera parked inside the upper floor.
            // Outside invites a low, level view; inside that angle is under the floor, looking at
            // nothing. Park each mode's orbit and hand it back rather than carrying one across.
            if (wasExterior != null && wasExterior != exterior) {
                val park = floatArrayOf(azimuth, elevation, zoom)
                parkedOrbit?.let { azimuth = it[0]; elevation = it[1]; zoom = it[2] }
                    ?: run { elevation = if (exterior) 18f else 28f; zoom = 1f }
                parkedOrbit = park
            }
            wasExterior = exterior

            val storeys = if (exterior) plan.levelCount else activeLevel + 1
            val stackH = storeys.coerceAtLeast(1) * (roomHeightCm * CM + FLOOR_SLAB_M)
            centerX = 0f; centerZ = 0f; centerY = stackH * (if (exterior) 0.4f else 0.45f)
            radius = maxOf(spanX, spanZ) * CM * (if (exterior) 2.1f else 1.3f) + stackH
        }

        roomHeightM = roomHeightCm * CM
        // Incremental: load only new items, reload one whose model/colour changed, drop removed, move the rest.
        val wanted = furniture.associateBy { it.id }
        furnitureAssets.keys.filter { it !in wanted }.toList().forEach { removeFurniture(it) }
        furniture.forEach { f ->
            val last = furnitureMeta[f.id]
            when {
                last == null || last.furnitureId != f.furnitureId || last.colorOverride != f.colorOverride ->
                    { removeFurniture(f.id); addFurniture(f) }
                last === f -> Unit   // same instance → untouched since last pass
                else -> furnitureAssets[f.id]?.let { transformFurniture(it, f); furnitureMeta[f.id] = f }
            }
        }
        dirty = true; wallFurnDirty = true
    }

    private fun rebuildStructure(
        plan: FloorPlan,
        activeLevel: Int,
        roomHeightCm: Float,
        stairModel: String, stairColorHex: String, stairTileM: Float,
        exterior: Boolean,
        roofModel: String, roofColorHex: String, roofTileM: Float,
        groundModel: String, groundColorHex: String, groundTileM: Float,
    ) {
        structureAssets.forEach { runCatching { assetLoader.destroyAsset(it) } }
        structureAssets.clear()
        destroyMeshes()
        wallSegs.clear(); wallHidden = BooleanArray(0); openingWorld.clear()
        cornerSegs.clear(); cornerHidden = BooleanArray(0)
        fun wx(cm: Float) = (cm - houseCx) * CM
        fun wz(cm: Float) = (cm - houseCz) * CM
        val wt = WALL_THICK_CM * CM
        val hM = roomHeightCm * CM
        val floorT = FLOOR_SLAB_M
        val stairMi = materialOf("stair", stairModel, stairColorHex) ?: return

        // One floor per room, built from the room polygon itself and grown outward by the wall
        // thickness so it runs under the walls (a bbox slab would poke past off-square edges).
        val nodes = plan.nodes
        // Storeys stack: everything from the ground up to the one being edited is built, and
        // each storey's floor slab doubles as the ceiling of the one below.
        val topLevel = if (exterior) plan.levelCount - 1 else activeLevel.coerceAtMost(plan.levelCount - 1)
        for (level in 0..topLevel) {
            val baseY = level * (hM + floorT)
            // Finishes are per storey, so each gets its own material slot.
            val surf = plan.surfaceOf(level)
            val wp = WALL_PRESETS[surf.wallPresetIdx.coerceIn(WALL_PRESETS.indices)]
            val fp = FLOOR_PRESETS[surf.floorPresetIdx.coerceIn(FLOOR_PRESETS.indices)]
            val wallTint = surf.wallColor.ifBlank { wp.colorHex }
            val wallTileM = wp.tileM
            val floorTileM = fp.tileM
            val wallMi = materialOf("wall$level", wp.model, wallTint) ?: continue
            val trimMi = materialOf("trim$level", "mat_paint", wallTint, 0.72f) ?: continue
            val floorMi = materialOf("floor$level", fp.model, fp.colorHex) ?: continue
            val rooms = plan.roomsOnLevel(level).map { plan.rooms[it] }
            if (rooms.isEmpty()) continue
            val holes = plan.floorHoles(level)
            for (room in rooms) buildFloorMesh(room.map { nodes[it] }, floorMi, floorTileM, baseY, holes)
            // A storey with another above it needs a ceiling, or you see straight down through any
            // part of the footprint the upper storey does not cover. Sits just under the upper
            // floor slab rather than level with it, so the two never fight for the same plane.
            if (level < activeLevel) {
                val ceilHoles = plan.floorHoles(level + 1)
                for (room in rooms) buildFloorMesh(room.map { nodes[it] }, wallMi, wallTileM, baseY + hM + 0.01f, ceilHoles)
            }

            // ONE wall per unique edge. Two rooms sharing an edge used to build a slab each, offset
            // outward into one another — that is where the 20 cm party wall came from, and why a door
            // cut on a shared wall opened into the neighbour's solid wall.
            val uses = LinkedHashMap<Long, MutableList<EdgeUse>>()
            rooms.forEachIndexed { ri, room ->
                val poly = room.map { nodes[it] }
                val cx = poly.map { it.x }.average().toFloat()
                val cz = poly.map { it.y }.average().toFloat()
                for (i in room.indices) {
                    val n0 = room[i]; val n1 = room[(i + 1) % room.size]
                    val a = nodes[n0]; val b = nodes[n1]
                    val dx = b.x - a.x; val dz = b.y - a.y
                    val len = sqrt(dx * dx + dz * dz); if (len < 1f) continue
                    var nx = -dz / len; var nz = dx / len
                    // Point it away from this room's centre.
                    if (nx * (cx - (a.x + b.x) / 2f) + nz * (cz - (a.y + b.y) / 2f) > 0f) { nx = -nx; nz = -nz }
                    uses.getOrPut(edgeKey(n0, n1)) { mutableListOf() } += EdgeUse(ri, n0, n1, nx, nz)
                }
            }

            for ((_, edgeUses) in uses) {
                val first = edgeUses.first()
                val exterior = edgeUses.size == 1
                val n0 = first.nodeA; val n1 = first.nodeB
                val a = nodes[n0]; val b = nodes[n1]
                val dx = b.x - a.x; val dz = b.y - a.y
                val lenCm = sqrt(dx * dx + dz * dz); if (lenCm < 1f) continue
                val ux = dx / lenCm; val uz = dz / lenCm
                val nx = first.nx; val nz = first.nz
                val rotDeg = Math.toDegrees(atan2(-dz.toDouble(), dx.toDouble())).toFloat()
                // An exterior wall still sits outside its room's polygon, so interiors keep their size.
                // A shared wall is centred on the edge instead, so one wall serves both rooms.
                val off = if (exterior) WALL_THICK_CM / 2f else 0f
                val edgeEnts = mutableListOf<Int>()

                fun along(t: Float) = (a.x + ux * t * lenCm) to (a.y + uz * t * lenCm)
                fun slab(t0: Float, t1: Float, y0: Float, y1: Float) {
                    val segLen = (t1 - t0) * lenCm; if (segLen < 1f || y1 - y0 < 0.005f) return
                    val (mx, mz) = along((t0 + t1) / 2f)
                    edgeEnts += buildBox(wallMi, segLen * CM, y1 - y0, wt,
                        wx(mx + nx * off), baseY + y0, wz(mz + nz * off), rotDeg, wallTileM)
                }
                // 8 cm strip just proud of a face. A shared wall is seen from both sides, so it gets two.
                fun baseboardSide(t0: Float, t1: Float, side: Float) {
                    val segLen = (t1 - t0) * lenCm; if (segLen < 1f) return
                    val (mx, mz) = along((t0 + t1) / 2f)
                    val d = off + side * (WALL_THICK_CM / 2f + 0.6f)
                    edgeEnts += buildBox(trimMi, segLen * CM, 0.08f, 0.012f,
                        wx(mx + nx * d), baseY, wz(mz + nz * d), rotDeg, 1f)
                }
                fun baseboard(t0: Float, t1: Float) {
                    baseboardSide(t0, t1, -1f)
                    if (!exterior) baseboardSide(t0, t1, 1f)
                }

                val edgeOpenings = plan.openingsOn(n0, n1, level).sortedBy { op ->
                    if (op.nodeA == n0) op.t else 1f - op.t
                }
                var tPrev = 0f
                for (op in edgeOpenings) {
                    val t = if (op.nodeA == n0) op.t else 1f - op.t
                    val isDoor = op.type == OpeningType.DOOR
                    val doorH = DOOR_HEIGHT_M.coerceAtMost(hM - 0.10f).coerceAtLeast(1.80f)
                    val sill = if (isDoor) 0f else minOf(0.9f, hM * 0.35f)
                    val boxH = if (isDoor) doorH else minOf(1.3f, hM - sill - 0.3f).coerceAtLeast(0.4f)
                    val model = when {
                        op.leafHidden -> null            // applies to a window frame just as much as a door leaf
                        !isDoor -> if (op.widthCm > 130f) "q_window_large" else "q_window_small"
                        op.style == OPENING_CASED -> null
                        op.style.startsWith("q_door") -> op.style
                        op.widthCm >= DOUBLE_DOOR_MIN_CM -> "q_door_double"
                        else -> "q_door"
                    }
                    // Fit the joinery first, then cut the hole to the size it actually became —
                    // cutting to op.widthCm left a strip of bare wall beside anything fitted by height.
                    val path = model?.let { "models/$it.glb" }
                    val ext = path?.let { extentOf(it) }
                    val fitS = if (ext == null) 1f else minOf(op.widthCm * CM / ext[0], boxH / ext[1])
                    val cutW = if (ext == null) op.widthCm * CM else ext[0] * fitS
                    val cutH = if (ext == null) boxH else ext[1] * fitS

                    val halfT = (cutW / CM / 2f) / lenCm
                    val tS = (t - halfT).coerceIn(0f, 1f); val tE = (t + halfT).coerceIn(0f, 1f)
                    if (tS > tPrev + 1e-3f) { slab(tPrev, tS, -floorT, hM); baseboard(tPrev, tS) }
                    val (ox, oz) = along(t)
                    val lx = wx(ox + nx * off); val lz = wz(oz + nz * off)

                    // A cased opening wider than a doorway reads as one shared space: no lintel at all.
                    val openPlan = isDoor && model == null && op.widthCm >= OPEN_PLAN_MIN_CM
                    if (isDoor) {
                        if (!openPlan) slab(tS, tE, cutH, hM)
                    } else {
                        slab(tS, tE, -floorT, sill); baseboard(tS, tE)
                        slab(tS, tE, sill + cutH, hM)
                    }
                    openingWorld[op.id] = floatArrayOf(lx, baseY + sill + cutH / 2f, lz)
                    if (path != null) {
                        val swing = if (isDoor && op.leafOpen) DOOR_OPEN_DEG else 0f
                        placeFitted(path, cutW, cutH, lx, baseY + sill, lz, rotDeg, swing)
                            ?.let { edgeEnts += it.entities.toList() }
                    }
                    tPrev = tE
                }
                if (tPrev < 1f - 1e-3f) { slab(tPrev, 1f, -floorT, hM); baseboard(tPrev, 1f) }
                wallSegs.add(WallSeg(n0, n1, exterior, nx, nz, off,
                    wx((a.x + b.x) / 2f + nx * off), wz((a.y + b.y) / 2f + nz * off), edgeEnts.toIntArray()))
            }

            // Stairs rising from this storey to the next. The hole they need overhead is cut by
            // floorHoles(level + 1), from the very same footprint.
            plan.stairs.filter { it.level == level }.forEach { st ->
                val parts = mutableListOf<BoxSpec>()
                val rise = hM + floorT
                val count = Stair.stepCount(rise / CM)
                val riser = rise / count
                val runs = st.runs()
                val runLen = runs.map { (a, b) -> hypot(b.x - a.x, b.y - a.y) }
                val totalRun = runLen.sum().coerceAtLeast(1f)

                // Steps share out across the runs by length; the landing between them is flat and
                // sits level with the last step of the run feeding it.
                var stepIndex = 0
                val runStart = IntArray(runs.size)
                runs.forEachIndexed { ri, (a, b) ->
                    runStart[ri] = stepIndex
                    val isLast = ri == runs.lastIndex
                    val n = if (isLast) count - stepIndex
                            else ((count * runLen[ri] / totalRun).roundToInt()).coerceIn(1, count - stepIndex - 1)
                    if (n <= 0) return@forEachIndexed
                    val len = runLen[ri].coerceAtLeast(1e-3f)
                    val ux = (b.x - a.x) / len; val uy = (b.y - a.y) / len
                    val tread = len / n
                    val rot = Math.toDegrees(atan2(-uy.toDouble(), ux.toDouble())).toFloat()
                    for (k in 0 until n) {
                        val d = (k + 0.5f) * tread
                        val h = stepIndex + k
                        parts += BoxSpec(tread * CM, riser, st.widthCm * CM,
                            wx(a.x + ux * d), baseY + h * riser, wz(a.y + uy * d), rot)
                    }
                    stepIndex += n

                    // Handrail down both sides. The rail is one raking box along the nosing line and
                    // a baluster stands on every nosing, so the two always meet whatever the pitch.
                    val riseRun = n * riser
                    val pitch = Math.toDegrees(atan2(riseRun.toDouble(), (len * CM).toDouble())).toFloat()
                    val railLen = hypot(len * CM, riseRun)
                    val nX = -uy; val nY = ux            // plan normal, to step off to either side
                    val railY = baseY + runStart[ri] * riser + RAIL_H_M
                    for (side in intArrayOf(-1, 1)) {
                        val off = side * (st.widthCm / 2f - RAIL_T_CM / 2f)
                        val sx = a.x + nX * off; val sy = a.y + nY * off
                        parts += BoxSpec(railLen, RAIL_T_CM * CM, RAIL_T_CM * CM,
                            wx(sx + ux * len / 2f), railY + riseRun / 2f, wz(sy + uy * len / 2f),
                            rot, pitch)
                        val step = (POST_SPACING_CM / tread).roundToInt().coerceAtLeast(1)
                        for (k in step - 1 until n step step) {
                            val d = (k + 1) * tread
                            parts += BoxSpec(BALUSTER_T_CM * CM, RAIL_H_M, BALUSTER_T_CM * CM,
                                wx(sx + ux * d), baseY + (runStart[ri] + k + 1) * riser,
                                wz(sy + uy * d), rot)
                        }
                    }

                    // Landing at the top of every run but the last, guarded on the sides no run
                    // arrives at — otherwise the two flights' rails just stop in mid-air.
                    if (!isLast) {
                        val deckY = baseY + stepIndex * riser
                        st.landings().getOrNull(ri)?.let { (c, su, sv) ->
                            parts += BoxSpec(su * CM, riser, sv * CM,
                                wx(c.x), deckY - riser, wz(c.y), -st.rotationDeg)
                        }
                        st.landingRails().getOrNull(ri)?.forEach { (ra, rb) ->
                            railRun(ra, rb, deckY, parts, ::wx, ::wz)
                        }
                    }
                }
                buildBoxes(stairMi, parts, stairTileM)
            }

            // Balconies hang off the outside, so they belong to the storey whose wall carries them.
            plan.balconies.filter { it.level == level }.forEach { b ->
                val slab = plan.balconySlab(b) ?: return@forEach
                buildFloorMesh(slab, floorMi, floorTileM, baseY = baseY, outsetCm = 0f)
                // Rail the three open sides; the fourth is the wall it hangs from.
                val rails = mutableListOf<BoxSpec>()
                for (i in 1 until slab.size) {
                    railRun(slab[i], slab[(i + 1) % slab.size], baseY, rails, ::wx, ::wz)
                }
                buildBoxes(wallMi, rails, wallTileM)
                // Soffit, so it does not read as a floating sheet from below.
                val cx = slab.map { it.x }.average().toFloat()
                val cy = slab.map { it.y }.average().toFloat()
                val n = plan.outwardNormal(b.nodeA, b.nodeB, b.level) ?: return@forEach
                val rot = Math.toDegrees(atan2(n.x.toDouble(), n.y.toDouble())).toFloat()
                buildBox(wallMi, b.widthCm * CM, 0.12f, b.depthCm * CM,
                    wx(cx), baseY - 0.12f, wz(cy), rot, wallTileM)
            }

            // Guard the hole this flight leaves in the floor above, on every side but the one you
            // step out of. Without it the upper storey has an unfenced opening in it.
            if (level < activeLevel || (exterior && level < topLevel)) {
                val guards = mutableListOf<BoxSpec>()
                plan.stairs.filter { it.level == level }.forEach { st ->
                    val deckY = baseY + hM + floorT
                    st.wellGuards(HOLE_MARGIN_CM).forEach { (a, b) -> railRun(a, b, deckY, guards, ::wx, ::wz) }
                }
                buildBoxes(stairMi, guards, stairTileM)
            }

            // One post per plan corner. It has to sit where its walls actually are: an exterior wall is
            // pushed out by half its thickness, so the post follows by the sum of those pushes — parked
            // on the bare node it left a step at every outside corner.
            val usedNodes = rooms.flatten().toSet()
            for (nodeIdx in usedNodes) {
                val v = nodes[nodeIdx]
                val touching = wallSegs.indices.filter { wallSegs[it].touches(nodeIdx) }
                if (touching.isEmpty()) continue
                // Count each outward DIRECTION once. A wall running straight through a T-junction is two
                // segments sharing one normal, and summing both pushed the post out by a full thickness.
                var ox = 0f; var oz = 0f
                val counted = mutableListOf<FloatArray>()
                touching.forEach { i ->
                    val seg = wallSegs[i]
                    if (seg.offCm == 0f) return@forEach
                    if (counted.none { abs(it[0] - seg.nx) < 0.01f && abs(it[1] - seg.nz) < 0.01f }) {
                        counted += floatArrayOf(seg.nx, seg.nz)
                        ox += seg.nx * seg.offCm
                        oz += seg.nz * seg.offCm
                    }
                }
                val post = buildBox(wallMi, wt + 0.004f, hM + floorT, wt + 0.004f,
                    wx(v.x + ox), baseY - floorT, wz(v.y + oz), 0f, wallTileM)
                cornerSegs.add(CornerSeg(touching.toIntArray(), intArrayOf(post)))
            }
        }

        if (exterior) buildExterior(plan, topLevel, hM, floorT, ::wx, ::wz,
            roofModel, roofColorHex, roofTileM, groundModel, groundColorHex, groundTileM)
    }

    /**
     * What the house has when you step back from it: the ground it sits on, a plot around it, and a
     * flat roof over every storey. Roofs follow each storey's own outline rings, so an L-shaped or
     * split plan gets an L-shaped or split roof; the storey above is cut out as a hole, which is what
     * makes a smaller upper floor read as a box standing on a terrace instead of a lid on a lid.
     */
    private fun buildExterior(
        plan: FloorPlan, topLevel: Int, hM: Float, floorT: Float,
        wx: (Float) -> Float, wz: (Float) -> Float,
        roofModel: String, roofColorHex: String, roofTileM: Float,
        groundModel: String, groundColorHex: String, groundTileM: Float,
    ) {
        val ground = plan.outlineRings(0).filter { signedArea(it) > 0f }
        if (ground.isEmpty()) return

        // The field beyond the plot is always turf: it is scenery, not a finish anyone picks.
        val fieldMi = materialOf("field", FIELD_MODEL, FIELD_TINT) ?: return
        val plotMi = materialOf("plot", groundModel, groundColorHex) ?: return
        val roofMi = materialOf("roof", roofModel, roofColorHex) ?: return

        // Sized from how far the camera can actually get, not from the house: the orbit radius is
        // bounded and so is the zoom, so there IS a furthest point, and past it the ground can never
        // be seen to end. It costs nothing to be large — this is one quad whatever its size — but it
        // is not unbounded either: the projection stops at 1000 m, and a plane that dwarfs the house
        // would stretch the shadow cascade over ground nobody looks at.
        val pts = ground.flatten()
        val cx = (pts.minOf { it.x } + pts.maxOf { it.x }) / 2f
        val cy = (pts.minOf { it.y } + pts.maxOf { it.y }) / 2f
        val reach = maxOf(pts.maxOf { it.x } - pts.minOf { it.x }, pts.maxOf { it.y } - pts.minOf { it.y })
        val camMaxM = (reach * CM * 2.1f + (topLevel + 1) * (hM + floorT)) / MIN_ZOOM
        val half = maxOf(reach * 1.6f + PLOT_MARGIN_CM, camMaxM * 1.4f / CM)
        buildFloorMesh(
            listOf(
                WallPoint(cx - half, cy - half), WallPoint(cx + half, cy - half),
                WallPoint(cx + half, cy + half), WallPoint(cx - half, cy + half),
            ),
            fieldMi, FIELD_TILE_M, baseY = -GROUND_DROP_M, outsetCm = 0f,
        )
        ground.forEach { ring ->
            buildFloorMesh(
                outset(ring, PLOT_MARGIN_CM), plotMi, groundTileM,
                baseY = -GROUND_DROP_M / 2f, outsetCm = 0f,
            )
        }

        val ext = plan.exterior
        for (level in 0..topLevel) {
            val rings = plan.outlineRings(level).filter { signedArea(it) > 0f }
            if (rings.isEmpty()) continue
            val above = if (level < topLevel) {
                plan.outlineRings(level + 1).filter { signedArea(it) > 0f }
                    .map { outset(it, WALL_THICK_CM / 2f) }
            } else emptyList()
            val roofY = (level + 1) * (hM + floorT)

            // A pitched roof only crowns the top storey; the ones below stay flat, which is what
            // makes their uncovered part a terrace. Slanted plans fall back to flat, because the
            // mass split only holds for an orthogonal outline.
            if (ext.roofShape != RoofShape.FLAT && level == topLevel && plan.isOrthogonal(level)) {
                val masses = when (ext.roofShape) {
                    RoofShape.HIP -> listOf(boundingRing(rings.flatten()))
                    else -> plan.roofMasses(level)
                }
                // Stepping the ridges is what makes a mái Thái read as separate volumes rather than
                // one lid folded over the plan; a single-mass plan steps by nothing.
                // Eaves rest on the wall head, not on the next storey's floor level: that extra
                // slab thickness is right for a flat roof and leaves a pitched one floating.
                // Every mass sits at the same height — a narrower mass already gets a lower ridge
                // from its own width, so nothing needs stepping, and stepping it only lifted the
                // smaller roofs clear of their walls.
                val eavesY = level * (hM + floorT) + hM
                masses.forEach { mass ->
                    buildHip(mass, eavesY, ext.pitch, ext.eaves, ext.hipFactor, roofMi, roofTileM, wx, wz)
                }
                continue
            }

            rings.forEach { ring ->
                val eaves = outset(ring, WALL_THICK_CM / 2f + ext.eaves)
                val holes = above.filter { h -> h.all { pointInPoly(it, eaves) } }
                // Walking surface AT the storey-above floor level, with the slab hanging below it.
                // Sitting it on top instead put the terrace a slab's thickness above that storey, so
                // a balcony hung off an upper wall was buried in its own roof.
                buildFloorMesh(eaves, roofMi, roofTileM, baseY = roofY, holes = holes, outsetCm = 0f)
                // Fascia: without a visible edge the roof read as a sheet of paper floating there.
                eaves.indices.forEach { i ->
                    val a = eaves[i]; val b = eaves[(i + 1) % eaves.size]
                    val len = hypot(b.x - a.x, b.y - a.y)
                    if (len < 1f) return@forEach
                    val rot = Math.toDegrees(
                        atan2(-((b.y - a.y) / len).toDouble(), ((b.x - a.x) / len).toDouble())
                    ).toFloat()
                    buildBox(roofMi, len * CM, ROOF_T_M, 0.02f,
                        wx((a.x + b.x) / 2f), roofY - ROOF_T_M, wz((a.y + b.y) / 2f), rot, roofTileM)
                }
                // A flat roof is a terrace you could stand on, so it gets a parapet — the wall
                // carried up past the slab, which is also what stops it reading as a bare lid.
                val wallLine = outset(ring, WALL_THICK_CM / 2f)
                val parapet = mutableListOf<BoxSpec>()
                wallLine.indices.forEach { i ->
                    val a = wallLine[i]; val b = wallLine[(i + 1) % wallLine.size]
                    val len = hypot(b.x - a.x, b.y - a.y)
                    if (len < 1f) return@forEach
                    val rot = Math.toDegrees(
                        atan2(-((b.y - a.y) / len).toDouble(), ((b.x - a.x) / len).toDouble())
                    ).toFloat()
                    parapet += BoxSpec(len * CM, PARAPET_H_M, WALL_THICK_CM * CM,
                        wx((a.x + b.x) / 2f), roofY, wz((a.y + b.y) / 2f), rot)
                }
                buildBoxes(roofMi, parapet, roofTileM)
            }
        }
    }

    /** A horizontal rail with posts along one plan segment: stairwell guards and landing rails. */
    private fun railRun(
        a: WallPoint, b: WallPoint, deckY: Float, into: MutableList<BoxSpec>,
        wx: (Float) -> Float, wz: (Float) -> Float,
    ) {
        val len = hypot(b.x - a.x, b.y - a.y)
        if (len < 1f) return
        val ux = (b.x - a.x) / len; val uy = (b.y - a.y) / len
        val rot = Math.toDegrees(atan2(-uy.toDouble(), ux.toDouble())).toFloat()
        into += BoxSpec(len * CM, RAIL_T_CM * CM, RAIL_T_CM * CM,
            wx((a.x + b.x) / 2f), deckY + RAIL_H_M, wz((a.y + b.y) / 2f), rot)
        val posts = (len / POST_SPACING_CM).roundToInt().coerceIn(2, 14)
        for (q in 0..posts) {
            val d = len * q / posts
            into += BoxSpec(BALUSTER_T_CM * CM, RAIL_H_M, BALUSTER_T_CM * CM,
                wx(a.x + ux * d), deckY, wz(a.y + uy * d), rot)
        }
    }

    /** Four planes meeting at a ridge, over one rectangular mass. A square mass gives a pyramid. */
    private fun buildHip(
        mass: List<WallPoint>, baseY: Float, pitchDeg: Float, eavesCm: Float, hipFactor: Float,
        mi: MaterialInstance, tileM: Float, wx: (Float) -> Float, wz: (Float) -> Float,
    ) {
        val x0 = mass.minOf { it.x } - eavesCm; val x1 = mass.maxOf { it.x } + eavesCm
        val y0 = mass.minOf { it.y } - eavesCm; val y1 = mass.maxOf { it.y } + eavesCm
        val w = x1 - x0; val d = y1 - y0
        if (w < 1f || d < 1f) return
        val halfShort = minOf(w, d) / 2f
        val rise = halfShort * tan(Math.toRadians(pitchDeg.toDouble())).toFloat() * CM
        val top = baseY + rise
        // How far the ridge is pulled in from each end. Full inset is a hip; none leaves the ridge
        // running out to the wall, which turns the end plane vertical — that is a gable.
        val inset = halfShort * hipFactor.coerceIn(0f, 1f)
        fun p(x: Float, y: Float, h: Float) = Triple(wx(x), h, wz(y))
        val c0 = p(x0, y0, baseY); val c1 = p(x1, y0, baseY)
        val c2 = p(x1, y1, baseY); val c3 = p(x0, y1, baseY)
        val (r0, r1) = if (w >= d) {
            p(x0 + inset, (y0 + y1) / 2f, top) to p(x1 - inset, (y0 + y1) / 2f, top)
        } else {
            p((x0 + x1) / 2f, y0 + inset, top) to p((x0 + x1) / 2f, y1 - inset, top)
        }
        val core = p((x0 + x1) / 2f, (y0 + y1) / 2f, baseY)
        if (w >= d) {
            buildFace(listOf(c0, c1, r1, r0), mi, tileM, core)
            buildFace(listOf(c1, c2, r1), mi, tileM, core)
            buildFace(listOf(c2, c3, r0, r1), mi, tileM, core)
            buildFace(listOf(c3, c0, r0), mi, tileM, core)
        } else {
            buildFace(listOf(c0, c1, r0), mi, tileM, core)
            buildFace(listOf(c1, c2, r1, r0), mi, tileM, core)
            buildFace(listOf(c2, c3, r1), mi, tileM, core)
            buildFace(listOf(c3, c0, r0, r1), mi, tileM, core)
        }
    }

    private fun boundingRing(pts: List<WallPoint>): List<WallPoint> {
        val x0 = pts.minOf { it.x }; val x1 = pts.maxOf { it.x }
        val y0 = pts.minOf { it.y }; val y1 = pts.maxOf { it.y }
        return listOf(WallPoint(x0, y0), WallPoint(x1, y0), WallPoint(x1, y1), WallPoint(x0, y1))
    }



    private fun edgeKey(a: Int, b: Int): Long = minOf(a, b).toLong() * 100_000L + maxOf(a, b)

    private class EdgeUse(val room: Int, val nodeA: Int, val nodeB: Int, val nx: Float, val nz: Float)

    private fun addFurniture(f: PlacedFurniture) {
        val asset = load("models/${f.furnitureId}.glb") ?: return
        furnitureAssets[f.id] = asset
        furnitureMeta[f.id] = f
        f.colorOverride?.let { tint(asset, it) }
        transformFurniture(asset, f)
        scene.addEntities(asset.entities)
    }

    private fun removeFurniture(id: String) {
        val asset = furnitureAssets.remove(id) ?: return
        furnitureMeta.remove(id); furnitureWorld.remove(id); furnitureHidden.remove(id)
        runCatching { scene.removeEntities(asset.entities) }
        runCatching { assetLoader.destroyAsset(asset) }
    }

    private fun transformFurniture(asset: FilamentAsset, f: PlacedFurniture) {
        val bb = asset.boundingBox
        val s = worldScale(f)
        val halfH = bb.halfExtent[1] * s
        val mount = catalogItem(f.furnitureId)?.mount ?: MountType.FLOOR
        val levelY = f.level * (roomHeightM + FLOOR_SLAB_M)   // everything sits on its own storey

        if (f.isWallMounted || mount == MountType.WALL) {
            findNearestWall(f.posX, f.posZ, polysOnLevel(f.level))?.let { wall ->
                val inX = wall.normalX; val inZ = wall.normalZ
                val depth = bb.halfExtent[2] * s                      // back face flush with the wall
                val px = (wall.snappedX - houseCx) * CM + inX * depth
                val pz = (wall.snappedZ - houseCz) * CM + inZ * depth
                // model front is -Z → aim it along the inward normal so it faces the room
                val rotDeg = Math.toDegrees(atan2(-inX.toDouble(), -inZ.toDouble())).toFloat()
                val py = levelY + f.wallMountHeight * CM
                applyTransform(asset, s, s, s, bb.center, bb.halfExtent, px, py, pz, rotDeg, 0)
                furnitureWorld[f.id] = floatArrayOf(px, py, pz)
                return
            }
        }
        val wx = (f.posX - houseCx) * CM; val wz = (f.posZ - houseCz) * CM
        if (mount == MountType.CEILING) {
            applyTransform(asset, s, s, s, bb.center, bb.halfExtent, wx, levelY + roomHeightM, wz, f.rotationY, 1)
            furnitureWorld[f.id] = floatArrayOf(wx, levelY + roomHeightM - halfH, wz)
        } else {
            val baseY = levelY + supportTopUnder(f, wx, wz, bb, s)
            applyTransform(asset, s, s, s, bb.center, bb.halfExtent, wx, baseY, wz, f.rotationY, -1)
            furnitureWorld[f.id] = floatArrayOf(wx, baseY + halfH, wz)
        }
    }

    /** Small items (≤1 m tall, ≤0.9 m wide) rest on top of a `surface` item (table/desk/cabinet) they're over. */
    private fun supportTopUnder(f: PlacedFurniture, wx: Float, wz: Float, bb: Box, s: Float): Float {
        if (bb.halfExtent[1] * 2f * s > 1.0f || maxOf(bb.halfExtent[0], bb.halfExtent[2]) * 2f * s > 0.9f) return 0f
        if (catalogItem(f.furnitureId)?.surface == true) return 0f
        var top = 0f
        furnitureMeta.forEach { (id, o) ->
            if (id == f.id || catalogItem(o.furnitureId)?.surface != true) return@forEach
            val ob = furnitureAssets[id]?.boundingBox ?: return@forEach
            val ow = furnitureWorld[id] ?: return@forEach
            val os = worldScale(o)
            val half = maxOf(ob.halfExtent[0], ob.halfExtent[2]) * os   // rotation-safe footprint
            if (abs(wx - ow[0]) <= half && abs(wz - ow[2]) <= half) top = maxOf(top, ob.halfExtent[1] * 2f * os)
        }
        return top
    }

    // ── Drag placement: no overlap, stay inside the room, snap flush to a wall only when very close ──

    private val SNAP_CM = 8f

    /** Rotation-safe half extents (cm) of an item's XZ footprint. */
    private fun footprintCm(id: String): FloatArray? {
        val bb = furnitureAssets[id]?.boundingBox ?: return null
        val m = furnitureMeta[id] ?: return null
        val ws = worldScale(m)
        val hx = bb.halfExtent[0] * ws / CM; val hz = bb.halfExtent[2] * ws / CM
        val r = Math.toRadians(m.rotationY.toDouble())
        val c = abs(cos(r)).toFloat(); val s = abs(sin(r)).toFloat()
        return floatArrayOf(hx * c + hz * s, hx * s + hz * c)
    }

    private fun isFlat(id: String) = (furnitureAssets[id]?.boundingBox?.halfExtent?.get(1) ?: 1f) * 2f < 0.06f
    private fun isSmall(id: String): Boolean {
        val bb = furnitureAssets[id]?.boundingBox ?: return false
        val s = furnitureMeta[id]?.let { worldScale(it) } ?: 1f
        return bb.halfExtent[1] * 2f * s <= 1.0f && maxOf(bb.halfExtent[0], bb.halfExtent[2]) * 2f * s <= 0.9f
    }
    private fun onFloor(o: PlacedFurniture) =
        !o.isWallMounted && (catalogItem(o.furnitureId)?.mount ?: MountType.FLOOR) == MountType.FLOOR

    private fun inside(x: Float, z: Float, poly: List<WallPoint>): Boolean {
        var hit = false; var j = poly.size - 1
        for (i in poly.indices) {
            val a = poly[i]; val b = poly[j]
            if ((a.y > z) != (b.y > z) && x < (b.x - a.x) * (z - a.y) / (b.y - a.y) + a.x) hit = !hit
            j = i
        }
        return hit
    }

    /** A door prop (q_door* / doorway*) released within 20 cm of a wall becomes a real DOOR opening on that edge. */
    private fun tryDropDoorOnWall(id: String) {
        val m = furnitureMeta[id] ?: return
        if (!m.furnitureId.startsWith("q_door") && !m.furnitureId.startsWith("doorway")) return
        val (nA, nB, t) = nearestEdge(m.posX, m.posZ, 20f) ?: return
        val bb = furnitureAssets[id]?.boundingBox
        val widthCm = ((bb?.halfExtent?.get(0) ?: 0.45f) * 2f * worldScale(m) / CM).coerceAtLeast(80f)
        onDropOpening(nA, nB, t, widthCm, id)
    }

    private fun resolveDrag(id: String, x0: Float, z0: Float): FloatArray {
        val me = furnitureMeta[id] ?: return floatArrayOf(x0, z0)
        if (!onFloor(me)) return floatArrayOf(x0, z0)          // wall/ceiling items snap on their own
        val fp = footprintCm(id) ?: return floatArrayOf(x0, z0)
        var x = x0; var z = z0
        val small = isSmall(id)
        val flat = isFlat(id)
        val poly = polys.firstOrNull { inside(x, z, it) }
            ?: polys.firstOrNull { inside(me.posX, me.posZ, it) }
        val cx = poly?.map { it.x }?.average()?.toFloat() ?: 0f
        val cz = poly?.map { it.y }?.average()?.toFloat() ?: 0f

        // Push out of other floor items along the min-penetration axis. Rugs never collide;
        // small items may sit on a surface.
        fun pushOutOfFurniture() {
            if (flat) return
            furnitureMeta.forEach { (oid, o) ->
                if (oid == id || !onFloor(o) || isFlat(oid)) return@forEach
                if (small && catalogItem(o.furnitureId)?.surface == true) return@forEach
                val ofp = footprintCm(oid) ?: return@forEach
                val dx = x - o.posX; val dz = z - o.posZ
                val px = fp[0] + ofp[0] - abs(dx); val pz = fp[1] + ofp[1] - abs(dz)
                if (px > 0f && pz > 0f) {
                    if (px < pz) x += (if (dx < 0f) -px else px) else z += (if (dz < 0f) -pz else pz)
                }
            }
        }
        // Keep inside the room; snap flush to a wall only when the gap is already tiny.
        fun clampToWalls() {
            poly ?: return
            for (i in poly.indices) {
                val a = poly[i]; val b = poly[(i + 1) % poly.size]
                val ex = b.x - a.x; val ez = b.y - a.y
                val len = hypot(ex, ez); if (len < 1f) continue
                var nx = -ez / len; var nz = ex / len
                if (nx * (cx - a.x) + nz * (cz - a.y) < 0f) { nx = -nx; nz = -nz }   // inward
                val d = (x - a.x) * nx + (z - a.y) * nz
                val ext = fp[0] * abs(nx) + fp[1] * abs(nz)
                val gap = d - ext
                if (gap < 0f || gap < SNAP_CM) { x -= gap * nx; z -= gap * nz }
            }
        }
        // Alternate a few times: a wall snap can push back into furniture and vice versa.
        repeat(3) { pushOutOfFurniture(); clampToWalls() }
        return floatArrayOf(x, z)
    }

    private fun destroyMeshes() {
        meshEntities.forEach { runCatching { scene.removeEntity(it) }; runCatching { engine.destroyEntity(it) } }
        meshBuffers.forEach { (vb, ib) ->
            runCatching { engine.destroyVertexBuffer(vb) }; runCatching { engine.destroyIndexBuffer(ib) }
        }
        meshEntities.clear(); meshBuffers.clear()
    }

    /** Material instance of a mat_*.glb (textured quad) — reused as-is: a fresh instance would miss
     *  the glTF defaults gltfio fills in and render near-black. One asset per slot so wall / trim /
     *  floor can carry different tints. The asset itself never enters the scene. */
    private fun materialOf(slot: String, model: String, colorHex: String, mul: Float = 1f): MaterialInstance? {
        val cur = matAssets[slot]
        val asset = if (cur != null && cur.first == model) cur.second else {
            cur?.let { runCatching { assetLoader.destroyAsset(it.second) } }
            val a = load("models/$model.glb") ?: load("models/mat_paint.glb") ?: return null
            matAssets[slot] = model to a; a
        }
        val rm = engine.renderableManager
        for (e in asset.entities) {
            val ri = rm.getInstance(e)
            if (ri != 0 && rm.getPrimitiveCount(ri) > 0) {
                val mi = rm.getMaterialInstanceAt(ri, 0)
                val (r, g, b) = hexLinear(colorHex)
                runCatching { mi.setParameter("baseColorFactor", r * mul, g * mul, b * mul, 1f) }
                return mi
            }
        }
        return null
    }

    // The ubershader declares position+tangents+color+uv0+uv1; every one must be supplied.
    private val VSTRIDE = 60
    private fun ByteBuffer.vertex(x: Float, y: Float, z: Float, q: FloatArray, u: Float, v: Float): ByteBuffer =
        putFloat(x).putFloat(y).putFloat(z).putFloat(q[0]).putFloat(q[1]).putFloat(q[2]).putFloat(q[3])
            .putFloat(1f).putFloat(1f).putFloat(1f).putFloat(1f).putFloat(u).putFloat(v).putFloat(u).putFloat(v)

    private fun addMesh(vbData: ByteBuffer, vCount: Int, ibData: ByteBuffer, iCount: Int, mi: MaterialInstance,
                        bbox: Box, transform: FloatArray?, castShadows: Boolean): Int {
        val vb = VertexBuffer.Builder().bufferCount(1).vertexCount(vCount)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, VSTRIDE)
            .attribute(VertexBuffer.VertexAttribute.TANGENTS, 0, VertexBuffer.AttributeType.FLOAT4, 12, VSTRIDE)
            .attribute(VertexBuffer.VertexAttribute.COLOR, 0, VertexBuffer.AttributeType.FLOAT4, 28, VSTRIDE)
            .attribute(VertexBuffer.VertexAttribute.UV0, 0, VertexBuffer.AttributeType.FLOAT2, 44, VSTRIDE)
            .attribute(VertexBuffer.VertexAttribute.UV1, 0, VertexBuffer.AttributeType.FLOAT2, 52, VSTRIDE)
            .build(engine)
        vb.setBufferAt(engine, 0, vbData)
        val ib = IndexBuffer.Builder().indexCount(iCount).bufferType(IndexBuffer.Builder.IndexType.USHORT).build(engine)
        ib.setBuffer(engine, ibData)
        val entity = EntityManager.get().create()
        RenderableManager.Builder(1).boundingBox(bbox)
            .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vb, ib, 0, iCount)
            .material(0, mi).castShadows(castShadows).receiveShadows(true)
            .build(engine, entity)
        if (transform != null) {
            val tm = engine.transformManager
            tm.create(entity); tm.setTransform(tm.getInstance(entity), transform)
        }
        scene.addEntity(entity)
        meshEntities += entity; meshBuffers += vb to ib
        return entity
    }

    /** Box sx×sy×sz with its bottom centre at (px,py,pz), rotated about Y. Face UVs are in metres /
     *  [tileM] so the texture repeats at real size regardless of the box dimensions. */
    /** One box in a batch; the same numbers [buildBox] takes, minus the material and tiling. */
    private class BoxSpec(
        val sx: Float, val sy: Float, val sz: Float,
        val px: Float, val py: Float, val pz: Float,
        val rotDeg: Float, val pitchDeg: Float = 0f,
    )

    /**
     * Many boxes, one renderable. Filament draws each renderable separately, so a balustrade built a
     * baluster at a time cost one draw call per post — a single U flight ran to over a hundred, which
     * is fine on a desktop GPU and not on a phone. Here the transform is baked into the vertices
     * instead of living on the entity, which is the whole reason they can share one buffer.
     */
    private fun buildBoxes(mi: MaterialInstance, boxes: List<BoxSpec>, tileM: Float) {
        if (boxes.isEmpty()) return
        val vb = ByteBuffer.allocateDirect(boxes.size * 24 * VSTRIDE).order(ByteOrder.nativeOrder())
        val ib = ByteBuffer.allocateDirect(boxes.size * 36 * 2).order(ByteOrder.nativeOrder())
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

        boxes.forEachIndexed { bi, b ->
            val rad = Math.toRadians(b.rotDeg.toDouble()); val c = cos(rad).toFloat(); val sn = sin(rad).toFloat()
            val pr = Math.toRadians(b.pitchDeg.toDouble()); val cp = cos(pr).toFloat(); val sp = sin(pr).toFloat()
            // Columns of the same rotation buildBox uses, as the images of local x, y, z.
            val ax = c * cp; val ay = sp; val az = -sn * cp
            val bx = -c * sp; val by = cp; val bz = sn * sp
            val cx = sn; val cy = 0f; val cz = c
            val qR = quatOf(ax, ay, az, bx, by, bz, cx, cy, cz)
            val w = b.sx / 2f; val h = b.sz / 2f; val t = 1f / tileM
            fun put(lx: Float, ly: Float, lz: Float, q: FloatArray, u: Float, v: Float) {
                val x = b.px + ax * lx + bx * ly + cx * lz
                val y = b.py + ay * lx + by * ly + cy * lz
                val z = b.pz + az * lx + bz * ly + cz * lz
                if (x < minX) minX = x; if (x > maxX) maxX = x
                if (y < minY) minY = y; if (y > maxY) maxY = y
                if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
                vb.vertex(x, y, z, quatMul(qR, q), u, v)
            }
            val qPZ = floatArrayOf(0f, 0f, 0f, 1f); val qNZ = floatArrayOf(0f, 1f, 0f, 0f)
            val qPX = floatArrayOf(0f, 0.70710678f, 0f, 0.70710678f); val qNX = floatArrayOf(0f, -0.70710678f, 0f, 0.70710678f)
            val qPY = floatArrayOf(-0.70710678f, 0f, 0f, 0.70710678f); val qNY = floatArrayOf(0.70710678f, 0f, 0f, 0.70710678f)
            put(-w, 0f, h, qPZ, -w * t, 0f); put(w, 0f, h, qPZ, w * t, 0f); put(w, b.sy, h, qPZ, w * t, b.sy * t); put(-w, b.sy, h, qPZ, -w * t, b.sy * t)
            put(w, 0f, -h, qNZ, w * t, 0f); put(-w, 0f, -h, qNZ, -w * t, 0f); put(-w, b.sy, -h, qNZ, -w * t, b.sy * t); put(w, b.sy, -h, qNZ, w * t, b.sy * t)
            put(w, 0f, h, qPX, h * t, 0f); put(w, 0f, -h, qPX, -h * t, 0f); put(w, b.sy, -h, qPX, -h * t, b.sy * t); put(w, b.sy, h, qPX, h * t, b.sy * t)
            put(-w, 0f, -h, qNX, -h * t, 0f); put(-w, 0f, h, qNX, h * t, 0f); put(-w, b.sy, h, qNX, h * t, b.sy * t); put(-w, b.sy, -h, qNX, -h * t, b.sy * t)
            put(-w, b.sy, h, qPY, -w * t, h * t); put(w, b.sy, h, qPY, w * t, h * t); put(w, b.sy, -h, qPY, w * t, -h * t); put(-w, b.sy, -h, qPY, -w * t, -h * t)
            put(w, 0f, h, qNY, w * t, h * t); put(-w, 0f, h, qNY, -w * t, h * t); put(-w, 0f, -h, qNY, -w * t, -h * t); put(w, 0f, -h, qNY, w * t, -h * t)
            val base = bi * 24
            for (f in 0 until 6) {
                val o = base + f * 4
                intArrayOf(o, o + 1, o + 2, o, o + 2, o + 3).forEach { ib.putShort(it.toShort()) }
            }
        }
        vb.flip(); ib.flip()
        addMesh(
            vb, boxes.size * 24, ib, boxes.size * 36, mi,
            Box((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f,
                (maxX - minX) / 2f + 0.01f, (maxY - minY) / 2f + 0.01f, (maxZ - minZ) / 2f + 0.01f),
            null, true,
        )
    }

    /** Rotation matrix (given as its three column vectors) to a quaternion. */
    private fun quatOf(
        ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float, cx: Float, cy: Float, cz: Float,
    ): FloatArray {
        val tr = ax + by + cz
        return if (tr > 0f) {
            val s = sqrt(tr + 1f) * 2f
            floatArrayOf((bz - cy) / s, (cx - az) / s, (ay - bx) / s, 0.25f * s)
        } else if (ax > by && ax > cz) {
            val s = sqrt(1f + ax - by - cz) * 2f
            floatArrayOf(0.25f * s, (bx + ay) / s, (cx + az) / s, (bz - cy) / s)
        } else if (by > cz) {
            val s = sqrt(1f + by - ax - cz) * 2f
            floatArrayOf((bx + ay) / s, 0.25f * s, (cy + bz) / s, (cx - az) / s)
        } else {
            val s = sqrt(1f + cz - ax - by) * 2f
            floatArrayOf((cx + az) / s, (cy + bz) / s, 0.25f * s, (ay - bx) / s)
        }
    }

    private fun quatMul(a: FloatArray, b: FloatArray) = floatArrayOf(
        a[3] * b[0] + a[0] * b[3] + a[1] * b[2] - a[2] * b[1],
        a[3] * b[1] - a[0] * b[2] + a[1] * b[3] + a[2] * b[0],
        a[3] * b[2] + a[0] * b[1] - a[1] * b[0] + a[2] * b[3],
        a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2],
    )

    /**
     * [pitchDeg] tilts the box about its own length, so a box can rake: positive lifts its +X end.
     * Rotation happens about the local origin — centred in X and Z, at the base in Y — so place a
     * raking box at the midpoint of the line it should follow.
     */
    private fun buildBox(mi: MaterialInstance, sx: Float, sy: Float, sz: Float,
                         px: Float, py: Float, pz: Float, rotDeg: Float, tileM: Float,
                         pitchDeg: Float = 0f): Int {
        val w = sx / 2f; val h = sz / 2f; val t = 1f / tileM
        val qPZ = floatArrayOf(0f, 0f, 0f, 1f); val qNZ = floatArrayOf(0f, 1f, 0f, 0f)
        val qPX = floatArrayOf(0f, 0.70710678f, 0f, 0.70710678f); val qNX = floatArrayOf(0f, -0.70710678f, 0f, 0.70710678f)
        val qPY = floatArrayOf(-0.70710678f, 0f, 0f, 0.70710678f); val qNY = floatArrayOf(0.70710678f, 0f, 0f, 0.70710678f)
        val vb = ByteBuffer.allocateDirect(24 * VSTRIDE).order(ByteOrder.nativeOrder())
        // Each face: 4 corners counter-clockwise seen from outside.
        vb.vertex(-w, 0f, h, qPZ, -w * t, 0f).vertex(w, 0f, h, qPZ, w * t, 0f).vertex(w, sy, h, qPZ, w * t, sy * t).vertex(-w, sy, h, qPZ, -w * t, sy * t)
        vb.vertex(w, 0f, -h, qNZ, w * t, 0f).vertex(-w, 0f, -h, qNZ, -w * t, 0f).vertex(-w, sy, -h, qNZ, -w * t, sy * t).vertex(w, sy, -h, qNZ, w * t, sy * t)
        vb.vertex(w, 0f, h, qPX, h * t, 0f).vertex(w, 0f, -h, qPX, -h * t, 0f).vertex(w, sy, -h, qPX, -h * t, sy * t).vertex(w, sy, h, qPX, h * t, sy * t)
        vb.vertex(-w, 0f, -h, qNX, -h * t, 0f).vertex(-w, 0f, h, qNX, h * t, 0f).vertex(-w, sy, h, qNX, h * t, sy * t).vertex(-w, sy, -h, qNX, -h * t, sy * t)
        vb.vertex(-w, sy, h, qPY, -w * t, h * t).vertex(w, sy, h, qPY, w * t, h * t).vertex(w, sy, -h, qPY, w * t, -h * t).vertex(-w, sy, -h, qPY, -w * t, -h * t)
        vb.vertex(w, 0f, h, qNY, w * t, h * t).vertex(-w, 0f, h, qNY, -w * t, h * t).vertex(-w, 0f, -h, qNY, -w * t, -h * t).vertex(w, 0f, -h, qNY, w * t, -h * t)
        vb.flip()
        val ib = ByteBuffer.allocateDirect(36 * 2).order(ByteOrder.nativeOrder())
        for (f in 0 until 6) { val b = f * 4; intArrayOf(b, b + 1, b + 2, b, b + 2, b + 3).forEach { ib.putShort(it.toShort()) } }
        ib.flip()
        val rad = Math.toRadians(rotDeg.toDouble()); val c = cos(rad).toFloat(); val sn = sin(rad).toFloat()
        val pr = Math.toRadians(pitchDeg.toDouble()); val cp = cos(pr).toFloat(); val sp = sin(pr).toFloat()
        val m = floatArrayOf(
            c * cp, sp, -sn * cp, 0f,
            -c * sp, cp, sn * sp, 0f,
            sn, 0f, c, 0f,
            px, py, pz, 1f,
        )
        return addMesh(vb, 24, ib, 36, mi, Box(0f, sy / 2f, 0f, w, sy / 2f, h), m, true)
    }

    /** Flat floor at y = 0 covering the room polygon (grown outward by the wall thickness). */
    /** One flat convex face at arbitrary heights — a roof plane, which buildFloorMesh cannot express. */
    private fun buildFace(
        face: List<Triple<Float, Float, Float>>, mi: MaterialInstance, tileM: Float,
        inside: Triple<Float, Float, Float>,
    ) {
        if (face.size < 3) return
        // The material is single-sided, so winding decides whether the face exists at all. "Points
        // up" is not enough to settle it: a gable end stands vertical, so one of the two ends always
        // came out culled. Wind every face away from [inside] instead.
        val pts = if (facesAwayFrom(face, inside)) face else face.reversed()
        val q = faceTangent(pts)
        val vb = ByteBuffer.allocateDirect(pts.size * VSTRIDE).order(ByteOrder.nativeOrder())
        pts.forEach { (x, y, z) -> vb.vertex(x, y, z, q, x / tileM, z / tileM) }
        vb.flip()
        val ib = ByteBuffer.allocateDirect((pts.size - 2) * 3 * 2).order(ByteOrder.nativeOrder())
        for (i in 1 until pts.size - 1) {
            ib.putShort(0); ib.putShort(i.toShort()); ib.putShort((i + 1).toShort())
        }
        ib.flip()
        val cx = pts.map { it.first }.average().toFloat()
        val cy = pts.map { it.second }.average().toFloat()
        val cz = pts.map { it.third }.average().toFloat()
        val hx = (pts.maxOf { it.first } - pts.minOf { it.first }) / 2f + 0.01f
        val hy = (pts.maxOf { it.second } - pts.minOf { it.second }) / 2f + 0.01f
        val hz = (pts.maxOf { it.third } - pts.minOf { it.third }) / 2f + 0.01f
        addMesh(vb, pts.size, ib, (pts.size - 2) * 3, mi, Box(cx, cy, cz, hx, hy, hz), null, false)
    }

    /**
     * Filament wants a tangent frame, not a normal. The frame's local +Z is the normal — that is why
     * the flat-floor quaternion below is a -90° turn about X — so this is the shortest arc from
     * (0,0,1) to the face's own normal.
     */
    private fun facesAwayFrom(
        pts: List<Triple<Float, Float, Float>>, inside: Triple<Float, Float, Float>,
    ): Boolean {
        val (ax, ay, az) = pts[0]; val (bx, by, bz) = pts[1]; val (cx, cy, cz) = pts[2]
        val ux = bx - ax; val uy = by - ay; val uz = bz - az
        val vx = cx - ax; val vy = cy - ay; val vz = cz - az
        val nx = uy * vz - uz * vy; val ny = uz * vx - ux * vz; val nz = ux * vy - uy * vx
        val mx = pts.map { it.first }.average().toFloat() - inside.first
        val my = pts.map { it.second }.average().toFloat() - inside.second
        val mz = pts.map { it.third }.average().toFloat() - inside.third
        return nx * mx + ny * my + nz * mz > 0f
    }

    private fun faceTangent(pts: List<Triple<Float, Float, Float>>): FloatArray {
        val (ax, ay, az) = pts[0]; val (bx, by, bz) = pts[1]; val (cx, cy, cz) = pts[2]
        val ux = bx - ax; val uy = by - ay; val uz = bz - az
        val vx = cx - ax; val vy = cy - ay; val vz = cz - az
        var nx = uy * vz - uz * vy; var ny = uz * vx - ux * vz; var nz = ux * vy - uy * vx
        val len = sqrt(nx * nx + ny * ny + nz * nz)
        if (len < 1e-6f) return floatArrayOf(-0.70710678f, 0f, 0f, 0.70710678f)
        nx /= len; ny /= len; nz /= len
        if (ny < 0f) { nx = -nx; ny = -ny; nz = -nz }      // a roof plane always faces up
        val w = 1f + nz
        if (w < 1e-6f) return floatArrayOf(0f, 1f, 0f, 0f)  // straight down: half turn about Y
        val qx = -ny; val qy = nx; val qz = 0f
        val n2 = sqrt(qx * qx + qy * qy + qz * qz + w * w)
        return floatArrayOf(qx / n2, qy / n2, qz / n2, w / n2)
    }

    /**
     * [outsetCm] defaults to the wall thickness because a room floor has to run under its walls. It
     * is wrong for anything that is not a room: a balcony slab grew 10 cm on every side, poking past
     * its own railing and back through the wall it hangs on.
     */
    private fun buildFloorMesh(
        poly: List<WallPoint>, mi: MaterialInstance, tileM: Float, baseY: Float = 0f,
        holes: List<List<WallPoint>> = emptyList(), outsetCm: Float = WALL_THICK_CM,
    ) {
        if (poly.size < 3) return
        var pts = if (outsetCm != 0f) outset(poly, outsetCm) else poly
        // Only holes lying WHOLLY inside this room. Bridging a hole that pokes past the outer ring
        // produces a self-crossing polygon, which the ear clipper abandons half-done.
        holes.filter { h -> h.all { pointInPoly(it, pts) } }.forEach { pts = bridgeHole(pts, it) }
        val tris = triangulate(pts); if (tris.isEmpty()) return
        val qUp = floatArrayOf(-0.70710678f, 0f, 0f, 0.70710678f)
        val vbData = ByteBuffer.allocateDirect(pts.size * VSTRIDE).order(ByteOrder.nativeOrder())
        pts.forEach { pt -> vbData.vertex((pt.x - houseCx) * CM, baseY, (pt.y - houseCz) * CM, qUp, pt.x * CM / tileM, pt.y * CM / tileM) }
        vbData.flip()
        val ibData = ByteBuffer.allocateDirect(tris.size * 2).order(ByteOrder.nativeOrder())
        tris.forEach { ibData.putShort(it.toShort()) }
        ibData.flip()
        val minX = pts.minOf { it.x }; val maxX = pts.maxOf { it.x }
        val minZ = pts.minOf { it.y }; val maxZ = pts.maxOf { it.y }
        addMesh(vbData, pts.size, ibData, tris.size, mi,
            Box(((minX + maxX) / 2f - houseCx) * CM, baseY, ((minZ + maxZ) / 2f - houseCz) * CM,
                (maxX - minX) * CM / 2f + 0.01f, 0.01f, (maxZ - minZ) * CM / 2f + 0.01f), null, false)
    }

    private fun pointInPoly(pt: WallPoint, poly: List<WallPoint>): Boolean {
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val a = poly[i]; val b = poly[j]
            if ((a.y > pt.y) != (b.y > pt.y) &&
                pt.x < (b.x - a.x) * (pt.y - a.y) / (b.y - a.y) + a.x
            ) inside = !inside
            j = i
        }
        return inside
    }

    /** Offset every edge outward by [cm] (corners = intersection of the two offset lines), so the
     *  floor reaches the outer face of its wall on every edge — including ones drawn off-square. */
    private fun outset(poly: List<WallPoint>, cm: Float): List<WallPoint> {
        val n = poly.size
        val cx = poly.map { it.x }.average().toFloat(); val cz = poly.map { it.y }.average().toFloat()
        val nx = FloatArray(n); val nz = FloatArray(n)
        for (i in 0 until n) {
            val a = poly[i]; val b = poly[(i + 1) % n]
            val len = hypot(b.x - a.x, b.y - a.y); if (len < 1e-3f) return poly
            var ux = -(b.y - a.y) / len; var uz = (b.x - a.x) / len
            if (ux * (cx - (a.x + b.x) / 2f) + uz * (cz - (a.y + b.y) / 2f) > 0f) { ux = -ux; uz = -uz }
            nx[i] = ux; nz[i] = uz
        }
        return List(n) { i ->
            val a = (i - 1 + n) % n                                  // edge arriving at vertex i
            val ca = nx[a] * poly[i].x + nz[a] * poly[i].y + cm
            val cb = nx[i] * poly[i].x + nz[i] * poly[i].y + cm
            val det = nx[a] * nz[i] - nz[a] * nx[i]
            if (abs(det) < 1e-4f) WallPoint(poly[i].x + nx[i] * cm, poly[i].y + nz[i] * cm)
            else WallPoint((ca * nz[i] - nz[a] * cb) / det, (nx[a] * cb - ca * nx[i]) / det)
        }
    }

    /** Signed area; positive is counter-clockwise in plan coordinates. */
    private fun signedArea(p: List<WallPoint>): Float {
        var a = 0f
        for (i in p.indices) { val u = p[i]; val v = p[(i + 1) % p.size]; a += u.x * v.y - v.x * u.y }
        return a / 2f
    }

    private fun wound(p: List<WallPoint>, ccw: Boolean): List<WallPoint> =
        if ((signedArea(p) > 0f) == ccw) p else p.reversed()

    private fun segmentsCross(a: WallPoint, b: WallPoint, c: WallPoint, d: WallPoint): Boolean {
        fun side(p: WallPoint, q: WallPoint, r: WallPoint) =
            (q.x - p.x) * (r.y - p.y) - (q.y - p.y) * (r.x - p.x)
        val d1 = side(a, b, c); val d2 = side(a, b, d)
        val d3 = side(c, d, a); val d4 = side(c, d, b)
        return ((d1 > 0f) != (d2 > 0f)) && ((d3 > 0f) != (d4 > 0f))
    }

    /**
     * Joins [hole] into [outer] with a two-sided bridge, producing one simple ring the ear clipper
     * can triangulate. The bridge runs from the hole's rightmost corner to the nearest outer corner
     * it can reach without crossing an edge.
     */
    private fun bridgeHole(outer: List<WallPoint>, hole: List<WallPoint>): List<WallPoint> {
        if (hole.size < 3 || outer.size < 3) return outer
        val out = wound(outer, ccw = true)
        val hol = wound(hole, ccw = false)
        val mi = hol.indices.maxByOrNull { hol[it].x } ?: return out
        val m = hol[mi]

        fun clear(to: WallPoint, skipOuter: Int): Boolean {
            for (i in out.indices) {
                if (i == skipOuter || (i + 1) % out.size == skipOuter) continue
                if (segmentsCross(m, to, out[i], out[(i + 1) % out.size])) return false
            }
            for (i in hol.indices) {
                if (i == mi || (i + 1) % hol.size == mi) continue
                if (segmentsCross(m, to, hol[i], hol[(i + 1) % hol.size])) return false
            }
            return true
        }

        var best = -1; var bestD = Float.MAX_VALUE
        for (i in out.indices) {
            val d = hypot(out[i].x - m.x, out[i].y - m.y)
            if (d < bestD && clear(out[i], i)) { bestD = d; best = i }
        }
        if (best < 0) return out

        // The bridge is walked in both directions. Repeating M and P exactly leaves two coincident
        // edges, and the ear clipper then finds no ear and gives up half-triangulated — which shows
        // up as whole triangles missing from the slab. Nudging the return pair keeps the ring simple.
        val p = out[best]
        val bx = p.x - m.x; val by = p.y - m.y
        val bl = hypot(bx, by).coerceAtLeast(1e-3f)
        val ex = -by / bl * BRIDGE_NUDGE_CM
        val ey = bx / bl * BRIDGE_NUDGE_CM

        val ring = mutableListOf<WallPoint>()
        for (i in 0..best) ring += out[i]
        for (k in 0 until hol.size) ring += hol[(mi + k) % hol.size]
        ring += WallPoint(m.x + ex, m.y + ey)
        ring += WallPoint(p.x + ex, p.y + ey)
        for (i in best + 1 until out.size) ring += out[i]
        return ring
    }

    private fun triangulate(p: List<WallPoint>): List<Int> {
        val n = p.size
        var area2 = 0f
        for (i in 0 until n) { val a = p[i]; val b = p[(i + 1) % n]; area2 += a.x * b.y - b.x * a.y }
        val idx = if (area2 > 0f) (0 until n).toMutableList() else (n - 1 downTo 0).toMutableList()   // CCW in plan
        val out = mutableListOf<Int>()
        var guard = 0
        while (idx.size > 2 && guard++ < 8 * n + 64) {
            var clipped = false
            for (k in idx.indices) {
                val i0 = idx[(k - 1 + idx.size) % idx.size]; val i1 = idx[k]; val i2 = idx[(k + 1) % idx.size]
                val a = p[i0]; val b = p[i1]; val c = p[i2]
                if ((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) <= 0f) continue      // reflex corner
                if (idx.any { it != i0 && it != i1 && it != i2 && inTriangle(p[it], a, b, c) }) continue
                out += i2; out += i1; out += i0                                                // CW in plan = +Y normal
                idx.removeAt(k); clipped = true; break
            }
            if (!clipped) break
        }
        return out
    }

    private fun inTriangle(p: WallPoint, a: WallPoint, b: WallPoint, c: WallPoint): Boolean {
        val d1 = (p.x - b.x) * (a.y - b.y) - (a.x - b.x) * (p.y - b.y)
        val d2 = (p.x - c.x) * (b.y - c.y) - (b.x - c.x) * (p.y - c.y)
        val d3 = (p.x - a.x) * (c.y - a.y) - (c.x - a.x) * (p.y - a.y)
        val neg = d1 < 0f || d2 < 0f || d3 < 0f
        val pos = d1 > 0f || d2 > 0f || d3 > 0f
        return !(neg && pos)
    }

    /**
     * Places a model at ONE scale factor so it keeps its authored proportions, sized to fit
     * inside [fitW] × [fitH] and centred there. Doors and windows go through this; [place]
     * stretches three axes independently, which squashed every leaf to the opening it sat in.
     */
    private fun placeFitted(
        path: String, fitW: Float, fitH: Float,
        px: Float, py: Float, pz: Float, rotDeg: Float,
        swingDeg: Float = 0f,
        bucket: MutableList<FilamentAsset> = structureAssets,
    ): FilamentAsset? {
        val asset = load(path) ?: return null
        val bb = asset.boundingBox
        val ex = (bb.halfExtent[0] * 2f).coerceAtLeast(1e-4f)
        val ey = (bb.halfExtent[1] * 2f).coerceAtLeast(1e-4f)
        val s = minOf(fitW / ex, fitH / ey)
        // A swung leaf turns about its hinge edge, not its centre, so the hinge stays in the frame.
        var cx = px; var cz = pz
        if (swingDeg != 0f) {
            val half = ex * s / 2f
            val r0 = Math.toRadians(rotDeg.toDouble())
            val r1 = Math.toRadians((rotDeg + swingDeg).toDouble())
            val hingeX = px - cos(r0).toFloat() * half
            val hingeZ = pz + sin(r0).toFloat() * half
            cx = hingeX + cos(r1).toFloat() * half
            cz = hingeZ - sin(r1).toFloat() * half
        }
        applyTransform(asset, s, s, s, bb.center, bb.halfExtent, cx, py, cz, rotDeg + swingDeg, -1)
        scene.addEntities(asset.entities)
        bucket.add(asset)
        return asset
    }

    private fun place(
        path: String, sizeX: Float, sizeY: Float, sizeZ: Float,
        px: Float, py: Float, pz: Float, rotDeg: Float, anchorBottom: Boolean,
        bucket: MutableList<FilamentAsset> = structureAssets,
    ): FilamentAsset? {
        val asset = load(path) ?: return null
        val bb = asset.boundingBox
        val ex = (bb.halfExtent[0] * 2f).coerceAtLeast(1e-4f)
        val ey = (bb.halfExtent[1] * 2f).coerceAtLeast(1e-4f)
        val ez = (bb.halfExtent[2] * 2f).coerceAtLeast(1e-4f)
        applyTransform(asset, sizeX / ex, sizeY / ey, sizeZ / ez, bb.center, bb.halfExtent, px, py, pz, rotDeg, if (anchorBottom) -1 else 0)
        scene.addEntities(asset.entities)
        bucket.add(asset)
        return asset
    }

    private fun applyTransform(
        asset: FilamentAsset, sx: Float, sy: Float, sz: Float,
        center: FloatArray, halfExtent: FloatArray,
        px: Float, py: Float, pz: Float, rotDeg: Float, anchor: Int,   // -1 base, 0 centre, +1 top sits at py
    ) {
        val rad = Math.toRadians(rotDeg.toDouble())
        val c = cos(rad).toFloat(); val s = sin(rad).toFloat()
        val a0 = center[0]
        val a1 = center[1] + anchor * halfExtent[1]
        val a2 = center[2]
        val la0 = sx * c * a0 + sz * s * a2
        val la1 = sy * a1
        val la2 = sx * (-s) * a0 + sz * c * a2
        val m = floatArrayOf(
            sx * c, 0f, sx * (-s), 0f,
            0f, sy, 0f, 0f,
            sz * s, 0f, sz * c, 0f,
            px - la0, py - la1, pz - la2, 1f
        )
        val tm = engine.transformManager
        tm.setTransform(tm.getInstance(asset.root), m)
    }

    /** Stop a flat surface (floor/ground/grid) from casting shadows — only furniture & walls cast. */
    private fun noCast(asset: FilamentAsset) {
        val rm = engine.renderableManager
        asset.entities.forEach { val ri = rm.getInstance(it); if (ri != 0) runCatching { rm.setCastShadows(ri, false) } }
    }

    fun setAutoHideWalls(on: Boolean) { if (on != autoHideWalls) { autoHideWalls = on; dirty = true } }

    /** Nearest room edge to a plan point: (roomIdx, edgeIdx, t) or null if farther than maxDistCm. */
    /** Nearest wall to a point, as (nodeA, nodeB, t along nodeA → nodeB). */
    private fun nearestEdge(px: Float, pz: Float, maxDistCm: Float): Triple<Int, Int, Float>? {
        var best = maxDistCm; var res: Triple<Int, Int, Float>? = null
        planRooms.forEach { room ->
            for (i in room.indices) {
                val nA = room[i]; val nB = room[(i + 1) % room.size]
                val a = planNodes.getOrNull(nA) ?: continue
                val b = planNodes.getOrNull(nB) ?: continue
                val ex = b.x - a.x; val ez = b.y - a.y
                val len = hypot(ex, ez); if (len < 1f) continue
                val t = (((px - a.x) * ex + (pz - a.y) * ez) / (len * len)).coerceIn(0.05f, 0.95f)
                val d = hypot(px - (a.x + ex * t), pz - (a.y + ez * t))
                if (d < best) { best = d; res = Triple(nA, nB, t) }
            }
        }
        return res
    }

    /** Hide walls whose outward normal faces the camera (they'd block the interior) plus anything mounted on them. */
    private fun applyWallVisibility() {
        if (wallHidden.size != wallSegs.size) wallHidden = BooleanArray(wallSegs.size)
        var changed = false
        // Horizontal view direction. Testing the wall against the DIRECTION (not the camera's side of
        // the wall plane) is what keeps the hide correct when the camera sits inside the room footprint
        // — which it does as soon as you zoom in or tilt near top-down.
        val vLen = hypot(fwd[0], fwd[2]).coerceAtLeast(1e-4f)
        val vdx = fwd[0] / vLen; val vdz = fwd[2] / vLen
        wallSegs.forEachIndexed { i, seg ->
            // We look at the wall's outer face → it blocks the interior. The plane test still catches a
            // camera parked outside a wall that is edge-on. Thresholds avoid flicker at grazing angles.
            val facing = seg.nx * vdx + seg.nz * vdz < -0.05f
            val outside = (eX - seg.mx) * seg.nx + (eZ - seg.mz) * seg.nz > 0.05f
            val hide = autoHideWalls && seg.exterior && (facing || outside)
            if (hide != wallHidden[i]) {
                if (hide) scene.removeEntities(seg.entities) else scene.addEntities(seg.entities)
                wallHidden[i] = hide; changed = true
            }
        }
        if (cornerHidden.size != cornerSegs.size) cornerHidden = BooleanArray(cornerSegs.size)
        cornerSegs.forEachIndexed { i, c ->
            val hide = c.segs.any { wallHidden.getOrNull(it) == true }
            if (hide != cornerHidden[i]) {
                if (hide) scene.removeEntities(c.entities) else scene.addEntities(c.entities)
                cornerHidden[i] = hide
            }
        }
        if (!changed && !wallFurnDirty) return
        wallFurnDirty = false
        // Wall-mounted furniture follows the visibility of the edge it sits on.
        furnitureMeta.forEach { (id, f) ->
            val asset = furnitureAssets[id] ?: return@forEach
            val onWall = f.isWallMounted || catalogItem(f.furnitureId)?.mount == MountType.WALL
            val edge = if (onWall) nearestEdge(f.posX, f.posZ, 60f) else null
            val segIdx = if (edge == null) -1 else wallSegs.indexOfFirst { it.isEdge(edge.first, edge.second) }
            val hide = segIdx >= 0 && wallHidden[segIdx]
            if (hide != (id in furnitureHidden)) {
                if (hide) { scene.removeEntities(asset.entities); furnitureHidden.add(id) }
                else { scene.addEntities(asset.entities); furnitureHidden.remove(id) }
            }
        }
    }

    fun setShadows(on: Boolean) {
        if (on == shadowsOn) return
        shadowsOn = on
        runCatching { view.setShadowingEnabled(on) }; dirty = true
    }

    private fun tint(asset: FilamentAsset, hex: String, mul: Float = 1f) {
        val (r0, g0, b0) = hexLinear(hex)
        val r = r0 * mul; val g = g0 * mul; val b = b0 * mul
        val rm = engine.renderableManager
        asset.entities.forEach { e ->
            val ri = rm.getInstance(e)
            if (ri != 0) for (p in 0 until rm.getPrimitiveCount(ri)) {
                runCatching { rm.getMaterialInstanceAt(ri, p).setParameter("baseColorFactor", r, g, b, 1f) }
            }
        }
    }

    /**
     * Authored size of a model in its own units, measured once. The wall cut needs this BEFORE
     * anything is placed, so the hole can be sized to the joinery instead of the other way round.
     */
    private fun extentOf(path: String): FloatArray? {
        modelExtent[path]?.let { return it }
        val asset = load(path) ?: return null
        val bb = asset.boundingBox
        val e = floatArrayOf(
            (bb.halfExtent[0] * 2f).coerceAtLeast(1e-4f),
            (bb.halfExtent[1] * 2f).coerceAtLeast(1e-4f),
            (bb.halfExtent[2] * 2f).coerceAtLeast(1e-4f),
        )
        runCatching { assetLoader.destroyAsset(asset) }
        modelExtent[path] = e
        return e
    }

    private fun load(path: String): FilamentAsset? {
        val buf = modelCache.getOrPut(path) { readAsset(ctx, path) }
        buf.rewind()
        val asset = assetLoader.createAsset(buf) ?: return null
        resourceLoader.loadResources(asset)
        asset.releaseSourceData()
        return asset
    }

    private fun render(t: Long) {
        val sc = swapChain ?: return
        if (!uiHelper.isReadyToRender) return
        val azR = Math.toRadians(azimuth.toDouble()); val elR = Math.toRadians(elevation.toDouble())
        val d = radius / zoom
        eX = centerX + (d * cos(elR) * sin(azR)).toFloat()
        eY = centerY + (d * sin(elR)).toFloat()
        eZ = centerZ + (d * cos(elR) * cos(azR)).toFloat()
        normSet(fwd, centerX - eX, centerY - eY, centerZ - eZ)
        normSet(rgt, -fwd[2], 0f, fwd[0])
        upv[0] = rgt[1] * fwd[2] - rgt[2] * fwd[1]
        upv[1] = rgt[2] * fwd[0] - rgt[0] * fwd[2]
        upv[2] = rgt[0] * fwd[1] - rgt[1] * fwd[0]
        camera.lookAt(eX.toDouble(), eY.toDouble(), eZ.toDouble(),
            centerX.toDouble(), centerY.toDouble(), centerZ.toDouble(), 0.0, 1.0, 0.0)
        applyWallVisibility()
        if (renderer.beginFrame(sc, t)) { renderer.render(view); renderer.endFrame(); dirty = false }
    }

    private fun normSet(o: FloatArray, x: Float, y: Float, z: Float) {
        val l = sqrt(x * x + y * y + z * z).coerceAtLeast(1e-5f); o[0] = x / l; o[1] = y / l; o[2] = z / l
    }

    private fun projectToScreen(wx: Float, wy: Float, wz: Float): FloatArray? {
        val rx = wx - eX; val ry = wy - eY; val rz = wz - eZ
        val cf = rx * fwd[0] + ry * fwd[1] + rz * fwd[2]; if (cf <= 0.01f) return null
        val xc = rx * rgt[0] + ry * rgt[1] + rz * rgt[2]
        val yc = rx * upv[0] + ry * upv[1] + rz * upv[2]
        val th = tan(Math.toRadians(fovV / 2.0)).toFloat(); val aspect = vpW / vpH
        return floatArrayOf((xc / (cf * th * aspect) * 0.5f + 0.5f) * vpW, (1f - (yc / (cf * th) * 0.5f + 0.5f)) * vpH)
    }

    private fun unprojectToFloor(sx: Float, sy: Float): FloatArray? {
        val th = tan(Math.toRadians(fovV / 2.0)).toFloat(); val aspect = vpW / vpH
        val ndcx = (sx / vpW) * 2f - 1f; val ndcy = 1f - (sy / vpH) * 2f
        val dx = fwd[0] + ndcx * th * aspect * rgt[0] + ndcy * th * upv[0]
        val dy = fwd[1] + ndcx * th * aspect * rgt[1] + ndcy * th * upv[1]
        val dz = fwd[2] + ndcx * th * aspect * rgt[2] + ndcy * th * upv[2]
        if (abs(dy) < 1e-4f) return null
        val t = -eY / dy; if (t <= 0f) return null
        return floatArrayOf(eX + t * dx, eZ + t * dz)
    }

    private var lastX = 0f; private var lastY = 0f; private var lastDist = 0f
    private var grabbedId: String? = null; private var moved = 0f
    private fun onTouch(v: android.view.View, e: MotionEvent): Boolean {
        dirty = true
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX = e.x; lastY = e.y; lastDist = 0f; moved = 0f; grabbedId = pickFurniture(e.x, e.y) }
            MotionEvent.ACTION_POINTER_DOWN -> { lastDist = pinchDist(e); grabbedId = null }
            MotionEvent.ACTION_MOVE -> {
                if (e.pointerCount >= 2) {
                    val d = pinchDist(e); if (lastDist > 0f) zoom = (zoom * d / lastDist).coerceIn(MIN_ZOOM, 4f); lastDist = d
                } else {
                    moved += hypot(e.x - lastX, e.y - lastY)
                    val gid = grabbedId
                    if (gid != null) unprojectToFloor(e.x, e.y)?.let {
                        val r = resolveDrag(gid, it[0] / CM + houseCx, it[1] / CM + houseCz)
                        onMove(gid, r[0], r[1])
                    }
                    else { azimuth -= (e.x - lastX) * 0.3f; elevation = (elevation + (e.y - lastY) * 0.3f).coerceIn(5f, 85f) }
                    lastX = e.x; lastY = e.y
                }
            }
            MotionEvent.ACTION_POINTER_UP -> { lastDist = 0f; lastX = e.x; lastY = e.y }
            MotionEvent.ACTION_UP -> {
                val gid = grabbedId
                if (moved < 18f) {
                    // Furniture wins a shared tap; an opening is only picked when nothing sits on it.
                    if (gid == null) pickOpening(e.x, e.y)?.let { onSelectOpening(it) } ?: onSelect(null)
                    else onSelect(gid)
                }
                else if (gid != null) tryDropDoorOnWall(gid)
                grabbedId = null
            }
        }
        return true
    }

    private fun pickOpening(sx: Float, sy: Float): String? {
        var best: String? = null; var bestD = 70f
        openingWorld.forEach { (id, w) ->
            projectToScreen(w[0], w[1], w[2])?.let { p ->
                val d = hypot(p[0] - sx, p[1] - sy); if (d < bestD) { bestD = d; best = id }
            }
        }
        return best
    }

    private fun pickFurniture(sx: Float, sy: Float): String? {
        var best: String? = null; var bestD = 90f
        furnitureWorld.forEach { (id, w) ->
            projectToScreen(w[0], w[1], w[2])?.let { p ->
                val d = hypot(p[0] - sx, p[1] - sy); if (d < bestD) { bestD = d; best = id }
            }
        }
        return best
    }

    private fun pinchDist(e: MotionEvent) = if (e.pointerCount < 2) 0f else hypot(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1))

    private var running = true
    fun pause() { running = false; runCatching { choreographer.removeFrameCallback(frameCallback) } }
    fun resume() {
        if (running) return
        running = true; dirty = true
        choreographer.removeFrameCallback(frameCallback)
        choreographer.postFrameCallback(frameCallback)
    }

    fun destroy() {
        runCatching { choreographer.removeFrameCallback(frameCallback) }
        runCatching { uiHelper.detach() }
        structureAssets.forEach { runCatching { assetLoader.destroyAsset(it) } }
        furnitureAssets.values.forEach { runCatching { assetLoader.destroyAsset(it) } }
        structureAssets.clear(); furnitureAssets.clear(); furnitureWorld.clear()
        destroyMeshes()
        matAssets.values.forEach { runCatching { assetLoader.destroyAsset(it.second) } }; matAssets.clear()
        runCatching { resourceLoader.destroy() }
        runCatching { assetLoader.destroy() }
        runCatching { materialProvider.destroyMaterials() }
        indirectLight?.let { il -> runCatching { engine.destroyIndirectLight(il) } }
        lights.forEach { runCatching { engine.destroyEntity(it) } }
        runCatching { engine.destroyRenderer(renderer) }
        runCatching { engine.destroyView(view) }
        runCatching { engine.destroyScene(scene) }
        runCatching { engine.destroyCameraComponent(cameraEntity) }
        runCatching { engine.destroyEntity(cameraEntity) }
        runCatching { engine.destroy() }
    }
}

private fun hexLinear(hex: String): Triple<Float, Float, Float> {
    val c = runCatching { android.graphics.Color.parseColor(hex) }.getOrDefault(android.graphics.Color.LTGRAY)
    fun lin(v: Int): Float { val s = v / 255f; return if (s <= 0.04045f) s / 12.92f else ((s + 0.055f) / 1.055f).pow(2.4f) }
    return Triple(lin(android.graphics.Color.red(c)), lin(android.graphics.Color.green(c)), lin(android.graphics.Color.blue(c)))
}

private fun readAsset(context: Context, path: String): ByteBuffer {
    val bytes = context.assets.open(path).use { it.readBytes() }
    return ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply { put(bytes); rewind() }
}

/** Filament skybox colours are linear; Compose colours are sRGB. */
private fun srgbToLinear(c: Float): Float =
    if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
