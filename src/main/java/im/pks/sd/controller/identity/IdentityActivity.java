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

package im.pks.sd.controller.identity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import im.pks.sd.controller.R;
import im.pks.sd.entities.Identity;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.SigningKey;

import java.util.Iterator;

public class IdentityActivity extends AppCompatActivity {

    private Identity identity;
    private SigningKey signingKey;
    private TextView publicKey;
    private TextView keySeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity);

        publicKey = (TextView) findViewById(R.id.public_key);
        keySeed = (TextView) findViewById(R.id.key_seed);

        Iterator<Identity> identities = Identity.findAll(Identity.class);
        if (!identities.hasNext()) {
            identity = new Identity(new SigningKey());
            identity.save();
        } else {
            identity = identities.next();
        }

        setKey(identity.getKey().toString());
    }

    public void onImportClicked(View view) {
        final EditText seedInput = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.paste_your_key_seed);
        builder.setView(seedInput);
        builder.setPositiveButton(R.string.import_key, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();

        // Override default onClickListener to avoid dismissing dialog
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String input = seedInput.getText().toString();

                        try {
                            setKey(input);
                            alertDialog.dismiss();
                        } catch (Exception e) {
                            Toast.makeText(IdentityActivity.this, R.string.invalid_seed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void onGenerateClicked(View view) {
        signingKey = new SigningKey();
        publicKey.setText(signingKey.getVerifyKey().toString());
        keySeed.setText(signingKey.toString());

        identity.setKey(signingKey);
        identity.save();
    }

    private void setKey(String input) {
        byte[] seed = Encoder.HEX.decode(input);
        SigningKey key = new SigningKey(seed);
        identity.setKey(key);
        identity.save();

        publicKey.setText(identity.getKey().getVerifyKey().toString());
        keySeed.setText(identity.getKey().toString());
    }

}
