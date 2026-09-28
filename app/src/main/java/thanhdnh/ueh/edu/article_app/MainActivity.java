package thanhdnh.ueh.edu.article_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  public GridView gridview;
  public ProgressBar progressBar;

  private final AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      UserProfile user = (UserProfile) gridview.getAdapter().getItem(position);
      if (user != null) {
        Intent intent = new Intent(MainActivity.this, ViewUserActivity.class);
        intent.putExtra("user_id", user.getId());
        startActivity(intent);
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    gridview = findViewById(R.id.gridview);
    progressBar = findViewById(R.id.progressBar);

    UserData.loadData(
        "https://raw.githubusercontent.com/thanhdnh/json/main/users.json",
        this,
        gridview,
        progressBar,
        this
    );

    gridview.setOnItemClickListener(onItemClick);
  }
}
