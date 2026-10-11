import bpy

bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")

arm = bpy.data.armatures[0]
for i, b in enumerate(arm.bones):
    parent_name = b.parent.name if b.parent else "None"
    print(f"{i}: {b.name} (parent: {parent_name})")
