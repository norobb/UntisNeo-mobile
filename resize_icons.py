import os
import json
from PIL import Image

src_image = r"C:\Users\marya\.gemini\antigravity-cli\brain\e5bafb9d-5d5a-4de2-86ca-691ae8df3617\untisneo_logo_1779730745806.png"

android_res = r"androidApp\src\androidMain\res"
ios_res = r"iosApp\iosApp\Assets.xcassets\AppIcon.appiconset"

android_sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

ios_configs = [
    {"size": "20x20", "idiom": "iphone", "scale": "2x", "dim": 40},
    {"size": "20x20", "idiom": "iphone", "scale": "3x", "dim": 60},
    {"size": "29x29", "idiom": "iphone", "scale": "2x", "dim": 58},
    {"size": "29x29", "idiom": "iphone", "scale": "3x", "dim": 87},
    {"size": "40x40", "idiom": "iphone", "scale": "2x", "dim": 80},
    {"size": "40x40", "idiom": "iphone", "scale": "3x", "dim": 120},
    {"size": "60x60", "idiom": "iphone", "scale": "2x", "dim": 120},
    {"size": "60x60", "idiom": "iphone", "scale": "3x", "dim": 180},
    {"size": "20x20", "idiom": "ipad", "scale": "1x", "dim": 20},
    {"size": "20x20", "idiom": "ipad", "scale": "2x", "dim": 40},
    {"size": "29x29", "idiom": "ipad", "scale": "1x", "dim": 29},
    {"size": "29x29", "idiom": "ipad", "scale": "2x", "dim": 58},
    {"size": "40x40", "idiom": "ipad", "scale": "1x", "dim": 40},
    {"size": "40x40", "idiom": "ipad", "scale": "2x", "dim": 80},
    {"size": "76x76", "idiom": "ipad", "scale": "1x", "dim": 76},
    {"size": "76x76", "idiom": "ipad", "scale": "2x", "dim": 152},
    {"size": "83.5x83.5", "idiom": "ipad", "scale": "2x", "dim": 167},
    {"size": "1024x1024", "idiom": "ios-marketing", "scale": "1x", "dim": 1024}
]

def main():
    if not os.path.exists(src_image):
        print(f"Error: {src_image} not found!")
        return

    img = Image.open(src_image).convert("RGBA")
    
    # Android
    for density, size in android_sizes.items():
        dir_path = os.path.join(android_res, f"mipmap-{density}")
        os.makedirs(dir_path, exist_ok=True)
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        resized.save(os.path.join(dir_path, "ic_launcher.png"))
        resized.save(os.path.join(dir_path, "ic_launcher_round.png"))
        print(f"Generated Android {density} ({size}x{size})")
        
    # iOS
    os.makedirs(ios_res, exist_ok=True)
    contents = {
        "images": [],
        "info": {
            "author": "xcode",
            "version": 1
        }
    }
    
    for config in ios_configs:
        dim = config["dim"]
        filename = f"icon_{config['size']}_{config['idiom']}_{config['scale']}.png"
        resized = img.resize((dim, dim), Image.Resampling.LANCZOS)
        
        # Remove alpha for iOS
        bg = Image.new("RGB", resized.size, (255, 255, 255))
        bg.paste(resized, mask=resized.split()[3])
        bg.save(os.path.join(ios_res, filename))
        
        contents["images"].append({
            "size": config["size"],
            "idiom": config["idiom"],
            "filename": filename,
            "scale": config["scale"]
        })
        print(f"Generated iOS {config['idiom']} {config['size']}@{config['scale']} ({dim}x{dim})")
        
    with open(os.path.join(ios_res, "Contents.json"), "w") as f:
        json.dump(contents, f, indent=2)
        
if __name__ == "__main__":
    main()
