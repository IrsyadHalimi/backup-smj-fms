from django.urls import path, include
from rest_framework.routers import DefaultRouter
from api.views import FuelTransactionViewSet, fuel_monitor_view, fuel_explore_data

router = DefaultRouter()
# /api/fuel-transactions/
router.register(r'fuel-transactions', FuelTransactionViewSet, basename='fueltransaction')

urlpatterns = [
    path('fuel-explore/', fuel_explore_data, name='fuelexplore'),
    path('', include(router.urls)), 
    path('monitor/', fuel_monitor_view, name='fuel-monitor'),
]