from rest_framework import serializers
from ..models import ModelUnit, MasterBudgetUnit

class ModelUnitSerializer(serializers.ModelSerializer):
    class Meta:
        model = ModelUnit
        fields = '__all__'