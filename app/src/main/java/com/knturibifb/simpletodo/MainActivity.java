package com.knturibifb.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //to be passed to other activities
    // a numeric code to identify the edit activity
    public static final int EDIT_REQUEST_CODE = 20;
    // keys used for passing data between activities
    public static final String ITEM_TEXT = "itemText";
    public static final String ITEM_POSITION = "itemPosition";

    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // reference to the listView
        lvItems = (ListView) findViewById(R.id.lvItems);
        // read items will initialize and get persistent
        readItems();
        //initialize adapter
        itemsAdapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        //set up the list view listener
        setupListViewListener();

    }


    public void onAddItem(View v){
        //get a reference to the edit text
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        //get the text from the edittext
        String itemText =  etNewItem.getText().toString();
        //if text is empty, return show a toast then return
        if (itemText.trim().equals("")) {
            Toast.makeText(getApplicationContext(),"No empty entries please!", Toast.LENGTH_SHORT).show();
            return;
        }
        //add item to the adapter
        itemsAdapter.add(itemText);
        //save data for persistence
        writeItems();
        //clear the edit text
        etNewItem.setText("");
        //notify user after adding
        Toast.makeText(getApplicationContext(),"Item added to list", Toast.LENGTH_SHORT).show();

    }

    public void setupListViewListener(){
        //set the Listviews itemLongClickListener - will be used for deleting
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                items.remove(position);
                //notify the adapter that the underlying dataset has changed
                itemsAdapter.notifyDataSetChanged();
                //save data for persistence
                writeItems();
                Log.i("MainActivity", "Removed item: "+position);
                //return true to signify that the action(long click) was used up/ it's handler executed
                return true;
            }

        });


        // set the ListView's regular click listener- this will be used for editing
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // first parameter is the context, second is the class of the activity to launch
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                // put "extras" into the bundle for access in the edit activity
                i.putExtra(ITEM_TEXT, items.get(position));
                i.putExtra(ITEM_POSITION, position);
                // brings up the edit activity with the expectation of a result
                startActivityForResult(i, EDIT_REQUEST_CODE);
            }
        });
    }
    //returns file where the data is stored
    private File getDataFile(){
        return new File(getFilesDir(), "todo.txt");
    }
    //method that reads from the file system
    private void readItems() {
        try {
            // create the array using the content in the file
            items = new ArrayList<String>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            // print the error to the console
            e.printStackTrace();
            // just load an empty list
            items = new ArrayList<>();
        }
    }

    // write the items to the filesystem
    private void writeItems() {
        try {
            // save the item list as a line-delimited text file
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            // print the error to the console
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // EDIT_REQUEST_CODE defined with constants
        if (resultCode == RESULT_OK && requestCode == EDIT_REQUEST_CODE) {
            // extract updated item value from result extras
            String updatedItem = data.getExtras().getString(ITEM_TEXT);
            // get the position of the item which was edited
            int position = data.getExtras().getInt(ITEM_POSITION, 0);
            // update the model with the new item text at the edited position
            items.set(position, updatedItem);
            // notify the adapter the model changed
            itemsAdapter.notifyDataSetChanged();
            // Store the updated items back to disk
            writeItems();
            // notify the user the operation completed OK
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        }
    }
}
