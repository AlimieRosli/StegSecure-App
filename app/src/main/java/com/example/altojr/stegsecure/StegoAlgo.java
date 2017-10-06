package com.example.altojr.stegsecure;

import android.graphics.Bitmap;


public interface StegoAlgo {
    public Object stego(Bitmap im, String secret, Boolean whatDo);
}
