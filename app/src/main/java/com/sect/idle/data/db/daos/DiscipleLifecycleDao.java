package com.sect.idle.data.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.sect.idle.data.db.entities.DiscipleLifecycleEntity;
import java.util.List;

/**
 * DiscipleLifecycleDao - Room Data Access Object for DiscipleLifecycleEntity.
 * Provides high-performance database operations for lifecycle, needs, and cultivation tracking.
 */
@Dao
public interface DiscipleLifecycleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DiscipleLifecycleEntity disciple);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DiscipleLifecycleEntity> disciples);

    @Update
    void update(DiscipleLifecycleEntity disciple);

    @Delete
    void delete(DiscipleLifecycleEntity disciple);

    @Query("SELECT * FROM disciple_lifecycles WHERE disciple_id = :discipleId LIMIT 1")
    DiscipleLifecycleEntity getById(String discipleId);

    @Query("SELECT * FROM disciple_lifecycles ORDER BY realm DESC, sub_stage DESC, cultivation_exp DESC")
    List<DiscipleLifecycleEntity> getAll();

    @Query("SELECT * FROM disciple_lifecycles WHERE current_state != 10 AND current_state != 11 ORDER BY realm DESC")
    List<DiscipleLifecycleEntity> getActiveDisciples();

    @Query("SELECT * FROM disciple_lifecycles WHERE current_state = :state")
    List<DiscipleLifecycleEntity> getByState(int state);

    @Query("SELECT * FROM disciple_lifecycles WHERE realm = :realm")
    List<DiscipleLifecycleEntity> getByRealm(int realm);

    @Query("SELECT * FROM disciple_lifecycles WHERE lifecycle_stage = :stage")
    List<DiscipleLifecycleEntity> getByLifecycleStage(int stage);

    @Query("SELECT * FROM disciple_lifecycles WHERE hunger <= :threshold AND current_state != 11")
    List<DiscipleLifecycleEntity> getHungryDisciples(int threshold);

    @Query("SELECT * FROM disciple_lifecycles WHERE energy <= :threshold AND current_state != 11")
    List<DiscipleLifecycleEntity> getExhaustedDisciples(int threshold);

    @Query("SELECT * FROM disciple_lifecycles WHERE cultivation_exp >= max_cultivation_exp AND current_state != 10 AND current_state != 11")
    List<DiscipleLifecycleEntity> getBreakthroughReadyDisciples();

    @Query("SELECT COUNT(*) FROM disciple_lifecycles WHERE current_state != 10 AND current_state != 11")
    int getActiveDiscipleCount();

    @Query("DELETE FROM disciple_lifecycles WHERE disciple_id = :discipleId")
    void deleteById(String discipleId);

    @Query("DELETE FROM disciple_lifecycles")
    void deleteAll();
}
