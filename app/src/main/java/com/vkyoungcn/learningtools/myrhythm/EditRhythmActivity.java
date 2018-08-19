package com.vkyoungcn.learningtools.myrhythm;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.RhythmEditFragment;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;

public class EditRhythmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_rhythm2);

        CompoundRhythm compoundRhythm = getIntent().getParcelableExtra("COMPOUND_RHYTHM");

        FragmentTransaction transaction = (getFragmentManager().beginTransaction());
        Fragment prev = (getFragmentManager().findFragmentByTag("EDIT_TEXT"));

        if (prev != null) {
            Toast.makeText(this, "Old Dfg still there, removing...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }

        Fragment editFragment = RhythmEditFragment.newInstance(compoundRhythm);
        transaction.add(R.id.flt_fgContainer_ERA,editFragment,"EDIT_TEXT").commit();

    }
}