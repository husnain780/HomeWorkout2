import os
import struct
import json

MODELS_DIR = r"C:\Users\husnain_fazal\AndroidStudioProjects\HomeWorkout2\app\src\main\assets\models"

def inspect_glb(filepath):
    with open(filepath, "rb") as f:
        magic = f.read(4)
        if magic != b'glTF':
            return False, "Not glTF"
        version, length = struct.unpack("<II", f.read(8))
        chunk_len, chunk_type = struct.unpack("<II", f.read(8))
        if chunk_type != 0x4E4F534A: # JSON
            return False, "No JSON chunk"
        json_bytes = f.read(chunk_len)
        gltf = json.loads(json_bytes.decode('utf-8'))
        
        # Binary chunk
        chunk2_len, chunk2_type = struct.unpack("<II", f.read(8))
        bin_data = f.read(chunk2_len)

    animations = gltf.get("animations", [])
    if not animations:
        return False, "No animations"
    
    anim = animations[0]
    channels = anim.get("channels", [])
    samplers = anim.get("samplers", [])
    accessors = gltf.get("accessors", [])
    buffer_views = gltf.get("bufferViews", [])

    max_delta = 0.0
    dynamic_channels = 0

    for ch in channels:
        sampler_idx = ch["sampler"]
        sampler = samplers[sampler_idx]
        output_acc_idx = sampler["output"]
        output_acc = accessors[output_acc_idx]
        
        bv_idx = output_acc["bufferView"]
        bv = buffer_views[bv_idx]
        byte_offset = bv.get("byteOffset", 0) + output_acc.get("byteOffset", 0)
        count = output_acc["count"]
        acc_type = output_acc["type"] # VEC3 or VEC4
        comp_type = output_acc["componentType"] # 5126 = float
        
        if comp_type == 5126:
            floats_per_item = 4 if acc_type == "VEC4" else (3 if acc_type == "VEC3" else 1)
            fmt = f"<{floats_per_item * count}f"
            data = struct.unpack_from(fmt, bin_data, byte_offset)
            
            # Group into items
            items = [data[i*floats_per_item:(i+1)*floats_per_item] for i in range(count)]
            if len(items) > 1:
                # Calculate max range across keyframes
                for dim in range(floats_per_item):
                    vals = [it[dim] for it in items]
                    delta = max(vals) - min(vals)
                    if delta > 0.01:
                        dynamic_channels += 1
                    if delta > max_delta:
                        max_delta = delta

    return True, f"channels={len(channels)}, dynamic_channels={dynamic_channels}, max_delta={max_delta:.4f}"

files = sorted([f for f in os.listdir(MODELS_DIR) if f.endswith(".glb")])
print(f"Total GLB models: {len(files)}")
all_valid = True
for f in files:
    ok, info = inspect_glb(os.path.join(MODELS_DIR, f))
    status = "OK" if "dynamic_channels=0" not in info and ok else "STATIC_WARN"
    if status == "STATIC_WARN":
        all_valid = False
    print(f"[{status}] {f:30s} -> {info}")

print(f"\nVerification summary: {'ALL 30 DYNAMIC' if all_valid else 'SOME REMAIN STATIC'}")
