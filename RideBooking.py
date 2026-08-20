import json
import sys

class RideBooking:
    def __init__(self):
        # Configuration rules
        self.vehicle_configs = {
            "Bike": {"base_fare": 30.0, "per_km": 10.0, "max_passengers": 1},
            "Sedan": {"base_fare": 50.0, "per_km": 15.0, "max_passengers": 4},
            "SUV": {"base_fare": 80.0, "per_km": 22.0, "max_passengers": 7},
            "Premium": {"base_fare": 120.0, "per_km": 30.0, "max_passengers": 4}
        }

    def process_booking(self, customer_id, pickup, drop, distance, passengers, vehicle_type, booking_time, driver_available, promo_code=None):
        # 1. General Validations
        if distance <= 0:
            return {"status": "REJECTED", "reason": "Invalid distance"}
        
        if vehicle_type not in self.vehicle_configs:
            return {"status": "REJECTED", "reason": "Invalid vehicle type"}
            
        config = self.vehicle_configs[vehicle_type]
        if passengers <= 0 or passengers > config["max_passengers"]:
            return {"status": "REJECTED", "reason": "Excessive or invalid passenger count"}
            
        try:
            hour = int(booking_time.split(":")[0])
            minute = int(booking_time.split(":")[1])
            if not (0 <= hour <= 23 and 0 <= minute <= 59):
                raise ValueError
        except:
            return {"status": "REJECTED", "reason": "Invalid booking time"}

        if not driver_available:
            return {"status": "REJECTED", "reason": "Unavailable vehicle/driver"}

        # 2. Fare Calculations
        base_fare = config["base_fare"]
        distance_fare = distance * config["per_km"]
        
        # Peak-hour surcharge (08:00-10:59, 17:00-20:59)
        peak_surcharge = 0.0
        if (8 <= hour <= 10) or (17 <= hour <= 20):
            peak_surcharge = 25.0
            
        # Night surcharge (23:00-04:59)
        night_surcharge = 0.0
        if (hour >= 23) or (hour <= 4):
            night_surcharge = 40.0
            
        # Passenger surcharge (Extra passengers past 1 for cars)
        passenger_surcharge = 0.0
        if vehicle_type != "Bike" and passengers > 1:
            passenger_surcharge = (passengers - 1) * 15.0

        # Subtotal calculation
        subtotal = base_fare + distance_fare + peak_surcharge + night_surcharge + passenger_surcharge
        
        # Promotional discount calculation
        discount = 0.0
        if promo_code == "MAXSAVINGS":
            discount = subtotal * 0.20 # 20% discount
            if discount > 100.0: # Hard ceiling
                discount = 100.0

        final_fare = max(0.0, subtotal - discount)
        driver_id = f"DRV-{vehicle_type.upper()}-99"

        return {
            "status": "ACCEPTED",
            "customer_id": customer_id,
            "pickup": pickup,
            "drop": drop,
            "vehicle_type": vehicle_type,
            "breakdown": {
                "base_fare": round(base_fare, 2),
                "distance_fare": round(distance_fare, 2),
                "peak_surcharge": round(peak_surcharge, 2),
                "night_surcharge": round(night_surcharge, 2),
                "passenger_surcharge": round(passenger_surcharge, 2),
                "promotional_discount": round(discount, 2)
            },
            "final_fare": round(final_fare, 2),
            "driver_assignment": driver_id
        }

if __name__ == "__main__":
    # Provides execution interface for Java QA CLI execution
    if len(sys.argv) > 1:
        engine = RideBooking()
        args = sys.argv[1:]
        res = engine.process_booking(
            customer_id=args[0],
            pickup=args[1],
            drop=args[2],
            distance=float(args[3]),
            passengers=int(args[4]),
            vehicle_type=args[5],
            booking_time=args[6],
            driver_available=args[7].lower() == 'true',
            promo_code=args[8] if len(args) > 8 else None
        )
        print(json.dumps(res))
