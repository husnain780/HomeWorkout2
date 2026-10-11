import bpy
import math

bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")

arm = next(o for o in bpy.data.objects if o.type == 'ARMATURE')
bpy.context.view_layer.objects.active = arm
bpy.ops.object.mode_set(mode='POSE')

for b_name in ['m_avg_L_Shoulder', 'm_avg_L_Hip', 'm_avg_L_Knee']:
    pb = arm.pose.bones[b_name]
    print(f"Bone {b_name} matrix_basis:\n", pb.matrix_basis)
    print(f"Bone {b_name} head: {pb.head}, tail: {pb.tail}")
