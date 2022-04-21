package com.ivyiot.appsdk;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * 手势控制类
 *
 * @author MZP
 *
 */
 abstract class FlipGestureDetector {
	static final String LOG_TAG = "FlipGestureDetector";
	OnGestureListener mListener;

	public static FlipGestureDetector newInstance(Context context, OnGestureListener listener) {
		FlipGestureDetector detector = null;
		detector = new CupcakeDetector(context);
		detector.mListener = listener;
		return detector;
	}

	public abstract boolean onTouchEvent(MotionEvent ev);

	/*
	 * 回调接口，用于扩展
	 */
	public interface OnGestureListener {
		void onDrag(float dx, float dy);// 单指拖拽

		void onFling(float startX, float startY, float velocityX, float velocityY);// 手势

	}

	/**
	 *
	 * @author MZP
	 *
	 */
	private static class CupcakeDetector extends FlipGestureDetector {

		float mLastTouchX;// 按下的横坐标
		float mLastTouchY;// 按下的纵坐标
		final float mTouchSlop;// 拖动的最小距离
		final float mMinimumVelocity;// 监测手势的最小速率

		public CupcakeDetector(Context context) {
			final ViewConfiguration configuration = ViewConfiguration.get(context);
			mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
			mTouchSlop = configuration.getScaledTouchSlop();
		}

		private VelocityTracker mVelocityTracker;// 获取速率对象
		private boolean mIsDragging;// 是否在拖动

		/**
		 * 获取横坐标
		 *
		 * @param ev
		 * @return
		 */
		float getActiveX(MotionEvent ev) {
			return ev.getX();
		}

		/**
		 * 获取纵坐标
		 *
		 * @param ev
		 * @return
		 */
		float getActiveY(MotionEvent ev) {
			return ev.getY();
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			switch (ev.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					mVelocityTracker = VelocityTracker.obtain();
					mVelocityTracker.addMovement(ev);

					mLastTouchX = getActiveX(ev);
					mLastTouchY = getActiveY(ev);
					mIsDragging = false;
					break;
				}

				case MotionEvent.ACTION_MOVE: {
					final float x = getActiveX(ev);
					final float y = getActiveY(ev);
					final float dx = x - mLastTouchX, dy = y - mLastTouchY;

					if (!mIsDragging) {
						// Use Pythagoras to see if drag length is larger than
						// touch slop
						mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
					}

					if (mIsDragging) {
						mListener.onDrag(dx, dy);
						mLastTouchX = x;
						mLastTouchY = y;

						if (null != mVelocityTracker) {
							mVelocityTracker.addMovement(ev);
						}
					}
					break;
				}

				case MotionEvent.ACTION_CANCEL: {
					// Recycle Velocity Tracker
					if (null != mVelocityTracker) {// 释放
						mVelocityTracker.recycle();
						mVelocityTracker = null;
					}
					break;
				}

				case MotionEvent.ACTION_UP: {
					if (mIsDragging) {
						if (null != mVelocityTracker) {
							mLastTouchX = getActiveX(ev);
							mLastTouchY = getActiveY(ev);

							// Compute velocity within the last 1000ms
							mVelocityTracker.addMovement(ev);
							mVelocityTracker.computeCurrentVelocity(1000);

							final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker.getYVelocity();

							// If the velocity is greater than minVelocity, call
							// listener
							if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {// 达到最小速率
								mListener.onFling(mLastTouchX, mLastTouchY, -vX, -vY);
							}
						}
					}

					// Recycle Velocity Tracker
					if (null != mVelocityTracker) {
						mVelocityTracker.recycle();
						mVelocityTracker = null;
					}
					break;
				}
			}

			return true;
		}
	}
}