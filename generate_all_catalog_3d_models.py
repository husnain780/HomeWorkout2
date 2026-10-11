import bpy
import math

def reset_armature():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")
    arm = next(o for o in bpy.data.objects if o.type == 'ARMATURE')
    arm.location = (0, 0, 0)
    bpy.context.view_layer.update()
    
    # Configure stylish athletic materials
    for mat in bpy.data.materials:
        if mat.use_nodes:
            bsdf = next((n for n in mat.node_tree.nodes if n.type == 'BSDF_PRINCIPLED'), None)
            if bsdf:
                if 'Body' in mat.name or 'Surface' in mat.name:
                    bsdf.inputs['Base Color'].default_value = (0.06, 0.76, 0.58, 1.0)
                    bsdf.inputs['Roughness'].default_value = 0.35
                elif 'Joints' in mat.name:
                    bsdf.inputs['Base Color'].default_value = (0.08, 0.12, 0.20, 1.0)
                    bsdf.inputs['Roughness'].default_value = 0.40

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
scene.render.fps = 30
act = bpy.data.actions.new(name="Push_Up")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-80), 0, 0)
    pb_p.location = (0, -0.65, 0.45)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
pb_p.location = (0, -0.85, 0.45)
pb_p.keyframe_insert(data_path="location", frame=20)
pb_l_sh.rotation_euler = (math.radians(45), 0, math.radians(-25))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_sh.rotation_euler = (math.radians(45), 0, math.radians(25))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_el.rotation_euler = (math.radians(85), 0, 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_el.rotation_euler = (math.radians(85), 0, 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("push_up.glb")

# 2. INCLINE PUSH UP
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Incline_Push_Up")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-55), 0, 0)
    pb_p.location = (0, -0.45, 0.55)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(65), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(65), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
pb_p.location = (0, -0.65, 0.55)
pb_p.keyframe_insert(data_path="location", frame=20)
pb_l_sh.rotation_euler = (math.radians(35), 0, math.radians(-25))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_sh.rotation_euler = (math.radians(35), 0, math.radians(25))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_el.rotation_euler = (math.radians(80), 0, 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_el.rotation_euler = (math.radians(80), 0, 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("incline_push_up.glb")

# 3. DIAMOND PUSH UP
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Diamond_Push_Up")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-80), 0, 0)
    pb_p.location = (0, -0.65, 0.45)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(75), 0, math.radians(-15))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(75), 0, math.radians(15))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
pb_p.location = (0, -0.85, 0.45)
pb_p.keyframe_insert(data_path="location", frame=20)
pb_l_sh.rotation_euler = (math.radians(45), 0, math.radians(-10))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_sh.rotation_euler = (math.radians(45), 0, math.radians(10))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_el.rotation_euler = (math.radians(95), 0, 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_el.rotation_euler = (math.radians(95), 0, 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("diamond_push_up.glb")

# 4. PLANK
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30
act = bpy.data.actions.new(name="Plank")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 30]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-82), 0, 0)
    pb_p.location = (0, -0.78, 0.40)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(85), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(85), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (math.radians(85), 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (math.radians(85), 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)
export_model("plank.glb")

# 5. SIDE PLANK
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30
act = bpy.data.actions.new(name="Side_Plank")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']

for f in [1, 30]:
    scene.frame_set(f)
    pb_p.rotation_euler = (0, math.radians(75), 0)
    pb_p.location = (0, -0.65, 0.45)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    # Right arm supporting on floor
    pb_r_sh.rotation_euler = (0, 0, math.radians(-75))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    # Left arm raised upward
    pb_l_sh.rotation_euler = (0, 0, math.radians(85))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
export_model("side_plank.glb")

# 6. LUNGES
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Lunges")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.location = (0, 0, 0)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (0, 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_sh.rotation_euler = (0, 0, math.radians(-70))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(70))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
# Step forward left leg, drop right knee
pb_p.location = (0, -0.22, -0.32)
pb_p.keyframe_insert(data_path="location", frame=20)
pb_l_hip.rotation_euler = (math.radians(-70), 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_knee.rotation_euler = (math.radians(85), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_hip.rotation_euler = (math.radians(35), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_knee.rotation_euler = (math.radians(80), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("lunges.glb")

# 7. REVERSE LUNGE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Reverse_Lunge")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.location = (0, 0, 0)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (0, 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
pb_p.location = (0, 0.18, -0.32)
pb_p.keyframe_insert(data_path="location", frame=20)
pb_l_hip.rotation_euler = (math.radians(35), 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_knee.rotation_euler = (math.radians(80), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_hip.rotation_euler = (math.radians(-70), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_knee.rotation_euler = (math.radians(85), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("reverse_lunge.glb")

# 8. BICEPS CURL
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
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

# 9. SHOULDER PRESS
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
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

# 10. LATERAL RAISE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Lateral_Raise")
arm.animation_data_create()
arm.animation_data.action = act
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']

for f in [1, 36]:
    scene.frame_set(f)
    pb_l_sh.rotation_euler = (0, 0, math.radians(-75))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(75))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_l_sh.rotation_euler = (0, 0, 0)
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_sh.rotation_euler = (0, 0, 0)
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("lateral_raise.glb")

# 11. CALF RAISE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 32
scene.render.fps = 30
act = bpy.data.actions.new(name="Calf_Raise")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_ank = arm.pose.bones['m_avg_L_Ankle']
pb_r_ank = arm.pose.bones['m_avg_R_Ankle']

for f in [1, 32]:
    scene.frame_set(f)
    pb_p.location = (0, 0, 0)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_ank.rotation_euler = (0, 0, 0)
    pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_ank.rotation_euler = (0, 0, 0)
    pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(16)
pb_p.location = (0, 0.12, 0)
pb_p.keyframe_insert(data_path="location", frame=16)
pb_l_ank.rotation_euler = (math.radians(-35), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=16)
pb_r_ank.rotation_euler = (math.radians(-35), 0, 0)
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=16)
export_model("calf_raise.glb")

# 12. HIGH KNEES
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30
act = bpy.data.actions.new(name="High_Knees")
arm.animation_data_create()
arm.animation_data.action = act
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

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

# 13. STANDING KNEE RAISE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 32
scene.render.fps = 30
act = bpy.data.actions.new(name="Standing_Knee_Raise")
arm.animation_data_create()
arm.animation_data.action = act
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']

for f in [1, 32]:
    scene.frame_set(f)
    pb_l_hip.rotation_euler = (0, 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(16)
pb_l_hip.rotation_euler = (math.radians(95), 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=16)
pb_l_knee.rotation_euler = (math.radians(-95), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=16)
export_model("standing_knee_raise.glb")

# 14. MOUNTAIN CLIMBER
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30
act = bpy.data.actions.new(name="Mountain_Climber")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 30]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-80), 0, 0)
    pb_p.location = (0, -0.65, 0.45)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    # Left knee driven to chest, right leg extended
    pb_l_hip.rotation_euler = (math.radians(-75), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (math.radians(90), 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(15)
pb_l_hip.rotation_euler = (0, 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=15)
pb_l_knee.rotation_euler = (0, 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_hip.rotation_euler = (math.radians(-75), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_knee.rotation_euler = (math.radians(90), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=15)
export_model("mountain_climber.glb")

# 15. GLUTE BRIDGE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Glute_Bridge")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(85), 0, 0)
    pb_p.location = (0, -0.85, 0.20)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (math.radians(-45), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (math.radians(-45), 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (math.radians(80), 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (math.radians(80), 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
# Thrust hips up
pb_p.location = (0, -0.55, 0.35)
pb_p.keyframe_insert(data_path="location", frame=18)
pb_l_hip.rotation_euler = (0, 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_hip.rotation_euler = (0, 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("glute_bridge.glb")

# 16. TRICEPS DIP
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Triceps_Dip")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.location = (0, -0.20, -0.35)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (math.radians(-75), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (math.radians(-75), 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (math.radians(85), 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (math.radians(85), 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_sh.rotation_euler = (math.radians(-25), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(-25), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_p.location = (0, -0.20, -0.55)
pb_p.keyframe_insert(data_path="location", frame=18)
pb_l_el.rotation_euler = (math.radians(-85), 0, 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_el.rotation_euler = (math.radians(-85), 0, 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("triceps_dip.glb")

# 17. BURPEE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 45
scene.render.fps = 30
act = bpy.data.actions.new(name="Burpee")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 45]:
    scene.frame_set(f)
    pb_p.location = (0, 0, 0)
    pb_p.rotation_euler = (0, 0, 0)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)

# Frame 12: Drop to squat
scene.frame_set(12)
pb_p.location = (0, -0.2, -0.4)
pb_p.keyframe_insert(data_path="location", frame=12)

# Frame 22: Plank kick out
scene.frame_set(22)
pb_p.rotation_euler = (math.radians(-80), 0, 0)
pb_p.location = (0, -0.65, 0.45)
pb_p.keyframe_insert(data_path="rotation_euler", frame=22)
pb_p.keyframe_insert(data_path="location", frame=22)

# Frame 34: Jump up
scene.frame_set(34)
pb_p.rotation_euler = (0, 0, 0)
pb_p.location = (0, 0, 0.25)
pb_p.keyframe_insert(data_path="rotation_euler", frame=34)
pb_p.keyframe_insert(data_path="location", frame=34)
pb_l_sh.rotation_euler = (0, 0, math.radians(80))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=34)
pb_r_sh.rotation_euler = (0, 0, math.radians(-80))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=34)
export_model("burpee.glb")

# 18. BICYCLE CRUNCH
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Bicycle_Crunch")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_sp = arm.pose.bones['m_avg_Spine1']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(80), 0, 0)
    pb_p.location = (0, -0.75, 0.25)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_sp.rotation_euler = (0, 0, math.radians(20))
    pb_sp.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_hip.rotation_euler = (math.radians(-75), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_sp.rotation_euler = (0, 0, math.radians(-20))
pb_sp.keyframe_insert(data_path="rotation_euler", frame=18)
pb_l_hip.rotation_euler = (0, 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_hip.rotation_euler = (math.radians(-75), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("bicycle_crunch.glb")

# 19. DEAD BUG
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Dead_Bug")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(85), 0, 0)
    pb_p.location = (0, -0.75, 0.25)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_hip.rotation_euler = (math.radians(-80), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (math.radians(-80), 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
# Contralateral extend: Right arm back, Left leg forward
pb_r_sh.rotation_euler = (math.radians(160), 0, 0)
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_hip.rotation_euler = (math.radians(-20), 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("dead_bug.glb")

# 20. BIRD DOG
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Bird_Dog")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-80), 0, 0)
    pb_p.location = (0, -0.65, 0.45)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(80), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
pb_l_sh.rotation_euler = (math.radians(165), 0, 0)
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_hip.rotation_euler = (math.radians(45), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("bird_dog.glb")

# 21. RUSSIAN TWIST
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Russian_Twist")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_sp = arm.pose.bones['m_avg_Spine1']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(45), 0, 0)
    pb_p.location = (0, -0.35, -0.25)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_sp.rotation_euler = (0, 0, math.radians(35))
    pb_sp.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_sp.rotation_euler = (0, 0, math.radians(-35))
pb_sp.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("russian_twist.glb")

# 22. WALL SIT
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30
act = bpy.data.actions.new(name="Wall_Sit")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 30]:
    scene.frame_set(f)
    pb_p.location = (0, -0.15, -0.42)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (math.radians(-85), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (math.radians(-85), 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (math.radians(90), 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (math.radians(90), 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
export_model("wall_sit.glb")

# 23. SUPERMAN
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Superman")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-85), 0, 0)
    pb_p.location = (0, -0.85, 0.25)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_sh.rotation_euler = (math.radians(160), 0, 0)
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(160), 0, 0)
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_hip.rotation_euler = (0, 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_l_sh.rotation_euler = (math.radians(180), 0, 0)
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_sh.rotation_euler = (math.radians(180), 0, 0)
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=18)
pb_l_hip.rotation_euler = (math.radians(25), 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=18)
pb_r_hip.rotation_euler = (math.radians(25), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("superman.glb")

# 24. DONKEY KICK
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Donkey_Kick")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-80), 0, 0)
    pb_p.location = (0, -0.65, 0.45)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (math.radians(90), 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_r_hip.rotation_euler = (math.radians(50), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("donkey_kick.glb")

# 25. FIRE HYDRANT
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Fire_Hydrant")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(-80), 0, 0)
    pb_p.location = (0, -0.65, 0.45)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_r_hip.rotation_euler = (0, 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (math.radians(90), 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_r_hip.rotation_euler = (0, 0, math.radians(-55))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("fire_hydrant.glb")

# 26. HIP HINGE
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 36
scene.render.fps = 30
act = bpy.data.actions.new(name="Hip_Hinge")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_sp = arm.pose.bones['m_avg_Spine1']

for f in [1, 36]:
    scene.frame_set(f)
    pb_p.location = (0, 0, 0)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_sp.rotation_euler = (0, 0, 0)
    pb_sp.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(18)
pb_p.location = (0, 0.22, -0.15)
pb_p.keyframe_insert(data_path="location", frame=18)
pb_sp.rotation_euler = (math.radians(70), 0, 0)
pb_sp.keyframe_insert(data_path="rotation_euler", frame=18)
export_model("hip_hinge.glb")

# 27. STEP UP
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Step_Up")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']

for f in [1, 40]:
    scene.frame_set(f)
    pb_p.location = (0, 0, 0)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (0, 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
pb_p.location = (0, -0.1, 0.25)
pb_p.keyframe_insert(data_path="location", frame=20)
pb_l_hip.rotation_euler = (math.radians(-75), 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_knee.rotation_euler = (math.radians(85), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("step_up.glb")

# 28. FLUTTER KICKS
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30
act = bpy.data.actions.new(name="Flutter_Kicks")
arm.animation_data_create()
arm.animation_data.action = act
pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']

for f in [1, 30]:
    scene.frame_set(f)
    pb_p.rotation_euler = (math.radians(85), 0, 0)
    pb_p.location = (0, -0.85, 0.25)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (math.radians(-35), 0, 0)
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (math.radians(-10), 0, 0)
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(15)
pb_l_hip.rotation_euler = (math.radians(-10), 0, 0)
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=15)
pb_r_hip.rotation_euler = (math.radians(-35), 0, 0)
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=15)
export_model("flutter_kicks.glb")

print("ALL CATALOG 3D MODELS EXPORTED SUCCESSFULLY!")
