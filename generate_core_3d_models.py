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
                    # Athletic mint/cyan body suit: RGB (0.05, 0.75, 0.58)
                    bsdf.inputs['Base Color'].default_value = (0.06, 0.76, 0.58, 1.0)
                    bsdf.inputs['Roughness'].default_value = 0.35
                elif 'Joints' in mat.name:
                    # Deep athletic navy: RGB (0.08, 0.12, 0.20)
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

# 1. SQUAT
arm = reset_armature()
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
pb_l_ank = arm.pose.bones['m_avg_L_Ankle']
pb_r_ank = arm.pose.bones['m_avg_R_Ankle']
pb_l_sh = arm.pose.bones['m_avg_L_Shoulder']
pb_r_sh = arm.pose.bones['m_avg_R_Shoulder']

for f in [1, 40]:
    scene.frame_set(f)
    pb_pelvis.location = (0, 0, 0)
    pb_pelvis.keyframe_insert(data_path="location", frame=f)
    pb_spine1.rotation_euler = (0, 0, 0)
    pb_spine1.keyframe_insert(data_path="rotation_euler", frame=f)
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
    pb_l_sh.rotation_euler = (0, 0, math.radians(-75))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(75))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)

scene.frame_set(20)
# Deep squat at frame 20
pb_pelvis.location = (0, -0.25, -0.42)
pb_pelvis.keyframe_insert(data_path="location", frame=20)
pb_spine1.rotation_euler = (math.radians(22), 0, 0)
pb_spine1.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_hip.rotation_euler = (math.radians(-75), 0, math.radians(10))
pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_hip.rotation_euler = (math.radians(-75), 0, math.radians(-10))
pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_knee.rotation_euler = (math.radians(95), 0, 0)
pb_l_knee.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_knee.rotation_euler = (math.radians(95), 0, 0)
pb_r_knee.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_ank.rotation_euler = (math.radians(-25), 0, 0)
pb_l_ank.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_ank.rotation_euler = (math.radians(-25), 0, 0)
pb_r_ank.keyframe_insert(data_path="rotation_euler", frame=20)
pb_l_sh.rotation_euler = (math.radians(80), 0, 0)
pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=20)
pb_r_sh.rotation_euler = (math.radians(80), 0, 0)
pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=20)
export_model("squat.glb")

# 2. JUMPING JACKS
arm = reset_armature()
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

for f in [1, 30]:
    scene.frame_set(f)
    pb_pelvis.location = (0, 0, 0)
    pb_pelvis.keyframe_insert(data_path="location", frame=f)
    pb_l_hip.rotation_euler = (0, 0, math.radians(5))
    pb_l_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_hip.rotation_euler = (0, 0, math.radians(-5))
    pb_r_hip.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_sh.rotation_euler = (0, 0, math.radians(-75))
    pb_l_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_sh.rotation_euler = (0, 0, math.radians(75))
    pb_r_sh.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_l_el.rotation_euler = (0, 0, 0)
    pb_l_el.keyframe_insert(data_path="rotation_euler", frame=f)
    pb_r_el.rotation_euler = (0, 0, 0)
    pb_r_el.keyframe_insert(data_path="rotation_euler", frame=f)

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
export_model("jumping_jacks.glb")
