from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
import pandas as pd
from scheduler import optimize_schedule_avoid_breaks_in_high_hours  # import your existing function

app = FastAPI()

class HourlyPrediction(BaseModel):
    hour: int
    predicted_earnings: float

class OptimizeRequest(BaseModel):
    hourly_predictions: List[HourlyPrediction]
    available_hours: List[int]
    max_consecutive: int = 2
    break_penalty_factor: float = 0.6
    total_hours_limit: Optional[int] = 8

@app.post("/optimize")
def optimize(req: OptimizeRequest):
    df = pd.DataFrame([{"hour": h.hour, "predicted_earnings": h.predicted_earnings} for h in req.hourly_predictions])
    schedule = optimize_schedule_avoid_breaks_in_high_hours(
        hourly_predictions=df,
        available_hours=req.available_hours,
        max_consecutive=req.max_consecutive,
        break_penalty_factor=req.break_penalty_factor,
        total_hours_limit=req.total_hours_limit
    )
    return schedule.to_dict(orient="records")