import bpy
import math
import os

ASSETS_DIR = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models"
FBX_PATH = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx"
REPORT_DIR = r"C:\Users\husnain_fazal\.gemini\antigravity\brain\633e00ca-c742-440d-be4e-10649744348d"

def reset_armature():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.ops.import_scene.fbx(filepath=FBX_PATH)
    arm = next(o for o in bpy.data.objects if o.type == 'ARMATURE')
    arm.location = (0, 0, 0)
    bpy.context.view_layer.update()
    
    # Configure stylish athletic materials with high visibility
    for mat in bpy.data.materials:
        bsdf = next((n for n in mat.node_tree.nodes if n.type == 'BSDF_PRINCIPLED'), None) if mat.node_tree else None
        if bsdf:
            if 'Body' in mat.name or 'Surface' in mat.name:
                # Mint/cyan athletic suit
                bsdf.inputs['Base Color'].default_value = (0.06, 0.76, 0.58, 1.0)
                bsdf.inputs['Roughness'].default_value = 0.35
            elif 'Joints' in mat.name:
                # Athletic slate-navy undersuit with visible brightness
                bsdf.inputs['Base Color'].default_value = (0.12, 0.18, 0.28, 1.0)
                bsdf.inputs['Roughness'].default_value = 0.35

    bpy.context.view_layer.objects.active = arm
    bpy.ops.object.mode_set(mode='POSE')
    for pb in arm.pose.bones:
        pb.rotation_mode = 'XYZ'
        pb.rotation_euler = (0, 0, 0)
        pb.location = (0, 0, 0)
    return arm

def export_model(filename):
    bpy.ops.object.mode_set(mode='OBJECT')
    out_path = os.path.join(ASSETS_DIR, filename)
    bpy.ops.export_scene.gltf(
        filepath=out_path,
        export_format='GLB',
        export_animations=True,
        export_skins=True
    )
    print(f"Exported {filename} -> {out_path} ({os.path.getsize(out_path)} bytes)")

def setup_render_scene(cam_pos=(0, -3.2, 0.95), cam_rot=(90, 0, 0)):
    cam_data = bpy.data.cameras.new(name='Cam')
    cam_obj = bpy.data.objects.new('Cam', cam_data)
    bpy.context.collection.objects.link(cam_obj)
    cam_obj.location = cam_pos
    cam_obj.rotation_euler = tuple(math.radians(a) for a in cam_rot)
    bpy.context.scene.camera = cam_obj

    light_data = bpy.data.lights.new(name='KeyLight', type='POINT')
    light_data.energy = 700
    light_obj = bpy.data.objects.new('KeyLight', light_data)
    bpy.context.collection.objects.link(light_obj)
    light_obj.location = (0.8, -2.2, 1.6)

    light_data2 = bpy.data.lights.new(name='FillLight', type='POINT')
    light_data2.energy = 450
    light_obj2 = bpy.data.objects.new('FillLight', light_data2)
    bpy.context.collection.objects.link(light_obj2)
    light_obj2.location = (-1.4, -2.0, 0.8)

    bpy.context.scene.render.resolution_x = 400
    bpy.context.scene.render.resolution_y = 600

# ==============================================================================
# 1. SQUAT
# ==============================================================================
print("Generating Squat...")
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Squat")
arm.animation_data_create()
arm.animation_data.action = act

pb_p = arm.pose.bones['m_avg_Pelvis']
pb_s1 = arm.pose.bones['m_avg_Spine1']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']
pb_l_ank = arm.pose.bones['m_avg_L_Ankle']
pb_r_ank = arm.pose.bones['m_avg_R_Ankle']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']

for f in [1, 40]:
    pb_p.location = (0, 0, 0)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_s1.rotation_euler = (0, 0, 0)
    pb_s1.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_hip.rotation_euler = (0, 0, math.radians(-6))
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, math.radians(6))
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_ank.rotation_euler = (0, 0, 0)
    pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_ank.rotation_euler = (0, 0, 0)
    pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_sh.rotation_euler = (0, 0, math.radians(-70))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(70))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)

# Deep squat at mid-cycle (frame 20)
f = 20
pb_p.location = (0, -0.40, -0.22)
pb_p.keyframe_insert(data_path="location", frame=f)
pb_s1.rotation_euler = (math.radians(22), 0, 0)
pb_s1.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_hip.rotation_euler = (math.radians(-82), 0, math.radians(-10))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_hip.rotation_euler = (math.radians(-82), 0, math.radians(10))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_knee.rotation_euler = (math.radians(95), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_knee.rotation_euler = (math.radians(95), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_ank.rotation_euler = (math.radians(-18), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_ank.rotation_euler = (math.radians(-18), 0, 0)
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)
# Arms extended horizontally forward
pb_l_sh.rotation_euler = (0, math.radians(-85), math.radians(-25))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_sh.rotation_euler = (0, math.radians(85), math.radians(25))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)

setup_render_scene(cam_pos=(1.4, -2.5, 0.9), cam_rot=(85, 0, 28))
scene.frame_set(20)
scene.render.filepath = os.path.join(REPORT_DIR, "preview_squat.png")
bpy.ops.render.render(write_still=True)
export_model("squat.glb")

# ==============================================================================
# 2. JUMPING JACKS
# ==============================================================================
print("Generating Jumping Jacks...")
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30
act = bpy.data.actions.new(name="Jumping_Jacks")
arm.animation_data_create()
arm.animation_data.action = act

pb_p = arm.pose.bones['m_avg_Pelvis']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']
pb_l_ank = arm.pose.bones['m_avg_L_Ankle']
pb_r_ank = arm.pose.bones['m_avg_R_Ankle']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

for f in [1, 30]:
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
    pb_l_ank.rotation_euler = (0, 0, 0)
    pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_ank.rotation_euler = (0, 0, 0)
    pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_sh.rotation_euler = (0, 0, math.radians(-72))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(72))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

# Peak open position at frame 15
f = 15
pb_p.location = (0, 0.08, 0)
pb_p.keyframe_insert(data_path="location", frame=f)
pb_l_hip.rotation_euler = (0, 0, math.radians(18))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_hip.rotation_euler = (0, 0, math.radians(-18))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_knee.rotation_euler = (0, 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_knee.rotation_euler = (0, 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_ank.rotation_euler = (0, 0, math.radians(-5))
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_ank.rotation_euler = (0, 0, math.radians(5))
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_sh.rotation_euler = (0, 0, math.radians(72))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_sh.rotation_euler = (0, 0, math.radians(-72))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_el.rotation_euler = (0, 0, math.radians(-12))
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_el.rotation_euler = (0, 0, math.radians(12))
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

setup_render_scene(cam_pos=(0, -3.2, 0.95), cam_rot=(90, 0, 0))
scene.frame_set(15)
scene.render.filepath = os.path.join(REPORT_DIR, "preview_jumping_jacks.png")
bpy.ops.render.render(write_still=True)
export_model("jumping_jacks.glb")

# ==============================================================================
# 3. PUSH UP
# ==============================================================================
print("Generating Push Up...")
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Push_Up")
arm.animation_data_create()
arm.animation_data.action = act

pb_p = arm.pose.bones['m_avg_Pelvis']
pb_s1 = arm.pose.bones['m_avg_Spine1']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']
pb_l_ank = arm.pose.bones['m_avg_L_Ankle']
pb_r_ank = arm.pose.bones['m_avg_R_Ankle']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']
pb_l_el = arm.pose.bones['m_avg_L_Elbow']
pb_r_el = arm.pose.bones['m_avg_R_Elbow']

# Top plank position (frames 1 and 40)
for f in [1, 40]:
    pb_p.rotation_euler = (math.radians(82), 0, 0)
    pb_p.location = (0, -0.65, 0.10)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_s1.rotation_euler = (0, 0, 0)
    pb_s1.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_hip.rotation_euler = (0, 0, math.radians(-4))
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, math.radians(4))
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_ank.rotation_euler = (math.radians(-65), 0, 0)
    pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_ank.rotation_euler = (math.radians(-65), 0, 0)
    pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    # Arms straight down supporting chest
    pb_l_sh.rotation_euler = (math.radians(20), math.radians(-70), math.radians(-25))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(20), math.radians(70), math.radians(25))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

# Bottom push-up position (frame 20): chest to floor, elbows bent
f = 20
pb_p.location = (0, -0.85, 0.10)
pb_p.keyframe_insert(data_path="location", frame=f)
pb_l_ank.rotation_euler = (math.radians(-75), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_ank.rotation_euler = (math.radians(-75), 0, 0)
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_sh.rotation_euler = (math.radians(35), math.radians(-45), math.radians(-40))
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_sh.rotation_euler = (math.radians(35), math.radians(45), math.radians(40))
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_el.rotation_euler = (0, math.radians(-85), 0)
pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_el.rotation_euler = (0, math.radians(85), 0)
pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

setup_render_scene(cam_pos=(2.2, -3.0, 1.1), cam_rot=(75, 0, 35))
scene.frame_set(20)
scene.render.filepath = os.path.join(REPORT_DIR, "preview_push_up.png")
bpy.ops.render.render(write_still=True)
export_model("push_up.glb")

# ==============================================================================
# 4. MOUNTAIN CLIMBERS
# ==============================================================================
print("Generating Mountain Climbers...")
arm = reset_armature()
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 40
scene.render.fps = 30
act = bpy.data.actions.new(name="Mountain_Climbers")
arm.animation_data_create()
arm.animation_data.action = act

pb_p = arm.pose.bones['m_avg_Pelvis']
pb_s1 = arm.pose.bones['m_avg_Spine1']
pb_l_hip = arm.pose.bones['m_avg_L_Hip']
pb_r_hip = arm.pose.bones['m_avg_R_Hip']
pb_l_knee = arm.pose.bones['m_avg_L_Knee']
pb_r_knee = arm.pose.bones['m_avg_R_Knee']
pb_l_ank = arm.pose.bones['m_avg_L_Ankle']
pb_r_ank = arm.pose.bones['m_avg_R_Ankle']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']

# Base stable high plank setup for frames 1, 20, 40
for f in [1, 20, 40]:
    pb_p.rotation_euler = (math.radians(82), 0, 0)
    pb_p.location = (0, -0.65, 0.10)
    pb_p.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_p.keyframe_insert(data_path="location", frame=f)
    pb_s1.rotation_euler = (0, 0, 0)
    pb_s1.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_sh.rotation_euler = (math.radians(20), math.radians(-70), math.radians(-25))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (math.radians(20), math.radians(70), math.radians(25))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)

for f in [1, 40]:
    pb_l_hip.rotation_euler = (0, 0, math.radians(-4))
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, math.radians(4))
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_knee.rotation_euler = (0, 0, 0)
    pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_knee.rotation_euler = (0, 0, 0)
    pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_ank.rotation_euler = (math.radians(-65), 0, 0)
    pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_ank.rotation_euler = (math.radians(-65), 0, 0)
    pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)

# Frame 10: Left knee drives forward under chest
f = 10
pb_l_hip.rotation_euler = (math.radians(-75), 0, math.radians(-10))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_knee.rotation_euler = (math.radians(95), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_ank.rotation_euler = (math.radians(-15), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)

pb_r_hip.rotation_euler = (0, 0, math.radians(4))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_knee.rotation_euler = (0, 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_ank.rotation_euler = (math.radians(-65), 0, 0)
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)

# Frame 20: Left leg back
f = 20
pb_l_hip.rotation_euler = (0, 0, math.radians(-4))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_knee.rotation_euler = (0, 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_ank.rotation_euler = (math.radians(-65), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)

# Frame 30: Right knee drives forward under chest
f = 30
pb_r_hip.rotation_euler = (math.radians(-75), 0, math.radians(10))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_knee.rotation_euler = (math.radians(95), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_r_ank.rotation_euler = (math.radians(-15), 0, 0)
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=f)

pb_l_hip.rotation_euler = (0, 0, math.radians(-4))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_knee.rotation_euler = (0, 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=f)
pb_l_ank.rotation_euler = (math.radians(-65), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=f)

setup_render_scene(cam_pos=(2.2, -3.0, 1.1), cam_rot=(75, 0, 35))
scene.frame_set(10)
scene.render.filepath = os.path.join(REPORT_DIR, "preview_mountain_climbers.png")
bpy.ops.render.render(write_still=True)
export_model("mountain_climber.glb")

print("ALL 4 CORE EXERCISES SUCCESSFULLY GENERATED!")
