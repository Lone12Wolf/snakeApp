package com.example1.ashu.snakeapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

/**
 * Created by om on 12/31/2017.
 */

public class SnakeView extends SurfaceView implements Runnable {


    private Thread m_Thread = null;
    private volatile  boolean m_Playing;
    private Canvas m_Canvas;
    private SurfaceHolder m_Holder;
    private Paint m_Paint;
    private Context m_context;
    private SoundPool m_SoundPool;
    private int m_get_mouse_sound = -1;
    private int m_dead_sound = -1;
    public enum Direction{UP,RIGHT,DOWN,LEFT};
    private Direction m_direction = Direction.RIGHT;
    private int m_ScreenWidth;
    private int m_ScreenHeight;
    private int m_Score;
    private long m_NextFrameTime;
    private final long FPS = 10;
    private final long MILLIS_IN_A_SECOND = 1000;
    private int[] m_SnakeKeXs;
    private int[] m_SnakeKeYs;
    private int m_SnakeLength;
    private int m_MouseX;
    private int m_MouseY;
    private int m_BlockSize;
    private final int NUM_BLOCKS_WIDE = 40;
    private int m_Num_Blocks_High;

    public SnakeView(Context context, Point size) {
        super(context);

        m_context = context;

        m_ScreenWidth = size.x;
        m_ScreenHeight = size.y;

        m_BlockSize = m_ScreenWidth / NUM_BLOCKS_WIDE;
        m_Num_Blocks_High = m_ScreenHeight / m_BlockSize ;

        loadSound();

        m_Holder = getHolder();
        m_Paint = new Paint();
        m_SnakeKeXs = new int[200];
        m_SnakeKeYs = new int[200];

        startGame();
    }

    @Override
    public void run() {
        while (m_Playing) {
            if (checkForUpdate()) {
                updateGame();
                drawGame();
            }
        }
    }

    public void pause()
    {
        m_Playing = false;
        try {
            m_Thread.join();

        }catch (InterruptedException e){
        }

    }


    public void resume()
    {
        m_Playing = true;
        m_Thread = new Thread(this);
        m_Thread.start();

    }

    public void startGame()
    {
        m_SnakeLength = 1;
        m_SnakeKeXs[0] = NUM_BLOCKS_WIDE / 2;
        m_SnakeKeYs[0] = m_Num_Blocks_High / 2;

        spawnMouse();

        m_Score = 0 ;
        m_NextFrameTime = System.currentTimeMillis();
    }

    public void loadSound()
    {
        m_SoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try {
            AssetManager assetManager = m_context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("get_mouse_sound.ogg");
            m_get_mouse_sound = m_SoundPool.load(descriptor,0);

            descriptor = assetManager.openFd("death_sound.ogg");
            m_get_mouse_sound = m_SoundPool.load(descriptor,0);

        } catch (IOException e)
        {

        }
    }


    public void spawnMouse()
    {
        Random random = new Random();

        m_MouseX = random.nextInt(NUM_BLOCKS_WIDE - 1)+1;
        m_MouseY = random.nextInt(m_Num_Blocks_High - 1)+1;
    }

    private void eatMouse()
    {
        m_SnakeLength++;
        spawnMouse();

        m_Score = m_Score + 1;
        m_SoundPool.play(m_get_mouse_sound,1,1,0,0,1);
    }

    private void moveSnake()
    {
        for ( int i = m_SnakeLength ; i > 0 ; i++)
        {
            m_SnakeKeXs[i] = m_SnakeKeXs[i-1];
            m_SnakeKeYs[i] = m_SnakeKeYs[i-1];
        }

        switch (m_direction)
        {
            case UP:
                m_SnakeKeYs[0]--;
                break;
            case RIGHT:
                m_SnakeKeXs[0]++;
                break;
            case DOWN:
                m_SnakeKeYs[0]++;
                break;
            case LEFT:
                m_SnakeKeXs[0]--;
                break;
        }
    }


    private boolean detectDeath()
    {
        boolean dead = false;

        if(m_SnakeKeXs[0] == -1) dead = true;
        if(m_SnakeKeXs[0] >= NUM_BLOCKS_WIDE) dead = true;

        if(m_SnakeKeYs[0] == -1) dead = true;
        if(m_SnakeKeYs[0] >= m_Num_Blocks_High) dead = true;

        for( int i = m_SnakeLength - 1 ; i > 0 ; i-- )
        {
            if(( i > 4)&&(m_SnakeKeXs[0] == m_SnakeKeXs[i] && (m_SnakeKeYs[0] == m_SnakeKeYs[i])))
            {
                dead = true;
            }
        }
        return  dead;
    }



    public void updateGame()
    {
        if(m_SnakeKeXs[0] == m_MouseX && m_SnakeKeYs[0] == m_MouseY)
        {
            eatMouse();
        }

        moveSnake();

        if(detectDeath())
        {
            m_SoundPool.play(m_dead_sound,1,1,0,0,1);
            startGame();
        }
    }

    public void drawGame() {
        // Prepare to draw
        if (m_Holder.getSurface().isValid()) {
            m_Canvas = m_Holder.lockCanvas();

            // Clear the screen with my favorite color
            m_Canvas.drawColor(Color.argb(255, 120, 197, 87));

            // Set the color of the paint to draw the snake and mouse with
            m_Paint.setColor(Color.argb(255, 255, 255, 255));

            // Choose how big the score will be
            m_Paint.setTextSize(30);
            m_Canvas.drawText("Score:" + m_Score, 10, 30, m_Paint);

            //Draw the snake
            for (int i = 0; i < m_SnakeLength; i++) {
                m_Canvas.drawRect(m_SnakeKeXs[i] * m_BlockSize,
                        (m_SnakeKeYs[i] * m_BlockSize),
                        (m_SnakeKeXs[i] * m_BlockSize) + m_BlockSize,
                        (m_SnakeKeYs[i] * m_BlockSize) + m_BlockSize,
                        m_Paint);
            }

            //draw the mouse
            m_Canvas.drawRect(m_MouseX * m_BlockSize,
                    (m_MouseY * m_BlockSize),
                    (m_MouseX * m_BlockSize) + m_BlockSize,
                    (m_MouseY * m_BlockSize) + m_BlockSize,
                    m_Paint);

            // Draw the whole frame
            m_Holder.unlockCanvasAndPost(m_Canvas);
        }
    }

    public boolean checkForUpdate() {

        // Are we due to update the frame
        if(m_NextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            m_NextFrameTime =System.currentTimeMillis() + MILLIS_IN_A_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= m_ScreenWidth / 2) {
                    switch(m_direction){
                        case UP:
                            m_direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            m_direction = Direction.DOWN;
                            break;
                        case DOWN:
                            m_direction = Direction.LEFT;
                            break;
                        case LEFT:
                            m_direction = Direction.UP;
                            break;
                    }
                } else {
                    switch(m_direction){
                        case UP:
                            m_direction = Direction.LEFT;
                            break;
                        case LEFT:
                            m_direction = Direction.DOWN;
                            break;
                        case DOWN:
                            m_direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            m_direction = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }

}
