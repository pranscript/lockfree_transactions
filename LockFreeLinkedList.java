
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeLinkedList {
	
	static AtomicReference<NodeWrapper> head = new AtomicReference<NodeWrapper>(new NodeWrapper());
	static AtomicReference<NodeWrapper> tail = new AtomicReference<NodeWrapper>(new NodeWrapper());
	
	static LockFreeLinkedList obj = new LockFreeLinkedList();
	static Execution execute = new Execution();
	
	public static void main(String[] args) {
		
		tail.set(head.get());
		for(int i=0;i<5;i++) {
			obj.createLinkedList();
		}
		//NodeWrapper curr1 = head.get().next;
		//NodeWrapper curr2 = tail.get();
		/*while(curr1!=null) {
			System.out.println("print - "+curr1.key);
			curr1=curr1.next;
		}*/
		ThreadPoolExecutor ex = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		for(int i=0;i<10;i++) {	
			ex.execute(obj.new ThreadObj());
		}
	}
	
	public void createLinkedList() {
		int randLen = 2;
		//Random r = new Random();
		//int randKey =r.nextInt(100-1) + 1;
		//Node node = new Node(randKey);
		execute.create(new Transaction(randLen));
		/*if(head.get()==null) {
			head.set(node);
			tail.set(node);
		}
		else {
			tail.get().next=node;
			tail.set(node);
		}*/
		
	}

	class ThreadObj extends Thread{
		public void run() {
			int randLen = 2;
			execute.create(new Transaction(randLen));
		}
	}
}
