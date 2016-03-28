package im.pks.sd.controller.invoke;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetails;
import im.pks.sd.services.PluginFragment;
import im.pks.sd.services.Services;

public class InvokeActivity extends Activity {

    public static final String EXTRA_SERVICE = "service";

    private ServiceDetails service;
    private PluginFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        service = (ServiceDetails) getIntent().getSerializableExtra(EXTRA_SERVICE);

        setContentView(R.layout.activity_invoke);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragment = Services.getFragment(service);
        transaction.add(R.id.plugin_view, fragment);
        transaction.commit();
    }

    public void onConnectClicked(View view) {
        fragment.onConnectClicked();
    }

}
