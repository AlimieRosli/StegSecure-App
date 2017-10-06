package com.example.altojr.stegsecure;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.RippleDrawable;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_SELECT_CODE = 1, FILE_SELECT_CODE = 2; //code for task 'select image'
    String code = "DEBUG", SM = "";
    Toast toast;

    //component declaration
    ImageView coverImage,stegoImage;
    Button selectImage,encrypt,decrypt,save,exit;
    TextView secretMsg, feedbackMsg;

    /**

    Vector
            vecRed = new Vector<Integer>(),
            vecGreen = new Vector<Integer>(),
            vecBlue = new Vector<Integer>(),
            vecAlpha = new Vector<Integer>(),

            vecASCII = new Vector<Integer>(), //ASCII fo all pixel
            vecBinaryWord = new Vector<String>(), //binary word of all characters of the secret message

            vecDI = new Vector<Integer>(), // difference of two consecutive pixels, d(i)
            vecLJ = new Vector<Integer>(), // lower boundary, l(j) of the range, R(j)
            vecUJ = new Vector<Integer>(), // upper boundary, u(j) of the range, R(j)
            vecWJ = new Vector<Integer>(), // width, w(j) of the range, R(j) : w(j) = u(j) - l(j) + 1
            vecTI = new Vector<Integer>(), //hiding capacity, t(i) = lg(w(j))
            vecTI2 = new Vector<Integer>(), //new hiding capacity, t'(i)
            vecM = new Vector<Integer>(), // difference of d'(i) and d(i) : m = | d'(i) - d(i) |
            vecDI2 = new Vector<Integer>(), // new difference value, d'(i) = t'(i) + l(j)

            vecTable = new Vector<Integer>(), //range of bit that can be inserted into current pixel
            vecBinaryDecrypt = new Vector<Integer>(); //binary word from decrypt process
     */

    /**
     * specify layout for this class
     * activity_main layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        secretMsg = (TextView) findViewById(R.id.secretMsg); //secret message
        //feedbackMsg = (TextView) findViewById(R.id.feedbackMsg); //feedback or secret message decrypted from stego-image
        coverImage = (ImageView) findViewById(R.id.selectedImage); // cover image
        stegoImage = (ImageView) findViewById(R.id.stegoImage); //stego-image

        //main task button
        selectImage = (Button) findViewById(R.id.selectImageButton);
        encrypt = (Button) findViewById(R.id.encryptButton);
        decrypt = (Button) findViewById(R.id.decryptButton);
        save = (Button) findViewById(R.id.saveButton);
        exit = (Button) findViewById(R.id.exitButton);

        selectImage.setEnabled(true);
        encrypt.setEnabled(false);
        decrypt.setEnabled(false);
        save.setEnabled(false);

        Log.d(code,"hello");//test debug, print on console
    }

    /**
     * to select image through file chooser
     * @param view
     */
    public void showFileChooser(View view) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(i, "Select File"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please Install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * to get result(image-path) from previous activity and display on the image frame
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = null;
                    try {
                        path = getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    coverImage.setImageURI(uri);
                }
                break;
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d(code, "File Uri :" + uri.toString());
                    String path = null;
                    try {
                        path = getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    coverImage.setImageURI(uri);
                    //selectedFile = new File(path);
                    encrypt.setEnabled(true);
                    decrypt.setEnabled(true);
                }
                break;
        }

    }

    /**
     * to get the path of the selected image
     * @param context
     * @param uri
     * @return null
     * @throws URISyntaxException
     */
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {

            }
        } else if ("file".equalsIgnoreCase((uri.getScheme()))) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * to exit the app
     * exe when 'EXIT' button clicked
     * @param view
     */
    public void exit(View view){
        finish();
        System.exit(0);
    }

    /**
     * to encrypt secret message into the cover image
     * exe when 'ENCRYPT' button clicked
     * @param
     */
    public void encrypt(View view) {

        if (getSecretMsg().trim().length() <= 0) {
            toast = Toast.makeText(MainActivity.this, "Your text is empty", 5000);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {

            cubaEn();
        }
    }

    /**
     * to get cover image from imageview
     * @return bitmap
     */
    public Bitmap getCoverImage(){
        BitmapDrawable img = ((BitmapDrawable) coverImage.getDrawable());
        Bitmap bitmap = img.getBitmap();
        return bitmap;
    }

    /**
     * to get stego-image image from imageview
     * @return bitmap
     */
    public Bitmap getStegoImage(){
        BitmapDrawable img = ((BitmapDrawable) stegoImage.getDrawable());
        Bitmap bitmap = img.getBitmap();
        return bitmap;
    }

    /**
     * to get the secret message
     * @return
     */
    public String getSecretMsg(){
        String text = secretMsg.getText().toString();
        return text;
    }

    /**
     * to save stego-image into mobile internal memory
     * exe when 'SAVE IMAGE' button clicked
     * @param view
     */
    public void saveImage(View view){

        Bitmap myBitmap = getStegoImage();

        String root = Environment.getExternalStorageDirectory().toString();

        File myDir = new File(root + "/StegoImage");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Test-" + n + ".png";
        File file = new File(myDir, fname);
        if(file.exists()) file.delete();

        try{
            FileOutputStream out = new FileOutputStream(file);
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.flush();
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        save.setEnabled(false);
        toast = Toast.makeText(MainActivity.this, "Stego-image saved", 5000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * to decrypt stego-image
     * exe when 'DECRYPT' button clicked
     * @param view
     */
    public void decrypt(View view){

            cubaDe();
    }

    //test encrypt
    public void cubaEn(){

        StegoPVD pvd = new PVDColor();
        Drawable d = coverImage.getDrawable();
        Bitmap myBitmap = ((BitmapDrawable) d).getBitmap();
        Object obj = pvd.stego(myBitmap, secretMsg.getText().toString(), true);
        Bitmap newBitmap = (Bitmap) obj;
        stegoImage.setImageBitmap(newBitmap);
        save.setEnabled(true);
    }

    //test decrypt
    public void cubaDe(){

        StegoPVD pvd = new PVDColor();
        Drawable d = coverImage.getDrawable();
        Bitmap StegoBitmap = ((BitmapDrawable) d).getBitmap();

        Object obj = pvd.stego(StegoBitmap, "", false);


        if(obj != null){
            String secretStego = (String) obj;
            StringBuilder theLast = new StringBuilder();
            int[] strChar = new int[8];
            for (int i = 0; i < secretStego.length(); )
            {
                strChar = new int[8];
                for (int j = 0; j < 8; j++)
                {
                    if(i < secretStego.length())
                        strChar[j] = Integer.parseInt(String.valueOf(secretStego.charAt(i++)));
                }

                int b = 0;
                int bin = 1;
                for (int k= strChar.length-1; k >= 0; k--){
                    b+= strChar[k] * bin;
                    bin = bin * 2;
                }
                theLast.append(String.valueOf((char)b));
            }
            //feedbackMsg.setText(theLast.toString());
            SM = theLast.toString();

            showSecretMessage();
        }
        else {
            toast = Toast.makeText(MainActivity.this, "There is no secret message in the picture !", 5000);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void showSecretMessage(){

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder( this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Secret message")
                .setMessage(SM)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
