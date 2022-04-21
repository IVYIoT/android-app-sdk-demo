package com.ivyiot.appsdk;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class LiveVideoFrame extends FrameLayout implements FlipGestureDetector.OnGestureListener {
	private int view_width;// 每个播放view的宽
	private int view_height;// 每个播放view的高
	private View videoSurface;// 当前的播放View

	private boolean once_flag = false;// 保证执行一次标志
	double rate = 10.0 / 16.0;// 竖屏时窗口按16:9比例缩放
	private FlipGestureDetector mScaleDragDetector;// 单指滑动手势
	/** 最大放大倍数 */
	private float MAX_TIMES = 3;
	/** 最小缩小倍数 */
	private float MIN_TIMES = 1;
	/** 记录上次两指之间的距离 */
	private double lastFingerDis, fingerDis;
	public static int  SWIPE_DISTANCE = 200 ;  //最小的滑动速率

	/**
	 * 模式 NONE：无 DRAG：拖拽. ZOOM:缩放
	 */
	private enum TOUCH_MODE {
		NONE, DRAG, ZOOM
	}

	/**
	 * 滑动方向
	 */
	public enum SWIPE_DIRECTION{
		UP, DOWN, LEFT, RIGHT
	}

	private TOUCH_MODE mode = TOUCH_MODE.NONE;// 默认模式
	private float scaledRatio;// 缩放比例
	private boolean is_horizontal_listen = false;// 垂直监控
	private boolean is_vertical_listen = false;// 水平监控
	private boolean isScaleAnim = false;// 是否处于缩放动画
	private Thread viewRetraction;// 回缩线程
	/** 用于记录正常播放时的区域 */
	private Rect initRect;
	/** 是否可以缩放标志 当实时流未播放时则不可以缩放 */
	private boolean allow_scale_view = false;
	/** 是否可以滑动触发云台转动 */
	private boolean is_support_ptz_swipe = false;
	/**
	 * 抓拍效果图层 private View live_capture_flash;
	 */
	/** 扩展接口 */
	private LiveVideoExtendsListener extendListener;

	public LiveVideoFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScaleDragDetector = FlipGestureDetector.newInstance(context, this);
		initRect = new Rect();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!once_flag) {// 保证只执行一次
			videoSurface = this.findViewById(R.id.videoView);
			once_flag = true;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		view_width = w;
		view_height = h;
//		if (view_width == Global.screenWidth) {// 竖屏
//			view_height = (int) (view_width * rate);
//		} else {
//			view_height = Global.screenWidth;
//			view_width = (int)(Global.screenWidth/rate);
//		}
		videoSurface.layout(0, 0, view_width, view_height);
		LayoutParams layoutParams=	new LayoutParams(view_width, view_height);
		videoSurface.setLayoutParams(layoutParams);
		initRect = new Rect(0, 0 , view_width, view_height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

	}

	/**
	 * 抓拍特效
	 */
	public void captureAnim() {
		videoSurface.layout(0, 0, 0, 0);
		videoSurface.setLayoutParams(new LayoutParams(view_width, view_height));
		videoSurface.layout(initRect.left, initRect.top, initRect.right, initRect.bottom);
	}

	private int offset_x, offset_y;// 初始偏移的横纵坐标
	private int mLastX, mLastY;// 初始横纵坐标
	private boolean one_time = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = true;
		/** 处理单点、多点触摸 **/
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if (null != extendListener) {
					extendListener.onTouchDownEvent();
				}
				mode = TOUCH_MODE.DRAG;
				if(event.getPointerCount() == 1){//一根手指按在屏幕上时
					mLastX = (int) event.getRawX();
					mLastY = (int) event.getRawY();
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				if (event.getPointerCount() == 2) {// 两个手指按在屏幕上时
					mode = TOUCH_MODE.ZOOM;
					lastFingerDis = getDistance(event);// 获取两点的距离
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (event.getPointerCount() == 2 && allow_scale_view) {
					fingerDis = getDistance(event);
					float gapLenght = (float) (lastFingerDis - fingerDis);// 变化的长度
					if (Math.abs(gapLenght) > 5f) {
						scaledRatio = (float) (fingerDis / lastFingerDis);// 求的缩放的比例
						setBitMapScale(scaledRatio);
						lastFingerDis = fingerDis;
					}
				}
				if(is_support_ptz_swipe){//支持云台才可支持滑动触发
					if (event.getPointerCount() == 1 && allow_scale_view && mode == TOUCH_MODE.DRAG && !is_horizontal_listen && !is_vertical_listen) {//计算出滑动方向
						int x = (int) event.getRawX();
						int y = (int) event.getRawY();
						offset_x = x - mLastX;
						offset_y = y - mLastY;
						if (null != extendListener) {
							if(!one_time){
								if(offset_x >= SWIPE_DISTANCE && (offset_y <= SWIPE_DISTANCE || offset_y >= -SWIPE_DISTANCE)){//右滑
									extendListener.swipeDirection(SWIPE_DIRECTION.RIGHT);
									one_time = true;
								}else if(offset_x <= -SWIPE_DISTANCE && (offset_y <= SWIPE_DISTANCE || offset_y >= -SWIPE_DISTANCE)){//左滑
									extendListener.swipeDirection(SWIPE_DIRECTION.LEFT);
									one_time = true;
								}else if(offset_y >= SWIPE_DISTANCE && (offset_x <= SWIPE_DISTANCE || offset_x >= -SWIPE_DISTANCE)) {//下滑
									extendListener.swipeDirection(SWIPE_DIRECTION.DOWN);
									one_time = true;
								}else if(offset_y <= -SWIPE_DISTANCE && (offset_x <= SWIPE_DISTANCE || offset_x >= -SWIPE_DISTANCE)) {//上滑
									extendListener.swipeDirection(SWIPE_DIRECTION.UP);
									one_time = true;
								}

							}
						}
					}
				}

				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode = TOUCH_MODE.NONE;
				/** 执行缩放还原 **/
				if (isScaleAnim) {
					//doScaleAnim();
				}
				//one_time = false;
				break;
			case MotionEvent.ACTION_UP:
				mode = TOUCH_MODE.NONE;
				if (null != extendListener) {
					if(one_time){
						extendListener.onTouchCancleEvent();

					}
					one_time = false;
				}

				break;
		}
		// Finally, try the Drag detector
		if (null != mScaleDragDetector && mScaleDragDetector.onTouchEvent(event)) {
			handled = true;
		}

		return handled;
	}


	/**
	 * 设置位图缩放
	 *
	 * @param scale
	 *            缩放倍率
	 */
	private void setBitMapScale(float scale) {
		if (mode == TOUCH_MODE.ZOOM) {
			int disX = (int) (videoSurface.getWidth() * Math.abs(1 - scale) / MAX_TIMES);// 获取缩放水平距离
			int disY = (int) (videoSurface.getHeight() * Math.abs(1 - scale) / MAX_TIMES);// 获取缩放垂直距离
			// Logs.i(TAG, "--------------------> ZOOM disX:"+disX +"--> disY:"+disY);
			int left = videoSurface.getLeft();
			int top = videoSurface.getTop();
			int right = videoSurface.getRight();
			//int bottom = videoSurface.getBottom();
			int bottom = (int)((right - left) * ((float)initRect.height() / (float)initRect.width()) + top);
			if (scale > 1 && videoSurface.getWidth() <= initRect.width() * MAX_TIMES) {// 放大
				left = videoSurface.getLeft() - disX;
				top = videoSurface.getTop() - disY;
				right = videoSurface.getRight() + disX;
				//bottom = videoSurface.getBottom() + disY;
				bottom = (int)((right - left) * ((float)initRect.height() / (float)initRect.width()) + top);
				/***
				 * 此时因为考虑到对称，所以只做一遍判断就可以了。
				 */
				// Log.e("jj", "屏幕高度=" + this.getHeight());
				// 开启垂直监控
				is_horizontal_listen = top <= initRect.top && videoSurface.getBottom() >= initRect.bottom;
				// 开启水平监控
				is_vertical_listen = left <= initRect.left && right >= initRect.right;
			} else if (scale < 1 && videoSurface.getWidth() > initRect.width() * MIN_TIMES && videoSurface.getHeight() > initRect.height() * MIN_TIMES) {
				left = videoSurface.getLeft() + disX;
				top = videoSurface.getTop() + disY;
				right = videoSurface.getRight() - disX;
				//bottom = videoSurface.getBottom() - disY;
				bottom = (int)((right - left) * ((float)initRect.height() / (float)initRect.width()) + top);
				/***
				 * 在这里要进行缩放处理
				 */
				// 上边越界
				if (is_horizontal_listen && top >= initRect.top) {
					top = initRect.top;
					bottom = bottom - 2 * disY;
					if (bottom < initRect.bottom) {
						bottom = initRect.bottom;
						is_horizontal_listen = false;// 关闭垂直监听
					}
				}
				// 下边越界
				if (is_horizontal_listen && bottom <= initRect.bottom) {
					bottom = initRect.bottom;
					top = top + 2 * disY;
					if (top > initRect.top) {
						top = initRect.top;
						is_horizontal_listen = false;// 关闭垂直监听
					}
				}

				// 左边越界
				if (is_vertical_listen && left >= initRect.left) {
					left = initRect.left;
					right = right - 2 * disX;
					if (right <= initRect.right) {
						right = initRect.right;
						is_vertical_listen = false;// 关闭
					}
				}
				// 右边越界
				if (is_vertical_listen && right <= initRect.right) {
					right = initRect.right;
					left = left + 2 * disX;
					if (left >= initRect.left) {
						left = initRect.left;
						is_vertical_listen = false;// 关闭
					}
				}
				if (is_vertical_listen || is_horizontal_listen) {
				} else {
					isScaleAnim = true;// 开启缩放动画
				}
			}

			videoSurface.layout(left, top, right, bottom);
		}
	}

	/** 获取两点的距离 **/
	private float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * 检测拖拽边距
	 */
	private void checkDragBounds(float dx, float dy) {

		int left = 0, top = 0, right = 0, bottom = 0;
		/** 在这里要进行判断处理，防止在drag时候越界 **/
		int i_dx = (int) dx;
		int i_dy = (int) dy;
		/** 获取相应的l，t,r ,b **/
		left = i_dx + videoSurface.getLeft();
		right = i_dx + videoSurface.getRight();
		top = videoSurface.getTop() + i_dy;
		//bottom = i_dy + videoSurface.getBottom();
		bottom = (int)((right - left) * ((float)initRect.height() / (float)initRect.width()) + top);
		/** 水平进行判断 **/
		if (is_vertical_listen) {
			if (left >= initRect.left) {
				left = initRect.left;
				right = videoSurface.getRight();
			}
			if (right <= initRect.right) {
				left = videoSurface.getLeft();
				right = initRect.right;
			}
		} else {
			left = videoSurface.getLeft();
			right = videoSurface.getRight();
		}
		/** 垂直判断 **/
		if (is_horizontal_listen) {
			if (top >= initRect.top) {
				top = initRect.top;
				bottom = videoSurface.getBottom();
			}
			if (bottom <= initRect.bottom) {
				top = videoSurface.getTop();
				bottom = initRect.bottom;
			}
		} else {
			top = videoSurface.getTop();
			bottom = videoSurface.getBottom();
		}
		if (is_vertical_listen || is_horizontal_listen)
			videoSurface.layout(left, top, right, bottom);

	}

	@Override
	public void onDrag(float dx, float dy) {
		if (mode == TOUCH_MODE.DRAG) {// 单指滑动
			checkDragBounds(dx, dy);
		}
	}

	@Override
	public void onFling(float startX, float startY, float velocityX, float velocityY) {
		/*
		 * Logs.e(TAG, "--------------------> onFling velocityX:"+velocityX+"  velocityY:"+velocityY); FlingRunnable mCurrentFlingRunnable = new FlingRunnable(context); mCurrentFlingRunnable.fling(dst, initRect, (int) velocityX, (int) velocityY); new Thread(mCurrentFlingRunnable).start();
		 */
	}

	/**
	 * 设置当前缩放标志
	 *
	 * @param isAllow
	 */
	public void setAllowScaleOperate(boolean isAllow) {
		allow_scale_view = isAllow;
	}

	/**
	 * 是否支持云台滑动
	 * @param isSupport
	 */
	public void setSupportPtzSwipe(boolean isSupport){
		is_support_ptz_swipe = isSupport;
	}

	public void setLiveVideoExtendsListener(LiveVideoExtendsListener extendListener) {
		this.extendListener = extendListener;
	}

	public interface LiveVideoExtendsListener {
		/** 按下事件回调 */
		void onTouchDownEvent();
		/** 取消事件回调 */
		void onTouchCancleEvent();
		/**
		 * 滑动方向
		 */
		void swipeDirection(SWIPE_DIRECTION direction);
	}
}
