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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.persistence.Identity;
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

        publicKey.setText(identity.getKey().getVerifyKey().toString());
        keySeed.setText(identity.getKey().toString());
    }

    public void onImportClicked(View view) {
    }

    public void onGenerateClicked(View view) {
        signingKey = new SigningKey();
        publicKey.setText(signingKey.getVerifyKey().toString());
        keySeed.setText(signingKey.toString());

        identity.setKey(signingKey);
        identity.save();
    }

}
