#!/bin/bash

export FLASK_APP=/root/coordinator.py
export FLASK_ENV=development

# El modo de funcionamiento puede tener los valores: random, location, vCache1, vCache2 o vCache3.
export COORDINATOR_MODE=location

flask run --host=10.0.0.40 --port=5000
