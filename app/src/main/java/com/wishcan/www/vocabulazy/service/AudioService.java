package com.wishcan.www.vocabulazy.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.wishcan.www.vocabulazy.VLApplication;
import com.wishcan.www.vocabulazy.storage.databaseObjects.Option;
import com.wishcan.www.vocabulazy.storage.Preferences;
import com.wishcan.www.vocabulazy.storage.databaseObjects.Vocabulary;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/**
 * Created by allencheng07 on 2016/1/26.
 */
public class AudioService extends IntentService
        implements AudioPlayer.OnEventListener, WCTextToSpeech.OnUtteranceStatusListener, AudioManager.OnAudioFocusChangeListener {

    /**
     * tag for debugging
     */
    public static final String TAG = AudioService.class.getSimpleName();

    /**
     * strings for intent action identification
     */
    public static final String ACTION_REQUEST_AUDIO_FOCUS = "request-audio-focus";
    public static final String ACTION_INIT_TTS_ENGINE = "init-tts-engine";
    public static final String ACTION_START_SERVICE = "start-service";
    public static final String ACTION_STOP_SERVICE = "stop-service";
    public static final String ACTION_SET_CONTENT = "set-content";
    public static final String ACTION_START_PLAYING = "start-playing";
    public static final String ACTION_PAUSE_PLAYING = "pause-playing";
    public static final String ACTION_RESUME_PLAYING = "resume-playing";
    public static final String ACTION_STOP_PLAYING = "stop-playing";
    public static final String ACTION_PLAY_BUTTON_CLICKED = "play-button-clicked";
    public static final String ACTION_OPTION_SETTING_CHANGED = "option-setting-changed";
    public static final String ACTION_NEW_LIST = "new-list";
    public static final String ACTION_PLAYERVIEW_SCROLLING = "playerview-scrolling";
    public static final String ACTION_NEW_SENTENCE_FOCUSED = "new-sentence-focused";
    public static final String ACTION_SET_LANGUAGE = "set-language";

    /**
     * keys for bundle identification
     */
    public static final String KEY_PLAYER_CONTENT = "player-content";
    public static final String KEY_START_ITEM_INDEX = "start-item-index";
    public static final String KEY_START_SENTENCE_INDEX = "start-sentence-index";
    public static final String KEY_PLAYING_FIELD = "playing-field";
    public static final String KEY_OPTION_SETTING = "option-setting";
    public static final String KEY_STOP_AT_ITEM_INDEX = "stop-at-item-index";
    public static final String KEY_NEW_CONTENT = "new-content";
    public static final String KEY_NEW_SENTENCE_INDEX = "new-sentence-index";
    public static final String KEY_LANGUAGE = "language";

    /**
     * strings for identifying the status of the player
     */
    public static final String STATUS_IDLE = "status-idle";
    public static final String STATUS_PLAYING = "status-playing";
    public static final String STATUS_PAUSE = "status-pause";
    public static final String STATUS_PAUSE_FOR_FOCUS_LOSS = "status-pause-for-focus-loss";
    public static final String STATUS_STOPPED = "status-stopped";
    public static final String STATUS_SCROLLING = "statuc-scrolling";

    /**
     * strings for identifying what field of the vocabulary is being played
     */
    public static final String PLAYING_DEFAULT = "playing-default";
    public static final String PLAYING_SPELL = "playing-spell";
    public static final String PLAYING_TRANSLATION = "playing-translation";
    public static final String PLAYING_EnSENTENCE = "playing-ensentence";
    public static final String PLAYING_CnSENTENCE = "playing-cnsentence";

    /**
     * The preferences of the entire application.
     */
    private Preferences wPreference;

    private AudioPlayer wAudioPlayer;
    private AudioManager wAudioManager;
    private ServiceBroadcaster wServiceBroadcaster;
    private ArrayList<Vocabulary> wVoabularies;
    private Option wOptionSetting;
    private WCTextToSpeech wcTextToSpeech;
    private boolean isAudioFocused;

    private int wCurrentItemIndex = -1;
    private int wCurrentItemAmount = 0;
    private int wCurrentSentenceIndex = -1;
    private int wCurrentSentenceAmount = 0;
    private String wStatus = STATUS_IDLE;
    private String wPlaying = PLAYING_DEFAULT;

    private boolean wIsRandom = false;
    private int wListLoop;
    private boolean wEnSentenceEnabled = true;
    private boolean wCnSentenceEnabled = false;

    private int wStopPeriod;
    private int wItemLoop;
    private int wSpeed;
    private int wPlayTime;

    private int spellCountDown;
    private int itemloopCountDown;
    private int listloopCountDown;

    private Locale wSpellLanguage;
    private Locale wTranslationLanguage;

    public AudioService() {
        super(null);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AudioService(String name) {
        super(name);
    }

    /**
     * the method is required by the IntentService class.
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {

    }

    /**
     * the purpose of overriding the method startcommand(intent, flags, startid) is to return START_STICKY
     * rather than default return value.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return START_STICKY: representing that the service object will last after each command has
     *                       been executed.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = (intent != null) ? intent.getAction() : "";
//        Log.d(TAG, action);

        /**
         * switch-case block for deciding the corresponding tasks for each action
         */
        switch (action) {

            case ACTION_REQUEST_AUDIO_FOCUS:
                getAudioFocus();
                break;

            case ACTION_INIT_TTS_ENGINE:
                wcTextToSpeech.initializeTTSEngine(getApplicationContext());
                break;

            case ACTION_START_SERVICE:
                VLApplication vlApplication = (VLApplication) getApplication();
                wPreference = vlApplication.getPreferences();

                wAudioPlayer = new AudioPlayer(this);
                wServiceBroadcaster = new ServiceBroadcaster(this);
                wcTextToSpeech = new WCTextToSpeech(getApplicationContext(), this);
                break;

            case ACTION_STOP_SERVICE:
                if (wAudioPlayer != null)
                    wAudioPlayer.releasePlayer();
                if (wcTextToSpeech != null)
                    wcTextToSpeech.shutdown();
                abandonAudioFocus();
                break;

            case ACTION_SET_LANGUAGE:
                int bookIndex = intent.getIntExtra(KEY_LANGUAGE, -1);
                wSpellLanguage = (bookIndex == 3) ? Locale.JAPAN : Locale.ENGLISH;
                wTranslationLanguage = Locale.TAIWAN;
                break;

            case ACTION_SET_CONTENT:
                Log.d(TAG, "set content");
                wVoabularies = wPreference.wCurrentContentInPlayer;
                Log.d(TAG, wVoabularies.size() + " vocabularies");
//                wVoabularies = intent.getParcelableArrayListExtra(KEY_PLAYER_CONTENT);
                wOptionSetting = wPreference.wCurrentOptionSettings;
//                wOptionSetting = intent.getParcelableExtra(KEY_OPTION_SETTING);
                wCurrentItemAmount = wVoabularies.size();
                updateOptionSetting(wOptionSetting);
//                Log.d(TAG, voabularies.size() + " vocabulary objects received from controller");
//                Log.d(TAG, "content set");
                break;

            case ACTION_START_PLAYING:

                int itemIndex = intent.getIntExtra(KEY_START_ITEM_INDEX, -1);
                int sentenceIndex = intent.getIntExtra(KEY_START_SENTENCE_INDEX, -1);
                String playing = intent.getStringExtra(KEY_PLAYING_FIELD);

                spellCountDown = 3;
                wCurrentItemIndex = itemIndex;
                wCurrentSentenceIndex = sentenceIndex;
                wStatus = STATUS_PLAYING;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                wPlaying = playing;
                wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                startPlayingItemAt(itemIndex, sentenceIndex, checkIsItemFinishing());
                break;

            case ACTION_PAUSE_PLAYING:
                wStatus = STATUS_PAUSE;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                wcTextToSpeech.pause();
//                abandonAudioFocus();
                break;

            case ACTION_RESUME_PLAYING:
//                wStatus = STATUS_PLAYING;
//                startPlayingItemAt(wCurrentItemIndex, wCurrentSentenceIndex, checkIsItemFinishing());
                break;

            case ACTION_STOP_PLAYING:
                wStatus = STATUS_STOPPED;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                wcTextToSpeech.stop();
                abandonAudioFocus();
                wCurrentItemIndex = intent.getIntExtra(KEY_STOP_AT_ITEM_INDEX, -1);

                break;

            case ACTION_PLAY_BUTTON_CLICKED:
                if (wStatus.equals(STATUS_PLAYING)) {
                    wStatus = STATUS_PAUSE;
                    wServiceBroadcaster.updatePlayerStatus(wStatus);
                    wcTextToSpeech.pause();
//                    abandonAudioFocus();
                } else {
                    if (!isAudioFocused) getAudioFocus();
                    wStatus = STATUS_PLAYING;
                    wServiceBroadcaster.updatePlayerStatus(wStatus);
                    startPlayingItemAt(wCurrentItemIndex, wCurrentSentenceIndex, checkIsItemFinishing());
                }
                break;

            case ACTION_OPTION_SETTING_CHANGED:
                wOptionSetting = wPreference.wCurrentOptionSettings;
//                wOptionSetting = intent.getParcelableExtra(KEY_OPTION_SETTING);
//                Log.d(TAG, "update option setting: random: " + wOptionSetting.mIsRandom + ", sentence: " + wOptionSetting.mSentence);
                updateOptionSetting(wOptionSetting);
                break;

            case ACTION_NEW_LIST:
                wVoabularies = wPreference.wCurrentContentInPlayer;
//                wVoabularies = intent.getParcelableArrayListExtra(KEY_NEW_CONTENT);
                wCurrentItemAmount = wVoabularies.size();

                spellCountDown = 3;
                listloopCountDown = wListLoop;
                wCurrentItemIndex = 0;
                wCurrentSentenceIndex = 0;
                wStatus = STATUS_PLAYING;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                wPlaying = PLAYING_SPELL;
                wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                startPlayingItemAt(wCurrentItemIndex, wCurrentSentenceIndex, checkIsItemFinishing());
                break;

            case ACTION_PLAYERVIEW_SCROLLING:
                if (!wStatus.equals(STATUS_SCROLLING)) {
//                    Log.d(TAG, "scrollllllllllllllllllllll");
                    wStatus = STATUS_SCROLLING;
                    wServiceBroadcaster.updatePlayerStatus(wStatus);
                    wcTextToSpeech.stop();
                }
                break;

            case ACTION_NEW_SENTENCE_FOCUSED:
                wCurrentSentenceIndex = intent.getIntExtra(KEY_NEW_SENTENCE_INDEX, -1);
                wPlaying = PLAYING_EnSENTENCE;
                wStatus = STATUS_PLAYING;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                startPlayingItemAt(wCurrentItemIndex, wCurrentSentenceIndex, checkIsItemFinishing());
                break;

            default:
                Log.d(TAG, "undefined case in onHandleIntent");
                return START_REDELIVER_INTENT;
//                break;
        }

        return START_STICKY;
    }

    void getAudioFocus() {
        wAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int result = wAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        Log.d(TAG, "get audio focus, result " + result);
        isAudioFocused = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    void abandonAudioFocus() {
        Log.d(TAG, "abandon audio focus");
        if (wAudioManager != null)
            wAudioManager.abandonAudioFocus(this);
    }

    void startPlayingItemAt(int itemIndex, int sentenceIndex, boolean isItemFinished) {

        String string;

        switch (wPlaying) {

            case PLAYING_SPELL:
                string = wVoabularies.get(itemIndex).getSpell();
                wcTextToSpeech.setLanguage(wSpellLanguage);
                wcTextToSpeech.speak(string, isItemFinished);
                break;

            case PLAYING_TRANSLATION:
//                Log.d(TAG, "YOLO2");
//                string = wVoabularies.get(itemIndex).getTranslationInOneString();
                string = wVoabularies.get(itemIndex).getTranslation().get(0);
                wcTextToSpeech.setLanguage(wTranslationLanguage);
                wcTextToSpeech.speak(string, isItemFinished);
                break;

            case PLAYING_EnSENTENCE:
                string = wVoabularies.get(itemIndex).getEn_sentence().get(sentenceIndex);
                wcTextToSpeech.setLanguage(wSpellLanguage);
                wcTextToSpeech.speak(string, isItemFinished);
                break;

            case PLAYING_CnSENTENCE:
                string = wVoabularies.get(itemIndex).getCn_sentence().get(sentenceIndex);
                wcTextToSpeech.setLanguage(wTranslationLanguage);
                wcTextToSpeech.speak(string, isItemFinished);
                break;

            default:
                Log.d(TAG, "unexpected case in startPlayingItemAt: " + itemIndex + ", " +
                        "playing " + wPlaying + ", sentence # " + sentenceIndex);
                break;
        }

        updateIndices(itemIndex, sentenceIndex);
    }

    int pickNextItem(int currentIndex) {
        int nextItem = -1;
        if (wIsRandom) {
            Random random = new Random(System.currentTimeMillis());
            do {
                nextItem = random.nextInt(wCurrentItemAmount);
            } while (currentIndex == nextItem);
            return nextItem;
        } else {
            currentIndex++;
//            Log.d(TAG, "pickNextItem: " + currentIndex + ", itemAmount: " + wCurrentItemAmount);
            if (currentIndex < wCurrentItemAmount) return currentIndex;
        }
        return nextItem;
    }

    void updateOptionSetting(Option optionSetting) {
        wIsRandom = optionSetting.isRandom();
        wEnSentenceEnabled = optionSetting.isSentence();
        wCnSentenceEnabled = optionSetting.isSentence();
        wPlayTime = optionSetting.getPlaytime();

        wListLoop = optionSetting.getListloop();
        wItemLoop = optionSetting.getItemloop();
        itemloopCountDown = wItemLoop;
        listloopCountDown = wListLoop;

        wcTextToSpeech.setStopPeriod(optionSetting.getStopperiod());
        wcTextToSpeech.setSpeed(optionSetting.getSpeed());
    }

    boolean checkIsItemFinishing() {
        if (wPlaying.equals(PLAYING_CnSENTENCE) && wCurrentSentenceIndex == wCurrentSentenceAmount-1) return true;
        if (wPlaying.equals(PLAYING_TRANSLATION) && !wOptionSetting.isSentence()) return true;
        return false;
    }

    void updateIndices(int itemIndex, int sentenceIndex) {
        wPreference.wItemIndex = itemIndex;
        wPreference.wSentenceIndex = sentenceIndex;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory");
        if (wcTextToSpeech != null)
            wcTextToSpeech.shutdown();
        abandonAudioFocus();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        Log.d(TAG, "onCompletion");
    }

    @Override
    public void onUtteranceCompleted() {

        /**
         * if the status is SCOLLING, then do nothing.
         */
        if (wStatus.equals(STATUS_SCROLLING)) return;

        /**
         * if the status is STOPPED, which means the player is forced stopped and has jumped out of
         * the looping mechanism, start playing the desired item to enter the playback looping again.
         */
        if (wStatus.equals(STATUS_STOPPED)) {
//            Log.d(TAG, "status is stopped");
            if (wCurrentItemIndex >= 0) {
                wCurrentSentenceIndex = -1;
                wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                wPlaying = PLAYING_SPELL;
                wStatus = STATUS_PLAYING;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                startPlayingItemAt(wCurrentItemIndex, wCurrentSentenceIndex, checkIsItemFinishing());
            } else {
                wCurrentItemIndex = 0;
                wCurrentSentenceAmount = wVoabularies.get(0).getEn_sentence().size();
//                Log.d(TAG, wVoabularies.get(0).getSpell());
                wPlaying = PLAYING_SPELL;
                wStatus = STATUS_PLAYING;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                startPlayingItemAt(0, -1, checkIsItemFinishing());
            }
            return;
        }

        /**
         * if the status is playing, do nothing, preventing the playback from any interruption
         */
        if (!wStatus.equals(STATUS_PLAYING)) return;

        /**
         * switch-case block for deciding the next item or field to be played.
         */
        switch (wPlaying) {
            /**
             * if the utterance just played was SPELL.
             */
            case PLAYING_SPELL:
                spellCountDown--;
                wPlaying = (spellCountDown > 0 ? PLAYING_SPELL : PLAYING_TRANSLATION);
//                wPlaying = PLAYING_TRANSLATION;
                break;

            /**
             * if the utterance just played was TRANSLATION
             */
            case PLAYING_TRANSLATION:
                spellCountDown = 3;
                if (wEnSentenceEnabled) {
                    wCurrentSentenceIndex = 0;
                    wPlaying = PLAYING_EnSENTENCE;
                    wServiceBroadcaster.onShowDetail();
                } else if (wCnSentenceEnabled) {
                    wCurrentSentenceIndex = 0;
                    wPlaying = PLAYING_CnSENTENCE;
                    wServiceBroadcaster.onShowDetail();
                } else {

                    itemloopCountDown--;
                    if (itemloopCountDown > 0) {
                        wPlaying = PLAYING_SPELL;
                        break;
                    }

                    itemloopCountDown = wItemLoop;
                    wCurrentItemIndex = pickNextItem(wCurrentItemIndex);
                    wCurrentSentenceIndex = -1;
                    if (wCurrentItemIndex >= 0) {
                        wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                        wPlaying = PLAYING_SPELL;
                        wServiceBroadcaster.onHideDetail();
                        wServiceBroadcaster.onItemComplete(wCurrentItemIndex);
                    } else {

                        listloopCountDown--;
                        if (listloopCountDown > 0) {
                            wPlaying = PLAYING_SPELL;
                            wCurrentItemIndex = 0;
                            wCurrentSentenceIndex = 0;
                            wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                            wServiceBroadcaster.onHideDetail();
                            wServiceBroadcaster.onItemComplete(wCurrentItemIndex);
//                            return;
                            break;
                        }

//                        wCurrentItemIndex = 0;
//                        wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_Sentence().size();
//                        wPlaying = PLAYING_SPELL;
                        listloopCountDown = wListLoop;
                        wServiceBroadcaster.onListComplete();
                        return;
                    }

                }
                break;

            /**
             * if the utterance just played was EnSentence
             */
            case PLAYING_EnSENTENCE:
                if (wCnSentenceEnabled) {
                    wPlaying = PLAYING_CnSENTENCE;
                    break;
                }

                wCurrentSentenceIndex++;
                if (wCurrentSentenceIndex < wCurrentSentenceAmount) {
                    wPlaying = PLAYING_EnSENTENCE;
                    wServiceBroadcaster.onPlaySentence(wCurrentSentenceIndex);
                } else if (wCurrentSentenceIndex == wCurrentSentenceAmount) {

                    itemloopCountDown--;
                    if (itemloopCountDown > 0) {
                        wPlaying = PLAYING_SPELL;
                        wCurrentSentenceIndex = 0;
                        break;
                    }

                    itemloopCountDown = wItemLoop;
                    wCurrentItemIndex = pickNextItem(wCurrentItemIndex);
                    wCurrentSentenceIndex = -1;
                    if (wCurrentItemIndex >= 0) {
                        wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                        wPlaying = PLAYING_SPELL;
                        wServiceBroadcaster.onHideDetail();
                        wServiceBroadcaster.onItemComplete(wCurrentItemIndex);
                    } else {

                        listloopCountDown--;
                        if (listloopCountDown > 0) {
                            wPlaying = PLAYING_SPELL;
                            wCurrentItemIndex = 0;
                            wCurrentSentenceIndex = 0;
                            wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                            wServiceBroadcaster.onHideDetail();
                            wServiceBroadcaster.onItemComplete(wCurrentItemIndex);
//                            return;
                            break;
                        }

                        // load new vocabularies
                        listloopCountDown = wListLoop;
                        wServiceBroadcaster.onListComplete();
                        return;
                    }
                }

                break;

            /**
             * if the utterance just played was CnSENTENCE
             */
            case PLAYING_CnSENTENCE:
                wCurrentSentenceIndex++;
                if (wCurrentSentenceIndex < wCurrentSentenceAmount) {
                    if (wEnSentenceEnabled) {
                        wPlaying = PLAYING_EnSENTENCE;
                    } else if (wCnSentenceEnabled) {
                        wPlaying = PLAYING_CnSENTENCE;
                    }
                    wServiceBroadcaster.onPlaySentence(wCurrentSentenceIndex);
                } else if (wCurrentSentenceIndex == wCurrentSentenceAmount) {

                    itemloopCountDown--;
                    if (itemloopCountDown > 0) {
                        wPlaying = PLAYING_SPELL;
                        wCurrentSentenceIndex = 0;
                        break;
                    }

                    itemloopCountDown = wItemLoop;
                    wCurrentItemIndex = pickNextItem(wCurrentItemIndex);
                    wCurrentSentenceIndex = -1;
                    if (wCurrentItemIndex >= 0) {
                        wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                        wPlaying = PLAYING_SPELL;
                        wServiceBroadcaster.onHideDetail();
                        wServiceBroadcaster.onItemComplete(wCurrentItemIndex);
                    } else {

                        listloopCountDown--;
                        if (listloopCountDown > 0) {
                            wPlaying = PLAYING_SPELL;
                            wCurrentItemIndex = 0;
                            wCurrentSentenceIndex = 0;
                            wCurrentSentenceAmount = wVoabularies.get(wCurrentItemIndex).getEn_sentence().size();
                            wServiceBroadcaster.onHideDetail();
                            wServiceBroadcaster.onItemComplete(wCurrentItemIndex);
//                            return;
                            break;
                        }

                        // load new vocabularies
                        listloopCountDown = wListLoop;
                        wServiceBroadcaster.onListComplete();
                        return;
                    }
                }

                break;

            /**
             * default case, should be called anyway
             */
            default:
                Log.d(TAG, "unexpected case happened in onUtteranceCompleted");
                break;
        }
//        Log.d(TAG, "YOLO");
        startPlayingItemAt(wCurrentItemIndex, wCurrentSentenceIndex, checkIsItemFinishing());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "onAudioFocusChange " + focusChange);

        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // resume playing
            if (wStatus.equals(STATUS_PAUSE_FOR_FOCUS_LOSS)) {
                wStatus = STATUS_PLAYING;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                startPlayingItemAt(wCurrentItemIndex, wCurrentSentenceIndex, checkIsItemFinishing());
            }
        }

        if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            isAudioFocused = false;
            // pause playing
            if (wStatus.equals(STATUS_PLAYING)) {
                wStatus = STATUS_PAUSE_FOR_FOCUS_LOSS;
                wServiceBroadcaster.updatePlayerStatus(wStatus);
                wcTextToSpeech.pause();
            }
        }

    }
}