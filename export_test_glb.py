import bpy

bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")

# Export to GLB
bpy.ops.export_scene.gltf(
    filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\test_ybot.glb",
    export_format='GLB',
    export_animations=True,
    export_skins=True
)
print("Exported GLB successfully!")
