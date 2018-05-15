package pw.lunzi.cloudstorage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by 家杰 on 2015/11/20.
 */
class MyPasswordEditText : MyEditText {

    //资源
    private val INVISIBLE = R.drawable.close
    private val VISIBLE = R.drawable.open
    //按钮宽度dp
    private var mWidth: Int = 0
    //按钮的bitmap
    private var mBitmap_invisible: Bitmap? = null
    private var mBitmap_visible: Bitmap? = null
    //间隔
    private var theInterval: Int = 0
    //内容是否可见
    private var isVisible = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        setSingleLine()
        //设置EditText文本为隐藏的(注意！需要在setSingleLine()之后调用)
        transformationMethod = PasswordTransformationMethod.getInstance()

        mWidth = getmWidth_clear()
        theInterval = interval
        addRight(mWidth + theInterval)
        mBitmap_invisible = createBitmap(INVISIBLE, context)
        mBitmap_visible = createBitmap(VISIBLE, context)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val right = width + scrollX - theInterval
        val left = right - mWidth
        val top = (height - mWidth) / 2
        val bottom = top + mWidth
        val rect = Rect(left, top, right, bottom)

        if (isVisible) {
            canvas.drawBitmap(mBitmap_visible!!, null, rect, null)
        } else {
            canvas.drawBitmap(mBitmap_invisible!!, null, rect, null)
        }
    }

    /**
     * 改写父类的方法
     */
    override fun drawClear(translationX: Int, canvas: Canvas) {
        val scale = 1f - translationX.toFloat() / (getmWidth_clear() + theInterval).toFloat()
        val right = ((width + scrollX).toFloat() - theInterval.toFloat() - mWidth.toFloat() - theInterval.toFloat() - getmWidth_clear() * (1f - scale) / 2f).toInt()
        val left = ((width + scrollX).toFloat() - theInterval.toFloat() - mWidth.toFloat() - theInterval.toFloat() - getmWidth_clear() * (scale + (1f - scale) / 2f)).toInt()
        val top = ((height - getmWidth_clear() * scale) / 2).toInt()
        val bottom = (top + getmWidth_clear() * scale).toInt()
        val rect = Rect(left, top, right, bottom)
        canvas.drawBitmap(getmBitmap_clear()!!, null, rect, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val touchable = width - mWidth - theInterval < event.x && event.x < width - theInterval
            if (touchable) {
                isVisible = !isVisible
                if (isVisible) {
                    //设置EditText文本为可见的
                    transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    //设置EditText文本为隐藏的
                    transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
        }
        return super.onTouchEvent(event)
    }
}