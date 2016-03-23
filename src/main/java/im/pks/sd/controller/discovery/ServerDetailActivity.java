package im.pks.sd.controller.discovery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetailActivity;

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
        TextView addressView = (TextView) findViewById(R.id.server_address);
        addressView.setText(server.address);

        final ListView serviceList = (ListView) findViewById(R.id.service_list);
        serviceList.setAdapter(new ArrayAdapter<Service>(this, R.layout.list_item_server, server.services) {
            @Override
            public View getView(final int position, View view, ViewGroup group) {
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.list_item_service, null);
                }

                final Service service = server.services.get(position);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ServerDetailActivity.this,
                                ServiceDetailActivity.class);
                        intent.putExtra(ServiceDetailActivity.EXTRA_SERVICE, service);
                        intent.putExtra(ServiceDetailActivity.EXTRA_SERVER, server);
                        startActivity(intent);
                    }
                });

                ImageView serviceImage = (ImageView) view.findViewById(R.id.service_image);
                serviceImage.setImageResource(service.getResourceId());

                TextView serviceName = (TextView) view.findViewById(R.id.service_name);
                serviceName.setText(service.name);
                TextView serviceType = (TextView) view.findViewById(R.id.service_type);
                serviceType.setText(service.type);
                TextView servicePort = (TextView) view.findViewById(R.id.service_port);
                servicePort.setText(String.valueOf(service.port));

                return view;
            }
        });
    }

}
