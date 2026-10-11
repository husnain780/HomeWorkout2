import bpy

bpy.ops.wm.read_factory_settings(use_empty=True)

# 1. Import YBot to get armature
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\ybot.fbx")

# Find armature
arm = None
for obj in bpy.data.objects:
    if obj.type == 'ARMATURE':
        arm = obj
        break
print("Found armature:", arm.name)

# Delete existing YBot mesh objects to keep only armature
mesh_objs = [o for o in bpy.data.objects if o.type == 'MESH']
for o in mesh_objs:
    bpy.data.objects.remove(o, do_unlink=True)

# 2. Import Vincent
bpy.ops.import_scene.fbx(filepath=r"C:\Users\husnain_fazal\Downloads\Character_1_3DXM.fbx")

# Join Vincent body and head into one mesh
vincent_meshes = [o for o in bpy.data.objects if o.type == 'MESH']
print("Vincent meshes:", [o.name for o in vincent_meshes])

# Select all vincent meshes and armature
bpy.ops.object.select_all(action='DESELECT')
for m in vincent_meshes:
    m.select_set(True)
bpy.context.view_layer.objects.active = vincent_meshes[0]
bpy.ops.object.join()

joined_mesh = bpy.context.view_layer.objects.active
joined_mesh.name = "HumanTrainer"
print("Joined mesh:", joined_mesh.name, "verts:", len(joined_mesh.data.vertices))

# Parent mesh to armature with automatic weights
bpy.ops.object.select_all(action='DESELECT')
joined_mesh.select_set(True)
arm.select_set(True)
bpy.context.view_layer.objects.active = arm

bpy.ops.object.parent_set(type='ARMATURE_AUTO')
print("Successfully parented Vincent to Armature with automatic weights!")
print("Vgroups generated:", len(joined_mesh.vertex_groups))
