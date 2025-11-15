package ru.netology.nmadia_hw.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object PostsTable {
    const val TABLE = "posts"
    const val COLUMN_ID = "id"
    const val COLUMN_AUTHOR = "author"
    const val COLUMN_PUBLISHED = "published"
    const val COLUMN_CONTENT = "content"
    const val COLUMN_LIKED_BY_ME = "likedByMe"
    const val COLUMN_LIKES = "likes"
    const val COLUMN_SHARES = "shares"
    const val COLUMN_VIEWS = "views"
    const val COLUMN_VIDEO = "video"
}

private const val DB_NAME = "posts.db"
private const val DB_VERSION = 1

class PostDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${PostsTable.TABLE} (
                ${PostsTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${PostsTable.COLUMN_AUTHOR} TEXT NOT NULL,
                ${PostsTable.COLUMN_PUBLISHED} TEXT NOT NULL,
                ${PostsTable.COLUMN_CONTENT} TEXT NOT NULL,
                ${PostsTable.COLUMN_LIKED_BY_ME} INTEGER NOT NULL DEFAULT 0,
                ${PostsTable.COLUMN_LIKES} INTEGER NOT NULL DEFAULT 0,
                ${PostsTable.COLUMN_SHARES} INTEGER NOT NULL DEFAULT 0,
                ${PostsTable.COLUMN_VIEWS} INTEGER NOT NULL DEFAULT 0,
                ${PostsTable.COLUMN_VIDEO} TEXT
            );
            """.trimIndent()
        )

        // seed-данные
        db.execSQL(
            """
            INSERT INTO ${PostsTable.TABLE}(
                ${PostsTable.COLUMN_AUTHOR},
                ${PostsTable.COLUMN_PUBLISHED},
                ${PostsTable.COLUMN_CONTENT},
                ${PostsTable.COLUMN_LIKED_BY_ME},
                ${PostsTable.COLUMN_LIKES},
                ${PostsTable.COLUMN_SHARES},
                ${PostsTable.COLUMN_VIEWS},
                ${PostsTable.COLUMN_VIDEO}
            ) VALUES
            (
                'Нетология',
                '21 мая в 18:36',
                'Привет, это новая Нетология! …',
                0,
                10,
                0,
                0,
                NULL
            ),
            (
                'Demo',
                'Сегодня',
                'Пост с видео!',
                0,
                1,
                0,
                0,
                'https://rutube.ru/video/6550a91e7e523f9503bed47e4c46d0cb'
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${PostsTable.TABLE}")
        onCreate(db)
    }
}
