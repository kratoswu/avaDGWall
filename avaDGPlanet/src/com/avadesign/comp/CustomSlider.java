package com.avadesign.comp;

import com.avadesign.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CustomSlider extends LinearLayout {

    private TextView nameLbl;
    private TextView progInfo;
    private Button switchBtn;
    private SeekBar seek;

    private boolean status;
    private boolean isMultiple;
    private int originalProgress;
    private int lockTime;
    
    private void setLockStatus() {
        setAlpha(.45F);
    }
    
    private void undoLockStatus() {
        setAlpha(100F);
    }
    
    public void setLockTime(int lockTime) {
        this.lockTime = lockTime;
        
        if (this.lockTime > 0) {
            setLockStatus();
        } else {
            undoLockStatus();
        }
    }
    
    public int getLockTime() {
        return lockTime;
    }

    public void setDemoStatus(boolean isDemo) {
        seek.setEnabled(!isDemo);
    }

    public int getOldProgress() {
        return originalProgress;
    }

    public void setSeekListener(OnSeekBarChangeListener lsnr) {
        seek.setOnSeekBarChangeListener(lsnr);
    }

    public boolean isOn() {
        return status;
    }

    public void setSwitchBtnClickListener(OnClickListener lsnr) {
        switchBtn.setOnClickListener(lsnr);
    }

    public void updateProgInfo(int progress) {
        progInfo.setText(progress + "%");
    }

    public void updateProgress(int progress) {
        if (!isMultiple) {
            seek.setProgress(progress > originalProgress ? 100 : 0);
        } else {
            seek.setProgress(progress);
        }

        originalProgress = seek.getProgress();
        status = seek.getProgress() > 0;
        updateSwitchBtnStatus();
    }

    public void turnOn() {
        originalProgress = 0;
        updateProgress(100);
    }

    public void turnOff() {
        originalProgress = 100;
        updateProgress(0);
    }

    public CustomSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.slider_custom, this, true);

        nameLbl = (TextView) findViewById(R.id.sliderLbl);
        progInfo = (TextView) findViewById(R.id.sliderProgInfo);
        initSwitchBtn();
        initSeekbar();
    }

    /**
     *
     */
    private void initSeekbar() {
        seek = (SeekBar) findViewById(R.id.sliderSeek);
        progInfo.setText(seek.getProgress() + "%");
    }

    /**
     *
     */
    private void initSwitchBtn() {
        switchBtn = (Button) findViewById(R.id.nvrBtn);
        updateSwitchBtnStatus();
    }

    private void updateSwitchBtnStatus() {
        switchBtn.setText(status ? "ON" : "OFF");
    }

    public CustomSlider(Context context) {
        this(context, null);
    }

    public void setTitle(String title) {
        if (nameLbl != null) {
            nameLbl.setText(title);
        }
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public void setMultiple(boolean isMultiple) {
        this.isMultiple = isMultiple;
    }

    public int getCurrentProgress() {
        return seek.getProgress();
    }

}
