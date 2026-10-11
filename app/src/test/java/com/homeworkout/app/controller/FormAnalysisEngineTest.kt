package com.homeworkout.app.controller

import com.google.mlkit.vision.pose.PoseLandmark
import com.homeworkout.app.model.PosePoint
import org.junit.Assert.assertEquals
import org.junit.Test

class FormAnalysisEngineTest {

    private fun point(
        type: Int,
        x: Float,
        y: Float
    ): PosePoint {
        return PosePoint(
            type = type,
            x = x,
            y = y,
            confidence = 1f
        )
    }

    private fun standingSquatPoints():
        List<PosePoint> {

        return listOf(
            point(
                PoseLandmark.LEFT_HIP,
                0f,
                0f
            ),
            point(
                PoseLandmark.LEFT_KNEE,
                0f,
                1f
            ),
            point(
                PoseLandmark.LEFT_ANKLE,
                0f,
                2f
            ),
            point(
                PoseLandmark.RIGHT_HIP,
                2f,
                0f
            ),
            point(
                PoseLandmark.RIGHT_KNEE,
                2f,
                1f
            ),
            point(
                PoseLandmark.RIGHT_ANKLE,
                2f,
                2f
            ),
            point(
                PoseLandmark.LEFT_SHOULDER,
                0f,
                -1f
            ),
            point(
                PoseLandmark.RIGHT_SHOULDER,
                2f,
                -1f
            )
        )
    }

    private fun bottomSquatPoints():
        List<PosePoint> {

        return listOf(
            point(
                PoseLandmark.LEFT_HIP,
                0f,
                0f
            ),
            point(
                PoseLandmark.LEFT_KNEE,
                0f,
                1f
            ),
            point(
                PoseLandmark.LEFT_ANKLE,
                1f,
                1f
            ),
            point(
                PoseLandmark.RIGHT_HIP,
                2f,
                0f
            ),
            point(
                PoseLandmark.RIGHT_KNEE,
                2f,
                1f
            ),
            point(
                PoseLandmark.RIGHT_ANKLE,
                1f,
                1f
            ),
            point(
                PoseLandmark.LEFT_SHOULDER,
                0f,
                -1f
            ),
            point(
                PoseLandmark.RIGHT_SHOULDER,
                2f,
                -1f
            )
        )
    }

    @Test
    fun noPose_returnsZeroReps() {

        val engine =
            FormAnalysisEngine()

        val result =
            engine.analyze(
                emptyList(),
                "Squat"
            )

        assertEquals(
            0,
            result.reps
        )
    }

    @Test
    fun squatDownThenUp_countsOneRep() {

        val engine =
            FormAnalysisEngine()

        engine.analyze(
            standingSquatPoints(),
            "Squat"
        )

        engine.analyze(
            bottomSquatPoints(),
            "Squat"
        )

        val result =
            engine.analyze(
                standingSquatPoints(),
                "Squat"
            )

        assertEquals(
            1,
            result.reps
        )
    }
}
