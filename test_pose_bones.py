import bpy
import math

bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")

# Find armature
arm = None
for obj in bpy.data.objects:
    if obj.type == 'ARMATURE':
        arm = obj
        break

bpy.context.view_layer.objects.active = arm
bpy.ops.object.mode_set(mode='POSE')

# Set all pose bones to Euler XYZ for easy mathematical rotations
for pb in arm.pose.bones:
    pb.rotation_mode = 'XYZ'

# Print orientation and sample bone names
print("Pose bones available:", len(arm.pose.bones))
for b in ['m_avg_Pelvis', 'm_avg_L_Hip', 'm_avg_L_Knee', 'm_avg_L_Shoulder', 'm_avg_R_Shoulder']:
    if b in arm.pose.bones:
        print(f"Bone {b} found in pose bones!")
