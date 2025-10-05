from flask import Flask, request, jsonify
import pandas as pd
import os
from ultralytics import YOLO
import cv2
from scheduler import (
    optimize_schedule_avoid_breaks_in_high_hours,
    prediction_per_hour,
    load_uber_mock_data,
)

app = Flask(__name__)

best_model_path = os.path.join('best.pt')
if not os.path.exists(best_model_path):
    raise FileNotFoundError(f"Best model not found at {best_model_path}. Please ensure training was successful.")

model = YOLO(best_model_path)
DATASET_PATH = f'../dataset'
# Load data once
tables = load_uber_mock_data()
hourly_predictions = prediction_per_hour(
    driver_id='E10111',
    city_id=3,
    date='2023-01-13',
    rides_trips=tables['rides_trips'],
    surge_by_hour=tables['surge_by_hour'],
    cancellation_rates=tables['cancellation_rates'],
    riders=tables['riders'],
    heatmap=tables.get('heatmap', None)
)

@app.route('/optimize', methods=['POST'])
def optimize():
    data = request.get_json()
    print("/optimize called with:", data)

    available_hours = data.get('available_hours', [])
    schedule = optimize_schedule_avoid_breaks_in_high_hours(
        hourly_predictions=hourly_predictions,
        available_hours=available_hours,
        max_consecutive=2,
        break_penalty_factor=0.6,
        total_hours_limit=8
    )

    print("✅ Computed schedule.")
    return jsonify(schedule.to_dict(orient="records"))

@app.route('/checkDrowsy', methods=['POST'])
def checkDrowsy():
    data = request.get_json()

    # Convert list of ints to actual bytes
    byte_list = data.get('bytes')
    if not isinstance(byte_list, list):
        return jsonify({'error': 'Expected a list of bytes'}), 400
    image_bytes = bytes((b % 256) for b in byte_list)
    print(byte_list)
    image_bytes = bytes(image_bytes)  # ✅ convert to bytes

    with open("temp.png", "wb") as f:
        f.write(image_bytes)
    img = cv2.imread("temp.png")  # Load image
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)  # Convert to grayscale
    cv2.imwrite("temp.png", gray)
    pred_prob = model.predict("temp.png")[0].probs
    print(pred_prob)

    return jsonify(True if pred_prob.data[0] > 0.5 else False)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)
