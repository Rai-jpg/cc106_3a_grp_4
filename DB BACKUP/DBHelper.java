package com.example.wellness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wellness.db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    private static final String TABLE_USER = "users";
    private static final String TABLE_MEDICINE = "medicine";
    private static final String TABLE_PERIOD = "period";

    // User table columns
    public static final String USER_ID = "id";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";
    public static final String USER_AGE = "age";
    public static final String USER_SEX = "sex";
    public static final String USER_PRESCRIPTION = "prescription";

    // Medicine table columns
    public static final String MEDICINE_ID = "id";
    public static final String MEDICINE_NAME = "medicineName";
    public static final String MEDICINE_PURPOSE = "purpose";
    public static final String MEDICINE_DATE = "date";
    public static final String MEDICINE_TIME = "time";
    public static final String MEDICINE_EXPIRATION = "expiration";
    public static final String MEDICINE_USER_ID = "user_id";
    public static final String MEDICINE_DAYS_TO_TAKE = "daysToTake";
    public static final String MEDICINE_HOURS_INTERVAL = "hoursInterval";
    public static final String MEDICINE_IS_EVERYDAY = "isEveryday";

    // Period table columns
    public static final String PERIOD_ID = "id";
    public static final String PERIOD_START_DATE = "startDate";
    public static final String PERIOD_CYCLE_LENGTH = "cycleLength";
    public static final String PERIOD_USER_ID = "user_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user table
        String createUserTable = "CREATE TABLE " + TABLE_USER + "(" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_NAME + " TEXT, " +
                USER_EMAIL + " TEXT UNIQUE, " +
                USER_PASSWORD + " TEXT, " +
                USER_AGE + " INTEGER, " +
                USER_SEX + " TEXT, " +
                USER_PRESCRIPTION + " TEXT)";
        db.execSQL(createUserTable);

        // Create medicine table
        String createMedicineTable = "CREATE TABLE " + TABLE_MEDICINE + "(" +
                MEDICINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEDICINE_USER_ID + " INTEGER NOT NULL, " +
                MEDICINE_NAME + " TEXT, " +
                MEDICINE_PURPOSE + " TEXT, " +
                MEDICINE_DATE + " TEXT, " +
                MEDICINE_TIME + " TEXT, " +
                MEDICINE_EXPIRATION + " TEXT, " +
                MEDICINE_DAYS_TO_TAKE + " TEXT, " +
                MEDICINE_IS_EVERYDAY + " INTEGER, " + // 0 for false, 1 for true
                MEDICINE_HOURS_INTERVAL + " INTEGER, " +
                "FOREIGN KEY(" + MEDICINE_USER_ID + ") REFERENCES " + TABLE_USER + "(" + USER_ID + "))";
        db.execSQL(createMedicineTable);

        // Create period table
        String createPeriodTable = "CREATE TABLE " + TABLE_PERIOD + "(" +
                PERIOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PERIOD_START_DATE + " TEXT, " +
                PERIOD_CYCLE_LENGTH + " INTEGER, " +
                PERIOD_USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + PERIOD_USER_ID + ") REFERENCES " + TABLE_USER + "(" + USER_ID + "))";
        db.execSQL(createPeriodTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERIOD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Insert user method
    public long insertUser(String name, int age, String sex, String email, String password, String prescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, name);
        values.put(USER_EMAIL, email);

        // No need to re-declare the password variable
        values.put(USER_PASSWORD, password);

        values.put(USER_AGE, age);
        values.put(USER_SEX, sex);
        values.put(USER_PRESCRIPTION, prescription);

        long id = db.insert(TABLE_USER, null, values);
        if (id > 0) {
            Log.d("DBHelper", "User inserted successfully with ID: " + id);
        } else {
            Log.e("DBHelper", "Error inserting user into database.");
        }
        db.close();
        return id;
    }





    // Check if user exists by email and password
    public boolean checkUser(String email, String inputPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_USER, new String[]{USER_PASSWORD}, USER_EMAIL + "=?", new String[]{email}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String password = cursor.getString(0);
                Log.d("LoginDebug", "Encrypted Password from DB: " + password);

                // Decrypt the password
                String decryptedPassword = password;
                Log.d("LoginDebug", "Decrypted Password: " + decryptedPassword);
                Log.d("LoginDebug", "Input Password: " + inputPassword);

                return decryptedPassword.equals(inputPassword);
            } else {
                Log.d("LoginDebug", "No user found with email: " + email);
            }
        } catch (Exception e) {
            Log.e("LoginDebug", "Error checking user: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return false;
    }


    // Update user's password
    public boolean updatePassword(long userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Directly store the password without encryption
        values.put(USER_PASSWORD, newPassword);

        int rows = db.update(TABLE_USER, values, USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }


    // Fetch user information
    public Cursor getUserInfo(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    // Delete user account
    public boolean deleteUser(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USER, USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    public boolean deleteMedicine(long medicineId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete the medicine from the database using its ID
        int rowsDeleted = db.delete(TABLE_MEDICINE, MEDICINE_ID + " = ?", new String[]{String.valueOf(medicineId)});
        db.close();
        return rowsDeleted > 0;
    }


    // Method to validate the current password
    public boolean validatePassword(long userId, String currentPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0); // Get the stored password
            cursor.close();
            db.close();
            // Compare the provided password with the stored password
            return storedPassword.equals(currentPassword);
        }
        if (cursor != null) cursor.close();
        db.close();
        return false;
    }

    // Retrieve user ID by email
    public long getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;

        try (Cursor cursor = db.query(TABLE_USER, new String[]{USER_ID}, USER_EMAIL + "=?", new String[]{email}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getLong(cursor.getColumnIndexOrThrow(USER_ID));
            }
        } finally {
            db.close();
        }
        return userId;
    }

    // Insert medicine method

    public long insertMedicine(long userId, String name, String purpose, String date, String time,
                               String expiration, String daysToTake, boolean isEveryday, int hoursInterval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(MEDICINE_USER_ID, userId);
        values.put(MEDICINE_NAME, name);
        values.put(MEDICINE_PURPOSE, purpose);
        values.put(MEDICINE_DATE, date);
        values.put(MEDICINE_TIME, time);
        values.put(MEDICINE_EXPIRATION, expiration);
        values.put(MEDICINE_DAYS_TO_TAKE, daysToTake);
        values.put(MEDICINE_IS_EVERYDAY, isEveryday ? 1 : 0);
        values.put(MEDICINE_HOURS_INTERVAL, hoursInterval);

        try {
            long id = db.insertOrThrow(TABLE_MEDICINE, null, values);
            Log.d("DBHelper", "Medicine inserted successfully with ID: " + id);
            return id;
        } catch (Exception e) {
            Log.e("DBHelper", "Error inserting medicine: " + e.getMessage(), e);
            return -1;
        } finally {
            db.close();
        }
    }

    // Insert period method
    public long insertPeriod(long userId, String startDate, int cycleLength) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PERIOD_USER_ID, userId);
        values.put(PERIOD_START_DATE, startDate);
        values.put(PERIOD_CYCLE_LENGTH, cycleLength);

        long result = db.insert(TABLE_PERIOD, null, values); // TABLE_PERIOD ensures correct table name is used
        db.close();
        return result;
    }

    public List<Medicine> getMedicinesForUser(long userId) {
        List<Medicine> medicineList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_MEDICINE, null, MEDICINE_USER_ID + " = ?", new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Medicine medicine = new Medicine(
                            cursor.getLong(cursor.getColumnIndexOrThrow(MEDICINE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MEDICINE_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MEDICINE_PURPOSE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MEDICINE_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MEDICINE_TIME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MEDICINE_EXPIRATION)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(MEDICINE_USER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MEDICINE_DAYS_TO_TAKE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(MEDICINE_IS_EVERYDAY)) == 1,
                            cursor.getInt(cursor.getColumnIndexOrThrow(MEDICINE_HOURS_INTERVAL))
                    );
                    medicineList.add(medicine);
                } while (cursor.moveToNext());
            } else {
                Log.w("DBHelper", "No medicines found for userId: " + userId);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error retrieving medicines for user", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return medicineList;
    }


    public List<MenstrualPeriod> getPeriodsForUser(long userId) {
        List<MenstrualPeriod> periods = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM period WHERE user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("startDate"));
                int cycleLength = cursor.getInt(cursor.getColumnIndexOrThrow("cycleLength"));

                MenstrualPeriod period = new MenstrualPeriod(id, userId, startDate, cycleLength);

                // Calculate the next period date
                String nextPeriodDate = calculateNextPeriod(startDate, cycleLength);
                period.setNextPeriodDate(nextPeriodDate);

                periods.add(period);
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        return periods;
    }

    private String calculateNextPeriod(String startDate, int cycleLength) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(startDate));
            calendar.add(Calendar.DAY_OF_YEAR, cycleLength);

            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }



}


