import numpy as np
import pandas as pd
import glob
from xgboost import XGBRegressor


from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error


def prediction_per_hour(driver_id, city_id, date,
                             rides_trips, surge_by_hour,
                             cancellation_rates, riders,
                             heatmap=None,
                             max_continuous_hours=3,
                             max_shift_hours=6,
                             break_minutes=15,
                             meal_minutes=30,
                             rating_weight=0.05):
    """
    Generate a recommended work schedule for a driver with breaks, maximizing expected earnings,
    accounting for surge, heatmap, cancellations, and rider ratings.

    Parameters:
        driver_id: str, e.g., 'E10111'
        city_id: int, city identifier
        date: str, 'YYYY-MM-DD'
        rides_trips: pd.DataFrame, historical trips with 'start_time', 'net_earnings', 'rider_id', 'city_id'
        surge_by_hour: pd.DataFrame, 'city_id', 'hour', 'surge_multiplier'
        cancellation_rates: pd.DataFrame, 'city_id', 'cancellation_rate_pct', 'hexagon_id9'
        riders: pd.DataFrame, rider profiles with 'rider_id' and 'rating'
        heatmap: pd.DataFrame (optional), 'hexagon_id_9', 'predicted_eph'
        max_continuous_hours: int, max driving hours before a short break
        max_shift_hours: int, max shift hours before meal break
        break_minutes: int, short break duration
        meal_minutes: int, meal break duration
        rating_weight: float, factor for adjusting earnings based on rider rating

    Returns:
        schedule: pd.DataFrame, columns ['time', 'activity', 'predicted_earnings', 'notes']
    """

    rides_trips_df = tables['rides_trips']
    city_trips = rides_trips_df[rides_trips_df['city_id'] == city_id].copy()
    city_trips['start_time'] = pd.to_datetime(city_trips['start_time'])
    city_trips['hour'] = city_trips['start_time'].dt.hour
    city_trips['weekday'] = city_trips['start_time'].dt.weekday  # 0=Monday
    city_trips.sort_values('start_time', inplace=True)

    # Rolling mean of net earnings (past 50 trips as example)
    city_trips['rolling_earnings'] = city_trips['net_earnings'].rolling(window=50, min_periods=1).mean()

    # Encode hour circularly
    city_trips['hour_sin'] = np.sin(2 * np.pi * city_trips['hour'] / 24)
    city_trips['hour_cos'] = np.cos(2 * np.pi * city_trips['hour'] / 24)

    # Encode weekday circularly
    city_trips['weekday_sin'] = np.sin(2 * np.pi * city_trips['weekday'] / 7)
    city_trips['weekday_cos'] = np.cos(2 * np.pi * city_trips['weekday'] / 7)

    surge_city = surge_by_hour[surge_by_hour['city_id'] == city_id][['hour', 'surge_multiplier']]
    city_trips = city_trips.merge(surge_city, on='hour', how='left')
    print(city_trips.columns)
    city_trips['surge_multiplier'] = city_trips['surge_multiplier_x'].fillna(1.0)

    # Features: circular hour, circular weekday, rolling earnings, surge
    X = city_trips[['hour_sin', 'hour_cos', 'weekday_sin', 'weekday_cos', 'rolling_earnings', 'surge_multiplier']]
    y = city_trips['net_earnings']

    model = XGBRegressor()
    model.fit(X, y)
    y_pred = model.predict(X)
    mse = mean_squared_error(y, y_pred)
    rmse = np.sqrt(mse)

    print(f"Training MSE: {mse:.2f}")
    print(f"Training RMSE: {rmse:.2f}")

    target_date = pd.to_datetime(date)  # e.g., '2025-10-04'
    weekday = target_date.weekday()  # 0=Monday, 6=Sunday

    # Create 24-hour DataFrame
    hours_df = pd.DataFrame({'hour': range(24)})
    hours_df['weekday'] = weekday

    # Rolling earnings (average past earnings for city)
    rolling_mean = city_trips['rolling_earnings'].mean()
    hours_df['rolling_earnings'] = rolling_mean

    # Circular encoding
    hours_df['hour_sin'] = np.sin(2 * np.pi * hours_df['hour'] / 24)
    hours_df['hour_cos'] = np.cos(2 * np.pi * hours_df['hour'] / 24)
    hours_df['weekday_sin'] = np.sin(2 * np.pi * hours_df['weekday'] / 7)
    hours_df['weekday_cos'] = np.cos(2 * np.pi * hours_df['weekday'] / 7)

    # Merge surge multiplier for the city
    surge_city = surge_by_hour[surge_by_hour['city_id'] == city_id][['hour', 'surge_multiplier']]
    hours_df = hours_df.merge(surge_city, on='hour', how='left')
    hours_df['surge_multiplier'] = hours_df['surge_multiplier'].fillna(1.0)

    # Predict net earnings
    feature_cols = ['hour_sin', 'hour_cos', 'weekday_sin', 'weekday_cos', 'rolling_earnings', 'surge_multiplier']
    hours_df['predicted_earnings'] = model.predict(hours_df[feature_cols])


    return hours_df

import pandas as pd

def build_driver_schedule_from_availability(hourly_predictions,
                                            available_hours,
                                            break_interval=2,
                                            break_minutes=15):
    """
    Build a driver schedule based on availability, adding breaks,
    and showing predicted earnings.

    Parameters:
        hourly_predictions: pd.DataFrame
            Must contain ['hour', 'predicted_earnings']
        available_hours: list of int
            Hours the driver is available (e.g. [8,9,10,11,12,13])
        break_interval: int
            Consecutive driving hours before inserting a break
        break_minutes: int
            Duration of break in minutes

    Returns:
        pd.DataFrame: schedule with ['time', 'activity', 'predicted_earnings', 'notes']
    """
    schedule = []
    consecutive = 0

    for i, hour in enumerate(available_hours):
        # Get prediction for this hour
        earning = hourly_predictions.loc[hourly_predictions['hour'] == hour, 'predicted_earnings'].values[0]

        # Driving block
        schedule.append({
            'time': f"{hour}:00 - {hour+1}:00",
            'activity': 'Drive',
            'predicted_earnings': earning,
            'notes': ''
        })

        consecutive += 1

        # Insert break if needed
        if consecutive >= break_interval and i != len(available_hours)-1:
            schedule.append({
                'time': f"{hour+1}:00 - {hour+1}:{break_minutes:02d}",
                'activity': 'Break',
                'predicted_earnings': 0,
                'notes': f'{break_minutes} min rest'
            })
            consecutive = 0

    return pd.DataFrame(schedule)



# -------------------------
# Example Usage:
# -------------------------
def load_uber_mock_data(path_pattern='uber_hackathon_v2_mock_data_*.csv'):
    """
    Loads multiple CSV files with pattern: uber_hackathon_v2_mock_data_<table>.csv
    Returns a dict of {table_name: dataframe}.
    """
    data_tables = {}
    files = glob.glob('../data/*')
    for f in files:
        # Extract table name from file
        table_name = f.split("-")[-1].replace(".csv","").lstrip()  # assumes last part before .csv is table name
        df = pd.read_csv(f)
        data_tables[table_name] = df
    return data_tables


import pulp
import pandas as pd

def optimize_schedule_avoid_breaks_in_high_hours(hourly_predictions,
                                                  available_hours,
                                                  max_consecutive=2,
                                                  break_penalty_factor=0.5,
                                                  total_hours_limit=None):
    """
    Optimize schedule to maximize earnings while penalizing breaks during high-earning hours.

    Parameters
    ----------
    hourly_predictions : pd.DataFrame
        DataFrame with columns ['hour','predicted_earnings'] for hours 0..23 (or subset).
    available_hours : list[int]
        Hours the driver is available (e.g., [8,9,10,11,12,13])
    max_consecutive : int
        Max number of consecutive driving hours allowed (forces at least one break in any block of length max_consecutive+1).
    break_penalty_factor : float
        Weight for penalizing breaks in productive hours (β). Larger => stronger preference to avoid breaks in high-value hours.
    total_hours_limit : int or None
        If set, the solver will limit the total driving hours to this number.

    Returns
    -------
    pd.DataFrame
        schedule with columns ['hour','activity','predicted_earnings'] in the order of available_hours and inserted breaks (hours with activity 'Break').
    """

    # Ensure available_hours sorted (use chronological order)
    avail = sorted(available_hours)

    # Map predicted earnings to hours (use 0 if missing)
    pred_map = hourly_predictions.set_index('hour')['predicted_earnings'].to_dict()
    pred = {h: float(pred_map.get(h, 0.0)) for h in avail}

    # Problem
    model = pulp.LpProblem("DriverScheduleAvoidBreaksInHighHours", pulp.LpMaximize)

    # Decision variables: x_h = 1 if driving in hour h, 0 if break
    x = {h: pulp.LpVariable(f"x_{h}", cat="Binary") for h in avail}

    # Objective:
    # max Σ pred[h]*x[h] - beta * Σ pred[h] * (1 - x[h])
    # = max Σ pred[h]*x[h] - beta * (Σ pred[h] - Σ pred[h]*x[h])
    # constant term -beta*Σpred ignored; equivalently:
    # max Σ ((1 + beta) * pred[h]) * x[h]  (ignoring constant)
    # but we'll write it explicitly for clarity:
    model += pulp.lpSum(pred[h] * x[h] for h in avail) - break_penalty_factor * pulp.lpSum(pred[h] * (1 - x[h]) for h in avail)

    # Constraint: at most total_hours_limit driving hours (if provided)
    if total_hours_limit is not None:
        model += pulp.lpSum(x[h] for h in avail) <= total_hours_limit

    # Constraint: in any sliding block of length (max_consecutive + 1),
    # at most max_consecutive can be driving => ensures at least one break
    # We must create sliding windows across the *chronological positions* in avail.
    n = len(avail)
    window = max_consecutive + 1
    if n >= window:
        for i in range(n - window + 1):
            block = avail[i:i + window]
            model += pulp.lpSum(x[h] for h in block) <= max_consecutive

    # Solve
    solver = pulp.PULP_CBC_CMD(msg=False)  # silent solve; set msg=True to debug
    model.solve(solver)

    # Build schedule: iterate through avail hours in chronological order
    schedule = []
    for h in avail:
        val = pulp.value(x[h])
        activity = "Drive" if val >= 0.5 else "Break"
        earning = pred[h] if activity == "Drive" else 0.0
        schedule.append({'hour': h, 'activity': activity, 'predicted_earnings': round(earning, 2)})

    return pd.DataFrame(schedule)




# Load all tables
#
tables = load_uber_mock_data()
#
# # Filter trips for city
#
#
# hourly_predictions = prediction_per_hour(
#     driver_id='E10111',
#     city_id=3,
#     date='2023-01-13',
#     rides_trips=tables['rides_trips'],
#     surge_by_hour=tables['surge_by_hour'],
#     cancellation_rates=tables['cancellation_rates'],
#     riders=tables['riders'],
#     heatmap=tables.get('heatmap', None)
# )
#
#
#
# available_hours = list(range(8, 20))  # 8:00-19:00
# schedule_df = optimize_schedule_avoid_breaks_in_high_hours(hourly_predictions,
#                                                                available_hours,
#                                                                max_consecutive=2,
#                                                                break_penalty_factor=0.6,
#                                                                total_hours_limit=8)
# print(schedule_df)
#

