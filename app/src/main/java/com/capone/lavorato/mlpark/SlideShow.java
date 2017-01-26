package com.capone.lavorato.mlpark;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by luca on 25/01/17.
 */

public class SlideShow extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance
                ("Parcheggia", getResources().getString(R.string.parking_desc), R.drawable.slide1, getColor(R.color.Slide1)));
        addSlide(AppIntroFragment.newInstance
                ("Trova", getResources().getString(R.string.retrieve_desc), R.drawable.slide2, getColor(R.color.Slide2)));
        addSlide(AppIntroFragment.newInstance
                ("Sincronizza", getResources().getString(R.string.retrieve_drive), R.drawable.slide3, getColor(R.color.Slide3)));
        addSlide(AppIntroFragment.newInstance
                ("Scatta", getResources().getString(R.string.retrieve_img_drive), R.drawable.slide4, getColor(R.color.Slide4)));
        addSlide(AppIntroFragment.newInstance
                ("Fatto", "Benvenuto in MLPark, premi su FATTO per iniziare", R.drawable.slide5,getColor(R.color.Slide5)));

        setFadeAnimation();
        setDoneText("Fatto");
        setSkipText("Annulla");
        //Permessi da chiedere al variare della slide
        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        askForPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, 3);
        askForPermissions(new String[]{Manifest.permission.CAMERA}, 4);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        //Finisci quando premi su Skip
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        //Finisci quando premi su Done
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}
