package com.example.mengweather.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Pingyin {
	public static String getPingYin(String src) {  	  
		char[] t1 = null;  
		t1 = src.toCharArray();  
		String[] t2 = new String[t1.length];  
		HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();  

		t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);  
		t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  
		t3.setVCharType(HanyuPinyinVCharType.WITH_V);  
		String t4 = "";  
		int t0 = t1.length;  
		try {  
			for (int i = 0; i < t0; i++) {  
				// ÅÐ¶ÏÊÇ·ñÎªºº×Ö×Ö·û  
				if (java.lang.Character.toString(t1[i]).matches(  
						"[\\u4E00-\\u9FA5]+")) {  
					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);  
					t4 += t2[0];  
				} else  
					t4 += java.lang.Character.toString(t1[i]);  
			}  
			// System.out.println(t4);  
			return t4;  
		} catch (BadHanyuPinyinOutputFormatCombination e1) {  
			e1.printStackTrace();  
		}  
		return t4;  
	}  	

	public static int getimageID(String image_name){
		int imageID = 0;
		switch (image_name) {
		case "bingbao":
			imageID=0x7f020002;
			break;
		case "daxue":
			imageID=0x7f020003;
			break;
		case "dayu":
			imageID=0x7f020004;
			break;
		case "duoyun":
			imageID=0x7f020005;
			break;
		case "leizhenyu":
			imageID=0x7f020009;
			break;
		case "mai":
			imageID=0x7f02000a;
			break;
		case "qing":
			imageID=0x7f02000b;
			break;
		case "wu":
			imageID=0x7f020010;
			break;
		case "xiaoxue":
			imageID=0x7f020011;
			break;
		case "xiaoyu":
			imageID=0x7f020012;
			break;
		case "yin":
			imageID=0x7f020013;
			break;
		case "yujiaxue":
			imageID=0x7f020014;
			break;
		case "zhenyu":
			imageID=0x7f020015;
			break;
		case "zhongxue":
			imageID=0x7f020016;
			break;
		case "zhongyu":	
			imageID=0x7f020017;
			break;
		default:
			break;
		}
		return imageID;
	}
}



