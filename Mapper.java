import java.io.IOException;
import java.io.Serializable;
import java.util.List;



public class Mapper implements MapReduceInterface, Serializable {
	
	/**
	 * Executes the mapping phase of the 
	 * chord class.
	 */
	public void map(Long key, String value, ChordMessageInterface context) throws IOException
	{
		context.emitMap(key, value);
	}
	/**
	 * Executes the reduce phase of the chord class
	 */
	public void reduce(Long key, List< String > value, ChordMessageInterface context) throws IOException
	{
		context.emitReduce(key, value.get(0) +":"+ value.size());
	}
}