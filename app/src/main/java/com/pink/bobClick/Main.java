package com.pink.bobClick;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adivery.sdk.Adivery;
import com.adivery.sdk.AdiveryAdListener;
import com.adivery.sdk.AdiveryNativeAdView;
import com.google.android.material.button.MaterialButton;
import com.pink.bobClick.model.InputApplicationVersionDTO;
import com.pink.bobClick.model.OutputGetApplicationVersion;
import com.pink.bobClick.model.OutputServiceModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Main extends AppCompatActivity {
    Handler h = new Handler();
    SharedPreferences sp;
    Editor ed;
    boolean isForeground = true;
    MediaPlayer mp;
    SoundPool sndpool;
    int snd_info;
    int snd_hit;
    int snd_result;
    int snd_move;
    int score;
    int t;
    int screen_width;
    int screen_height;
    int current_section = R.id.main;
    boolean show_leaderboard;
    final List<ImageView> holes = new ArrayList<ImageView>();
    float start_x;
    float start_y;
    int monster_size;
    AnimatorSet anim;
    int current_monster;
    static int time = 120; // time
    final int cols = 6; // number of cols
    final int rows = 3; // number of rows
    static int show_time = 550; // monster show time in milliseconds
    boolean state = true;
    boolean temp = false;
    // true hero -- false
    public Random rand;
    public int cunterGift = 0;
    public int c;


    // AdMob
    //AdView adMob_smart;
    //InterstitialAd adMob_interstitial;
    final boolean show_admob_smart = true; // show AdMob Smart banner
    final boolean show_admob_interstitial = true; // show AdMob Interstitial
    InputApplicationVersionDTO inputApplicationVersionDTO;
    public void loaddataForVersion(){
        inputApplicationVersionDTO = new InputApplicationVersionDTO();
        inputApplicationVersionDTO.setApplicationName(getPackageName());
        inputApplicationVersionDTO.setMarketName(getString(R.string.market));
    }
    int width;
    private void getApplicationVersion(InputApplicationVersionDTO input) {

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<OutputServiceModel<OutputGetApplicationVersion>> call = apiInterface.getApplicationVersionCode(input);
        call.enqueue(new Callback<OutputServiceModel<OutputGetApplicationVersion>>() {
            @Override
            public void onResponse(Call<OutputServiceModel<OutputGetApplicationVersion>> call, Response<OutputServiceModel<OutputGetApplicationVersion>> response) {

                if (response.isSuccessful()) {
                    int statusCode = response.body().getStatus().getCode();
                    String message = response.body().getStatus().getMessage();
                    Log.i("elsaTeam", statusCode + "");
                    if (statusCode == 1) {
                        String newVersion = response.body().getBody().getVersionNumber();
                        String versionText = response.body().getBody().getVersionText();
                        boolean forceUpdate = response.body().getBody().isForceUpate();
                        Log.i("elsaTeam", newVersion + "/" + versionText);
                        String appVersionName = BuildConfig.VERSION_NAME;
                        if (Float.parseFloat(newVersion) > Float.parseFloat(appVersionName)) {
                            Log.e("elsaTeam", "Display Dialog Update");
                            updateDialog(versionText, forceUpdate);
                        }
                    } else {

                        Log.i("elsaTeam", message + "");

                    }
                } else {

                    Log.i("elsaTeam", "Error:-(");

                }
            }

            @Override
            public void onFailure(Call<OutputServiceModel<OutputGetApplicationVersion>> call, Throwable t) {
                if (t instanceof IOException) {

                    Log.i("elsaTeam", "Error :-( MayBe  internet Problem");
                }
            }
        });


    }
    public void updateDialog(String s, Boolean forceUpdate) {
        final Dialog dialog = new Dialog(Main.this);
        dialog.setContentView(R.layout.dialog_update);
        Window window = dialog.getWindow();
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        MaterialButton updateBtn = (MaterialButton) dialog.findViewById(R.id.updateBtn);
        TextView versionText = (TextView) dialog.findViewById(R.id.versionText);
        versionText.setText(s);
        updateBtn.setOnClickListener(v1 -> {


            if (forceUpdate) {

                if (getString(R.string.market).equals("CafeBazar")) {
                    Uri uri = Uri.parse(getResources().getString(R.string.cafe_link) + getPackageName());
                    Intent goMarket = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(goMarket);
                } else {
                    Uri uri = Uri.parse(getResources().getString(R.string.myket_link) + getPackageName());
                    Intent goMarket = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(goMarket);
                }
                dialog.dismiss();
                finish();
            } else {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
        loaddataForVersion();
        getApplicationVersion(inputApplicationVersionDTO);
        Adivery.prepareInterstitialAd(Main.this, getString(R.string.interstitial));
        time = 90;
        show_time = 550;

        AdiveryNativeAdView adView = findViewById(R.id.native_ad_view);
        adView.setListener(new AdiveryAdListener() {
            @Override
            public void onAdLoaded() {
                // تبلیغ شما بارگذاری شد
                adView.setVisibility(View.VISIBLE);
                Log.i("elsaTeam","kkkk");

            }

            @Override
            public void onError(String reason) {
                adView.setVisibility(View.INVISIBLE);
                Log.i("elsaTeam","kkkkfffffff");
            }

        });
        adView.loadAd();



//		Adad.setTestMode(true);

        // fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // preferences
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        ed = sp.edit();

        // AdMob smart
        add_admob_smart();

        // bg sound
        mp = new MediaPlayer();
        try {
            AssetFileDescriptor descriptor = getAssets().openFd("snd_bg.mp3");
            mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setLooping(true);
            mp.setVolume(0, 0);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
        }

        // if mute
        if (sp.getBoolean("mute", false)) {
            ((ImageView) findViewById(R.id.btn_sound)).setImageResource(R.drawable.btn_sound_off);
        } else {
            mp.setVolume(0.2f, 0.2f);
        }

        // SoundPool
        sndpool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        try {
            snd_result = sndpool.load(getAssets().openFd("snd_result.mp3"), 1);
            snd_info = sndpool.load(getAssets().openFd("snd_info.mp3"), 1);
            snd_hit = sndpool.load(getAssets().openFd("snd_hit.mp3"), 1);
            snd_move = sndpool.load(getAssets().openFd("snd_move.mp3"), 1);
        } catch (IOException e) {
        }

        // hide navigation bar listener
        findViewById(R.id.all).setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                hide_navigation_bar();
            }
        });

        // add holes
        for (int i = 0; i < cols * rows; i++) {
            ImageView hole = new ImageView(this);
            hole.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            hole.setImageResource(R.drawable.hole);
            ((ViewGroup) findViewById(R.id.game)).addView(hole);
            holes.add(hole);
        }

        // index
        findViewById(R.id.txt_score).bringToFront();
        findViewById(R.id.txt_time).bringToFront();
        findViewById(R.id.monster).bringToFront();
        findViewById(R.id.mess).bringToFront();



        SCALE();
        // touch listener
        findViewById(R.id.monster).setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.isEnabled() && event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setEnabled(false);
                    h.removeCallbacks(show_monster);
                    h.removeCallbacks(hide_monster);
                    // score

                    // mohammad
                    cunterGift++;

                    if (state) {
                        ((ImageView) findViewById(R.id.monster)).setImageResource(R.drawable.dead);
                        score += 2;
                        ((TextView) findViewById(R.id.txt_score)).setText(getString(R.string.score) + " " + score);

                    } else {
                        ((ImageView) findViewById(R.id.monster)).setImageResource(R.drawable.dead2);
                        t += 10;
                        ((TextView) findViewById(R.id.txt_time)).setText(getString(R.string.time) + " " + t);
                        score += 20;
                        ((TextView) findViewById(R.id.txt_score)).setText(getString(R.string.score) + " " + score);

                        state = true;
                    }


                    Main.time += 1;
                    int level = score % 100;
                    if (level == 0)
                        show_time -= 30;

                    // sound
                    if (!sp.getBoolean("mute", false) && isForeground)
                        sndpool.play(snd_hit, 1f, 1f, 0, 0, 1);

                    // animation
                    if (anim != null) {
                        anim.removeAllListeners();
                        anim.cancel();
                    }

                    h.post(hide_monster);
                }

                return false;
            }
        });
    }

    // SCALE
    void SCALE() {
        // txt_score
        ((TextView) findViewById(R.id.txt_score)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(22));
        FrameLayout.LayoutParams l = (FrameLayout.LayoutParams) findViewById(R.id.txt_score).getLayoutParams();
        l.setMargins((int) DpToPx(6), (int) DpToPx(2), 0, 0);
        findViewById(R.id.txt_score).setLayoutParams(l);

        // txt_time
        ((TextView) findViewById(R.id.txt_time)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(22));
        l = (FrameLayout.LayoutParams) findViewById(R.id.txt_time).getLayoutParams();
        l.setMargins(0, (int) DpToPx(2), (int) DpToPx(6), 0);
        findViewById(R.id.txt_time).setLayoutParams(l);


        // text result
        ((TextView) findViewById(R.id.txt_result)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(40));
        ((TextView) findViewById(R.id.txt_high_result)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(25));

        // text mess
        ((TextView) findViewById(R.id.mess)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(30));
    }

    // START
    void START() {
        score = 0;

        t = time;
        show_section(R.id.game);
        findViewById(R.id.mess).setVisibility(View.GONE);
        findViewById(R.id.monster).setEnabled(false);
        findViewById(R.id.monster).setScaleX(0f);
        findViewById(R.id.monster).setScaleY(0f);

        ((TextView) findViewById(R.id.txt_score)).setText(getString(R.string.score) + " " + score);
        ((TextView) findViewById(R.id.txt_time)).setText(getString(R.string.time) + " " + t);

        // screen size
        screen_width = Math.max(findViewById(R.id.all).getWidth(), findViewById(R.id.all).getHeight());
        screen_height = Math.min(findViewById(R.id.all).getWidth(), findViewById(R.id.all).getHeight());

        // monster_size
        monster_size = Math.min(screen_width / cols, screen_height / rows);
        findViewById(R.id.monster).getLayoutParams().width = findViewById(R.id.monster).getLayoutParams().height = monster_size;

        start_x = (screen_width - monster_size * cols) / 2f;
        start_y = (screen_height - monster_size * rows) / 2f;

        // holes position
        int x_pos = 0;
        int y_pos = 0;
        for (int i = 0; i < holes.size(); i++) {
            holes.get(i).getLayoutParams().width = holes.get(i).getLayoutParams().height = monster_size;
            holes.get(i).setX(start_x + x_pos * monster_size);
            holes.get(i).setY(start_y + y_pos * monster_size);
            x_pos++;

            if (x_pos == cols) {
                x_pos = 0;
                y_pos++;
            }
        }

        h.postDelayed(TIMER, 1000);
        h.postDelayed(show_monster, 1 + Math.round(Math.random() * 500));
    }

    // show_monster
    Runnable show_monster = new Runnable() {
        @Override
        public void run() {

            // random_monster
            // mohammad

            if (state == false) {
                state = true;
            }

            if (cunterGift >= 5) {
                Random Randy = new Random();
                int randomNumber = Randy.nextInt(15);


                if (randomNumber % 5 == 0) {
                    state = false;
                    cunterGift = 0;
                }
                if (randomNumber % 5 == 1) {
                    state = true;
                }
                if (randomNumber % 5 == 2) {
                    state = false;
                    cunterGift = 0;
                }
                if (randomNumber % 5 == 3) {
                    state = true;
                }
                if (randomNumber % 5 == 4) {
                    state = true;
                }
            }


            int random_monster = (int) Math.round(Math.random() * (holes.size() - 1));

            if (state) {
                ((ImageView) findViewById(R.id.monster)).setImageResource(R.drawable.monster);

            } else {
                ((ImageView) findViewById(R.id.monster)).setImageResource(R.drawable.monster2);
            }


            findViewById(R.id.monster).setX(holes.get(random_monster).getX());
            findViewById(R.id.monster).setY(holes.get(random_monster).getY());

            // animate
            anim = new AnimatorSet();
            anim.playTogether(ObjectAnimator.ofFloat(findViewById(R.id.monster), "scaleX", 1f),
                    ObjectAnimator.ofFloat(findViewById(R.id.monster), "scaleY", 1f));
            anim.setDuration(100);
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // sound
                    if (!sp.getBoolean("mute", false) && isForeground)
                        sndpool.play(snd_move, 0.2f, 0.2f, 0, 0, 1);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    findViewById(R.id.monster).setEnabled(true);
                    h.postDelayed(hide_monster, show_time);
                }
            });
            anim.start();
        }
    };

    // hide_monster
    Runnable hide_monster = new Runnable() {
        @Override
        public void run() {
            // animate
            anim = new AnimatorSet();
            anim.playTogether(ObjectAnimator.ofFloat(findViewById(R.id.monster), "scaleX", 0f),
                    ObjectAnimator.ofFloat(findViewById(R.id.monster), "scaleY", 0f));
            anim.setDuration(250);
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    findViewById(R.id.monster).setEnabled(false);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    h.postDelayed(show_monster, 1 + Math.round(Math.random() * 500));//
                }
            });
            anim.start();
        }
    };

    // TIMER
    Runnable TIMER = new Runnable() {
        @Override
        public void run() {
            t--;
            ((TextView) findViewById(R.id.txt_time)).setText(getString(R.string.time) + " " + t);

            // complete
            if (t == 0) {
                // animation
                if (anim != null) {
                    anim.end();
                    anim.removeAllListeners();
                    anim.cancel();
                }
                h.removeCallbacks(show_monster);
                h.removeCallbacks(hide_monster);

                // sound
                if (!sp.getBoolean("mute", false) && isForeground)
                    sndpool.play(snd_info, 1f, 1f, 0, 0, 1);

                findViewById(R.id.mess).setVisibility(View.VISIBLE);
                findViewById(R.id.monster).setEnabled(false);
                h.postDelayed(STOP, 3000);
                return;
            }

            h.postDelayed(TIMER, 1000);
        }
    };

    // STOP
    Runnable STOP = new Runnable() {
        @Override
        public void run() {
            // show result

            current_section = R.id.result;
            findViewById(R.id.main).setVisibility(View.GONE);
            findViewById(R.id.game).setVisibility(View.GONE);
            findViewById(R.id.result).setVisibility(View.VISIBLE);
            findViewById(current_section).setVisibility(View.VISIBLE);
            if (Adivery.isLoaded(getString(R.string.interstitial))) {
                Adivery.showAd(getString(R.string.interstitial));
            }
            // save score
            if (score > sp.getInt("score", 0)) {
                ed.putInt("score", score);
                ed.commit();
            }

            // show score
            ((TextView) findViewById(R.id.txt_result)).setText(getString(R.string.score) + " " + score);
            ((TextView) findViewById(R.id.txt_high_result)).setText(getString(R.string.high_score) + " " + sp.getInt("score", 0));

            // sound
            if (!sp.getBoolean("mute", false) && isForeground)
                sndpool.play(snd_result, 1f, 1f, 0, 0, 1);

            // save score to leaderboard
            ///if (getApiClient().isConnected()) {
            //	Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_id), sp.getInt("score", 0));
            //}

            // AdMob Interstitial
            add_admob_interstitial();
        }
    };

    // onClick
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
            case R.id.btn_start2:
                START();
                if (Adivery.isLoaded(getString(R.string.interstitial))) {
                    Adivery.showAd(getString(R.string.interstitial));
                }
                break;
            case R.id.btn_home:
                show_section(R.id.main);
                if (Adivery.isLoaded(getString(R.string.interstitial))) {
                    Adivery.showAd(getString(R.string.interstitial));
                }
                break;

            case R.id.btn_sound:
                if (sp.getBoolean("mute", false)) {
                    ed.putBoolean("mute", false);
                    mp.setVolume(0.2f, 0.2f);
                    ((ImageView) findViewById(R.id.btn_sound)).setImageResource(R.drawable.btn_sound_on);

                } else {
                    ed.putBoolean("mute", true);
                    mp.setVolume(0, 0);
                    ((ImageView) findViewById(R.id.btn_sound)).setImageResource(R.drawable.btn_sound_off);

                }
                ed.commit();
                break;


        }
    }

    @Override
    public void onBackPressed() {
        switch (current_section) {
            case R.id.main:
                super.onBackPressed();
                break;
            case R.id.result:
                show_section(R.id.main);
                break;
            case R.id.game:
                if (Adivery.isLoaded(getString(R.string.interstitial))) {
                    Adivery.showAd(getString(R.string.interstitial));
                }
                show_section(R.id.main);
                h.removeCallbacks(STOP);
                h.removeCallbacks(TIMER);
                h.removeCallbacks(show_monster);
                h.removeCallbacks(hide_monster);

                // animation
                if (anim != null) {
                    anim.removeAllListeners();
                    anim.cancel();
                }
                break;
        }
    }

    // show_section
    void show_section(int section) {
        current_section = section;
        findViewById(R.id.main).setVisibility(View.GONE);
        findViewById(R.id.game).setVisibility(View.GONE);
        findViewById(R.id.result).setVisibility(View.GONE);
        findViewById(current_section).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        h.removeCallbacks(STOP);
        h.removeCallbacks(TIMER);
        h.removeCallbacks(show_monster);
        h.removeCallbacks(hide_monster);
        mp.release();
        sndpool.release();

        if (anim != null) {
            anim.removeAllListeners();
            anim.cancel();
        }


        super.onDestroy();
    }

    @Override
    protected void onPause() {
        isForeground = false;
        mp.setVolume(0, 0);
        super.onPause();
    }

    @Override
    protected void onResume() {


        super.onResume();
        isForeground = true;

        if (!sp.getBoolean("mute", false) && isForeground)
            mp.setVolume(0.2f, 0.2f);
    }

    // DpToPx
    float DpToPx(float dp) {
        return (dp * Math.max(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels) / 540f);
    }

    // hide_navigation_bar
    @TargetApi(Build.VERSION_CODES.KITKAT)
    void hide_navigation_bar() {
        // fullscreen mode
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hide_navigation_bar();
        }
    }

    //@Override
    //public void onSignInSucceeded() {
        /*((Button) findViewById(R.id.btn_sign)).setText(getString(R.string.btn_sign_out));

		// save score to leaderboard
		if (show_leaderboard) {
			if (sp.contains("score"))
				Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_id), sp.getInt("score", 0));

			// show leaderboard
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(), getString(R.string.leaderboard_id)),
					9999);
		}

		// get score from leaderboard
		Games.Leaderboards.loadCurrentPlayerLeaderboardScore(getApiClient(), getString(R.string.leaderboard_id),
				LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
				new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
					@Override
					public void onResult(final Leaderboards.LoadPlayerScoreResult scoreResult) {
						if (scoreResult != null && scoreResult.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK
								&& scoreResult.getScore() != null) {
							// save score localy
							if ((int) scoreResult.getScore().getRawScore() > sp.getInt("score", 0)) {
								ed.putInt("score", (int) scoreResult.getScore().getRawScore());
								ed.commit();
							}
						}
					}
				});

		show_leaderboard = false;*/
//	}

    //@Override
    //public void onSignInFailed() {
    //	((Button) findViewById(R.id.btn_sign)).setText(getString(R.string.btn_sign_in));
    //	show_leaderboard = false;
    //}

    // add_admob_smart
    void add_admob_smart() {

    }


    // add_admob_interstitial
    void add_admob_interstitial() {

    }


 public void rate(View view) {

        if (getString(R.string.market).equals("CafeBazar")) {
            Uri uri = Uri.parse(getString(R.string.cafe_link)+ getPackageName());
            Intent goMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(goMarket);
        }else {
            try {
                String url = getResources().getString(R.string.comment_link) + Apps.context.getPackageName();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }catch (Exception e){
                Uri uri = Uri.parse(getResources().getString(R.string.myket_link) + Apps.context.getPackageName());
                Intent goMarket = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(goMarket);
            }
        }
    }
}