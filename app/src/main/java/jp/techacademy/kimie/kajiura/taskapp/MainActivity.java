package jp.techacademy.kimie.kajiura.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.kimie.kajiura.taskapp.TASK";

    //メンバ変数（Realmクラスを保持するmRealmを定義）
    private Realm mRealm;
    //Realmベータベースに追加や削除等変化があった時に呼ばれるリスナー。
    //onChangeメソッドをオーバーライドしてreloadListViewメソッドを呼び出す。
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.sortButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.category_text);
                String item = editText.getText().toString();
                if (item.equals("")){
                    reloadListView();
                }else{
                    RealmResults<Task> taskRealmResults = mRealm.where(Task.class)
                            .equalTo("category",item).findAll();
                    updatelistview(taskRealmResults);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        //ListViewを長押しした時の処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //タスク削除
                final Task task = (Task)parent.getAdapter().getItem(position);

                //ダイアログ表示
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id",task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(),TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }

                });
                builder.setNegativeButton("CANCEL",null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });
        reloadListView();
    }

    private void reloadListView() {
        //RealmDBから「全てのデータを取得して新しい日時順にならべた結果」を取得
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        updatelistview(taskRealmResults);

    }

    private void updatelistview(RealmResults<Task> taskRealmResults) {
        //上記の結果をtoastとしてセット
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        //taskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        //表示更新のため、アダプタにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
 }
