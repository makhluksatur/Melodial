package com.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "music_love.db";
    public static final int DATABASE_VERSION = 5;

    public static final String TABLE_NAME = "favorite_song";
    public static final String COLUMN_USER_EMAIL_REL = "user_email";
    public static final String COLUMN_TRACK_ID = "track_id";
    public static final String COLUMN_TITLE = "track_name";
    public static final String COLUMN_ARTIST = "artist_name";
    public static final String COLUMN_ALBUM = "collection_name";
    public static final String COLUMN_ARTWORK = "artwork_url";
    public static final String COLUMN_PREVIEW = "preview_url";

    public static final String TABLE_PLAYLIST = "playlist";
    public static final String COLUMN_PLAYLIST_ID = "playlist_id";
    public static final String COLUMN_PLAYLIST_NAME = "playlist_name";

    public static final String TABLE_PLAYLIST_SONG = "playlist_song";
    public static final String COLUMN_PLAYLIST_SONG_ID = "playlist_song_id";

    // User Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_AVATAR = "avatar_uri";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        String createUsersQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COLUMN_USER_EMAIL + " TEXT PRIMARY KEY COLLATE NOCASE," +
                COLUMN_USER_NAME + " TEXT," +
                COLUMN_USER_PASSWORD + " TEXT," +
                COLUMN_USER_AVATAR + " TEXT)";
        db.execSQL(createUsersQuery);

        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_USER_EMAIL_REL + " TEXT," +
                COLUMN_TRACK_ID + " INTEGER," +
                COLUMN_TITLE + " TEXT," +
                COLUMN_ARTIST + " TEXT," +
                COLUMN_ALBUM + " TEXT," +
                COLUMN_ARTWORK + " TEXT," +
                COLUMN_PREVIEW + " TEXT," +
                "PRIMARY KEY (" + COLUMN_USER_EMAIL_REL + ", " + COLUMN_TRACK_ID + "))";
        db.execSQL(createTableQuery);

        String createPlaylistQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLIST + " (" +
                COLUMN_PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USER_EMAIL_REL + " TEXT," +
                COLUMN_PLAYLIST_NAME + " TEXT)";
        db.execSQL(createPlaylistQuery);

        String createPlaylistSongQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLIST_SONG + " (" +
                COLUMN_PLAYLIST_SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_PLAYLIST_ID + " INTEGER," +
                COLUMN_TRACK_ID + " INTEGER," +
                COLUMN_TITLE + " TEXT," +
                COLUMN_ARTIST + " TEXT," +
                COLUMN_ALBUM + " TEXT," +
                COLUMN_ARTWORK + " TEXT," +
                COLUMN_PREVIEW + " TEXT," +
                "UNIQUE(" + COLUMN_PLAYLIST_ID + ", " + COLUMN_TRACK_ID + ") ON CONFLICT REPLACE)";
        db.execSQL(createPlaylistSongQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONG);
            onCreate(db);
        }
    }

    public boolean registerUser(String email, String name, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email.toLowerCase().trim());
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_AVATAR, "");
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean updateUser(String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        int result = db.update(TABLE_USERS, values, "LOWER(" + COLUMN_USER_EMAIL + ") = LOWER(?)", new String[]{email.trim()});
        db.close();
        return result > 0;
    }

    public boolean updateUserAvatar(String email, String avatarUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_AVATAR, avatarUri);
        int result = db.update(TABLE_USERS, values, "LOWER(" + COLUMN_USER_EMAIL + ") = LOWER(?)", new String[]{email.trim()});
        db.close();
        return result > 0;
    }

    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_EMAIL}, "LOWER(" + COLUMN_USER_EMAIL + ") = LOWER(?)", new String[]{email.trim()}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public Cursor getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, "LOWER(" + COLUMN_USER_EMAIL + ") = LOWER(?)", new String[]{email.trim()}, null, null, null);
    }

    public boolean addFavorite(String email, Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL_REL, email.toLowerCase().trim());
        values.put(COLUMN_TRACK_ID, song.getTrackId());
        values.put(COLUMN_TITLE, song.getTrackName() != null ? song.getTrackName() : "");
        values.put(COLUMN_ARTIST, song.getArtistName() != null ? song.getArtistName() : "");
        values.put(COLUMN_ALBUM, song.getCollectionName() != null ? song.getCollectionName() : "");
        values.put(COLUMN_ARTWORK, song.getArtworkUrl100() != null ? song.getArtworkUrl100() : "");
        values.put(COLUMN_PREVIEW, song.getPreviewUrl() != null ? song.getPreviewUrl() : "");

        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result != -1;
    }

    public boolean removeFavorite(String email, long trackId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COLUMN_USER_EMAIL_REL + " = ? AND " + COLUMN_TRACK_ID + " = ?", 
                new String[]{email.toLowerCase().trim(), String.valueOf(trackId)});
        db.close();
        return result > 0;
    }

    public boolean isFavorite(String email, long trackId) {
        if (email == null || email.isEmpty()) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_TRACK_ID}, 
                COLUMN_USER_EMAIL_REL + " = ? AND " + COLUMN_TRACK_ID + " = ?", 
                new String[]{email.toLowerCase().trim(), String.valueOf(trackId)}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public List<Song> getAllFavorites(String email) {
        List<Song> favoriteList = new ArrayList<>();
        if (email == null || email.isEmpty()) return favoriteList;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_USER_EMAIL_REL + " = ?", 
                new String[]{email.toLowerCase().trim()}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int trackIdIndex = cursor.getColumnIndex(COLUMN_TRACK_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                int artistIndex = cursor.getColumnIndex(COLUMN_ARTIST);
                int albumIndex = cursor.getColumnIndex(COLUMN_ALBUM);
                int artworkIndex = cursor.getColumnIndex(COLUMN_ARTWORK);
                int previewIndex = cursor.getColumnIndex(COLUMN_PREVIEW);

                long trackId = cursor.getLong(trackIdIndex);
                String title = titleIndex >= 0 ? cursor.getString(titleIndex) : "";
                String artist = artistIndex >= 0 ? cursor.getString(artistIndex) : "";
                String album = albumIndex >= 0 ? cursor.getString(albumIndex) : "";
                String artwork = artworkIndex >= 0 ? cursor.getString(artworkIndex) : "";
                String preview = previewIndex >= 0 ? cursor.getString(previewIndex) : "";

                favoriteList.add(new Song(trackId, title, artist, album, artwork, preview));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return favoriteList;
    }


    public long createPlaylist(String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL_REL, email.toLowerCase().trim());
        values.put(COLUMN_PLAYLIST_NAME, name);
        long id = db.insert(TABLE_PLAYLIST, null, values);
        db.close();
        return id;
    }

    public boolean deletePlaylist(long playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST_SONG, COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)});
        int result = db.delete(TABLE_PLAYLIST, COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)});
        db.close();
        return result > 0;
    }

    public List<Playlist> getAllPlaylists(String email) {
        List<Playlist> list = new ArrayList<>();
        if (email == null || email.isEmpty()) return list;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLAYLIST, null, COLUMN_USER_EMAIL_REL + " = ?", 
                new String[]{email.toLowerCase().trim()}, null, null, COLUMN_PLAYLIST_ID + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_PLAYLIST_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_PLAYLIST_NAME);
            do {
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                list.add(new Playlist(id, name));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    public boolean addSongToPlaylist(long playlistId, Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYLIST_ID, playlistId);
        values.put(COLUMN_TRACK_ID, song.getTrackId());
        values.put(COLUMN_TITLE, song.getTrackName() != null ? song.getTrackName() : "");
        values.put(COLUMN_ARTIST, song.getArtistName() != null ? song.getArtistName() : "");
        values.put(COLUMN_ALBUM, song.getCollectionName() != null ? song.getCollectionName() : "");
        values.put(COLUMN_ARTWORK, song.getArtworkUrl100() != null ? song.getArtworkUrl100() : "");
        values.put(COLUMN_PREVIEW, song.getPreviewUrl() != null ? song.getPreviewUrl() : "");

        long result = db.insertWithOnConflict(TABLE_PLAYLIST_SONG, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result != -1;
    }

    public boolean removeSongFromPlaylist(long playlistId, long trackId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PLAYLIST_SONG, COLUMN_PLAYLIST_ID + " = ? AND " + COLUMN_TRACK_ID + " = ?", new String[]{String.valueOf(playlistId), String.valueOf(trackId)});
        db.close();
        return result > 0;
    }

    public List<Song> getSongsInPlaylist(long playlistId) {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLAYLIST_SONG + " WHERE " + COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)});
        if (cursor != null && cursor.moveToFirst()) {
            int trackIdIndex = cursor.getColumnIndex(COLUMN_TRACK_ID);
            int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
            int artistIndex = cursor.getColumnIndex(COLUMN_ARTIST);
            int albumIndex = cursor.getColumnIndex(COLUMN_ALBUM);
            int artworkIndex = cursor.getColumnIndex(COLUMN_ARTWORK);
            int previewIndex = cursor.getColumnIndex(COLUMN_PREVIEW);

            do {
                long trackId = cursor.getLong(trackIdIndex);
                String title = titleIndex >= 0 ? cursor.getString(titleIndex) : "";
                String artist = artistIndex >= 0 ? cursor.getString(artistIndex) : "";
                String album = albumIndex >= 0 ? cursor.getString(albumIndex) : "";
                String artwork = artworkIndex >= 0 ? cursor.getString(artworkIndex) : "";
                String preview = previewIndex >= 0 ? cursor.getString(previewIndex) : "";

                list.add(new Song(trackId, title, artist, album, artwork, preview));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }
}
