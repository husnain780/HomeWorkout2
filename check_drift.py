import struct, json, os

MODELS_DIR = r"app/src/main/assets/models"

def check_model(fn):
    path = os.path.join(MODELS_DIR, fn)
    with open(path, 'rb') as f:
        f.read(12)
        clen, _ = struct.unpack('<II', f.read(8))
        jd = json.loads(f.read(clen).decode('utf-8'))
        f.read(8)
        bin_data = f.read()

    anim = jd['animations'][0]
    drift_found = False
    for ch in anim['channels']:
        target = ch['target']
        node = jd['nodes'][target['node']]
        path_type = target['path']
        node_name = node.get('name', '')
        if path_type == 'translation' and ('Pelvis' in node_name or 'Root' in node_name or 'Armature' in node_name):
            sampler = anim['samplers'][ch['sampler']]
            out_acc = jd['accessors'][sampler['output']]
            bv = jd['bufferViews'][out_acc['bufferView']]
            offset = bv.get('byteOffset', 0) + out_acc.get('byteOffset', 0)
            count = out_acc['count']
            data = struct.unpack_from(f'<{count*3}f', bin_data, offset)
            first_pos = [round(v, 4) for v in data[:3]]
            last_pos = [round(v, 4) for v in data[-3:]]
            diff = [round(abs(a - b), 4) for a, b in zip(first_pos, last_pos)]
            if max(diff) > 0.05:
                print(f"[DRIFT DETECTED] {fn}: {node_name} first={first_pos} last={last_pos} diff={diff}")
                drift_found = True
            else:
                print(f"[LOOP OK] {fn}: {node_name} first={first_pos} last={last_pos} diff={diff}")
    if not drift_found:
        pass

for f in sorted(os.listdir(MODELS_DIR)):
    if f.endswith('.glb'):
        check_model(f)
