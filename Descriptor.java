import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Descriptor {

	int size;
	AtomicReference<TxStatus> status = new AtomicReference<TxStatus>();
	ArrayList<OperationNode> ops;
	
	Descriptor(){
		status.set(TxStatus.ACTIVE);
	}
}
