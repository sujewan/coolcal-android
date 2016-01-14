package co.netguru.android.coolcal.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import co.netguru.android.coolcal.R
import co.netguru.android.coolcal.model.Event
import java.util.concurrent.TimeUnit

/**
 * todo: configurable scale (day/hour/minute/second/millisecond)
 * todo: configurable text scale marks
 *
 * example: day timeline with scale every 30min and text ind. every hour
 */

class EventTimelineView : View {

    companion object {

        val DEFAULT_TIME_SPAN = TimeUnit.HOURS.toMillis(24) // hours in millis
        const val DEFAULT_TIME_UNIT_WIDTH = 40 // px
        const val DEFAULT_START_DT = 0L
        const val DEFAULT_BAR_HEIGHT = 6 // px
        const val DEFAULT_BAR_SPACING = 6 // px
        const val DEFAULT_SCALE_COLOR = Color.LTGRAY
    }

    val linePaint: Paint by lazy {
        val paint = Paint()
        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 4f
        paint
    }

    val scalePaint: Paint by lazy {
        val paint = Paint()
        paint.color = Color.LTGRAY
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeWidth = 1f
        paint
    }

    private var barHeight: Int = DEFAULT_BAR_HEIGHT
    private var barSpacing: Int = DEFAULT_BAR_SPACING
    private var scaleColor: Int = DEFAULT_SCALE_COLOR
    private var timeUnitWidth: Int = DEFAULT_TIME_UNIT_WIDTH

    private var w = 0
    private var h = 0

    private var _events = emptyList<Event>()
    public var events: List<Event>
        get() = _events
        set(value) {
            _events = value
            invalidate()
        }

    private var _startDt = DEFAULT_START_DT
    public var startDt: Long
        get() = _startDt
        set(value) {
            _startDt = value
            invalidate()
        }

    private var timeSpan: Long = DEFAULT_TIME_SPAN

    public var hourSpan: Long
        get() = TimeUnit.MILLISECONDS.toHours(timeSpan)
        set(value) {
            timeSpan = TimeUnit.HOURS.toMillis(value)
        }

    private fun stopDt() = startDt + timeSpan

    constructor(context: Context) : this(context, null) {
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initFromAttributes(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun initFromAttributes(context: Context, attrs: AttributeSet?,
                                   defStyleAttr: Int, defStyleRes: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EventTimelineView,
                defStyleAttr, defStyleRes)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = w
        this.h = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Get size requested and size mode
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //Determine Width:
        val width = when (widthMode) {
        // if exactly, set width as desired and modify unit width to fill view
            MeasureSpec.EXACTLY -> widthSize
        // if wrap-content, uses attribute-specified unit width * units to determine
            MeasureSpec.AT_MOST -> {
                widthSize
            } // todo: !!! nie ma jeszcze osobnej obslugi wrapa
            else -> {
                widthSize
            }
        }

        //Determine Height: no default, wrap content and static bar height * bars
        val height = when (heightMode) {
        // if exactly, modify bar spacing and text size to fit
            MeasureSpec.EXACTLY -> {
                barSpacing = heightSize / events.size
                heightSize
            }
        // if wrap-content, use attr-specified bar spacing and textsize to determine final height
            MeasureSpec.AT_MOST -> {
                barSpacing * events.size + 1
            }
            else -> {
                heightSize
            }
        }

        setMeasuredDimension(width, height)
    }
}

internal enum class TimelineUnit(millis: Long) {
    MILLISECOND(1L),
    SECOND(TimeUnit.SECONDS.toMillis(1)),
    MINUTE(TimeUnit.MINUTES.toMillis(1)),
    HOUR(TimeUnit.HOURS.toMillis(1)),
    DAY(TimeUnit.DAYS.toMillis(1)),
}

internal data class TimelineParams(
        /*
            Event bar
         */
        var barHeight: Int = 0, // px
        var barSpacing: Int = 0, // px
        var borderSpacing: Int = 0, // px

        /*
            Time span
         */
        var timelineUnit: TimelineUnit = TimelineUnit.HOUR,
        var unitWidth: Int = 0,
        var markTime: Float = 0f,
        var markScale: Float = 0f,

        /*
            Styling
         */
        var scaleColor: Int = Color.LTGRAY,
        var defaultBarColor: Int = Color.RED,
        var showTime: Boolean = true,
        var showTitles: Boolean = true,
        var fontSize: Float = 0f,

        /*
            Span
         */
        var startDateTime: Long = 0L,
        var timeSpan: Long = 0L
) {
    internal fun stopDt() = startDateTime + timeSpan

    internal fun setFromAttributes(a: TypedArray) {
        for (i in 0..a.indexCount) {
            val attr = a.getIndex(i)
            when (attr) { // todo: defaults, exceptions
                R.styleable.EventTimelineView_barHeight ->
                    this.barHeight = a.getDimensionPixelSize(attr, this.barHeight)

                R.styleable.EventTimelineView_barSpacing ->
                    this.barSpacing = a.getDimensionPixelSize(attr, this.barSpacing)

                R.styleable.EventTimelineView_borderSpacing ->
                    this.borderSpacing = a.getDimensionPixelSize(attr, this.borderSpacing)

                R.styleable.EventTimelineView_unitWidth ->
                    this.unitWidth = a.getDimensionPixelSize(attr, this.unitWidth)

                R.styleable.EventTimelineView_markTime ->
                    this.markTime = a.getFloat(attr, this.markTime)

                R.styleable.EventTimelineView_markScale ->
                    this.markScale = a.getFloat(attr, this.markScale)

                R.styleable.EventTimelineView_timelineUnit ->
                    this.timelineUnit = TimelineUnit.valueOf(a.getString(attr))

                R.styleable.EventTimelineView_scaleColor ->
                    this.scaleColor = a.getColor(attr, this.scaleColor)

                R.styleable.EventTimelineView_defaultBarColor ->
                    this.defaultBarColor = a.getColor(attr, this.defaultBarColor)

                R.styleable.EventTimelineView_showTime ->
                    this.showTime = a.getBoolean(attr, this.showTime)

                R.styleable.EventTimelineView_showTitles ->
                    this.showTitles = a.getBoolean(attr, this.showTitles)

                R.styleable.EventTimelineView_fontSize ->
                    this.fontSize = a.getFloat(attr, this.fontSize)

                R.styleable.EventTimelineView_startDateTime ->
                    this.startDateTime = a.getString(attr).toLong()

                R.styleable.EventTimelineView_timeSpan ->
                    this.timeSpan = a.getString(attr).toLong()
            }
        }
    }

}

// DRAWING
//        val stopDt = stopDt()
//
//        fun normForRange(value: Long) = (value - startDt).toFloat() / timeSpan
//
//        events.forEachIndexed { i, event ->
//            val start = if (event.dtStart < startDt) startDt else event.dtStart
//            val stop = if (event.dtStop > stopDt) stopDt else event.dtStop
//            val startX = normForRange(start) * w
//            val stopX = normForRange(stop) * w
//            val Y = ((barSpacing) / 2 + i * barSpacing).toFloat()
//
//            Log.d("Timeline", "drawing: ($startX,$Y)->($stopX,$Y)")
//            canvas!!.drawLine(startX, Y, stopX, Y, linePaint)
//        }
//
//        for (i in startDt..stopDt) { // todo: maybe optimize to minutes? or attribute-based accuracy?
//            if(i % HALF_HOUR_MILLIS == 0L) {
//                // hour
//                if (i % HOUR_MILLIS == 0L) {
//                    val X = normForRange(i) * w
//                    canvas!!.drawLine(X, 0f, X, h.toFloat(), scalePaint)
//                }
//            } else {
//                // todo: half hour scale
//            }
//        }
