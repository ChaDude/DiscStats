package com.summithillsoftware.ultimate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.summithillsoftware.ultimate.ui.team.TeamsActivity;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // just transition to the Teams view
        startActivity(new Intent(this, TeamsActivity.class));
    }


    
}
