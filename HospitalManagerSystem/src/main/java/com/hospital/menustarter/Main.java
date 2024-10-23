package com.hospital.menustarter;

import com.hospital.manager.HospitalManager;
import com.hospital.menu.Menu;

/*
 * This is the Main Method 
 */

public class Main {
    public static void main(String[] args) {
        HospitalManager manager = new HospitalManager();
        Menu menu = new Menu(manager);
        
        // Start the menu
        menu.start();
    }
}