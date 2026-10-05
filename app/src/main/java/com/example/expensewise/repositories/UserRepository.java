package com.example.expensewise.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensewise.database.AppDatabase;
import com.example.expensewise.database.UserDao;
import com.example.expensewise.models.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(User user, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = userDao.insertUser(user);
            if (callback != null) {
                callback.onResult(id);
            }
        });
    }

    public void update(User user) {
        executorService.execute(() -> userDao.updateUser(user));
    }

    public void delete(User user) {
        executorService.execute(() -> userDao.deleteUser(user));
    }

    public void getUserById(long userId, Callback<User> callback) {
        executorService.execute(() -> {
            User user = userDao.getUserById(userId);
            if (callback != null) {
                callback.onResult(user);
            }
        });
    }

    public LiveData<User> getUserByIdLiveData(long userId) {
        return userDao.getUserByIdLiveData(userId);
    }

    public void getUserByEmail(String email, Callback<User> callback) {
        executorService.execute(() -> {
            User user = userDao.getUserByEmail(email);
            if (callback != null) {
                callback.onResult(user);
            }
        });
    }

    public void login(String email, String password, Callback<User> callback) {
        executorService.execute(() -> {
            User user = userDao.login(email, password);
            if (callback != null) {
                callback.onResult(user);
            }
        });
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }
}
