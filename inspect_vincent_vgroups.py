import bpy

bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\Downloads\Character_1_3DXM.fbx")

for obj in bpy.data.objects:
    if obj.type == 'MESH':
        print(f"Mesh: {obj.name}, verts: {len(obj.data.vertices)}, vgroups: {len(obj.vertex_groups)}")
        if len(obj.vertex_groups) > 0:
            print(f"Sample vgroups for {obj.name}:", [vg.name for vg in obj.vertex_groups[:10]])
