package and.todo;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.HashMap;

public class FinishFragment extends Fragment {
    EditDatabaseHelper helper;
    ArrayList<String> bigTitle = new ArrayList<>(); //表示する大目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> bigData = new ArrayList<>(); //表示する大目標データを保管するための配列
    ArrayList<String> middleTitle = new ArrayList<>();//表示する中目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> middleData = new ArrayList<>();//表示する中目標データを保管するための配列
    ArrayList<String> smallTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> smallData = new ArrayList<>();//表示する小目標データを保管するための配列
    ArrayList<String> scheTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> scheData = new ArrayList<>();//表示するスケジュールデータを保管するための配列
    ArrayList<String> todoTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> todoData = new ArrayList<>(); //表示するやることリストデータを保管するための配列
    int bid=0,mid=0,sid=0,scheid=0,todoid=0;//項目選択時のID保管用変数
    ImageButton bedit,medit,sedit,scheedit,todoedit;//各編集ボタン変数
    ImageButton bdl,mdl,sdl,schedl,tododl;//各削除ボタン変数
    Spinner bigTarget,middleTarget; //大中目標のスピナー変数
    ListView sList,scheList,todoList; //小目標スケジュールのリスト変数
    DeleteData del = null; //データ削除用クラス

    public static FinishFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        FinishFragment fragment = new FinishFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_hold,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Activity activity = requireActivity();

        TextView title = activity.findViewById(R.id.holdTitle);
        title.setText("完了済みタスク一覧");

        //Bundleデータ取得
        Bundle Data = getArguments();

        //データベースから各データを取得
        helper = new EditDatabaseHelper(requireActivity());

        //完了済みデータ一覧に表示するデータの配列を取得
        getArrays();

        del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成

        //大目標にデータ設定
        bigTarget = (Spinner) view.findViewById(R.id.bigTarget);
        ArrayAdapter<String> bAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle);
        bigTarget.setAdapter(bAdapter);
        bigTarget.setOnItemSelectedListener(new SpinSelecter());
        if(bigData.size()>0) { //大目標が１つ以上あるときの初期設定
            bid = Integer.parseInt(bigData.get(0).get("id")); //大目標の初期位置ID
        }


        //中目標にデータ設定
        middleTarget = (Spinner) view.findViewById(R.id.middleTarget);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
        middleTarget.setAdapter(mAdapter);
        middleTarget.setOnItemSelectedListener(new SpinSelecter());
        if(middleData.size()>0) { //中目標が１つ以上あるときの初期設定
            mid = Integer.parseInt(middleData.get(0).get("id")); //中目標の初期位置ID
        }

        //小目標にデータ設定
        sList = view.findViewById(R.id.smallTargetList);
        sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));
        sList.setOnItemClickListener(new ListSelecter());

        //スケジュールにデータ設定
        scheList = view.findViewById(R.id.scheduleList);
        scheList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,scheTitle));
        scheList.setOnItemClickListener(new ListSelecter());

        //やることリストにデータ設定
        todoList = view.findViewById(R.id.todoList);
        todoList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todoTitle));
        todoList.setOnItemClickListener(new ListSelecter());

        //各編集ボタン要素を取得
        bedit = (ImageButton) view.findViewById(R.id.BeditButton);
        medit = (ImageButton) view.findViewById(R.id.MeditButton);
        sedit = (ImageButton) view.findViewById(R.id.SeditButton);
        scheedit = (ImageButton) view.findViewById(R.id.ScheEditButton);
        todoedit = (ImageButton) view.findViewById(R.id.todoEdit);
        //各編集ボタンのイベントリスナー
        bedit.setOnClickListener(new editClicker());
        medit.setOnClickListener(new editClicker());
        sedit.setOnClickListener(new editClicker());
        scheedit.setOnClickListener(new editClicker());
        todoedit.setOnClickListener(new editClicker());

        //各削除ボタン要素を取得
        bdl = (ImageButton) view.findViewById(R.id.BdeleteButton);
        mdl = (ImageButton) view.findViewById(R.id.MdeleteButton);
        sdl = (ImageButton) view.findViewById(R.id.SdeleteButton);
        schedl = (ImageButton) view.findViewById(R.id.ScheDeleteButton);
        tododl = (ImageButton) view.findViewById(R.id.todoDelete);
        //各削除ボタンのイベントリスナー
        bdl.setOnClickListener(new editClicker());
        mdl.setOnClickListener(new editClicker());
        sdl.setOnClickListener(new editClicker());
        schedl.setOnClickListener(new editClicker());
        tododl.setOnClickListener(new editClicker());


    }


    class editClicker implements View.OnClickListener { //編集ボタンクリックで編集画面へ飛ばす
        private String dlevel = ""; //削除データの項目変数
        private int did = 0; //削除データのID変数

        Bundle editData = new Bundle(); //データ送信用
        @Override
        public void onClick(View view) {
            if(view == bedit){ //大目標の編集ボタン
                editData.putString("level","big");
                editData.putInt("id",bid);
            }else if(view == medit){ //中目標の編集ボタン
                editData.putString("level","middle");
                editData.putInt("id",mid);
            }else if(view == sedit){ //小目標の編集ボタン
                editData.putString("level","small");
                editData.putInt("id",sid);
            }else if(view == scheedit){ //スケジュール編集ボタン
                editData.putString("level","schedule");
                editData.putInt("id",scheid);
            }else if(view == todoedit){//やること編集ボタン
                editData.putString("level","todo");
                editData.putInt("id",todoid);
            }else if(view == bdl){//大目標の削除ボタン
                dlevel = "big";
                did = bid;
            }else if(view == mdl){//中目標の削除ボタン
                dlevel = "middle";
                did = mid;
            }else if(view == sdl){//小目標の削除ボタン
                dlevel = "small";
                did = sid;
            }else if(view == schedl){//スケジュールの削除ボタン
                dlevel = "schedule";
                did = scheid;
            }else if(view == tododl){//やることリスト削除ボタン
                dlevel = "todo";
                did = todoid;
            }

            if(view == bedit || view == medit || view == sedit || view == todoedit || view == scheedit){
                //編集画面へ飛ばす

                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == bdl || view == mdl || view == sdl || view == schedl || view == tododl){
                //データベースから削除
                del.delete(dlevel,did);
            }

        }
    }


    class SpinSelecter implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            //項目選択時のIDを取得
            if(adapterView == bigTarget){ //大目標選択時のID取得
                bid = Integer.parseInt( bigData.get(position).get("id"));
            }else if(adapterView == middleTarget){ //中目標選択時のID取得
                mid = Integer.parseInt( middleData.get(position).get("id"));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class ListSelecter implements AdapterView.OnItemClickListener{ //クリックされた項目のIDを取得

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if(adapterView == sList){ //小目標選択時のID取得
                sid = Integer.parseInt( smallData.get(position).get("id"));
            }else if(adapterView == scheList){ //スケジュール選択時のID取得
                scheid = Integer.parseInt( scheData.get(position).get("id"));
            }else if(adapterView == todoList){ //やることリスト選択時のID取得
                todoid = Integer.parseInt( todoData.get(position).get("id") );
            }
        }
    }


    void getArrays(){ //データベースから取得してデータ配列に挿入する

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{
                //大目標を取得

                String[] bcols = {"id","title","content","hold","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] blevel = { "big","1" };//大目標、完了済データのみを抽出
                Cursor bcs = db.query("ToDoData",bcols,"level=? and fin=?",blevel,null,null,null,null);

                bigData.clear(); //いったん配列を空にする
                bigTitle.clear();

                boolean next = bcs.moveToFirst();//カーソルの先頭に移動
                while(next){ //Cursorデータが空になるまでbigTitle,bigDataに加えていく
                        bigTitle.add(bcs.getString(1));//大目標のタイトル
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+bcs.getInt(0));
                        item.put("content",bcs.getString(2));
                        item.put("hold",""+bcs.getInt(3));
                        item.put("important",bcs.getString(4));
                        item.put("memo",bcs.getString(5));
                        item.put("proceed",bcs.getString(6));
                        item.put("fin",bcs.getString(7));
                        bigData.add(item);
                    next = bcs.moveToNext();
                }

                //中目標を取得
                String[] mcols = {"id","title","big","bigtitle","bighold","content","hold","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] mlevel = { "middle","1" };//中目標のみを抽出
                Cursor mcs = db.query("ToDoData",mcols,"level=? and fin=?",mlevel,null,null,null,null);

                middleData.clear(); //いったん配列を空にする
                middleTitle.clear();
                next = mcs.moveToFirst();//カーソルの先頭に移動
                while(next){
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+mcs.getInt(0));
                        item.put("big",""+mcs.getInt(2));
                        item.put("bigtitle",""+mcs.getString(3));
                        item.put("bighold",""+mcs.getInt(4));
                        item.put("content",mcs.getString(5));
                        item.put("hold",""+mcs.getInt(6));
                        item.put("important",mcs.getString(7));
                        item.put("memo",mcs.getString(8));
                        item.put("proceed",mcs.getString(9));
                        item.put("fin",mcs.getString(10));
                        if(!item.get("big").equals("0")){ //大目標が存在するとき
                            middleTitle.add(String.format("(%s)-%s",item.get("bigtitle"),mcs.getString(1)));
                        }else{
                            middleTitle.add(String.format("大目標未設定-%s",mcs.getString(1)));
                        }
                        middleData.add(item); //中目標データ配列に追加

                    next = mcs.moveToNext();
                }

                //小目標を取得
                String[] scols = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] slevel = { "small","1" };//小目標のみを抽出
                Cursor scs = db.query("ToDoData",scols,"level=? and fin=?",slevel,null,null,null,null);

                smallData.clear(); //いったん配列を空にする
                smallTitle.clear();
                next = scs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+scs.getInt(0));
                    item.put("title",scs.getString(1));
                    item.put("big",""+scs.getInt(2));
                    item.put("bigtitle",scs.getString(3));
                    item.put("bighold",""+scs.getInt(4));
                    item.put("middle",""+scs.getInt(5));
                    item.put("middletitle",scs.getString(6));
                    item.put("middlehold",""+scs.getInt(7));
                    item.put("content",scs.getString(8));
                    item.put("important",scs.getString(9));
                    item.put("memo",scs.getString(10));
                    item.put("proceed",scs.getString(11));
                    item.put("fin",scs.getString(12));
                    if(!item.get("middle").equals("0")){ //中目標が存在するとき
                        smallTitle.add(String.format("(%s)-(%s)-%s",item.get("bigtitle"),item.get("middletitle"),mcs.getString(1)));
                    }else{
                        middleTitle.add(String.format("大目標未設定-中目標未設定-%s",mcs.getString(1)));
                    }
                    smallData.add(item); //中目標データ配列に追加
                    next = scs.moveToNext();
                }

                //スケジュールを取得
                String[] schecols = {"id","title","date","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] schelevel = { "schedule","1" };//スケジュールのみを抽出
                Cursor schecs = db.query("ToDoData",schecols,"level=? and fin=?",schelevel,null,null,null,null);

                scheData.clear(); //いったん配列を空にする
                scheTitle.clear();
                next = schecs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+schecs.getInt(0));
                    item.put("title",schecs.getString(1));
                    item.put("date",""+schecs.getString(2));
                    item.put("content",schecs.getString(3));
                    item.put("important",schecs.getString(4));
                    item.put("memo",schecs.getString(5));
                    item.put("proceed",schecs.getString(6));
                    item.put("fin",schecs.getString(7));
                    scheData.add(item); //中目標データ配列に追加
                    scheTitle.add(String.format("[%s]%s",item.get("date"),item.get("title")));
                    next = schecs.moveToNext();
                }

                //やることリストを取得
                String[] todocols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] todolevel = { "todo","1" };//やることリストのみを抽出
                Cursor todocs = db.query("ToDoData",todocols,"level=? and fin=?",todolevel,null,null,null,null);

                todoData.clear(); //いったん配列を空にする
                todoTitle.clear();
                next = todocs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+todocs.getInt(0));
                    item.put("title",todocs.getString(1));
                    item.put("content",todocs.getString(2));
                    item.put("important",todocs.getString(3));
                    item.put("memo",todocs.getString(4));
                    item.put("proceed",todocs.getString(5));
                    item.put("fin",todocs.getString(6));
                    todoData.add(item); //中目標データ配列に追加
                    todoTitle.add(String.format("%s",item.get("title")));
                    next = todocs.moveToNext();
                }

                //トランザクション成功
                db.setTransactionSuccessful();
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                //トランザクションを終了
                db.endTransaction();
            }
        }
    }


}