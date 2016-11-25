package by.wink.expressionkit;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.profileexpression.FacebookProfileMediaUtils;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    View uploadBtn;
    VideoView videoView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        uploadBtn = findViewById(R.id.upload_btn);
        videoView = (VideoView) findViewById(R.id.video_view);


        if (getIntent().getAction() != null &&
                getIntent().getAction().equals("facebook.intent.action.PROFILE_MEDIA_CREATE")) {
            FacebookProfileMediaUtils.onDeepLinkFromFacebook(getIntent().getExtras());
        }

        textView = (TextView) findViewById(R.id.usernmae);

        /*

        *  Facebook Login
        * */
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Facebook success: ", loginResult.getAccessToken().toString());
                textView.setText("user_id: " + loginResult.getAccessToken().getUserId());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });


        /*
        *   button that call the profile expression kit method
        * */
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (canDeepLinkIntoFacebookProfileVideoFlow(getApplicationContext())) {
                    Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.party);
                    deepLinkIntoFacebookProfileVideoFlow(getApplicationContext(), videoUri);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "You can't set a profile video, Error code:" +
                                    FacebookProfileMediaUtils.canSetProfileVideo(getApplicationContext()), Toast.LENGTH_LONG)
                            .show();
                }

            }
        });

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.party));
        videoView.setOnPreparedListener(PreparedListener);
        videoView.start();

    }


    /*
    * listener for video player
    * */

    MediaPlayer.OnPreparedListener PreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer m) {
            try {
                if (m.isPlaying()) {
                    m.stop();
                    m.release();
                    m = new MediaPlayer();
                }
                m.setVolume(0f, 0f);
                m.setLooping(false);
                m.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public void deepLinkIntoFacebookProfileVideoFlow(Context context, Uri videoUri) {
        FacebookProfileMediaUtils.setProfileVideo(context,
                getResources().getString(R.string.facebook_app_id), context.getPackageName(), videoUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    public boolean canDeepLinkIntoFacebookProfileVideoFlow(Context context) {
        int deepLinkStatus = FacebookProfileMediaUtils.canSetProfileVideo(context);
        return deepLinkStatus == FacebookProfileMediaUtils.RESULT_CODE_CAN_DEEP_LINK;
    }

}
