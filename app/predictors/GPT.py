import os
from openai import OpenAI
from dotenv import load_dotenv

class GPT():
    def __init__(self, token = None): # not a key, not secret dont worry
        load_dotenv()
        # Use token from .env if available, else fallback
        self.token = token or os.getenv("HF_API_KEY")
        self.timeout = 100

    def prompt(self, routes_df, driver_context = None):
        client = OpenAI(
            base_url="https://router.huggingface.co/v1",
            api_key=self.token,
        )

        prompt = (
            "You are a concise in-app Uber assistant widget. Your goal is to recommend "
            "the best route for a driver quickly. Respond as if you were giving a short, "
            "motivating tip. Always start your answer with the route number, then give a one-sentence explanation.\n\n"
        )

        if driver_context:
            prompt += f"Driver context:\n{driver_context}\n\n"

        prompt += "Route options:\n\n"

        # Add all routes from the DataFrame
        for i, row in routes_df.iterrows():
            prompt += (
                f"{i + 1}. Pickup: {row['pickup_address']}), "
                f"Dropoff: {row['drop_address']}\n"
                f"   Distance: {row['distance_km']} km, "
                f"Expected earnings: €{row['price']:.2f}, "
                f"Duration: {row['duration']} min\n\n"
            )

        prompt += (
            "Instructions:\nPick the best route number and explain briefly why it’s the best "
            "considering the driver’s preferences, earnings, surge, and route features.\n"
            "Format your answer exactly like this:\n[Route number]: [one-line motivation]."
        )
        print(prompt)

        completion = client.chat.completions.create(
            model="openai/gpt-oss-120b:fireworks-ai",
            messages=[
                {
                    "role": "user",
                    "content": prompt

                }
            ],
        )


        print(completion.choices[0].message.content)
        return completion.choices[0].message.content

from geopy.geocoders import Nominatim

geolocator = Nominatim(user_agent="my_app")
location = geolocator.reverse((52.379, 4.900))  # lat, lon

print(location.address)
print(location.raw)  # contains detailed info like city, suburb, postcode
