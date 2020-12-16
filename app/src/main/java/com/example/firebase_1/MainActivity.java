package com.example.firebase_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private ListView listViewData;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter adapter;
    private FirebaseDatabase firebaseControl;
    private DatabaseReference studentDB;
    private int dbCount;
    private EditText editTextName,editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        listViewData = (ListView) findViewById(R.id.listView_id);

        dataList = new ArrayList<Map<String,Object>>();
        dataList.clear();

        adapter = new SimpleAdapter(context,dataList,R.layout.item_layout,new String[]{"name","phone"},new int[]{R.id.textView_name,R.id.textView_phone});
        listViewData.setAdapter(adapter);

        firebaseControl = FirebaseDatabase.getInstance();
        studentDB = firebaseControl.getReference("student");
        Log.d("main","firebaseControl = "+firebaseControl);
        Log.d("main","studentDB = "+studentDB);
//        studentDB.child("4").child("name").setValue("Jane");
//        studentDB.child("4").child("phone").setValue("46546");
        //監聽firebase變化
        studentDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                dataList.clear();
                dbCount = (int) snapshot.getChildrenCount();
                Log.d("main","db count = "+dbCount);

                for(DataSnapshot ds:snapshot.getChildren()){
                    HashMap<String, Object> mapData = new HashMap<String, Object>();
                    String nameData = (String) ds.child("name").getValue();
                    Log.d("main","nameData = "+nameData);
                    if(nameData.length() == 0){
                        mapData.put("name","unKnow");
                    } else {
                        mapData.put("name",nameData);
                    }

                    String phoneData = (String) ds.child("phone").getValue();
                    Log.d("main","phoneData = "+phoneData);
                    if(phoneData.length() == 0){
                        mapData.put("phone","no number");
                    } else {
                        mapData.put("phone",phoneData);
                    }

                    dataList.add(mapData);
                }//end for

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listViewData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            String name;
            String phone;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("main","listview");
                Map<String,Object> itemData = (Map<String, Object>) parent.getItemAtPosition(position);
                name = (String) itemData.get("name");
                phone =(String) itemData.get("phone");
                Log.d("main","name = "+name);
                Log.d("main","phone = "+phone);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete item");
                builder.setMessage("name = "+name+"\nphone = "+phone+"\n");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Query nameQuery = studentDB.orderByChild("name").equalTo(name);
                        nameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) { //取的跟名字相同的電話
                                for(DataSnapshot nameshot : snapshot.getChildren()){
                                    String phoneString = (String) nameshot.child("phone").getValue();
                                    Log.d("main","phoneString = "+phoneString);
                                    //如果彈窗的電話和原本電話相同則刪掉
                                    if(phoneString.equals(phone)){
                                        nameshot.getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        dialog.dismiss();

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                });

                builder.create().show();

            }//end onItem click
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.setup,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_item:
//                Toast.makeText(context,"Add item",Toast.LENGTH_SHORT).show();
                dialog_add();
            case  R.id.login_item:
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialog_add() {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_layout, (ViewGroup) findViewById(R.id.dialogLayout_id));

        editTextName = (EditText) view.findViewById(R.id.editText_name);
        editTextPhone = (EditText) view.findViewById(R.id.editText_phone);
        editTextName.setText("");
        editTextPhone.setText("");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add item");
        builder.setIcon(R.drawable.add_images);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editTextName.getText().toString();
                String phone = editTextPhone.getText().toString();
                Log.d("main","edit name = "+name);
                Log.d("main","edit phone = "+phone);

                HashMap<String, Object> data = new HashMap<String, Object>();
                data.put("name",name);
                data.put("phone",phone);

                Task<Void> result = studentDB.child("").push().setValue(data);//隨意產生ID亂碼 要自行維護
//                Task<Void> result = studentDB.child("10").updateChildren(data);//可以產生ID
                result.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"add OK",Toast.LENGTH_SHORT).show();
                    }
                });

                result.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"add fail",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}