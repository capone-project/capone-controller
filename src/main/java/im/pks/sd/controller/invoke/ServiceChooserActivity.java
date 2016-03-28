package im.pks.sd.controller.invoke;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.*;
import im.pks.sd.controller.query.ServiceDetails;
import im.pks.sd.entities.Identity;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import im.pks.sd.protocol.QueryTask;

public class ServiceChooserActivity extends Activity {

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
            public void onProgressUpdate(ServerTo... server) {
                serverAdapter.add(server[0]);
            }
        };
        discoveryTask.execute();
    }

    private void setServerAdapter(final ListView view) {
        if (serverAdapter == null) {
            serverAdapter = new ServerListAdapter(this) {
                @Override
                public void onServerClicked(final ServerTo server) {
                    setServiceAdapter(view, server);
                }
            };
        }

        view.setAdapter(serverAdapter);
    }

    private void setServiceAdapter(final ListView view, final ServerTo server) {
        discoveryTask.cancel();

        ServiceListAdapter adapter = new ServiceListAdapter(this) {
            @Override
            public void onServiceClicked(ServiceTo service) {
                QueryTask.QueryParameters parameters = new QueryTask.QueryParameters(Identity.getSigningKey(), server, service);

                QueryTask queryTask = new QueryTask() {
                    @Override
                    public void onProgressUpdate(ServiceDetails... details) {
                        onServiceDetailsReceived(details[0]);
                    }
                };
                queryTask.execute(parameters);
            }
        };
        view.setAdapter(adapter);

        adapter.addAll(server.services);
    }

    private void onServiceDetailsReceived(ServiceDetails details) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SELECTED_SERVICE, details);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void onCancelClicked(View view) {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

}
