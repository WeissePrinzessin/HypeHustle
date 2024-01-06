package MainScreen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import HelpClass.SampleTask;

public class MainS extends Activity{
    TextView nameTask, descriptionTask, priceTask, timeTask,authorTask,proofTask,idAddTask;
    String idTask,URL, idPrev;
    int myID;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        gestureDetector = new GestureDetector(this, this);

        nameTask = findViewById(R.id.name);
        descriptionTask = findViewById(R.id.description);
        priceTask = findViewById(R.id.price);
        timeTask = findViewById(R.id.time);
        authorTask = findViewById(R.id.author);
        myID=1;
        URL = " https://ecb7-95-104-188-73.ngrok-free.app/get_task.php";

        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ID_TASK", "1");
            editor.apply();
        }
        //SharedPreferences.Editor editor = prefs.edit();
        //my_id = prefs.getInt("myID",0);
    }

    protected void onStop() {
        super.onStop();
        finish(); // Закрыть текущую активность
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        idTask = prefs.getString("ID_TASK","ID_TASK");
        idPrev = idTask;
        try {
            get_task(URL,0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        nameTask.setText("loading");
    }

    protected void saved() {

        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ID_TASK", idTask);
        editor.apply();
    }

    public void profile1(View view) {
        Intent intent = new Intent(this, profile1.class);
        startActivity(intent);
    }

    public void fortuna(View view) {
        Intent intent = new Intent(this, fortuna.class);
        startActivity(intent);
    }

    public void main_screen(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void new_task(View view) {
        Intent intent = new Intent(this, new_task.class);
        startActivity(intent);
    }

    public void current_task(View view) {
        Intent intent = new Intent(this, ProposedTask.class);
        startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (e1.getX() < e2.getX()) {
            try {
                get_task(URL,0);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                get_task(URL,1);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    private void get_task(String url_str, int flag) throws JSONException {

        // Создаем JSON-объект с данными
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id_task", idTask);
        jsonObject.put("id_prev", idPrev);
        jsonObject.put("status", "ok");

        if (flag == 1){
            jsonObject.put("id_accept", myID);
        }
        else{
            jsonObject.put("id_accept", 0);
        }

        // Создаем Executor
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                // Создаем URL-соединение
                URL url = new URL(URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");

                // Получаем OutputStream и пишем в него JSON-строку
                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
                outputStream.close();

                // Считываем ответ от сервера
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Обрабатываем ответ от сервера

                    JSONObject responseJson = new JSONObject(response.toString());
                    String status = responseJson.getString("status");
                    idTask = responseJson.getString("id_task");
                    idPrev = responseJson.getString("id_prev");
                    String name_api = responseJson.getString("name");
                    String description_api = responseJson.getString("description");
                    String author_api = responseJson.getString("author");
                    String price_api = responseJson.getString("price");
                    String proof_api = responseJson.getString("proof");
                    String time_api = responseJson.getString("time");
                    String id_add_api = responseJson.getString("id_add");
                    //String id_accept_api = responseJson.getString("id_accept");
                    saved();
                    if (status.equals("ok"))
                    {
                        nameTask.setText(name_api);
                        descriptionTask.setText(description_api);
                        authorTask.setText(author_api);
                        priceTask.setText(price_api);
                        proofTask.setText(proof_api);
                        timeTask.setText(time_api);

                    } else {
                        // Делаем что-то, если ответ от сервера не "ok"
                    }
                } else {
                    // Делаем что-то, если ответ от сервера не "HTTP_OK"
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
