package com.example.esutisl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Mysql extends SQLiteOpenHelper {

    public Mysql(@Nullable Context context) {
        super(context, "eSuits.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表
        String sql = "create table person(id integer primary key autoincrement,password text, count integer,setcount integer)";
        db.execSQL(sql);
    }

    //数据库升级
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * 插入
     */
    public long insert(Bean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", 0);
        values.put("count", bean.getCount());
        values.put("setcount",bean.getSetcount());
        values.put("password", bean.getPassword());
        long l = db.insert("person", null, values);
        return l;
    }

    /**
     * 查询
     */
    public Bean select() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select *from person";
        Cursor cursor = db.rawQuery(sql, null);
        Bean bean = new Bean();
        while (cursor.moveToNext()) {
            cursor.getInt(cursor.getColumnIndex("setcount"));
            int count = cursor.getInt(cursor.getColumnIndex("count"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            bean.setCount(count);
            bean.setPassword(password);
        }
        return bean;
    }

    /**
     * 修改
     */
    public int Updata(int id, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("password", password);
        int i = db.update("person", values, "id=?", new String[]{id + ""});
        return i;
    }
    /*
    *查询数据库是否有数据
     */
    public int selectQuery() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select *from person";
        Cursor cursor = db.rawQuery(sql, null);
        int i = 0;
        while (cursor.moveToNext()) {
            i++;
        }

        return i;
    }
    /**
     *根据id 修改登录失败次数
     */

    public int UpdataCount(int id, int count) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("count", count);
        int i = db.update("person", values, "id=?", new String[]{id + ""});
        return i;
    }
    /*
    *修改设置密码失败次数
     */
    public int UpdataSetCount(int id, int setcount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("setcount", setcount);
        int i = db.update("person", values, "id=?", new String[]{id + ""});
        return i;
    }
}
