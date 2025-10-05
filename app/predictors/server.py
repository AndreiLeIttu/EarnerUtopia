from flask import Flask, request, jsonify
import pandas as pd
from scheduler import (
    optimize_schedule_avoid_breaks_in_high_hours,
    prediction_per_hour,
    load_uber_mock_data,
)

app = Flask(__name__)

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
    print("🚀 /optimize called with:", data)

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

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)
