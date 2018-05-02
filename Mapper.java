import java.io.IOException;
import java.util.List;



public class Mapper implements MapReduceInterface {
	
	public void map(Long key, String value, ChordMessageInterface context) throws IOException
	{
		context.emitMap(key, value);
	}
	public void reduce(Long key, List< String > value, ChordMessageInterface context) throws IOException
	{
		context.emitReduce(key, word +":"+ value.size());
	}
}