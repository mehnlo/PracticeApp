package android.example.com.practiceapp.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(PostEntry... post);

    @Query("SELECT * FROM post WHERE author = :author")
    LiveData<PostEntry> getPostByAuthor(String author);
}
