import pandas as pd
import os
from geopy.geocoders import Nominatim
from geopy.extra.rate_limiter import RateLimiter

class RoutePicker:
    def __init__(self, csv_path="../artificial_data/rides.csv"):
        """
        Simple route manager that loads and refreshes routes from a CSV file.
        Automatically handles new routes added later.
        """
        self.csv_path = csv_path
        self.routes = None
        self.last_row_count = 0
        self.geolocator = Nominatim(user_agent="uber_route_picker")
        # Rate limiter to avoid API throttling (1 request per second)
        self.reverse = RateLimiter(self.geolocator.reverse, min_delay_seconds=1)
        self.load_routes()

    def load_routes(self):
        """Load or reload the route data from CSV."""
        if not os.path.exists(self.csv_path):
            raise FileNotFoundError(f"Route file not found: {self.csv_path}")

        df = pd.read_csv(self.csv_path)
        df = df.dropna(subset=["pickup_lat", "pickup_lon", "drop_lat", "drop_lon"])
        df.reset_index(drop=True, inplace=True)

        # Add addresses if not already in file
        if "pickup_address" not in df.columns or "drop_address" not in df.columns:
            print("Adding pickup and drop-off addresses...")
            df["pickup_address"] = df.apply(
                lambda row: self._get_address(row["pickup_lat"], row["pickup_lon"]), axis=1
            )
            df["drop_address"] = df.apply(
                lambda row: self._get_address(row["drop_lat"], row["drop_lon"]), axis=1
            )

        self.routes = df
        self.last_row_count = len(df)
        print(f"Loaded {self.last_row_count} routes from {self.csv_path}.")

    def _get_address(self, lat, lon):
        """Get the human-readable address for a pair of coordinates."""
        try:
            location = self.reverse((lat, lon), language="en")
            return location.address if location else "Unknown location"
        except Exception as e:
            print(f"Geocoding failed for ({lat}, {lon}): {e}")
            return "Unknown location"

    def refresh_routes(self):
        """
        Refresh the routes if the CSV file has been updated (e.g., new routes added).
        Returns True if new data was loaded, False otherwise.
        """
        df = pd.read_csv(self.csv_path)
        if len(df) != self.last_row_count:
            print(f"Detected {len(df) - self.last_row_count} new routes. Reloading data...")
            self.routes = df.reset_index(drop=True)
            self.last_row_count = len(df)
            return True
        print("No new routes detected.")
        return False

    def get_routes(self):
        """Return the current routes DataFrame."""
        if self.routes is None:
            self.load_routes()
        return self.routes.copy()

    def summary(self):
        """Print a short summary of all routes currently loaded."""
        if self.routes is None or self.routes.empty:
            print("No routes loaded.")
            return
        print(f"Total routes: {len(self.routes)}")
        print(self.routes[["pickup_address", "drop_address", "price", "distance_km"]].head())
