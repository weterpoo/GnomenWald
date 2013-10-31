package World;

public class InsufficientCashException extends Exception
{
	final static String MESSAGE = "There was not enough cash. Sorry";
	public InsufficientCashException()
	{
		super();
	}
	
	public InsufficientCashException(String message)
	{  
        super(message);  
     }
	
	public String getMessage()
	{
		return MESSAGE;
	}
}
