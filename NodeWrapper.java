import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

public class NodeWrapper {

	AtomicReference<NodeInfo> info;
	int key;
	AtomicMarkableReference<NodeInfo> markRef;
	NodeWrapper next;
	
	NodeWrapper(){
		this.info =  new AtomicReference<NodeInfo>();
		this.info.set(new NodeInfo());
		this.markRef = new AtomicMarkableReference<NodeInfo>(this.info.get(),false);
		this.next = null;
	}
}
