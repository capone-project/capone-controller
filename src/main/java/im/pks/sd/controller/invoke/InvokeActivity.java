package im.pks.sd.controller.invoke;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetails;
import im.pks.sd.services.Plugin;
import im.pks.sd.services.PluginFragment;
import im.pks.sd.services.PluginTask;
import im.pks.sd.services.Plugins;

public class InvokeActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE = "service";

    private ServiceDetails service;
    private Plugin plugin;
    private PluginFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        service = (ServiceDetails) getIntent().getSerializableExtra(EXTRA_SERVICE);
        plugin = Plugins.getPlugin(service);

        setContentView(R.layout.activity_invoke);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragment = plugin.getFragment(service);
        transaction.add(R.id.plugin_view, fragment);
        transaction.commit();
    }

    public void onConnectClicked(View view) {
        PluginTask task = plugin.getTask(fragment);
        if (task == null) {
            Toast.makeText(this, R.string.service_handler_not_implemented, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        task.execute();
    }

}
