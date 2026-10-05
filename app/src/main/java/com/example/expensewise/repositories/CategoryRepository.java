package com.example.expensewise.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensewise.database.AppDatabase;
import com.example.expensewise.database.CategoryDao;
import com.example.expensewise.models.Category;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private final CategoryDao categoryDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    public CategoryRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.categoryDao = database.categoryDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Category category, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = categoryDao.insertCategory(category);
            if (callback != null) {
                callback.onResult(id);
            }
        });
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(String type) {
        return categoryDao.getCategoriesByType(type);
    }

    public void getAllCategoriesSync(Callback<List<Category>> callback) {
        executorService.execute(() -> {
            List<Category> list = categoryDao.getAllCategoriesSync();
            if (callback != null) {
                callback.onResult(list);
            }
        });
    }
}
