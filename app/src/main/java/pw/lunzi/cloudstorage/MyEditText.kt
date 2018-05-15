package pw.lunzi.cloudstorage

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation

open class MyEditText : AppCompatEditText {

    //按钮资源
    private val CLEAR = R.drawable.clearfill
    //动画时长
    private val ANIMATOR_TIME = 200
    //按钮左右间隔,单位DP
    private val INTERVAL = 5
    //清除按钮宽度,单位DP
    private val WIDTH_OF_CLEAR = 23


    //间隔记录
    //----------------以下方法为方便子类继承，只使用MyEditText就没有用处---------------------------------------------------------------------

    var interval: Int = 0
        private set
    //清除按钮宽度记录
    private var mWidth_clear: Int = 0
    //右内边距
    private var mPaddingRight: Int = 0
    //清除按钮的bitmap
    private var mBitmap_clear: Bitmap? = null
    //清除按钮出现动画
    private var mAnimator_visible: ValueAnimator? = null
    //消失动画
    private var mAnimator_gone: ValueAnimator? = null
    //是否显示的记录
    private var isVisible = false
    //右边添加其他按钮时使用
    private var mRight = 0

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

        mBitmap_clear = createBitmap(CLEAR, context)

        interval = dp2px(INTERVAL.toFloat())
        mWidth_clear = dp2px(WIDTH_OF_CLEAR.toFloat())
        mPaddingRight = interval + mWidth_clear + interval
        mAnimator_gone = ValueAnimator.ofFloat(1f, 0f).setDuration(ANIMATOR_TIME.toLong())
        mAnimator_visible = ValueAnimator.ofInt(mWidth_clear + interval, 0).setDuration(ANIMATOR_TIME.toLong())

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        //设置内边距
        setPadding(paddingLeft, paddingTop, mPaddingRight + mRight, paddingBottom)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)//抗锯齿
        if (mAnimator_visible!!.isRunning) {
            val x = mAnimator_visible!!.animatedValue as Int
            drawClear(x, canvas)
            invalidate()
        } else if (isVisible) {
            drawClear(0, canvas)
        }

        if (mAnimator_gone!!.isRunning) {
            val scale = mAnimator_gone!!.animatedValue as Float
            drawClearGone(scale, canvas)
            invalidate()
        }
    }

    /**
     * 绘制清除按钮出现的图案
     * @param translationX 水平移动距离
     * @param canvas
     */
    protected open fun drawClear(translationX: Int, canvas: Canvas) {
        val right = width + scrollX - interval - mRight + translationX
        val left = right - mWidth_clear
        val top = (height - mWidth_clear) / 2
        val bottom = top + mWidth_clear
        val rect = Rect(left, top, right, bottom)
        canvas.drawBitmap(mBitmap_clear!!, null, rect, null)

    }

    /**
     * 绘制清除按钮消失的图案
     * @param scale 缩放比例
     * @param canvas
     */
    protected fun drawClearGone(scale: Float, canvas: Canvas) {
        val right = ((width + scrollX).toFloat() - interval.toFloat() - mRight.toFloat() - mWidth_clear * (1f - scale) / 2f).toInt()
        val left = ((width + scrollX).toFloat() - interval.toFloat() - mRight.toFloat() - mWidth_clear * (scale + (1f - scale) / 2f)).toInt()
        val top = ((height - mWidth_clear * scale) / 2).toInt()
        val bottom = (top + mWidth_clear * scale).toInt()
        val rect = Rect(left, top, right, bottom)
        canvas.drawBitmap(mBitmap_clear!!, null, rect, null)
    }

    /**
     * 开始清除按钮的显示动画
     */
    private fun startVisibleAnimator() {
        endAnaimator()
        mAnimator_visible!!.start()
        invalidate()
    }

    /**
     * 开始清除按钮的消失动画
     */
    private fun startGoneAnimator() {
        endAnaimator()
        mAnimator_gone!!.start()
        invalidate()
    }

    /**
     * 结束所有动画
     */
    private fun endAnaimator() {
        mAnimator_gone!!.end()
        mAnimator_visible!!.end()
    }

    /**
     * Edittext内容变化的监听
     * @param text
     * @param start
     * @param lengthBefore
     * @param lengthAfter
     */
    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (text.length > 0) {
            if (!isVisible) {
                isVisible = true
                startVisibleAnimator()
            }
        } else {
            if (isVisible) {
                isVisible = false
                startGoneAnimator()
            }
        }
    }

    /**
     * 触控执行的监听
     * @param event
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {

            val touchable = width - interval - mRight - mWidth_clear < event.x && event.x < width - interval - mRight
            if (touchable) {
                error = null
                this.setText("")
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 开始晃动动画
     */
    fun startShakeAnimation() {
        if (animation == null) {
            this.animation = shakeAnimation(4)
        }
        this.startAnimation(animation)
    }

    /**
     * 晃动动画
     * @param counts 0.5秒钟晃动多少下
     * @return
     */
    private fun shakeAnimation(counts: Int): Animation {
        val translateAnimation = TranslateAnimation(0f, 10f, 0f, 0f)
        translateAnimation.interpolator = CycleInterpolator(counts.toFloat())
        translateAnimation.duration = 500
        return translateAnimation
    }

    /**
     * 给图标染上当前提示文本的颜色并且转出Bitmap
     * @param resources
     * @param context
     * @return
     */
    fun createBitmap(resources: Int, context: Context): Bitmap {
        val drawable = ContextCompat.getDrawable(context, resources)
        val wrappedDrawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(wrappedDrawable, currentHintTextColor)
        return drawableToBitamp(wrappedDrawable)
    }

    /**
     * drawable转换成bitmap
     * @param drawable
     * @return
     */
    private fun drawableToBitamp(drawable: Drawable): Bitmap {
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val config = if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val bitmap = Bitmap.createBitmap(w, h, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    fun dp2px(dipValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun getmWidth_clear(): Int {
        return mWidth_clear
    }

    fun getmBitmap_clear(): Bitmap? {
        return mBitmap_clear
    }

    fun addRight(right: Int) {
        mRight += right
    }


}