package android.example.com.pseudogram.data.database;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.Date;

@Dao
public interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(PostEntry... post);
    @Query("SELECT * FROM post ORDER BY date DESC")
    DataSource.Factory<Integer,PostEntry> getCurrentFeed();
    @Query("DELETE FROM post WHERE date >= :date")
    void deleteOldFeed(Date date);
    @Query("DELETE FROM post WHERE id = :id")
    void deletePost(String id);
}
