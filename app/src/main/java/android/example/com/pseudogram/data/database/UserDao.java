package android.example.com.pseudogram.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(UserEntry... user);

    @Query("SELECT * FROM user WHERE email = :email")
    LiveData<UserEntry> getUserByEmail(String email);
}
