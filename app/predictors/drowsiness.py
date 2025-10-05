import os
from ultralytics import YOLO
import yaml
import kagglehub
from PIL import Image

download_paths = []
dataset_urls = [
    'yasharjebraeily/drowsy-detection-dataset'
]

for url in dataset_urls:
    download_path = kagglehub.dataset_download(url)
    download_paths.append(download_path)
    print(download_path)

# --- 1. Load the Best Model ---
best_model_path = os.path.join('best.pt')
if not os.path.exists(best_model_path):
    raise FileNotFoundError(f"Best model not found at {best_model_path}. Please ensure training was successful.")

model = YOLO(best_model_path)
print(f"Loaded best model from: {best_model_path}")
DATASET_PATH = f'../dataset'
input_dir = os.path.join(DATASET_PATH, "test", "Non drowsy")

# optional: save grayscale copies to a new folder
output_dir = os.path.join(DATASET_PATH, "test", "Non drowsy")
os.makedirs(output_dir, exist_ok=True)

# process all images
for filename in os.listdir(input_dir):
    if filename.lower().endswith((".png", ".jpg", ".jpeg")):
        input_path = os.path.join(input_dir, filename)
        output_path = os.path.join(output_dir, filename)

        # open image and convert to grayscale
        with Image.open(input_path) as img:
            bw = img.convert("L")  # grayscale mode
            bw.save(output_path)

        print(f"Converted: {filename}")

print("All images converted to grayscale!")
print("prezic cica")
print(model.predict(os.path.join(DATASET_PATH, 'test/Non drowsy'))[5 ].probs)
