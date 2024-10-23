package com.hospital.manager;

import com.hospital.entity.Patient;
import com.hospital.entity.Shift;
import com.hibernate.util.HibernateUtil;
import com.hospital.billing.BillingSystem;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Medicine;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;


import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

public class HospitalManager {
	
	private SessionFactory sessionFactory;
    private BillingSystem billingSystem;

    // Constructor to initialize the SessionFactory and BillingSystem
    public HospitalManager() {
        setupSessionFactory();
        billingSystem = new BillingSystem(this); // Initialize BillingSystem 
    }


 
    // Method to get the Session Factory 
    
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    
    // Method to initialize Hibernate SessionFactory
    
    public void setupSessionFactory() {
        try {
            // Assuming you have a 'hibernate.cfg.xml' file properly configured
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up Hibernate SessionFactory.");
        }
    }
    
    // Method to Close the Hibernate SessionFactory
    public void exit() {
        sessionFactory.close();
    }
    
   /*
    * This Part Contains all the methods for the patients menu and their Helper Methods  
    */
    
    
   // Method to add a Patient
    public void addPatient(String name, int age, String medicalHistory, Map<String, Integer> medicines,
            boolean isSurgeryRequired, int bedDays, int surgeryDays, String contactNumber) {
			Session session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();
			
			Patient patient = new Patient();
			patient.setName(name);
			patient.setAge(age);
			patient.setMedicalHistory(medicalHistory);
			patient.setPrescribedMedicines(medicines);
			patient.setSurgeryRequired(isSurgeryRequired);
			patient.setBedDays(bedDays);
			patient.setSurgeryDays(surgeryDays);
			patient.setContactNumber(contactNumber);
			
			session.persist(patient);
			transaction.commit();
			session.close();
			
			//Now dispense the medicines for the patient
			dispenseMedicines(medicines); // Call dispenseMedicines method

    	}
     
    // Method to View Patient
    public Patient getPatient(Long id) {
	    Session session = sessionFactory.openSession();
	    Patient patient = session.get(Patient.class, id);
	    session.close();
	    return patient;
	}
     
   // Method to Update the Patient  
    @SuppressWarnings("deprecation")
  	public void updatePatient(Long id, String name, int age, String medicalHistory, Map<String, Integer> medicines,
              boolean isSurgeryRequired, int bedDays, int surgeryDays, String contactNumber) {
  Session session = sessionFactory.openSession();
  Transaction transaction = session.beginTransaction();

  Patient patient = session.get(Patient.class, id);
  if (patient != null) {
  patient.setName(name);
  patient.setAge(age);
  patient.setMedicalHistory(medicalHistory);
  patient.setPrescribedMedicines(medicines);
  patient.setSurgeryRequired(isSurgeryRequired);
  patient.setBedDays(bedDays);
  patient.setSurgeryDays(surgeryDays);
  patient.setContactNumber(contactNumber);
  
  session.update(patient);
  }
  transaction.commit();
  session.close();
  }
    
    //Method to Delete Patient
    public void deletePatient(Long id) {
	    Session session = sessionFactory.openSession();
	    Transaction transaction = session.beginTransaction();

	    Patient patient = session.get(Patient.class, id);
	    if (patient != null) {
	        session.remove(patient);
	    }
	    transaction.commit();
	    session.close();
	}
    
    
    
    
 /*
  * Here We have the methods for the Pharamacy Menu and their Helper Methods 
  */
    
    
    // Method to add Medicine
    public void addMedicine(String type, int quantity, double price) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        Medicine medicine = new Medicine(type, quantity, price);
        session.persist(medicine);

        transaction.commit();
        session.close();
        
    }

    // Method to restock medicine
    public void restockMedicine(String type, int quantity, Double newPrice) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        Medicine medicine = session.createQuery("FROM Medicine WHERE type = :type", Medicine.class)
                                    .setParameter("type", type)
                                    .uniqueResult();
        if (medicine != null) {
            medicine.setQuantity(medicine.getQuantity() + quantity);
            if (newPrice != null) {
                medicine.setPrice(newPrice); // Update the price if provided
            }
            session.update(medicine);
        } else {
            System.out.println("No medicine found for type: " + type);
        }

        transaction.commit();
        session.close();
    }
    
    
    //Method to View All the Medicines ( Stock in the Pharmacy)
    public Map<String, Integer> viewAllMedicines() {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        Map<String, Integer> medicinesMap = new HashMap<>();

        try {
            transaction = session.beginTransaction();
            List<Medicine> medicines = session.createQuery("FROM Medicine", Medicine.class).getResultList();
            transaction.commit();

            // Populate the map with medicine types and their quantities
            for (Medicine medicine : medicines) {
                medicinesMap.put(medicine.getType(), medicine.getQuantity());
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return medicinesMap; // Ensure you return the map
    }

 	
    // This is a helper method to remove the medicines from stock when prescribed 
    public void dispenseMedicines(Map<String, Integer> prescribedMedicines) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        for (Map.Entry<String, Integer> entry : prescribedMedicines.entrySet()) {
            String medicineType = entry.getKey();
            int quantityToDispense = entry.getValue();

            // Find the medicine in stock
            Medicine medicine = session.createQuery("FROM Medicine WHERE type = :type", Medicine.class)
                                        .setParameter("type", medicineType)
                                        .uniqueResult();
            if (medicine != null) {
                int currentStock = medicine.getQuantity();
                if (currentStock >= quantityToDispense) {
                    // Update the stock
                    medicine.setQuantity(currentStock - quantityToDispense);
                    session.update(medicine);
                } else {
                    System.out.println("Not enough stock for " + medicineType);
                }
            } else {
                System.out.println("No medicine found for type: " + medicineType);
            }
        }

        transaction.commit();
        session.close();
    }

    //This is a Helper Method for getting the medicine based on their type ( becuase we dont have id for medicine)
    public Medicine getMedicine(String type) {
        Transaction transaction = null;
        Medicine medicine = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Query to fetch the medicine by type
            String hql = "FROM Medicine WHERE type = :type";
            medicine = session.createQuery(hql, Medicine.class)
                    .setParameter("type", type)
                    .uniqueResult();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return medicine;
    }
    
   // This is method to Generate a detailed Bill of each Patient  
    public void generateBill(Long patientId) {
        try (Session session = sessionFactory.openSession()) {
            Patient patient = session.get(Patient.class, patientId);
            if (patient != null) {
                
                billingSystem.printDetailedBill(patient);
            } else {
                System.out.println("No patient found with the given ID.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This is a Helper method which brings the prescribed medicines from the database based 
    public Patient getPatientWithMedicines(Long id) {
        Transaction transaction = null;
        Patient patient = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Fetch the patient by ID
            patient = session.get(Patient.class, id);

            // Initialize the prescribedMedicines collection if patient is found
            if (patient != null) {
                Hibernate.initialize(patient.getPrescribedMedicines());
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return patient;
    }
    
    // Method to get medicine price by type
    public double getMedicinePrice(String medicineType) {
        Transaction transaction = null;
        Double price = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<Double> query = session.createQuery("SELECT price FROM Medicine WHERE type = :type", Double.class);
            query.setParameter("type", medicineType);
            price = query.uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
        return (price != null) ? price : 0.0;
    }
    
   
    
    
    
    
   /*
    * This part  Contains the methods for the Reports Menu and their Helper Methods
    */
    
    
    
    
    // Method to get all patients in the database
    public List<Patient> getAllPatients() {
        Transaction transaction = null;
        List<Patient> patients = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<Patient> query = session.createQuery("FROM Patient", Patient.class);
            patients = query.list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
        return patients;
    }

    // Method to Caluculate the total Earinings of the Hospital by adding the Bill of the individual Patients 
    public double calculateTotalEarnings() {
        double totalEarnings = 0.0;

        // Fetch all patients
        List<Patient> patients = getAllPatients();
        
        // Check if patients list is not empty
        if (patients == null || patients.isEmpty()) {
            System.out.println("No patients found in the database.");
            return totalEarnings; // Return 0 if no patients
        }

        System.out.println("Patient Bills:");
        System.out.println("--------------------------");
        for (Patient patient : patients) {
            double patientBill = calculateTotalBill(patient);
            totalEarnings += patientBill;

            // Print patient bill details
            System.out.println("Patient ID: " + patient.getId());
            System.out.println("Patient Name: " + patient.getName());
            System.out.println("Patient Age: " + patient.getAge());
            System.out.println("Patient Contact Number: " + patient.getContactNumber());
            System.out.println("Total Bill: " + patientBill);
            System.out.println("--------------------------");
        }

        // Print total earnings
       
        return totalEarnings;
    }
    
    // Declaring the Cost of BedPrice and SurgeryPrice per Day
    private static final double BED_DAY_COST = 200.0;
    private static final double SURGERY_DAY_COST = 500.0;
    
    
  
    // Method to calculate total bill for a patient
    public double calculateTotalBill(Patient patient) {
        double totalBill = 0.0;

        // Calculate the cost of prescribed medicines
        for (Map.Entry<String, Integer> entry : patient.getPrescribedMedicines().entrySet()) {
            String medicineType = entry.getKey();
            int quantity = entry.getValue();
            double price = getMedicinePrice(medicineType);
            totalBill += price * quantity; // Add medicine cost
        }

        // Add costs for bed days and surgery days
        totalBill += calculateOtherCosts(patient);

        return totalBill; // Return the total bill amount
    }

    // Helper Method to calculate other costs (bed days and surgery days)
    private double calculateOtherCosts(Patient patient) {
        double totalOtherCosts = 0.0;

        
        int bedDays = patient.getBedDays(); // Get the number of bed days
        int surgeryDays = patient.getSurgeryDays(); // Get the number of surgery days

        // Calculate costs
        totalOtherCosts += bedDays * BED_DAY_COST; // Cost for bed days
        totalOtherCosts += surgeryDays * SURGERY_DAY_COST; // Cost for surgery days

        return totalOtherCosts; // Return total other costs
    }
   
    
    
    // Appointment Booking related Methods 
    public boolean addAppointment(LocalDate date, Doctor doctor, Patient patient, Shift shift) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Create a new Appointment object
            Appointment appointment = new Appointment(date, doctor, patient, shift);
            
            // Save the appointment to the database
            session.save(appointment);

            // Commit the transaction
            transaction.commit();
            return true; 
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace(); 
            return false; 
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        
    }
    
    // Method to get appointments by doctor ID
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        List<Appointment> appointments = null;
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Appointment WHERE doctor.id = :doctorId";
            appointments = session.createQuery(hql, Appointment.class)
                                  .setParameter("doctorId", doctorId)
                                  .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }
    
    
    // Helper Method to get a patient by ID
    public Patient getPatientById(Long patientId) {
        Session session = HibernateUtil.getSessionFactory().openSession(); 
        Transaction transaction = null;
        Patient patient = null;

        try {
            transaction = session.beginTransaction();
            patient = session.get(Patient.class, patientId); // Fetch the patient by ID
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        return patient; // Return the found patient or null if not found
    }
    
    
    
    // Method to add a doctor
    public void addDoctor(String name, String specialization) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession(); // Open session
            transaction = session.beginTransaction();

            Doctor doctor = new Doctor(name, specialization);
            session.save(doctor); // Save the doctor to the database

            transaction.commit(); // Commit the transaction
            System.out.println("Doctor added successfully!");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback if there's an exception
            }
            e.printStackTrace(); // Log the exception
        } finally {
            if (session != null && session.isOpen()) {
                session.close(); // Close the session in the finally block
            }
        }
    }
    
    // Method to get Doctor by ID
    public Doctor getDoctorById(Long doctorId) {
        Session session = HibernateUtil.getSessionFactory().openSession(); // Assume you have a HibernateUtil for session management
        Transaction transaction = null;
        Doctor doctor = null;

        try {
            transaction = session.beginTransaction();
            doctor = session.get(Doctor.class, doctorId); // Fetch the doctor by ID
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        return doctor; // Return the found doctor or null if not found
    }
    
    // Method to get booked shifts for a specific doctor and date
    public List<Shift> getBookedShiftsForDoctorOnDate(Long doctorId, LocalDate date) {
        Session session = null;
        Transaction transaction = null;
        List<Shift> bookedShifts = new ArrayList<>();

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Query to get booked shifts for a doctor on a specific date
            String hql = "SELECT a.shift FROM Appointment a WHERE a.doctor.id = :doctorId AND a.date = :date";  // Go the 
            Query<Shift> query = session.createQuery(hql, Shift.class);
            query.setParameter("doctorId", doctorId);
            query.setParameter("date", date);

            bookedShifts = query.list();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return bookedShifts;
    }
    
    // Helper Method to get available shifts for a given doctor and date
    public Set<Shift> getAvailableShifts(Long doctorId, LocalDate date) {
        // Fetch booked shifts for the doctor on the given date
        List<Shift> bookedShifts = getBookedShiftsForDoctorOnDate(doctorId, date);

        // All possible shifts
        Set<Shift> allShifts = EnumSet.allOf(Shift.class);

        // Remove booked shifts from the set of all shifts to get the available ones
        allShifts.removeAll(bookedShifts);

        return allShifts;
    }
    
    
    // Method to list all doctors
    public void listAllDoctors() {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Fetch all doctors from the database
            List<Doctor> doctors = session.createQuery("FROM Doctor", Doctor.class).list();

            // Display all doctors
            System.out.println("List of All Doctors:");
            for (Doctor doctor : doctors) {
                System.out.println("ID: " + doctor.getId());
                System.out.println("Name: " + doctor.getName());
                System.out.println("Specialisatoin :" + doctor.getSpecialization());               
                System.out.println("Appointments Count: " + doctor.getAppointments().size());
                System.out.println("-----------------------------------");
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
 // Helper Method to get available shifts for a given doctor and date ( this method is same as the get available shift for doctor on date but just without using Hib Ses)
    public Set<Shift> getAvailableShiftsForDoctor(Long doctorId, LocalDate date) {
        // Get booked shifts for the doctor on the specified date
        List<Shift> bookedShifts = getBookedShiftsForDoctorOnDate(doctorId, date);

        // Initialize all shifts and remove booked shifts
        Set<Shift> availableShifts = EnumSet.allOf(Shift.class);
        availableShifts.removeAll(bookedShifts); // Remove booked shifts

        return availableShifts; // Return available shifts
    }
    
    
    
    }
    


    


    

    







