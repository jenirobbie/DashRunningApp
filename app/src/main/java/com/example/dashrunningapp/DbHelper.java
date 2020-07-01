package com.example.dashrunningapp;


    import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

    import com.example.dashrunningapp.exceptions.NoStoredUserException;
    import com.example.dashrunningapp.exceptions.TooManyUsersException;

    import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

    public class DbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "Dash.db";
        private static final int DATABASE_VERSION = 1;
        private final Context context;
        SQLiteDatabase db;

        private static final String DATABASE_PATH = "/data/data/com.example.dashrunningapp/databases/";
        private final String USER_TABLE = "User";


        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
            createDb();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        public void createDb(){
            boolean dbExist = checkDbExist();

            if(!dbExist){
                this.getReadableDatabase();
                copyDatabase();
            }
        }

        private boolean checkDbExist(){
            SQLiteDatabase sqLiteDatabase = null;

            try{
                String path = DATABASE_PATH + DATABASE_NAME;
                sqLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            } catch (Exception ex){
            }

            if(sqLiteDatabase != null){
                sqLiteDatabase.close();
                return true;
            }

            return false;
        }
//remove?
        private void copyDatabase(){
            try {
                InputStream inputStream = context.getAssets().open(DATABASE_NAME);

                String outFileName = DATABASE_PATH + DATABASE_NAME;

                OutputStream outputStream = new FileOutputStream(outFileName);

                byte[] b = new byte[1024];
                int length;

                while ((length = inputStream.read(b)) > 0){
                    outputStream.write(b, 0, length);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private SQLiteDatabase openDatabase(){
            String path = DATABASE_PATH + DATABASE_NAME;
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
            return db;
        }

        public void close(){
            if(db != null){
                db.close();
            }
        }


        public void AddUser(String email, String password){
            db = openDatabase();
            db.execSQL("CREATE TABLE IF NOT EXISTS User(email VARCHAR,password VARCHAR);");
            db.execSQL("INSERT INTO User VALUES(\'"+ email +"\', \'"+ password +"\');");
            db.close();

        }
        public void DropUserTable(){
            db.execSQL("DROP TABLE IF EXISTS User;");
        }


        public UserDetails checkUserExist() throws NoStoredUserException, TooManyUsersException {


            UserDetails loginDetails = new UserDetails();

            String[] columns = {"email", "password"};
            db = openDatabase();
            db.execSQL("CREATE TABLE IF NOT EXISTS User(email VARCHAR,password VARCHAR);");
            Cursor cursor = db.query(USER_TABLE, columns, null, null, null, null, null);

            if (cursor != null && cursor.getCount()>0) {
                cursor.moveToFirst();
            }
            else
                throw new NoStoredUserException();

            if (cursor.getCount() > 1) {
                throw new TooManyUsersException();
            }
            loginDetails.password = cursor.getString(cursor.getColumnIndex("password"));
            loginDetails.email = cursor.getString(cursor.getColumnIndex("email"));
            cursor.close();
            db.close();


            return loginDetails;


        }




    }

