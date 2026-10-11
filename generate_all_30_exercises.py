import bpy
import math
import os

ASSETS_DIR = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models"
FBX_PATH = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx"

os.makedirs(ASSETS_DIR, exist_ok=True)

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

# ==============================================================================
# BUILDERS FOR ALL 30 EXERCISES
# ==============================================================================

def build_squat():
    arm = reset_armature()
    create_action(arm, "Squat", 40)
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

    for f in [1, 40]:
        kf(p, 'loc', f, (0, 0, 0))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (0, 0, -6))
        kf(rh, 'rot', f, (0, 0, 6))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(la, 'rot', f, (0, 0, 0))
        kf(ra, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    f = 20
    kf(p, 'loc', f, (0, -0.40, -0.22))
    kf(s1, 'rot', f, (22, 0, 0))
    kf(lh, 'rot', f, (-82, 0, -10))
    kf(rh, 'rot', f, (-82, 0, 10))
    kf(lk, 'rot', f, (95, 0, 0))
    kf(rk, 'rot', f, (95, 0, 0))
    kf(la, 'rot', f, (-18, 0, 0))
    kf(ra, 'rot', f, (-18, 0, 0))
    kf(ls, 'rot', f, (0, -85, -25))
    kf(rs, 'rot', f, (0, 85, 25))
    export_model("squat.glb")

def build_jumping_jacks():
    arm = reset_armature()
    create_action(arm, "Jumping_Jacks", 30)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']

    for f in [1, 30]:
        kf(p, 'loc', f, (0, 0, 0))
        kf(lh, 'rot', f, (0, 0, 0))
        kf(rh, 'rot', f, (0, 0, 0))
        kf(la, 'rot', f, (0, 0, 0))
        kf(ra, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -72))
        kf(rs, 'rot', f, (0, 0, 72))
        kf(le, 'rot', f, (0, 0, 0))
        kf(re, 'rot', f, (0, 0, 0))

    f = 15
    kf(p, 'loc', f, (0, 0.08, 0))
    kf(lh, 'rot', f, (0, 0, 18))
    kf(rh, 'rot', f, (0, 0, -18))
    kf(la, 'rot', f, (0, 0, -5))
    kf(ra, 'rot', f, (0, 0, 5))
    kf(ls, 'rot', f, (0, 0, 72))
    kf(rs, 'rot', f, (0, 0, -72))
    kf(le, 'rot', f, (0, 0, -12))
    kf(re, 'rot', f, (0, 0, 12))
    export_model("jumping_jacks.glb")

def build_push_up():
    arm = reset_armature()
    create_action(arm, "Push_Up", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']

    for f in [1, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(p, 'loc', f, (0, -0.65, 0.10))
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(la, 'rot', f, (-65, 0, 0))
        kf(ra, 'rot', f, (-65, 0, 0))
        kf(ls, 'rot', f, (20, -70, -25))
        kf(rs, 'rot', f, (20, 70, 25))
        kf(le, 'rot', f, (0, 0, 0))
        kf(re, 'rot', f, (0, 0, 0))

    f = 20
    kf(p, 'loc', f, (0, -0.85, 0.10))
    kf(la, 'rot', f, (-75, 0, 0))
    kf(ra, 'rot', f, (-75, 0, 0))
    kf(ls, 'rot', f, (35, -45, -40))
    kf(rs, 'rot', f, (35, 45, 40))
    kf(le, 'rot', f, (0, -85, 0))
    kf(re, 'rot', f, (0, 85, 0))
    export_model("push_up.glb")

def build_mountain_climber():
    arm = reset_armature()
    create_action(arm, "Mountain_Climbers", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(p, 'loc', f, (0, -0.65, 0.10))
        kf(ls, 'rot', f, (20, -70, -25))
        kf(rs, 'rot', f, (20, 70, 25))

    for f in [1, 40]:
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(la, 'rot', f, (-65, 0, 0))
        kf(ra, 'rot', f, (-65, 0, 0))

    # Frame 10: Left knee drives forward
    f = 10
    kf(lh, 'rot', f, (-75, 0, -10))
    kf(lk, 'rot', f, (95, 0, 0))
    kf(la, 'rot', f, (-15, 0, 0))
    kf(rh, 'rot', f, (0, 0, 4))
    kf(rk, 'rot', f, (0, 0, 0))
    kf(ra, 'rot', f, (-65, 0, 0))

    # Frame 20: Return to plank
    f = 20
    kf(lh, 'rot', f, (0, 0, -4))
    kf(lk, 'rot', f, (0, 0, 0))
    kf(la, 'rot', f, (-65, 0, 0))

    # Frame 30: Right knee drives forward
    f = 30
    kf(rh, 'rot', f, (-75, 0, 10))
    kf(rk, 'rot', f, (95, 0, 0))
    kf(ra, 'rot', f, (-15, 0, 0))
    kf(lh, 'rot', f, (0, 0, -4))
    kf(lk, 'rot', f, (0, 0, 0))
    kf(la, 'rot', f, (-65, 0, 0))

    export_model("mountain_climber.glb")

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

    # Isometric plank hold with gentle breathing core bracing
    for f in [1, 20, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(la, 'rot', f, (-65, 0, 0))
        kf(ra, 'rot', f, (-65, 0, 0))
        kf(ls, 'rot', f, (20, -70, -25))
        kf(rs, 'rot', f, (20, 70, 25))

    kf(p, 'loc', 1, (0, -0.65, 0.10))
    kf(s1, 'rot', 1, (0, 0, 0))
    kf(p, 'loc', 20, (0, -0.655, 0.10))
    kf(s1, 'rot', 20, (1.5, 0, 0))
    kf(p, 'loc', 40, (0, -0.65, 0.10))
    kf(s1, 'rot', 40, (0, 0, 0))
    export_model("plank.glb")

def build_lunges():
    arm = reset_armature()
    create_action(arm, "Lunges", 40)
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

    # Standing tall at 1 and 40
    for f in [1, 40]:
        kf(p, 'loc', f, (0, 0, 0))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(la, 'rot', f, (0, 0, 0))
        kf(ra, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    # Lunge drop at frame 20: Right leg forward, Left leg back
    f = 20
    kf(p, 'loc', f, (0, -0.35, 0))
    kf(s1, 'rot', f, (6, 0, 0))
    # Right front leg: 90 deg hip, 90 deg knee
    kf(rh, 'rot', f, (-75, 0, 4))
    kf(rk, 'rot', f, (85, 0, 0))
    kf(ra, 'rot', f, (-12, 0, 0))
    # Left back leg: hip extended back, knee hovering
    kf(lh, 'rot', f, (15, 0, -4))
    kf(lk, 'rot', f, (90, 0, 0))
    kf(la, 'rot', f, (-45, 0, 0))
    # Hands placed on hips
    kf(ls, 'rot', f, (0, -35, -50))
    kf(rs, 'rot', f, (0, 35, 50))
    export_model("lunges.glb")

def build_reverse_lunge():
    arm = reset_armature()
    create_action(arm, "Reverse_Lunge", 40)
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

    for f in [1, 40]:
        kf(p, 'loc', f, (0, 0, 0))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(la, 'rot', f, (0, 0, 0))
        kf(ra, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    # Step left leg backward at frame 20
    f = 20
    kf(p, 'loc', f, (0, -0.35, -0.10))
    kf(s1, 'rot', f, (5, 0, 0))
    kf(rh, 'rot', f, (-72, 0, 4))
    kf(rk, 'rot', f, (85, 0, 0))
    kf(ra, 'rot', f, (-12, 0, 0))
    kf(lh, 'rot', f, (20, 0, -4))
    kf(lk, 'rot', f, (90, 0, 0))
    kf(la, 'rot', f, (-50, 0, 0))
    kf(ls, 'rot', f, (0, -35, -50))
    kf(rs, 'rot', f, (0, 35, 50))
    export_model("reverse_lunge.glb")

def build_biceps_curl():
    arm = reset_armature()
    create_action(arm, "Biceps_Curl", 40)
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']

    for f in [1, 40]:
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(ls, 'rot', f, (0, 0, -72))
        kf(rs, 'rot', f, (0, 0, 72))
        kf(le, 'rot', f, (0, 0, 0))
        kf(re, 'rot', f, (0, 0, 0))

    f = 20
    # Elbows curl up to 110 degrees
    kf(ls, 'rot', f, (0, -10, -70))
    kf(rs, 'rot', f, (0, 10, 70))
    kf(le, 'rot', f, (0, math.degrees(-1.8), 0))
    kf(re, 'rot', f, (0, math.degrees(1.8), 0))
    export_model("biceps_curl.glb")

def build_shoulder_press():
    arm = reset_armature()
    create_action(arm, "Shoulder_Press", 40)
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']

    # Racked position at shoulders (frames 1 & 40)
    for f in [1, 40]:
        kf(ls, 'rot', f, (0, -15, 0))
        kf(rs, 'rot', f, (0, 15, 0))
        kf(le, 'rot', f, (0, -85, 0))
        kf(re, 'rot', f, (0, 85, 0))

    # Overhead lockout at frame 20
    f = 20
    kf(ls, 'rot', f, (0, 0, 75))
    kf(rs, 'rot', f, (0, 0, -75))
    kf(le, 'rot', f, (0, 0, -5))
    kf(re, 'rot', f, (0, 0, 5))
    export_model("shoulder_press.glb")

def build_lateral_raise():
    arm = reset_armature()
    create_action(arm, "Lateral_Raise", 40)
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']

    for f in [1, 40]:
        kf(ls, 'rot', f, (0, 0, -72))
        kf(rs, 'rot', f, (0, 0, 72))
        kf(le, 'rot', f, (0, 0, 0))
        kf(re, 'rot', f, (0, 0, 0))

    # Raise arms straight out to sides forming T-pose
    f = 20
    kf(ls, 'rot', f, (0, 0, 0))
    kf(rs, 'rot', f, (0, 0, 0))
    kf(le, 'rot', f, (0, 0, -8))
    kf(re, 'rot', f, (0, 0, 8))
    export_model("lateral_raise.glb")

def build_calf_raise():
    arm = reset_armature()
    create_action(arm, "Calf_Raise", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 40]:
        kf(p, 'loc', f, (0, 0, 0))
        kf(la, 'rot', f, (0, 0, 0))
        kf(ra, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    # Up on tiptoes at frame 20
    f = 20
    kf(p, 'loc', f, (0, 0.08, 0))
    kf(la, 'rot', f, (-42, 0, 0))
    kf(ra, 'rot', f, (-42, 0, 0))
    kf(ls, 'rot', f, (0, 0, -70))
    kf(rs, 'rot', f, (0, 0, 70))
    export_model("calf_raise.glb")

def build_high_knees():
    arm = reset_armature()
    create_action(arm, "High_Knees", 40)
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(la, 'rot', f, (0, 0, 0))
        kf(ra, 'rot', f, (0, 0, 0))

    # Frame 10: Left knee drives high to chest
    f = 10
    kf(lh, 'rot', f, (-85, 0, -4))
    kf(lk, 'rot', f, (95, 0, 0))
    kf(la, 'rot', f, (-15, 0, 0))
    # Right arm pumps forward
    kf(rs, 'rot', f, (0, 45, 45))
    kf(ls, 'rot', f, (0, -25, -60))

    # Frame 30: Right knee drives high to chest
    f = 30
    kf(rh, 'rot', f, (-85, 0, 4))
    kf(rk, 'rot', f, (95, 0, 0))
    kf(ra, 'rot', f, (-15, 0, 0))
    # Left arm pumps forward
    kf(ls, 'rot', f, (0, -45, -45))
    kf(rs, 'rot', f, (0, 25, 60))

    export_model("high_knees.glb")

def build_standing_knee_raise():
    arm = reset_armature()
    create_action(arm, "Standing_Knee_Raise", 40)
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 40]:
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    # Frame 20: Controlled knee raise
    f = 20
    kf(rh, 'rot', f, (-85, 0, 4))
    kf(rk, 'rot', f, (95, 0, 0))
    kf(ls, 'rot', f, (0, -35, -50))
    kf(rs, 'rot', f, (0, 35, 50))
    export_model("standing_knee_raise.glb")

def build_hip_hinge():
    arm = reset_armature()
    create_action(arm, "Hip_Hinge", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 40]:
        kf(p, 'loc', f, (0, 0, 0))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (0, 0, -5))
        kf(rh, 'rot', f, (0, 0, 5))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    # Hinge at hips with flat back at frame 20
    f = 20
    kf(p, 'loc', f, (0, -0.15, -0.28))
    kf(s1, 'rot', f, (38, 0, 0))
    kf(lh, 'rot', f, (-58, 0, -5))
    kf(rh, 'rot', f, (-58, 0, 5))
    kf(lk, 'rot', f, (25, 0, 0))
    kf(rk, 'rot', f, (25, 0, 0))
    kf(ls, 'rot', f, (0, -25, -60))
    kf(rs, 'rot', f, (0, 25, 60))
    export_model("hip_hinge.glb")

def build_glute_bridge():
    arm = reset_armature()
    create_action(arm, "Glute_Bridge", 40)
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

    # Supine position on floor (rot X = -82 deg)
    for f in [1, 40]:
        kf(p, 'rot', f, (-82, 0, 0))
        kf(p, 'loc', f, (0, 0.40, 0.10))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (70, 0, -6))
        kf(rh, 'rot', f, (70, 0, 6))
        kf(lk, 'rot', f, (-85, 0, 0))
        kf(rk, 'rot', f, (-85, 0, 0))
        kf(la, 'rot', f, (18, 0, 0))
        kf(ra, 'rot', f, (18, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    # Bridge drive hips up at frame 20
    f = 20
    kf(p, 'loc', f, (0, 0.65, 0.10))
    kf(lh, 'rot', f, (10, 0, -6))
    kf(rh, 'rot', f, (10, 0, 6))
    kf(lk, 'rot', f, (-75, 0, 0))
    kf(rk, 'rot', f, (-75, 0, 0))
    export_model("glute_bridge.glb")

def build_bicycle_crunch():
    arm = reset_armature()
    create_action(arm, "Bicycle_Crunch", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        kf(p, 'rot', f, (-82, 0, 0))
        kf(p, 'loc', f, (0, 0.40, 0.10))

    # Frame 10: Left elbow to Right knee
    f = 10
    kf(s1, 'rot', f, (15, 0, -22))
    kf(rh, 'rot', f, (75, 0, 6))
    kf(rk, 'rot', f, (-90, 0, 0))
    kf(lh, 'rot', f, (15, 0, -6))
    kf(lk, 'rot', f, (-15, 0, 0))
    kf(ls, 'rot', f, (-25, 45, 45))
    kf(rs, 'rot', f, (0, 0, 70))

    # Frame 30: Right elbow to Left knee
    f = 30
    kf(s1, 'rot', f, (15, 0, 22))
    kf(lh, 'rot', f, (75, 0, -6))
    kf(lk, 'rot', f, (-90, 0, 0))
    kf(rh, 'rot', f, (15, 0, 6))
    kf(rk, 'rot', f, (-15, 0, 0))
    kf(rs, 'rot', f, (-25, -45, -45))
    kf(ls, 'rot', f, (0, 0, -70))

    export_model("bicycle_crunch.glb")

def build_dead_bug():
    arm = reset_armature()
    create_action(arm, "Dead_Bug", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    # Tabletop base
    for f in [1, 20, 40]:
        kf(p, 'rot', f, (-82, 0, 0))
        kf(p, 'loc', f, (0, 0.40, 0.10))
        kf(lh, 'rot', f, (75, 0, -6))
        kf(rh, 'rot', f, (75, 0, 6))
        kf(lk, 'rot', f, (-90, 0, 0))
        kf(rk, 'rot', f, (-90, 0, 0))
        kf(ls, 'rot', f, (0, -85, 0))
        kf(rs, 'rot', f, (0, 85, 0))

    # Frame 10: Right arm back, Left leg extends
    f = 10
    kf(rs, 'rot', f, (0, 0, -75))
    kf(lh, 'rot', f, (10, 0, -6))
    kf(lk, 'rot', f, (-10, 0, 0))

    # Frame 30: Left arm back, Right leg extends
    f = 30
    kf(ls, 'rot', f, (0, 0, 75))
    kf(rh, 'rot', f, (10, 0, 6))
    kf(rk, 'rot', f, (-10, 0, 0))

    export_model("dead_bug.glb")

def build_flutter_kicks():
    arm = reset_armature()
    create_action(arm, "Flutter_Kicks", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        kf(p, 'rot', f, (-82, 0, 0))
        kf(p, 'loc', f, (0, 0.40, 0.10))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    kf(lh, 'rot', 10, (30, 0, -4))
    kf(rh, 'rot', 10, (8, 0, 4))
    kf(lh, 'rot', 30, (8, 0, -4))
    kf(rh, 'rot', 30, (30, 0, 4))
    export_model("flutter_kicks.glb")

def build_russian_twist():
    arm = reset_armature()
    create_action(arm, "Russian_Twist", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        kf(p, 'rot', f, (-45, 0, 0))
        kf(p, 'loc', f, (0, 0.10, 0.10))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (65, 0, -6))
        kf(rh, 'rot', f, (65, 0, 6))
        kf(lk, 'rot', f, (-80, 0, 0))
        kf(rk, 'rot', f, (-80, 0, 0))
        kf(ls, 'rot', f, (0, -60, -20))
        kf(rs, 'rot', f, (0, 60, 20))

    # Twist left at frame 10
    f = 10
    kf(s1, 'rot', f, (0, 0, -32))
    kf(ls, 'rot', f, (0, -75, -45))
    kf(rs, 'rot', f, (0, 40, -10))

    # Twist right at frame 30
    f = 30
    kf(s1, 'rot', f, (0, 0, 32))
    kf(ls, 'rot', f, (0, -40, 10))
    kf(rs, 'rot', f, (0, 75, 45))

    export_model("russian_twist.glb")

def build_superman():
    arm = reset_armature()
    create_action(arm, "Superman", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(p, 'loc', f, (0, -0.85, 0.10))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(ls, 'rot', f, (0, 0, 72))
        kf(rs, 'rot', f, (0, 0, -72))

    # Arch back: chest, arms, and legs lifted
    f = 20
    kf(s1, 'rot', f, (-22, 0, 0))
    kf(lh, 'rot', f, (-18, 0, -4))
    kf(rh, 'rot', f, (-18, 0, 4))
    kf(ls, 'rot', f, (25, 0, 72))
    kf(rs, 'rot', f, (25, 0, -72))
    export_model("superman.glb")

def build_bird_dog():
    arm = reset_armature()
    create_action(arm, "Bird_Dog", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    # Quadruped tabletop
    for f in [1, 20, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(p, 'loc', f, (0, -0.65, 0.10))
        kf(lh, 'rot', f, (-75, 0, -6))
        kf(rh, 'rot', f, (-75, 0, 6))
        kf(lk, 'rot', f, (90, 0, 0))
        kf(rk, 'rot', f, (90, 0, 0))
        kf(ls, 'rot', f, (20, -70, -25))
        kf(rs, 'rot', f, (20, 70, 25))

    # Frame 10: Left arm reach forward, Right leg kick back
    f = 10
    kf(ls, 'rot', f, (0, 0, 75))
    kf(rh, 'rot', f, (0, 0, 4))
    kf(rk, 'rot', f, (0, 0, 0))

    # Frame 30: Right arm reach forward, Left leg kick back
    f = 30
    kf(rs, 'rot', f, (0, 0, -75))
    kf(lh, 'rot', f, (0, 0, -4))
    kf(lk, 'rot', f, (0, 0, 0))

    export_model("bird_dog.glb")

def build_donkey_kick():
    arm = reset_armature()
    create_action(arm, "Donkey_Kick", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(p, 'loc', f, (0, -0.65, 0.10))
        kf(lh, 'rot', f, (-75, 0, -6))
        kf(rh, 'rot', f, (-75, 0, 6))
        kf(lk, 'rot', f, (90, 0, 0))
        kf(rk, 'rot', f, (90, 0, 0))
        kf(ls, 'rot', f, (20, -70, -25))
        kf(rs, 'rot', f, (20, 70, 25))

    # Frame 20: Right leg drives heel to ceiling
    f = 20
    kf(rh, 'rot', f, (25, 0, 6))
    kf(rk, 'rot', f, (95, 0, 0))
    export_model("donkey_kick.glb")

def build_fire_hydrant():
    arm = reset_armature()
    create_action(arm, "Fire_Hydrant", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(p, 'loc', f, (0, -0.65, 0.10))
        kf(lh, 'rot', f, (-75, 0, -6))
        kf(rh, 'rot', f, (-75, 0, 6))
        kf(lk, 'rot', f, (90, 0, 0))
        kf(rk, 'rot', f, (90, 0, 0))
        kf(ls, 'rot', f, (20, -70, -25))
        kf(rs, 'rot', f, (20, 70, 25))

    # Frame 20: Right leg abducts outward to side
    f = 20
    kf(rh, 'rot', f, (-75, 0, 48))
    kf(rk, 'rot', f, (90, 0, 0))
    export_model("fire_hydrant.glb")

def build_side_plank():
    arm = reset_armature()
    create_action(arm, "Side_Plank", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 20, 40]:
        # Side-facing rotation
        kf(p, 'rot', f, (82, 90, 0))
        kf(p, 'loc', f, (0, -0.65, 0.10))
        kf(lh, 'rot', f, (0, 0, -3))
        kf(rh, 'rot', f, (0, 0, 3))
        # Supporting left arm
        kf(ls, 'rot', f, (0, -75, -25))
        # Top right arm along side
        kf(rs, 'rot', f, (0, 0, 70))
    export_model("side_plank.glb")

def build_diamond_push_up():
    arm = reset_armature()
    create_action(arm, "Diamond_Push_Up", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']

    for f in [1, 40]:
        kf(p, 'rot', f, (82, 0, 0))
        kf(p, 'loc', f, (0, -0.65, 0.10))
        kf(la, 'rot', f, (-65, 0, 0))
        kf(ra, 'rot', f, (-65, 0, 0))
        # Hands close together in center
        kf(ls, 'rot', f, (25, -60, -10))
        kf(rs, 'rot', f, (25, 60, 10))
        kf(le, 'rot', f, (0, 0, 0))
        kf(re, 'rot', f, (0, 0, 0))

    f = 20
    kf(p, 'loc', f, (0, -0.85, 0.10))
    kf(ls, 'rot', f, (30, -35, -20))
    kf(rs, 'rot', f, (30, 35, 20))
    kf(le, 'rot', f, (0, -90, 0))
    kf(re, 'rot', f, (0, 90, 0))
    export_model("diamond_push_up.glb")

def build_incline_push_up():
    arm = reset_armature()
    create_action(arm, "Incline_Push_Up", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']
    la = arm.pose.bones['m_avg_L_Ankle']
    ra = arm.pose.bones['m_avg_R_Ankle']

    for f in [1, 40]:
        kf(p, 'rot', f, (55, 0, 0))
        kf(p, 'loc', f, (0, -0.45, 0.10))
        kf(la, 'rot', f, (-45, 0, 0))
        kf(ra, 'rot', f, (-45, 0, 0))
        kf(ls, 'rot', f, (20, -70, -25))
        kf(rs, 'rot', f, (20, 70, 25))
        kf(le, 'rot', f, (0, 0, 0))
        kf(re, 'rot', f, (0, 0, 0))

    f = 20
    kf(p, 'loc', f, (0, -0.62, 0.10))
    kf(ls, 'rot', f, (35, -45, -35))
    kf(rs, 'rot', f, (35, 45, 35))
    kf(le, 'rot', f, (0, -85, 0))
    kf(re, 'rot', f, (0, 85, 0))
    export_model("incline_push_up.glb")

def build_triceps_dip():
    arm = reset_armature()
    create_action(arm, "Triceps_Dip", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']
    le = arm.pose.bones['m_avg_L_Elbow']
    re = arm.pose.bones['m_avg_R_Elbow']

    for f in [1, 40]:
        kf(p, 'loc', f, (0, -0.20, 0.10))
        kf(lh, 'rot', f, (-60, 0, -5))
        kf(rh, 'rot', f, (-60, 0, 5))
        kf(lk, 'rot', f, (70, 0, 0))
        kf(rk, 'rot', f, (70, 0, 0))
        kf(ls, 'rot', f, (0, 25, -65))
        kf(rs, 'rot', f, (0, -25, 65))
        kf(le, 'rot', f, (0, 0, 0))
        kf(re, 'rot', f, (0, 0, 0))

    f = 20
    kf(p, 'loc', f, (0, -0.42, 0.10))
    kf(le, 'rot', f, (0, -90, 0))
    kf(re, 'rot', f, (0, 90, 0))
    export_model("triceps_dip.glb")

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

    # Static 90-degree hold
    for f in [1, 20, 40]:
        kf(p, 'loc', f, (0, -0.42, -0.20))
        kf(s1, 'rot', f, (0, 0, 0))
        kf(lh, 'rot', f, (-85, 0, -6))
        kf(rh, 'rot', f, (-85, 0, 6))
        kf(lk, 'rot', f, (90, 0, 0))
        kf(rk, 'rot', f, (90, 0, 0))
        kf(la, 'rot', f, (-15, 0, 0))
        kf(ra, 'rot', f, (-15, 0, 0))
        kf(ls, 'rot', f, (0, -35, -50))
        kf(rs, 'rot', f, (0, 35, 50))
    export_model("wall_sit.glb")

def build_step_up():
    arm = reset_armature()
    create_action(arm, "Step_Up", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    for f in [1, 40]:
        kf(p, 'loc', f, (0, 0, 0))
        kf(lh, 'rot', f, (0, 0, -4))
        kf(rh, 'rot', f, (0, 0, 4))
        kf(lk, 'rot', f, (0, 0, 0))
        kf(rk, 'rot', f, (0, 0, 0))
        kf(ls, 'rot', f, (0, 0, -70))
        kf(rs, 'rot', f, (0, 0, 70))

    # Step right foot onto box at frame 10
    f = 10
    kf(rh, 'rot', f, (-80, 0, 4))
    kf(rk, 'rot', f, (90, 0, 0))

    # Drive up standing on box at frame 20
    f = 20
    kf(p, 'loc', f, (0, 0.35, 0))
    kf(rh, 'rot', f, (0, 0, 4))
    kf(rk, 'rot', f, (0, 0, 0))
    kf(lh, 'rot', f, (0, 0, -4))
    kf(lk, 'rot', f, (0, 0, 0))

    # Step down at frame 30
    f = 30
    kf(lh, 'rot', f, (20, 0, -4))
    kf(lk, 'rot', f, (45, 0, 0))

    export_model("step_up.glb")

def build_burpee():
    arm = reset_armature()
    create_action(arm, "Burpee", 40)
    p = arm.pose.bones['m_avg_Pelvis']
    s1 = arm.pose.bones['m_avg_Spine1']
    lh = arm.pose.bones['m_avg_L_Hip']
    rh = arm.pose.bones['m_avg_R_Hip']
    lk = arm.pose.bones['m_avg_L_Knee']
    rk = arm.pose.bones['m_avg_R_Knee']
    ls = arm.pose.bones['m_avg_L_Shoulder']
    rs = arm.pose.bones['m_avg_R_Shoulder']

    # Standing tall at frame 1
    kf(p, 'rot', 1, (0, 0, 0))
    kf(p, 'loc', 1, (0, 0, 0))
    kf(ls, 'rot', 1, (0, 0, -70))
    kf(rs, 'rot', 1, (0, 0, 70))

    # Squat down hands to floor at frame 10
    kf(p, 'loc', 10, (0, -0.45, -0.15))
    kf(s1, 'rot', 10, (30, 0, 0))
    kf(lh, 'rot', 10, (-80, 0, -10))
    kf(rh, 'rot', 10, (-80, 0, 10))
    kf(lk, 'rot', 10, (95, 0, 0))
    kf(rk, 'rot', 10, (95, 0, 0))
    kf(ls, 'rot', 10, (0, -80, -25))
    kf(rs, 'rot', 10, (0, 80, 25))

    # Plank kick back at frame 20
    kf(p, 'rot', 20, (82, 0, 0))
    kf(p, 'loc', 20, (0, -0.65, 0.10))
    kf(lh, 'rot', 20, (0, 0, -4))
    kf(rh, 'rot', 20, (0, 0, 4))
    kf(lk, 'rot', 20, (0, 0, 0))
    kf(rk, 'rot', 20, (0, 0, 0))
    kf(ls, 'rot', 20, (20, -70, -25))
    kf(rs, 'rot', 20, (20, 70, 25))

    # Jump feet back in at frame 30
    kf(p, 'rot', 30, (0, 0, 0))
    kf(p, 'loc', 30, (0, -0.40, -0.10))
    kf(lh, 'rot', 30, (-75, 0, -10))
    kf(rh, 'rot', 30, (-75, 0, 10))
    kf(lk, 'rot', 30, (90, 0, 0))
    kf(rk, 'rot', 30, (90, 0, 0))

    # Explosive vertical jump with arms overhead at frame 36
    kf(p, 'loc', 36, (0, 0.35, 0))
    kf(lh, 'rot', 36, (0, 0, -4))
    kf(rh, 'rot', 36, (0, 0, 4))
    kf(lk, 'rot', 36, (0, 0, 0))
    kf(rk, 'rot', 36, (0, 0, 0))
    kf(ls, 'rot', 36, (0, 0, 75))
    kf(rs, 'rot', 36, (0, 0, -75))

    # Land standing at frame 40
    kf(p, 'loc', 40, (0, 0, 0))
    kf(ls, 'rot', 40, (0, 0, -70))
    kf(rs, 'rot', 40, (0, 0, 70))

    export_model("burpee.glb")


# ==============================================================================
# MAIN EXECUTION
# ==============================================================================
print("Starting generation of all 30 exercise models...")
builders = [
    build_squat,
    build_jumping_jacks,
    build_push_up,
    build_mountain_climber,
    build_plank,
    build_lunges,
    build_reverse_lunge,
    build_biceps_curl,
    build_shoulder_press,
    build_lateral_raise,
    build_calf_raise,
    build_high_knees,
    build_standing_knee_raise,
    build_hip_hinge,
    build_glute_bridge,
    build_bicycle_crunch,
    build_dead_bug,
    build_flutter_kicks,
    build_russian_twist,
    build_superman,
    build_bird_dog,
    build_donkey_kick,
    build_fire_hydrant,
    build_side_plank,
    build_diamond_push_up,
    build_incline_push_up,
    build_triceps_dip,
    build_wall_sit,
    build_step_up,
    build_burpee,
]

for b in builders:
    b()

print(f"SUCCESSFULLY GENERATED ALL {len(builders)} EXERCISE MODELS!")
