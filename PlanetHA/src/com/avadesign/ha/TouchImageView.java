package com.avadesign.ha;
 
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
 
public class TouchImageView extends ImageView 
{
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
 
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
 
    private int mode = NONE;
    private PointF startPoint = new PointF();
    private float oldDistance;
    private PointF midPoint = new PointF();
 
    public TouchImageView(Context context)
    {
        super(context);
        init(context);
    }
 
    public TouchImageView(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
        init(context);
    }
 
    public TouchImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }
 
    private void init(Context context) 
    {
        this.setClickable(true);
        this.setScaleType(ScaleType.MATRIX);
 
        this.setOnTouchListener(new OnTouchListener() 
        {
            public boolean onTouch(View v, MotionEvent event) 
            {
                switch (event.getAction() & MotionEvent.ACTION_MASK) 
                {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    startPoint.set(event.getX(), event.getY());
                    savedMatrix.set(matrix);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mode == DRAG)
                    {
                        matrix.set(savedMatrix);
                        float x = event.getX() - startPoint.x;
                        float y = event.getY() - startPoint.y;
                        matrix.postTranslate(x, y);
                    }
                    else if (mode == ZOOM) 
                    {
                        float newDist = (float) culcDistance(event);
                        float scale = newDist / oldDistance;
                        matrix.set(savedMatrix);
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = ZOOM;
                    oldDistance = (float) culcDistance(event);
                    culcMidPoint(midPoint, event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                default:
                    break;
                }
 
                setImageMatrix(matrix);
                return true;
            }
        });
    }
 
    private double culcDistance(MotionEvent event) 
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }
 
    private void culcMidPoint(PointF midPoint, MotionEvent event) 
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        midPoint.set(x / 2, y / 2);
    }
}