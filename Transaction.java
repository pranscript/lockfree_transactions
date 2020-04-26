
import java.util.ArrayList;
import java.util.Random;

public class Transaction {
	
	ArrayList<OperationNode> node = new ArrayList<OperationNode>();
	NodeWrapper[] nodeWrapper;
	
	
	Transaction(int len){
		System.out.println("transaction");
		this.node = new ArrayList<OperationNode>(len);
		this.nodeWrapper = new NodeWrapper[len];
		System.out.println("len - "+len);
		for(int i=0;i<len;i++) {
			Random r = new Random();
			int randKey =r.nextInt(100-1) + 1;
			OpType randOperation = OpType.getRandomColor();
			this.node.add(new OperationNode(randKey, randOperation));
			System.out.println("operation" + node.get(i).operation);
			nodeWrapper[i] = new NodeWrapper();
			//nodeWrapper[i].key = (int) Instant.now().getEpochSecond();
			nodeWrapper[i].key = randKey;//(int) Instant.now().getNano();
			System.out.println("yes - " + nodeWrapper[i].key);
			nodeWrapper[i].info.get().opID = i;
			System.out.println("no");
		}
		
		for(int i=0;i<len;i++) {
			nodeWrapper[i].info.get().desc.ops = node;
			nodeWrapper[i].info.get().desc.size = node.size();
		}
		
	}
	
}
