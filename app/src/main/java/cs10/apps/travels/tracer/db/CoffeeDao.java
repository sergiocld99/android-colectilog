package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import cs10.apps.travels.tracer.model.Coffee;

@Dao
public interface CoffeeDao {

    @Insert
    void insert(Coffee coffee);

    @Query("SELECT SUM(price) FROM coffee WHERE month is :month")
    double getTotalSpent(int month);
}
