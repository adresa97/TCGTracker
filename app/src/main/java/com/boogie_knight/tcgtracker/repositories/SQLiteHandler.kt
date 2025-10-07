package com.boogie_knight.tcgtracker.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.boogie_knight.tcgtracker.models.SQLFilterConfig
import com.boogie_knight.tcgtracker.models.SQLOwnedCard
import com.boogie_knight.tcgtracker.models.SQLOwnedSet

data object UserRepository{
    private var handler: SQLiteHandler? = null

    fun init(context: Context) {
        handler = SQLiteHandler(context)
    }

    fun saveCards(
        cards: List<SQLOwnedCard>
    ) {
        if (handler == null) return
        if (cards.isEmpty()) return
        handler!!.saveCardsInBatches(cards)
    }

    fun saveSets(
        sets: List<SQLOwnedSet>
    ) {
        if (handler == null) return
        if (sets.isEmpty()) return
        handler!!.saveSetsInBatches(sets)
    }

    fun saveFilters(
        filters: List<SQLFilterConfig>
    ) {
        if (handler == null) return
        if (filters.isEmpty()) return
        handler!!.saveFiltersInBatches(filters)
    }

    fun getCardsBySet(
        set: String
    ): Map<String, Boolean> {
        if (handler == null) return mapOf()
        return handler!!.getCardsBySet(set)
    }

    fun getSetPrecalculations(
        set: String,
        booster: String = "",
        rarity: String = ""
    ): Pair<Int, Int> {
        if (handler == null) return Pair(0, 0)
        return handler!!.getSetPrecalculations(set, booster, rarity)
    }

    fun getFilterState(
        filter: String,
        type: String
    ): Boolean? {
        if (handler == null) return null
        return handler!!.getFilterState(filter, type)
    }
}

class SQLiteHandler(
    context: Context?
): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)  {
    override fun onCreate(db: SQLiteDatabase) {
        var query = (
            """
            CREATE TABLE IF NOT EXISTS ${CARD_TABLE} (
            ${ID_COL} TEXT NOT NULL,
            ${OWNED_COL} INTEGER NOT NULL,
            ${SET_COL} TEXT NOT NULL,
            CONSTRAINT Card_PK PRIMARY KEY (${ID_COL})
            );
            """
        )
        db.execSQL(query)

        query = (
            """
            CREATE TABLE IF NOT EXISTS ${SET_TABLE} (
            ${SET_COL} TEXT NOT NULL,
            ${BOOSTER_COL} TEXT NOT NULL,
            ${RARITY_COL} TEXT NOT NULL,
            ${OWNED_COL} INTEGER NOT NULL,
            ${TOTAL_COL} INTEGER NOT NULL,
            CONSTRAINT Set_PK PRIMARY KEY (${SET_COL},${BOOSTER_COL},${RARITY_COL})
            );
            """
        )
        db.execSQL(query)

        query = (
            """
            CREATE TABLE IF NOT EXISTS ${FILTER_TABLE} (
            ${FILTER_COL} TEXT NOT NULL,
            ${TYPE_COL} TEXT NOT NULL,
            ${STATE_COL} INTEGER NOT NULL,
            CONSTRAINT Filter_PK PRIMARY KEY (${FILTER_COL},${TYPE_COL})
            );
            """
        )
        db.execSQL(query)
    }

    private fun addNewCard(
        id: String,
        set: String,
        isOwned: Boolean
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(ID_COL, id)
        values.put(SET_COL, set)
        values.put(OWNED_COL, isOwned)

        db.insert(CARD_TABLE, null, values)
        db.close()
    }

    private fun addNewSet(
        set: String,
        booster: String = "",
        rarity: String = "",
        owned: Int,
        total: Int
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(SET_COL, set)
        values.put(BOOSTER_COL, booster)
        values.put(RARITY_COL, rarity)
        values.put(OWNED_COL, owned)
        values.put(TOTAL_COL, total)

        db.insert(SET_TABLE, null, values)
        db.close()
    }

    private fun changeCard(
        id: String,
        isOwned: Boolean
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(OWNED_COL, isOwned)

        db.update(CARD_TABLE, values, "${ID_COL}=?", arrayOf(id)) == 0
        db.close()
    }

    private fun changeSet(
        set: String,
        booster: String?,
        rarity: String?,
        owned: Int
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(OWNED_COL, owned)

        db.update(SET_TABLE, values, "${SET_COL}=? AND ${BOOSTER_COL}=? AND ${RARITY_COL}=?", arrayOf(set, booster, rarity))
        db.close()
    }

    fun saveCard(
        id: String,
        set: String,
        isOwned: Boolean
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(ID_COL, id)
        values.put(SET_COL, set)
        values.put(OWNED_COL, isOwned)

        db.replace(CARD_TABLE, null, values)
        db.close()
    }

    fun saveCardsInBatches(
        cards: List<SQLOwnedCard>,
        batchSize: Int = 300
    ) {
        if (cards.isEmpty()) return

        val maxIndex = cards.size

        var startIndex = 0
        var endIndex = batchSize
        if (endIndex > maxIndex) endIndex = maxIndex
        while (startIndex < maxIndex) {
            saveCardsInBatch(cards.subList(startIndex, endIndex))

            startIndex = endIndex
            endIndex += batchSize
            if (endIndex > maxIndex) endIndex = maxIndex
        }
    }

    private fun saveCardsInBatch(
        cards: List<SQLOwnedCard>
    ) {
        if (cards.isEmpty()) return

        val db = this.writableDatabase

        var query = "REPLACE INTO ${CARD_TABLE} (${ID_COL}, ${SET_COL}, ${OWNED_COL}) VALUES "
        val arguments = mutableListOf<Any>()
        cards.forEach { card ->
            query += "(?, ?, ?),"
            arguments.add(card.id)
            arguments.add(card.set)
            arguments.add(card.isOwned)
        }
        query = query.dropLast(1)

        db.execSQL(query, arguments.toTypedArray())
        db.close()
    }

    fun saveSet(
        set: String,
        booster: String = "",
        rarity: String = "",
        owned: Int,
        total: Int
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(SET_COL, set)
        values.put(BOOSTER_COL, booster)
        values.put(RARITY_COL, rarity)
        values.put(OWNED_COL, owned)
        values.put(TOTAL_COL, total)

        db.replace(SET_TABLE, null, values)
        db.close()
    }

    fun saveSetsInBatches(
        sets: List<SQLOwnedSet>,
        batchSize: Int = 150
    ) {
        if (sets.isEmpty()) return

        val maxIndex = sets.size

        var startIndex = 0
        var endIndex = batchSize
        if (endIndex > maxIndex) endIndex = maxIndex
        while (startIndex < maxIndex) {
            saveSetsInBatch(sets.subList(startIndex, endIndex))

            startIndex = endIndex
            endIndex += batchSize
            if (endIndex > maxIndex) endIndex = maxIndex
        }
    }

    private fun saveSetsInBatch(
        sets: List<SQLOwnedSet>
    ) {
        if (sets.isEmpty()) return

        val db = this.writableDatabase

        var query = "REPLACE INTO ${SET_TABLE} (${SET_COL}, ${BOOSTER_COL}, ${RARITY_COL}, ${OWNED_COL}, ${TOTAL_COL}) VALUES "
        val arguments = mutableListOf<Any>()
        sets.forEach { set ->
            query += "(?, ?, ?, ?, ?),"
            arguments.add(set.set)
            arguments.add(set.booster)
            arguments.add(set.rarity)
            arguments.add(set.owned)
            arguments.add(set.total)
        }
        query = query.dropLast(1)

        db.execSQL(query, arguments.toTypedArray())
        db.close()
    }

    fun saveFiltersInBatches(
        filters: List<SQLFilterConfig>,
        batchSize: Int = 150
    ) {
        if (filters.isEmpty()) return

        val maxIndex = filters.size

        var startIndex = 0
        var endIndex = batchSize
        if (endIndex > maxIndex) endIndex = maxIndex
        while (startIndex < maxIndex) {
            saveFiltersInBatch(filters.subList(startIndex, endIndex))

            startIndex = endIndex
            endIndex += batchSize
            if (endIndex > maxIndex) endIndex = maxIndex
        }
    }

    private fun saveFiltersInBatch(
        filters: List<SQLFilterConfig>
    ) {
        if (filters.isEmpty()) return

        val db = this.writableDatabase

        var query = "REPLACE INTO ${FILTER_TABLE} (${FILTER_COL}, ${TYPE_COL}, ${STATE_COL}) VALUES "
        val arguments = mutableListOf<Any>()
        filters.forEach { filter ->
            query += "(?, ?, ?),"
            arguments.add(filter.filter)
            arguments.add(filter.type)
            arguments.add(filter.state)
        }
        query = query.dropLast(1)

        db.execSQL(query, arguments.toTypedArray())
        db.close()
    }

    fun getCardsBySet(
        set: String
    ): Map<String, Boolean> {
        val db = this.readableDatabase

        val query = "SELECT ${ID_COL},${OWNED_COL} FROM ${CARD_TABLE} WHERE ${SET_COL}=? ORDER BY ${ID_COL}"
        val cursorCards: Cursor = db.rawQuery(query, arrayOf(set))

        val outMap = mutableMapOf<String, Boolean>()
        if (cursorCards.moveToFirst()) {
            do {
                outMap.put(cursorCards.getString(0), cursorCards.getInt(1) == 1)
            } while (cursorCards.moveToNext())
        }

        cursorCards.close()
        return outMap
    }

    fun getSetPrecalculations(
        set: String,
        booster: String = "",
        rarity: String = ""
    ): Pair<Int, Int> {
        val db = this.readableDatabase

        val query: String ="SELECT ${OWNED_COL},${TOTAL_COL} FROM ${SET_TABLE} WHERE ${SET_COL}=? AND ${BOOSTER_COL}=? AND ${RARITY_COL}=?"
        val cursorSets: Cursor = db.rawQuery(query, arrayOf(set, booster, rarity))

        var outPair = Pair(0, 0)
        if (cursorSets.moveToFirst()) {
            outPair = Pair(cursorSets.getInt(0), cursorSets.getInt(1))
        }

        cursorSets.close()
        return outPair
    }

    fun getFilterState(
        filter: String,
        type: String
    ): Boolean? {
        val db = this.readableDatabase

        val query: String ="SELECT ${STATE_COL} FROM ${FILTER_TABLE} WHERE ${FILTER_COL}=? AND ${TYPE_COL}=?"
        val cursorFilters: Cursor = db.rawQuery(query, arrayOf(filter, type))

        var outBool: Boolean? = null
        if (cursorFilters.moveToFirst()) {
            outBool = cursorFilters.getInt(0) == 1
        }

        cursorFilters.close()
        return outBool
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS ${CARD_TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${SET_TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${FILTER_TABLE}")

        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "userData"
        private const val DB_VERSION = 1
        private const val CARD_TABLE = "OwnedCard"
        private const val SET_TABLE = "OwnedSet"
        private const val FILTER_TABLE = "FiltersConfig"
        private const val ID_COL = "id"
        private const val OWNED_COL = "owned"
        private const val SET_COL = "\"set\""
        private const val BOOSTER_COL = "booster"
        private const val RARITY_COL = "rarity"
        private const val TOTAL_COL = "total"
        private const val FILTER_COL = "name"
        private const val TYPE_COL = "kind"
        private const val STATE_COL = "state"
    }
}