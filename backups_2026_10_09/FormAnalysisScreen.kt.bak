package com.homeworkout.app.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.homeworkout.app.controller.FormAnalysisEngine
import com.homeworkout.app.model.FormAnalysisState
import com.homeworkout.app.model.PosePoint
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText

@Composable
fun FormAnalysisScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraSelector by remember {
        mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    var selectedExercise by remember {
        mutableStateOf("Squat")
    }

    var posePoints by remember {
        mutableStateOf<List<PosePoint>>(emptyList())
    }

    var analysis by remember {
        mutableStateOf(
            FormAnalysisState()
        )
    }

    val engine = remember {
        FormAnalysisEngine()
    }

    val cameraController = remember {
        LifecycleCameraController(context)
    }

    val poseDetector = remember {

        PoseDetection.getClient(
            PoseDetectorOptions.Builder()
                .setDetectorMode(
                    PoseDetectorOptions.STREAM_MODE
                )
                .build()
        )
    }

    LaunchedEffect(selectedExercise) {

        engine.reset()

        analysis =
            FormAnalysisState(
                exercise = selectedExercise
            )
    }

    LaunchedEffect(
        hasCameraPermission,
        cameraSelector
    ) {

        if (hasCameraPermission) {

            cameraController.cameraSelector =
                cameraSelector

            cameraController.setImageAnalysisBackpressureStrategy(
                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            )

            cameraController.bindToLifecycle(
                lifecycleOwner
            )
        }
    }

    DisposableEffect(hasCameraPermission) {

        if (hasCameraPermission) {

            val executor =
                ContextCompat.getMainExecutor(context)

            cameraController.setImageAnalysisAnalyzer(
                executor,
                MlKitAnalyzer(
                    listOf(poseDetector),
                    CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    executor
                ) { result: MlKitAnalyzer.Result? ->

                    val pose =
                        result?.getValue(
                            poseDetector
                        )

                    if (pose == null) {

                        posePoints = emptyList()

                        analysis =
                            engine.analyze(
                                points = emptyList(),
                                exercise = selectedExercise
                            )

                    } else {

                        val landmarks =
                            pose.getAllPoseLandmarks()

                        val points =
                            landmarks.map { landmark ->

                                PosePoint(
                                    type = landmark.landmarkType,
                                    x = landmark.position.x,
                                    y = landmark.position.y,
                                    confidence = landmark.inFrameLikelihood
                                )
                            }

                        posePoints = points

                        analysis =
                            engine.analyze(
                                points = points,
                                exercise = selectedExercise
                            )
                    }
                }
            )
        }

        onDispose {

            cameraController.clearImageAnalysisAnalyzer()

            cameraController.unbind()

            poseDetector.close()
        }
    }

    if (!hasCameraPermission) {

        PermissionScreen(
            onBack = onBack,
            onAllowCamera = {
                permissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        )

    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
        ) {

            FormHeader(
                onBack = onBack,
                exercise = selectedExercise
            )

            CameraAnalysisPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraController = cameraController,
                posePoints = posePoints,
                analysis = analysis
            )

            AnalysisControls(
                selectedExercise = selectedExercise,
                analysis = analysis,
                onSelectExercise = {
                    selectedExercise = it
                },
                onSwitchCamera = {
                    cameraSelector =
                        if (
                            cameraSelector ==
                            CameraSelector.DEFAULT_BACK_CAMERA
                        ) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                },
                onDone = onBack
            )
        }
    }
}

@Composable
private fun FormHeader(
    onBack: () -> Unit,
    exercise: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                horizontal = 18.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onBack),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(
                1.dp,
                BorderSoft
            ),
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    color = BrandNavy,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "FORM ANALYSIS",
                color = BrandGreenDark,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )

            Text(
                text = "Live coaching",
                color = BrandNavy,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            BrandGreen,
                            CircleShape
                        )
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = "LIVE",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun CameraAnalysisPanel(
    modifier: Modifier,
    cameraController: LifecycleCameraController,
    posePoints: List<PosePoint>,
    analysis: FormAnalysisState
) {

    Box(
        modifier = modifier
            .padding(
                horizontal = 14.dp
            )
            .clip(
                RoundedCornerShape(27.dp)
            )
            .background(Color.Black)
    ) {

        AndroidView(
            factory = { ctx ->

                PreviewView(ctx).apply {

                    scaleType =
                        PreviewView.ScaleType.FILL_CENTER

                    implementationMode =
                        PreviewView.ImplementationMode.COMPATIBLE

                    controller =
                        cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val map =
                posePoints.associateBy {
                    it.type
                }

            val connections =
                listOf(
                    PoseLandmark.NOSE to
                        PoseLandmark.LEFT_SHOULDER,

                    PoseLandmark.NOSE to
                        PoseLandmark.RIGHT_SHOULDER,

                    PoseLandmark.LEFT_SHOULDER to
                        PoseLandmark.RIGHT_SHOULDER,

                    PoseLandmark.LEFT_SHOULDER to
                        PoseLandmark.LEFT_ELBOW,

                    PoseLandmark.LEFT_ELBOW to
                        PoseLandmark.LEFT_WRIST,

                    PoseLandmark.RIGHT_SHOULDER to
                        PoseLandmark.RIGHT_ELBOW,

                    PoseLandmark.RIGHT_ELBOW to
                        PoseLandmark.RIGHT_WRIST,

                    PoseLandmark.LEFT_SHOULDER to
                        PoseLandmark.LEFT_HIP,

                    PoseLandmark.RIGHT_SHOULDER to
                        PoseLandmark.RIGHT_HIP,

                    PoseLandmark.LEFT_HIP to
                        PoseLandmark.RIGHT_HIP,

                    PoseLandmark.LEFT_HIP to
                        PoseLandmark.LEFT_KNEE,

                    PoseLandmark.LEFT_KNEE to
                        PoseLandmark.LEFT_ANKLE,

                    PoseLandmark.RIGHT_HIP to
                        PoseLandmark.RIGHT_KNEE,

                    PoseLandmark.RIGHT_KNEE to
                        PoseLandmark.RIGHT_ANKLE
                )

            connections.forEach { connection ->

                val first =
                    map[connection.first]

                val second =
                    map[connection.second]

                if (
                    first != null &&
                    second != null &&
                    first.confidence > 0.5f &&
                    second.confidence > 0.5f
                ) {

                    drawLine(
                        color = BrandGreen,
                        start = Offset(
                            first.x,
                            first.y
                        ),
                        end = Offset(
                            second.x,
                            second.y
                        ),
                        strokeWidth = 6f
                    )
                }
            }

            posePoints.forEach { point ->

                if (point.confidence > 0.5f) {

                    drawCircle(
                        color = Color.White,
                        radius = 9f,
                        center = Offset(
                            point.x,
                            point.y
                        )
                    )

                    drawCircle(
                        color = BrandGreen,
                        radius = 5f,
                        center = Offset(
                            point.x,
                            point.y
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.58f),
                            Color.Transparent
                        )
                    )
                )
                .padding(
                    15.dp
                )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = BrandGreen
                ) {

                    Text(
                        text = analysis.exercise.uppercase(),
                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 7.dp
                        ),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = Color.Black.copy(alpha = 0.48f)
                ) {

                    Text(
                        text = "POSE TRACKING",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 7.dp
                        ),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(
                    alpha = 0.64f
                )
            )
        ) {

            Column(
                modifier = Modifier.padding(
                    horizontal = 13.dp,
                    vertical = 11.dp
                ),
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "REP COUNT",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp
                )

                Text(
                    text = analysis.reps.toString(),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(
                    alpha = 0.94f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                Text(
                    text = "FORM FEEDBACK",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = analysis.feedback,
                    color = BrandNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Score",
                        color = BrandMuted,
                        fontSize = 9.sp
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Text(
                        text = "${analysis.score}/100",
                        color = BrandGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text(
                        text = "Phase",
                        color = BrandMuted,
                        fontSize = 9.sp
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Text(
                        text = analysis.phase,
                        color = BrandNavy,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisControls(
    selectedExercise: String,
    analysis: FormAnalysisState,
    onSelectExercise: (String) -> Unit,
    onSwitchCamera: () -> Unit,
    onDone: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 11.dp
            ),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "TRAINING EXERCISE",
                    color = BrandMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Choose what you're performing",
                    color = BrandText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            ExerciseSelector(
                title = "Squat",
                selected = selectedExercise == "Squat",
                modifier = Modifier.weight(1f),
                onClick = {
                    onSelectExercise("Squat")
                }
            )

            ExerciseSelector(
                title = "Push Up",
                selected = selectedExercise == "Push Up",
                modifier = Modifier.weight(1f),
                onClick = {
                    onSelectExercise("Push Up")
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            MetricCard(
                title = "KNEE L",
                value = "${analysis.leftKneeAngle.toInt()}°",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "KNEE R",
                value = "${analysis.rightKneeAngle.toInt()}°",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "ELBOW L",
                value = "${analysis.leftElbowAngle.toInt()}°",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "ELBOW R",
                value = "${analysis.rightElbowAngle.toInt()}°",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            OutlinedButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    BorderSoft
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BrandNavy
                )
            ) {

                Text(
                    text = "Switch Camera",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White
                )
            ) {

                Text(
                    text = "Done",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            color = BrandGreenSoft
        ) {

            Text(
                text = "Pose tracking uses visible landmarks for workout guidance. It is not a medical assessment.",
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
                color = BrandGreenDark,
                fontSize = 8.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun ExerciseSelector(
    title: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) {
            BrandGreenSoft
        } else {
            Color.White
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                BrandGreen
            } else {
                BorderSoft
            }
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 13.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (selected) {
                            BrandGreen
                        } else {
                            BorderSoft
                        },
                        CircleShape
                    )
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = title,
                color = if (selected) {
                    BrandGreenDark
                } else {
                    BrandNavy
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            BorderSoft
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 8.dp
            )
        ) {

            Text(
                text = title,
                color = BrandMuted,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = value,
                color = BrandNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun PermissionScreen(
    onBack: () -> Unit,
    onAllowCamera: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = BrandGreenSoft
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "CAM",
                    color = BrandGreenDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = "Camera access required",
            color = BrandNavy,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = "HomeWorkout uses your camera for real-time pose and form analysis.",
            modifier = Modifier.fillMaxWidth(),
            color = BrandMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onAllowCamera,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(17.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen
            )
        ) {

            Text(
                text = "Allow Camera",
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(
            modifier = Modifier.height(9.dp)
        )

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(17.dp),
            border = BorderStroke(
                1.dp,
                BorderSoft
            )
        ) {

            Text(
                text = "Go Back",
                color = BrandNavy,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
