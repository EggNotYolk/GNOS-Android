package com.gnos.model;

import android.widget.Button;

/**
 * 文字按钮
 */
public class WordButton {

    private int mIndex;
    private boolean mIsVisible;
    private String mWordString;

    private Button mViewButton;

    public WordButton(){

        mIsVisible = true;
        mWordString = "";
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int Index) {
        this.mIndex = Index;
    }

    public boolean isIsVisible() {
        return mIsVisible;
    }

    public void setIsVisible(boolean IsVisible) {
        this.mIsVisible = IsVisible;
    }

    public String getWordString() {
        return mWordString;
    }

    public void setWordString(String WordString) {
        this.mWordString = WordString;
    }

    public Button getViewButton() {
        return mViewButton;
    }

    public void setViewButton(Button ViewButton) {
        this.mViewButton = ViewButton;
    }
}