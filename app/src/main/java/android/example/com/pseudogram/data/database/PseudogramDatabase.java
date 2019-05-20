package android.example.com.pseudogram.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {UserEntry.class, PostEntry.class}, version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class PseudogramDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "practiceApp";

    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static volatile PseudogramDatabase sInstance;

    public static PseudogramDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            PseudogramDatabase.class, PseudogramDatabase.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return sInstance;
    }

    public abstract UserDao userDao();
    public abstract PostDao postDao();
}
