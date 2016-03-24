package im.pks.sd.controller.invoke;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.*;
import im.pks.sd.persistence.Identity;

public class ServiceChooserActivity extends Activity {

    public static final String EXTRA_SELECTED_SERVER = "selected_server";
    public static final String EXTRA_SELECTED_SERVICE = "selected_service";

    private ServerListAdapter serverAdapter;
    private DiscoveryTask discoveryTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_chooser);

        ListView view = (ListView) findViewById(R.id.server_list);
        setServerAdapter(view);
    }

    @Override
    protected void onPause() {
        super.onPause();
        discoveryTask.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        discoveryTask = new DiscoveryTask(null, Identity.getSigningKey().getVerifyKey()) {
            @Override
            public void onProgressUpdate(Server... server) {
                serverAdapter.add(server[0]);
            }
        };
        discoveryTask.execute();
    }

    private void setServerAdapter(final ListView view) {
        if (serverAdapter == null) {
            serverAdapter = new ServerListAdapter(this) {
                @Override
                public void onServerClicked(final Server server) {
                    setServiceAdapter(view, server);
                }
            };
        }

        view.setAdapter(serverAdapter);
    }

    private void setServiceAdapter(final ListView view, final Server server) {
        ServiceListAdapter adapter = new ServiceListAdapter(this) {
            @Override
            public void onServiceClicked(Service service) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECTED_SERVER, server);
                intent.putExtra(EXTRA_SELECTED_SERVICE, service);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        };
        view.setAdapter(adapter);

        adapter.addAll(server.services);
    }

    public void onCancelClicked(View view) {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

}
