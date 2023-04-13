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

    @Query("SELECT MAX(id) FROM coffee")
    Long getLastId();

    @Query("SELECT SUM(price) FROM coffee where id > :coffeeId")
    double getSpentSince(long coffeeId);

    @Query("SELECT price FROM coffee order by id desc limit 1")
    Double getLastPrice();

    @Query("SELECT * FROM coffee order by id desc limit 1")
    Coffee getLastCoffee();
}
