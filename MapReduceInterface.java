import java.util.List;
import java.io.IOException;


public interface MapReduceInterface {

	public void map(Long key, String value, ChordMessageInterface context) throws IOException;
	public void reduce(Long key, List< String > value, ChordMessageInterface context) throws IOException;
	
	
}
