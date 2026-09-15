"""Rebuild of the pipeline that made assets/models/mat_*.glb: an ambientCG CC0 colour map,
downscaled, wrapped in a one-quad glTF binary with exactly the material the app's viewport expects."""
import io, json, struct, sys, zipfile, urllib.request
from PIL import Image

SIZE = 512
JPEG_Q = 86

def color_map(asset_id: str) -> Image.Image:
    url = f"https://ambientcg.com/get?file={asset_id}_1K-JPG.zip"
    req = urllib.request.Request(url, headers={"User-Agent": "curl/8.0"})
    raw = urllib.request.urlopen(req, timeout=180).read()
    z = zipfile.ZipFile(io.BytesIO(raw))
    name = next(n for n in z.namelist() if n.endswith("_Color.jpg"))
    im = Image.open(io.BytesIO(z.read(name))).convert("RGB")
    return im.resize((SIZE, SIZE), Image.LANCZOS)

def glb(img: Image.Image) -> bytes:
    buf = io.BytesIO(); img.save(buf, "JPEG", quality=JPEG_Q, optimize=True)
    jpg = buf.getvalue()

    pos = struct.pack("<12f", -.5,0,-.5,  .5,0,-.5,  .5,0,.5,  -.5,0,.5)
    nrm = struct.pack("<12f", *([0,1,0]*4))
    uv  = struct.pack("<8f",  0,0, 1,0, 1,1, 0,1)
    idx = struct.pack("<6H", 0,1,2, 0,2,3)
    pad = lambda b, n=4: b + b"\x00" * (-len(b) % n)
    bin_parts, views, off = [], [], 0
    for blob, align in ((pos,4),(nrm,4),(uv,4),(idx,4),(jpg,4)):
        views.append({"buffer":0, "byteOffset":off, "byteLength":len(blob)})
        blob = pad(blob, align); bin_parts.append(blob); off += len(blob)
    binary = b"".join(bin_parts)

    g = {
      "asset":{"version":"2.0"}, "scene":0, "scenes":[{"nodes":[0]}], "nodes":[{"mesh":0}],
      "meshes":[{"primitives":[{"attributes":{"POSITION":0,"NORMAL":1,"TEXCOORD_0":2},
                                "indices":3,"material":0}]}],
      "materials":[{"pbrMetallicRoughness":{"baseColorFactor":[1,1,1,1],"metallicFactor":0.0,
                     "roughnessFactor":0.85,"baseColorTexture":{"index":0,"texCoord":0}},
                    "doubleSided":False}],
      "accessors":[
        {"bufferView":0,"componentType":5126,"count":4,"type":"VEC3",
         "min":[-0.5,0,-0.5],"max":[0.5,0,0.5]},
        {"bufferView":1,"componentType":5126,"count":4,"type":"VEC3"},
        {"bufferView":2,"componentType":5126,"count":4,"type":"VEC2"},
        {"bufferView":3,"componentType":5123,"count":6,"type":"SCALAR"}],
      "bufferViews":views, "buffers":[{"byteLength":len(binary)}],
      "samplers":[{"magFilter":9729,"minFilter":9987,"wrapS":10497,"wrapT":10497}],
      "textures":[{"sampler":0,"source":0}],
      "images":[{"bufferView":4,"mimeType":"image/jpeg"}],
    }
    js = pad(json.dumps(g, separators=(",",":")).encode(), 4)
    js += b" " * (-len(js) % 4)
    out = b"glTF" + struct.pack("<II", 2, 12 + 8+len(js) + 8+len(binary))
    out += struct.pack("<II", len(js), 0x4E4F534A) + js
    out += struct.pack("<II", len(binary), 0x004E4942) + binary
    return out

if __name__ == "__main__":
    dest = sys.argv[1]
    for spec in sys.argv[2:]:
        asset, name = spec.split("=")
        try:
            data = glb(color_map(asset))
            open(f"{dest}/mat_{name}.glb", "wb").write(data)
            print(f"  mat_{name}.glb  {len(data)//1024} KB   <- {asset}")
        except Exception as e:
            print(f"  FAILED {asset}: {type(e).__name__} {e}")
