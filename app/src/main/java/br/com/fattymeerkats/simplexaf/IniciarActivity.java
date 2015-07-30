package br.com.fattymeerkats.simplexaf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Isabel on 18/07/15.
 */
public class IniciarActivity extends ActionBarActivity {
    EditText num_var, num_rest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar);
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
        num_var = (EditText)findViewById(R.id.et_var);
        num_rest = (EditText)findViewById(R.id.et_rest);

        //num_var.getText().toString();
        //num_rest.getText().toString();

        Intent i = new Intent(getApplicationContext(), IniciarActivity2.class);
        i.putExtra("var",num_var.getText().toString());
        i.putExtra("rest", num_rest.getText().toString());
        startActivity(i);
    }
}
