package com.example.currencyexchange;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.scrounger.countrycurrencypicker.library.Buttons.CountryCurrencyButton;
import com.scrounger.countrycurrencypicker.library.Country;
import com.scrounger.countrycurrencypicker.library.Currency;
import com.scrounger.countrycurrencypicker.library.Listener.CountryCurrencyPickerListener;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String BPI_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private EditText bitcoinTV, currencyTV;
    private TextView time;
    private Button currencySubmitBTN, bitcoinSubmitBTN;
    private CountryCurrencyButton button;
    private String currencyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitcoinTV = findViewById(R.id.bitcoinTV);
        currencyTV = findViewById(R.id.currencyTV);
        time = findViewById(R.id.timeUpdateTV);

        button = (CountryCurrencyButton) findViewById(R.id.countryBTN);
        button.setOnClickListener(new CountryCurrencyPickerListener() {
            @Override
            public void onSelectCountry(Country country) {
                if (country.getCurrency() == null) {
                    Toast.makeText(MainActivity.this,
                            String.format("name: %s\ncode: %s", country.getName(), country.getCode())
                            , Toast.LENGTH_SHORT).show();
                    currencyCode = country.getCode();
                } else {
                    Toast.makeText(MainActivity.this,
                            String.format("name: %s\ncurrencySymbol: %s", country.getName(), country.getCurrency().getSymbol())
                            , Toast.LENGTH_SHORT).show();
                    currencyCode = country.getCurrency().getSymbol();
                }
            }

            @Override
            public void onSelectCurrency(Currency currency) {

            }
        });
    }

    public void submit(final View v) {
        Request request = new Request.Builder().url(BPI_ENDPOINT).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Error during BPI loading : "
                        + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, final Response response)
                    throws IOException {
                final String body = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                                response(v, body)
                        } catch (Exception e) {

                        }
                    }
                });
            }
        });

    }

    private void response(View v, String body) {
        try {
            JSONObject jsonObject = new JSONObject(body);
            JSONObject timeObject = jsonObject.getJSONObject("time");
            time.setText(timeObject.getString("updated"));
            JSONObject bpiObject = jsonObject.getJSONObject("bpi");

            if(v.getId() == R.id.bitcoinSubmitBTN) {
                JSONObject cObject = bpiObject.getJSONObject(currencyCode);
                float currencyRate = (Float.parseFloat(cObject.getString("rate_float")))
                    * Float.parseFloat(String.valueOf(bitcoinTV.getText()));

                currencyTV.setText(currencyRate + "");
            }
            else {
                JSONObject cObject = bpiObject.getJSONObject(currencyCode);
                float bitcoinRate = (Float.parseFloat(String.valueOf(currencyTV.getText()))
                        / Float.parseFloat(cObject.getString("rate_float")));

                bitcoinTV.setText(bitcoinRate + "");
            }

        } catch (Exception e) {

        }
    }

}
