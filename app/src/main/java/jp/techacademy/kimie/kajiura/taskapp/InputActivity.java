package jp.techacademy.kimie.kajiura.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity {

    //メンバ変数定義
    //タスクの日時を保持するint型
    private int mYear, mMonth, mDay, mHour, mMinute;
    //Button：日付と時間を設定
    private Button mDateButton, mTimeButton;
    //EditText：タイトルを入力と、内容を入力するTextTextの保持をする
    private EditText mTitleEdit, mContentEdit;
    private Spinner mCategorySpinner;
    //Taskクラスのオブジェクト
    private Task mTask;

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //DatePickerDialog：日付をユーザーに入力してもらう。各引き数に与えて生成し、
            //onDateSetメソッドでそれらの値を入力された日付で更新。
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d", (mMonth + 1)) + "/" + String.format("%2d", mDay);
                            mDateButton.setText(dateString);
                        }
                    },mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //TimePicnerDialog：時間をユーザに入力してもらう。各引数に与えて生成し、
            //onTimeSetメソッドでそれらの値を入力された時間で更新。
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%2d",mHour) + ":" + String.format("%2d",mMinute);
                            mTimeButton.setText(timeString);
                        }
                    },mHour,mMinute,false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Realmに保存/更新した後、finishメソッドを呼ぶことでInputActivityを閉じて前の画面に戻る。
            addTask();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        //ツールバーをActionボタンとして使えるように設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            //setDisplayHomeAsUpEnabled(true)：ActionBarに「戻る」ボタン表示
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mDateButton = (Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);
        mCategorySpinner = (Spinner)findViewById(R.id.category_sppiner_text);

        Intent intent = getIntent();
        //intent.getIntExtra(MainActivity.EXTRA_TASK, -1);：EXTRA_TASK から　Task のidを取得して、id からTaskのインスタンスを取得
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        Realm realm = Realm.getDefaultInstance();
        //mTask = realm.where(Task.class).equalTo("id", taskId).findFirst();：Taskのidがtaskのものが検索され、
        // findFirst()によって最初に見つかったインスタンスが返されmTaskへ代入。
        mTask = realm.where(Task.class).equalTo("id", taskId).findFirst();
        realm.close();

        if (mTask == null) {
            //新規作成
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.DAY_OF_MONTH);
            mDay = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());
            //mCategorySpinner.set(mTask.getCategory());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }
    }

    private void addTask() {
        Realm realm = Realm.getDefaultInstance();

        //Realmでデータの追加・削除を行う時はbeginTransaction呼び出し→データ処理→commitTransaction
        realm.beginTransaction();

        if (mTask == null) {
            //新規登録
            mTask = new Task();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

            int identifier;
            if (taskRealmResults == null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            }else{
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        String title = mTitleEdit.getText().toString();
        String content  = mContentEdit.getText().toString();
        String category = mCategorySpinner.getSelectedItem().toString();

        mTask.setTitle(title);
        mTask.setContents(content);
        mTask.setCategory(category);

        GregorianCalendar calendar =new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date date = calendar.getTime();
        mTask.setDate(date);

        //copyToRealmOrUpdate：データの保存・更新
        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();

        realm.close();

        //TaskAlarmReceiverを起動するIntent
        Intent resultIntent = new Intent(getApplicationContext(),TaskAlarmReceiver.class);
        resultIntent.putExtra(MainActivity.EXTRA_TASK,mTask.getId());
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                //PendingIntent.FLAG_UPDATE_CURRENT：既存のPendingintentがあればそのままextraのデータだけ書き換える
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        //RTC_WAKEUP：「UTC時間を指定する。画面スリープ中でもアラームを発行する」
        //第2引数でタスクの時間をUTC時間で指定。
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),resultPendingIntent);
    }
}
