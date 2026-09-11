package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DiscipleEntity - Persists all unlocked and recruited disciples.
 */
@Entity(tableName = "disciples")
data class DiscipleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val title: String = "",
    val isMale: Boolean = true,
    val age: Int = 18,
    val lifespan: Int = 100,
    val realm: Int = 0,
    val realmExp: Int = 0,
    val element: Int = 0,
    val talentName: String = "Common Roots",
    val talentGrade: Int = 1,
    val currentTask: Int = 0,
    val atk: Int = 20,
    val def: Int = 15,
    val spd: Int = 10,
    val crit: Int = 5,
    val maxHp: Int = 100,
    val hp: Int = 100,
    val maxMp: Int = 50,
    val mp: Int = 50,
    val energy: Int = 100,
    val maxEnergy: Int = 100,
    val mood: Int = 100,
    val stress: Int = 0,
    val loyalty: Int = 100,
    val dailyWage: Int = 5,
    val alchemySkill: Int = 0,
    val bodyRefiningStage: Int = 0,
    val str: Int = 10,
    val agi: Int = 10,
    val intel: Int = 10,
    val lck: Int = 10,
    val vit: Int = 10,
    val wis: Int = 10,
    val cha: Int = 10,
    val equippedWeaponName: String = "",
    val equippedArmorName: String = "",
    val equippedRelicName: String = ""
)
