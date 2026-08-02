package com.interiordesign3d.ui.screens

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
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Utils
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.OpeningType
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.models.WallOpening
import com.interiordesign3d.data.models.WallPoint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

private val filamentReady: Boolean by lazy { Utils.init(); true }

private const val CM = 0.01f
private const val WALL_THICK_CM = 10f

@Composable
fun FilamentRoomViewport(
    floorPlan: FloorPlan,
    roomPolygons: List<List<WallPoint>>,
    placedFurniture: List<PlacedFurniture>,
    roomHeight: Float,
    wallModel: String = "wall",
    wallColorHex: String = "#EFEAE3",
    floorModel: String = "floorFull",
    floorColorHex: String = "#C9A877",
    shadows: Boolean = false,
    onSelectFurniture: (String?) -> Unit = {},
    onMoveFurniture: (String, Float, Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    if (!filamentReady) return
    val sceneRef = remember { mutableStateOf<RoomScene?>(null) }
    val onSelect = rememberUpdatedState(onSelectFurniture)
    val onMove = rememberUpdatedState(onMoveFurniture)

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
            sceneRef.value = RoomScene(ctx, sv, { onSelect.value(it) }, { id, x, z -> onMove.value(id, x, z) })
            sv
        },
        update = {
            sceneRef.value?.let { s ->
                s.setShadows(shadows)
                s.update(roomPolygons, floorPlan.openings, placedFurniture, roomHeight,
                    wallModel, wallColorHex, floorModel, floorColorHex)
            }
        },
        onRelease = { sceneRef.value?.destroy(); sceneRef.value = null }
    )
}

private class RoomScene(
    context: Context,
    private val surfaceView: SurfaceView,
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
    private val furnitureAssets = LinkedHashMap<String, FilamentAsset>()
    private val furnitureWorld = LinkedHashMap<String, FloatArray>()
    private val lights = mutableListOf<Int>()
    private var sunEntity = 0
    private var shadowsOn = false
    private var structSig = ""
    private var furnSig = ""
    private var polys: List<List<WallPoint>> = emptyList()

    private var houseCx = 0f; private var houseCz = 0f

    private var azimuth = 35f; private var elevation = 28f; private var zoom = 1f
    private var centerX = 0f; private var centerY = 0.8f; private var centerZ = 0f
    private var radius = 6f
    private var eX = 0f; private var eY = 0f; private var eZ = 0f
    private val fwd = FloatArray(3); private val rgt = FloatArray(3); private val upv = FloatArray(3)
    private var vpW = 1f; private var vpH = 1f
    private val fovV = 50.0

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(t: Long) { choreographer.postFrameCallback(this); render(t) }
    }

    init {
        view.scene = scene
        view.camera = camera
        camera.setExposure(16f, 1f / 125f, 100f)
        scene.skybox = Skybox.Builder().color(0.80f, 0.80f, 0.82f, 1.0f).build(engine)
        // Perf: models are flat/unlit — drop the shadow pass, MSAA and dithering.
        view.setShadowingEnabled(false)
        view.setAntiAliasing(com.google.android.filament.View.AntiAliasing.NONE)
        view.setDithering(com.google.android.filament.View.Dithering.NONE)
        addLights()
        uiHelper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface)
                displayHelper.attach(renderer, surfaceView.display)
            }
            override fun onDetachedFromSurface() {
                displayHelper.detach()
                swapChain?.let { engine.destroySwapChain(it); engine.flushAndWait(); swapChain = null }
            }
            override fun onResized(w: Int, h: Int) {
                vpW = w.toFloat(); vpH = h.toFloat()
                camera.setProjection(fovV, w.toDouble() / h.toDouble(), 0.05, 1000.0, Camera.Fov.VERTICAL)
                view.viewport = Viewport(0, 0, w, h)
            }
        }
        uiHelper.attachTo(surfaceView)
        surfaceView.setOnTouchListener(::onTouch)
        choreographer.postFrameCallback(frameCallback)
    }

    private fun addLights() {
        indirectLight = IndirectLight.Builder()
            .irradiance(1, floatArrayOf(0.7f, 0.7f, 0.72f)).intensity(50_000f).build(engine)
        scene.indirectLight = indirectLight
        val sun = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1f, 0.98f, 0.95f).intensity(80_000f)
            .direction(0.4f, -1f, -0.5f).castShadows(true).build(engine, sun)
        scene.addEntity(sun); lights.add(sun); sunEntity = sun
        val fill = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.9f, 0.92f, 1f).intensity(30_000f)
            .direction(-0.5f, -0.7f, 0.5f).castShadows(false).build(engine, fill)
        scene.addEntity(fill); lights.add(fill)
    }

    fun update(
        roomPolygons: List<List<WallPoint>>, openings: List<WallOpening>,
        furniture: List<PlacedFurniture>, roomHeightCm: Float,
        wallModel: String, wallColorHex: String, floorModel: String, floorColorHex: String,
    ) {
        val allPts = roomPolygons.flatten()
        if (allPts.isEmpty()) return
        polys = roomPolygons
        val minX = allPts.minOf { it.x }; val maxX = allPts.maxOf { it.x }
        val minZ = allPts.minOf { it.y }; val maxZ = allPts.maxOf { it.y }
        houseCx = (minX + maxX) / 2f; houseCz = (minZ + maxZ) / 2f
        val spanX = (maxX - minX).coerceAtLeast(1f); val spanZ = (maxZ - minZ).coerceAtLeast(1f)

        val sSig = roomPolygons.joinToString(";") { p -> p.joinToString(",") { "${it.x.toInt()}/${it.y.toInt()}" } } +
                "|${roomHeightCm.toInt()}|$wallModel|$wallColorHex|$floorModel|$floorColorHex" +
                "|" + openings.joinToString(",") { "${it.roomIdx}/${it.edgeIdx}/${it.t}/${it.type}/${it.widthCm}" }
        if (sSig != structSig) {
            structSig = sSig
            rebuildStructure(roomPolygons, openings, roomHeightCm, spanX, spanZ, wallModel, wallColorHex, floorModel, floorColorHex)
            // frame the room only when its geometry changes (keeps user's orbit otherwise)
            centerX = 0f; centerZ = 0f; centerY = roomHeightCm * CM * 0.35f
            radius = maxOf(spanX, spanZ) * CM * 1.5f + roomHeightCm * CM
        }

        val fSig = furniture.joinToString(";") { "${it.id}:${it.furnitureId}:${it.isWallMounted}:${it.colorOverride}" }
        if (fSig != furnSig) {
            furnSig = fSig
            furnitureAssets.values.forEach { runCatching { assetLoader.destroyAsset(it) } }
            furnitureAssets.clear(); furnitureWorld.clear()
            furniture.forEach { addFurniture(it) }
        } else {
            furniture.forEach { f -> furnitureAssets[f.id]?.let { transformFurniture(it, f) } }
        }
    }

    private fun rebuildStructure(
        roomPolygons: List<List<WallPoint>>, openings: List<WallOpening>,
        roomHeightCm: Float, spanX: Float, spanZ: Float,
        wallModel: String, wallColorHex: String, floorModel: String, floorColorHex: String,
    ) {
        structureAssets.forEach { runCatching { assetLoader.destroyAsset(it) } }
        structureAssets.clear()
        fun wx(cm: Float) = (cm - houseCx) * CM
        fun wz(cm: Float) = (cm - houseCz) * CM
        val wt = WALL_THICK_CM * CM
        val hM = roomHeightCm * CM

        // Room floor — opaque, recoloured. Sits above the grid (y top = 0) so it hides the grid
        // inside the room. Extended by the wall thickness so it reaches under the walls.
        place("models/$floorModel.glb", spanX * CM + wt * 2f, 0.05f, spanZ * CM + wt * 2f, 0f, -0.025f, 0f, 0f, false)
            ?.let { tint(it, floorColorHex); noCast(it) }

        // Walls — split each edge around doors/windows. Solid parts use the chosen wall model;
        // openings use Kenney's wallDoorway / wallWindow tile (so you see the hole). Solid panels
        // at a true room corner are extended by the thickness so adjacent walls overlap (no gap).
        roomPolygons.forEachIndexed { roomIdx, poly ->
            val cx = poly.map { it.x }.average().toFloat(); val cz = poly.map { it.y }.average().toFloat()
            for (i in poly.indices) {
                val a = poly[i]; val b = poly[(i + 1) % poly.size]
                val dx = b.x - a.x; val dz = b.y - a.y
                val lenCm = sqrt(dx * dx + dz * dz); if (lenCm < 1f) continue
                val ux = dx / lenCm; val uz = dz / lenCm
                var nx = -dz / lenCm; var nz = dx / lenCm
                if (nx * (cx - (a.x + b.x) / 2f) + nz * (cz - (a.y + b.y) / 2f) > 0f) { nx = -nx; nz = -nz }
                val rotDeg = Math.toDegrees(atan2(-dz.toDouble(), dx.toDouble())).toFloat()

                fun panel(t0: Float, t1: Float, model: String, extendEnds: Boolean) {
                    var s = t0 * lenCm; var e = t1 * lenCm
                    if (extendEnds && t0 <= 1e-3f) s -= WALL_THICK_CM / 2f
                    if (extendEnds && t1 >= 1f - 1e-3f) e += WALL_THICK_CM / 2f
                    val segLen = e - s; if (segLen < 1f) return
                    val mid = (s + e) / 2f
                    val mxC = a.x + ux * mid; val mzC = a.y + uz * mid
                    val midX = mxC + nx * (WALL_THICK_CM / 2f); val midZ = mzC + nz * (WALL_THICK_CM / 2f)
                    place("models/$model.glb", segLen * CM, hM, wt, wx(midX), hM / 2f, wz(midZ), rotDeg, false)
                        ?.let { tint(it, wallColorHex) }
                }

                val edgeOpenings = openings.filter { it.roomIdx == roomIdx && it.edgeIdx == i }.sortedBy { it.t }
                if (edgeOpenings.isEmpty()) {
                    panel(0f, 1f, wallModel, true)
                } else {
                    var tPrev = 0f
                    for (op in edgeOpenings) {
                        val halfT = (op.widthCm / 2f) / lenCm
                        val tS = (op.t - halfT).coerceIn(0f, 1f); val tE = (op.t + halfT).coerceIn(0f, 1f)
                        if (tS > tPrev + 1e-3f) panel(tPrev, tS, wallModel, true)
                        panel(tS, tE, if (op.type == OpeningType.DOOR) "wallDoorway" else "wallWindow", false)
                        tPrev = tE
                    }
                    if (tPrev < 1f - 1e-3f) panel(tPrev, 1f, wallModel, true)
                }
            }
        }
    }

    private fun addFurniture(f: PlacedFurniture) {
        val asset = load("models/${f.furnitureId}.glb") ?: load("models/chair.glb") ?: return
        furnitureAssets[f.id] = asset
        f.colorOverride?.let { tint(asset, it) }
        transformFurniture(asset, f)
        scene.addEntities(asset.entities)
    }

    private fun transformFurniture(asset: FilamentAsset, f: PlacedFurniture) {
        val bb = asset.boundingBox
        val foot = (catalogItem(f.furnitureId)?.footprintM ?: 0.6f) * f.scale
        val widest = maxOf(bb.halfExtent[0], bb.halfExtent[2]) * 2f
        val s = if (widest > 1e-4f) foot / widest else 1f

        if (f.isWallMounted) {
            val wall = findNearestWall(f.posX, f.posZ, polys)
            if (wall != null) {
                val inX = wall.normalX; val inZ = wall.normalZ          // inward
                val px = (wall.snappedX - houseCx) * CM + inX * 0.06f
                val pz = (wall.snappedZ - houseCz) * CM + inZ * 0.06f
                val rotDeg = Math.toDegrees(atan2(-wall.tangentZ.toDouble(), wall.tangentX.toDouble())).toFloat()
                applyTransform(asset, s, s, s, bb.center, bb.halfExtent, px, f.wallMountHeight * CM, pz, rotDeg, false)
                furnitureWorld[f.id] = floatArrayOf(px, pz)
                return
            }
        }
        val wx = (f.posX - houseCx) * CM; val wz = (f.posZ - houseCz) * CM
        applyTransform(asset, s, s, s, bb.center, bb.halfExtent, wx, 0f, wz, f.rotationY, true)
        furnitureWorld[f.id] = floatArrayOf(wx, wz)
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
        applyTransform(asset, sizeX / ex, sizeY / ey, sizeZ / ez, bb.center, bb.halfExtent, px, py, pz, rotDeg, anchorBottom)
        scene.addEntities(asset.entities)
        bucket.add(asset)
        return asset
    }

    private fun applyTransform(
        asset: FilamentAsset, sx: Float, sy: Float, sz: Float,
        center: FloatArray, halfExtent: FloatArray,
        px: Float, py: Float, pz: Float, rotDeg: Float, anchorBottom: Boolean,
    ) {
        val rad = Math.toRadians(rotDeg.toDouble())
        val c = cos(rad).toFloat(); val s = sin(rad).toFloat()
        val a0 = center[0]
        val a1 = if (anchorBottom) center[1] - halfExtent[1] else center[1]
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

    fun setShadows(on: Boolean) {
        if (on == shadowsOn) return
        shadowsOn = on
        runCatching { view.setShadowingEnabled(on) }
    }

    private fun tint(asset: FilamentAsset, hex: String) {
        val (r, g, b) = hexLinear(hex)
        val rm = engine.renderableManager
        asset.entities.forEach { e ->
            val ri = rm.getInstance(e)
            if (ri != 0) for (p in 0 until rm.getPrimitiveCount(ri)) {
                runCatching { rm.getMaterialInstanceAt(ri, p).setParameter("baseColorFactor", r, g, b, 1f) }
            }
        }
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
        if (renderer.beginFrame(sc, t)) { renderer.render(view); renderer.endFrame() }
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
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX = e.x; lastY = e.y; lastDist = 0f; moved = 0f; grabbedId = pickFurniture(e.x, e.y) }
            MotionEvent.ACTION_POINTER_DOWN -> { lastDist = pinchDist(e); grabbedId = null }
            MotionEvent.ACTION_MOVE -> {
                if (e.pointerCount >= 2) {
                    val d = pinchDist(e); if (lastDist > 0f) zoom = (zoom * d / lastDist).coerceIn(0.4f, 4f); lastDist = d
                } else {
                    moved += hypot(e.x - lastX, e.y - lastY)
                    val gid = grabbedId
                    if (gid != null) unprojectToFloor(e.x, e.y)?.let { onMove(gid, it[0] / CM + houseCx, it[1] / CM + houseCz) }
                    else { azimuth += (e.x - lastX) * 0.3f; elevation = (elevation - (e.y - lastY) * 0.3f).coerceIn(5f, 85f) }
                    lastX = e.x; lastY = e.y
                }
            }
            MotionEvent.ACTION_POINTER_UP -> { lastDist = 0f; lastX = e.x; lastY = e.y }
            MotionEvent.ACTION_UP -> { if (moved < 18f) onSelect(grabbedId); grabbedId = null }
        }
        return true
    }

    private fun pickFurniture(sx: Float, sy: Float): String? {
        var best: String? = null; var bestD = 90f
        furnitureWorld.forEach { (id, w) ->
            projectToScreen(w[0], 0.3f, w[1])?.let { p ->
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
        running = true
        choreographer.removeFrameCallback(frameCallback)
        choreographer.postFrameCallback(frameCallback)
    }

    fun destroy() {
        runCatching { choreographer.removeFrameCallback(frameCallback) }
        runCatching { uiHelper.detach() }
        structureAssets.forEach { runCatching { assetLoader.destroyAsset(it) } }
        furnitureAssets.values.forEach { runCatching { assetLoader.destroyAsset(it) } }
        structureAssets.clear(); furnitureAssets.clear(); furnitureWorld.clear()
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
