package com.label305.stan.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.label305.stan.R;
import com.label305.stan.ui.anim.ExpandViewAnimation;

public class ExpandableTitleView extends LinearLayout {

	private static final int ANIMATIONDURATION = 400;

	private View mBannerView;
	private CustomFontTextView mTitleTV;
	private ImageView mIconIV;
	private ImageView mMoreImageButton;
	private ProgressBar mProgressBar;

	private ViewGroup mContentVG;

	private boolean mExpanded = false;

	private int mBackgroundColor;
	private int mIconResId;
	private String mText;
	private int mTextColor;
	private String mTextFont;
	private int mTextSize;
	private int mArrowResId;
	private boolean mContentVisible;
	private boolean mDataAvailable = true;

	public ExpandableTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_expandabletitle, this);
		setOrientation(VERTICAL);

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTitleView);
		mBackgroundColor = a.getColor(R.styleable.ExpandableTitleView_bg, R.color.transparent);
		mIconResId = a.getResourceId(R.styleable.ExpandableTitleView_icon, 0);
		mText = a.getString(R.styleable.ExpandableTitleView_titleText);
		mTextColor = a.getColor(R.styleable.ExpandableTitleView_titleTextColor, R.color.black);
		mTextFont = a.getString(R.styleable.ExpandableTitleView_titleTextFont);
		mTextSize = a.getDimensionPixelSize(R.styleable.ExpandableTitleView_titleTextSize,
				getResources().getDimensionPixelSize(R.dimen.textsize_medium));
		mArrowResId = a.getResourceId(R.styleable.ExpandableTitleView_arrow, 0);
		mContentVisible = a.getBoolean(R.styleable.ExpandableTitleView_contentVisible, true);
		mExpanded = mContentVisible;
		a.recycle();

		setupViews();
	}

	private void setupViews() {
		mBannerView = findViewById(R.id.view_expandabletitle_banner);
		mBannerView.setBackgroundColor(mBackgroundColor);
		mBannerView.setOnClickListener(new BannerOnClickListener());

		mTitleTV = (CustomFontTextView) findViewById(R.id.view_expandabletitle_titletv);
		mTitleTV.setFont(mTextFont);
		mTitleTV.setTextColor(mTextColor);
		mTitleTV.setText(mText);
		mTitleTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);

		mIconIV = (ImageView) findViewById(R.id.view_expandabletitle_iconiv);
		mIconIV.setImageResource(mIconResId);

		mMoreImageButton = (ImageView) findViewById(R.id.view_expandabletitle_morebutton);
		mMoreImageButton.setImageResource(mArrowResId);

		mProgressBar = (ProgressBar) findViewById(R.id.view_expandabletitle_progressbar);

		mContentVG = (ViewGroup) findViewById(R.id.view_expandabletitle_expandedcontent);

		if (mContentVisible) {
			mContentVG.setVisibility(View.VISIBLE);
			mMoreImageButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotatecw90));
		} else {
			//
		}
	}

	public void setTitle(String title) {
		mTitleTV.setText(title);
	}

	public void setIcon(int resId) {
		mIconIV.setImageResource(resId);
	}

	public void setIcon(Drawable drawable) {
		mIconIV.setImageDrawable(drawable);
	}

	public void setIcon(Bitmap bitmap) {
		mIconIV.setImageBitmap(bitmap);
	}

	public void setContent(View view) {
		mContentVG.removeAllViews();
		mContentVG.addView(view);
	}

	public ViewGroup getContent() {
		return mContentVG;
	}

	/**
	 * If !mDataAvailable, will show a indeterminate spinner instead of an arrow
	 * until setDataAvailable(true);
	 */
	public void setDataAvailable(boolean dataAvailable) {
		this.mDataAvailable = dataAvailable;

		if (dataAvailable) {
			mProgressBar.setVisibility(View.GONE);
			mMoreImageButton.setVisibility(View.VISIBLE);
		} else {
			mProgressBar.setVisibility(View.VISIBLE);
			mMoreImageButton.setVisibility(View.GONE);
		}
	}

	public boolean hasDataAvailable() {
		return mDataAvailable;
	}

	public void expandContent() {
		final int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
		final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		mContentVG.measure(widthSpec, heightSpec);

		ExpandViewAnimation a = new ExpandViewAnimation(mContentVG, ANIMATIONDURATION, ExpandViewAnimation.EXPAND);
		a.setHeight(mContentVG.getMeasuredHeight());
		mContentVG.startAnimation(a);

		mMoreImageButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotatecw90));
		mExpanded = true;
	}

	public void collapseContent() {
		ExpandViewAnimation a = new ExpandViewAnimation(mContentVG, ANIMATIONDURATION, ExpandViewAnimation.COLLAPSE);
		mContentVG.startAnimation(a);

		mMoreImageButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotateccw90));
		mExpanded = false;
	}

	private class BannerOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (mDataAvailable) {
				if (mExpanded) {
					collapseContent();
				} else {
					expandContent();
				}
			}
		}
	}
}