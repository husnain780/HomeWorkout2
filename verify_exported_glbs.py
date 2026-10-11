import bpy

for f in ['jumping_jacks.glb', 'squat.glb']:
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.ops.import_scene.gltf(filepath=fr"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models\{f}")
    print(f"=== {f} ===")
    print("Objects:", len(bpy.data.objects))
    print("Actions:", [a.name for a in bpy.data.actions])
    for a in bpy.data.actions:
        print(f"  Action {a.name}: range {a.frame_range[0]} to {a.frame_range[1]}")
