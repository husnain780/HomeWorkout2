import bpy

bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")

for mat in bpy.data.materials:
    print(f"Material: {mat.name}")
    if mat.use_nodes:
        for node in mat.node_tree.nodes:
            print(f"  Node: {node.name} ({node.type})")
