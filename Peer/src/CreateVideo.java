import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class CreateVideo {
	public static void main(String args[]){
	int i=0, j=0;
	String[] lengths= {"4567", "4460", "7860"};
	
	
	String str;
	for(i=0; i<lengths.length; i++)
	{
		str=lengths[i];
		byte[] charArray = str.getBytes();
		System.out.println(charArray);
		
		BufferedWriter writer = null;
		try
		{
		    writer = new BufferedWriter( new FileWriter( "filename"+i+".txt"));
		    writer.write( charArray.toString());

		}
		catch ( IOException e)
		{
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close( );
		    }
		    catch ( IOException e)
		    {
		    }
		}
//		if(charArray.length<5)
//		{
//			for(j=0;j<charArray.length;j++)
//			charArray[j]=;
//			System.out.println(charArray[1]);
//			
//		}
		
	}
	
	}

}
