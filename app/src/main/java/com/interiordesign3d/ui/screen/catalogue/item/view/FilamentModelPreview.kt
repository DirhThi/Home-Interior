package com.interiordesign3d.ui.screen.catalogue.item.view

import android.content.Context
import android.view.Choreographer
import android.view.SurfaceView
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** Loads the native libraries. Must be touched before the first `Engine.create()`. */
private val filamentReady: Boolean by lazy { Utils.init(); true }

/**
 * One model on a turntable.
 *
 * Deliberately not `FilamentRoomViewport`: that one exists to build walls, floors and roofs from a
 * plan, and none of it applies to a single glTF sitting on nothing.
 */
@Composable
fun FilamentModelPreview(
    modelPath: String,
    background: Color,
    modifier: Modifier = Modifier,
) {
    val argb = background.toArgb()
    val sceneRef = remember { mutableListOf<PreviewScene>() }

    AndroidView(
        modifier = modifier.pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                sceneRef.firstOrNull()?.orbit(pan.x, pan.y, zoom)
            }
        },
        factory = { ctx ->
            SurfaceView(ctx).also { sv -> sceneRef.add(PreviewScene(ctx, sv, argb, modelPath)) }
        },
        onRelease = {
            sceneRef.forEach { it.destroy() }
            sceneRef.clear()
        },
    )

    DisposableEffect(modelPath) {
        onDispose { }
    }
}

private const val FOV_DEG = 45.0
private const val WARMUP_FRAMES = 6

private class PreviewScene(
    context: Context,
    private val surfaceView: SurfaceView,
    backgroundColor: Int,
    modelPath: String,
) {
    // Declared first on purpose: field initializers run in order, and Engine.create() needs the
    // native libraries already loaded.
    private val ready = filamentReady
    private val engine = Engine.create()
    private val renderer = engine.createRenderer()
    private val scene = engine.createScene()
    private val view = engine.createView()
    private val cameraEntity = EntityManager.get().create()
    private val camera: Camera = engine.createCamera(cameraEntity)
    private val displayHelper = DisplayHelper(context)
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private val choreographer = Choreographer.getInstance()

    private val materialProvider = UbershaderProvider(engine)
    private val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
    private val resourceLoader = ResourceLoader(engine)

    private var swapChain: SwapChain? = null
    private var asset: FilamentAsset? = null
    private var indirectLight: IndirectLight? = null
    private val lights = mutableListOf<Int>()

    private var azimuth = 35f
    private var elevation = 18f
    private var zoom = 1f
    private var radius = 2f
    private var cx = 0f
    private var cy = 0f
    private var cz = 0f
    // A single render right after load/resize can beat the driver's first-use shader compile for this
    // material and show a black frame that then never gets redrawn; keep rendering for a few frames
    // instead of trusting the first one.
    private var dirtyFrames = WARMUP_FRAMES

    private val frame = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            if (dirtyFrames <= 0) return
            render()
            dirtyFrames--
        }
    }

    init {
        view.scene = scene
        view.camera = camera
        camera.setExposure(16f, 1f / 125f, 100f)

        val r = ((backgroundColor shr 16) and 0xFF) / 255f
        val g = ((backgroundColor shr 8) and 0xFF) / 255f
        val b = (backgroundColor and 0xFF) / 255f
        scene.skybox = Skybox.Builder().color(r, g, b, 1f).build(engine)

        addLights()
        loadModel(context, modelPath)

        uiHelper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: android.view.Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface)
                displayHelper.attach(renderer, surfaceView.display)
                dirtyFrames = WARMUP_FRAMES
            }

            override fun onDetachedFromSurface() {
                displayHelper.detach()
                swapChain?.let { engine.destroySwapChain(it); engine.flushAndWait(); swapChain = null }
            }

            override fun onResized(width: Int, height: Int) {
                view.viewport = Viewport(0, 0, width, height)
                camera.setProjection(FOV_DEG, width.toDouble() / height, 0.05, 100.0, Camera.Fov.VERTICAL)
                dirtyFrames = WARMUP_FRAMES
            }
        }
        uiHelper.attachTo(surfaceView)
        choreographer.postFrameCallback(frame)
    }

    fun orbit(panX: Float, panY: Float, scale: Float) {
        azimuth -= panX * 0.4f
        elevation = (elevation + panY * 0.3f).coerceIn(-80f, 80f)
        zoom = (zoom * scale).coerceIn(0.5f, 4f)
        dirtyFrames = WARMUP_FRAMES
    }

    private fun addLights() {
        indirectLight = IndirectLight.Builder()
            .irradiance(1, floatArrayOf(0.8f, 0.8f, 0.82f)).intensity(30_000f).build(engine)
        scene.indirectLight = indirectLight

        val sun = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1f, 0.97f, 0.92f).intensity(90_000f)
            .direction(0.6f, -0.8f, -0.4f).castShadows(false)
            .build(engine, sun)
        scene.addEntity(sun); lights += sun

        val fill = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.9f, 0.92f, 1f).intensity(22_000f)
            .direction(-0.5f, -0.6f, 0.6f).castShadows(false).build(engine, fill)
        scene.addEntity(fill); lights += fill
    }

    /** Frames whatever the model's own bounds turn out to be — the packs are not authored to a scale. */
    private fun loadModel(context: Context, path: String) {
        val bytes = runCatching { context.assets.open(path).use { it.readBytes() } }.getOrNull() ?: return
        val buf = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
            .apply { put(bytes); rewind() }
        val loaded = assetLoader.createAsset(buf) ?: return
        resourceLoader.loadResources(loaded)
        loaded.releaseSourceData()
        scene.addEntities(loaded.entities)
        asset = loaded

        val box = loaded.boundingBox
        cx = box.center[0]; cy = box.center[1]; cz = box.center[2]
        // Fit from the camera's own vertical field of view rather than a guessed multiplier — a tall
        // chair and a wide sofa need very different distances.
        val h = box.halfExtent
        val extent = maxOf(h[0], h[1], h[2]).coerceAtLeast(0.05f)
        radius = extent / tan(Math.toRadians(FOV_DEG / 2.0)).toFloat() * 1.8f
        dirtyFrames = WARMUP_FRAMES
    }

    private fun render() {
        val sc = swapChain ?: return
        if (!uiHelper.isReadyToRender) return
        val azR = Math.toRadians(azimuth.toDouble())
        val elR = Math.toRadians(elevation.toDouble())
        val d = radius / zoom
        val eX = cx + (d * cos(elR) * sin(azR)).toFloat()
        val eY = cy + (d * sin(elR)).toFloat()
        val eZ = cz + (d * cos(elR) * cos(azR)).toFloat()
        camera.lookAt(eX.toDouble(), eY.toDouble(), eZ.toDouble(),
            cx.toDouble(), cy.toDouble(), cz.toDouble(), 0.0, 1.0, 0.0)
        if (renderer.beginFrame(sc, System.nanoTime())) {
            renderer.render(view)
            renderer.endFrame()
        }
    }

    fun destroy() {
        choreographer.removeFrameCallback(frame)
        uiHelper.detach()
        asset?.let { assetLoader.destroyAsset(it) }
        indirectLight?.let { engine.destroyIndirectLight(it) }
        lights.forEach { scene.removeEntity(it); engine.destroyEntity(it) }
        scene.skybox?.let { engine.destroySkybox(it) }
        swapChain?.let { engine.destroySwapChain(it) }
        engine.destroyRenderer(renderer)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyCameraComponent(cameraEntity)
        EntityManager.get().destroy(cameraEntity)
        resourceLoader.destroy()
        assetLoader.destroy()
        materialProvider.destroyMaterials()
        engine.destroy()
    }
}
