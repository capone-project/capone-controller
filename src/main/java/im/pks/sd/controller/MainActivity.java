/*
 * Copyright (C) 2016 Patrick Steinhardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package im.pks.sd.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import im.pks.sd.controller.about.AboutActivity;
import im.pks.sd.controller.discovery.DiscoveryListActivity;
import im.pks.sd.controller.identity.IdentityActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDiscoveryClicked(View view) {
        startActivity(new Intent(this, DiscoveryListActivity.class));
    }

    public void onIdentityClicked(View view) {
        startActivity(new Intent(this, IdentityActivity.class));
    }

    public void onAboutClicked(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

}
