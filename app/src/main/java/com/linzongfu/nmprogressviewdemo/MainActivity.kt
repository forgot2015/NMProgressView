package com.linzongfu.nmprogressviewdemo

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.linzongfu.nmprogressviewdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.sbProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.myProgressView.setProgress(progress)
                    binding.myProgressView2.setProgress(progress)
                    binding.myProgressView3.setProgress(progress)
                    binding.myProgressView4.setProgress(progress)
                    binding.myProgressHorizontal.setProgress(progress)
                    if (progress % 10 == 0) {
                        binding.myProgressView.setTextEnable(false)
                    } else {
                        binding.myProgressView.setTextEnable(true)
                    }
                    if (progress > 95) {
                        binding.myProgressHorizontal.stopAnimation()
                    } else {
                        binding.myProgressHorizontal.startAnimation()
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}