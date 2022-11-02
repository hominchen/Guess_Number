package com.example.guessnumber;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    // initView()
    // 四顆在版面輸入的數字。整數陣列宣告，並且設定他初值
    private int[] inputRes =
            {R.id.main_input1,R.id.main_input2,R.id.main_input3,R.id.main_input4};
    private TextView[] input = new TextView[4];
    // 宣告0~9的button。
    private int[] numberRes = {R.id.main_btn_0,R.id.main_btn_1,R.id.main_btn_2,
            R.id.main_btn_3,R.id.main_btn_4,R.id.main_btn_5,R.id.main_btn_6,
            R.id.main_btn_7,R.id.main_btn_8,R.id.main_btn_9};
    private View[] btnNumber = new View[10]; // 因為只被用來做onclick的動作，所以用view即可(原來是button)
    // 在android裡面，任何view都可以被按下；都可以觸發按鈕的事件

    private LinkedList<Integer> answer = new LinkedList<>();

    // 程式後端處理中的四個數字
    private int inputPoint;     // 輸入指標位置 0 - 3
    private LinkedList<Integer> inputValue = new LinkedList<>();   // 輸入數值陣列

    // ListView與adapter
    private ListView listView;
    private SimpleAdapter adapter;
    private String[] from = {"order","guess","result"};
    // 到layout去建置item_round.xml
    private int[] to = {R.id.item_order,R.id.item_guess,R.id.item_result};
    private LinkedList<HashMap<String,String>> hist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initGame();
        initListView();
    }

    // 重玩
    public void replay(View view) {
        // 整個遊戲初始化
        initGame();
        // 歷史紀錄清空
        hist.clear();
        // 通知調變器
        adapter.notifyDataSetChanged();
    }

    // 按下送出
    public void send(View view) {
        // 如果未滿四個數字則直接忽略
        if (inputPoint != 4) return;

        int a, b; a = b = 0; String guess = "";
        for (int i=0; i<inputValue.size(); i++){
            guess += inputValue.get(i);
            // 判斷相同位置的值是否相等
            if (inputValue.get(i).equals(answer.get(i))){
                a++;
            }
            // 判斷answer是否包含inputValue相同的值
            else if (answer.contains(inputValue.get(i))){
                b++;
            }
        }
        // 這邊寫了個log要去判斷對不對
        Log.d("brad", a + "A" + b + "B");
        clear(null);

        HashMap<String,String> row = new HashMap<>();
        row.put(from[0], "" + (hist.size()+1));
        row.put(from[1], guess);
        row.put(from[2], a + "A" + b + "B");
        hist.add(row);
        adapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(hist.size()-1);

        if (a == 4){
            // winner
            displayResult(true);
        }else if(hist.size() == 10){
            // loser
            displayResult(false);
        }
    }

    // 顯示輸贏結果
    private void displayResult(boolean isWinner){
        // 建立對話框的物件實體。AlertDialog對話方塊很像Windows上的彈跳視窗
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("遊戲結果");

        StringBuffer ansString = new StringBuffer();
        for (int i=0; i<answer.size();i++) ansString.append(answer.get(i));

        builder.setMessage(isWinner?"完全正確":"挑戰失敗\n" + "答案:" + ansString);
        builder.setPositiveButton("開新局", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                replay(null);
            }
        });
        builder.create().show();
    }

    // 初始化畫面
    private void initView() {
        // 四個被輸入的數字
        for (int i=0; i<inputRes.length; i++){
            // 將inputRes(資源)逐一放到input陣列當中，放到陣列後跑會較順，也可以寫成四行。
            input[i] = findViewById(inputRes[i]);  // 要回到前面宣告input
        }
        // 0~9的按鈕
        for (int i=0; i<numberRes.length; i++){
            btnNumber[i] = findViewById(numberRes[i]);
        }
    }

    // 初始化一局遊戲
    private void initGame(){
        answer = createAnswer();
        // 產生謎底後，進行清除版面。
        // clear要版面觸發，這邊呼叫clear後將螢幕view宣告為null
        clear(null);
        // 初始化時並將答案印出
        Log.d("brad","answer = " + answer);
    }


    // 輸入數字鍵
    public void inputNumber(View view){
        // inputPoint會逐漸增加直到4，此時只能 send or back or clear
        if (inputPoint == 4) return;

        // 比對輸入鍵0~9
        for (int i=0; i<btnNumber.length; i++){
            // 比對view是不是同於btnNumber[i]
            if (view == btnNumber[i]){
                // Set是一個Interface，有最簡單的Collection資料結構-集合(不重複)
                inputValue.set(inputPoint, i);
                // setText()沒有回傳值，需要String型態字串物件當參數
                input[inputPoint].setText("" + i);  // input是版面數字
                // 因為inputPoint有宣告為全域變數，所以變動後會帶到外面去
                inputPoint++;  // 呼應前面inputPoint==4的限制
                // 按鈕按過之後就鎖住
                btnNumber[i].setEnabled(false);
                break;
            }
        }
    }

    // 輸入清除鍵
    public void clear(View view) {
        inputPoint = 0;  // inputPoint為0~3
        inputValue.clear();
        // 將後端的數字陣列inputValue全設值為-1
        for (int i=0; i<4; i++){
            inputValue.add(-1);
        }
        // 將版面數字input清除
        for (int i = 0; i < input.length; i++) {
            input[i].setText("");  // input是TextView的陣列容器值
        }
        // 將btnNumber 0~9啟用
        for(int i = 0; i<btnNumber.length; i++){
            btnNumber[i].setEnabled(true);
        }
    }

    // 輸入退位鍵
    public void back(View view) {
        // 先判斷位數是否為0
        if (inputPoint == 0) return;

        // 如果可以輸入，代表位數會少一位
        inputPoint--;
        // 存放btnNumber在inputPoint的位置把它取出，按鍵啟用
        btnNumber[inputValue.get(inputPoint)].setEnabled(true);
        // 將inputValue對應inputPoint的位置，值設為-1
        inputValue.set(inputPoint, -1);
        // 將版面數字該位置值清空
        input[inputPoint].setText("");
    }

    // 初始化 ListView
    private void initListView(){
        listView = findViewById(R.id.main_listview);
        hist = new LinkedList<>();
        adapter = new SimpleAdapter(this,hist,R.layout.item_round,from,to);
        listView.setAdapter(adapter);
    }

    // 產生謎底
    private LinkedList<Integer> createAnswer(){
        LinkedList<Integer> ret = new LinkedList<>();
        // HashSet將產生不會重複且無序的數字
        HashSet<Integer> nums = new HashSet<>();
        // 產生4個0~9隨機整數加到nums裡面
        while (nums.size()<4){
            nums.add((int)(Math.random()*10));
        }
        // 將nums的無序性，用i逐一提取nums放到ret裡面產生順序
        for (Integer i : nums){
            ret.add(i);
        }
        // 洗牌，隨機打亂原來的順序
        // 再將前10個球拿來洗，取出前4顆
        Collections.shuffle(ret);
        return ret;
    }
}