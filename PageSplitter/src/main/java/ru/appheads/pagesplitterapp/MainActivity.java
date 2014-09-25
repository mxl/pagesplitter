package ru.appheads.pagesplitterapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.view.ViewTreeObserver;


public class MainActivity extends FragmentActivity {
    private ViewPager pagesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        pagesView = (ViewPager) findViewById(R.id.pages);

        // to get ViewPager width and height we have to wait global layout
        pagesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                PageSplitter pageSplitter = new PageSplitter(pagesView.getWidth(), pagesView.getHeight(), 1, 0);

                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
                for (int i = 0; i < 1000; i++) {
                    pageSplitter.append("Hello, ", textPaint);
                    textPaint.setFakeBoldText(true);
                    pageSplitter.append("world", textPaint);
                    textPaint.setFakeBoldText(false);
                    pageSplitter.append("! ", textPaint);
                    if ((i + 1) % 100 == 0) {
                        pageSplitter.append("\n", textPaint);
                    }
                }

                pagesView.setAdapter(new TextPagerAdapter(getSupportFragmentManager(), pageSplitter.getPages()));
                pagesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }
}
