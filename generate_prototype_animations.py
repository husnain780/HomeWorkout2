import bpy
import math

def setup_scene():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")
    arm = next(o for o in bpy.data.objects if o.type == 'ARMATURE')
    bpy.context.view_layer.objects.active = arm
    bpy.ops.object.mode_set(mode='POSE')
    for pb in arm.pose.bones:
        pb.rotation_mode = 'XYZ'
        pb.rotation_euler = (0, 0, 0)
        pb.location = (0, 0, 0)
    return arm

# --- 1. JUMPING JACKS ---
arm = setup_scene()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30

act = bpy.data.actions.new(name="Jumping_Jacks")
arm.animation_data_create()
arm.animation_data.action = act

pb_pelvis = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

# Frame 1: Neutral
for frame in [1, 30]:
    scene.frame_set(frame)
    pb_pelvis.location = (0, 0, 0)
    pb_pelvis.keyframe_insert(data_path="location", frame=frame)
    
    pb_l_hip.rotation_euler = (0, 0, math.radians(5))
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=frame)
    pb_r_hip.rotation_euler = (0, 0, math.radians(-5))
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=frame)
    
    pb_l_sh.rotation_euler = (0, 0, math.radians(-75))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=frame)
    pb_r_sh.rotation_euler = (0, 0, math.radians(75))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=frame)
    
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=frame)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=frame)

# Frame 15: Out / Arms Overhead
scene.frame_set(15)
pb_pelvis.location = (0, 0.08, 0)
pb_pelvis.keyframe_insert(data_path="location", frame=15)

pb_l_hip.rotation_euler = (0, 0, math.radians(24))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_hip.rotation_euler = (0, 0, math.radians(-24))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=15)

pb_l_sh.rotation_euler = (0, 0, math.radians(75))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_sh.rotation_euler = (0, 0, math.radians(-75))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=15)

pb_l_el.rotation_euler = (0, 0, math.radians(15))
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_el.rotation_euler = (0, 0, math.radians(-15))
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=15)

bpy.ops.object.mode_set(mode='OBJECT')
bpy.ops.export_scene.gltf(
    filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models\jumping_jacks.glb",
    export_format='GLB',
    export_animations=True,
    export_skins=True
)
print("Exported jumping_jacks.glb successfully!")

# --- 2. SQUATS ---
arm = setup_scene()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30

act = bpy.data.actions.new(name="Squat")
arm.animation_data_create()
arm.animation_data.action = act

pb_pelvis = arm.pose.bones['m_avg_Pelvis']
pb_spine1 = arm.pose.bones['m_avg_Spine1']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']
pb_l_ankle = arm.pose.bones['m_avg_L_Ankle']
pb_r_ankle = arm.pose.bones['m_avg_R_Ankle']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']

# Frame 1 and 40: Standing
for frame in [1, 40]:
    scene.frame_set(frame)
    pb_pelvis.location = (0, 0, 0)
    pb_pelvis.keyframe_insert(data_path="location", frame=frame)
    
    pb_spine1.rotation_euler = (0, 0, 0)
    pb_spine1.keyframe_insert(data_path="rotation_euler", frame=frame)
    
    pb_l_hip.rotation_euler = (0, 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=frame)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=frame)
    
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=frame)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=frame)
    
    pb_l_ankle.rotation_euler = (0, 0, 0)
    pb_l_ankle.keyframe_insert(data_path="rotation_euler", frame=frame)
    pb_r_ankle.rotation_euler = (0, 0, 0)
    pb_r_ankle.keyframe_insert(data_path="rotation_euler", frame=frame)
    
    pb_l_sh.rotation_euler = (0, 0, math.radians(-70))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=frame)
    pb_r_sh.rotation_euler = (0, 0, math.radians(70))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=frame)

# Frame 20: Squat Bottom
scene.frame_set(20)
pb_pelvis.location = (0, -0.38, -0.15)
pb_pelvis.keyframe_insert(data_path="location", frame=20)

pb_spine1.rotation_euler = (math.radians(-25), 0, 0)
pb_spine1.keyframe_insert(data_path="rotation_euler", frame=20)

pb_l_hip.rotation_euler = (math.radians(85), 0, math.radians(10))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_hip.rotation_euler = (math.radians(85), 0, math.radians(-10))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=20)

pb_l_knee.rotation_euler = (math.radians(-95), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_knee.rotation_euler = (math.radians(-95), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=20)

pb_l_ankle.rotation_euler = (math.radians(18), 0, 0)
pb_l_ankle.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_ankle.rotation_euler = (math.radians(18), 0, 0)
pb_r_ankle.keyframe_insert(data_path="rotation_euler", frame=20)

# Arms forward for balance
pb_l_sh.rotation_euler = (math.radians(80), 0, math.radians(-10))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_sh.rotation_euler = (math.radians(80), 0, math.radians(10))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=20)

bpy.ops.object.mode_set(mode='OBJECT')
bpy.ops.export_scene.gltf(
    filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models\squat.glb",
    export_format='GLB',
    export_animations=True,
    export_skins=True
)
print("Exported squat.glb successfully!")
