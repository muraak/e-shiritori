package kenta.android.apps.eshiritori;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Kenta on 2015/05/03.
 */

public class DBHelper extends SQLiteOpenHelper
{
    private static final String NAME    = "eshiritori.db";
    private static final int    VERSION = 1;

    private static final String   TABLE 	    = "eshiritori";
    private static final String   _ID 			= "_id";
    private static final String   TITLE			= "title";
    private static final String   PICTURE 		= "picture";
    private static final String[] COLUMNS       = {_ID, TITLE, PICTURE};

    public DBHelper(Context context)
    {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //tableが存在するときは呼ばれない
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + TITLE + " TEXT NOT NULL,"
                + PICTURE + " BLOB NOT NULL"
                + ");");
//        Log.d("KMShiritori", "Created the table: " + TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        this.onCreate(db);
    }

    public void clear()
    {
        /******************************************************
         * DBから絵しりとり用のテーブルを削除するメソッドです *
         ******************************************************/

        SQLiteDatabase db = super.getWritableDatabase();
        db.delete(TABLE, null, null);
    }

    private synchronized void setChacheAsBlob(String title, byte[] bytes)
    {
		/*****************************************************
		 * 第2引数の画像のバイト列を                         *
		 * 第1引数のタイトルと一緒にDBへ挿入するメソッドです *
		 *****************************************************/

        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(PICTURE, bytes);

        SQLiteDatabase db = super.getWritableDatabase();
        db.insert(TABLE, null, values);
    }

    private synchronized byte[] getChacheAsBlob(int key)
    {
		/********************************************
		 * 引数のキーとセットの画像を               *
		 * DBからバイト列として取得するメソッドです *
		 ********************************************/

        final SQLiteDatabase db = super.getReadableDatabase();
        final Cursor cursor = db.query(TABLE, COLUMNS, _ID + "=" + "'" + key + "'",
                null, null, null, null);

        byte[] bytes = null;

        if(cursor.getCount() == 1)
        {
            if(cursor.moveToFirst())
            {
                //3番目のカラムの値をバイト列で取得
                bytes = cursor.getBlob(2);
            }
        }

        cursor.close();

        return bytes;
    }

    public void insertData(String title, Bitmap bitmap)
    {
		/********************************************
		 * ビットマップをバイト列に変換して         *
		 * タイトルと一緒にDBへ挿入するメソッドです *
         ********************************************/

        ByteArrayOutputStream byteArrayOStream =  new ByteArrayOutputStream();

        if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOStream))
        {
            byte[] bytes = byteArrayOStream.toByteArray();
            if(title != null && bytes != null)
                setChacheAsBlob(title, bytes);
        }
    }

    public void getPictures(int offset, ArrayList<Integer> ids, ArrayList<Bitmap> pictures)
    {

        /*******************************************************************
         * 最後(の挿入)から(offset * 5)番目以前の5枚の画像を               *
         * 古い順にArrayListに格納するメソッドです.                        *
         * idsには画像が最初から何枚目かを示す整数(先頭は1)が格納されます. *
         *******************************************************************/

        int number_of_target_rows = this.getRecordCount() - 5 * offset;

        for(int i = number_of_target_rows; i >= (number_of_target_rows - 4); i--)
        {
            byte[] bytes = this.getChacheAsBlob(i);

            if(bytes != null)
            {
                ids.add(i);
                pictures.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
            else
                break;
        }
        Collections.reverse(ids);
        Collections.reverse(pictures);
    }

    public void getPicturesOfAnswer(int offset, ArrayList<Integer> ids, ArrayList<Bitmap> pictures)
    {
        int number_of_target_rows = 1 + 5 * offset;

        for(int i = number_of_target_rows; i <= (number_of_target_rows + 4); i++)
        {
            byte[] bytes = this.getChacheAsBlob(i);

            if(bytes != null)
            {
                ids.add(i);
                pictures.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
            else
                break;
        }
    }

    public int getRecordCount()
    {
        /*****************************************
         *  いくつの画像がDBに格納されているかを *
         *  算出するメソッドです.                *
         *****************************************/

        String query = "SELECT COUNT(*) FROM " + TABLE;
        SQLiteDatabase db = super.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToLast();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private Bitmap getPicture(int key)
    {
		/***********************************************
		 * 指定したkeyの画像を返すメソッドです．       *
		 * 画像はバイト列からビットマップに変換します. *
		 ***********************************************/

        Bitmap bitmap = null;
        byte[] bytes = getChacheAsBlob(key);

        if(bytes != null)
        {
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        return bitmap;
    }

    public String getTitle(int key)
    {
		/********************************************
		 * 指定したkeyのタイトルを返すメソッドです. *
		 ********************************************/

        final SQLiteDatabase db = super.getReadableDatabase();
        final Cursor cursor = db.query(TABLE, COLUMNS, _ID + "=" + "'" + key + "'",
                null, null, null, null);

        String title = null;

        if(cursor.getCount() == 1)
        {
            if(cursor.moveToFirst())
            {
                //2番目のカラムの値をバイト列で取得
                title = cursor.getString(1);
            }
        }
        cursor.close();
        return title;
    }
}
