import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RideBookingQA {

    public static void main(String[] args) {
        System.out.println("Starting Ride-Sharing Automated Test Suite...");
        
        testNormalBooking();
        testPeakHourBooking();
        testNightBooking();
        testInvalidDistance();
        testInvalidPassengerCount();
        testUnavailableDriver();
        testMaximumDiscount();
        testMultipleVehicleTypes();
        testBoundaryFareValues();
        testDriverAllocationLogic();
    }

    private static String executeEngine(String custId, String pickup, String drop, String dist, String pass, String type, String time, String driver, String promo) {
        try {
            String pythonCmd = "python"; 
            ProcessBuilder pb = new ProcessBuilder(
                pythonCmd, "RideBooking.py", custId, pickup, drop, dist, pass, type, time, driver, promo
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            return "{\"status\":\"ERROR\",\"reason\":\"" + e.getMessage() + "\"}";
        }
    }

    public static void testNormalBooking() {
        String res = executeEngine("C01", "LocA", "LocB", "10.0", "1", "Sedan", "14:30", "true", "NONE");
        System.out.println("Test Normal Booking: " + (res.contains("ACCEPTED") && res.contains("200.0") ? "PASSED" : "FAILED"));
    }

    public static void testPeakHourBooking() {
        String res = executeEngine("C02", "LocA", "LocB", "5.0", "1", "Bike", "09:00", "true", "NONE");
        System.out.println("Test Peak-Hour Booking: " + (res.contains("ACCEPTED") && res.contains("25.0") ? "PASSED" : "FAILED"));
    }

    public static void testNightBooking() {
        String res = executeEngine("C03", "LocA", "LocB", "10.0", "1", "Sedan", "23:30", "true", "NONE");
        System.out.println("Test Night Booking: " + (res.contains("ACCEPTED") && res.contains("night_surcharge\": 40.0") ? "PASSED" : "FAILED"));
    }

    public static void testInvalidDistance() {
        String res = executeEngine("C04", "LocA", "LocB", "0.0", "1", "Sedan", "12:00", "true", "NONE");
        System.out.println("Test Invalid Distance: " + (res.contains("REJECTED") && res.contains("Invalid distance") ? "PASSED" : "FAILED"));
    }

    public static void testInvalidPassengerCount() {
        String res = executeEngine("C05", "LocA", "LocB", "5.0", "5", "Sedan", "12:00", "true", "NONE");
        System.out.println("Test Invalid Passenger Count: " + (res.contains("REJECTED") && res.contains("Excessive") ? "PASSED" : "FAILED"));
    }

    public static void testUnavailableDriver() {
        String res = executeEngine("C06", "LocA", "LocB", "5.0", "2", "SUV", "12:00", "false", "NONE");
        System.out.println("Test Unavailable Driver: " + (res.contains("REJECTED") && res.contains("Unavailable") ? "PASSED" : "FAILED"));
    }

    public static void testMaximumDiscount() {
        String res = executeEngine("C07", "LocA", "LocB", "100.0", "1", "Premium", "12:00", "true", "MAXSAVINGS");
        System.out.println("Test Maximum Discount Ceiling: " + (res.contains("promotional_discount\": 100.0") ? "PASSED" : "FAILED"));
    }

    public static void testMultipleVehicleTypes() {
        String resBike = executeEngine("C08", "LocA", "LocB", "2.0", "1", "Bike", "12:00", "true", "NONE");
        String resSUV = executeEngine("C09", "LocA", "LocB", "2.0", "4", "SUV", "12:00", "true", "NONE");
        boolean valid = resBike.contains("Bike") && resSUV.contains("SUV");
        System.out.println("Test Multiple Vehicle Types: " + (valid ? "PASSED" : "FAILED"));
    }

    public static void testBoundaryFareValues() {
        String res = executeEngine("C10", "LocA", "LocB", "0.01", "1", "Bike", "12:00", "true", "NONE");
        System.out.println("Test Boundary Fare Values (Short Distance): " + (res.contains("ACCEPTED") ? "PASSED" : "FAILED"));
    }

    public static void testDriverAllocationLogic() {
        String res = executeEngine("C11", "LocA", "LocB", "10.0", "2", "SUV", "12:00", "true", "NONE");
        System.out.println("Test Driver Allocation Logic: " + (res.contains("DRV-SUV-99") ? "PASSED" : "FAILED"));
    }
}
