package sitzung;

public class UserAgentDetection {
	
	final static int FIREFOX = 1, CHROME = 2, INTERNET_EXPLORER = 3, EDGE = 4, SAFARI = 5, BROWSER_OTHERS = 0;
	final public static String[] BROWSERS = {"andere","Firefox","Google Chrome","Internet Explorer","Edge","Safari"};
	
	final static int WINDOWS_XP = 1, WINDOWS_7 = 2, WINDOWS_8 = 3, WINDOWS_10 = 4, MAC_OS = 5, LINUX = 6, IOS = 7, ANDROID = 8, OS_OTHERS = 0;
	final public static String[] OS = {"andere","Windows XP","Windows 7","Windows 8","Windows 10","Mac OS","Linux","iOS","Android"};
	
	//bitte userAgent.toLowerCase vorher!
	static public int getBrowser(String userAgent) {
		
		if(userAgent.contains("edge")) {
			return EDGE;
		}
		
		if(userAgent.contains("firefox")) {
			return FIREFOX;
		}
		
		if(userAgent.contains("chrome")) {
			return CHROME;
		}
		
		if(userAgent.contains("msie") || userAgent.contains("trident")) {
			return INTERNET_EXPLORER;
		}
		
		if(userAgent.contains("safari") || userAgent.contains("applewebkit")) {
			return SAFARI;
		}
		
		return BROWSER_OTHERS;
	}
	
	static public int getOS(String userAgent) {
		if(userAgent.contains("windows nt 5.1") || userAgent.contains("windows nt 5.2")) {
			return WINDOWS_XP;
		}
		
		if(userAgent.contains("windows nt 6.1")) {
			return WINDOWS_7;
		}
		
		if(userAgent.contains("windows nt 6.2") || userAgent.contains("windows nt 6.3")) {
			return WINDOWS_8;
		}
		
		if(userAgent.contains("windows nt 10.0")) {
			return WINDOWS_10;
		}
		
		if(userAgent.contains("macintosh")) {
			return MAC_OS;
		}
		
		if(userAgent.contains("android")) {
			return ANDROID;
		}
		
		if(userAgent.contains("linux")) {
			return LINUX;
		}
		
		if(userAgent.contains("iphone") || userAgent.contains("ipad")) {
			return IOS;
		}
		
		return OS_OTHERS;
	}
	
	static public boolean isMobile(String userAgent) {
		if((userAgent.contains("android") ||
				userAgent.contains("iphone") ||
				userAgent.contains("phone") || //windows phone
				userAgent.contains("blackberry") ||
				userAgent.contains("mobile")) //andere
				&& !userAgent.contains("ipad")) //kein ipad
			return true;
		else
			return false;
	}
	
	static public boolean isBot(String userAgent) {
		return userAgent.contains("googlebot") ||
				userAgent.contains("bingbot") ||
				userAgent.contains("dotbot") ||
				userAgent.contains("mj12bot") ||
				userAgent.contains("slurp") ||
				userAgent.contains("baiduspider") ||
				userAgent.contains("yandexbot")|| 
				userAgent.contains("xovibot") ||
				userAgent.contains("ahrefsbot") ||
				userAgent.contains("semrushbot") ||
				userAgent.contains("lyncautodiscover") ||
				userAgent.contains("zoominfobot");
	}

}
