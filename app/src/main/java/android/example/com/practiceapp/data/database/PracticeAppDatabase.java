package android.example.com.practiceapp.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {UserEntry.class, PostEntry.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class PracticeAppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "practiceApp";

    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static volatile PracticeAppDatabase sInstance;

    public static PracticeAppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(), PracticeAppDatabase.class, PracticeAppDatabase.DATABASE_NAME).build();
                }
            }
        }
        return sInstance;
    }

    public abstract UserDao userDao();
    public abstract PostDao postDao();
}
