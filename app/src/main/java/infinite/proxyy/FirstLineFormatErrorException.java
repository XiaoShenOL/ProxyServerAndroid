package infinite.proxyy;

/**
 * @author zyq 16-3-7
 */
public class FirstLineFormatErrorException extends Exception {

	public FirstLineFormatErrorException(String firstLine){
		super("Http请求头首行格式出错:"+firstLine);
	}
}
