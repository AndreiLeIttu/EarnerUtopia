from app.predictors.GPT import GPT
from app.predictors.route_picker import RoutePicker

route_picker = RoutePicker()
gpt = GPT()
gpt.prompt(route_picker.routes)