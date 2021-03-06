package tornadofx

import javafx.animation.*
import javafx.beans.value.WritableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.Pane
import javafx.util.Duration
import java.util.ArrayList

operator fun Timeline.plusAssign(keyFrame: KeyFrame) {
    keyFrames.add(keyFrame)
}

operator fun KeyFrame.plusAssign(keyValue: KeyValue) {
    values.add(keyValue)
}

fun Pane.timeline(play: Boolean = true, op: (Timeline).() -> Unit): Timeline {
    val timeline = Timeline()
    timeline.op()
    if (play) timeline.play()
    return timeline
}

fun Timeline.keyframe(duration: Duration, op: (KeyFrameBuilder).() -> Unit): KeyFrame {
    val keyFrameBuilder = KeyFrameBuilder(duration)
    keyFrameBuilder.op()

    return keyFrameBuilder.build().apply {
        this@keyframe +=  this
    }
}

class KeyFrameBuilder(val duration: Duration) {

    var keyValues: MutableList<KeyValue> = ArrayList()
    var name: String? = null
    private var _onFinished: (ActionEvent) -> Unit = {}

    fun setOnFinished(onFinished: (ActionEvent) -> Unit) {
        this._onFinished = onFinished
    }

    operator fun plusAssign(keyValue: KeyValue) {
        keyValues.add(keyValue)
    }

    fun <T> keyvalue(writableValue: WritableValue<T>, endValue: T, interpolator: Interpolator? = null): KeyValue {
        val keyValue = interpolator?.let {  KeyValue(writableValue, endValue, it) } ?: KeyValue(writableValue, endValue)
        this += keyValue
        return keyValue
    }

    internal fun build() = KeyFrame(duration,name,_onFinished,keyValues)

}

fun <T> WritableValue<T>.animate(endValue: T, duration: Duration, interpolator: Interpolator? = null, op: (Timeline.() -> Unit)? = null) {
    val writableValue = this
    val timeline = Timeline()

    timeline.apply {
        keyframe(duration) {
            keyvalue(writableValue,endValue,interpolator)
        }
    }

    op?.apply {this.invoke(timeline) }

    timeline.play()

}

val Number.millis: Duration get() = Duration.millis(this.toDouble())
val Number.seconds: Duration get() = Duration.seconds(this.toDouble())
val Number.minutes: Duration get() = Duration.minutes(this.toDouble())
val Number.hours: Duration get() = Duration.hours(this.toDouble())

operator fun Duration.plus(duration: Duration): Duration = this.add(duration)
operator fun Duration.minus(duration: Duration): Duration = this.minus(duration)

class ViewTransition {
    companion object {
        val SlideIn = fun (existing: UIComponent, replacement: UIComponent, transitionCompleteCallback: () -> Unit) {
            replacement.root.translateX = existing.root.boundsInLocal.width

            val existingSlide = TranslateTransition(0.2.seconds, existing.root).apply {
                toX = -existing.root.boundsInLocal.width
                interpolator = Interpolator.EASE_OUT
            }

            val replacementSlide = TranslateTransition(0.2.seconds, replacement.root).apply {
                toX = 0.0
                onFinished = EventHandler { transitionCompleteCallback() }
                interpolator = Interpolator.EASE_OUT
            }

            existingSlide.play()
            replacementSlide.play()
        }

        val SlideOut = fun (existing: UIComponent, replacement: UIComponent, transitionCompleteCallback: () -> Unit) {
            replacement.root.translateX = -existing.root.boundsInLocal.width

            val existingSlide = TranslateTransition(0.2.seconds, existing.root).apply {
                toX = existing.root.boundsInLocal.width
                interpolator = Interpolator.EASE_OUT
            }

            val replacementSlide = TranslateTransition(0.2.seconds, replacement.root).apply {
                toX = 0.0
                onFinished = EventHandler { transitionCompleteCallback() }
                interpolator = Interpolator.EASE_OUT
            }

            existingSlide.play()
            replacementSlide.play()
        }
    }
}