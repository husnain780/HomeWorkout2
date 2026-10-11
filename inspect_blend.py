import bpy
bpy.ops.wm.open_mainfile(filepath=r"C:\Users\husnain_fazal\Desktop\project 1.blend")
print("=== PROJECT 1 BLEND INSPECTION ===")
print("Objects:", [o.name for o in bpy.data.objects])
print("Meshes:", [m.name for m in bpy.data.meshes])
print("Armatures:", [a.name for a in bpy.data.armatures])
print("Actions:", [act.name for act in bpy.data.actions])
