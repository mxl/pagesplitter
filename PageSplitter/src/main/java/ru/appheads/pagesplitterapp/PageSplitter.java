package ru.appheads.pagesplitterapp;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.StyleSpan;

import java.util.ArrayList;
import java.util.List;

public class PageSplitter {
    private final int pageWidth;
    private final int pageHeight;
    private final float lineSpacingMultiplier;
    private final int lineSpacingExtra;
    private final List<CharSequence> pages = new ArrayList<CharSequence>();
    private SpannableStringBuilder currentLine = new SpannableStringBuilder();
    private SpannableStringBuilder currentPage = new SpannableStringBuilder();
    private int currentLineHeight;
    private boolean bStartNewLine;
    private boolean bNoSpaceLang = false;
    private int pageContentHeight;
    private int currentLineWidth;
    private int textLineHeight;

    public PageSplitter(int pageWidth, int pageHeight, float lineSpacingMultiplier, int lineSpacingExtra) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.lineSpacingMultiplier = lineSpacingMultiplier;
        this.lineSpacingExtra = lineSpacingExtra;
    }
    
    public void append(String text, TextPaint textPaint, boolean noSpace) 
    {
    	bNoSpaceLang = noSpace;
    	
    	append(text, textPaint);
    }

    public void append(String text, TextPaint textPaint) {
        textLineHeight = (int) Math.ceil(textPaint.getFontMetrics(null) * lineSpacingMultiplier + lineSpacingExtra);
        String[] paragraphs = text.split("\n", -1);
        int i;
        for (i = 0; i < paragraphs.length - 1; i++) {
            appendText(paragraphs[i], textPaint);
            appendNewLine();
        }
        appendText(paragraphs[i], textPaint);
    }

    private void appendText(String text, TextPaint textPaint) {
        String[] words = text.split(" ", -1);
        int i;
        for (i = 0; i < words.length - 1; i++) {
            appendWord(words[i] + " ", textPaint);
        }
        appendWord(words[i], textPaint);
    }

    private void appendNewLine() {
        currentLine.append("\n");
        checkForPageEnd();
        appendLineToPage(textLineHeight);
    }

    private void checkForPageEnd() {
        if (pageContentHeight + currentLineHeight > pageHeight) {
            pages.add(currentPage);
            currentPage = new SpannableStringBuilder();
            pageContentHeight = 0;
        }
    }
    
    private String getSuitableSubSentence(String appendedText, TextPaint textPaint, int offset)
    {
        int oneCharWidth = (int) Math.ceil(textPaint.measureText(appendedText.substring(offset, offset + 1)));
        
        bStartNewLine = false;
        
     	/*
     	 * the appendedText should be placed in a new line.
     	 */
        if (offset == 0 || oneCharWidth + currentLineWidth > pageWidth)
        {
        	bStartNewLine = true;
        }
     	
        /*
         * the left space of current line.
         */
     	int leftLineWidth = ((offset != 0 || bStartNewLine) ? pageWidth : pageWidth - currentLineWidth);
     	
     	/*
     	 * how many chars be able to be filled in the left space of current line.
     	 */
     	int leftCharNum = leftLineWidth / oneCharWidth;
     	int totalLen = appendedText.length();
     	int leftLen = totalLen - offset;
     	
     	/*
     	 * Only one char is left.
     	 */
     	if (leftLen == 1)
     	{
     		return appendedText.substring(offset,  offset + 1);
     	}
     	else 
     	{
     		int start = offset;
     		int end = totalLen;
    		int textWidth;
    		int textWidthR;
    		textWidth = (int) Math.ceil(textPaint.measureText(appendedText, start, end));
    		
    		/*
    		 * the left substring is not enough to fill one line.
    		 */
    		if (textWidth < leftLineWidth)
    		{
    			return appendedText.substring(offset);
    		}
    		
         	while (true)
        	{
         		leftCharNum = (end + start) / 2 - offset;
        		textWidth = (int) Math.ceil(textPaint.measureText(appendedText, offset, offset + leftCharNum));
        		textWidthR = (int) Math.ceil(textPaint.measureText(appendedText, offset, offset + leftCharNum + 1));
        		
        		if (textWidth <= leftLineWidth && textWidthR  > leftLineWidth)
        		{
        			break;
        		}
        		else if (textWidthR <= leftLineWidth)
        		{
        			start = offset + leftCharNum;
        		}
        		else
        		{
        			end =  offset  + leftCharNum;
        		}
        	}
     	}
    	
    	return appendedText.substring(offset,  offset + leftCharNum);
    }

    private void appendSentence(String appendedText, TextPaint textPaint) {
        int textWidth;
        String subSentence;
        
    	int offset = 0;
    	int len = appendedText.length();
        	
    	while (true)
    	{
           	subSentence = getSuitableSubSentence(appendedText, textPaint, offset);
    		textWidth = (int) Math.ceil(textPaint.measureText(subSentence));
        	
        	/*
        	 * if current line  already fully fill pageWidth, do as before. 
        	 */
        	if (bStartNewLine)
        	{
				appendTextToLine(subSentence, textPaint, textWidth);
				appendLineToPage(textLineHeight);
				checkForPageEnd();
        	}
        	else 
        	{
            	appendTextToLine(subSentence, textPaint, textWidth);
        	}
            	
        	/*
        	 * if the subsentence is the final part of appendedText, this appendedText handling should be finished.
        	 */
        	if (offset + subSentence.length() == len)
        	{
        		break;
        	}
        	
        	if (bStartNewLine == false)
        	{
                checkForPageEnd();
                appendLineToPage(textLineHeight);
        	}
            
        	offset += subSentence.length();
    	}
    }

    private void appendWord(String appendedText, TextPaint textPaint) {
        int textWidth = (int) Math.ceil(textPaint.measureText(appendedText));
        if (currentLineWidth + textWidth >= pageWidth) {
        	if (bNoSpaceLang)
        	{
            	appendSentence(appendedText, textPaint);
            	return;
        	}
        	
            checkForPageEnd();
            appendLineToPage(textLineHeight);
        }
        appendTextToLine(appendedText, textPaint, textWidth);
    }

    private void appendLineToPage(int textLineHeight) {
        currentPage.append(currentLine);
        pageContentHeight += currentLineHeight;

        currentLine = new SpannableStringBuilder();
        currentLineHeight = textLineHeight;
        currentLineWidth = 0;
    }

    private void appendTextToLine(String appendedText, TextPaint textPaint, int textWidth) {
        currentLineHeight = Math.max(currentLineHeight, textLineHeight);
        currentLine.append(renderToSpannable(appendedText, textPaint));
        currentLineWidth += textWidth;
    }

    public List<CharSequence> getPages() {
        List<CharSequence> copyPages = new ArrayList<CharSequence>(pages);
        SpannableStringBuilder lastPage = new SpannableStringBuilder(currentPage);
        if (pageContentHeight + currentLineHeight > pageHeight) {
            copyPages.add(lastPage);
            lastPage = new SpannableStringBuilder();
        }
        lastPage.append(currentLine);
        copyPages.add(lastPage);
        return copyPages;
    }

    private SpannableString renderToSpannable(String text, TextPaint textPaint) {
        SpannableString spannable = new SpannableString(text);

        if (textPaint.isFakeBoldText()) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), 0);
        }
        return spannable;
    }
}
