package com.example.racinggame

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), Runnable {
    constructor(context: Context) : this(context, null) {}
    private var thread: Thread? = null
    private var isPlaying = false

    // Paint and screen properties
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val screenWidth = context.resources.displayMetrics.widthPixels
    private val screenHeight = context.resources.displayMetrics.heightPixels

    // Game assets
    private val carBitmap: Bitmap
    private val obstacleBitmap: Bitmap
    private val roadBitmap: Bitmap

    // Car properties
    private var carX = screenWidth / 3f
    private var carY = screenHeight - 500f
    private val carWidth = 150f
    private val carHeight = 150f

    // Obstacles
    private val obstacles = mutableListOf<Pair<Float, Float>>()
    private val obstacleWidth = 150
    private val obstacleHeight = 150

    // Road properties
    private val roadSpeed = 10f
    private var roadOffsetY = 0f

    // Road boundaries
    private val roadLeft = (screenWidth * 0.2).toInt()
    private val roadRight = (screenWidth * 0.8).toInt()

    // Score
    private var score = 0;
    private var onUpdateScoreListener : ((Int) -> Unit)? = null;

    // Sound Effects
    private var carRunningPlayer: MediaPlayer? = null;
    private var carCrashPlayer: MediaPlayer? = null

    // GameOver
    private var onGameOverListener: (()->Unit)? = null;

    init {
        carBitmap = rotateImage(loadBitmapWithTransparency(R.drawable.konnigseg, carWidth.toInt(), carHeight.toInt()), 180f)
        obstacleBitmap = loadBitmapWithTransparency(R.drawable.obstacle_1, obstacleWidth, obstacleHeight)
        roadBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.road), screenWidth, screenHeight, true)

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                resume()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                pause()
            }
        })
    }
    init {
        initCarRunningSound();
        initCarCrashSound();
    }
    private fun initCarRunningSound(){
        carRunningPlayer = MediaPlayer.create(context, R.raw.car_acceleration);
        carRunningPlayer?.apply {
            isLooping = true;
            setOnPreparedListener{
                seekTo(5000);
            }
        }
    }
    private fun playCarRunningSound() {
        carRunningPlayer?.start()
    }
    private fun stopCarRunningSound(){
        carRunningPlayer?.stop();
        carRunningPlayer?.seekTo(0);
    }
    private fun initCarCrashSound(){
        carCrashPlayer = MediaPlayer.create(context, R.raw.crash_effect);
    }
    private fun playCarCrashSound(){
        carCrashPlayer?.start();
    }
    fun releaseSounds() {
        carRunningPlayer?.release()
        carRunningPlayer = null

        carCrashPlayer?.release()
        carCrashPlayer = null
    }

    fun setOnGameOverListener(listener: (() -> Unit)?){
        onGameOverListener = listener;
    }
    fun setOnUpdateScoreListener(listener: ((Int) -> Unit)?){
        onUpdateScoreListener = listener;
    }
    override fun run() {
        while (isPlaying) {
            update()
            draw()
            sleep()
        }
    }
    fun restartGame(){
        score = 0;
        isPlaying = true;
        synchronized(obstacles){
            obstacles.clear();
        }
        thread = Thread(this);
        thread?.start()
    }

    private fun update() {
        // Scroll the road
        roadOffsetY += roadSpeed
        if (roadOffsetY > screenHeight) roadOffsetY = 0f

        // Car hitbox
        val carRect = Rect(
            carX.toInt() + 20,
            carY.toInt(),
            (carX + carWidth).toInt() - 20,
            (carY + carHeight).toInt()
        )

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()

            // Update obstacle position
            val updatedObstacle = Pair(obstacle.first, obstacle.second + roadSpeed)

            // Obstacle hitbox
            val obstacleRect = Rect(
                updatedObstacle.first.toInt() + 30,
                updatedObstacle.second.toInt(),
                (updatedObstacle.first + obstacleWidth).toInt() - 25,
                (updatedObstacle.second + obstacleHeight).toInt()
            )

            // Check for collision
            if (Rect.intersects(carRect, obstacleRect)) {
                isPlaying = false
                onGameOverListener?.invoke();
                stopCarRunningSound();
                playCarCrashSound();
                break
            }

            // Check if obstacle is off-screen
            if (updatedObstacle.second > screenHeight) {
                score++;
                onUpdateScoreListener?.invoke(score);
                iterator.remove()
            } else {
                // Update obstacle in the list
                obstacles[obstacles.indexOf(obstacle)] = updatedObstacle
            }
        }

        // Add new obstacles if needed
        if (obstacles.isEmpty() || obstacles.last().second > 200) {
            val obstacleX = (roadLeft..(roadRight - obstacleWidth)).random().toFloat()
            obstacles.add(Pair(obstacleX, -obstacleHeight.toFloat()))
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
            // Draw score, road, car, and obstacles
            drawRoad(canvas)
            drawCar(canvas)
            paint.style = Paint.Style.STROKE
            val carRect = Rect(carX.toInt()+20, carY.toInt(), (carX + carWidth).toInt()-20, (carY + carHeight).toInt())
            paint.color = Color.BLUE
            canvas.drawRect(carRect, paint)
            drawObstacles(canvas)
            paint.color = Color.GREEN
            for (obstacle in obstacles) {
                val obstacleRect = Rect(
                    obstacle.first.toInt() +30,
                    obstacle.second.toInt(),
                    (obstacle.first + obstacleWidth -25).toInt(),
                    (obstacle.second + obstacleHeight).toInt()
                )
                canvas.drawRect(obstacleRect, paint)
            }
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawRoad(canvas: Canvas) {
        canvas.drawBitmap(roadBitmap, 0f, roadOffsetY - screenHeight, null)
        canvas.drawBitmap(roadBitmap, 0f, roadOffsetY, null)
    }

    private fun drawCar(canvas: Canvas) {
        canvas.drawBitmap(carBitmap, carX, carY, null)
    }

    private fun drawObstacles(canvas: Canvas) {
        synchronized(obstacles) {
            for (obstacle in obstacles) {
                canvas.drawBitmap(obstacleBitmap, obstacle.first, obstacle.second, null)
            }
        }
    }

    private fun loadBitmapWithTransparency(resourceId: Int, width: Int, height: Int): Bitmap {
        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true)
    }


    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun sleep() {
        Thread.sleep(16) // ~60 FPS
    }

    fun resume() {
        isPlaying = true
        thread = Thread(this)
        playCarRunningSound();
        thread?.start()
    }

    fun pause() {
        isPlaying = false
        stopCarRunningSound();
        thread?.join()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_MOVE -> {
                    carX = it.x - carWidth / 2
                    carX = carX.coerceIn(roadLeft.toFloat(), roadRight.toFloat() - carWidth)
                }
            }
        }
        return true
    }
}
