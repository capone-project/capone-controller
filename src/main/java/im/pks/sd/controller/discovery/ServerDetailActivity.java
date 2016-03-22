package im.pks.sd.controller.discovery;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import im.pks.sd.controller.R;

public class ServerDetailActivity extends Activity {

    public static final String EXTRA_SERVER = "server";

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_detail);

        server = (Server) getIntent().getSerializableExtra(EXTRA_SERVER);

        TextView keyView = (TextView) findViewById(R.id.server_key);
        keyView.setText(server.publicKey);

        ListView serviceList = (ListView) findViewById(R.id.service_list);
        serviceList.setAdapter(new ArrayAdapter<Service>(this, R.layout.list_item_server, server.services) {
            @Override
            public View getView(final int position, View view, ViewGroup group) {
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.list_item_service, null);
                }

                Service service = server.services.get(position);
                TextView serviceName = (TextView) view.findViewById(R.id.service_name);
                serviceName.setText(service.name);
                TextView serviceType = (TextView) view.findViewById(R.id.service_type);
                serviceType.setText(service.type);
                TextView servicePort = (TextView) view.findViewById(R.id.service_port);
                servicePort.setText(service.port);

                return view;
            }
        });
    }
}
