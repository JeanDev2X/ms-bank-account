package spring.boot.webflu.ms.cuenta.banco.app.exception;


public class RequestException extends RuntimeException {

	public RequestException(String message)
	{
	  super(message);
	}
	
	public RequestException(String message, Throwable cause)
	{
	  super(message, cause);
	}
	
	
}
