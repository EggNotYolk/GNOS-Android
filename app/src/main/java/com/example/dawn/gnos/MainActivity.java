package com.example.dawn.gnos;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.gnos.data.Const;
import com.gnos.model.IAlertDialogButtonListener;
import com.gnos.model.IWordButtonClickListener;
import com.gnos.model.Song;
import com.gnos.model.WordButton;
import com.gnos.myui.MyGridView;
import com.gnos.myutil.MyPlayer;
import com.gnos.myutil.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements IWordButtonClickListener {

    /** 答案状态--正确 */
    public final static int STATUS_ANSWER_RIGHT = 1;

    /** 答案状态--错误 */
    public final static int STATUS_ANSWER_WRONG = 2;

    /** 答案状态--不完整 */
    public final static int STATUS_ANSWER_LACK = 3;

    // 文字闪烁次数
    public final static int SPARK_TIMES = 6;

    public final static int ID_DIALOG_HINT_ANSWER = 1;

    // 唱片旋转动画
    private Animation mPanAnim;
    private LinearInterpolator mPanLin;

    // 拨杆动画
    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;

    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;

    // Play按键事件
    private ImageButton mBtnPlayStart;

    // 过关界面
    private View mPassView;

    // 唱片控件
    private ImageView mViewPan;
    // 拨杆控件
    private ImageView mViewPanBar;

    // 当前关卡的索引（图片上数字）
    private TextView mCurrentStagePassView;

    private TextView mCurrentStageView;

    // 当前歌曲名称
    private TextView mCurrentSongNamePassView;

    // 当前动画是否正在运行
    private boolean mIsRunning = false;

    // 文字框容器
    private ArrayList<WordButton> mAllWords;

    private ArrayList<WordButton> mBtnSelectWords;

    private MyGridView mMyGridView;

    // 已选择文字框UI容器
    private LinearLayout mViewWordsContainer;

    // 当前歌曲
    private Song mCurrentSong;

    // 当前关卡的索引
    private int mCurrentStageIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        mViewPan = (ImageView)findViewById(R.id.game_disc);
        mViewPanBar = (ImageView)findViewById(R.id.index_pin);

        mMyGridView = (MyGridView)findViewById(R.id.grid_view);

        // 注册监听
        mMyGridView.registerOnWordButtonClick(this);

        mViewWordsContainer = (LinearLayout)findViewById(R.id.word_select_container);

        // 初始化唱片动画
        mPanAnim = AnimationUtils.loadAnimation(this,R.anim.pan_rotate);
        mPanLin = new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);// 设置动画为匀速速率
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 开启拨杆退出动画
                mViewPanBar.startAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // 初始化拨杆开启动画
        mBarInAnim = AnimationUtils.loadAnimation(this,R.anim.barin_rotate);
        mBarInLin = new LinearInterpolator();
        mBarInAnim.setInterpolator(mBarInLin);// 设置动画为匀速速率
        mBarInAnim.setFillAfter(true);// 使拨杆动画结束时，停留在最后一帧
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 开始唱片动画
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // 初始化拨杆退出动画
        mBarOutAnim = AnimationUtils.loadAnimation(this,R.anim.barout_rotate);
        mBarOutLin = new LinearInterpolator();
        mBarOutAnim.setInterpolator(mBarOutLin);// 设置动画为匀速速率
        mBarOutAnim.setFillAfter(true);// 使拨杆动画结束时，停留在最后一帧
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 整套动画播放完毕，使播放按钮可见
                mIsRunning = false;
                mBtnPlayStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // 初始化并监听播放按钮
        mBtnPlayStart = (ImageButton)findViewById(R.id.play_button_icon);
        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayButton();
            }
        });

        // 初始化游戏数据
        initCurrentStageData();

        // 处理提示按键事件
        handleHintAnswer();

    }

    @Override
    public void onWordButtonClick(WordButton wordButton) {
        //Toast.makeText(this, wordButton.getIndex() + " ", Toast.LENGTH_SHORT).show();
        setSelectWord(wordButton);

        // 获得答案状态
        int checkResult = checkTheAnswer();

        // 检查答案
        if(checkResult == STATUS_ANSWER_RIGHT) {
            // 过关并获得奖励
            handlePassEvent();
        }
        else if(checkResult == STATUS_ANSWER_WRONG) {
            // 闪烁文字并提示用户
            sparkTheWords();
        }
        else if(checkResult == STATUS_ANSWER_LACK) {
            // 设置文字颜色为白色（Normal状态）
            for (int i = 0; i < mBtnSelectWords.size(); i++) {
                mBtnSelectWords.get(i).getViewButton().setTextColor(Color.WHITE);
            }
        }
    }

    /**
     * 处理过关界面及事件
     */
    private void handlePassEvent() {
        // 显示过关界面
        mPassView = (LinearLayout)this.findViewById(R.id.pass_view);
        mPassView.setVisibility(View.VISIBLE);

        // 停止未完成的动画
        mViewPan.clearAnimation();

        // 停止未播放完的歌曲
        MyPlayer.stopTheSong(MainActivity.this);

        // 当前关卡的索引
        mCurrentStagePassView = (TextView)findViewById(R.id.text_current_stage_pass);
        if(mCurrentStagePassView != null) {
            mCurrentStagePassView.setText((mCurrentStageIndex + 1) + "");
        }

        // 显示歌曲名称
        mCurrentSongNamePassView = (TextView)findViewById(R.id.text_current_stage_song_name);
        if(mCurrentSongNamePassView != null){
            mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
        }

        // 处理下一关按键
        ImageButton btnPass = (ImageButton)findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(judgeAppPassed()) {
                    // 进入通关界面
                    Util.startActivity(MainActivity.this, AllPassView.class);
                }
                else {
                    // 开始新关卡，隐藏过关界面
                    mPassView.setVisibility(View.GONE);

                    // 加载关卡数据
                    initCurrentStageData();
                }
            }
        });
    }

    /**
     * 判断是否通关
     *
     * @return
     */
    private boolean judgeAppPassed() {
        return (mCurrentStageIndex == Const.SONG_INFO.length - 1);
    }

    private void clearTheAnswer(WordButton wordButton) {
        // 设置已选框的可见性
        wordButton.getViewButton().setText("");
        wordButton.setWordString("");
        wordButton.setIsVisible(false);

        // 设置待选框的可见性
        setButtonVisible(mAllWords.get(wordButton.getIndex()), View.VISIBLE);
    }

    /**
     * 设置答案文字框
     *
     * @param wordButton
     */
    private void setSelectWord(WordButton wordButton) {

        for(int i = 0; i < mBtnSelectWords.size(); i++) {
            if(mBtnSelectWords.get(i).getWordString().length() == 0) {
                // 设置答案文字框内容及可见性
                mBtnSelectWords.get(i).getViewButton().setText(wordButton.getWordString());
                mBtnSelectWords.get(i).setIsVisible(true);
                mBtnSelectWords.get(i).setWordString(wordButton.getWordString());

                // 记录索引
                mBtnSelectWords.get(i).setIndex(wordButton.getIndex());

                // 设置待选框可见性
                setButtonVisible(wordButton, View.INVISIBLE);

                break;
            }
        }
    }

    /**
     * 设置待选文字框是否可见
     *
     * @param wordButton
     * @param visibility
     */
    private void setButtonVisible(WordButton wordButton, int visibility) {
        wordButton.getViewButton().setVisibility(visibility);
        wordButton.setIsVisible((visibility == View.VISIBLE) ? true : false);
    }


    /**
     * 处理圆盘中间的播放按钮：开始播放音乐
     */
    private void handlePlayButton(){
        if(mViewPanBar != null) {
            if(!mIsRunning){
                mIsRunning = true;

                // 开始拨杆进入动画，将播放按钮设置为不可见
                mViewPanBar.startAnimation(mBarInAnim);
                mBtnPlayStart.setVisibility(View.INVISIBLE);

                // 播放音乐
                MyPlayer.playSong(MainActivity.this,
                        mCurrentSong.getSongFileName());
            }
        }
    }

    @Override
    public void onPause() {
        mViewPan.clearAnimation();

        // 暂停音乐（实现切换到后台时暂停音乐）
        MyPlayer.stopTheSong(MainActivity.this);

        super.onPause();
    }

    /**
     * 读取当前关卡的歌曲信息
     *
     * @param StageIndex
     * @return
     */
    private Song loadStageSongInfo(int StageIndex) {
        Song song = new Song();

        String[] stage = Const.SONG_INFO[StageIndex];
        song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
        song.setSongName(stage[Const.INDEX_SONG_NAME]);

        return song;
    }


    /**
     *  加载当前关卡的数据
     */
    private void initCurrentStageData() {
        // 读取当前关卡的歌曲信息
        mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

        // 初始化答案框
        mBtnSelectWords = initWordSelect();

        LayoutParams params = new LayoutParams(140, 140);

        // 清空原来的答案
        mViewWordsContainer.removeAllViews();

        // 增加新的答案框
        for(int i = 0; i < mBtnSelectWords.size(); i++) {
            mViewWordsContainer.addView(
                    mBtnSelectWords.get(i).getViewButton(),
                    params);
        }

        // 显示当前关索引
        mCurrentStageView = (TextView)findViewById(R.id.text_current_stage);
        if(mCurrentStageView != null) {
            mCurrentStageView.setText((mCurrentStageIndex + 1) + "");
        }

        // 获得数据
        mAllWords = initAllWord();
        // 更新数据
        mMyGridView.updateData(mAllWords);
        // 进入新关卡自动播放音乐
        handlePlayButton();
    }

    /**
     * 初始化待选文字框
     *
     * @return
     */
    private ArrayList<WordButton> initAllWord() {
        ArrayList<WordButton> data = new ArrayList<WordButton>();

        // 获得所有待选文字
        String[] words = generateWords();

        for(int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
            WordButton button = new WordButton();

            button.setWordString(words[i]);

            data.add(button);
        }
        return data;
    }

    /**
     * 初始化已选择文字框
     *
     * @return
     */
    private ArrayList<WordButton> initWordSelect() {
        ArrayList<WordButton> data = new ArrayList<WordButton>();

        for(int i = 0; i < mCurrentSong.getNameLength(); i++){
            View view = Util.getView(MainActivity.this, R.layout.myui_gridview);

            final WordButton holder = new WordButton();

            holder.setViewButton((Button)view.findViewById(R.id.item_btn));
            holder.getViewButton().setTextColor(Color.WHITE);
            holder.getViewButton().setText("");
            holder.setIsVisible(false);

            holder.getViewButton().setBackgroundResource(R.drawable.game_wordblank);
            holder.getViewButton().setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    clearTheAnswer(holder);
                }
            });

            data.add(holder);
        }
        return data;
    }

    /**
     * 生成所有的待选文字(包括正确答案所包含文字)
     *
     * @return
     */
    private String[] generateWords() {
        Random random = new Random();

        String[] words = new String[MyGridView.COUNTS_WORDS];

        // 存入歌名
        for(int i = 0; i < mCurrentSong.getNameLength(); i++) {
            words[i] = mCurrentSong.getNameCharacters()[i] + "";
        }

        // 获取随机文字并存入数组
        for(int i = mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORDS; i++) {
            words[i] = getRandomChar() + "";
        }

        /* 打乱文字顺序：首先从所有元素中随机选取一个与第一个元素进行交换，
           然后再从剩下元素中随机选取一个与第二个元素交换，直到最后一个元素。*/
        for (int i = MyGridView.COUNTS_WORDS - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);

            String buf = words[index];
            words[index] = words[i];
            words[i] = buf;
        }

        return words;
    }


    /**
     * 生成随机汉字
     *
     * @return
     */
    private char getRandomChar() {
        String str = "";
        int highPos;
        int lowPos;

        Random random = new Random();

        /* 汉字高位字节 = 0xA0 (160) + 区号。16~55区为一级汉字。55-16=39。
           故，在一级汉字中随机选择汉字 */
        highPos = (176 + Math.abs(random.nextInt(39)));

        // 汉字低位字节 = 0xA0 (160) + 位号。共有 01~94 位。
        lowPos = (161 + Math.abs(random.nextInt(93)));

        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(highPos)).byteValue();
        b[1] = (Integer.valueOf(lowPos)).byteValue();

        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str.charAt(0);
    }

    /**
     * 检查答案
     *
     * @return
     */
    private int checkTheAnswer() {
        // 先检查长度
        for(int i = 0; i < mBtnSelectWords.size(); i++) {
            // 如果有空的，说明答案不完整
            if(mBtnSelectWords.get(i).getWordString().length() == 0) {
                return STATUS_ANSWER_LACK;
            }
        }

        StringBuffer sb = new StringBuffer();
        // 答案完整，继续检查正确性
        for(int i = 0; i < mBtnSelectWords.size(); i++) {
            sb.append(mBtnSelectWords.get(i).getWordString());
        }

        return (sb.toString().equals(mCurrentSong.getSongName())) ?
                STATUS_ANSWER_RIGHT : STATUS_ANSWER_WRONG;
    }

    /**
     * 文字闪烁
     */
    private void sparkTheWords() {
        // 定时器相关
        TimerTask task = new TimerTask() {
            boolean mChange = false;
            int mSparkTimes = 0;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(++mSparkTimes > SPARK_TIMES){
                            return;
                        }

                        // 执行闪烁逻辑：交替显示红色和白色文字
                        for (int i = 0; i < mBtnSelectWords.size(); i++) {
                            mBtnSelectWords.get(i).getViewButton().setTextColor(
                                    mChange ? Color.RED : Color.WHITE);
                        }

                        mChange = !mChange;
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1, 150);
    }

    /**
     * 处理提示按键事件
     */
    private void handleHintAnswer() {
        ImageButton button = (ImageButton)findViewById(R.id.btn_hint_answer);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //hintAnswer();
                showConfirmDialog(ID_DIALOG_HINT_ANSWER);
            }
        });

    }

    /**
     * 自动选择一个答案
     */
    private void hintAnswer() {
        boolean hintWord = false;

        // 根据当前的答案框条件选择对应的文字并填入
        for(int i = 0; i < mBtnSelectWords.size(); i++) {
            if(mBtnSelectWords.get(i).getWordString().length() == 0){
                onWordButtonClick(findIsAnswerWord(i));

                hintWord = true;
                break;
            }
        }

        // 答案框已满，没有位置放置正确答案
        if(!hintWord){
            // 闪烁文字提示用户
            sparkTheWords();
        }

    }


    /**
     * 找到一个答案文字
     *
     * @param index 当前需要填入答案框的索引
     *
     * @return
     */
    private WordButton findIsAnswerWord(int index) {
        WordButton buf = null;

        for(int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
            buf = mAllWords.get(i);

            if(buf.getWordString().equals("" + mCurrentSong.getNameCharacters()[index])){
                return buf;
            }
        }

        return null;
    }

    // 自定义AlertDialog事件响应
    // 答案提示
    private IAlertDialogButtonListener mBtnOkHintAnswerListener =
            new IAlertDialogButtonListener() {

                @Override
                public void onClick() {
                    // 执行事件
                    hintAnswer();
                }
            };

    /**
     * 显示对话框
     *
     * @param id
     */
    private void showConfirmDialog(int id) {
        switch (id) {
            case ID_DIALOG_HINT_ANSWER:
                Util.showDialog(MainActivity.this,
                        "确定获得一个文字提示？",
                        mBtnOkHintAnswerListener);
                break;
        }

    }
}
