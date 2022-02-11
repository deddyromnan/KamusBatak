package com.romnan.kamusbatak.feature_dictionary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.romnan.kamusbatak.feature_dictionary.data.local.entity.EntryEntity

@Database(
    entities = [EntryEntity::class],
    version = 1
)
abstract class DictionaryDatabase: RoomDatabase() {

    abstract  val dao : DictionaryDao
}