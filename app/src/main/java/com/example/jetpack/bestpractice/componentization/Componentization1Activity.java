package com.example.jetpack.bestpractice.componentization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.jetpack.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Componentization1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_componentization1);
        try {
            Class<?> cl = Class.forName("android.app.ActivityTaskManager");
//            Method getService = cl.getDeclaredMethod("getService");
            Method getService = cl.getDeclaredMethod("supportsSplitScreenMultiWindow", Context.class);
            Object ojb = getService.invoke(null,this);
            Log.i("Componentization1Activity", "hash---" + ojb.hashCode());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}