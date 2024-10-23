package com.hospital.menu;

import com.hospital.manager.HospitalManager;
import com.hospital.billing.BillingSystem;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Medicine;
import com.hospital.entity.Patient;
import com.hospital.entity.Shift;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.hibernate.Session;
/*
 * This is the Main menu Help to Selct the mangement yo uwant to do 
 * 
 */


public class Menu {
    private HospitalManager manager;
    private Scanner scanner;
    private BillingSystem billingSystem;

    public Menu(HospitalManager manager) {
        this.manager = manager;
        this.billingSystem = new BillingSystem(manager); 
        scanner = new Scanner(System.in);
    }


	public void start() {
        manager.setupSessionFactory();
        boolean running = true;

        while (running) {
            System.out.println("\n==== Hospital Management System ====");
            System.out.println("1. Manage Patients");
            System.out.println("2. Manage Pharmacy");
            System.out.println("3. Manage Reports"); 
            System.out.println("4. Manage Doctors"); 
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            switch (choice) {
                case 1:
                    patientMenu();
                    break;

                case 2:
                    pharmacyMenu();
                    break;

                case 3:
                    reportMenu(); // Call the new reportMenu method
                    break;
                
                case 4:
                	DoctorMenu();
                	break;

                case 5:
                    running = false;
                    System.out.println("Exiting the program.");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        manager.exit();
        scanner.close();
    }

	
	
	/*
	 * This is the Patient Menu and their Methods 
	 * 
	 */
   
	
	public void patientMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n==== Patient Management ====");
            System.out.println("1. Add Patient");
            System.out.println("2. View Patient");
            System.out.println("3. Update Patient");
            System.out.println("4. Delete Patient");  
            System.out.println("5. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            switch (choice) {
                case 1:
                    addPatient();
                    break;

                case 2:
                    viewPatient();
                    break;

                case 3:
                    updatePatient();
                    break;

                case 4:
                	deletePatient();
                    break;

                case 5:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

	// Method to Call Add Patient
    private void addPatient() {
    	System.out.print("");
        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Patient Age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); 
        

        System.out.print("Enter patient contact number: "); 
        String contactNumber = scanner.nextLine();


        System.out.print("Enter Medical History: ");  //  Can be String With his past history
        String medicalHistory = scanner.nextLine();

       
        Map<String, Integer> medicines = new HashMap<>();
        System.out.print("Enter number of medicines to prescribe: ");
        int numMedicines = scanner.nextInt();
        scanner.nextLine(); 
        for (int i = 0; i < numMedicines; i++) {
            System.out.print("Enter Medicine Type: ");
            String medicineType = scanner.nextLine();
            System.out.print("Enter Quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); 
            medicines.put(medicineType, quantity);
        }

        System.out.print("Is Surgery Required? (true/false): ");
        boolean isSurgeryRequired = scanner.nextBoolean();

        int bedDays = 0;
        int surgeryDays = 0;

        if (isSurgeryRequired) {
            System.out.print("Enter Number of Bed Days: ");
            bedDays = scanner.nextInt();

            System.out.print("Enter Number of Surgery Days: ");
            surgeryDays = scanner.nextInt();
        }
        
  

        manager.addPatient(name, age, medicalHistory, medicines, isSurgeryRequired, bedDays, surgeryDays, contactNumber);
        System.out.println("Patient added successfully!");
    }
    
    //Method to Call View Patient
    private void viewPatient() {
        System.out.print("Enter Patient ID to View: ");
        Long viewId = scanner.nextLong();
        Patient patient = manager.getPatient(viewId);

        if (patient != null) {
            System.out.println("Patient ID: " + patient.getId());
            System.out.println("Name: " + patient.getName());
            System.out.println("Age: " + patient.getAge());
            System.out.println("Medical History: " + patient.getMedicalHistory());
            System.out.println("Prescribed Medicines: " + patient.getPrescribedMedicines());
            System.out.println("Surgery Required: " + (patient.isSurgeryRequired() ? "Yes" : "No"));
            System.out.println("Bed Days: " + patient.getBedDays());
            System.out.println("Surgery Days: " + patient.getSurgeryDays());
            System.out.println("Contact Number " + patient.getContactNumber());
        } else {
  
            System.out.println("No Patient found with ID: " + viewId);
        }
    }

    //Method to call Update Patient 
    private void updatePatient() {
        System.out.print("Enter Patient ID to Update: ");
        Long updateId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        System.out.print("Enter New Patient Name: ");
        String newName = scanner.nextLine();

        System.out.print("Enter New Patient Age: ");
        int newAge = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        System.out.print("Enter patient contact number: "); 
        String newContactNumber = scanner.nextLine();

        System.out.print("Enter New Medical History: ");
        String newMedicalHistory = scanner.nextLine();

        // Update prescribed medicines
        Map<String, Integer> newMedicines = new HashMap<>();
        System.out.print("Enter number of medicines to prescribe: ");
        int newNumMedicines = scanner.nextInt();
        scanner.nextLine(); 
        for (int i = 0; i < newNumMedicines; i++) {
            System.out.print("Enter Medicine Type: ");
            String newMedicineType = scanner.nextLine();
            System.out.print("Enter Quantity: ");
            int newQuantity = scanner.nextInt();
            scanner.nextLine(); 
            newMedicines.put(newMedicineType, newQuantity);
        }

        System.out.print("Is Surgery Required? (true/false): ");
        boolean newIsSurgeryRequired = scanner.nextBoolean();

        int newBedDays = 0;
        int newSurgeryDays = 0;

        if (newIsSurgeryRequired) {
            System.out.print("Enter Number of Bed Days: ");
            newBedDays = scanner.nextInt();

            System.out.print("Enter Number of Surgery Days: ");
            newSurgeryDays = scanner.nextInt();
               
        }
        
       

        manager.updatePatient(updateId, newName, newAge, newMedicalHistory, newMedicines, newIsSurgeryRequired, newBedDays, newSurgeryDays, newContactNumber);
        System.out.println("Patient updated successfully!");
    }

    // Method to Call Delete Patient
    private void deletePatient() {

        System.out.print("Enter Patient ID to Delete: ");
        Long deleteId = scanner.nextLong();
        manager.deletePatient(deleteId);
        System.out.println("Patient deleted successfully!");
    }
    
    
    
    /*
     * This part is the Pharmacy Menu and thier caller methods 
     * 
     */

    
    
    

    public void pharmacyMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n==== Pharmacy Management ====");
            System.out.println("1. Add Medicine Type");
            System.out.println("2. Restock Medicine Type");
            System.out.println("3. View All Medicines");
            System.out.println("4. Generate Bill");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            switch (choice) {
                case 1:
                    addMedicine();
                    break;

                case 2:
                    restockMedicine();
                    break;
                    
                case 3:
                    
                	viewAllMedicines();
                	break;
                
                case 4:
                	generateBillForPatient();
                    break;

                case 5:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    
    // Method to call add medicine
	private void addMedicine() {
        System.out.print("Enter Medicine Type (e.g., capsule, tablet): ");
        String type = scanner.nextLine();

        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter Price: ");
        double price = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        manager.addMedicine(type, quantity, price);
        System.out.println("Medicine type added successfully!");
    }

	//Method to call restock medicine
    private void restockMedicine() {
        System.out.print("Enter Medicine Type to Restock: ");
        String restockType = scanner.nextLine();

        System.out.print("Enter Quantity to Add: ");
        int restockQuantity = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter New Price (or press Enter to skip): ");
        String newPriceInput = scanner.nextLine();
        Double newPrice = newPriceInput.isEmpty() ? null : Double.parseDouble(newPriceInput);

        manager.restockMedicine(restockType, restockQuantity, newPrice);
        System.out.println("Medicine type restocked successfully!");
    }
    
    // Method to call view Medicine
    private void viewAllMedicines() {
        Map<String, Integer> medicines = manager.viewAllMedicines(); // This should now match the return type

        if (medicines.isEmpty()) {
            System.out.println("No medicines found.");
        } else {
            System.out.println("=== List of All Medicines ===");
            for (Map.Entry<String, Integer> entry : medicines.entrySet()) {
                System.out.println("Type: " + entry.getKey() + ", Quantity: " + entry.getValue());
            }
        }
    }
    
    
    // Method to generate a detailed bill for a patient
    private void generateBillForPatient() {
        System.out.print("Enter Patient ID to Generate Bill: ");
        Long patientId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        // Fetch the patient 
        Patient patient = manager.getPatientWithMedicines(patientId);

        if (patient != null) {
            // Generate and print the detailed bill
            billingSystem.printDetailedBill(patient);
        } else {
            System.out.println("No patient found with ID: " + patientId);
        }
    }

    
    
    
   /*
    * This part contians The Report MEnu and their caller methods 
    * 
    */
    private void reportMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n==== Report Menu ====");
            System.out.println("1. List All Patients");
            System.out.println("2. Total Earnings of the Hospital");
            System.out.println("3. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            switch (choice) {
                case 1:
                	displayAllPatients();
                    break;

                case 2:
                	displayTotalEarnings();
                    break;

                case 3:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
   
    //Method to List all the patients in the database
    public void displayAllPatients() {
        List<Patient> patients = manager.getAllPatients(); // Fetch all patients

        if (patients != null && !patients.isEmpty()) {
        	System.out.println("");
            System.out.println("List of Patients:");
            System.out.println("--------------------------");
            for (Patient patient : patients) {
                System.out.println("Patient ID: " + patient.getId());
                System.out.println("Patient Name: " + patient.getName());
                System.out.println("Patient Age: " + patient.getAge());
                System.out.println("Patient Contact Number: " + patient.getContactNumber()); // Assuming you have this field
                System.out.println("--------------------------"); // Line separator
            }
        } else {
            System.out.println("No patients found in the database.");
        }
    }
    
    
 // Method to display total earnings and patient bills
    public void displayTotalEarnings() {
        double totalEarnings = manager.calculateTotalEarnings();

        // Print total earnings
        System.out.println("Total Earnings of the Hospital: " + totalEarnings);
        
    }
 
 
   /*
    * This part contains the Doctor Menu and their Caller Methods
    */
    public void DoctorMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n==== Doctor Management ====");
            System.out.println("1. Add Doctors");
            System.out.println("2. Check Availability");
            System.out.println("3. Book an Appointment");        
            System.out.println("4. View Doctors Appointments");
            System.out.println("5. List of Doctors");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            switch (choice) {
                case 1:
                	addDoctor();
                    break;
                
                case 2:
                	checkDoctorAvailability();
                	break;
                	
                	
                case 3:
                	bookAppointment();
                    break;
                    
                case 4:
                    
                	viewAppointments();
                	break;                
                
                	
                case 5:
                	manager.listAllDoctors(); // this method got called directly from the manager because we dont need an input 
                case 6:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    

    //Method to call add Doctor 
    private void addDoctor() {
        System.out.print("Enter doctor's name: ");
        String doctorName = scanner.nextLine();
        
        System.out.print("Enter Specialization: ");
        String specialization = scanner.nextLine();
        
       manager.addDoctor(doctorName, specialization);
    }
    
    //Method to book an Appointment 
    private void bookAppointment() {
        System.out.print("Enter patient ID: ");
        Long patientId = scanner.nextLong();
        scanner.nextLine(); // Consume the newline

        System.out.print("Enter doctor ID: ");
        Long doctorId = scanner.nextLong();
        scanner.nextLine(); // Consume the newline

        System.out.print("Enter appointment date (yyyy-mm-dd): ");
        String dateInput = scanner.nextLine();
        LocalDate appointmentDate = LocalDate.parse(dateInput);

        System.out.print("Enter shift (MORNING, AFTERNOON, EVENING): ");
        String shiftInput = scanner.nextLine();
        Shift shift = Shift.valueOf(shiftInput.toUpperCase());

        // Fetch the patient and doctor by ID (You need to implement these methods)
        Patient patient = manager.getPatientById(patientId); // Implement this method
        Doctor doctor = manager.getDoctorById(doctorId); // Implement this method

        if (patient != null && doctor != null) {
            // Add the appointment
            manager.addAppointment(appointmentDate, doctor, patient, shift);
            System.out.println("Appointment booked successfully!");
        } else {
            System.out.println("Patient or Doctor not found.");
        }
    }
    
    //Method to view the Appointments
    private void viewAppointments() {
        System.out.print("Enter doctor ID to view appointments: ");
        Long doctorId = scanner.nextLong();

        List<Appointment> appointments = manager.getAppointmentsByDoctor(doctorId);

        if (appointments.isEmpty()) {
            System.out.println("No appointments found for this doctor.");
        } else {
            System.out.println("Appointments for Doctor ID " + doctorId + ":");
            for (Appointment appointment : appointments) {
                System.out.println("Date: " + appointment.getDate() + ", Patient: " + appointment.getPatient().getName() + ", Shift: " + appointment.getShift());
            }
        }
    }
    
    //Method to check if the doctor is available on a day and if you want to book an  appointment
    private void checkDoctorAvailability() {
        System.out.print("Enter Doctor ID: ");
        Long doctorId = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter Appointment Date (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        LocalDate appointmentDate = LocalDate.parse(dateInput);

        // Fetch available shifts for the given date
        Set<Shift> availableShifts = manager.getAvailableShiftsForDoctor(doctorId, appointmentDate);

        if (availableShifts.isEmpty()) {
            System.out.println("No available shifts for the selected date.");
            return;
        }

        System.out.println("Available Shifts: " + availableShifts);
        System.out.print("Do you want to book an appointment? (yes/no): ");
        String choice = scanner.nextLine();

        if (choice.equalsIgnoreCase("yes")) {
            System.out.print("Enter Shift (MORNING, AFTERNOON, EVENING): ");
            String shiftInput = scanner.nextLine();
            Shift selectedShift = Shift.valueOf(shiftInput.toUpperCase());

            // Fetch and display all patients to choose from
            List<Patient> patients = manager.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients found.");
                return; // Exit if no patients are available
            }

            System.out.println("Select a patient from the list below:");
            for (Patient patient : patients) {
                System.out.println("ID: " + patient.getId() + ", Name: " + patient.getName());
            }

            System.out.print("Enter Patient ID: ");
            Long patientId = Long.parseLong(scanner.nextLine());
            Patient selectedPatient = manager.getPatientById(patientId); // Fetch the patient object

            Doctor doctor = manager.getDoctorById(doctorId); // Fetch the doctor object

            // Proceed to booking using the gathered details (only one call)
            System.out.println("Attempting to book an appointment...");
            boolean success = manager.addAppointment(appointmentDate, doctor, selectedPatient, selectedShift);

            // Check if the booking was successful
            if (success) {
                System.out.println("Appointment booked successfully!");
            } else {
                System.out.println("Failed to book appointment. Please try again.");
            }
        }
    }
    
    

 }
    

    

