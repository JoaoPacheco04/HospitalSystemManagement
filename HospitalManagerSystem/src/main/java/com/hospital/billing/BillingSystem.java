package com.hospital.billing;
/*
 * This is the Class where the billing is done.

 *  Detailed caluculation of a patien bill
 *  Calculate individual bills
 *  gettting medicne price from database
 * 
 * 
 */
import com.hibernate.util.HibernateUtil;
import com.hospital.entity.Medicine;
import com.hospital.entity.Patient;
import com.hospital.manager.HospitalManager;

import antlr.collections.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Map;

public class BillingSystem {
    private final HospitalManager manager;

    public BillingSystem(HospitalManager manager) {
        this.manager = manager;
    }

    // Method to print a detailed bill for a patient
    public void printDetailedBill(Patient patient) {
        double totalCost = 0.0;
        System.out.println("");
        System.out.println("==== Detailed Bill for Patient ====");
        System.out.println("Patient Name: " + patient.getName());
        System.out.println("Patient ID: " + patient.getId());

        // Detailed bill for prescribed medicines
        System.out.println("Prescribed Medicines:");
        
        try (Session session = manager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Iterate over each prescribed medicine
            for (Map.Entry<String, Integer> entry : patient.getPrescribedMedicines().entrySet()) {
                String medicineType = entry.getKey();
                int quantity = entry.getValue();

                // Fetch medicine by type (instead of ID )
                Medicine medicine = session.createQuery("FROM Medicine WHERE type = :type", Medicine.class)
                        .setParameter("type", medicineType)
                        .uniqueResult();

                if (medicine != null) {
                    double cost = medicine.getPrice() * quantity;
                    totalCost += cost;

                    System.out.printf("- %s: Quantity = %d, Price per Unit = %.2f, Cost = %.2f%n",
                            medicine.getType(), quantity, medicine.getPrice(), cost);
                } else {
                    System.out.printf("- %s: Quantity = %d (No price information available)%n", medicineType, quantity);
                }
            }

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Calculate and display the cost for bed and surgery days
        int bedDays = patient.getBedDays();
        int surgeryDays = patient.getSurgeryDays();
        double bedDayCost = bedDays * 100.0; //  cost per bed day
        double surgeryDayCost = surgeryDays * 500.0; // per surgery day

        System.out.printf("Bed Days: %d, Cost per Day = %.2f, Total = %.2f%n", bedDays, 100.0, bedDayCost);
        System.out.printf("Surgery Days: %d, Cost per Day = %.2f, Total = %.2f%n", surgeryDays, 500.0, surgeryDayCost);

        totalCost += bedDayCost + surgeryDayCost;

        // Print the final total cost
        System.out.printf("Total Cost: %.2f%n", totalCost);
        System.out.println("=================================");
    }
    
    public double calculateTotalBill(Patient patient) {
        double totalBill = 0.0;

        // Get prescribed medicines
        Map<String, Integer> prescribedMedicines = patient.getPrescribedMedicines();

        // Iterate through each prescribed medicine
        for (Map.Entry<String, Integer> entry : prescribedMedicines.entrySet()) {
            String medicineType = entry.getKey(); // Type of medicine
            int quantity = entry.getValue(); // Quantity prescribed

            // Fetch the medicine price from the database
            double medicinePrice = getMedicinePrice(medicineType); // Assuming you have this method defined

            // Calculate total cost for this medicine
            totalBill += medicinePrice * quantity;
        }

        // Additional charges (e.g., bed days, surgery, etc.) can be added here if needed

        return totalBill; // Return the total bill for the patient
    }
    
    
    public double getMedicinePrice(String medicineName) {
        Double price = null; // Initialize a variable to hold the fetched price

        // Logic to fetch medicine price from the database
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT price FROM Medicine m WHERE m.name = :medicineName";
            price = (Double) session.createQuery(hql)
                    .setParameter("medicineName", medicineName)
                    .uniqueResult(); // Fetch the price directly

        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions
        }

        // Return 0.0 if price is null
        if (price == null) {
            return 0.0;
        }
        return price; // Return the fetched price
    }
   
    

   
    
    
  
   }
