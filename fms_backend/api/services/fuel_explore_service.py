# api/services/dashboard_service.py

from django.db.models import Sum, Avg, Count, Q, F
from django.db.models.functions import Cast
from django.db import models
from datetime import datetime, timedelta
from api.models import FuelTransaction

class FuelExploreService:

    @classmethod
    def get_current_month_dates(cls):
        """Helper untuk mendapatkan rentang tanggal bulan ini (format string Flutter YYYY-MM-DD)"""
        now = datetime.now()
        # Jika format string dari Flutter adalah 'YYYY-MM-DD' (contoh: '2026-05-01')
        start_date = now.replace(day=1).strftime('%Y-%m-%d')
        end_date = now.strftime('%Y-%m-%d')
        
        # Untuk mencari pembanding bulan lalu (fuelChange, workingHourChange, dll)
        last_month = now.replace(day=1) - timedelta(days=1)
        start_last_month = last_month.replace(day=1).strftime('%Y-%m-%d')
        end_last_month = last_month.strftime('%Y-%m-%d')
        
        return start_date, end_date, start_last_month, end_last_month

    @classmethod
    def build_fuel_trend(cls):
        """Mengambil data tren harian berdasarkan total qty_value per tanggal bulan ini"""
        start_date, end_date, _, _ = cls.get_current_month_dates()
        
        # Query total konsumsi per hari
        daily_data = (
            FuelTransaction.objects.filter(date__range=[start_date, end_date])
            .values('date')
            .annotate(total_consumed=Sum('consumed_qty'))
            .order_by('date')
        )
        
        # Ubah format date string ke format visual (misal '2026-05-01' -> '1 May')
        trend = []
        for data in daily_data:
            try:
                date_obj = datetime.strptime(data['date'], '%Y-%m-%d')
                day_str = date_obj.strftime('%d %b') # Hasil: '01 May'
            except ValueError:
                day_str = data['date'] # Fallback jika format string berbeda
                
            trend.append({
                "day": day_str,
                "rate": round(data['total_consumed'] or 0, 2)
            })
        return trend

    @classmethod
    def build_model_rates(cls):
        """Menghitung rata-rata Fuel Rate (consumed_qty / (hm_km_unit - last_hm)) per Model Unit"""
        start_date, end_date, _, _ = cls.get_current_month_dates()
        
        # Kita gunakan no_unit_sap sebagai acuan model (atau tech_id jika itu kode modelnya)
        # Menghitung fuel rate = total_consumed / total_hours
        model_stats = (
            FuelTransaction.objects.filter(date__range=[start_date, end_date])
            .values('no_unit_sap') # Kelompokkan berdasarkan jenis/nama unit
            .annotate(
                total_fuel=Sum('consumed_qty'),
                total_hours=Sum(F('hm_km_unit') - F('last_hm')),
            )
        )
        
        rates = []
        for stat in model_stats:
            fuel = stat['total_fuel'] or 0
            hours = stat['total_hours'] or 0
            rate = (fuel / hours) if hours > 0 else 0
            
            rates.append({
                "model": stat['no_unit_sap'],
                "rate": round(rate, 2)
            })
            
        # Urutkan dari yang paling efisien (rate terkecil)
        return sorted(rates, key=lambda x: x['rate'])

    @classmethod
    def build_units(cls):
        """Membangun data detail per unit beserta status efisiensinya"""
        start_date, end_date, _, _ = cls.get_current_month_dates()
        
        # Rata-rata global untuk hitung vsAverage
        avg_global = FuelTransaction.objects.filter(date__range=[start_date, end_date]).aggregate(
            total_f=Sum('consumed_qty'),
            total_h=Sum(F('hm_km_unit') - F('last_hm'))
        )
        global_fuel = avg_global['total_f'] or 0
        global_hours = avg_global['total_h'] or 0
        global_avg_rate = (global_fuel / global_hours) if global_hours > 0 else 1 # menghindari div by zero
        
        # Ambil data per unit
        unit_data = (
            FuelTransaction.objects.filter(date__range=[start_date, end_date])
            .values('no_unit_sap')
            .annotate(
                total_fuel=Sum('consumed_qty'),
                total_hours=Sum(F('hm_km_unit') - F('last_hm')),
            )
        )
        
        units = []
        for idx, u in enumerate(unit_data, start=1):
            fuel = u['total_fuel'] or 0
            hours = u['total_hours'] or 0
            fuel_rate = (fuel / hours) if hours > 0 else 0
            
            # Hitung perbandingan persentase terhadap rata-rata global
            vs_avg = ((fuel_rate - global_avg_rate) / global_avg_rate) * 100
            
            # Klasifikasi Performance
            if vs_avg < -20: performance = "Sangat Efisien"
            elif vs_avg < 0: performance = "Efisien"
            elif vs_avg < 15: performance = "Cukup"
            elif vs_avg < 40: performance = "Boros"
            else: performance = "Sangat Boros"
            
            units.append({
                "id": idx,
                "model": u['no_unit_sap'],
                "fuel": round(fuel, 2),
                "hour": round(hours, 2),
                "fuelRate": round(fuel_rate, 2),
                "vsAverage": round(vs_avg, 2),
                "performance": performance,
                "trend": [round(fuel_rate * 0.9, 1), round(fuel_rate, 1), round(fuel_rate * 1.1, 1)] # Mock mini trend internal unit
            })
            
        return units

    @classmethod
    def build_summary(cls):
        start_date, end_date, start_last, end_last = cls.get_current_month_dates()

        # --- BULAN INI ---
        this_month_data = FuelTransaction.objects.filter(date__range=[start_date, end_date]).aggregate(
            total_fuel=Sum("consumed_qty"),
            total_hours=Sum(F("hm_km_unit") - F("last_hm")),
            total_units=Count("no_unit_sap", distinct=True),
            active_units=Count("no_unit_sap", distinct=True, filter=Q(sync_status='SUCCESS')) # Contoh logika unit aktif
        )
        
        total_fuel = this_month_data["total_fuel"] or 0
        working_hour = this_month_data["total_hours"] or 0
        avg_fuel_rate = (total_fuel / working_hour) if working_hour > 0 else 0

        # --- BULAN LALU (Untuk hitung % Perubahan / Change) ---
        last_month_data = FuelTransaction.objects.filter(date__range=[start_last, end_last]).aggregate(
            total_fuel=Sum("consumed_qty"),
            total_hours=Sum(F("hm_km_unit") - F("last_hm")),
        )
        last_fuel = last_month_data["total_fuel"] or 0
        last_hours = last_month_data["total_hours"] or 0
        last_rate = (last_fuel / last_hours) if last_hours > 0 else 0

        # Hitung Persentase Perubahan
        fuel_change = ((total_fuel - last_fuel) / last_fuel * 100) if last_fuel > 0 else 0
        hour_change = ((working_hour - last_hours) / last_hours * 100) if last_hours > 0 else 0
        rate_change = ((avg_fuel_rate - last_rate) / last_rate * 100) if last_rate > 0 else 0

        # --- MENCARI BEST & WORST MODEL ---
        all_models = cls.build_model_rates()
        best_model, best_rate = "", 0
        worst_model, worst_rate = "", 0
        
        if all_models:
            best_model = all_models[0]["model"]
            best_rate = all_models[0]["rate"]
            worst_model = all_models[-1]["model"]
            worst_rate = all_models[-1]["rate"]

        total_u = this_month_data["total_units"] or 0
        active_u = this_month_data["active_units"] or 0

        return {
            "totalFuel": round(total_fuel, 2),
            "fuelChange": round(fuel_change, 2),
            "workingHour": round(working_hour, 2),
            "workingHourChange": round(hour_change, 2),
            "avgFuelRate": round(avg_fuel_rate, 2),
            "avgFuelRateChange": round(rate_change, 2),
            "bestModel": best_model,
            "bestRate": best_rate,
            "worstModel": worst_model,
            "worstRate": worst_rate,
            "totalUnit": total_u,
            "activeUnit": active_u,
            "inactiveUnit": max(0, total_u - active_u)
        }

    @classmethod
    def build_payload(cls):
        return {
            "summary": cls.build_summary(),
            "fuelTrend": cls.build_fuel_trend(),
            "modelRates": cls.build_model_rates(),
            "units": cls.build_units()
        }