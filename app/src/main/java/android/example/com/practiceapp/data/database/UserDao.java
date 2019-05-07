package android.example.com.practiceapp.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(UserEntry... user);

    @Query("SELECT * FROM user WHERE email = :email")
    LiveData<UserEntry> getUserByEmail(String email);
}
