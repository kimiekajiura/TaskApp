package jp.techacademy.kimie.kajiura.taskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable{
    private String title;//タイトル
    private String contents;//内容
    private Date date;//日時

    private String category;

    //idを主キーに設定
    @PrimaryKey
    private int id;

    public String getTitle() {
        return title;
    }

    public  void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents){
        this.contents = contents;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public Date getDate() {
        return  date;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id=id;
    }


}