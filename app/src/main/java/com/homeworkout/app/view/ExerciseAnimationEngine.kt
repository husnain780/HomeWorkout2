package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.R
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwTextSecondary
import com.homeworkout.app.ui.theme.HwTextTertiary
import dev.romainguy.kotlin.math.Float3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ExercisePropType {
    NONE,
    FLOOR_MAT,
    BENCH_LEFT,
    BENCH_RIGHT,
    WALL_LEFT,
    STEP_BOX,
    DIAMOND_CUE
}

data class Vec2(val x: Float, val y: Float) {
    fun toOffset(width: Float, height: Float): Offset = Offset(x * width, y * height)
}

data class CharacterPose(
    val exerciseId: String,
    val phase: Float,
    val head: Vec2,
    val neck: Vec2,
    val shoulderL: Vec2,
    val elbowL: Vec2,
    val handL: Vec2,
    val shoulderR: Vec2,
    val elbowR: Vec2,
    val handR: Vec2,
    val hipL: Vec2,
    val kneeL: Vec2,
    val ankleL: Vec2,
    val hipR: Vec2,
    val kneeR: Vec2,
    val ankleR: Vec2,
    val focusPoint: Vec2,
    val propType: ExercisePropType = ExercisePropType.FLOOR_MAT,
    val headRadiusRatio: Float = 0.052f
) {
    val hipCenter: Vec2
        get() = Vec2((hipL.x + hipR.x) * 0.5f, (hipL.y + hipR.y) * 0.5f)

    val shoulderCenter: Vec2
        get() = Vec2((shoulderL.x + shoulderR.x) * 0.5f, (shoulderL.y + shoulderR.y) * 0.5f)

    fun allJoints(): List<Vec2> = listOf(
        head, neck,
        shoulderL, elbowL, handL,
        shoulderR, elbowR, handR,
        hipL, kneeL, ankleL,
        hipR, kneeR, ankleR
    )
}

object ExercisePoseEngine {

    val supportedExerciseIds: Set<String> = setOf(
        "push_up",
        "squat",
        "plank",
        "jumping_jack",
        "lunges",
        "mountain_climber",
        "glute_bridge",
        "biceps_curl",
        "shoulder_press",
        "triceps_dip",
        "calf_raise",
        "burpee",
        "high_knees",
        "bicycle_crunch",
        "dead_bug",
        "bird_dog",
        "russian_twist",
        "side_plank",
        "reverse_lunge",
        "wall_sit",
        "incline_push_up",
        "diamond_push_up",
        "superman",
        "hip_hinge",
        "donkey_kick",
        "fire_hydrant",
        "step_up",
        "lateral_raise",
        "flutter_kicks",
        "standing_knee_raise"
    )

    fun cycleDurationMs(exerciseId: String): Int = when (exerciseId.lowercase()) {
        "high_knees", "flutter_kicks", "mountain_climber" -> 1250
        "jumping_jack", "bicycle_crunch", "standing_knee_raise" -> 1650
        "biceps_curl", "shoulder_press", "lateral_raise", "calf_raise", "russian_twist" -> 1900
        "burpee", "bird_dog", "dead_bug", "lunges", "reverse_lunge", "step_up" -> 2600
        "plank", "side_plank", "wall_sit" -> 2400
        else -> 2100
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t.coerceIn(0f, 1f)
    private fun lerpVec(a: Vec2, b: Vec2, t: Float): Vec2 = Vec2(lerp(a.x, b.x, t), lerp(a.y, b.y, t))

    /**
     * Smooth 0 -> 1 -> 0 rep wave for phase in [0f, 1f].
     */
    private fun repWave(phase: Float): Float {
        val p = ((phase % 1f) + 1f) % 1f
        return (0.5f - 0.5f * cos(2.0 * PI * p)).toFloat()
    }

    /**
     * Alternating wave in [-1f, 1f] for bilateral exercises (e.g. mountain climber, high knees, bicycle crunch).
     */
    private fun altWave(phase: Float): Float {
        val p = ((phase % 1f) + 1f) % 1f
        return sin(2.0 * PI * p).toFloat()
    }

    fun computePose(exerciseId: String, rawPhase: Float): CharacterPose {
        val phase = ((rawPhase % 1f) + 1f) % 1f
        val w = repWave(phase)
        val alt = altWave(phase)
        val id = exerciseId.trim().lowercase()

        return when (id) {
            "push_up" -> posePushUp(id, phase, w, diamond = false)
            "diamond_push_up" -> posePushUp(id, phase, w, diamond = true)
            "incline_push_up" -> poseInclinePushUp(id, phase, w)
            "plank" -> posePlank(id, phase, w)
            "side_plank" -> poseSidePlank(id, phase, w)
            "mountain_climber" -> poseMountainClimber(id, phase, alt)
            "squat" -> poseSquat(id, phase, w)
            "jumping_jack" -> poseJumpingJack(id, phase, w)
            "lunges" -> poseLunge(id, phase, w, isReverse = false)
            "reverse_lunge" -> poseLunge(id, phase, w, isReverse = true)
            "glute_bridge" -> poseGluteBridge(id, phase, w)
            "biceps_curl" -> poseBicepsCurl(id, phase, w)
            "shoulder_press" -> poseShoulderPress(id, phase, w)
            "triceps_dip" -> poseTricepsDip(id, phase, w)
            "calf_raise" -> poseCalfRaise(id, phase, w)
            "burpee" -> poseBurpee(id, phase)
            "high_knees" -> poseHighKnees(id, phase, alt)
            "bicycle_crunch" -> poseBicycleCrunch(id, phase, alt)
            "dead_bug" -> poseDeadBug(id, phase, alt)
            "bird_dog" -> poseBirdDog(id, phase, alt)
            "russian_twist" -> poseRussianTwist(id, phase, alt)
            "wall_sit" -> poseWallSit(id, phase, w)
            "superman" -> poseSuperman(id, phase, w)
            "hip_hinge" -> poseHipHinge(id, phase, w)
            "donkey_kick" -> poseDonkeyKick(id, phase, w)
            "fire_hydrant" -> poseFireHydrant(id, phase, w)
            "step_up" -> poseStepUp(id, phase, w)
            "lateral_raise" -> poseLateralRaise(id, phase, w)
            "flutter_kicks" -> poseFlutterKicks(id, phase, alt)
            "standing_knee_raise" -> poseStandingKneeRaise(id, phase, alt)
            else -> poseSquat(id, phase, w)
        }
    }

    private fun posePushUp(id: String, phase: Float, w: Float, diamond: Boolean): CharacterPose {
        // Side-angle prone push-up: w=0 is top plank, w=1 is bottom chest near floor
        val drop = w * 0.14f
        val shoulder = Vec2(0.66f, 0.50f + drop)
        val hip = Vec2(0.42f, 0.58f + drop * 0.65f)
        val knee = Vec2(0.28f, 0.68f + drop * 0.35f)
        val ankle = Vec2(0.16f, 0.78f)
        val neck = Vec2(0.71f, 0.47f + drop)
        val head = Vec2(0.77f, 0.44f + drop)

        val handX = if (diamond) 0.65f else 0.68f
        val elbowX = if (diamond) lerp(0.65f, 0.57f, w) else lerp(0.67f, 0.59f, w)
        val elbowY = lerp(0.64f, 0.54f, w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = head,
            neck = neck,
            shoulderL = Vec2(shoulder.x - 0.02f, shoulder.y - 0.02f),
            elbowL = Vec2(elbowX - 0.02f, elbowY - 0.01f),
            handL = Vec2(handX - 0.02f, 0.78f),
            shoulderR = shoulder,
            elbowR = Vec2(elbowX, elbowY),
            handR = Vec2(handX, 0.78f),
            hipL = Vec2(hip.x - 0.01f, hip.y - 0.01f),
            kneeL = Vec2(knee.x - 0.01f, knee.y - 0.01f),
            ankleL = Vec2(ankle.x - 0.01f, ankle.y),
            hipR = hip,
            kneeR = knee,
            ankleR = ankle,
            focusPoint = shoulder,
            propType = if (diamond) ExercisePropType.DIAMOND_CUE else ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseInclinePushUp(id: String, phase: Float, w: Float): CharacterPose {
        val drop = w * 0.11f
        val shoulder = Vec2(0.65f, 0.38f + drop)
        val hip = Vec2(0.43f, 0.54f + drop * 0.6f)
        val knee = Vec2(0.29f, 0.67f + drop * 0.3f)
        val ankle = Vec2(0.17f, 0.78f)
        val neck = Vec2(0.69f, 0.34f + drop)
        val head = Vec2(0.75f, 0.30f + drop)
        val elbow = Vec2(lerp(0.67f, 0.58f, w), lerp(0.50f, 0.45f, w))
        val hand = Vec2(0.71f, 0.60f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = head,
            neck = neck,
            shoulderL = Vec2(shoulder.x - 0.02f, shoulder.y - 0.01f),
            elbowL = Vec2(elbow.x - 0.02f, elbow.y - 0.01f),
            handL = Vec2(hand.x - 0.01f, hand.y),
            shoulderR = shoulder,
            elbowR = elbow,
            handR = hand,
            hipL = Vec2(hip.x - 0.01f, hip.y - 0.01f),
            kneeL = Vec2(knee.x - 0.01f, knee.y - 0.01f),
            ankleL = Vec2(ankle.x - 0.01f, ankle.y),
            hipR = hip,
            kneeR = knee,
            ankleR = ankle,
            focusPoint = shoulder,
            propType = ExercisePropType.BENCH_RIGHT
        )
    }

    private fun posePlank(id: String, phase: Float, w: Float): CharacterPose {
        // Forearm plank with subtle core breathing pulse
        val breath = w * 0.018f
        val shoulder = Vec2(0.66f, 0.58f + breath)
        val hip = Vec2(0.43f, 0.60f + breath * 1.2f)
        val knee = Vec2(0.28f, 0.69f + breath * 0.5f)
        val ankle = Vec2(0.16f, 0.78f)
        val elbow = Vec2(0.66f, 0.77f)
        val hand = Vec2(0.77f, 0.77f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.77f, 0.54f + breath),
            neck = Vec2(0.71f, 0.56f + breath),
            shoulderL = Vec2(shoulder.x - 0.02f, shoulder.y - 0.01f),
            elbowL = Vec2(elbow.x - 0.02f, elbow.y),
            handL = Vec2(hand.x - 0.02f, hand.y),
            shoulderR = shoulder,
            elbowR = elbow,
            handR = hand,
            hipL = Vec2(hip.x - 0.01f, hip.y - 0.01f),
            kneeL = Vec2(knee.x - 0.01f, knee.y),
            ankleL = Vec2(ankle.x - 0.01f, ankle.y),
            hipR = hip,
            kneeR = knee,
            ankleR = ankle,
            focusPoint = hip,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseSidePlank(id: String, phase: Float, w: Float): CharacterPose {
        // Lateral side plank with gentle hip elevation pulse
        val lift = w * 0.045f
        val shoulderB = Vec2(0.66f, 0.55f - lift * 0.4f)
        val shoulderT = Vec2(0.63f, 0.45f - lift * 0.4f)
        val hip = Vec2(0.45f, 0.61f - lift)
        val knee = Vec2(0.31f, 0.70f - lift * 0.5f)
        val ankle = Vec2(0.18f, 0.78f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.73f, 0.44f - lift * 0.4f),
            neck = Vec2(0.67f, 0.48f - lift * 0.4f),
            shoulderL = shoulderB,
            elbowL = Vec2(0.68f, 0.77f),
            handL = Vec2(0.76f, 0.77f),
            shoulderR = shoulderT,
            elbowR = Vec2(0.61f, 0.34f - lift * 0.5f),
            handR = Vec2(0.60f, 0.23f - lift * 0.5f),
            hipL = Vec2(hip.x, hip.y + 0.02f),
            kneeL = Vec2(knee.x, knee.y + 0.01f),
            ankleL = ankle,
            hipR = Vec2(hip.x, hip.y - 0.02f),
            kneeR = Vec2(knee.x, knee.y - 0.01f),
            ankleR = Vec2(ankle.x, ankle.y - 0.02f),
            focusPoint = hip,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseMountainClimber(id: String, phase: Float, alt: Float): CharacterPose {
        val leftDrive = ((alt + 1f) * 0.5f)
        val rightDrive = 1f - leftDrive

        val shoulder = Vec2(0.68f, 0.50f)
        val hip = Vec2(0.43f, 0.55f)

        val kneeL = lerpVec(Vec2(0.28f, 0.68f), Vec2(0.57f, 0.62f), leftDrive)
        val ankleL = lerpVec(Vec2(0.15f, 0.78f), Vec2(0.44f, 0.72f), leftDrive)

        val kneeR = lerpVec(Vec2(0.28f, 0.68f), Vec2(0.57f, 0.62f), rightDrive)
        val ankleR = lerpVec(Vec2(0.15f, 0.78f), Vec2(0.44f, 0.72f), rightDrive)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.79f, 0.44f),
            neck = Vec2(0.73f, 0.47f),
            shoulderL = Vec2(shoulder.x - 0.02f, shoulder.y - 0.01f),
            elbowL = Vec2(0.67f, 0.64f),
            handL = Vec2(0.67f, 0.78f),
            shoulderR = shoulder,
            elbowR = Vec2(0.69f, 0.64f),
            handR = Vec2(0.69f, 0.78f),
            hipL = Vec2(hip.x - 0.01f, hip.y - 0.01f),
            kneeL = kneeL,
            ankleL = ankleL,
            hipR = hip,
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = kneeR,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseSquat(id: String, phase: Float, w: Float): CharacterPose {
        // 3/4 profile bodyweight squat: hips shift back and drop to knee height while arms extend forward
        val hipX = lerp(0.46f, 0.37f, w)
        val hipY = lerp(0.49f, 0.63f, w)
        val shoulderX = lerp(0.48f, 0.45f, w)
        val shoulderY = lerp(0.29f, 0.42f, w)
        val kneeX = lerp(0.49f, 0.56f, w)
        val kneeY = lerp(0.64f, 0.64f, w)
        val ankleX = 0.48f
        val ankleY = 0.80f

        val elbowX = lerp(0.47f, 0.58f, w)
        val elbowY = lerp(0.40f, 0.43f, w)
        val handX = lerp(0.50f, 0.69f, w)
        val handY = lerp(0.48f, 0.41f, w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(shoulderX + 0.02f, shoulderY - 0.10f),
            neck = Vec2(shoulderX + 0.01f, shoulderY - 0.04f),
            shoulderL = Vec2(shoulderX - 0.03f, shoulderY),
            elbowL = Vec2(elbowX - 0.02f, elbowY),
            handL = Vec2(handX - 0.02f, handY),
            shoulderR = Vec2(shoulderX + 0.02f, shoulderY),
            elbowR = Vec2(elbowX + 0.01f, elbowY),
            handR = Vec2(handX + 0.01f, handY),
            hipL = Vec2(hipX - 0.03f, hipY),
            kneeL = Vec2(kneeX - 0.04f, kneeY),
            ankleL = Vec2(ankleX - 0.04f, ankleY),
            hipR = Vec2(hipX + 0.02f, hipY),
            kneeR = Vec2(kneeX + 0.02f, kneeY),
            ankleR = Vec2(ankleX + 0.02f, ankleY),
            focusPoint = Vec2(hipX, hipY),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseJumpingJack(id: String, phase: Float, w: Float): CharacterPose {
        // Front view jumping jack: legs jump wide & arms sweep overhead
        val jumpLift = sin(PI * w).toFloat() * 0.035f
        val shoulderY = 0.30f - jumpLift
        val hipY = 0.50f - jumpLift

        val footSpread = lerp(0.04f, 0.16f, w)
        val kneeSpread = lerp(0.04f, 0.11f, w)

        // Arm angle from down at sides (-75 deg) to overhead (+75 deg)
        val armAngle = lerp(-1.25f, 1.25f, w)
        val upperLen = 0.11f
        val foreLen = 0.11f

        val shL = Vec2(0.43f, shoulderY)
        val shR = Vec2(0.57f, shoulderY)

        val elL = Vec2(shL.x - cos(armAngle) * upperLen, shL.y - sin(armAngle) * upperLen)
        val hdL = Vec2(elL.x - cos(armAngle + 0.25f) * foreLen, elL.y - sin(armAngle + 0.25f) * foreLen)

        val elR = Vec2(shR.x + cos(armAngle) * upperLen, shR.y - sin(armAngle) * upperLen)
        val hdR = Vec2(elR.x + cos(armAngle + 0.25f) * foreLen, elR.y - sin(armAngle + 0.25f) * foreLen)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, shoulderY - 0.10f),
            neck = Vec2(0.50f, shoulderY - 0.04f),
            shoulderL = shL,
            elbowL = elL,
            handL = hdL,
            shoulderR = shR,
            elbowR = elR,
            handR = hdR,
            hipL = Vec2(0.46f, hipY),
            kneeL = Vec2(0.50f - kneeSpread, 0.65f - jumpLift),
            ankleL = Vec2(0.50f - footSpread, 0.80f - jumpLift * 0.5f),
            hipR = Vec2(0.54f, hipY),
            kneeR = Vec2(0.50f + kneeSpread, 0.65f - jumpLift),
            ankleR = Vec2(0.50f + footSpread, 0.80f - jumpLift * 0.5f),
            focusPoint = Vec2(0.50f, shoulderY),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseLunge(id: String, phase: Float, w: Float, isReverse: Boolean): CharacterPose {
        // Side profile lunge: either stepping forward (lunges) or stepping backward (reverse_lunge)
        val hipX = if (isReverse) lerp(0.52f, 0.44f, w) else lerp(0.42f, 0.50f, w)
        val hipY = lerp(0.48f, 0.61f, w)
        val shoulderX = hipX
        val shoulderY = lerp(0.28f, 0.40f, w)

        val frontAnkleX = if (isReverse) 0.56f else lerp(0.44f, 0.64f, w)
        val backAnkleX = if (isReverse) lerp(0.50f, 0.27f, w) else 0.36f

        val frontKnee = Vec2(lerp(frontAnkleX - 0.01f, frontAnkleX, w), lerp(0.64f, 0.62f, w))
        val backKnee = Vec2(lerp(backAnkleX + 0.02f, hipX - 0.03f, w), lerp(0.64f, 0.75f, w))

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(shoulderX + 0.01f, shoulderY - 0.10f),
            neck = Vec2(shoulderX + 0.01f, shoulderY - 0.04f),
            shoulderL = Vec2(shoulderX - 0.02f, shoulderY),
            elbowL = Vec2(shoulderX - 0.05f, shoulderY + 0.11f),
            handL = Vec2(shoulderX + 0.02f, shoulderY + 0.14f),
            shoulderR = Vec2(shoulderX + 0.01f, shoulderY),
            elbowR = Vec2(shoulderX + 0.05f, shoulderY + 0.10f),
            handR = Vec2(shoulderX + 0.12f, shoulderY + 0.06f),
            hipL = Vec2(hipX - 0.01f, hipY),
            kneeL = backKnee,
            ankleL = Vec2(backAnkleX, 0.80f),
            hipR = Vec2(hipX + 0.01f, hipY),
            kneeR = frontKnee,
            ankleR = Vec2(frontAnkleX, 0.80f),
            focusPoint = frontKnee,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseGluteBridge(id: String, phase: Float, w: Float): CharacterPose {
        // Supine on mat: shoulders on left (0.30, 0.75), feet flat on right (0.68, 0.78), hips lift high
        val hipY = lerp(0.75f, 0.56f, w)
        val hipX = 0.48f
        val shoulder = Vec2(0.30f, 0.75f)
        val knee = Vec2(0.64f, 0.58f)
        val ankle = Vec2(0.70f, 0.78f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.20f, 0.74f),
            neck = Vec2(0.26f, 0.75f),
            shoulderL = Vec2(shoulder.x, shoulder.y - 0.01f),
            elbowL = Vec2(0.38f, 0.77f),
            handL = Vec2(0.48f, 0.78f),
            shoulderR = shoulder,
            elbowR = Vec2(0.39f, 0.78f),
            handR = Vec2(0.50f, 0.78f),
            hipL = Vec2(hipX - 0.01f, hipY),
            kneeL = Vec2(knee.x - 0.02f, knee.y),
            ankleL = Vec2(ankle.x - 0.02f, ankle.y),
            hipR = Vec2(hipX, hipY),
            kneeR = knee,
            ankleR = ankle,
            focusPoint = Vec2(hipX, hipY),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseBicepsCurl(id: String, phase: Float, w: Float): CharacterPose {
        // Standing front/3-4 view: elbows stay pinned at sides, forearms curl upward toward shoulders
        val shL = Vec2(0.43f, 0.30f)
        val shR = Vec2(0.57f, 0.30f)
        val elL = Vec2(0.41f, 0.42f)
        val elR = Vec2(0.59f, 0.42f)

        val handL = lerpVec(Vec2(0.40f, 0.53f), Vec2(0.43f, 0.32f), w)
        val handR = lerpVec(Vec2(0.60f, 0.53f), Vec2(0.57f, 0.32f), w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, 0.20f),
            neck = Vec2(0.50f, 0.26f),
            shoulderL = shL,
            elbowL = elL,
            handL = handL,
            shoulderR = shR,
            elbowR = elR,
            handR = handR,
            hipL = Vec2(0.46f, 0.50f),
            kneeL = Vec2(0.45f, 0.65f),
            ankleL = Vec2(0.45f, 0.80f),
            hipR = Vec2(0.54f, 0.50f),
            kneeR = Vec2(0.55f, 0.65f),
            ankleR = Vec2(0.55f, 0.80f),
            focusPoint = elR,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseShoulderPress(id: String, phase: Float, w: Float): CharacterPose {
        // Overhead shoulder press: hands press from shoulder level straight overhead
        val shL = Vec2(0.43f, 0.31f)
        val shR = Vec2(0.57f, 0.31f)

        val elL = lerpVec(Vec2(0.34f, 0.33f), Vec2(0.40f, 0.21f), w)
        val hdL = lerpVec(Vec2(0.35f, 0.23f), Vec2(0.43f, 0.10f), w)

        val elR = lerpVec(Vec2(0.66f, 0.33f), Vec2(0.60f, 0.21f), w)
        val hdR = lerpVec(Vec2(0.65f, 0.23f), Vec2(0.57f, 0.10f), w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, 0.21f),
            neck = Vec2(0.50f, 0.27f),
            shoulderL = shL,
            elbowL = elL,
            handL = hdL,
            shoulderR = shR,
            elbowR = elR,
            handR = hdR,
            hipL = Vec2(0.46f, 0.50f),
            kneeL = Vec2(0.45f, 0.65f),
            ankleL = Vec2(0.45f, 0.80f),
            hipR = Vec2(0.54f, 0.50f),
            kneeR = Vec2(0.55f, 0.65f),
            ankleR = Vec2(0.55f, 0.80f),
            focusPoint = shR,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseTricepsDip(id: String, phase: Float, w: Float): CharacterPose {
        // Bench behind back on the left; hands stay on bench (0.37, 0.57), elbows bend back as hips dip down
        val dip = w * 0.12f
        val shoulder = Vec2(0.42f, 0.40f + dip)
        val hip = Vec2(0.45f, 0.57f + dip)
        val elbow = Vec2(lerp(0.38f, 0.31f, w), lerp(0.49f, 0.47f, w))
        val hand = Vec2(0.37f, 0.57f)
        val knee = Vec2(0.60f, 0.61f + dip * 0.5f)
        val ankle = Vec2(0.69f, 0.78f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.43f, 0.30f + dip),
            neck = Vec2(0.42f, 0.35f + dip),
            shoulderL = Vec2(shoulder.x - 0.01f, shoulder.y),
            elbowL = Vec2(elbow.x - 0.01f, elbow.y),
            handL = Vec2(hand.x - 0.01f, hand.y),
            shoulderR = shoulder,
            elbowR = elbow,
            handR = hand,
            hipL = Vec2(hip.x - 0.01f, hip.y),
            kneeL = Vec2(knee.x - 0.02f, knee.y),
            ankleL = Vec2(ankle.x - 0.02f, ankle.y),
            hipR = hip,
            kneeR = knee,
            ankleR = ankle,
            focusPoint = elbow,
            propType = ExercisePropType.BENCH_LEFT
        )
    }

    private fun poseCalfRaise(id: String, phase: Float, w: Float): CharacterPose {
        // Standing calf raise: whole body rises vertically as ankles plantarflex
        val lift = w * 0.065f
        val shY = 0.30f - lift
        val hipY = 0.50f - lift
        val kneeY = 0.65f - lift
        val ankleY = 0.80f - lift * 0.85f

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, shY - 0.10f),
            neck = Vec2(0.50f, shY - 0.04f),
            shoulderL = Vec2(0.44f, shY),
            elbowL = Vec2(0.40f, shY + 0.10f),
            handL = Vec2(0.44f, hipY - 0.01f),
            shoulderR = Vec2(0.56f, shY),
            elbowR = Vec2(0.60f, shY + 0.10f),
            handR = Vec2(0.56f, hipY - 0.01f),
            hipL = Vec2(0.47f, hipY),
            kneeL = Vec2(0.47f, kneeY),
            ankleL = Vec2(0.47f, ankleY),
            hipR = Vec2(0.53f, hipY),
            kneeR = Vec2(0.53f, kneeY),
            ankleR = Vec2(0.53f, ankleY),
            focusPoint = Vec2(0.53f, ankleY),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseBurpee(id: String, phase: Float): CharacterPose {
        // 4-stage burpee: 0.0=Stand/Jump, 0.25=Squat Tuck, 0.5=Plank, 0.75=Squat Tuck, 1.0=Jump
        val standJump = CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, 0.16f),
            neck = Vec2(0.50f, 0.22f),
            shoulderL = Vec2(0.45f, 0.26f),
            elbowL = Vec2(0.42f, 0.16f),
            handL = Vec2(0.44f, 0.07f),
            shoulderR = Vec2(0.55f, 0.26f),
            elbowR = Vec2(0.58f, 0.16f),
            handR = Vec2(0.56f, 0.07f),
            hipL = Vec2(0.47f, 0.45f),
            kneeL = Vec2(0.47f, 0.60f),
            ankleL = Vec2(0.47f, 0.75f),
            hipR = Vec2(0.53f, 0.45f),
            kneeR = Vec2(0.53f, 0.60f),
            ankleR = Vec2(0.53f, 0.75f),
            focusPoint = Vec2(0.50f, 0.45f)
        )
        val squatHands = CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.62f, 0.45f),
            neck = Vec2(0.57f, 0.49f),
            shoulderL = Vec2(0.55f, 0.52f),
            elbowL = Vec2(0.58f, 0.65f),
            handL = Vec2(0.60f, 0.78f),
            shoulderR = Vec2(0.57f, 0.52f),
            elbowR = Vec2(0.60f, 0.65f),
            handR = Vec2(0.62f, 0.78f),
            hipL = Vec2(0.41f, 0.64f),
            kneeL = Vec2(0.53f, 0.66f),
            ankleL = Vec2(0.46f, 0.78f),
            hipR = Vec2(0.43f, 0.64f),
            kneeR = Vec2(0.55f, 0.66f),
            ankleR = Vec2(0.48f, 0.78f),
            focusPoint = Vec2(0.43f, 0.64f)
        )
        val plank = posePushUp(id, phase, 0.25f, diamond = false)

        return when {
            phase < 0.25f -> interpolatePose(standJump, squatHands, phase / 0.25f, phase)
            phase < 0.50f -> interpolatePose(squatHands, plank, (phase - 0.25f) / 0.25f, phase)
            phase < 0.75f -> interpolatePose(plank, squatHands, (phase - 0.50f) / 0.25f, phase)
            else -> interpolatePose(squatHands, standJump, (phase - 0.75f) / 0.25f, phase)
        }
    }

    private fun interpolatePose(a: CharacterPose, b: CharacterPose, t: Float, phase: Float): CharacterPose {
        return CharacterPose(
            exerciseId = a.exerciseId,
            phase = phase,
            head = lerpVec(a.head, b.head, t),
            neck = lerpVec(a.neck, b.neck, t),
            shoulderL = lerpVec(a.shoulderL, b.shoulderL, t),
            elbowL = lerpVec(a.elbowL, b.elbowL, t),
            handL = lerpVec(a.handL, b.handL, t),
            shoulderR = lerpVec(a.shoulderR, b.shoulderR, t),
            elbowR = lerpVec(a.elbowR, b.elbowR, t),
            handR = lerpVec(a.handR, b.handR, t),
            hipL = lerpVec(a.hipL, b.hipL, t),
            kneeL = lerpVec(a.kneeL, b.kneeL, t),
            ankleL = lerpVec(a.ankleL, b.ankleL, t),
            hipR = lerpVec(a.hipR, b.hipR, t),
            kneeR = lerpVec(a.kneeR, b.kneeR, t),
            ankleR = lerpVec(a.ankleR, b.ankleR, t),
            focusPoint = lerpVec(a.focusPoint, b.focusPoint, t),
            propType = a.propType
        )
    }

    private fun poseHighKnees(id: String, phase: Float, alt: Float): CharacterPose {
        val leftLift = ((alt + 1f) * 0.5f)
        val rightLift = 1f - leftLift
        val bounce = abs(alt) * 0.02f

        val shY = 0.29f - bounce
        val hipY = 0.49f - bounce

        val kneeL = lerpVec(Vec2(0.46f, 0.65f), Vec2(0.55f, 0.45f), leftLift)
        val ankleL = lerpVec(Vec2(0.45f, 0.80f), Vec2(0.48f, 0.62f), leftLift)

        val kneeR = lerpVec(Vec2(0.53f, 0.65f), Vec2(0.62f, 0.45f), rightLift)
        val ankleR = lerpVec(Vec2(0.52f, 0.80f), Vec2(0.55f, 0.62f), rightLift)

        // Opposite arm pump
        val elL = lerpVec(Vec2(0.39f, 0.40f), Vec2(0.52f, 0.38f), rightLift)
        val hdL = lerpVec(Vec2(0.37f, 0.48f), Vec2(0.58f, 0.31f), rightLift)

        val elR = lerpVec(Vec2(0.45f, 0.40f), Vec2(0.58f, 0.38f), leftLift)
        val hdR = lerpVec(Vec2(0.43f, 0.48f), Vec2(0.64f, 0.31f), leftLift)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, shY - 0.10f),
            neck = Vec2(0.50f, shY - 0.04f),
            shoulderL = Vec2(0.46f, shY),
            elbowL = elL,
            handL = hdL,
            shoulderR = Vec2(0.52f, shY),
            elbowR = elR,
            handR = hdR,
            hipL = Vec2(0.46f, hipY),
            kneeL = kneeL,
            ankleL = ankleL,
            hipR = Vec2(0.52f, hipY),
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = if (rightLift > 0.5f) kneeR else kneeL,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseBicycleCrunch(id: String, phase: Float, alt: Float): CharacterPose {
        // Supine bicycle crunch: alternating knee-to-elbow pedaling on mat
        val t = (alt + 1f) * 0.5f
        val hip = Vec2(0.48f, 0.74f)
        val shoulder = Vec2(0.31f, 0.66f)

        val kneeL = lerpVec(Vec2(0.66f, 0.66f), Vec2(0.44f, 0.54f), t)
        val ankleL = lerpVec(Vec2(0.82f, 0.67f), Vec2(0.56f, 0.57f), t)

        val kneeR = lerpVec(Vec2(0.44f, 0.54f), Vec2(0.66f, 0.66f), t)
        val ankleR = lerpVec(Vec2(0.56f, 0.57f), Vec2(0.82f, 0.67f), t)

        val elbowL = lerpVec(Vec2(0.26f, 0.58f), Vec2(0.41f, 0.55f), 1f - t)
        val elbowR = lerpVec(Vec2(0.26f, 0.58f), Vec2(0.41f, 0.55f), t)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.23f, 0.60f),
            neck = Vec2(0.27f, 0.63f),
            shoulderL = Vec2(shoulder.x, shoulder.y - 0.01f),
            elbowL = elbowL,
            handL = Vec2(0.24f, 0.60f),
            shoulderR = shoulder,
            elbowR = elbowR,
            handR = Vec2(0.25f, 0.60f),
            hipL = hip,
            kneeL = kneeL,
            ankleL = ankleL,
            hipR = hip,
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = Vec2(0.42f, 0.64f),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseDeadBug(id: String, phase: Float, alt: Float): CharacterPose {
        // Supine on mat: arms vertical, knees tabletop; opposite arm & leg extend toward floor
        val leftExt = (alt.coerceAtLeast(0f))
        val rightExt = ((-alt).coerceAtLeast(0f))

        val shoulder = Vec2(0.33f, 0.75f)
        val hip = Vec2(0.52f, 0.75f)

        val elL = lerpVec(Vec2(0.33f, 0.62f), Vec2(0.22f, 0.68f), leftExt)
        val hdL = lerpVec(Vec2(0.33f, 0.49f), Vec2(0.12f, 0.69f), leftExt)

        val elR = lerpVec(Vec2(0.35f, 0.62f), Vec2(0.22f, 0.68f), rightExt)
        val hdR = lerpVec(Vec2(0.35f, 0.49f), Vec2(0.12f, 0.69f), rightExt)

        val kneeL = lerpVec(Vec2(0.52f, 0.58f), Vec2(0.68f, 0.68f), rightExt)
        val ankleL = lerpVec(Vec2(0.66f, 0.58f), Vec2(0.84f, 0.71f), rightExt)

        val kneeR = lerpVec(Vec2(0.54f, 0.58f), Vec2(0.68f, 0.68f), leftExt)
        val ankleR = lerpVec(Vec2(0.68f, 0.58f), Vec2(0.84f, 0.71f), leftExt)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.23f, 0.73f),
            neck = Vec2(0.28f, 0.74f),
            shoulderL = shoulder,
            elbowL = elL,
            handL = hdL,
            shoulderR = Vec2(shoulder.x + 0.01f, shoulder.y),
            elbowR = elR,
            handR = hdR,
            hipL = hip,
            kneeL = kneeL,
            ankleL = ankleL,
            hipR = Vec2(hip.x + 0.01f, hip.y),
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = Vec2(0.43f, 0.72f),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseBirdDog(id: String, phase: Float, alt: Float): CharacterPose {
        // Quadruped on hands and knees: extends opposite arm forward and opposite leg straight back
        val ext = abs(alt)
        val swap = alt < 0f

        val shoulder = Vec2(0.58f, 0.54f)
        val hip = Vec2(0.38f, 0.55f)

        val reachElbow = lerpVec(Vec2(0.58f, 0.66f), Vec2(0.71f, 0.53f), ext)
        val reachHand = lerpVec(Vec2(0.58f, 0.78f), Vec2(0.84f, 0.52f), ext)
        val supportElbow = Vec2(0.58f, 0.66f)
        val supportHand = Vec2(0.58f, 0.78f)

        val reachKnee = lerpVec(Vec2(0.38f, 0.78f), Vec2(0.24f, 0.55f), ext)
        val reachAnkle = lerpVec(Vec2(0.26f, 0.78f), Vec2(0.11f, 0.55f), ext)
        val supportKnee = Vec2(0.38f, 0.78f)
        val supportAnkle = Vec2(0.26f, 0.78f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.69f, 0.50f),
            neck = Vec2(0.63f, 0.52f),
            shoulderL = Vec2(shoulder.x - 0.01f, shoulder.y),
            elbowL = if (swap) reachElbow else supportElbow,
            handL = if (swap) reachHand else supportHand,
            shoulderR = shoulder,
            elbowR = if (swap) supportElbow else reachElbow,
            handR = if (swap) supportHand else reachHand,
            hipL = Vec2(hip.x - 0.01f, hip.y),
            kneeL = if (swap) supportKnee else reachKnee,
            ankleL = if (swap) supportAnkle else reachAnkle,
            hipR = hip,
            kneeR = if (swap) reachKnee else supportKnee,
            ankleR = if (swap) reachAnkle else supportAnkle,
            focusPoint = Vec2(0.48f, 0.54f),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseRussianTwist(id: String, phase: Float, alt: Float): CharacterPose {
        // Seated V-sit on mat: knees bent, heels hovering, clasped hands twist across torso
        val hip = Vec2(0.47f, 0.75f)
        val twistX = alt * 0.12f
        val shCenter = Vec2(0.38f + twistX * 0.25f, 0.55f)
        val handTarget = Vec2(0.46f + twistX, 0.63f + abs(alt) * 0.03f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(shCenter.x - 0.02f, 0.45f),
            neck = Vec2(shCenter.x - 0.01f, 0.51f),
            shoulderL = Vec2(shCenter.x - 0.04f, shCenter.y),
            elbowL = Vec2(shCenter.x - 0.02f + twistX * 0.5f, 0.63f),
            handL = handTarget,
            shoulderR = Vec2(shCenter.x + 0.04f, shCenter.y),
            elbowR = Vec2(shCenter.x + 0.04f + twistX * 0.5f, 0.63f),
            handR = handTarget,
            hipL = Vec2(hip.x - 0.02f, hip.y),
            kneeL = Vec2(0.61f, 0.59f),
            ankleL = Vec2(0.73f, 0.71f),
            hipR = Vec2(hip.x + 0.01f, hip.y),
            kneeR = Vec2(0.63f, 0.61f),
            ankleR = Vec2(0.75f, 0.73f),
            focusPoint = handTarget,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseWallSit(id: String, phase: Float, w: Float): CharacterPose {
        // Back flat against left wall at x=0.33, thighs horizontal, shins vertical
        val pulse = w * 0.015f
        val shX = 0.36f
        val shY = 0.39f + pulse
        val hipX = 0.36f
        val hipY = 0.60f + pulse
        val kneeX = 0.54f
        val kneeY = 0.60f + pulse
        val ankleX = 0.54f
        val ankleY = 0.80f

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(shX + 0.01f, shY - 0.10f),
            neck = Vec2(shX, shY - 0.04f),
            shoulderL = Vec2(shX, shY),
            elbowL = Vec2(0.45f, shY + 0.09f),
            handL = Vec2(0.55f, shY + 0.07f),
            shoulderR = Vec2(shX + 0.01f, shY),
            elbowR = Vec2(0.47f, shY + 0.10f),
            handR = Vec2(0.57f, shY + 0.08f),
            hipL = Vec2(hipX, hipY),
            kneeL = Vec2(kneeX - 0.02f, kneeY),
            ankleL = Vec2(ankleX - 0.02f, ankleY),
            hipR = Vec2(hipX + 0.01f, hipY),
            kneeR = Vec2(kneeX, kneeY),
            ankleR = Vec2(ankleX, ankleY),
            focusPoint = Vec2(kneeX, kneeY),
            propType = ExercisePropType.WALL_LEFT
        )
    }

    private fun poseSuperman(id: String, phase: Float, w: Float): CharacterPose {
        // Prone on floor: lifts arms, chest, and legs into a smooth back-extension arc
        val lift = w * 0.11f
        val hip = Vec2(0.47f, 0.76f)
        val shoulder = Vec2(0.64f, 0.74f - lift * 0.75f)
        val head = Vec2(0.73f, 0.71f - lift * 0.95f)
        val elbow = Vec2(0.75f, 0.73f - lift * 0.95f)
        val hand = Vec2(0.86f, 0.71f - lift * 1.15f)
        val knee = Vec2(0.31f, 0.75f - lift * 0.65f)
        val ankle = Vec2(0.16f, 0.74f - lift * 1.05f)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = head,
            neck = Vec2(0.68f, 0.73f - lift * 0.85f),
            shoulderL = Vec2(shoulder.x - 0.01f, shoulder.y - 0.01f),
            elbowL = Vec2(elbow.x - 0.01f, elbow.y - 0.01f),
            handL = Vec2(hand.x - 0.01f, hand.y - 0.01f),
            shoulderR = shoulder,
            elbowR = elbow,
            handR = hand,
            hipL = Vec2(hip.x - 0.01f, hip.y),
            kneeL = Vec2(knee.x - 0.01f, knee.y - 0.01f),
            ankleL = Vec2(ankle.x - 0.01f, ankle.y - 0.01f),
            hipR = hip,
            kneeR = knee,
            ankleR = ankle,
            focusPoint = hip,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseHipHinge(id: String, phase: Float, w: Float): CharacterPose {
        // Side profile hip hinge: hips push back while flat torso hinges forward
        val hipX = lerp(0.48f, 0.36f, w)
        val hipY = lerp(0.49f, 0.52f, w)
        val shX = lerp(0.49f, 0.61f, w)
        val shY = lerp(0.29f, 0.45f, w)
        val kneeX = lerp(0.49f, 0.46f, w)
        val kneeY = 0.65f
        val ankleX = 0.48f
        val ankleY = 0.80f

        val elX = lerp(0.49f, 0.58f, w)
        val elY = lerp(0.40f, 0.56f, w)
        val hdX = lerp(0.50f, 0.56f, w)
        val hdY = lerp(0.50f, 0.66f, w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(lerp(0.50f, 0.68f, w), lerp(0.19f, 0.41f, w)),
            neck = Vec2(lerp(0.49f, 0.64f, w), lerp(0.25f, 0.43f, w)),
            shoulderL = Vec2(shX - 0.01f, shY),
            elbowL = Vec2(elX - 0.01f, elY),
            handL = Vec2(hdX - 0.01f, hdY),
            shoulderR = Vec2(shX, shY),
            elbowR = Vec2(elX, elY),
            handR = Vec2(hdX, hdY),
            hipL = Vec2(hipX - 0.01f, hipY),
            kneeL = Vec2(kneeX - 0.02f, kneeY),
            ankleL = Vec2(ankleX - 0.02f, ankleY),
            hipR = Vec2(hipX, hipY),
            kneeR = Vec2(kneeX, kneeY),
            ankleR = Vec2(ankleX, ankleY),
            focusPoint = Vec2(hipX, hipY),
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseDonkeyKick(id: String, phase: Float, w: Float): CharacterPose {
        // Quadruped on hands and knees: active right leg kicks bent 90-deg knee toward ceiling
        val shoulder = Vec2(0.60f, 0.55f)
        val hip = Vec2(0.39f, 0.55f)

        val kneeR = lerpVec(Vec2(0.39f, 0.78f), Vec2(0.24f, 0.51f), w)
        val ankleR = lerpVec(Vec2(0.27f, 0.78f), Vec2(0.23f, 0.35f), w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.70f, 0.52f),
            neck = Vec2(0.65f, 0.54f),
            shoulderL = Vec2(shoulder.x - 0.01f, shoulder.y),
            elbowL = Vec2(0.59f, 0.67f),
            handL = Vec2(0.59f, 0.78f),
            shoulderR = shoulder,
            elbowR = Vec2(0.61f, 0.67f),
            handR = Vec2(0.61f, 0.78f),
            hipL = Vec2(hip.x - 0.01f, hip.y),
            kneeL = Vec2(0.39f, 0.78f),
            ankleL = Vec2(0.27f, 0.78f),
            hipR = hip,
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = hip,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseFireHydrant(id: String, phase: Float, w: Float): CharacterPose {
        // Angled quadruped: lifts bent knee laterally outward to hip level
        val shoulder = Vec2(0.58f, 0.54f)
        val hip = Vec2(0.39f, 0.55f)

        val kneeR = lerpVec(Vec2(0.41f, 0.78f), Vec2(0.45f, 0.55f), w)
        val ankleR = lerpVec(Vec2(0.29f, 0.78f), Vec2(0.32f, 0.58f), w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.69f, 0.49f),
            neck = Vec2(0.63f, 0.52f),
            shoulderL = Vec2(shoulder.x - 0.02f, shoulder.y),
            elbowL = Vec2(0.57f, 0.66f),
            handL = Vec2(0.57f, 0.78f),
            shoulderR = shoulder,
            elbowR = Vec2(0.60f, 0.66f),
            handR = Vec2(0.60f, 0.78f),
            hipL = Vec2(hip.x - 0.02f, hip.y),
            kneeL = Vec2(0.38f, 0.78f),
            ankleL = Vec2(0.26f, 0.78f),
            hipR = hip,
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = kneeR,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseStepUp(id: String, phase: Float, w: Float): CharacterPose {
        // Step box in front on the right (x=0.52..0.68, top y=0.68)
        val lift = w * 0.12f
        val hipX = lerp(0.42f, 0.56f, w)
        val hipY = 0.50f - lift
        val shX = hipX + 0.01f
        val shY = 0.30f - lift

        // Lead right foot stays planted on top of the step box at (0.58, 0.68)
        val ankleR = Vec2(0.58f, 0.68f)
        val kneeR = Vec2(lerp(0.57f, 0.58f, w), lerp(0.53f, 0.54f, w))

        // Trail left leg steps up from floor (0.39, 0.80) into a high knee drive on top of box
        val kneeL = lerpVec(Vec2(0.41f, 0.65f), Vec2(0.65f, 0.43f), w)
        val ankleL = lerpVec(Vec2(0.39f, 0.80f), Vec2(0.58f, 0.58f), w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(shX, shY - 0.10f),
            neck = Vec2(shX, shY - 0.04f),
            shoulderL = Vec2(shX - 0.02f, shY),
            elbowL = lerpVec(Vec2(shX - 0.05f, shY + 0.10f), Vec2(shX + 0.05f, shY + 0.08f), w),
            handL = lerpVec(Vec2(shX - 0.02f, shY + 0.18f), Vec2(shX + 0.11f, shY + 0.03f), w),
            shoulderR = Vec2(shX + 0.01f, shY),
            elbowR = lerpVec(Vec2(shX + 0.05f, shY + 0.09f), Vec2(shX - 0.05f, shY + 0.10f), w),
            handR = lerpVec(Vec2(shX + 0.10f, shY + 0.05f), Vec2(shX - 0.03f, shY + 0.17f), w),
            hipL = Vec2(hipX - 0.01f, hipY),
            kneeL = kneeL,
            ankleL = ankleL,
            hipR = Vec2(hipX + 0.01f, hipY),
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = kneeR,
            propType = ExercisePropType.STEP_BOX
        )
    }

    private fun poseLateralRaise(id: String, phase: Float, w: Float): CharacterPose {
        // Front view lateral raise: arms sweep from sides up to horizontal T-position
        val shL = Vec2(0.43f, 0.30f)
        val shR = Vec2(0.57f, 0.30f)

        val elL = lerpVec(Vec2(0.40f, 0.41f), Vec2(0.31f, 0.30f), w)
        val hdL = lerpVec(Vec2(0.39f, 0.52f), Vec2(0.19f, 0.29f), w)

        val elR = lerpVec(Vec2(0.60f, 0.41f), Vec2(0.69f, 0.30f), w)
        val hdR = lerpVec(Vec2(0.61f, 0.52f), Vec2(0.81f, 0.29f), w)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, 0.20f),
            neck = Vec2(0.50f, 0.26f),
            shoulderL = shL,
            elbowL = elL,
            handL = hdL,
            shoulderR = shR,
            elbowR = elR,
            handR = hdR,
            hipL = Vec2(0.46f, 0.50f),
            kneeL = Vec2(0.45f, 0.65f),
            ankleL = Vec2(0.45f, 0.80f),
            hipR = Vec2(0.54f, 0.50f),
            kneeR = Vec2(0.55f, 0.65f),
            ankleR = Vec2(0.55f, 0.80f),
            focusPoint = shR,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseFlutterKicks(id: String, phase: Float, alt: Float): CharacterPose {
        // Supine on mat: straight legs alternate scissoring up and down above floor
        val shoulder = Vec2(0.29f, 0.72f)
        val hip = Vec2(0.48f, 0.75f)

        val leftUp = (alt + 1f) * 0.5f
        val rightUp = 1f - leftUp

        val kneeL = lerpVec(Vec2(0.65f, 0.73f), Vec2(0.63f, 0.61f), leftUp)
        val ankleL = lerpVec(Vec2(0.83f, 0.72f), Vec2(0.79f, 0.52f), leftUp)

        val kneeR = lerpVec(Vec2(0.65f, 0.73f), Vec2(0.63f, 0.61f), rightUp)
        val ankleR = lerpVec(Vec2(0.83f, 0.72f), Vec2(0.79f, 0.52f), rightUp)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.20f, 0.66f),
            neck = Vec2(0.25f, 0.69f),
            shoulderL = shoulder,
            elbowL = Vec2(0.38f, 0.76f),
            handL = Vec2(0.47f, 0.77f),
            shoulderR = shoulder,
            elbowR = Vec2(0.39f, 0.76f),
            handR = Vec2(0.48f, 0.77f),
            hipL = hip,
            kneeL = kneeL,
            ankleL = ankleL,
            hipR = hip,
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = hip,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    private fun poseStandingKneeRaise(id: String, phase: Float, alt: Float): CharacterPose {
        // Controlled standing knee march with hands at chest/temples
        val leftLift = (alt.coerceAtLeast(0f))
        val rightLift = ((-alt).coerceAtLeast(0f))

        val shL = Vec2(0.44f, 0.30f)
        val shR = Vec2(0.56f, 0.30f)

        val kneeL = lerpVec(Vec2(0.46f, 0.65f), Vec2(0.44f, 0.46f), leftLift)
        val ankleL = lerpVec(Vec2(0.46f, 0.80f), Vec2(0.45f, 0.62f), leftLift)

        val kneeR = lerpVec(Vec2(0.54f, 0.65f), Vec2(0.56f, 0.46f), rightLift)
        val ankleR = lerpVec(Vec2(0.54f, 0.80f), Vec2(0.55f, 0.62f), rightLift)

        return CharacterPose(
            exerciseId = id,
            phase = phase,
            head = Vec2(0.50f, 0.20f),
            neck = Vec2(0.50f, 0.26f),
            shoulderL = shL,
            elbowL = Vec2(0.36f, 0.28f),
            handL = Vec2(0.45f, 0.21f),
            shoulderR = shR,
            elbowR = Vec2(0.64f, 0.28f),
            handR = Vec2(0.55f, 0.21f),
            hipL = Vec2(0.46f, 0.50f),
            kneeL = kneeL,
            ankleL = ankleL,
            hipR = Vec2(0.54f, 0.50f),
            kneeR = kneeR,
            ankleR = ankleR,
            focusPoint = if (rightLift >= leftLift) kneeR else kneeL,
            propType = ExercisePropType.FLOOR_MAT
        )
    }

    fun peakPhase(exerciseId: String): Float = when (exerciseId.trim().lowercase()) {
        "push_up", "diamond_push_up", "incline_push_up" -> 0.42f
        "plank", "side_plank", "wall_sit" -> 0.50f
        "mountain_climber", "high_knees", "bicycle_crunch",
        "dead_bug", "bird_dog", "russian_twist",
        "flutter_kicks", "standing_knee_raise" -> 0.22f
        "burpee" -> 0.24f
        else -> 0.50f
    }
}



private enum class PoseViewMode {
    FRONTAL,
    STANDING_RIGHT,
    PRONE_RIGHT,
    SUPINE_LEFT
}

private fun poseViewModeFor(pose: CharacterPose): PoseViewMode {
    return when (pose.exerciseId.lowercase()) {
        "jumping_jack",
        "biceps_curl",
        "shoulder_press",
        "calf_raise",
        "lateral_raise",
        "standing_knee_raise",
        "russian_twist",
        "high_knees" -> PoseViewMode.FRONTAL

        "glute_bridge",
        "bicycle_crunch",
        "dead_bug",
        "flutter_kicks" -> PoseViewMode.SUPINE_LEFT

        "push_up",
        "diamond_push_up",
        "incline_push_up",
        "plank",
        "mountain_climber",
        "bird_dog",
        "donkey_kick",
        "fire_hydrant",
        "superman" -> PoseViewMode.PRONE_RIGHT

        "burpee" -> {
            val dx = pose.shoulderCenter.x - pose.hipCenter.x
            val dy = pose.hipCenter.y - pose.shoulderCenter.y
            if (abs(dx) > abs(dy)) PoseViewMode.PRONE_RIGHT else PoseViewMode.STANDING_RIGHT
        }

        else -> PoseViewMode.STANDING_RIGHT
    }
}

private fun getFormCue(exerciseId: String): String = when (exerciseId.trim().lowercase()) {
    "push_up" -> "LOWER CHEST TO FLOOR • KEEP CORE RIGID"
    "diamond_push_up" -> "CLOSE HAND PLACEMENT • TRICEPS FOCUS"
    "incline_push_up" -> "ELEVATED HANDS • CONTROL DESCENT"
    "squat", "bodyweight_squat" -> "HIPS BACK & DOWN • CHEST UPRIGHT"
    "plank" -> "HOLD SOLID LINE • TUCK PELVIS"
    "side_plank" -> "ALIGN SPINE • LIFT HIPS HIGH"
    "wall_sit" -> "90° KNEE BEND • PRESS BACK FLAT"
    "jumping_jack", "jumping_jacks" -> "ARMS OVERHEAD • SOFT LANDING ON TOES"
    "lunges", "forward_lunges" -> "90° FRONT KNEE • TORSO TALL"
    "reverse_lunge" -> "STEP BACK • STABILIZE HIP & KNEE"
    "mountain_climber", "mountain_climbers" -> "DRIVE KNEES FORWARD • HIPS LEVEL"
    "biceps_curl" -> "ELBOWS PINNED • FULL PEAK SQUEEZE"
    "shoulder_press" -> "DRIVE OVERHEAD • LOCK CORE"
    "triceps_dip" -> "LOWER TO 90° • EXTEND TRICEPS"
    "calf_raise" -> "LIFT ONTO TOES • PAUSE AT PEAK"
    "burpee" -> "DROP FAST • EXPLODE UPWARD"
    "high_knees" -> "DRIVE KNEES HIGH • STAY LIGHT"
    "standing_knee_raise" -> "CONTROLLED MARCH • BRACE CORE"
    "step_up" -> "DRIVE OFF FOOT • SQUEEZE GLUTE"
    "lateral_raise" -> "LIFT TO SHOULDER HEIGHT • NO SWING"
    "hip_hinge" -> "HINGE AT HIPS • FLAT BACK"
    "glute_bridge" -> "DRIVE THROUGH HEELS • GLUTE TENSION"
    "superman" -> "LIFT CHEST & LEGS • SQUEEZE BACK"
    "bird_dog" -> "EXTEND OPPOSITE ARM & LEG • FLAT SPINE"
    "donkey_kick" -> "KICK TO CEILING • ISOLATE GLUTE"
    "fire_hydrant" -> "LIFT LEG LATERALLY • SQUARE HIPS"
    "bicycle_crunch" -> "ELBOW TO OPPOSITE KNEE • ROTATE"
    "dead_bug" -> "PRESS LOW BACK DOWN • ALTERNATE SLOW"
    "russian_twist" -> "LEAN BACK 45° • TORSO ROTATION"
    "flutter_kicks" -> "SHORT FAST KICKS • ABDOMINAL BRACE"
    else -> "MAINTAIN PROPER FORM • BREATHE STEADILY"
}

private fun isHoldExercise(exerciseId: String): Boolean =
    exerciseId.trim().lowercase() in setOf("plank", "side_plank", "wall_sit", "superman")

@Composable
fun AnimatedExerciseCharacter(
    exerciseId: String,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier,
    lightStudio: Boolean = true
) {
    val isHold = remember(exerciseId) { isHoldExercise(exerciseId) }
    val formCue = remember(exerciseId) { getFormCue(exerciseId) }

    val bgBrush = if (lightStudio) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF7FAF9),
                Color(0xFFEAF4F0)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF192636),
                Color(0xFF111B27)
            )
        )
    }

    val model3dPath = remember(exerciseId) {
        when (exerciseId.trim().lowercase()) {
            "jumping_jack" -> "models/jumping_jacks.glb"
            "squat" -> "models/squat.glb"
            "push_up" -> "models/push_up.glb"
            "incline_push_up" -> "models/incline_push_up.glb"
            "diamond_push_up" -> "models/diamond_push_up.glb"
            "plank" -> "models/plank.glb"
            "side_plank" -> "models/side_plank.glb"
            "lunges" -> "models/lunges.glb"
            "reverse_lunge" -> "models/reverse_lunge.glb"
            "mountain_climber" -> "models/mountain_climber.glb"
            "high_knees" -> "models/high_knees.glb"
            "standing_knee_raise" -> "models/standing_knee_raise.glb"
            "glute_bridge" -> "models/glute_bridge.glb"
            "biceps_curl" -> "models/biceps_curl.glb"
            "shoulder_press" -> "models/shoulder_press.glb"
            "lateral_raise" -> "models/lateral_raise.glb"
            "triceps_dip" -> "models/triceps_dip.glb"
            "calf_raise" -> "models/calf_raise.glb"
            "burpee" -> "models/burpee.glb"
            "bicycle_crunch" -> "models/bicycle_crunch.glb"
            "dead_bug" -> "models/dead_bug.glb"
            "bird_dog" -> "models/bird_dog.glb"
            "russian_twist" -> "models/russian_twist.glb"
            "wall_sit" -> "models/wall_sit.glb"
            "superman" -> "models/superman.glb"
            "donkey_kick" -> "models/donkey_kick.glb"
            "fire_hydrant" -> "models/fire_hydrant.glb"
            "hip_hinge" -> "models/hip_hinge.glb"
            "step_up" -> "models/step_up.glb"
            "flutter_kicks" -> "models/flutter_kicks.glb"
            else -> "models/squat.glb"
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Exercise3DScene(
            modelPath = model3dPath,
            isPaused = isPaused,
            lightStudio = lightStudio,
            modifier = Modifier.fillMaxSize()
        )

        Surface(
            color = Color.Black.copy(alpha = 0.52f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isHold) Color(0xFF4DD0E1) else Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHold) "ISOMETRIC HOLD" else "ACTIVE REPS",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = formCue,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

private enum class ExercisePostureType {
    STANDING,
    FLOOR_PRONE,
    FLOOR_SUPINE,
    INCLINE_DIP,
    BURPEE
}

private fun getPostureType(modelPath: String): ExercisePostureType {
    val name = modelPath.substringAfterLast('/').removeSuffix(".glb").lowercase()
    return when (name) {
        "push_up", "diamond_push_up", "incline_push_up", "plank",
        "mountain_climber", "bird_dog", "superman", "donkey_kick", "fire_hydrant" -> ExercisePostureType.FLOOR_PRONE

        "glute_bridge", "bicycle_crunch", "dead_bug", "russian_twist",
        "flutter_kicks", "side_plank" -> ExercisePostureType.FLOOR_SUPINE

        "triceps_dip" -> ExercisePostureType.INCLINE_DIP

        "burpee" -> ExercisePostureType.BURPEE

        else -> ExercisePostureType.STANDING
    }
}

@Composable
fun Exercise3DScene(
    modelPath: String,
    isPaused: Boolean,
    pose: CharacterPose? = null,
    lightStudio: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val engine = remember {
        com.google.android.filament.Filament.init()
        com.google.android.filament.gltfio.Gltfio.init()
        val config = com.google.android.filament.Engine.Config().apply {
            minCommandBufferSizeMB = 64
            commandBufferSizeMB = 128
        }
        com.google.android.filament.Engine.Builder()
            .config(config)
            .build()
    }
    val modelLoader = remember(engine) {
        io.github.sceneview.SceneView.createModelLoader(engine, context)
    }
    val environmentLoader = remember(engine) {
        io.github.sceneview.SceneView.createEnvironmentLoader(engine, context)
    }
    val environment = remember(environmentLoader) {
        io.github.sceneview.SceneView.createEnvironment(environmentLoader, false)
    }
    val cameraNode = remember(engine) {
        io.github.sceneview.SceneView.createCameraNode(engine).apply {
            position = Position(x = 0.0f, y = 0.0f, z = 2.45f)
            lookAt(
                eye = Float3(0f, 0.0f, 2.45f),
                center = Float3(0f, 0.0f, 0f),
                up = Float3(0f, 1f, 0f)
            )
        }
    }
    val mainLightNode = remember(engine) {
        io.github.sceneview.SceneView.createMainLightNode(engine).apply {
            intensity = 120_000f
        }
    }

    var modelNode by remember(modelPath) { mutableStateOf<ModelNode?>(null) }
    var isLoading by remember(modelPath) { mutableStateOf(true) }
    var loadError by remember(modelPath) { mutableStateOf<String?>(null) }

    DisposableEffect(engine) {
        onDispose {
            try {
                modelNode?.destroy()
                modelNode = null
            } catch (t: Throwable) {
                android.util.Log.w("HomeWorkout3D", "Error destroying modelNode", t)
            }
            try {
                cameraNode.destroy()
            } catch (t: Throwable) {
                android.util.Log.w("HomeWorkout3D", "Error destroying cameraNode", t)
            }
            try {
                mainLightNode.destroy()
            } catch (t: Throwable) {
                android.util.Log.w("HomeWorkout3D", "Error destroying mainLightNode", t)
            }
            try {
                modelLoader.destroy()
            } catch (t: Throwable) {
                android.util.Log.w("HomeWorkout3D", "Error destroying modelLoader", t)
            }
            try {
                environmentLoader.destroy()
            } catch (t: Throwable) {
                android.util.Log.w("HomeWorkout3D", "Error destroying environmentLoader", t)
            }
            try {
                if (engine.isValid) {
                    engine.destroy()
                }
            } catch (t: Throwable) {
                android.util.Log.w("HomeWorkout3D", "Error destroying engine", t)
            }
        }
    }

    val posture = remember(modelPath) { getPostureType(modelPath) }
    val (basePosition, baseScale) = remember(posture) {
        when (posture) {
            ExercisePostureType.STANDING -> Position(x = 0.0f, y = -0.05f, z = 0.0f) to 1.35f
            ExercisePostureType.FLOOR_PRONE -> Position(x = 0.0f, y = -0.05f, z = 0.0f) to 1.25f
            ExercisePostureType.FLOOR_SUPINE -> Position(x = 0.0f, y = -0.05f, z = 0.0f) to 1.25f
            ExercisePostureType.INCLINE_DIP -> Position(x = 0.0f, y = -0.05f, z = 0.0f) to 1.30f
            ExercisePostureType.BURPEE -> Position(x = 0.0f, y = -0.05f, z = 0.0f) to 1.25f
        }
    }

    LaunchedEffect(modelPath, modelLoader) {
        modelNode?.let { oldNode ->
            try {
                oldNode.destroy()
            } catch (_: Throwable) {}
            modelNode = null
        }
        isLoading = true
        loadError = null
        try {
            android.util.Log.d("HomeWorkout3D", "Loading 3D model buffer: $modelPath")
            val bytes = withContext(Dispatchers.IO) {
                context.assets.open(modelPath).use { it.readBytes() }
            }
            val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
                rewind()
            }
            val instance = modelLoader.createModelInstance(buffer)
            if (instance != null) {
                val node = ModelNode(
                    modelInstance = instance,
                    autoAnimate = true,
                    scaleToUnits = baseScale,
                    centerOrigin = Float3(0f, 0f, 0f)
                ).apply {
                    position = basePosition
                    playAnimation(0, 1.0f, true)
                }
                modelNode = node
                isLoading = false
                android.util.Log.d("HomeWorkout3D", "3D model loaded successfully: $modelPath (${bytes.size} bytes)")
            } else {
                isLoading = false
                loadError = "Failed to create 3D instance"
                android.util.Log.w("HomeWorkout3D", "createModelInstance returned null for: $modelPath")
            }
        } catch (e: Throwable) {
            isLoading = false
            loadError = e.message ?: "Error loading 3D model"
            android.util.Log.e("HomeWorkout3D", "Failed to load 3D model: $modelPath", e)
        }
    }

    LaunchedEffect(modelNode, isPaused) {
        val node = modelNode ?: return@LaunchedEffect
        if (isPaused) {
            node.setAnimationSpeed(0, 0f)
        } else {
            node.setAnimationSpeed(0, 1.0f)
        }
    }

    val childNodes = remember(modelNode) {
        if (modelNode != null) listOf(modelNode!!) else emptyList()
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
            environment = environment,
            childNodes = childNodes,
            isOpaque = false
        )
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(38.dp),
                    color = HwAccent,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Loading 3D Trainer...",
                    color = HwTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (loadError != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = "Workout Model",
                    tint = HwTextTertiary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "3D Demonstration Loading Failed",
                    color = HwTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (loadError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = loadError ?: "",
                        color = HwTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ExercisePoseThumbnail(
    exerciseId: String,
    modifier: Modifier = Modifier,
    lightStudio: Boolean = true
) {
    val imgRes = ExerciseRepository.imageRes(exerciseId)
    val bgBrush = if (lightStudio) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF7FAF9), Color(0xFFEAF4F0))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF192636), Color(0xFF111B27))
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imgRes),
            contentDescription = exerciseId,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun RestDayRecoveryIllustration(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FAF7)),
        border = BorderStroke(1.dp, Color(0xFFD4E8DF))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.rest_day_recovery),
                contentDescription = "Rest & Recovery",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                color = Color.Black.copy(alpha = 0.58f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF38BDF8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECOVERY DAY • GENTLE MOBILITY & STRETCH",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStudioEnvironment(pose: CharacterPose, lightStudio: Boolean = true) {
    val w = size.width
    val h = size.height

    val ovalColor = if (lightStudio) Color(0xFFDDF1EB) else Color(0xFF213742).copy(alpha = 0.65f)
    drawOval(
        color = ovalColor,
        topLeft = Offset(w * 0.14f, h * 0.12f),
        size = Size(w * 0.72f, h * 0.70f)
    )

    val floorY = h * 0.82f
    val minX = pose.allJoints().minOf { it.x }.coerceIn(0.15f, 0.85f)
    val maxX = pose.allJoints().maxOf { it.x }.coerceIn(0.15f, 0.85f)
    val shadowLeft = (minX - 0.05f) * w
    val shadowWidth = ((maxX - minX) + 0.10f).coerceAtLeast(0.22f) * w
    drawOval(
        color = if (lightStudio) Color(0x180F172A) else Color(0x44000000),
        topLeft = Offset(shadowLeft, floorY - 3f),
        size = Size(shadowWidth, 16f)
    )

    val isFloorExercise = pose.exerciseId.lowercase() in setOf(
        "push_up", "plank", "mountain_climber", "glute_bridge", "bicycle_crunch",
        "dead_bug", "bird_dog", "russian_twist", "side_plank", "incline_push_up",
        "diamond_push_up", "superman", "donkey_kick", "fire_hydrant", "flutter_kicks"
    )
    if (isFloorExercise) {
        val matColor = if (lightStudio) Color(0xFFCBD5E1) else Color(0xFF2B3D50)
        drawRoundRect(
            color = matColor,
            topLeft = Offset(w * 0.14f, floorY - 2f),
            size = Size(w * 0.72f, 6f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    when (pose.propType) {
        ExercisePropType.BENCH_LEFT -> {
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(w * 0.16f, h * 0.57f),
                size = Size(w * 0.23f, h * 0.24f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(w * 0.15f, h * 0.55f),
                size = Size(w * 0.25f, h * 0.035f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
        ExercisePropType.STEP_BOX -> {
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(w * 0.51f, h * 0.69f),
                size = Size(w * 0.22f, h * 0.12f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(w * 0.50f, h * 0.67f),
                size = Size(w * 0.24f, h * 0.03f),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }
        else -> Unit
    }
}

private fun DrawScope.drawCharacterPose(pose: CharacterPose) {
    val w = size.width
    val h = size.height
    val u = minOf(w, h)

    val viewMode = poseViewModeFor(pose)

    val headOff = pose.head.toOffset(w, h)
    val neckOff = pose.neck.toOffset(w, h)
    val shL = pose.shoulderL.toOffset(w, h)
    val elL = pose.elbowL.toOffset(w, h)
    val hdL = pose.handL.toOffset(w, h)
    val shR = pose.shoulderR.toOffset(w, h)
    val elR = pose.elbowR.toOffset(w, h)
    val hdR = pose.handR.toOffset(w, h)
    val hipL = pose.hipL.toOffset(w, h)
    val knL = pose.kneeL.toOffset(w, h)
    val akL = pose.ankleL.toOffset(w, h)
    val hipR = pose.hipR.toOffset(w, h)
    val knR = pose.kneeR.toOffset(w, h)
    val akR = pose.ankleR.toOffset(w, h)
    val shCenter = pose.shoulderCenter.toOffset(w, h)
    val hipCenter = pose.hipCenter.toOffset(w, h)

    val skinLit = Color(0xFFF5D0B8)
    val skinBase = Color(0xFFE5B496)
    val skinMid = Color(0xFFCE9979)
    val skinShadow = Color(0xFFB57D5C)
    val muscleContour = Color(0xFF9E6544)

    val shirtPrimary = Color(0xFF1E293B)
    val shirtHighlight = Color(0xFF334155)
    val shirtMintAccent = Color(0xFF10B981)
    val shortsColor = Color(0xFF0F172A)
    val shortsWaist = Color(0xFF334155)

    val shoeUpper = Color(0xFFFFFFFF)
    val shoeSole = Color(0xFF64748B)
    val shoeMint = Color(0xFF10B981)

    val usesDumbbells = pose.exerciseId.lowercase() in setOf(
        "biceps_curl", "shoulder_press", "lateral_raise"
    )

    // 1. BACK LEG (Left Leg)
    drawMuscularLeg(
        hip = hipL,
        knee = knL,
        ankle = akL,
        unit = u,
        skinBase = skinMid,
        skinHighlight = skinBase,
        skinShadow = skinShadow,
        muscleShade = muscleContour.copy(alpha = 0.35f),
        shortsColor = Color(0xFF0B111A),
        shoeUpper = Color(0xFFCBD5E1),
        shoeSole = Color(0xFF475569),
        shoeMint = Color(0xFF059669),
        viewMode = viewMode
    )

    // 2. BACK ARM (Left Arm)
    drawMuscularArm(
        shoulder = shL,
        elbow = elL,
        hand = hdL,
        unit = u,
        skinBase = skinMid,
        skinHighlight = skinBase,
        skinShadow = skinShadow,
        muscleShade = muscleContour.copy(alpha = 0.35f),
        hasDumbbell = usesDumbbells
    )

    // 3. HUMAN NECK & HEAD
    drawAthleticHead(
        shCenter = shCenter,
        neck = neckOff,
        head = headOff,
        unit = u,
        headRadiusRatio = pose.headRadiusRatio,
        viewMode = viewMode,
        skinLit = skinLit,
        skinBase = skinBase,
        skinShadow = skinMid
    )

    // 4. ATHLETIC COMPRESSION SHIRT & SHORTS
    drawAthleticTorso(
        shCenter = shCenter,
        hipL = hipL,
        hipR = hipR,
        hipCenter = hipCenter,
        neck = neckOff,
        unit = u,
        viewMode = viewMode,
        shirtPrimary = shirtPrimary,
        shirtHighlight = shirtHighlight,
        shirtMintAccent = shirtMintAccent,
        shortsColor = shortsColor,
        shortsWaist = shortsWaist
    )

    // 5. FRONT LEG (Right Leg)
    drawMuscularLeg(
        hip = hipR,
        knee = knR,
        ankle = akR,
        unit = u,
        skinBase = skinBase,
        skinHighlight = skinLit,
        skinShadow = skinMid,
        muscleShade = muscleContour.copy(alpha = 0.50f),
        shortsColor = shortsColor,
        shoeUpper = shoeUpper,
        shoeSole = shoeSole,
        shoeMint = shoeMint,
        viewMode = viewMode
    )

    // 6. FRONT ARM (Right Arm)
    drawMuscularArm(
        shoulder = shR,
        elbow = elR,
        hand = hdR,
        unit = u,
        skinBase = skinBase,
        skinHighlight = skinLit,
        skinShadow = skinMid,
        muscleShade = muscleContour.copy(alpha = 0.52f),
        hasDumbbell = usesDumbbells
    )
}

private fun normalizeOffset(dx: Float, dy: Float): Offset {
    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.0001f)
    return Offset(dx / len, dy / len)
}

private fun DrawScope.drawAthleticTorso(
    shCenter: Offset,
    hipL: Offset,
    hipR: Offset,
    hipCenter: Offset,
    neck: Offset,
    unit: Float,
    viewMode: PoseViewMode,
    shirtPrimary: Color,
    shirtHighlight: Color,
    shirtMintAccent: Color,
    shortsColor: Color,
    shortsWaist: Color
) {
    val spineDx = hipCenter.x - shCenter.x
    val spineDy = hipCenter.y - shCenter.y
    val spineLen = sqrt(spineDx * spineDx + spineDy * spineDy).coerceAtLeast(unit * 0.14f)
    val uVec = Offset(spineDx / spineLen, spineDy / spineLen)
    val rawN = Offset(-uVec.y, uVec.x)

    val nVec = when (viewMode) {
        PoseViewMode.FRONTAL -> if (rawN.x >= 0f) rawN else Offset(-rawN.x, -rawN.y)
        PoseViewMode.STANDING_RIGHT -> if (rawN.x >= 0f) rawN else Offset(-rawN.x, -rawN.y)
        PoseViewMode.PRONE_RIGHT -> if (rawN.y >= 0f) rawN else Offset(-rawN.x, -rawN.y)
        PoseViewMode.SUPINE_LEFT -> if (rawN.y <= 0f) rawN else Offset(-rawN.x, -rawN.y)
    }

    val isFrontal = viewMode == PoseViewMode.FRONTAL
    val chestBackW = if (isFrontal) unit * 0.072f else unit * 0.045f
    val chestFrontW = if (isFrontal) unit * 0.072f else unit * 0.060f
    val waistBackW = if (isFrontal) unit * 0.050f else unit * 0.038f
    val waistFrontW = if (isFrontal) unit * 0.050f else unit * 0.048f
    val hipBackW = if (isFrontal) unit * 0.054f else unit * 0.040f
    val hipFrontW = if (isFrontal) unit * 0.054f else unit * 0.048f

    fun torsoPt(t: Float, nOffset: Float): Offset {
        val cx = shCenter.x + uVec.x * (spineLen * t)
        val cy = shCenter.y + uVec.y * (spineLen * t)
        return Offset(cx + nVec.x * nOffset, cy + nVec.y * nOffset)
    }

    val hipSpan = sqrt((hipR.x - hipL.x) * (hipR.x - hipL.x) + (hipR.y - hipL.y) * (hipR.y - hipL.y))
    val pelvisRadius = (unit * 0.046f + hipSpan * 0.22f).coerceAtMost(unit * 0.060f)
    drawCircle(
        color = shortsColor,
        radius = pelvisRadius,
        center = hipCenter
    )

    val shBack = torsoPt(0.0f, -chestBackW)
    val shFront = torsoPt(0.0f, chestFrontW)
    val latBack = torsoPt(0.30f, -chestBackW * 0.95f)
    val latFront = torsoPt(0.30f, chestFrontW * 1.02f)
    val waistBack = torsoPt(0.68f, -waistBackW)
    val waistFront = torsoPt(0.68f, waistFrontW)
    val hipBack = torsoPt(0.90f, -hipBackW)
    val hipFront = torsoPt(0.90f, hipFrontW)

    val shirtPath = Path().apply {
        moveTo(shBack.x, shBack.y)
        lineTo(shFront.x, shFront.y)
        cubicTo(latFront.x, latFront.y, waistFront.x, waistFront.y, hipFront.x, hipFront.y)
        lineTo(hipBack.x, hipBack.y)
        cubicTo(waistBack.x, waistBack.y, latBack.x, latBack.y, shBack.x, shBack.y)
        close()
    }

    drawPath(
        path = shirtPath,
        brush = Brush.linearGradient(
            colors = listOf(shirtPrimary.copy(alpha = 0.95f), shirtHighlight, shirtPrimary),
            start = torsoPt(0.40f, -chestBackW),
            end = torsoPt(0.40f, chestFrontW)
        )
    )

    val wbTopBack = torsoPt(0.86f, -hipBackW)
    val wbTopFront = torsoPt(0.86f, hipFrontW)
    val wbBotBack = Offset(hipCenter.x - nVec.x * hipBackW, hipCenter.y - nVec.y * hipBackW)
    val wbBotFront = Offset(hipCenter.x + nVec.x * hipFrontW, hipCenter.y + nVec.y * hipFrontW)
    val waistShortsPath = Path().apply {
        moveTo(wbTopBack.x, wbTopBack.y)
        lineTo(wbTopFront.x, wbTopFront.y)
        lineTo(wbBotFront.x, wbBotFront.y)
        lineTo(wbBotBack.x, wbBotBack.y)
        close()
    }
    drawPath(waistShortsPath, color = shortsColor)
    drawLine(
        color = shortsWaist,
        start = wbTopBack,
        end = wbTopFront,
        strokeWidth = (unit * 0.012f).coerceAtLeast(2.4f),
        cap = StrokeCap.Round
    )

    val pSeamTop = torsoPt(0.12f, if (isFrontal) 0f else waistFrontW * 0.20f)
    val pSeamBot = torsoPt(0.84f, if (isFrontal) 0f else waistFrontW * 0.20f)
    drawLine(
        color = shirtMintAccent.copy(alpha = 0.85f),
        start = pSeamTop,
        end = pSeamBot,
        strokeWidth = (unit * 0.0035f).coerceAtLeast(1.2f),
        cap = StrokeCap.Round
    )

    val pecLeft = torsoPt(0.24f, if (isFrontal) -chestFrontW * 0.45f else chestFrontW * 0.15f)
    val pecRight = torsoPt(0.24f, if (isFrontal) chestFrontW * 0.45f else chestFrontW * 0.75f)
    drawLine(
        color = Color.Black.copy(alpha = 0.22f),
        start = pecLeft,
        end = pecRight,
        strokeWidth = (unit * 0.004f).coerceAtLeast(1.5f),
        cap = StrokeCap.Round
    )
}

private fun buildSculptedSegmentPath(
    start: Offset,
    end: Offset,
    rStart: Float,
    rMidAnt: Float,
    rMidPost: Float,
    rEnd: Float,
    antNormal: Offset
): Path {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val pStartAnt = Offset(start.x + antNormal.x * rStart, start.y + antNormal.y * rStart)
    val pStartPost = Offset(start.x - antNormal.x * rStart, start.y - antNormal.y * rStart)
    val pEndAnt = Offset(end.x + antNormal.x * rEnd, end.y + antNormal.y * rEnd)
    val pEndPost = Offset(end.x - antNormal.x * rEnd, end.y - antNormal.y * rEnd)

    val c1Ant = Offset(
        start.x + dx * 0.32f + antNormal.x * rMidAnt,
        start.y + dy * 0.32f + antNormal.y * rMidAnt
    )
    val c2Ant = Offset(
        start.x + dx * 0.68f + antNormal.x * (rMidAnt * 0.85f),
        start.y + dy * 0.68f + antNormal.y * (rMidAnt * 0.85f)
    )

    val c1Post = Offset(
        start.x + dx * 0.68f - antNormal.x * (rMidPost * 0.85f),
        start.y + dy * 0.68f - antNormal.y * (rMidPost * 0.85f)
    )
    val c2Post = Offset(
        start.x + dx * 0.32f - antNormal.x * rMidPost,
        start.y + dy * 0.32f - antNormal.y * rMidPost
    )

    return Path().apply {
        moveTo(pStartAnt.x, pStartAnt.y)
        cubicTo(c1Ant.x, c1Ant.y, c2Ant.x, c2Ant.y, pEndAnt.x, pEndAnt.y)
        lineTo(pEndPost.x, pEndPost.y)
        cubicTo(c1Post.x, c1Post.y, c2Post.x, c2Post.y, pStartPost.x, pStartPost.y)
        close()
    }
}

private fun DrawScope.drawMuscularArm(
    shoulder: Offset,
    elbow: Offset,
    hand: Offset,
    unit: Float,
    skinBase: Color,
    skinHighlight: Color,
    skinShadow: Color,
    muscleShade: Color,
    hasDumbbell: Boolean
) {
    val uDir = normalizeOffset(elbow.x - shoulder.x, elbow.y - shoulder.y)
    val fDir = normalizeOffset(hand.x - elbow.x, hand.y - elbow.y)

    val rawN = Offset(-uDir.y, uDir.x)
    val flexDot = rawN.x * fDir.x + rawN.y * fDir.y
    val bicepsNormal = if (flexDot >= 0f) rawN else Offset(-rawN.x, -rawN.y)

    val alignDot = (uDir.x * fDir.x + uDir.y * fDir.y).coerceIn(-1f, 1f)
    val flexion = ((1f - alignDot) * 0.5f).coerceIn(0f, 1f)
    val bicepsPeak = unit * (0.027f + 0.009f * flexion)
    val tricepsSweep = unit * 0.024f

    val upperArmPath = buildSculptedSegmentPath(
        start = shoulder,
        end = elbow,
        rStart = unit * 0.027f,
        rMidAnt = bicepsPeak,
        rMidPost = tricepsSweep,
        rEnd = unit * 0.020f,
        antNormal = bicepsNormal
    )
    drawPath(
        path = upperArmPath,
        brush = Brush.linearGradient(
            colors = listOf(skinHighlight, skinBase, skinShadow),
            start = Offset(shoulder.x + bicepsNormal.x * bicepsPeak, shoulder.y + bicepsNormal.y * bicepsPeak),
            end = Offset(shoulder.x - bicepsNormal.x * tricepsSweep, shoulder.y - bicepsNormal.y * tricepsSweep)
        )
    )

    val deltRadius = unit * 0.028f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skinHighlight, skinBase),
            center = Offset(shoulder.x - unit * 0.003f, shoulder.y - unit * 0.004f),
            radius = deltRadius * 1.15f
        ),
        radius = deltRadius,
        center = shoulder
    )

    val foreNormal = Offset(-fDir.y, fDir.x)
    val forearmPath = buildSculptedSegmentPath(
        start = elbow,
        end = hand,
        rStart = unit * 0.020f,
        rMidAnt = unit * 0.022f,
        rMidPost = unit * 0.018f,
        rEnd = unit * 0.013f,
        antNormal = foreNormal
    )
    drawPath(
        path = forearmPath,
        brush = Brush.linearGradient(
            colors = listOf(skinHighlight, skinBase, skinShadow),
            start = Offset(elbow.x + foreNormal.x * (unit * 0.022f), elbow.y + foreNormal.y * (unit * 0.022f)),
            end = Offset(elbow.x - foreNormal.x * (unit * 0.022f), elbow.y - foreNormal.y * (unit * 0.022f))
        )
    )

    if (hasDumbbell) {
        val dbAxis = foreNormal
        val plateOffset = unit * 0.024f
        val p1 = Offset(hand.x - dbAxis.x * plateOffset, hand.y - dbAxis.y * plateOffset)
        val p2 = Offset(hand.x + dbAxis.x * plateOffset, hand.y + dbAxis.y * plateOffset)
        drawLine(
            color = Color(0xFF94A3B8),
            start = p1,
            end = p2,
            strokeWidth = (unit * 0.008f).coerceAtLeast(2.4f),
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(p1.x - unit * 0.014f, p1.y - unit * 0.016f),
            size = Size(unit * 0.028f, unit * 0.032f),
            cornerRadius = CornerRadius(unit * 0.006f, unit * 0.006f)
        )
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(p2.x - unit * 0.014f, p2.y - unit * 0.016f),
            size = Size(unit * 0.028f, unit * 0.032f),
            cornerRadius = CornerRadius(unit * 0.006f, unit * 0.006f)
        )
    }

    val handCenter = Offset(hand.x + fDir.x * (unit * 0.005f), hand.y + fDir.y * (unit * 0.005f))
    drawCircle(color = skinBase, radius = unit * 0.015f, center = handCenter)
}

private fun DrawScope.drawMuscularLeg(
    hip: Offset,
    knee: Offset,
    ankle: Offset,
    unit: Float,
    skinBase: Color,
    skinHighlight: Color,
    skinShadow: Color,
    muscleShade: Color,
    shortsColor: Color,
    shoeUpper: Color,
    shoeSole: Color,
    shoeMint: Color,
    viewMode: PoseViewMode
) {
    val tDir = normalizeOffset(knee.x - hip.x, knee.y - hip.y)
    val cDir = normalizeOffset(ankle.x - knee.x, ankle.y - knee.y)

    val rawN = Offset(-tDir.y, tDir.x)
    val quadNormal = when (viewMode) {
        PoseViewMode.PRONE_RIGHT -> if (rawN.y >= 0f) rawN else Offset(-rawN.x, -rawN.y)
        PoseViewMode.SUPINE_LEFT -> if (rawN.y <= 0f) rawN else Offset(-rawN.x, -rawN.y)
        else -> if (rawN.x >= 0f) rawN else Offset(-rawN.x, -rawN.y)
    }

    val thighPath = buildSculptedSegmentPath(
        start = hip,
        end = knee,
        rStart = unit * 0.040f,
        rMidAnt = unit * 0.041f,
        rMidPost = unit * 0.036f,
        rEnd = unit * 0.024f,
        antNormal = quadNormal
    )
    drawPath(
        path = thighPath,
        brush = Brush.linearGradient(
            colors = listOf(skinHighlight, skinBase, skinShadow),
            start = Offset(hip.x + quadNormal.x * (unit * 0.041f), hip.y + quadNormal.y * (unit * 0.041f)),
            end = Offset(hip.x - quadNormal.x * (unit * 0.036f), hip.y - quadNormal.y * (unit * 0.036f))
        )
    )

    val calfRawN = Offset(-cDir.y, cDir.x)
    val shinNormal = if (calfRawN.x * quadNormal.x + calfRawN.y * quadNormal.y >= 0f) {
        calfRawN
    } else {
        Offset(-calfRawN.x, -calfRawN.y)
    }

    val calfPath = buildSculptedSegmentPath(
        start = knee,
        end = ankle,
        rStart = unit * 0.023f,
        rMidAnt = unit * 0.021f,
        rMidPost = unit * 0.030f,
        rEnd = unit * 0.015f,
        antNormal = shinNormal
    )
    drawPath(
        path = calfPath,
        brush = Brush.linearGradient(
            colors = listOf(skinHighlight, skinBase, skinShadow),
            start = Offset(knee.x + shinNormal.x * (unit * 0.023f), knee.y + shinNormal.y * (unit * 0.023f)),
            end = Offset(knee.x - shinNormal.x * (unit * 0.030f), knee.y - shinNormal.y * (unit * 0.030f))
        )
    )

    val shortEnd = Offset(
        hip.x + (knee.x - hip.x) * 0.48f,
        hip.y + (knee.y - hip.y) * 0.48f
    )
    val shortLegPath = buildSculptedSegmentPath(
        start = hip,
        end = shortEnd,
        rStart = unit * 0.042f,
        rMidAnt = unit * 0.043f,
        rMidPost = unit * 0.039f,
        rEnd = unit * 0.037f,
        antNormal = quadNormal
    )
    drawPath(shortLegPath, color = shortsColor)

    drawAthleticSneaker(
        ankle = ankle,
        unit = unit,
        upperColor = shoeUpper,
        soleColor = shoeSole,
        mintAccent = shoeMint,
        viewMode = viewMode
    )
}

private fun DrawScope.drawAthleticSneaker(
    ankle: Offset,
    unit: Float,
    upperColor: Color,
    soleColor: Color,
    mintAccent: Color,
    viewMode: PoseViewMode
) {
    val r = unit * 0.036f
    val toeDir = if (viewMode == PoseViewMode.PRONE_RIGHT) -0.35f else 1.0f

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(ankle.x - r * 0.38f, ankle.y - r * 0.58f),
        size = Size(r * 0.76f, r * 0.40f),
        cornerRadius = CornerRadius(r * 0.18f, r * 0.18f)
    )

    val heelX = ankle.x - r * 0.58f
    val toeX = ankle.x + r * (1.20f + 0.20f * toeDir)
    val topY = ankle.y - r * 0.32f
    val botY = ankle.y + r * 0.54f

    val shoePath = Path().apply {
        moveTo(heelX, topY + r * 0.15f)
        lineTo(ankle.x + r * 0.25f, topY)
        quadraticTo(toeX, topY + r * 0.18f, toeX, botY - r * 0.12f)
        lineTo(heelX - r * 0.05f, botY - r * 0.12f)
        close()
    }
    drawPath(shoePath, color = upperColor)

    drawLine(
        color = mintAccent,
        start = Offset(ankle.x - r * 0.10f, ankle.y + r * 0.05f),
        end = Offset(ankle.x + r * 0.45f, ankle.y + r * 0.12f),
        strokeWidth = (unit * 0.0035f).coerceAtLeast(1.2f),
        cap = StrokeCap.Round
    )

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(heelX - r * 0.06f, botY - r * 0.18f),
        size = Size((toeX - heelX) + r * 0.10f, r * 0.24f),
        cornerRadius = CornerRadius(r * 0.10f, r * 0.10f)
    )
    drawLine(
        color = soleColor,
        start = Offset(heelX, botY + r * 0.03f),
        end = Offset(toeX - r * 0.05f, botY + r * 0.03f),
        strokeWidth = (unit * 0.005f).coerceAtLeast(1.8f),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawAthleticHead(
    shCenter: Offset,
    neck: Offset,
    head: Offset,
    unit: Float,
    headRadiusRatio: Float,
    viewMode: PoseViewMode,
    skinLit: Color,
    skinBase: Color,
    skinShadow: Color
) {
    val neckDir = normalizeOffset(head.x - shCenter.x, head.y - shCenter.y)
    val neckN = Offset(-neckDir.y, neckDir.x)
    val neckPath = buildSculptedSegmentPath(
        start = shCenter,
        end = head,
        rStart = unit * 0.024f,
        rMidAnt = unit * 0.020f,
        rMidPost = unit * 0.020f,
        rEnd = unit * 0.018f,
        antNormal = neckN
    )
    drawPath(neckPath, color = skinShadow)
    drawLine(
        color = skinBase,
        start = shCenter,
        end = neck,
        strokeWidth = unit * 0.028f,
        cap = StrokeCap.Round
    )

    val r = unit * headRadiusRatio * 1.05f
    val up = neckDir
    val rightN = Offset(-up.y, up.x)
    val faceDir = if (rightN.x >= 0f) rightN else Offset(-rightN.x, -rightN.y)
    val isFrontal = viewMode == PoseViewMode.FRONTAL
    val hairColor = Color(0xFF1E2024)

    val hairCrownCenter = Offset(
        head.x + up.x * (r * 0.18f) - faceDir.x * (r * 0.06f),
        head.y + up.y * (r * 0.18f) - faceDir.y * (r * 0.06f)
    )
    drawCircle(
        color = hairColor,
        radius = r * 1.02f,
        center = hairCrownCenter
    )

    val faceCenter = Offset(
        head.x - up.x * (r * 0.06f) + faceDir.x * (r * 0.08f),
        head.y - up.y * (r * 0.06f) + faceDir.y * (r * 0.08f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skinLit, skinBase, skinShadow),
            center = Offset(faceCenter.x + faceDir.x * (r * 0.18f), faceCenter.y + up.y * (r * 0.10f)),
            radius = r * 1.15f
        ),
        radius = r * 0.92f,
        center = faceCenter
    )

    val topHairPath = Path().apply {
        val backNape = Offset(
            head.x - up.x * (r * 0.25f) - faceDir.x * (r * 0.94f),
            head.y - up.y * (r * 0.25f) - faceDir.y * (r * 0.94f)
        )
        val topCrown = Offset(
            head.x + up.x * (r * 1.15f),
            head.y + up.y * (r * 1.15f)
        )
        val frontSwept = Offset(
            head.x + up.x * (r * 0.45f) + faceDir.x * (r * 0.92f),
            head.y + up.y * (r * 0.45f) + faceDir.y * (r * 0.92f)
        )
        val sideburn = Offset(
            head.x + up.x * (r * 0.05f) - faceDir.x * (r * 0.12f),
            head.y + up.y * (r * 0.05f) - faceDir.y * (r * 0.12f)
        )
        moveTo(backNape.x, backNape.y)
        quadraticTo(
            topCrown.x - faceDir.x * (r * 0.55f),
            topCrown.y - faceDir.y * (r * 0.55f),
            topCrown.x + faceDir.x * (r * 0.15f),
            topCrown.y + faceDir.y * (r * 0.15f)
        )
        quadraticTo(
            frontSwept.x - up.x * (r * 0.20f),
            frontSwept.y - up.y * (r * 0.20f),
            frontSwept.x,
            frontSwept.y
        )
        lineTo(sideburn.x, sideburn.y)
        close()
    }
    drawPath(topHairPath, color = hairColor)

    val earCenter = Offset(
        head.x - up.x * (r * 0.08f) - faceDir.x * (r * 0.10f),
        head.y - up.y * (r * 0.08f) - faceDir.y * (r * 0.10f)
    )
    drawCircle(color = skinBase, radius = r * 0.26f, center = earCenter)
}
