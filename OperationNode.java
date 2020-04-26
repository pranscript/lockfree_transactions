
public class OperationNode {
	
	int key;
	OpType operation;
	Node next;
	
	public OperationNode(int key, OpType operation){
		this.key = key;
		this.operation = OpType.values()[0];
	}

}
