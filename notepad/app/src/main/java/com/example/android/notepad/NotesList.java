/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad;
import com.example.android.notepad.NotePad;
import com.example.android.notepad.application.MyApplication;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class NotesList extends ListActivity implements View.OnClickListener {


    private EditText et_Search;//搜索框控件实例
    private ImageView iv_searchnotes;//搜索按钮实例
    private ListView lv_notesList;
    private NotesListAdapter adapter;
    private ImageView iv_addnotes;//添加按钮
    private LinearLayout ll_noteList;
    // For logging and debugging
    private static final String TAG = "NotesList";


    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_CREATE_DATE,//2
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE//3
    };



    private static final int COLUMN_INDEX_TITLE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteslist_layout);
        initView();
        // The user does not need to hold down the key to use menu shortcuts.
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        Intent intent = getIntent();
        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of notes.
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }


        getListView().setOnCreateContextMenuListener(this);


        Cursor cursor = managedQuery(
            getIntent().getData(),            // Use the default content URI for the provider.
            PROJECTION,                       // Return the note ID and title for each note.
            null,                             // No where clause, return all records.
            null,                             // No where clause, therefore no where column values.
            NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
        );

        adapter=new NotesListAdapter(getApplicationContext(),cursor,getIntent().getData(),getIntent().getAction());
        lv_notesList.setAdapter(adapter);

    }

    /*
    绑定id
     */
    private void initView() {
        ll_noteList= (LinearLayout) findViewById(R.id.noteList_layout);
        iv_addnotes= (ImageView) findViewById(R.id.fab);
        lv_notesList= (ListView) findViewById(android.R.id.list);//绑定listView;
        et_Search= (EditText) findViewById(R.id.et_Search);
        iv_searchnotes= (ImageView) findViewById(R.id.iv_searchnotes);

        iv_addnotes.setOnClickListener(this);
        iv_searchnotes.setOnClickListener(this);
        ll_noteList.setBackgroundColor(Color.parseColor(MyApplication.getBackground()));
        lv_notesList.setBackgroundColor(Color.parseColor(MyApplication.getBackground()));

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
                break;
            case R.id.iv_searchnotes:
                showOrhide();
                if(et_Search.getText().toString().equals("")){
                    Cursor cursor1 = managedQuery(
                            getIntent().getData(),            // Use the default content URI for the provider.
                            PROJECTION,                       // Return the note ID and title for each note.
                            null,                             // No where clause, return all records.
                            null,                             // No where clause, therefore no where column values.
                            NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                    );
                    adapter.readDate(cursor1);
                    adapter.notifyDataSetChanged();
                }else{
                    adapter.Search(et_Search.getText().toString());
                }

                break;
        }
    }

    /*
    软硬盘的显示和隐藏
     */
    private void showOrhide(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Cursor cursor1 = managedQuery(
               getIntent().getData(),            // Use the default content URI for the provider.
                PROJECTION,                       // Return the note ID and title for each note.
                null,                             // No where clause, return all records.
                null,                             // No where clause, therefore no where column values.
                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
        );
        adapter.readDate(cursor1);
        adapter.notifyDataSetChanged();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);


        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // The paste menu item is enabled if there is data on the clipboard.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);


        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        // If the clipboard contains an item, enables the Paste option on the menu.
        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            // If the clipboard is empty, disables the menu's Paste option.
            mPasteItem.setEnabled(false);
        }

        // Gets the number of notes currently being displayed.
        final boolean haveItems = adapter.getCount() > 0;


        if (haveItems) {

            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Creates an array of Intents with one element. This will be used to send an Intent
            // based on the selected menu item.
            Intent[] specifics = new Intent[1];

            // Sets the Intent in the array to be an EDIT action on the URI of the selected note.
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // Creates an array of menu items with one element. This will contain the EDIT option.
            MenuItem[] items = new MenuItem[1];

            // Creates an Intent with no specific action, using the URI of the selected note.
            Intent intent = new Intent(null, uri);


            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);


            menu.addIntentOptions(
                Menu.CATEGORY_ALTERNATIVE,  // Add the Intents as options in the alternatives group.
                Menu.NONE,                  // A unique item ID is not required.
                Menu.NONE,                  // The alternatives don't need to be in order.
                null,                       // The caller's name is not excluded from the group.
                specifics,                  // These specific options must appear first.
                intent,                     // These Intent objects map to the options in specifics.
                Menu.NONE,                  // No flags are required.
                items                       // The menu items generated from the specifics-to-
                                            // Intents mapping
            );
                // If the Edit menu item exists, adds shortcuts for it.
                if (items[0] != null) {

                    // Sets the Edit menu item shortcut to numeric "1", letter "e"
                    items[0].setShortcut('1', 'e');
                }
            } else {
                // If the list is empty, removes any existing alternative actions from the menu
                menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
            }

        // Displays the menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_paste:
          /*
           * Launches a new Activity using an Intent. The intent filter for the Activity
           * has to have action ACTION_PASTE. No category is set, so DEFAULT is assumed.
           * In effect, this starts the NoteEditor Activity in NotePad.
           */
          startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
          return true;
            case R.id.bg_change:
                showpopSelectBgWindows();
                return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        // Tries to get the position of the item in the ListView that was long-pressed.
        try {
            // Casts the incoming data object into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            // If the menu object can't be cast, logs an error.
            Log.e(TAG, "bad menuInfo", e);
            return;
        }



        Cursor cursor = managedQuery(
                Uri.parse(getIntent().getData()+"/"+adapter.getmDate().get(info.position).getCursor_id()),            // Use the default content URI for the provider.
                PROJECTION,                       // Return the note ID and title for each note.
                null,                             // No where clause, return all records.
                null,                             // No where clause, therefore no where column values.
                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
        );

        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }else{
            cursor.moveToNext();
        }
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        // Sets the menu header to be the title of the selected note.
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));


        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(),
                                        adapter.getmDate().get(info.position).getCursor_id()) );
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);


    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;


        try {
            // Casts the data object in the item into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {

            // If the object can't be cast, logs an error
            Log.e(TAG, "bad menuInfo", e);

            // Triggers default processing of the menu item.
            return false;
        }
        // Appends the selected note's ID to the URI sent with the incoming Intent.
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), Integer.parseInt(adapter.getmDate().get(info.position).getCursor_id()));
        /*
         * Gets the menu item's ID and compares it to known actions.
         */
        switch (item.getItemId()) {
            case R.id.context_open:
                // Launch activity to view/edit the currently selected item
                startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
                return true;
            //BEGIN_INCLUDE(copy)
            case R.id.context_copy:
                // Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);

                // Copies the notes URI to the clipboard. In effect, this copies the note itself
                clipboard.setPrimaryClip(ClipData.newUri(   // new clipboard item holding a URI
                        getContentResolver(),               // resolver to retrieve URI info
                        "Note",                             // label for the clip
                        noteUri)                            // the URI
                );

                // Returns to the caller and skips further processing.
                return true;
             //END_INCLUDE(copy)
            default:
                return super.onContextItemSelected(item);
        }
    }
    /*
       背景颜色选择框
        */
    private  void showpopSelectBgWindows(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bg_select_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("背景");//设置标题
        builder.setView(view);
        AlertDialog dialog = builder.create();//获取dialog
        dialog.show();//显示对话框
    }

    /*
    背景改变的监听
     */
    public void ColorSelect(View view){
        String color;
        switch(view.getId()){
            case R.id.pink:

                Drawable btnDrawable1 = getResources().getDrawable(R.drawable.pink);
                ll_noteList.setBackgroundDrawable(btnDrawable1);
                lv_notesList.setBackgroundDrawable(btnDrawable1);

                break;
            case R.id.Yello:
                Drawable btnDrawable2 = getResources().getDrawable(R.drawable.yellow);
                ll_noteList.setBackgroundDrawable(btnDrawable2);
                lv_notesList.setBackgroundDrawable(btnDrawable2);
                break;
            case R.id.PaleVioletRed:
                Drawable btnDrawable3 = getResources().getDrawable(R.drawable.palevioletred);
                ll_noteList.setBackgroundDrawable(btnDrawable3);
                lv_notesList.setBackgroundDrawable(btnDrawable3);
                break;
            case R.id.LightGrey:
                Drawable btnDrawable4 = getResources().getDrawable(R.drawable.lightgrey);
                ll_noteList.setBackgroundDrawable(btnDrawable4);
                lv_notesList.setBackgroundDrawable(btnDrawable4);
                break;
            case R.id.MediumPurple:
                Drawable btnDrawable5 = getResources().getDrawable(R.drawable.mediumpurple);
                ll_noteList.setBackgroundDrawable(btnDrawable5);
                lv_notesList.setBackgroundDrawable(btnDrawable5);
                break;
            case R.id.DarkGray:
                Drawable btnDrawable6 = getResources().getDrawable(R.drawable.darkgray);
                ll_noteList.setBackgroundDrawable(btnDrawable6);
                lv_notesList.setBackgroundDrawable(btnDrawable6);
                break;
            case R.id.Snow:
                Drawable btnDrawable7 = getResources().getDrawable(R.drawable.snow);
                ll_noteList.setBackgroundDrawable(btnDrawable7);
                lv_notesList.setBackgroundDrawable(btnDrawable7);
                break;
        }

    }



}
