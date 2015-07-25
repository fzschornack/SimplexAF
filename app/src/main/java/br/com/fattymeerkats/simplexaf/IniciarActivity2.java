package br.com.fattymeerkats.simplexaf;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.app.ActionBar.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Isabel on 19/07/15.
 */
public class IniciarActivity2 extends ActionBarActivity {
    //EditText num_var, num_rest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int num_var = Integer.parseInt(extras.getString("var"));
            int num_rest = Integer.parseInt(extras.getString("rest"));

            final LinearLayout lm = (LinearLayout) findViewById(R.id.ll);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.HORIZONTAL);

            /*String signs[] = {"Max","Min"};
            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, signs);
            spinner.setAdapter(spinnerArrayAdapter);
            l.addView(spinner);*/

            for (int j = 0; j < num_rest; j++) {
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.HORIZONTAL);

                for (int i = 1; i < num_var; i++) {

                    EditText coef = new EditText(this);
                    coef.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    coef.setEms(2);
                    coef.setGravity(Gravity.CENTER);
                    ll.addView(coef);

                    TextView txt = new TextView(this);
                    txt.setText(Html.fromHtml(" x<sub><small><small>" + i + "</small></small></sub> +"));
                    txt.setTextSize(18);
                    txt.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    ll.addView(txt);
                }

                EditText coef = new EditText(this);
                coef.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                coef.setEms(2);
                coef.setGravity(Gravity.CENTER);
                ll.addView(coef);

                TextView txt = new TextView(this);
                txt.setText(Html.fromHtml(" x<sub><small><small>" + num_var + "</small></small></sub>"));
                txt.setTextSize(18);
                ll.addView(txt);

                String signs[] = {"≤","≥", "="};
                Spinner spinner = new Spinner(this);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, signs);
                spinner.setAdapter(spinnerArrayAdapter);
                ll.addView(spinner);

                EditText rhs = new EditText(this);
                rhs.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                rhs.setEms(2);
                rhs.setGravity(Gravity.CENTER);
                ll.addView(rhs);

                lm.addView(ll);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickProx (View view) {
        /*num_var = (EditText)findViewById(R.id.et_var);
        num_rest = (EditText)findViewById(R.id.et_rest);

        //num_var.getText().toString();
        //num_rest.getText().toString();

        Intent i = new Intent(getApplicationContext(), IniciarActivity2.class);
        i.putExtra("var",num_var.getText().toString());
        i.putExtra("rest", num_rest.getText().toString());
        startActivity(i);*/
    }
}
