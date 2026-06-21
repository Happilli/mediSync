package com.bca.medisync.data.model;
import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    public static Patient getCurrentPatient() {
        return new Patient(
                "p001",
                "Sirish Chaudhary",
                "sirish@gmail.com",
                "9800000000",
                "Bharatpur-12, Chainpur",
                "2001-05-15",
                "Male",
                "B+",
                "+977-9875481229"
        );
    }

    public static List<Doctor> getDoctors() {
        List<Doctor> list = new ArrayList<>();
        list.add(new Doctor("d001", "Dr. Kakashi Hatake", "Cardiologist", "4.8 - 8+ Years Exp", "Cardiology", "+977-9800000001", null));
        list.add(new Doctor("d002", "Dr. Tsunade Senju", "Dermatologist", "4.7 - 6+ Years Exp", "Dermatology", "+977-9800000002", null));
        list.add(new Doctor("d003", "Dr. Kiyomi Takada", "Neurologist", "4.6 - 10+ Years Exp", "Neurology", "+977-9800000003", null));
        list.add(new Doctor("d004", "Dr. Sakura Haruno", "Pediatrician", "4.9 - 5+ Years Exp", "Pediatrics", "+977-9800000004", null));
        return list;
    }

    public static List<Hospital> getHospitals() {
        List<Hospital> list = new ArrayList<>();
        list.add(new Hospital("h001", "Chitwan Medical College", "Bharatpur, Chitwan", "+977-056-123456", "https://cmc.edu.np", "A leading medical college and hospital in Chitwan.", 4.6, null));
        list.add(new Hospital("h002", "B&B Hospital", "Gwarko, Lalitpur", "+977-01-5199600", "https://bpkihs.edu", "One of the largest private hospitals in Nepal.", 4.5, null));
        return list;
    }

    public static List<Appointment> getAppointments() {
        List<Appointment> list = new ArrayList<>();
        list.add(new Appointment("a001", "Dr. Kakashi Hatake", "Cardiologist", "Cardiology Department", "25 May 2024", "10:30 AM", "Confirmed", ""));
        list.add(new Appointment("a002", "Dr. Tsunade Senju", "Dermatologist", "Dermatology Department", "02 Jun 2024", "11:00 AM", "Pending", ""));
        list.add(new Appointment("a003", "Dr. Kiyomi Takada", "Neurologist", "Neurology Department", "10 Jun 2024", "09:00 AM", "Scheduled", ""));
        return list;
    }

    public static List<Medication> getMedications() {
        List<Medication> list = new ArrayList<>();
        list.add(new Medication("m001", "Telma", "40mg", "Once Daily", "01:00 PM", "30 Days", false));
        list.add(new Medication("m002", "Ecosprin", "75mg", "Once Daily", "08:00 PM", "30 Days", false));
        list.add(new Medication("m003", "Vitamin D3", "", "Once Daily", "09:00 PM", "30 Days", false));
        list.add(new Medication("m001", "paracetamol", "40mg", "Once Daily", "11:59 PM", "30 Days", false));
        list.add(new Medication("m001", "Fentanyl", "40mg", "Once Daily", "12:01 AM", "30 Days", false));
        return list;

    }

    public static List<TimeSlot> getTimeSlots() {
        List<TimeSlot> list = new ArrayList<>();
        String[] times = {
                "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM",
                "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
                "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM",
                "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM"
        };
        boolean[] available = {
                true, true, false, true,
                true, false, true, true,
                true, false, true, true,
                true, true, true, true
        };
        for (int i = 0; i < times.length; i++) {
            list.add(new TimeSlot(times[i], available[i]));
        }
        return list;
    }
}