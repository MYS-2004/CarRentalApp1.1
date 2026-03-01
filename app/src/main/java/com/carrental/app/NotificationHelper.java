package com.carrental.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "car_rental_channel";
    public static final String CHANNEL_NAME = "إشعارات الحجز";
    public static final int NOTIF_REMINDER = 1001;
    public static final int NOTIF_EXPIRED  = 1002;

    public static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("تنبيهات متعلقة بحجوزات السيارات");
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /** تنبيه فوري - يُستدعى بعد تأكيد الحجز */
    public static void showBookingConfirmed(Context ctx, String carName, String endDate) {
        createChannel(ctx);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("✅ تم تأكيد حجزك!")
                .setContentText("سيارة " + carName + " محجوزة حتى " + endDate)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_REMINDER, builder.build());
    }

    /** تنبيه مجدوَل قبل انتهاء الحجز بساعة */
    public static void scheduleExpiryReminder(Context ctx, int bookingId, long endTimeMillis, String carName) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(ctx, BookingReminderReceiver.class);
        intent.putExtra("carName", carName);
        intent.putExtra("type", "reminder");

        PendingIntent pi = PendingIntent.getBroadcast(ctx, bookingId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = endTimeMillis - (60 * 60 * 1000L); // قبل ساعة
        if (triggerTime > System.currentTimeMillis()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        }
    }

    /** مستقبل التنبيهات */
    public static class BookingReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            createChannel(ctx);
            String carName = intent.getStringExtra("carName");
            String type = intent.getStringExtra("type");

            String title, body;
            if ("reminder".equals(type)) {
                title = "⏰ تذكير بانتهاء الحجز";
                body = "حجزك لسيارة " + carName + " ينتهي خلال ساعة";
            } else {
                title = "🚗 انتهت مدة الاستئجار";
                body = "يرجى إرجاع سيارة " + carName + " في أقرب وقت";
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
