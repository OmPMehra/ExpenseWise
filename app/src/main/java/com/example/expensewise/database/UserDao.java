package com.example.expensewise.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensewise.models.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(long userId);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<User> getUserByIdLiveData(long userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE (email = :identifier OR name = :identifier) AND passwordHash = :password LIMIT 1")
    User login(String identifier, String password);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();
}
