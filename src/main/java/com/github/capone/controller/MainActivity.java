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

package com.github.capone.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.github.capone.controller.about.AboutActivity;
import com.github.capone.controller.discovery.DiscoveryListFragment;
import com.github.capone.controller.favorites.FavoritesFragment;
import com.github.capone.controller.identity.IdentityActivity;

public class MainActivity extends AppCompatActivity {

    private FavoritesFragment favoritesFragment = new FavoritesFragment();
    private DiscoveryListFragment discoveryFragment = new DiscoveryListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);

        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);

        tabs.addTab(createFavoritesTab(tabs));
        tabs.addTab(createDiscoveryTab(tabs));
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        favoritesFragment.notifyDataSetChanged();
                        break;
                    case 1:
                        discoveryFragment.notifyDataSetChanged();
                        discoveryFragment.startDiscovery();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        favoritesFragment.stopDiscovery();
                        break;
                    case 1:
                        discoveryFragment.stopDiscovery();
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return favoritesFragment;
                    case 1:
                        return discoveryFragment;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
    }

    private TabLayout.Tab createFavoritesTab(TabLayout tabs) {
        TabLayout.Tab tab = tabs.newTab();
        tab.setText(R.string.favorites);
        return tab;
    }

    private TabLayout.Tab createDiscoveryTab(TabLayout tabs) {
        TabLayout.Tab tab = tabs.newTab();
        tab.setText(R.string.discovery);
        return tab;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.identity:
                startActivity(new Intent(this, IdentityActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
