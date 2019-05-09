package android.example.com.practiceapp.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(PostEntry... post);

    @Query("SELECT * FROM post WHERE author = :author")
    LiveData<PostEntry> getPostByAuthor(String author);
    @Query("SELECT * FROM post ORDER BY date DESC")
    LiveData<List<PostEntry>> getCurrentFeed();
    @Query("DELETE FROM post WHERE date < :date")
    void deleteOldFeed(Date date);
}
