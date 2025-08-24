package com.cadebray.healthmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@SuppressWarnings("unused")
@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE email = :email")
    User getUser(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User authenticateUser(String email, String password);
}
