package com.github.capone.controller.about;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.github.capone.controller.R;

import java.io.InputStream;
import java.util.Scanner;

public class AboutActivity extends AppCompatActivity {

    private class Library {
        public final String name;
        public final String version;
        public final int resourceId;

        public Library(String name, String version, int resourceId) {
            this.name = name;
            this.version = version;
            this.resourceId = resourceId;
        }

        @Override
        public String toString() {
            return String.format("%s (version %s)", name, version);
        }
    }

    private Library[] libraries = new Library[]{
            new Library("Faenza Icon Theme", "1.3", R.raw.license_gplv3),
            new Library("Google Protobuf", "3.0.0-alpha-5", R.raw.license_protobuf),
            new Library("Robosodium", "1.0.0", R.raw.license_robosodium),
            new Library("Sugar ORM", "1.5", R.raw.license_sugar_orm),
            new Library("Apache Commons Lang", "2.6", R.raw.license_apache2_0),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ArrayAdapter<Library> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(libraries);

        ListView libraryList = (ListView) findViewById(R.id.library_list);
        libraryList.setAdapter(adapter);
        libraryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Library library = libraries[position];
                TextView license = new TextView(AboutActivity.this);
                setText(license, library.resourceId);

                new AlertDialog.Builder(AboutActivity.this)
                        .setView(license)
                        .show();
            }
        });

        TextView about = (TextView) findViewById(R.id.about_text);
        setText(about, R.raw.about);
    }

    private void setText(TextView textView, int resourceId) {
        textView.setMovementMethod(new ScrollingMovementMethod());

        InputStream stream = getResources().openRawResource(resourceId);
        String license = new Scanner(stream).useDelimiter("\\Z").next();
        textView.setText(license);
    }
}
