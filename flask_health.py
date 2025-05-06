from flask import Blueprint

# Create a simple health blueprint
health = Blueprint('health', __name__)

# Define a custom error for health checks
class HealthError(Exception):
    pass

# Simple route for health checks
@health.route('/live')
def live():
    return {'status': 'ok'}, 200

@health.route('/ready')
def ready():
    return {'status': 'ok'}, 200 