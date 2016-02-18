package com.wishcan.www.vocabulazy.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by allencheng07 on 2016/2/1.
 */
public class ServiceBroadcaster {

    /**
     * tag for debugging.
     */
    public static final String TAG = ServiceBroadcaster.class.getSimpleName();

    /**
     * intent name tag for identifying broadcast
     */
    public static final String BROADCAST_INTENT = "broadcast-intent";

    /**
     * key used in bundle for identifying the action of the broadcast.
     */
    public static final String KEY_ACTION = "key-action";
    public static final String KEY_NEXT_ITEM_INDEX = "key-next-item-index";
    public static final String KEY_PLAY_SENTENCE_INDEX = "key-play-sentence-index";

    /**
     * strings for identifying the action.
     */
    public static final String ACTION_ITEM_COMPLETE = "action-item-complete";
    public static final String ACTION_LIST_COMPLETE = "action-list-complete";
    public static final String ACTION_SHOW_DETAIL = "action-show-detail";
    public static final String ACTION_HIDE_DETAIL = "action-hide-detail";
    public static final String ACTION_PLAY_SENTENCE = "action-play-sentence";

    private Context wContext;
    private LocalBroadcastManager wBroadcastManager;
    private Intent wIntent;

    public ServiceBroadcaster(Context context) {
        wContext = context;
        wBroadcastManager = LocalBroadcastManager.getInstance(context);
        wIntent = new Intent(BROADCAST_INTENT);
    }

    public void onItemComplete(int nextItemIndex) {
        wIntent.putExtra(KEY_ACTION, ACTION_ITEM_COMPLETE);
        wIntent.putExtra(KEY_NEXT_ITEM_INDEX, nextItemIndex);
        wBroadcastManager.sendBroadcast(wIntent);
    }

    public void onListComplete() {
        wIntent.putExtra(KEY_ACTION, ACTION_LIST_COMPLETE);
        wBroadcastManager.sendBroadcast(wIntent);
    }

    public void onShowDetail() {
        wIntent.putExtra(KEY_ACTION, ACTION_SHOW_DETAIL);
        wBroadcastManager.sendBroadcast(wIntent);
    }

    public void onHideDetail() {
        wIntent.putExtra(KEY_ACTION, ACTION_HIDE_DETAIL);
        wBroadcastManager.sendBroadcast(wIntent);
    }

    public void onPlaySentence(int sentenceIndex) {
        wIntent.putExtra(KEY_ACTION, ACTION_PLAY_SENTENCE);
        wIntent.putExtra(KEY_PLAY_SENTENCE_INDEX, sentenceIndex);
        wBroadcastManager.sendBroadcast(wIntent);
    }
}
