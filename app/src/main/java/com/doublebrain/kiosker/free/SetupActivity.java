package com.doublebrain.kiosker.free;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SetupActivity extends AppCompatActivity {

    Button setupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupButton = (Button) findViewById(R.id.setupButton);
        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String defLauncher = AppHelper.getDefaultLauncher();
                if (!defLauncher.isEmpty() && !defLauncher.equals(getPackageName())){
                    Intent selector = new Intent();
                    selector.setAction(Intent.ACTION_MAIN);
                    selector.addCategory(Intent.CATEGORY_HOME);
                    selector.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(selector);
                    App.setValue(App.KEY_OLD_LAUNCHER,defLauncher);
                }
            }
        });
    }
}
