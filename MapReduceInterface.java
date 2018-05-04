import java.util.List;
import java.io.IOException;

//extends Serializable

public interface MapReduceInterface {

	public void map(Long key, String value, ChordMessageInterface context) throws IOException;
	public void reduce(Long key, List< String > value, ChordMessageInterface context) throws IOException;
	
	
}
