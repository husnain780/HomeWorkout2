import bpy
import math
import os

ASSETS_DIR = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models"
FBX_PATH = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx"

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
                bsdf.inputs['Base Color'].default_value = (0.06, 0.76, 0.58, 1.0)
                bsdf.inputs['Roughness'].default_value = 0.35
            elif 'Joints' in mat.name:
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
    print(f"Exported {filename} -> {os.path.getsize(out_path)} bytes")

def create_action(arm, name, frames=40):
    scene = bpy.context.scene
    scene.frame_start = 1
    scene.frame_end = frames
    scene.render.fps = 30
    act = bpy.data.actions.new(name=name)
    arm.animation_data_create()
    arm.animation_data.action = act
    return act

def kf(pb, channel, frame, val):
    if channel == 'loc':
        pb.location = val
        pb.keyframe_insert(data_path="location", frame=frame)
    elif channel == 'rot':
        pb.rotation_euler = tuple(math.radians(a) for a in val)
        pb.keyframe_insert(data_path="rotation_euler", frame=frame)

def build_plank():
    arm = reset_armature()
    create_action(arm, "Plank", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    # Static anchor points on extremities
    for f in [1, 20, 40]:
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(la, 'rot', f, (-65, 0, 0))
        kf(ra, 'rot', f, (-65, 0, 0))

    # Dynamic breathing / core bracing cycle across frames 1, 20, 40
    # Frame 1 & 40: Base stable plank
    kf(p, 'loc', 1, (0, -0.64, 0.10))
    kf(p, 'rot', 1, (82, 0, 0))
    kf(s1, 'rot', 1, (0, 0, 0))
    kf(ls, 'rot', 1, (20, -70, -25))
    kf(rs, 'rot', 1, (20, 70, 25))

    # Frame 20: Core engagement pulse & diaphragm breath
    kf(p, 'loc', 20, (0, -0.66, 0.10))
    kf(p, 'rot', 20, (80, 0, 0))
    kf(s1, 'rot', 20, (4.5, 0, 0))
    kf(ls, 'rot', 20, (23, -73, -23))
    kf(rs, 'rot', 20, (23, 73, 23))

    kf(p, 'loc', 40, (0, -0.64, 0.10))
    kf(p, 'rot', 40, (82, 0, 0))
    kf(s1, 'rot', 40, (0, 0, 0))
    kf(ls, 'rot', 40, (20, -70, -25))
    kf(rs, 'rot', 40, (20, 70, 25))

    export_model("plank.glb")

def build_side_plank():
    arm = reset_armature()
    create_action(arm, "Side_Plank", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        kf(lh, 'rot', f, (0, 0, -3))
        kf(rh, 'rot', f, (0, 0, 3))

    # Frames 1 & 40: Base side plank hold
    kf(p, 'rot', 1, (82, 90, 0))
    kf(p, 'loc', 1, (0, -0.66, 0.10))
    kf(s1, 'rot', 1, (0, 0, 0))
    kf(ls, 'rot', 1, (0, -75, -25))
    kf(rs, 'rot', 1, (0, 0, 70))

    # Frame 20: Active hip pulsing & lateral core lift
    kf(p, 'rot', 20, (84, 90, 0))
    kf(p, 'loc', 20, (0, -0.61, 0.10))
    kf(s1, 'rot', 20, (0, 0, 4.0))
    kf(ls, 'rot', 20, (0, -78, -22))
    kf(rs, 'rot', 20, (0, 5, 80))

    kf(p, 'rot', 40, (82, 90, 0))
    kf(p, 'loc', 40, (0, -0.66, 0.10))
    kf(s1, 'rot', 40, (0, 0, 0))
    kf(ls, 'rot', 40, (0, -75, -25))
    kf(rs, 'rot', 40, (0, 0, 70))

    export_model("side_plank.glb")

def build_wall_sit():
    arm = reset_armature()
    create_action(arm, "Wall_Sit", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        kf(la, 'rot', f, (-15, 0, 0))
        kf(ra, 'rot', f, (-15, 0, 0))

    # Frames 1 & 40: Solid 90 degree wall sit
    kf(p, 'loc', 1, (0, -0.41, -0.20))
    kf(s1, 'rot', 1, (0, 0, 0))
    kf(lh, 'rot', 1, (-85, 0, -6))
    kf(rh, 'rot', 1, (-85, 0, 6))
    kf(lk, 'rot', 1, (88, 0, 0))
    kf(rk, 'rot', 1, (88, 0, 0))
    kf(ls, 'rot', 1, (0, -35, -50))
    kf(rs, 'rot', 1, (0, 35, 50))

    # Frame 20: Athletic isometric quad tension pulse & deep breathing
    kf(p, 'loc', 20, (0, -0.44, -0.20))
    kf(s1, 'rot', 20, (-3.5, 0, 0))
    kf(lh, 'rot', 20, (-88, 0, -6))
    kf(rh, 'rot', 20, (-88, 0, 6))
    kf(lk, 'rot', 20, (94, 0, 0))
    kf(rk, 'rot', 20, (94, 0, 0))
    kf(ls, 'rot', 20, (0, -38, -48))
    kf(rs, 'rot', 20, (0, 38, 48))

    kf(p, 'loc', 40, (0, -0.41, -0.20))
    kf(s1, 'rot', 40, (0, 0, 0))
    kf(lh, 'rot', 40, (-85, 0, -6))
    kf(rh, 'rot', 40, (-85, 0, 6))
    kf(lk, 'rot', 40, (88, 0, 0))
    kf(rk, 'rot', 40, (88, 0, 0))
    kf(ls, 'rot', 40, (0, -35, -50))
    kf(rs, 'rot', 40, (0, 35, 50))

    export_model("wall_sit.glb")

if __name__ == "__main__":
    build_plank()
    build_side_plank()
    build_wall_sit()
    print("ALL ISOMETRIC ANIMATIONS GENERATED SUCCESSFULLY!")
