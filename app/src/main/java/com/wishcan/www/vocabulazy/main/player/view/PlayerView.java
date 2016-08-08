package com.wishcan.www.vocabulazy.main.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.wishcan.www.vocabulazy.storage.databaseObjects.OptionSettings;
import com.wishcan.www.vocabulazy.R;

/**
 * Created by swallow on 2016/1/17.
 */

public class PlayerView extends RelativeLayout {

	public interface PlayerEventListener {
		/** TODO: Change this event into dialogview */
		void onGrayBackClick();
		/** PlayerMainView's Event */
		void onPlayerVerticalScrollStop(int index, boolean isViewTouchedDown);
        void onPlayerVerticalScrolling();
        void onPlayerHorizontalScrollStop(boolean isOrderChanged, int direction, boolean isViewTouchedDown);
        void onPlayerHorizontalScrolling();
        void onPlayerDetailScrollStop(int index, boolean isViewTouchedDown);
        void onPlayerDetailScrolling();
        void onPlayerInitialItemPrepared();
        void onPlayerFinalItemPrepared();
        /** PlayerPanel's Event */
        void onPlayerPanelFavoriteClick();
		void onPlayerPanelPlayClick();
		void onPlayerPanelOptionClick();
        /** PlayerOptionView's Event */
        void onPlayerOptionChanged(View v, ArrayList<OptionSettings> optionSettingsLL, int currentMode);
	}

	private static final int VIEW_PLAYER_MAIN_RES_ID = R.id.player_main_view;
	private static final int VIEW_PLAYER_PANEL_RES_ID = R.id.player_panel;
	private static final int VIEW_PLAYER_OPTION_RES_ID = R.id.player_option_view;
	private static final int VIEW_PLAYER_OPTION_PARENT_RES_ID = R.id.player_option_parent;
	private static final int ANIMATE_TRANSLATE_BOTTOM_UP = R.anim.translate_bottom_up;
	private static final int ANIMATE_TRANSLATE_UP_BOTTOM = R.anim.translate_up_bottom;
	
	private PlayerMainView mPlayerMainView;
	private PlayerPanelView mPlayerPanelView;
	private PlayerOptionView mPlayerOptionView;
	private ViewGroup mPlayerOptionGrayBack;
	
	private PlayerEventListener mPlayerEventListener;
	
	public PlayerView(Context context) {
		this(context, null);
	}
	
	public PlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO: Apply the adding child animation to replace showPlayerOptionView and exitPlayerOptionView
	}
	
	@Override
    protected void onFinishInflate() {
        mPlayerMainView = (PlayerMainView) childView.findViewById(VIEW_PLAYER_MAIN_RES_ID);
		mPlayerPanelView = (PlayerPanelView) childView.findViewById(VIEW_PLAYER_PANEL_RES_ID);
        mPlayerOptionView = (PlayerOptionView) childView.findViewById(VIEW_PLAYER_OPTION_RES_ID);
        mPlayerOptionGrayBack = (ViewGroup) childView.findViewById(VIEW_PLAYER_OPTION_PARENT_RES_ID);
        
        registerEventListener();
    }
    
    /**
     * TODO: replace all event handler to PlayerEventListener
     */
    public void setPlayerEventListener(PlayerEventListener listener) {
    	mPlayerEventListener = listener;
    }
    
    private void registerEventListener() {
    	mPlayerMainView.setOnPlayerScrollListener(new PlayerMainView.OnPlayerScrollListener() {
    		@Override
    		void onPlayerVerticalScrollStop(int index, boolean isViewTouchedDown) {
    			if (mPlayerEventListener != null) {
    				mPlayerEventListener.onPlayerVerticalScrollStop(index, isViewTouchedDown);
    			}
    		}
    		@Override
	        void onPlayerVerticalScrolling() {
	        	if (mPlayerEventListener != null) {
	        		mPlayerEventListener.onPlayerVerticalScrolling();
    			}
	        }
	        @Override
	        void onPlayerHorizontalScrollStop(boolean isOrderChanged, int direction, boolean isViewTouchedDown) {
	        	if (mPlayerEventListener != null) {
	        		mPlayerEventListener.onPlayerHorizontalScrollStop(isOrderChanged, direction, isViewTouchedDown);
    			}
	        }
	        @Override
	        void onPlayerHorizontalScrolling() {
	        	if (mPlayerEventListener != null) {
	        		mPlayerEventListener.onPlayerHorizontalScrolling();
    			}
	        }
	        @Override
	        void onDetailScrollStop(int index, boolean isViewTouchedDown) {
	        	if (mPlayerEventListener != null) {
	        		mPlayerEventListener.onPlayerDetailScrollStop(index, isViewTouchedDown);
    			}
	        }
	        @Override
	        void onDetailScrolling() {
	        	if (mPlayerEventListener != null) {
	        		mPlayerEventListener.onPlayerDetailScrolling();
    			}
	        }
    	});
    	
    	mPlayerMainView.setOnItemPreparedListener(new PlayerMainView.OnPlayerItemPreparedListener(){
    		@Override
	        void onInitialItemPrepared() {
	        	if (mPlayerEventListener != null) {
	        		mPlayerEventListener.onPlayerInitialItemPrepared();
	        	}
	        }
	        @Override
	        void onFinalItemPrepared() {
	        	if (mPlayerEventListener != null) {
	        		mPlayerEventListener.onPlayerFinalItemPrepared();
	        	}
	        }
    	});
    	
    	mPlayerPanelView.setOnPanelItemClickListener(new PlayerPanel.OnPanelItemClickListener(){
    		@Override
    		void onOptionFavoriteClick() {
    			if (mPlayerEventListener != null) {
    				mPlayerPanelView.onPlayerPanelFavoriteClick();
    			}
    		}
    		@Override
			void onOptionPlayClick() {
				if (mPlayerEventListener != null) {
					mPlayerPanelView.onPlayerPanelPlayClick();
    			}
			}
			@Override
			void onOptionOptionClick() {
				if (mPlayerEventListener != null) {
					mPlayerPanelView.onPlayerPanelOptionClick();
    			}
			}
    	});
    	
    	mPlayerOptionView.setOnOptionChangedListener(new PlayerOptionView.OnOptionChangedListener(){
    		@Override
    		public void onOptionChanged(View v, ArrayList<OptionSettings> optionSettingsLL, int currentMode) {
    			if (mPlayerEventListener != null) {
    				mPlayerEventListener.onPlayerOptionChanged(v, optionSettingsLL, currentMode);
    			}
    		}
    	});

    	mPlayerOptionGrayBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPlayerEventListener != null) {
					mPlayerEventListener.onGrayBackClick();
				}
				exitPlayerOptionView();
			}
        });
    }
    
	/**------------------------------  PlayerMainView related action  ---------------------------**/
	public void addNewPlayer(LinkedList<HashMap> playerDataList, int initPosition) {
		mPlayerMainView.addNewPlayer(playerDataList, initPosition);
	}
	
	public void removeOldPlayer(int direction) {
		mPlayerMainView.removeOldPlayer(direction);
	}
	
	public void moveToPlayer(int direction) {
		mPlayerMainView.setCurrentItem(direction);
	}
	
	public void moveToPosition(int position) {
		mPlayerMainView.moveToPosition(position);
	}
	
	public void showDetail() {
		mPlayerMainView.showDetail();
	}
	
	public void hideDetail() {
		mPlayerMainView.hideDetail();
	}
	
	public void refreshPlayerDetail(HashMap<String, Object> dataMap) {
		mPlayerMainView.refreshPlayerDetail(dataMap);
	}
	
	public void moveDetailPage(int index) {
		mPlayerMainView.moveToDetailPage(index);
	}
	/**------------------------------ PlayerPanelView related action  ---------------------------**/
	public void setIconState(boolean favorite, boolean play, boolean option) {
		mPlayerPanelView.setIconState(favorite, play, option);
	}
	/**------------------------------ PlayerOptionView related action ---------------------------**/
	public void showPlayerOptionView() {
		if (mPlayerOptionGrayBack.getVisibility() == View.INVISIBLE) {
			mPlayerOptionGrayBack.setVisibility(View.VISIBLE);
		}

		Animation comeInAnimation = AnimationUtils.loadAnimation(getContext(), ANIMATE_TRANSLATE_BOTTOM_UP);
		mPlayerOptionView.startAnimation(comeInAnimation);

		mPlayerOptionGrayBack.invalidate();
	}
	
	public void exitPlayerOptionView() {
		if (mPlayerOptionGrayBack.getVisibility() != View.VISIBLE) {
			return;
		}
		Animation goOutAnimation = AnimationUtils.loadAnimation(getContext(), ANIMATE_TRANSLATE_UP_BOTTOM);
		mPlayerOptionView.startAnimation(goOutAnimation);
		goOutAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {
				mPlayerOptionGrayBack.setVisibility(View.INVISIBLE);
				mPlayerOptionGrayBack.invalidate();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
	}
	
	public void setPlayerOptionTabContent(ArrayList<OptionSettings> options) {
        mPlayerOptionView.setOptionsInTabContent(options);
	}

	public void setOnGrayBackClickListener(OnGrayBackClickListener listener) {
		mOnGrayBackClickListener = listener;
	}
}