package com.homeworkout.app.controller

import com.google.mlkit.vision.pose.PoseLandmark
import com.homeworkout.app.model.FormAnalysisState
import com.homeworkout.app.model.PosePoint
import kotlin.math.acos
import kotlin.math.sqrt

class FormAnalysisEngine {

    private var squatPhase = "up"
    private var pushUpPhase = "up"

    private var squatReps = 0
    private var pushUpReps = 0

    private var squatScoreTotal = 0
    private var pushUpScoreTotal = 0

    private var squatFrames = 0
    private var pushUpFrames = 0

    fun reset() {
        squatPhase = "up"
        pushUpPhase = "up"

        squatReps = 0
        pushUpReps = 0

        squatScoreTotal = 0
        pushUpScoreTotal = 0

        squatFrames = 0
        pushUpFrames = 0
    }

    fun analyze(
        points: List<PosePoint>,
        exercise: String
    ): FormAnalysisState {

        val map =
            points.associateBy { it.type }

        val leftKnee =
            angle(
                map,
                PoseLandmark.LEFT_HIP,
                PoseLandmark.LEFT_KNEE,
                PoseLandmark.LEFT_ANKLE
            )

        val rightKnee =
            angle(
                map,
                PoseLandmark.RIGHT_HIP,
                PoseLandmark.RIGHT_KNEE,
                PoseLandmark.RIGHT_ANKLE
            )

        val leftElbow =
            angle(
                map,
                PoseLandmark.LEFT_SHOULDER,
                PoseLandmark.LEFT_ELBOW,
                PoseLandmark.LEFT_WRIST
            )

        val rightElbow =
            angle(
                map,
                PoseLandmark.RIGHT_SHOULDER,
                PoseLandmark.RIGHT_ELBOW,
                PoseLandmark.RIGHT_WRIST
            )

        val detected =
            points.count { it.confidence > 0.5f } >= 8

        if (!detected) {
            return FormAnalysisState(
                exercise = exercise,
                reps =
                    if (exercise == "Squat") {
                        squatReps
                    } else {
                        pushUpReps
                    },
                score =
                    currentScore(exercise),
                feedback =
                    "Move your full body into the camera frame.",
                phase = "Waiting",
                leftKneeAngle = leftKnee,
                rightKneeAngle = rightKnee,
                leftElbowAngle = leftElbow,
                rightElbowAngle = rightElbow,
                isPersonDetected = false
            )
        }

        return if (exercise == "Squat") {
            analyzeSquat(
                leftKnee = leftKnee,
                rightKnee = rightKnee,
                points = map
            )
        } else {
            analyzePushUp(
                leftElbow = leftElbow,
                rightElbow = rightElbow,
                points = map
            )
        }
    }

    private fun analyzeSquat(
        leftKnee: Double,
        rightKnee: Double,
        points: Map<Int, PosePoint>
    ): FormAnalysisState {

        val knee =
            averageAngle(
                leftKnee,
                rightKnee
            )

        val goodDepth =
            knee <= 110.0

        val standing =
            knee >= 160.0

        if (
            squatPhase == "up" &&
            knee < 135.0
        ) {
            squatPhase = "down"
        }

        if (
            squatPhase == "down" &&
            standing
        ) {
            squatReps++
            squatPhase = "up"
        }

        val frameScore =
            when {
                goodDepth -> 100
                knee <= 130.0 -> 85
                knee <= 145.0 -> 70
                else -> 55
            }

        squatScoreTotal += frameScore
        squatFrames++

        val feedback =
            when {
                knee < 90.0 ->
                    "Good depth. Keep your movement controlled."

                knee <= 110.0 ->
                    "Good squat depth. Keep your chest steady."

                knee <= 135.0 ->
                    "Try going a little deeper."

                else ->
                    "Bend your knees more to complete the squat."
            }

        return FormAnalysisState(
            exercise = "Squat",
            reps = squatReps,
            score =
                currentScore("Squat"),
            feedback = feedback,
            phase =
                if (squatPhase == "down") {
                    "Down"
                } else {
                    "Up"
                },
            leftKneeAngle = leftKnee,
            rightKneeAngle = rightKnee,
            leftElbowAngle = 180.0,
            rightElbowAngle = 180.0,
            isPersonDetected = true
        )
    }

    private fun analyzePushUp(
        leftElbow: Double,
        rightElbow: Double,
        points: Map<Int, PosePoint>
    ): FormAnalysisState {

        val elbow =
            averageAngle(
                leftElbow,
                rightElbow
            )

        val down =
            elbow <= 100.0

        val up =
            elbow >= 155.0

        if (
            pushUpPhase == "up" &&
            elbow < 125.0
        ) {
            pushUpPhase = "down"
        }

        if (
            pushUpPhase == "down" &&
            up
        ) {
            pushUpReps++
            pushUpPhase = "up"
        }

        val frameScore =
            when {
                elbow <= 100.0 -> 100
                elbow <= 115.0 -> 90
                elbow <= 135.0 -> 75
                else -> 60
            }

        pushUpScoreTotal += frameScore
        pushUpFrames++

        val feedback =
            when {
                elbow <= 100.0 ->
                    "Good depth. Keep your movement controlled."

                elbow <= 120.0 ->
                    "Almost there. Lower a little further."

                elbow <= 145.0 ->
                    "Lower your body more for the repetition."

                else ->
                    "Bend your elbows and move down."
            }

        return FormAnalysisState(
            exercise = "Push Up",
            reps = pushUpReps,
            score =
                currentScore("Push Up"),
            feedback = feedback,
            phase =
                if (pushUpPhase == "down") {
                    "Down"
                } else {
                    "Up"
                },
            leftKneeAngle = 180.0,
            rightKneeAngle = 180.0,
            leftElbowAngle = leftElbow,
            rightElbowAngle = rightElbow,
            isPersonDetected = true
        )
    }

    private fun currentScore(
        exercise: String
    ): Int {

        return if (exercise == "Squat") {

            if (squatFrames == 0) {
                0
            } else {
                squatScoreTotal /
                    squatFrames
            }

        } else {

            if (pushUpFrames == 0) {
                0
            } else {
                pushUpScoreTotal /
                    pushUpFrames
            }
        }
    }

    private fun averageAngle(
        first: Double,
        second: Double
    ): Double {

        val firstValid =
            first in 0.0..180.0

        val secondValid =
            second in 0.0..180.0

        return when {
            firstValid && secondValid ->
                (first + second) / 2.0

            firstValid ->
                first

            secondValid ->
                second

            else ->
                180.0
        }
    }

    private fun angle(
        map: Map<Int, PosePoint>,
        firstId: Int,
        centerId: Int,
        lastId: Int
    ): Double {

        val first =
            map[firstId]
                ?.takeIf { it.confidence > 0.5f }

        val center =
            map[centerId]
                ?.takeIf { it.confidence > 0.5f }

        val last =
            map[lastId]
                ?.takeIf { it.confidence > 0.5f }

        if (
            first == null ||
            center == null ||
            last == null
        ) {
            return 180.0
        }

        val vectorOneX =
            (first.x - center.x).toDouble()

        val vectorOneY =
            (first.y - center.y).toDouble()

        val vectorTwoX =
            (last.x - center.x).toDouble()

        val vectorTwoY =
            (last.y - center.y).toDouble()

        val dot =
            vectorOneX * vectorTwoX +
                vectorOneY * vectorTwoY

        val magnitudeOne =
            sqrt(
                vectorOneX * vectorOneX +
                    vectorOneY * vectorOneY
            )

        val magnitudeTwo =
            sqrt(
                vectorTwoX * vectorTwoX +
                    vectorTwoY * vectorTwoY
            )

        if (
            magnitudeOne == 0.0 ||
            magnitudeTwo == 0.0
        ) {
            return 180.0
        }

        val cosine =
            (dot /
                (magnitudeOne * magnitudeTwo))
                .coerceIn(-1.0, 1.0)

        return Math.toDegrees(
            acos(cosine)
        )
    }
}
