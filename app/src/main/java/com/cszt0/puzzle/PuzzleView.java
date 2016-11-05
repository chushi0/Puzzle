package com.cszt0.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.Arrays;

public class PuzzleView extends View
{
	// the paint to draw view
	private Paint mPaint;
	// source bitmap
	private Bitmap source;
	// number of puzzle blocks segmentation
	// real number of segmentation is the square of this number
	private int number;
	// current state of segmentations
	private int[] state;
	// pieces of segmentations
	private Bitmap[] divideBitmap;
	// the flag to show if game is playing
	private boolean isPlaying;
	// active block
	private int activeBlock;
	// active position
	private float activeX;
	private float activeY;
	// listener
	private OnFinishListener mListener;

	public PuzzleView(Context context) {
		this(context, null);
	}

	public PuzzleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		number = 3;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		setBackgroundColor(0x40f00f0f);
		setBitmapSource(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
	}

	public void setBitmapSource(Bitmap source) {
		this.source = source;
		requestLayout();
		restartGame();
	}

	public void setSegmentationNumber(int number) {
		this.number = number;
		restartGame();
	}

	public int getSegmentationNumber() {
		return number;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		restartGame();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (source == null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		int w = source.getWidth();
		int h = source.getHeight();
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int size = Math.min(width / w, height / h);
		setMeasuredDimension(size * w, size * h);
	}

	public void restartGame() {
		state = null;
		divideBitmap = null;
		isPlaying = false;
		if (source != null) {
			int number = this.number;
			int size = number * number;
			state = new int[size];
			divideBitmap = new Bitmap[size];
			// divide bitmap
			Bitmap bmp = source;
			int width = bmp.getWidth() / number;
			int height = bmp.getHeight() / number;
			for (int x = 0;x < number;x++) {
				for (int y = 0;y < number;y++) {
					divideBitmap[y * number + x] = Bitmap.createBitmap(source, x * width, y * height, width, height);
				}
			}
			// random state
			Arrays.fill(state, -1);
			int len = size;
			int random = (int) (System.nanoTime() % size);
			block:
			while (len > 0) {
				for (int s:state) {
					if (s == random) {
						random++;
						if (random >= size) {
							random = 0;
						}
						continue block;
					}
				}
				len--;
				state[len] = random;
				random = (int) (System.nanoTime() % size);
			}
			isPlaying = true;
			activeBlock = -1;
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (source == null) {
			return;
		}
		int w = getWidth() / source.getWidth();
		int h = getHeight() / source.getHeight();
		int len = state.length;
		for (int i = 0;i < len;i++) {
			if (i == activeBlock) {
				continue;
			}
			int x = i % number;
			int y = i / number;
			Bitmap divideBitmap = this.divideBitmap[state[i]];
			int width = divideBitmap.getWidth();
			int height = divideBitmap.getHeight();
			canvas.drawBitmap(divideBitmap, new Rect(0, 0, width, height), new Rect(x * w * width, y * h * height, (x + 1) * w * width, (y + 1) * h * height), mPaint);
		}
		if (activeBlock != -1) {
			int i = activeBlock;
			int x = i % number;
			int y = i / number;
			Bitmap divideBitmap = this.divideBitmap[state[i]];
			int width = divideBitmap.getWidth();
			int height = divideBitmap.getHeight();
			Rect rect = new Rect(x * w * width, y * h * height, (x + 1) * w * width, (y + 1) * h * height);
			canvas.save();
			canvas.translate(activeX - rect.left - rect.width() / 2, activeY - rect.top - rect.height() / 2);
			rect.top += height / 10;
			rect.bottom -= height / 10;
			rect.left += width / 10;
			rect.right -= width / 10;
			canvas.drawBitmap(divideBitmap, new Rect(0, 0, width, height), rect, mPaint);
			canvas.restore();
		}
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isPlaying) {
			return false;
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
					int x = (int)(event.getX() / (getWidth() / number));
					int y = (int)(event.getY() / (getHeight() / number));
					int index = x + y * number;
					activeBlock = index;
					// fall down
				}
			case MotionEvent.ACTION_MOVE: {
					activeX = event.getX();
					activeY = event.getY();
					break;
				}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
					if (new RectF(0, 0, getWidth(), getHeight()).contains(event.getX(), event.getY())) {
						int x = (int)(event.getX() / (getWidth() / number));
						int y = (int)(event.getY() / (getHeight() / number));
						int index = x + y * number;
						if (activeBlock != index) {
							int tmp = state[index];
							state[index] = state[activeBlock];
							state[activeBlock] = tmp;
							int len = state.length;
							boolean finish =true;
							for (int i = 0;i < len;i++) {
								if (state[i] != i) {
									finish = false;
									break;
								}
							}
							if (finish) {
								isPlaying = false;
								if (mListener != null) {
									mListener.onFinish();
								}
							}
						}
					}
					activeBlock = -1;
					break;
				}
		}
		invalidate();
		return true;
	}

	public void setOnFinishListener(OnFinishListener listener) {
		mListener = listener;
	}

	public static interface OnFinishListener
	{
		public abstract void onFinish();
	}

}
