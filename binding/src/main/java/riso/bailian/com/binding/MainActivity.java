package riso.bailian.com.binding;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import riso.bailian.com.binding.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main );
        EventBus.getDefault().register(this);
        initViews();
    }

    private void initViews() {
        binding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageEvent msg = new MessageEvent();
                msg.setName("the damned world");
                msg.setAge("18");
                EventBus.getDefault().post(msg);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(MessageEvent msg){
        binding.tv.setText(msg.getName());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
