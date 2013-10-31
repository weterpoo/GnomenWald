package World;

public class PathNotFoundException extends Exception
{
	final static String MESSAGE = "The Path was not found. Sorry";
	
	public PathNotFoundException()
	{
		super();
	}
	
	public PathNotFoundException(String message)
	{  
        super(message);  
     }
	
	public String getMessage()
	{
		return MESSAGE;
	}
}
