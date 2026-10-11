import bpy

bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")

print("=== YBOT FBX INSPECTION ===")
print("Objects:", [o.name for o in bpy.data.objects])
print("Meshes:", [m.name for m in bpy.data.meshes])
print("Armatures:", [a.name for a in bpy.data.armatures])
for arm in bpy.data.armatures:
    print(f"Bones count in {arm.name}: {len(arm.bones)}")
    print("Sample bones:", [b.name for b in arm.bones[:10]])
