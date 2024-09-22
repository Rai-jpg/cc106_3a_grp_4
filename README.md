MainActivity.java

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the alarm manager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MedicineAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24, pendingIntent);
    }
}


MedicationReminder.Java

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
 import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MedicineAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Create the notification channel (required for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // unique id for the channel
            String channelId = "medicine_reminders";
            // name of the channel
            String channelName = "Medicine Reminders";
            // importance level of the channel
            int importance = NotificationManager.IMPORTANCE_HIGH;
            // creating channel
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            // enabling lights for the channel
            channel.enableLights(true);
            // setting light color for the channel
            channel.setLightColor(Color.RED);
            // enabling vibration for the channel
            channel.enableVibration(true);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        // unique id for the notification
        int notificationId = 1;
        // title of the notification
        String title = "Medicine Reminder";
        // text of the notification
        String text = "It's time to take your medicine.";
        // intent to launch when the notification is clicked
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        // creating the builder object
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicine_reminders");
        builder.setSmallIcon(R.drawable.ic_baseline_medication_24);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        // Display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}


MedicineAddActivity.java

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MedicineAddActivity extends AppCompatActivity {
    private EditText etMedicineName;
    private EditText etMedicineTime;
    private Button btnSave;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_add);

        etMedicineName = findViewById(R.id.etMedicineName);
        etMedicineTime = findViewById(R.id.etMedicineTime);
        btnSave = findViewById(R.id.btnSave);

        database = FirebaseDatabase.getInstance().getReference("Medicines");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String medicineName = etMedicineName.getText().toString();
                String medicineTime = etMedicineTime.getText().toString();
                Medicine medicine = new Medicine(medicineName, medicineTime);
                database.push().setValue(medicine);
            }
        });
    }
}


Medicine.Java

public class Medicine {
    private String medicineName;
    private String medicineTime;

    public Medicine(String medicineName, String medicineTime) {
        this.medicineName = medicineName;
        this.medicineTime = medicineTime;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getMedicineTime() {
        return medicineTime;
    }
}
