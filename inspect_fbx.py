import bpy
import sys

# Clear existing objects
bpy.ops.wm.read_factory_settings(use_empty=True)

# Import FBX
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\Downloads\Character_1_3DXM.fbx")

print("=== BLENDER INSPECTION ===")
print("Objects:", [o.name for o in bpy.data.objects])
print("Meshes:", [m.name for m in bpy.data.meshes])
print("Armatures:", [a.name for a in bpy.data.armatures])
print("Actions (animations):", [act.name for act in bpy.data.actions])
for act in bpy.data.actions:
    print(f"Action: {act.name}, frame_range: {act.frame_range[0]} to {act.frame_range[1]}")
