import bpy
import math

def reset_armature():
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

def export_model(filename):
    bpy.ops.object.mode_set(mode='OBJECT')
    bpy.ops.export_scene.gltf(
        filepath=fr"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models\{filename}",
        export_format='GLB',
        export_animations=True,
        export_skins=True
    )
    print(f"Exported {filename} successfully!")

# 1. PUSH UP
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
act = bpy.data.actions.new(name="Push_Up")
arm.animation_data_create()
arm.animation_data.action = act

pb_pelvis = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']

for f in [1, 40]:
    scene.frame_set(f)
    # High plank: rotated 90 deg prone
    pb_pelvis.rotation_euler = (math.radians(-80), 0, 0)
    pb_pelvis.location = (0, -0.65, 0.45)
    pb_pelvis.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_pelvis.keyframe_insert(data_path="location", frame=f)
    
    pb_l_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

# Mid rep: chest down
scene.frame_set(20)
pb_pelvis.location = (0, -0.85, 0.45)
pb_pelvis.keyframe_insert(data_path="location", frame=20)

pb_l_sh.rotation_euler = (math.radians(45), 0, math.radians(-25))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_sh.rotation_euler = (math.radians(45), 0, math.radians(25))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=20)

pb_l_el.rotation_euler = (math.radians(85), 0, 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_el.rotation_euler = (math.radians(85), 0, 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("push_up.glb")

# 2. PLANK
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
act = bpy.data.actions.new(name="Plank")
arm.animation_data_create()
arm.animation_data.action = act

pb_pelvis = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 30]:
    scene.frame_set(f)
    pb_pelvis.rotation_euler = (math.radians(-82), 0, 0)
    pb_pelvis.location = (0, -0.78, 0.40)
    pb_pelvis.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_pelvis.keyframe_insert(data_path="location", frame=f)
    
    pb_l_sh.rotation_euler = (math.radians(85), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(85), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    
    pb_l_el.rotation_euler = (math.radians(85), 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (math.radians(85), 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)
export_model("plank.glb")

# 3. BICEPS CURL
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
act = bpy.data.actions.new(name="Biceps_Curl")
arm.animation_data_create()
arm.animation_data.action = act

pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 36]:
    scene.frame_set(f)
    pb_l_sh.rotation_euler = (0, 0, math.radians(-70))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(70))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_l_el.rotation_euler = (math.radians(120), 0, 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_el.rotation_euler = (math.radians(120), 0, 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("biceps_curl.glb")

# 4. SHOULDER PRESS
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
act = bpy.data.actions.new(name="Shoulder_Press")
arm.animation_data_create()
arm.animation_data.action = act

pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 36]:
    scene.frame_set(f)
    pb_l_sh.rotation_euler = (0, 0, math.radians(20))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(-20))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    
    pb_l_el.rotation_euler = (math.radians(90), 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (math.radians(90), 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_l_sh.rotation_euler = (0, 0, math.radians(85))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_sh.rotation_euler = (0, 0, math.radians(-85))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=18)

pb_l_el.rotation_euler = (0, 0, 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_el.rotation_euler = (0, 0, 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("shoulder_press.glb")

# 5. CALF RAISE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 32
act = bpy.data.actions.new(name="Calf_Raise")
arm.animation_data_create()
arm.animation_data.action = act

pb_pelvis = arm.pose.bones['m_avg_Pelvis']
pb_l_ank = arm.pose.bones['m_avg_L_Ankle']
pb_r_ank = arm.pose.bones['m_avg_R_Ankle']

for f in [1, 32]:
    scene.frame_set(f)
    pb_pelvis.location = (0, 0, 0)
    pb_pelvis.keyframe_insert(data_path="location", frame=f)
    pb_l_ank.rotation_euler = (0, 0, 0)
    pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_ank.rotation_euler = (0, 0, 0)
    pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(16)
pb_pelvis.location = (0, 0.12, 0)
pb_pelvis.keyframe_insert(data_path="location", frame=16)
pb_l_ank.rotation_euler = (math.radians(-35), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=16)
pb_r_ank.rotation_euler = (math.radians(-35), 0, 0)
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=16)
export_model("calf_raise.glb")

# 6. HIGH KNEES
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
act = bpy.data.actions.new(name="High_Knees")
arm.animation_data_create()
arm.animation_data.action = act

pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

# Frame 1 and 30: Left high, Right down
for f in [1, 30]:
    scene.frame_set(f)
    pb_l_hip.rotation_euler = (math.radians(90), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (math.radians(-90), 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)

# Frame 15: Right high, Left down
scene.frame_set(15)
pb_l_hip.rotation_euler = (0, 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=15)
pb_l_knee.rotation_euler = (0, 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_hip.rotation_euler = (math.radians(90), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_knee.rotation_euler = (math.radians(-90), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=15)
export_model("high_knees.glb")
