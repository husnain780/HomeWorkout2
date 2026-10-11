import bpy
import os
import math

BLENDER_EXE = r"C:\Program Files\Blender Foundation\Blender 5.2\blender.exe"
MODELS_DIR = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models"
OUT_DIR = r"C:\Users\husnain_fazal\.gemini\antigravity\brain\633e00ca-c742-440d-be4e-10649744348d"

targets = [
    ("jumping_jacks.glb", "jumping_jacks", [1, 10, 20, 30], (0, -2.8, 0.95), (math.radians(80), 0, 0)),
    ("push_up.glb", "push_up", [1, 10, 20, 30], (0, -2.6, 0.35), (math.radians(75), 0, 0)),
    ("squat.glb", "squat", [1, 10, 20, 30], (0, -2.8, 0.95), (math.radians(80), 0, 0)),
    ("mountain_climber.glb", "mountain_climber", [1, 10, 20, 30], (0, -2.6, 0.35), (math.radians(75), 0, 0)),
]

for glb_name, label, frames, cam_pos, cam_rot in targets:
    bpy.ops.wm.read_factory_settings(use_empty=True)
    glb_path = os.path.join(MODELS_DIR, glb_name)
    bpy.ops.import_scene.gltf(filepath=glb_path)
    
    # Setup camera
    cam_data = bpy.data.cameras.new("Cam")
    cam_obj = bpy.data.objects.new("CamObj", cam_data)
    bpy.context.scene.collection.objects.link(cam_obj)
    bpy.context.scene.camera = cam_obj
    cam_obj.location = cam_pos
    cam_obj.rotation_euler = cam_rot
    
    # Setup light
    light_data = bpy.data.lights.new(name="Sun", type='SUN')
    light_data.energy = 3.5
    light_obj = bpy.data.objects.new(name="SunObj", object_data=light_data)
    bpy.context.scene.collection.objects.link(light_obj)
    light_obj.location = (2, -3, 4)
    light_obj.rotation_euler = (math.radians(45), math.radians(15), 0)
    
    # Render settings
    scene = bpy.context.scene
    scene.render.resolution_x = 480
    scene.render.resolution_y = 640
    scene.render.film_transparent = True
    scene.render.image_settings.file_format = 'PNG'
    
    for f in frames:
        scene.frame_set(f)
        out_file = os.path.join(OUT_DIR, f"anim_verify_{label}_f{f}.png")
        scene.render.filepath = out_file
        bpy.ops.render.render(write_still=True)
        print(f"Rendered {out_file}")

print("ALL VERIFICATION FRAMES RENDERED SUCCESSFULLY")
