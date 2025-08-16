package com.boogie_knight.tcgtracker.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.boogie_knight.tcgtracker.utils.NetworkFetchUtils

data object AssetsRepository{
    const val BASE_URL = "https://raw.githubusercontent.com/adresa97/TCGTracker-database/refs/heads/main/"
    private var handler: GitHubHandler? = null

    fun init(context: Context) {
        handler = GitHubHandler(context)
        checkVersion()
    }

    fun getData(
        path: String
    ): String {
        if (handler == null) return ""
        var outString = handler!!.getData(path)
        if (outString == null) {
            outString = NetworkFetchUtils.fetch(BASE_URL + path)
            if (outString.isNotEmpty()) handler!!.saveData(path, outString)
        }
        return outString
    }

    fun checkVersion() {
        if (handler == null) return
        val local = handler!!.getVersion()
        val remote = NetworkFetchUtils.fetch(BASE_URL + "PTCGPocket/version").toIntOrNull()
        if (remote != null && (local == null || remote > local)) {
            handler!!.reset()
            handler!!.saveVersion(remote)
        }
    }
}

class GitHubHandler(
    context: Context?
): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)  {
    override fun onCreate(db: SQLiteDatabase) {
        var query = (
            """
            CREATE TABLE IF NOT EXISTS ${ASSETS_TABLE} (
            ${NAME_COL} TEXT NOT NULL,
            ${DATA_COL} BLOB NOT NULL,
            CONSTRAINT Assets_PK PRIMARY KEY (${NAME_COL})
            );
            """
        )
        db.execSQL(query)

        query = (
            """
            CREATE TABLE IF NOT EXISTS ${VERSION_TABLE} (
            ${VERSION_COL} INTEGER NOT NULL,
            CONSTRAINT Version_PK PRIMARY KEY (${VERSION_COL})
            );
            """
        )
        db.execSQL(query)
    }

    fun getData(
        name: String
    ): String? {
        val db = this.readableDatabase

        val query = "SELECT ${DATA_COL} FROM ${ASSETS_TABLE} WHERE ${NAME_COL}=?"
        val cursorAssets: Cursor = db.rawQuery(query, arrayOf(name))

        var outString: String? = null
        if (cursorAssets.moveToFirst()) {
            outString = String(cursorAssets.getBlob(0))
        }

        cursorAssets.close()
        return outString
    }

    fun saveData(
        name: String,
        data: String
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(NAME_COL, name)
        values.put(DATA_COL, data.toByteArray())

        db.replace(ASSETS_TABLE, null, values)
        db.close()
    }

    fun getVersion(): Int? {
        val db = this.readableDatabase

        val query = "SELECT ${VERSION_COL} FROM ${VERSION_TABLE}"
        val cursorVersion: Cursor = db.rawQuery(query, arrayOf())

        var outInt: Int? = null
        if (cursorVersion.moveToFirst()) {
            outInt = cursorVersion.getInt(0)
        }

        cursorVersion.close()
        return outInt
    }

    fun saveVersion(
        version: Int
    ) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(VERSION_COL, version)

        db.replace(VERSION_TABLE, null, values)
        db.close()
    }

    fun reset() {
        val db = this.writableDatabase
        db.delete(ASSETS_TABLE, null, arrayOf())
        db.delete(VERSION_TABLE, null, arrayOf())
        db.close()
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS ${ASSETS_TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VERSION_TABLE}")

        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "assetsData"
        private const val DB_VERSION = 1
        private const val ASSETS_TABLE = "Assets"
        private const val VERSION_TABLE = "Version"
        private const val NAME_COL = "name"
        private const val DATA_COL = "data"
        private const val VERSION_COL = "version"
    }
}